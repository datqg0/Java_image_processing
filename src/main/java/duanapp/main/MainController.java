package duanapp.main;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javax.imageio.ImageIO;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.core.Mat;
import java.awt.image.BufferedImage;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.stage.DirectoryChooser;
import org.opencv.imgproc.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.Cursor;
import java.awt.Font;
import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.HttpResponse;

public class MainController {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    @FXML
    private ImageView mainImageView;
    public static String default_image="";
    ;
    int current_pos =0;
    public Mat currentImage= Imgcodecs.imread(default_image,Imgcodecs.IMREAD_UNCHANGED);
    public ArrayList<Mat> sto = new ArrayList<>();
    // Hiện ảnh nói chung


    @FXML
    private HBox cropOptionsMenu;
    @FXML
    private HBox new_text;

    @FXML
    private MenuButton resizeOptionsMenu;

    @FXML MenuButton Flip_and_rotate;

    @FXML
    private MenuButton fliterOptionsMenu;

    @FXML
    private MenuButton insertOptionsMenu;


    @FXML
    private HBox draw1 ,draw2,draw4,brightness1,brightness2,brightness3;

    @FXML
    private ColorPicker colorPicker = new ColorPicker();



    // Danh sách chứa tất cả các phần mở rộng
    private final List<Region> expandableMenus = new ArrayList<>();

