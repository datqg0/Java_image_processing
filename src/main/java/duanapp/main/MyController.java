package duanapp.main;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

/**
 * Controller for the project-selection screen (projects.fxml).
 *
 * <p>Loads the three most-recently-modified images from the user's save
 * directory and displays them as clickable thumbnails.
 */
public class MyController {

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    /** Default image shown when no recent files are available. */
    private static final String PLACEHOLDER_PATH =
            Paths.get(System.getProperty("user.dir"), "src", "main", "resources", "duanapp", "main", "icon", "anhsuademo.jpg").toAbsolutePath().toString();

    /**
     * Directory where recently saved images are loaded from.
     * Uses a "savehere" folder inside the project's current working directory.
     */
    private static final String SAVE_DIRECTORY =
            Paths.get(System.getProperty("user.dir"), "savehere").toAbsolutePath().toString();

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm dd/MM/yyyy");

    // -------------------------------------------------------------------------
    // FXML fields
    // -------------------------------------------------------------------------

    @FXML private Button    btnAddProject;
    @FXML private ImageView imageProject1;
    @FXML private ImageView imageProject2;
    @FXML private ImageView imageProject3;
    @FXML public  Label     time1;
    @FXML public  Label     time2;
    @FXML public  Label     time3;

    // -------------------------------------------------------------------------
    // State
    // -------------------------------------------------------------------------

    /** URLs of the three recent images (fallback to placeholder). */
    private String recentUrl1 = PLACEHOLDER_PATH;
    private String recentUrl2 = PLACEHOLDER_PATH;
    private String recentUrl3 = PLACEHOLDER_PATH;

    // -------------------------------------------------------------------------
    // Initialisation
    // -------------------------------------------------------------------------

    @FXML
    public void initialize() {
        loadRecentImages();

        btnAddProject.setOnAction(event -> openEditorWithPath(PLACEHOLDER_PATH));

        imageProject1.setOnMouseClicked(event -> openEditorWithUrl(recentUrl1));
        imageProject2.setOnMouseClicked(event -> openEditorWithUrl(recentUrl2));
        imageProject3.setOnMouseClicked(event -> openEditorWithUrl(recentUrl3));
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /** Scans {@link #SAVE_DIRECTORY} and populates the three thumbnail ImageViews. */
    private void loadRecentImages() {
        File folder = new File(SAVE_DIRECTORY);
        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("Thư mục lưu ảnh không tồn tại: " + SAVE_DIRECTORY);
            return;
        }

        File[] imageFiles = folder.listFiles(
                (dir, name) -> name.toLowerCase().endsWith(".png")
                            || name.toLowerCase().endsWith(".jpg")
                            || name.toLowerCase().endsWith(".jpeg"));

        if (imageFiles == null || imageFiles.length == 0) {
            System.out.println("Không tìm thấy ảnh trong: " + SAVE_DIRECTORY);
            return;
        }

        Arrays.sort(imageFiles, Comparator.comparingLong(File::lastModified).reversed());

        if (imageFiles.length > 0) {
            imageProject1.setImage(new Image(imageFiles[0].toURI().toString()));
            recentUrl1 = imageFiles[0].getAbsolutePath(); // store raw path, not URI
            if (time1 != null) time1.setText(DATE_FORMAT.format(new Date(imageFiles[0].lastModified())));
        }
        if (imageFiles.length > 1) {
            imageProject2.setImage(new Image(imageFiles[1].toURI().toString()));
            recentUrl2 = imageFiles[1].getAbsolutePath();
            if (time2 != null) time2.setText(DATE_FORMAT.format(new Date(imageFiles[1].lastModified())));
        }
        if (imageFiles.length > 2) {
            imageProject3.setImage(new Image(imageFiles[2].toURI().toString()));
            recentUrl3 = imageFiles[2].getAbsolutePath();
            if (time3 != null) time3.setText(DATE_FORMAT.format(new Date(imageFiles[2].lastModified())));
        }
    }

    /**
     * Opens the editor with an absolute file path (as stored in {@link #recentUrl1} etc.).
     * Paths are stored as raw absolute paths, not URIs, to avoid encoding issues with
     * spaces and special characters when passed to OpenCV's {@code imread}.
     */
    private void openEditorWithUrl(String filePath) {
        if (filePath == null || filePath.equals(PLACEHOLDER_PATH)) {
            openEditorWithPath(PLACEHOLDER_PATH);
            return;
        }
        openEditorWithPath(filePath);
    }

    /**
     * Sets the default image path on {@link MainController} and transitions to
     * the editor screen with a fade animation.
     */
    private void openEditorWithPath(String filePath) {
        MainController.defaultImagePath = filePath;
        openEditorView();
        // Reset after opening so the next "new project" click starts clean
        MainController.defaultImagePath = PLACEHOLDER_PATH;
    }

    /** Loads main-view.fxml and replaces the current scene with a fade transition. */
    public void openEditorView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/duanapp/main/main-view.fxml"));
            Scene newScene = new Scene(loader.load());

            Stage stage = (Stage) btnAddProject.getScene().getWindow();

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

        } catch (Exception e) {
            System.err.println("Lỗi khi mở màn hình chỉnh sửa: " + e.getMessage());
            e.printStackTrace();
        }
    }
}