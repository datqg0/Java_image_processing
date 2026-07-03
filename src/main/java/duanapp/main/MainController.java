package duanapp.main;

import javafx.animation.FadeTransition;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Main controller for the image editing view (main-view.fxml).
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Manage the currently displayed image ({@link #currentImage})</li>
 *   <li>Delegate pure image processing to {@link ImageProcessor}</li>
 *   <li>Delegate undo/redo state to {@link ImageHistory}</li>
 *   <li>Handle user interactions (draw, crop, brightness, filters, text, insert)</li>
 * </ul>
 */
public class MainController {

    // =========================================================================
    // OpenCV native library — path read from .env (OPENCV_LIB_PATH)
    // =========================================================================
    static {
        String opencvLibPath = loadEnv("OPENCV_LIB_PATH");
        try {
            if (!opencvLibPath.isEmpty()) {
                // In modern Java, modifying java.library.path at runtime via System.setProperty
                // has no effect. We must use System.load() with the absolute path to the DLL/SO.
                String libName = System.mapLibraryName(Core.NATIVE_LIBRARY_NAME);
                File libFile = new File(opencvLibPath, libName);
                if (libFile.exists()) {
                    System.load(libFile.getAbsolutePath());
                } else {
                    // Fallback to loadLibrary if the exact file wasn't found (maybe path is wrong)
                    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
                }
            } else {
                // If no env variable is set, rely on JVM launch arguments
                System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            }
        } catch (UnsatisfiedLinkError e) {
            System.err.println("[MainController] Không tải được OpenCV native library.");
            System.err.println("  → Hãy kiểm tra lại OPENCV_LIB_PATH trong file .env");
            System.err.println("  → Lỗi chi tiết: " + e.getMessage());
        }
    }

    // =========================================================================
    // Constants
    // =========================================================================

    /** Loaded from .env at startup; falls back to empty string if not found. */
    private static final String REMOVE_BG_API_URL = "https://api.remove.bg/v1.0/removebg";
    private static final String REMOVE_BG_API_KEY = loadEnv("REMOVE_BG_API_KEY");

    /** Working directory — used as the base for all relative file paths. */
    private static final Path WORK_DIR = Paths.get(System.getProperty("user.dir"));

    /** Path used when no image has been opened yet. */
    public static String defaultImagePath = "";

    // =========================================================================
    // FXML fields — injected by FXMLLoader
    // =========================================================================

    @FXML private ImageView mainImageView;
    /** Bottom-bar thumbnails. */
    @FXML private ImageView thumbOriginal;
    @FXML private ImageView thumbCurrent;

    // Toolbar sub-menus / option rows shown contextually
    @FXML private HBox cropOptionsMenu;
    @FXML private HBox new_text;
    @FXML private MenuButton resizeOptionsMenu;
    @FXML private MenuButton Flip_and_rotate;
    @FXML private MenuButton fliterOptionsMenu;
    @FXML private MenuButton insertOptionsMenu;
    @FXML private HBox draw1;
    @FXML private HBox draw2;
    @FXML private HBox draw4;
    @FXML private HBox brightness1;
    @FXML private HBox brightness2;
    @FXML private HBox brightness3;

    // Drawing controls (also injected from FXML)
    @FXML private ColorPicker colorPicker = new ColorPicker();
    @FXML Button draw9;
    @FXML Slider getsize       = new Slider();   // pen-size slider (fx:id="getsize")
    @FXML Slider opacity       = new Slider();   // opacity slider  (fx:id="opacity")
    @FXML Slider briness       = new Slider();   // brightness slider (fx:id="briness")

    // =========================================================================
    // State
    // =========================================================================

    /** The Mat that is currently being edited. */
    public Mat currentImage = Imgcodecs.imread(defaultImagePath, Imgcodecs.IMREAD_UNCHANGED);

    /** Snapshot of the image right after opening (used by the eraser tool). */
    private Mat originalSnapshot = currentImage.clone();

    /** Undo / redo history. */
    private final ImageHistory history = new ImageHistory();

    /** Writable image kept in sync with {@link #currentImage} for fast pixel edits. */
    private WritableImage writableImage = ImageProcessor.matToWritableImage(currentImage);

    // -- Drawing state --
    private int   penRed = 0, penGreen = 0, penBlue = 0;
    private double penSize    = 2.0;
    private double penOpacity = 1.0;
    private int   imageScale  = 1;
    private boolean eraserActive = false;

    // -- Brightness state --
    private int brightnessLevel = 0;

    // -- Crop state (all -1 = no selection) --
    private int cropStartRow = -1, cropStartCol = -1;
    private int cropEndRow   = -1, cropEndCol   = -1;
    // Anchor point of the crop drag (set on mousePressed, never mutated during drag)
    private int initialCropRow = -1, initialCropCol = -1;

    /** A pending cropped mat, committed when the user presses "Lưu". */
    private Mat pendingCrop = currentImage.clone();

    // -- Insert-image state --
    private Mat insertOverlay   = currentImage.clone(); // the sticker image
    private Mat insertedPreview = currentImage.clone(); // base + sticker preview

    // -- Insert-text state --
    private String insertText     = "G";
    private double insertTextSize = 10;
    private String insertTextFont = "Arial";

    /** All expandable tool-option regions; hidden when a new tool is selected. */
    private final List<Region> toolOptionPanels = new ArrayList<>();

    // =========================================================================
    // Initialisation
    // =========================================================================

    @FXML
    public void initialize() {
        refreshImageView();
        saveToHistory();

        toolOptionPanels.add(draw9);
        toolOptionPanels.add(Flip_and_rotate);
        toolOptionPanels.add(new_text);
        toolOptionPanels.add(cropOptionsMenu);
        toolOptionPanels.add(resizeOptionsMenu);
        toolOptionPanels.add(fliterOptionsMenu);
        toolOptionPanels.add(insertOptionsMenu);
        toolOptionPanels.add(draw1);
        toolOptionPanels.add(draw2);
        toolOptionPanels.add(draw4);
        toolOptionPanels.add(brightness1);
        toolOptionPanels.add(brightness2);
        toolOptionPanels.add(brightness3);

        colorPicker.setValue(Color.BLACK);
        resetToolbars();
    }

    // =========================================================================
    // History helpers
    // =========================================================================

    /** Saves a clone of the current image into the undo/redo history. */
    public void saveToHistory() {
        history.push(currentImage);
    }

    /** Refreshes the JavaFX ImageView and the current-state thumbnail with the current Mat. */
    public void refreshImageView() {
        writableImage = ImageProcessor.matToWritableImage(currentImage);
        mainImageView.setImage(writableImage);
        if (thumbCurrent != null) thumbCurrent.setImage(writableImage);
    }

    // =========================================================================
    // Toolbar / tool-selection helpers
    // =========================================================================

    /** Hides all contextual option rows and removes mouse handlers. */
    private void resetToolbars() {
        clearMouseHandlers();
        toolOptionPanels.forEach(panel -> panel.setVisible(false));
    }

    /** Removes any drag/press/release handlers from the main ImageView. */
    private void clearMouseHandlers() {
        mainImageView.setOnMousePressed(null);
        mainImageView.setOnMouseDragged(null);
        mainImageView.setOnMouseReleased(null);
        mainImageView.setCursor(Cursor.DEFAULT);
    }

    // =========================================================================
    // File actions
    // =========================================================================

    @FXML
    protected void handleOpenImage() {
        resetToolbars();
        if (history.isEmpty()) {
            history.push(currentImage);
        }

        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File file = chooser.showOpenDialog(mainImageView.getScene().getWindow());

        if (file != null) {
            currentImage     = Imgcodecs.imread(file.getAbsolutePath(), Imgcodecs.IMREAD_UNCHANGED);
            originalSnapshot = currentImage.clone();
            // Update the "original" thumbnail once per file load
            if (thumbOriginal != null) {
                thumbOriginal.setImage(ImageProcessor.matToWritableImage(currentImage));
            }
            saveToHistory();
            refreshImageView();
        }
    }

    @FXML
    protected void handleSave() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Chọn nơi lưu ảnh và chỉnh tên");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        chooser.setInitialFileName("output_image.png");

        File file = chooser.showSaveDialog(mainImageView.getScene().getWindow());
        if (file == null) {
            System.out.println("Người dùng không chọn nơi lưu!");
            return;
        }

        boolean saved = Imgcodecs.imwrite(file.getAbsolutePath(), currentImage);
        if (saved) {
            System.out.println("Ảnh đã được lưu tại: " + file.getAbsolutePath());
        } else {
            showAlert("Không thể lưu ảnh!");
        }
    }

    // =========================================================================
    // Undo / Redo
    // =========================================================================

    @FXML
    protected void handleUndo() {
        resetToolbars();
        history.undo().ifPresent(mat -> {
            currentImage = mat;
            refreshImageView();
        });
    }

    @FXML
    protected void handleRedo() {
        resetToolbars();
        history.redo().ifPresent(mat -> {
            currentImage = mat;
            refreshImageView();
        });
    }

    // handleRedo was previously wired to the "undo" toolbar button; keep the
    // old alias so existing FXML wiring still works.
    @FXML
    protected void handleNext() {
        handleRedo();
    }

    // =========================================================================
    // Resize
    // =========================================================================

    @FXML
    protected void handleResize() {
        resetToolbars();

        Label widthLabel  = new Label("Width now : "  + currentImage.cols());
        Label heightLabel = new Label("Height now : " + currentImage.rows());
        Label notifyLabel = new Label();

        TextField widthField  = new TextField();
        widthField.setPromptText("Width :");
        TextField heightField = new TextField();
        heightField.setPromptText("Height :");

        Button resizeBtn = new Button("Resize");
        resizeBtn.setOnAction(event -> {
            try {
                double w = Double.parseDouble(widthField.getText());
                double h = Double.parseDouble(heightField.getText());
                Imgproc.resize(currentImage, currentImage, new Size(w, h),
                               0, 0, Imgproc.INTER_AREA);
                widthLabel.setText("Width now: "  + currentImage.cols());
                heightLabel.setText("Height now: " + currentImage.rows());
                notifyLabel.setText("Thành công!");
                notifyLabel.setTextFill(Color.GREEN);
                saveToHistory();
                refreshImageView();
            } catch (NumberFormatException ex) {
                notifyLabel.setText("Kích thước không hợp lệ!");
                notifyLabel.setTextFill(Color.RED);
            }
        });

        VBox layout = new VBox(10, widthLabel, heightLabel, widthField, heightField, resizeBtn, notifyLabel);
        layout.setPadding(new Insets(30));
        Stage stage = new Stage();
        stage.setTitle("Thay đổi kích thước");
        stage.setScene(new Scene(layout, 400, 300));
        stage.show();
    }

    // =========================================================================
    // Flip & Rotate
    // =========================================================================

    @FXML
    protected void handleflip_and_rotate() {
        resetToolbars();
        Flip_and_rotate.setVisible(true);
    }

    @FXML
    protected void left_rot() {
        Core.rotate(currentImage, currentImage, Core.ROTATE_90_COUNTERCLOCKWISE);
        saveToHistory();
        refreshImageView();
    }

    @FXML
    protected void right_rot() {
        Core.rotate(currentImage, currentImage, Core.ROTATE_90_CLOCKWISE);
        saveToHistory();
        refreshImageView();
    }

    @FXML
    protected void flip_h() {
        Core.flip(currentImage, currentImage, 0);
        saveToHistory();
        refreshImageView();
    }

    @FXML
    protected void flip_v() {
        Core.flip(currentImage, currentImage, 1);
        saveToHistory();
        refreshImageView();
    }

    // =========================================================================
    // Filters
    // =========================================================================

    @FXML
    protected void handleFilter() {
        resetToolbars();

        Button invertBtn  = new Button("Âm bản");
        Button bwBtn      = new Button("Đen trắng");
        Button blurBtn    = new Button("Làm mịn ảnh");
        Button undoBtn    = new Button("Trở lại");

        invertBtn.setOnAction(e -> {
            currentImage = ImageProcessor.invertColors(currentImage);
            saveToHistory();
            refreshImageView();
        });
        bwBtn.setOnAction(e -> {
            currentImage = ImageProcessor.toGrayscale(currentImage);
            saveToHistory();
            refreshImageView();
        });
        blurBtn.setOnAction(e -> {
            currentImage = ImageProcessor.applyMedianBlur(currentImage);
            saveToHistory();
            refreshImageView();
        });
        undoBtn.setOnAction(e -> handleUndo());

        VBox layout = new VBox(10, invertBtn, bwBtn, blurBtn, undoBtn);
        layout.setPadding(new Insets(20));
        Stage stage = new Stage();
        stage.setTitle("Bộ lọc");
        stage.setScene(new Scene(layout, 200, 220));
        stage.show();
    }

    // =========================================================================
    // Brightness adjustment
    // =========================================================================

    @FXML
    protected void handleAdjust() {
        resetToolbars();
        brightness1.setVisible(true);

        briness.setMin(-50);
        briness.setMax(50);
        briness.setValue(0);
        briness.setBlockIncrement(2);
        briness.setMajorTickUnit(2);

        briness.valueProperty().addListener((obs, oldVal, newVal) -> {
            brightnessLevel = newVal.intValue();
            previewBrightness();
        });
    }

    /**
     * Applies the current {@link #brightnessLevel} to a temporary clone and
     * shows it in the ImageView (non-destructive preview).
     */
    private void previewBrightness() {
        Mat preview = ImageProcessor.applyBrightness(currentImage, brightnessLevel);
        mainImageView.setImage(ImageProcessor.matToWritableImage(preview));
    }

    /** Commits the brightness change to {@link #currentImage}. Called by "Lưu" button in FXML. */
    @FXML
    public void applyBrightness() {
        currentImage = ImageProcessor.applyBrightness(currentImage, brightnessLevel);
        brightnessLevel = 0;
        saveToHistory();
        refreshImageView();
    }

    // Keep old FXML binding name working
    @FXML
    public void save_image() {
        applyBrightness();
    }

    // =========================================================================
    // Drawing tool
    // =========================================================================

    @FXML
    protected void change_erase() {
        eraserActive = !eraserActive;
        draw9.setText(eraserActive ? "Bỏ Tẩy" : "Tẩy");
    }

    @FXML
    private void handleColorChange() {
        Color selected = colorPicker.getValue();
        penRed   = (int) (selected.getRed()   * 255);
        penGreen = (int) (selected.getGreen() * 255);
        penBlue  = (int) (selected.getBlue()  * 255);
    }

    @FXML
    protected void handleDraw() {
        resetToolbars();
        refreshImageView();
        mainImageView.setCursor(Cursor.CROSSHAIR);

        // Pen-size slider
        getsize.setMin(1);
        getsize.setMax(10);
        getsize.setValue(2);
        getsize.setBlockIncrement(0.5);
        getsize.setMajorTickUnit(0.5);

        // Opacity slider
        opacity.setMin(0);
        opacity.setMax(1);
        opacity.setValue(1);
        opacity.setBlockIncrement(0.1);
        opacity.setMajorTickUnit(0.1);

        penSize    = getsize.getValue();
        penOpacity = opacity.getValue();

        // Show toolbar rows
        draw1.setVisible(true);
        draw2.setVisible(true);
        draw4.setVisible(true);
        draw9.setVisible(true);

        PixelWriter pixelWriter = writableImage.getPixelWriter();

        getsize.valueProperty().addListener((obs, oldVal, newVal) -> penSize = newVal.doubleValue());
        opacity.valueProperty().addListener((obs, oldVal, newVal) -> penOpacity = newVal.doubleValue());

        mainImageView.setOnMouseDragged(event -> {
            double viewW = mainImageView.getFitWidth();
            double viewH = mainImageView.getFitHeight();
            double matW  = currentImage.cols();
            double matH  = currentImage.rows();

            double scaleX = viewW / matW;
            double scaleY = viewH / matH;
            double scale  = Math.min(scaleX, scaleY);

            imageScale = Math.max(1, Math.max((int) Math.round(scaleX), (int) Math.round(scaleY)));
            int brushRadius = (int) (imageScale * penSize);

            // Subtract the blank margin created by preserveRatio centering
            double[] offset = computeImageOffset();
            int centerX = (int) ((event.getX() - offset[0]) / scale);
            int centerY = (int) ((event.getY() - offset[1]) / scale);

            for (int px = centerX - brushRadius; px < Math.min(centerX + brushRadius, matW); px++) {
                for (int py = centerY - brushRadius; py < Math.min(centerY + brushRadius, matH); py++) {
                    if (px < 0 || py < 0 || px >= matW || py >= matH) continue;

                    if (!eraserActive) {
                        // Draw with current colour + opacity
                        double[] pixel = currentImage.get(py, px).clone();
                        pixel[0] = penBlue  * penOpacity + (1 - penOpacity) * 255;
                        pixel[1] = penGreen * penOpacity + (1 - penOpacity) * 255;
                        pixel[2] = penRed   * penOpacity + (1 - penOpacity) * 255;
                        currentImage.put(py, px, pixel);
                        pixelWriter.setColor(px, py, Color.rgb(penRed, penGreen, penBlue, penOpacity));
                    } else {
                        // Erase: restore from originalSnapshot if dimensions match, else white
                        double[] pixel = currentImage.get(py, px).clone();
                        if (originalSnapshot.cols() == currentImage.cols()
                                && originalSnapshot.rows() == currentImage.rows()) {
                            double[] orig = originalSnapshot.get(py, px);
                            pixel[0] = orig[0];
                            pixel[1] = orig[1];
                            pixel[2] = orig[2];
                            currentImage.put(py, px, pixel);
                            pixelWriter.setColor(px, py,
                                    Color.rgb((int) orig[2], (int) orig[1], (int) orig[0]));
                        } else {
                            pixel[0] = pixel[1] = pixel[2] = 255;
                            currentImage.put(py, px, pixel);
                            pixelWriter.setColor(px, py, Color.WHITE);
                        }
                    }
                    mainImageView.setImage(writableImage);
                }
            }
        });

        mainImageView.setOnMouseReleased(event -> saveToHistory());
    }

    // =========================================================================
    // Crop
    // =========================================================================

    @FXML
    protected void handleCrop() {
        resetToolbars();
        cropOptionsMenu.setVisible(true);

        mainImageView.setOnMousePressed(event -> {
            double scale = computeScale();
            double[] offset = computeImageOffset();
            initialCropRow = (int) ((event.getY() - offset[1]) / scale);
            initialCropCol = (int) ((event.getX() - offset[0]) / scale);
            // Clamp to image bounds
            initialCropRow = Math.max(0, Math.min(initialCropRow, currentImage.rows() - 1));
            initialCropCol = Math.max(0, Math.min(initialCropCol, currentImage.cols() - 1));
            cropStartRow = initialCropRow;
            cropStartCol = initialCropCol;
            cropEndRow   = initialCropRow;
            cropEndCol   = initialCropCol;
        });

        mainImageView.setOnMouseDragged(event -> {
            double scale = computeScale();
            double[] offset = computeImageOffset();
            int curRow = (int) ((event.getY() - offset[1]) / scale);
            int curCol = (int) ((event.getX() - offset[0]) / scale);
            // Clamp to image bounds
            curRow = Math.max(0, Math.min(curRow, currentImage.rows() - 1));
            curCol = Math.max(0, Math.min(curCol, currentImage.cols() - 1));

            // Re-compute start/end from the fixed anchor + current position
            cropStartRow = Math.min(initialCropRow, curRow);
            cropStartCol = Math.min(initialCropCol, curCol);
            cropEndRow   = Math.max(initialCropRow, curRow);
            cropEndCol   = Math.max(initialCropCol, curCol);

            Mat preview = ImageProcessor.buildCropPreview(
                    currentImage, cropStartRow, cropStartCol, cropEndRow, cropEndCol);
            mainImageView.setImage(ImageProcessor.matToWritableImage(preview));
        });

        mainImageView.setOnMouseReleased(event -> {
            boolean valid = cropStartRow >= 0 && cropStartCol >= 0
                    && cropEndRow > cropStartRow && cropEndCol > cropStartCol
                    && (cropEndCol - cropStartCol) <= currentImage.cols()
                    && (cropEndRow - cropStartRow) <= currentImage.rows();

            if (valid) {
                Rect roi = new Rect(cropStartCol, cropStartRow,
                                    cropEndCol - cropStartCol,
                                    cropEndRow  - cropStartRow);
                pendingCrop = new Mat(currentImage, roi);
            } else {
                showAlert("Vùng cắt không hợp lệ!");
            }
            cropStartRow = cropStartCol = cropEndRow = cropEndCol = -1;
            initialCropRow = initialCropCol = -1;
        });
    }

    @FXML
    public void save_crop() {
        currentImage = pendingCrop.clone();
        saveToHistory();
        refreshImageView();
    }

    // =========================================================================
    // Insert text
    // =========================================================================

    @FXML
    protected void input_string() {
        Label guide1 = new Label("Nhập text:");
        Label guide2 = new Label("Cỡ chữ:");
        Label guide3 = new Label("Phông chữ:");

        TextField textField = new TextField(insertText);
        TextField sizeField = new TextField(Double.toString(insertTextSize));

        ComboBox<String> fontBox = new ComboBox<>();
        fontBox.getItems().addAll("Arial", "Times New Roman", "Roboto", "Comic Sans MS");
        fontBox.setValue(insertTextFont);

        Button confirmBtn = new Button("Xác nhận");
        Stage stage = new Stage();
        confirmBtn.setOnAction(e -> {
            try {
                insertTextFont = fontBox.getValue();
                insertText     = textField.getText();
                insertTextSize = Double.parseDouble(sizeField.getText());
                stage.close();
            } catch (NumberFormatException ex) {
                showAlert("Cỡ chữ không hợp lệ!");
            }
        });

        VBox layout = new VBox(10, guide1, textField, guide2, sizeField, guide3, fontBox, confirmBtn);
        layout.setPadding(new Insets(20));
        stage.setTitle("Cài đặt văn bản");
        stage.setScene(new Scene(layout, 400, 320));
        stage.show();
    }

    @FXML
    protected void handleInsert() {
        resetToolbars();
        new_text.setVisible(true);
        draw4.setVisible(true);

        mainImageView.setOnMousePressed(event -> {
            double scale  = computeScale();
            double[] offset = computeImageOffset();
            int clickCol  = (int) ((event.getX() - offset[0]) / scale);
            int clickRow  = (int) ((event.getY() - offset[1]) / scale);
            // Clamp to image bounds
            clickCol = Math.max(0, Math.min(clickCol, currentImage.cols() - 1));
            clickRow = Math.max(0, Math.min(clickRow, currentImage.rows() - 1));

            BufferedImage buffImage = ImageProcessor.matToBufferedImage(currentImage);
            Graphics2D g2d = buffImage.createGraphics();
            Font font = new Font(insertTextFont, Font.PLAIN, (int) insertTextSize);
            g2d.setFont(font);
            g2d.setColor(new java.awt.Color(penRed, penGreen, penBlue));
            g2d.drawString(insertText, clickCol, clickRow);
            g2d.dispose();

            currentImage = ImageProcessor.bufferedImageToMat(buffImage, currentImage).clone();
            saveToHistory();
            refreshImageView();
        });
    }

    // =========================================================================
    // Insert overlay image
    // =========================================================================

    @FXML
    protected void handleInsertimage() {
        resetToolbars();
        brightness2.setVisible(true);

        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File file = chooser.showOpenDialog(mainImageView.getScene().getWindow());
        if (file != null) {
            insertOverlay = Imgcodecs.imread(file.getAbsolutePath(), Imgcodecs.IMREAD_UNCHANGED);
        }

        mainImageView.setOnMousePressed(event -> {
            insertedPreview = currentImage.clone();
            double scale = computeScale();
            double[] offset = computeImageOffset();
            int clickCol = (int) ((event.getX() - offset[0]) / scale);
            int clickRow = (int) ((event.getY() - offset[1]) / scale);
            // Clamp to image bounds
            clickCol = Math.max(0, Math.min(clickCol, currentImage.cols() - 1));
            clickRow = Math.max(0, Math.min(clickRow, currentImage.rows() - 1));

            insertedPreview = blendOverlay(insertedPreview, insertOverlay, clickRow, clickCol);
            mainImageView.setImage(ImageProcessor.matToWritableImage(insertedPreview));
        });
    }

    @FXML
    public void save_insert_image() {
        currentImage = insertedPreview.clone();
        saveToHistory();
        refreshImageView();
    }

    /**
     * Blends {@code overlay} onto {@code base} centred at ({@code centreRow}, {@code centreCol}).
     * Respects the alpha channel of the overlay when present.
     */
    private Mat blendOverlay(Mat base, Mat overlay, int centreRow, int centreCol) {
        Mat result   = base.clone();
        int overlayRows = overlay.rows();
        int overlayCols = overlay.cols();
        int channels    = overlay.channels();

        int startRow = centreRow - overlayRows / 2;
        int startCol = centreCol - overlayCols / 2;

        for (int oi = 0; oi < overlayRows; oi++) {
            for (int oj = 0; oj < overlayCols; oj++) {
                int bi = startRow + oi;
                int bj = startCol + oj;
                if (bi < 0 || bj < 0 || bi >= base.rows() || bj >= base.cols()) continue;

                double[] overlayPixel = overlay.get(oi, oj);
                if (channels == 4 && overlayPixel[3] == 0) continue; // transparent

                double[] basePixel = result.get(bi, bj).clone();
                basePixel[0] = overlayPixel[0];
                basePixel[1] = overlayPixel[1];
                basePixel[2] = overlayPixel[2];
                result.put(bi, bj, basePixel);
            }
        }
        return result;
    }

    // =========================================================================
    // AI background removal (remove.bg)
    // =========================================================================

    @FXML
    protected void AI() {
        resetToolbars();

        if (REMOVE_BG_API_KEY.isEmpty()) {
            showAlert("API key chưa được cấu hình. Vui lòng điền vào file .env.");
            return;
        }
        if (currentImage == null || currentImage.empty()) {
            showAlert("Không có ảnh để xử lý!");
            return;
        }

        try {
            // Export currentImage to a temporary file so we can POST it
            Path tempInput = Files.createTempFile("removebg_in_", ".png");
            Imgcodecs.imwrite(tempInput.toString(), currentImage);

            try (CloseableHttpClient client = HttpClients.createDefault()) {
                HttpPost post = new HttpPost(REMOVE_BG_API_URL);
                post.addHeader("X-Api-Key", REMOVE_BG_API_KEY);

                MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                builder.addBinaryBody("image_file", tempInput.toFile());
                builder.addTextBody("size", "auto");
                post.setEntity(builder.build());

                HttpResponse response = client.execute(post);
                HttpEntity entity = response.getEntity();

                int statusCode = response.getStatusLine().getStatusCode();
                System.out.println("remove.bg status: " + statusCode);

                Path outputPath = WORK_DIR.resolve("unscreen.png");
                try (InputStream in = entity.getContent();
                     FileOutputStream out = new FileOutputStream(outputPath.toFile())) {
                    in.transferTo(out);
                }

                if (statusCode == 200) {
                    currentImage = Imgcodecs.imread(outputPath.toString(), Imgcodecs.IMREAD_UNCHANGED);
                    saveToHistory();
                    refreshImageView();
                } else {
                    showAlert("remove.bg trả về lỗi (HTTP " + statusCode + "). Kiểm tra API key và hạn mức.");
                }
            } finally {
                Files.deleteIfExists(tempInput);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi khi gọi API remove.bg: " + e.getMessage());
        }
    }

    // =========================================================================
    // Navigation — back to project view
    // =========================================================================

    @FXML
    protected void handleExit() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/duanapp/main/projects.fxml"));
            Scene newScene = new Scene(loader.load());
            Stage stage    = (Stage) mainImageView.getScene().getWindow();

            FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), stage.getScene().getRoot());
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> {
                stage.setScene(newScene);
                FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), newScene.getRoot());
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            });
            fadeOut.play();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Lỗi khi chuyển về màn hình dự án!");
        }
    }

    // =========================================================================
    // Private utilities
    // =========================================================================

    /**
     * Computes the display scale factor (image-pixels per view-pixel) for the
     * current ImageView size and image dimensions.
     */
    private double computeScale() {
        double scaleX = mainImageView.getFitWidth()  / currentImage.cols();
        double scaleY = mainImageView.getFitHeight() / currentImage.rows();
        return Math.min(scaleX, scaleY);
    }

    /**
     * Computes the top-left offset (in view pixels) of the actual image content
     * inside the ImageView when {@code preserveRatio} is true.
     *
     * <p>JavaFX centres the image within the bounding box, creating blank margins
     * that shift the image away from (0,0) of the ImageView. Without subtracting
     * these offsets, mouse-event coordinates are wrong.
     *
     * @return double[]{offsetX, offsetY}
     */
    private double[] computeImageOffset() {
        double scale  = computeScale();
        double imageDisplayWidth  = currentImage.cols() * scale;
        double imageDisplayHeight = currentImage.rows() * scale;
        double offsetX = (mainImageView.getFitWidth()  - imageDisplayWidth)  / 2.0;
        double offsetY = (mainImageView.getFitHeight() - imageDisplayHeight) / 2.0;
        // Offsets are never negative (image never exceeds the fitWidth/fitHeight)
        return new double[]{ Math.max(0, offsetX), Math.max(0, offsetY) };
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Reads a single key from the {@code .env} file located in the working
     * directory.  Returns an empty string if the file or key is not found.
     *
     * <p>Safe to call from a {@code static{}} initialiser because it resolves
     * the path via {@code System.getProperty("user.dir")} at call-time rather
     * than relying on a class-level constant.
     *
     * @param key the key to look up (e.g. {@code "REMOVE_BG_API_KEY"})
     * @return the trimmed value, or {@code ""} if absent
     */
    static String loadEnv(String key) {
        Path envFile = Paths.get(System.getProperty("user.dir"), ".env");
        if (!Files.exists(envFile)) return "";
        try {
            String prefix = key + "=";
            for (String line : Files.readAllLines(envFile)) {
                String trimmed = line.trim();
                if (trimmed.startsWith("#") || trimmed.isEmpty()) continue;
                if (trimmed.startsWith(prefix)) {
                    return trimmed.substring(prefix.length()).trim();
                }
            }
        } catch (IOException e) {
            System.err.println("[MainController] Không đọc được .env: " + e.getMessage());
        }
        return "";
    }
}