    @FXML
    public void initialize() {
        show_the_images();
        // Thêm tất cả các phần mở rộng vào danh sách
        expandableMenus.add(Flip_and_rotate);
        expandableMenus.add(new_text);
        expandableMenus.add(cropOptionsMenu);
        expandableMenus.add(resizeOptionsMenu);
        expandableMenus.add(fliterOptionsMenu);
        expandableMenus.add(insertOptionsMenu);
        expandableMenus.add(draw1);
        expandableMenus.add(draw2);
        expandableMenus.add(draw4);
        expandableMenus.add(brightness1);
        expandableMenus.add(brightness2);
        expandableMenus.add(brightness3);

        // Đặt tất cả các phần mở rộng ban đầu là ẩn
        // Gắn listener để kiểm tra màu đã chọn
//        colorToggleGroup.selectedToggleProperty().addListener((observable, oldToggle, newToggle) -> {
//            if (newToggle != null) {
//                RadioButton selectedRadioButton = (RadioButton) newToggle;
//                String selectedColor = selectedRadioButton.getText();
//                System.out.println("Bạn đã chọn: " + selectedColor);
//            }
//        });
        // Đặt màu mặc định ban đầu cho ColorPicker
        colorPicker.setValue(Color.BLACK);


        hideAllMenus();
    }
    protected void mouse_skip () {
        mainImageView.setOnMousePressed(null);
        mainImageView.setOnMouseDragged(null);
        mainImageView.setOnMouseReleased(null);
        mainImageView.setCursor(Cursor.DEFAULT);
    }
    WritableImage image = matToImage(currentImage);
    //show u my code
    private void hideAllMenus() {
        mouse_skip();
        System.out.println(current_pos);
        for (Region menu : expandableMenus) {
            menu.setVisible(false); // Đặt menu về trạng thái ẩn
        }
    }
    public void show_the_images() {
        //System.out.println(current_pos);
        image = matToImage(currentImage);
        mainImageView.setImage(image);
    }
    public void add_action() {
        Mat clone = currentImage.clone();
        current_pos++;
        if((current_pos)<sto.size()) {
            sto.set(current_pos,clone);
        }
        else {
            sto.add(clone);
        }
        //handle out of memory
        if(sto.size()>100&&current_pos>0) {
            sto.remove(0);
            current_pos--;
        }
    }
    @FXML
    public void handleOpenImage() {
        if(sto.size()==0) {
            sto.add(currentImage);
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(mainImageView.getScene().getWindow());
        try {
            if (file != null) {
                System.out.println("ok");
                currentImage = Imgcodecs.imread(file.getAbsolutePath(),Imgcodecs.IMREAD_UNCHANGED);
                add_action();
                show_the_images();
            }
        }
        catch (Exception e) {
            System.out.println("Never - Give - Up");
        }
    }
    // đổi mat->javafx để show
    private WritableImage matToImage(Mat mat) {
        try {
            int type = BufferedImage.TYPE_BYTE_GRAY;
            if (mat.channels() > 1) {
                Mat convertedMat = new Mat();
                Imgproc.cvtColor(mat, convertedMat, Imgproc.COLOR_BGR2RGB);
                mat = convertedMat;
                type = BufferedImage.TYPE_3BYTE_BGR;
            }

            BufferedImage bufferedImage = new BufferedImage(mat.cols(), mat.rows(), type);
            byte[] data = new byte[mat.rows() * mat.cols() * mat.channels()];
            mat.get(0, 0, data);
            bufferedImage.getRaster().setDataElements(0, 0, mat.cols(), mat.rows(), data);

            return SwingFXUtils.toFXImage(bufferedImage, null);
        } catch (Exception e) {
            System.err.println("Error converting Mat to Image: " + e.getMessage());
            return null;
        }
    }
    @FXML
    protected void handleRedo() {
        if(current_pos>0) {
            current_pos--;
            currentImage=sto.get(current_pos).clone();
            show_the_images();
        }
    }
    @FXML
    protected void handleNext() {
        if(current_pos<sto.size()-1) {
            current_pos++;
            currentImage=sto.get(current_pos).clone();
            show_the_images();
        }
    }
    @FXML
    protected void handleResize() {
        //Control: label
        Label widthlb = new Label("Width now : " + currentImage.rows());
        Label heightlb = new Label("Height now " + currentImage.cols());
        Label notify = new Label();

        //Textfield
        TextField width = new TextField();
        width.setPromptText("Width :");

        TextField height = new TextField();
        height.setPromptText("Height :");
        //Control: button
        Button resize = new Button("resize");

        //Action
        resize.setOnAction(event ->{
            try {
                double w = Double.parseDouble(width.getText());
                double h = Double.parseDouble(height.getText());
                Size size = new Size(w, h);
                Imgproc.resize(currentImage, currentImage, size, 0, 0, Imgproc.INTER_AREA);
                widthlb.setText("Width now: " + currentImage.rows());
                heightlb.setText("Height now: "+ currentImage.cols());
                notify.setText("Successful");
                notify.setTextFill(Color.GREEN);
                add_action();
                show_the_images();
            }catch(NumberFormatException ex) {
                System.err.println("Error");
            }
        });
        // Layout
        Stage stage = new Stage();
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(30));
        layout.getChildren().addAll(widthlb, heightlb,width,height,resize,notify);
        //Scene
        Scene scene = new Scene(layout, 500,500);
        stage.setTitle("resize Window");
        stage.setScene(scene);
        stage.show();
    }
    @FXML
    protected void left_rot () {
        Core.rotate(currentImage, currentImage, Core.ROTATE_90_COUNTERCLOCKWISE);
        add_action();
        show_the_images();
    }
    @FXML
    protected void right_rot () {
        Core.rotate(currentImage, currentImage, Core.ROTATE_90_CLOCKWISE);
        add_action();
        show_the_images();
    }
    @FXML
    protected void flip_h () {
        Core.flip(currentImage, currentImage, 0);
        add_action();
        show_the_images();
    }
    @FXML
    protected void flip_v () {
        Core.flip(currentImage, currentImage, 1);
        add_action();
        show_the_images();
    }
    @FXML
    protected void handleflip_and_rotate() {
        hideAllMenus();
        Flip_and_rotate.setVisible(true);
    }
    //draw
    @FXML
    protected void handleSave() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn nơi lưu ảnh và chỉnh tên");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        // Mặc định tên file ban đầu
        fileChooser.setInitialFileName("output_image.jpg");

        // Hiển thị hộp thoại lưu file
        File file = fileChooser.showSaveDialog(mainImageView.getScene().getWindow());

        if (file != null) {
            // Lưu ảnh với tên file và đường dẫn người dùng chọn
            String outputImagePath = file.getAbsolutePath();
            boolean isSaved = Imgcodecs.imwrite(outputImagePath, currentImage);

            if (isSaved) {
                System.out.println("Ảnh đã được lưu tại: " + outputImagePath);
            } else {
                System.out.println("Không thể lưu ảnh!");
            }
        } else {
            System.out.println("Người dùng không chọn nơi lưu!");
        }
    }
    @FXML
    protected void handleFilter() {
        //Control: label
        Label widthlb = new Label("Width now : " + currentImage.rows());
        Label heightlb = new Label("Height now " + currentImage.cols());
        Label notify = new Label();

        //Textfield
        TextField width = new TextField();
        width.setPromptText("Width :");

        TextField height = new TextField();
        height.setPromptText("Height :");
        //Control: button
        Button reverse = new Button("Âm bản");
        Button black_and_white = new Button("đen trắng");
        Button filateral_filter = new Button("làm mịn ảnh");
        Button redoo = new Button("trở lại");

        //Action
        reverse.setOnAction(event ->{
            try {
                for (int i = 0; i < currentImage.rows(); i++) {
                    for (int j = 0; j < currentImage.cols(); j++) {
                        double[] data = currentImage.get(i, j);
                        double[] data2 = currentImage.get(i, j);
                        data2[0] = 255 - data[0];
                        data2[1] = 255 - data[1];
                        data2[2] = 255 - data[2];
                        //System.out.println(data[0]+" "+data2[0]);
                        currentImage.put(i, j, data2);
                    }
                }
                ///wh
                add_action();
                show_the_images();
            }catch(NumberFormatException ex) {
                System.err.println("Error");
            }
        });
        black_and_white.setOnAction(event ->{
            try {
                Imgproc.cvtColor(currentImage, currentImage, Imgproc.COLOR_BGR2GRAY);
                Mat bwRgbImage = new Mat();
                Imgproc.cvtColor(currentImage, currentImage, Imgproc.COLOR_GRAY2BGR);
                ///wh
                add_action();
                show_the_images();
            }catch(NumberFormatException ex) {
                System.err.println("Error");
            }
        });
        filateral_filter.setOnAction(event ->{
            try {
                //có thể cho diều chỉnh nếu còn thời gian
                Imgproc.medianBlur(currentImage, currentImage, 5);
                ///wh
                add_action();
                show_the_images();
            }catch(NumberFormatException ex) {
                System.err.println("Error");
            }
        });
        redoo.setOnAction(event ->{
            try {
                handleRedo();
            }catch(NumberFormatException ex) {
                System.err.println("Error");
            }
        });
        // Layout
        Stage stage = new Stage();
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(30));
        layout.getChildren().addAll(reverse,black_and_white,filateral_filter,redoo);
        //Scene
        Scene scene = new Scene(layout, 200,250);
        stage.setTitle("resize Window");
        stage.setScene(scene);
        stage.show();
    }
    double mul=2;
    int got=0;
    double opa=1;
    @FXML
    Slider getsize = new Slider();
    @FXML
    Slider opacity =new Slider();
    int r=0,g=0,b=0;
    @FXML
    private void handleColorChange() {
        // Lấy màu đã chọn từ ColorPicker
        Color selectedColor = colorPicker.getValue();

        // Lấy các thành phần màu (Red, Green, Blue) của màu đã chọn
        double red = selectedColor.getRed();   // Giá trị đỏ (0.0 - 1.0)
        double green = selectedColor.getGreen(); // Giá trị xanh lá (0.0 - 1.0)
        double blue = selectedColor.getBlue();  // Giá trị xanh dương (0.0 - 1.0)

        // Chuyển đổi từ 0-1 sang 0-255 để dễ dàng sử dụng trong các ứng dụng vẽ
        r = (int) (red * 255);
        g = (int) (green * 255);
        b = (int) (blue * 255);
        // Thực hiện các thao tác khác với giá trị màu, ví dụ: lưu vào biến hoặc sử dụng trong vẽ
    }
    @FXML
    protected void handleDraw() {
        hideAllMenus();
        //System.out.println(currentImage.rows()+ " " + currentImage.cols());
        show_the_images();
        mainImageView.setCursor(Cursor.CROSSHAIR);
        getsize.setMin(1); // Giá trị tối thiểu
        getsize.setMax(10); // Giá trị tối đa
        getsize.setValue(2); // Giá trị khởi tạo (bạn có thể điều chỉnh)
        // Thiết lập bước nhảy (tick unit) giữa các giá trị trên Slider
        getsize.setBlockIncrement(0.5); // Mỗi lần thay đổi 1 đơn vị
        getsize.setMajorTickUnit(0.5); // Bước nhảy chính 1 đơn vị
        opacity.setMin(0); // Giá trị tối thiểu
        opacity.setMax(1); // Giá trị tối đa
        opacity.setValue(1); // Giá trị khởi tạo (bạn có thể điều chỉnh)
        // Thiết lập bước nhảy (tick unit) giữa các giá trị trên Slider
        opacity.setBlockIncrement(0.1); // Mỗi lần thay đổi 1 đơn vị
        opacity.setMajorTickUnit(0.1);
        mul= getsize.getValue();
        opa = opacity.getValue();
        draw1.setVisible(true);
        draw2.setVisible(true);
        draw4.setVisible(true);
        PixelWriter pixelWriter = image.getPixelWriter();
        Pane root = new Pane();
        // Lắng nghe sự thay đổi của slider và cập nhật giá trị của mul
        getsize.valueProperty().addListener((observable, oldValue, newValue) -> {
            mul = newValue.doubleValue(); // Cập nhật giá trị của mul từ slider
            System.out.println("mul: " + mul); // Kiểm tra giá trị mul
        });
        opacity.valueProperty().addListener((observable, oldValue, newValue) -> {
            opa = newValue.doubleValue(); // Cập nhật giá trị của mul từ slider
            System.out.println("opa: " + opa); // Kiểm tra giá trị mul
        });
        mainImageView.setOnMouseDragged(event -> {
            double imageViewWidth = mainImageView.getFitWidth();
            double imageViewHeight = mainImageView.getFitHeight();

            // Kích thước gốc của ảnh (Mat)
            double matWidth = currentImage.cols();
            double matHeight = currentImage.rows();

            // Tính tỷ lệ co (scale)
            double scaleX = imageViewWidth / matWidth;
            double scaleY = imageViewHeight / matHeight;
            double scale = Math.min(scaleX, scaleY); // Sử dụng tỷ lệ nhỏ nhất để giữ nguyên tỷ lệ ảnh
            got=Math.max((int)Math.round(scaleX),((int)Math.round(scaleY)));
            got=Math.max(1,got);
            int size_of_pen=(int)(got*mul);

            // Tính phần dư (offset) nếu preserveRatio = true

            // Tọa độ chuột trên ImageView
            double mouseX = event.getX();
            double mouseY = event.getY();

            // Chuyển đổi về tọa độ gốc của ảnh
            int roundedX = (int) ((mouseX) / scale);
            int roundedY = (int) ((mouseY) / scale);
            for (int i=roundedX-size_of_pen;i<Math.min(roundedX+size_of_pen,matWidth);i++) {
                for (int j=roundedY-size_of_pen;j<Math.min(roundedY+size_of_pen,matHeight);j++) {
                    if(i<matWidth&&j<matHeight&&i>=0&&j>=0) {
                        //System.out.println("Mouse position: (" + roundedX + ", " + roundedY + ")");
                        double[] tmp = currentImage.get(j, i);
                        tmp[0] = b;
                        tmp[1] = g;
                        tmp[2] = r;
                        currentImage.put(j, i, tmp);
                        //System.out.println("run");
                        pixelWriter.setColor(i, j, javafx.scene.paint.Color.rgb(r, g, b,opa));
                        mainImageView.setImage(image);
                    }
                }
            }
        });
        mainImageView.setOnMouseReleased(event -> {
            System.out.println("end");
            add_action();
        });
        //show_the_images();
    }
    @FXML
    Slider briness = new Slider();
    int dosang=0;
    @FXML
    public void save_image() {
        for (int i = 0; i < currentImage.rows(); i++) {
            for (int j = 0; j < currentImage.cols(); j++) {
                double[] data = currentImage.get(i, j);
                double[] data2 = currentImage.get(i, j);
                data2[0] +=dosang;
                data2[1] +=dosang;
                data2[2] +=dosang;
                if(data2[0]<0) {
                    data2[0]=0;
                }
                if(data2[1]<0) {
                    data2[1]=0;
                }
                if(data2[2]<0) {
                    data2[1]=0;
                }
                if(data2[0]>255) {
                    data2[0]=255;
                }
                if(data2[1]>255) {
                    data2[1]=255;
                }
                if(data2[2]>255) {
                    data2[1]=255;
                }
                //System.out.println(data[0]+" "+data2[0]);
                currentImage.put(i, j, data2);
            }
        }
        add_action();
        show_the_images();
    }
    void tempshow() {
        Mat tempimg= currentImage.clone();
        for (int i = 0; i < tempimg.rows(); i++) {
            for (int j = 0; j < tempimg.cols(); j++) {
                double[] data = tempimg.get(i, j);
                double[] data2 = tempimg.get(i, j);
                data2[0] +=dosang;
                data2[1] +=dosang;
                data2[2] +=dosang;
                if(data2[0]<0) {
                    data2[0]=0;
                }
                if(data2[1]<0) {
                    data2[1]=0;
                }
                if(data2[2]<0) {
                    data2[1]=0;
                }
                if(data2[0]>255) {
                    data2[0]=255;
                }
                if(data2[1]>255) {
                    data2[1]=255;
                }
                if(data2[2]>255) {
                    data2[1]=255;
                }
                //System.out.println(data[0]+" "+data2[0]);
                tempimg.put(i, j, data2);
            }
        }
        WritableImage temppo = matToImage(tempimg);
        mainImageView.setImage(temppo);
    }
    @FXML
    protected void handleAdjust() {
        hideAllMenus();
        brightness1.setVisible(true);
        briness.setMin(-50); // Giá trị tối thiểu
        briness.setMax(50); // Giá trị tối đa
        briness.setValue(0); // Giá trị khởi tạo (bạn có thể điều chỉnh)
        // Thiết lập bước nhảy (tick unit) giữa các giá trị trên Slider
        briness.setBlockIncrement(2); // Mỗi lần thay đổi 1 đơn vị
        briness.setMajorTickUnit(2); // Bước nhảy chính 1 đơn vị
        briness.valueProperty().addListener((observable, oldValue, newValue) -> {
            dosang = (int)newValue.doubleValue();
            System.out.println(dosang);
            tempshow();
        });
    }
    int crx=-1,cry=-1,crz=-1,crt=-1;
    public void showcrop() {
        Mat tempimg= currentImage.clone();
        for (int i = 0; i < tempimg.rows(); i++) {
            for (int j = 0; j < tempimg.cols(); j++) {
                if(i>=crx&&i<=crz&&j>=cry&&j<=crt) {

                }
                else {
                    double[] data = tempimg.get(i, j);
                    double[] data2 = tempimg.get(i, j);
                    data2[0] -= 50;
                    data2[1] -= 50;
                    data2[2] -= 50;
                    if (data2[0] < 0) {
                        data2[0] = 0;
                    }
                    if (data2[1] < 0) {
                        data2[1] = 0;
                    }
                    if (data2[2] < 0) {
                        data2[1] = 0;
                    }
                    if (data2[0] > 255) {
                        data2[0] = 255;
                    }
                    if (data2[1] > 255) {
                        data2[1] = 255;
                    }
                    if (data2[2] > 255) {
                        data2[1] = 255;
                    }
                    //System.out.println(data[0]+" "+data2[0]);
                    tempimg.put(i, j, data2);
                }
            }
        }
        WritableImage temppo = matToImage(tempimg);
        mainImageView.setImage(temppo);
    }
    Mat tempcut = currentImage.clone();
    @FXML
    public void save_crop () {
        currentImage=tempcut.clone();
        add_action();
        show_the_images();
    }
    @FXML
    protected void handleCrop() {
        hideAllMenus();
        cropOptionsMenu.setVisible(true);

        mainImageView.setOnMousePressed(event -> {
            // Lấy kích thước của ảnh trong ImageView
            double imageViewWidth = mainImageView.getFitWidth();
            double imageViewHeight = mainImageView.getFitHeight();

            // Kích thước gốc của ảnh (Mat)
            double matWidth = currentImage.cols();
            double matHeight = currentImage.rows();

            // Tính tỷ lệ co (scale)
            double scaleX = imageViewWidth / matWidth;
            double scaleY = imageViewHeight / matHeight;
            double scale = Math.min(scaleX, scaleY); // Giữ nguyên tỷ lệ ảnh

            // Lấy tọa độ chuột khi bắt đầu kéo
            crx = (int) (event.getY() / scale);
            cry = (int) (event.getX() / scale);
        });

        mainImageView.setOnMouseDragged(event -> {
            double imageViewWidth = mainImageView.getFitWidth();
            double imageViewHeight = mainImageView.getFitHeight();

            // Kích thước gốc của ảnh (Mat)
            double matWidth = currentImage.cols();
            double matHeight = currentImage.rows();

            // Tính tỷ lệ co (scale)
            double scaleX = imageViewWidth / matWidth;
            double scaleY = imageViewHeight / matHeight;
            double scale = Math.min(scaleX, scaleY); // Giữ nguyên tỷ lệ ảnh

            // Lấy tọa độ chuột khi kéo
            int currentX = (int) (event.getX() / scale);
            int currentY = (int) (event.getY() / scale);
            int tempo = currentX;
            currentX=currentY;
            currentY=tempo;
            // Cập nhật tọa độ vùng cắt
            crz = Math.max(crz, currentX);
            crt = Math.max(crt, currentY);
            crx = Math.min(crx, currentX);
            cry = Math.min(cry, currentY);
            crz=Math.min(crz,(int)matHeight);
            crt=Math.min(crt,(int)matWidth);
            // Hiển thị khung vùng crop
            showcrop();
        });

        mainImageView.setOnMouseReleased(event -> {
            // Kiểm tra tính hợp lệ của vùng crop
            if (crx >= 0 && cry >= 0 && crz > crx && crt > cry && crt-cry <= currentImage.cols() && crz-crx <= currentImage.rows()) {
                Rect regionOfInterest = new Rect(cry, crx, crt - cry, crz - crx);
                tempcut = new Mat(currentImage, regionOfInterest);
            } else {
                showAlert("Invalid crop region!");
            }

            // Đặt lại giá trị vùng crop
            crx = cry = crz = crt = -1;
        });
    }
    String add_string = "G";
    double text_size = 10;
    String text_font = "Arial";

    @FXML
    protected void input_string () {
        Label guide1 = new Label("nhập text vào ô dưới ");
        Label guide2 =  new Label("nhập cỡ chữ");
        TextField input_text_here = new TextField();
        TextField sizez = new TextField();
        input_text_here.setPromptText("Nhập chuỗi cần chèn :");
        Label guide3 = new Label("chọn phông chữ");
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getItems().addAll("Arial", "Times New Roman", "Roboto");
        comboBox.setValue("Arial"); // Giá trị mặc định
        input_text_here.setText(add_string);
        sizez.setText(Double.toString(text_size));
        //Control: button
        Button add = new Button("Đổi chuỗi");
        Stage stage = new Stage();
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(30));
        layout.getChildren().addAll(guide1,input_text_here,guide2,sizez,guide3,comboBox,add);
        //Scene
        Scene scene = new Scene(layout, 500,500);
        stage.setTitle("đổi chữ");
        stage.setScene(scene);
        stage.show();
        //Action
        add.setOnAction(event -> {
            try {
                text_font = comboBox.getValue();
                add_string = input_text_here.getText();
                text_size = Double.parseDouble(sizez.getText());
                stage.close();
            } catch (NumberFormatException ex) {
                System.err.println("Error");
            }
        });
        // Layout

    }
    @FXML
    public void save_insert_image() {
        currentImage=reclone.clone();
        add_action();
        show_the_images();
    }
     // Kênh alpha
     protected Mat createtemp(int x, int y) {
         Mat a = clonea.clone();
         Mat b = reclone.clone();
         int n = a.rows();
         int m = a.cols();
         int p1 = b.rows();
         int p2 = b.cols();
         Mat out = b.clone();

         // Kiểm tra số kênh của ảnh
         int channels = a.channels();  // Số kênh của ảnh (RGB = 3, RGBA = 4)

         for (int i = 0; i < n && i + x < p1; i++) {
             for (int j = 0; j < m && j + y < p2; j++) {
                 double[] pixel = a.get(i, j);

                 // Nếu ảnh có 4 kênh (RGBA), kiểm tra kênh alpha
                 if (channels == 4) {
                     double alpha = pixel[3];  // Kênh alpha
                     // Kiểm tra xem phần nền có trong suốt không (alpha = 0)
                     if (alpha != 0) {
                         double[] arr = new double[3];
                         arr[0]=pixel[0];
                         arr[1]=pixel[1];
                         arr[2]=pixel[2];
                         if(i+x>=0&&j+y>=0) {
                             out.put(i + x, j + y, arr);
                         }// Di chuyển pixel vào vị trí thích hợp
                     }
                 } else if (channels == 3) {
                     // Nếu ảnh chỉ có 3 kênh (RGB), xử lý bình thường
                     if(i+x>=0&&j+y>=0) {
                         out.put(i + x, j + y, pixel);
                     }
                 }
             }
         }
         return out;
     }


    Mat clonea = currentImage.clone();
    Mat reclone = currentImage.clone();
    @FXML
    protected void handleInsertimage() {
        hideAllMenus();
        //mainImageView.setCursor(Cursor.CROSSHAIR);
        brightness2.setVisible(true);
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(mainImageView.getScene().getWindow());
        try {
            if (file != null) {
                clonea = Imgcodecs.imread(file.getAbsolutePath(),Imgcodecs.IMREAD_UNCHANGED);
            }
        }
        catch (Exception e) {
            System.out.println("Never - Give - Up");
        }
        mainImageView.setOnMousePressed(event -> {
            reclone=currentImage.clone();
            double imageViewWidth = mainImageView.getFitWidth();
            double imageViewHeight = mainImageView.getFitHeight();

            // Kích thước gốc của ảnh (Mat)
            double matWidth = currentImage.cols();
            double matHeight = currentImage.rows();

            // Tính tỷ lệ co (scale)
            double scaleX = imageViewWidth / matWidth;
            double scaleY = imageViewHeight / matHeight;
            double scale = Math.min(scaleX, scaleY); // Sử dụng tỷ lệ nhỏ nhất để giữ nguyên tỷ lệ ảnh


            // Tọa độ chuột trên ImageView
            double mouseX = event.getX();
            double mouseY = event.getY();

            // Chuyển đổi về tọa độ gốc của ảnh
            int roundedX = (int) ((mouseX) / scale);
            int roundedY = (int) ((mouseY) / scale);

            reclone= createtemp(roundedY,roundedX).clone();
            WritableImage temppo = matToImage(reclone);
            mainImageView.setImage(temppo);
        });
    }
    @FXML
    protected void handleInsert() {
        hideAllMenus();
        //mainImageView.setCursor(Cursor.CROSSHAIR);
        new_text.setVisible(true);
        draw4.setVisible(true);
        mainImageView.setOnMousePressed(event -> {
            double imageViewWidth = mainImageView.getFitWidth();
            double imageViewHeight = mainImageView.getFitHeight();

            // Kích thước gốc của ảnh (Mat)
            double matWidth = currentImage.cols();
            double matHeight = currentImage.rows();

            // Tính tỷ lệ co (scale)
            double scaleX = imageViewWidth / matWidth;
            double scaleY = imageViewHeight / matHeight;
            double scale = Math.min(scaleX, scaleY); // Sử dụng tỷ lệ nhỏ nhất để giữ nguyên tỷ lệ ảnh


            // Tọa độ chuột trên ImageView
            double mouseX = event.getX();
            double mouseY = event.getY();

            // Chuyển đổi về tọa độ gốc của ảnh
            int roundedX = (int) ((mouseX) / scale);
            int roundedY = (int) ((mouseY) / scale);
            /*Imgproc.putText(
                    currentImage,                      // Ảnh gốc
                    add_string,                    // Văn bản để chèn
                    new Point(roundedX, roundedY),        // Vị trí (tọa độ gốc của ảnh)
                    Imgproc.FONT_HERSHEY_SIMPLEX,     // Font chữ
                    text_size,                              // Kích thước font
                    new Scalar(r, g, b),            // Màu chữ (BGR)
                    2                                 // Độ dày ch
            );*/
            BufferedImage buffimage = matToBufferedImage(currentImage);
            Graphics2D g2d = buffimage.createGraphics();

            // Sử dụng font có thể hỗ trợ tiếng Việt
            Font font = new Font(text_font, Font.PLAIN, (int)text_size);  // Bạn có thể thay đổi font để phù hợp với tiếng Việt
            g2d.setFont(font);
            java.awt.Color textColor = new java.awt.Color(r, g, b);
            g2d.setColor(textColor);  // Màu chữ
            g2d.drawString(add_string, roundedX, roundedY);
            currentImage = bufferedImageToMat(buffimage).clone();
            add_action();
            show_the_images();
        });
    }
    private BufferedImage matToBufferedImage(Mat mat) {
        int width = mat.width();
        int height = mat.height();
        int channels = mat.channels();

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        mat.get(0, 0, ((java.awt.image.DataBufferByte) image.getRaster().getDataBuffer()).getData());
        return image;
    }
    private Mat bufferedImageToMat(BufferedImage bufferedImage) {
        // Lấy thông số về kích thước ảnh
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        Mat mat = currentImage.clone();

        // Lấy dữ liệu pixel từ BufferedImage
        for (int i=0;i<width;i++) {
            for (int j=0;j<height;j++) {
                int rgb = bufferedImage.getRGB(i,j);
                int red = (rgb >> 16) & 0xFF;   // Lấy kênh Red
                int green = (rgb >> 8) & 0xFF;  // Lấy kênh Green
                int blue = rgb & 0xFF;
                double[] tmp = currentImage.get(j, i);
                tmp[0] = blue;
                tmp[1] = green;
                tmp[2] = red;
                mat.put(j, i, tmp);
            }
        }
        return mat;
    }


    /*@FXML
    protected void handleExit() {
        showAlert("Thoát ứng dụng...");
        System.exit(0);
    }*/
    @FXML
    protected void handleExit() {
            Stage currentStage = (Stage) mainImageView.getScene().getWindow();
            currentStage.close();
    }
    @FXML
    protected void AI () {
        try {
            // API URL và API Key
            String url = "https://api.remove.bg/v1.0/removebg";
            String apiKey = "381DzVgo7ZQcnznKioPr1uzr";

            // Tạo client HTTP
            CloseableHttpClient client = HttpClients.createDefault();

            // Tạo yêu cầu POST
            HttpPost postRequest = new HttpPost(url);
            postRequest.addHeader("X-Api-Key", apiKey);
            //file open
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
            File file = fileChooser.showOpenDialog(mainImageView.getScene().getWindow());

            // Tạo multipart entity và thêm ảnh
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addBinaryBody("image_file", new File(file.getAbsolutePath()));
            builder.addTextBody("size", "auto");
            HttpEntity multipartEntity = builder.build();

            postRequest.setEntity(multipartEntity);

            // Gửi yêu cầu và nhận phản hồi
            HttpResponse response = client.execute(postRequest);
            HttpEntity entity = response.getEntity();

            // Đọc kết quả từ response và lưu vào file
            InputStream inputStream = entity.getContent();
            FileOutputStream outputStream = new FileOutputStream(new File("unscreen.png"));

            int byteRead;
            while ((byteRead = inputStream.read()) != -1) {
                outputStream.write(byteRead);
            }

            // Đóng streams
            inputStream.close();
            outputStream.close();

            // Xử lý nếu cần (in ra mã trạng thái)
            System.out.println("Response Code: " + response.getStatusLine().getStatusCode());

            client.close();

            // Chuyển file PNG thành Mat và hiển thị trên JavaFX
            currentImage = Imgcodecs.imread("unscreen.png",Imgcodecs.IMREAD_UNCHANGED);
            add_action();
            show_the_images();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Action Triggered");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
