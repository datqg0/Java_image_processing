package duanapp.main;
import java.io.*;
import javax.swing.*;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Button;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import java.io.IOException;
import org.opencv.core.Core;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
public class MyController {
    @FXML
    private Button btnAddProject;

    @FXML
    private ImageView imageProject1, imageProject2, imageProject3;
    public int got_somthing=0;
    public String gotthis="src/main/resources/duanapp/main/icon/anhsuademo.jpg";
    public String oneurl=gotthis,twourl=gotthis,threeurl=gotthis;
    @FXML
    public void initialize() {
        // Tải hình ảnh vào các ImageView
        loadImages();

        // Nút Dự án mới: mở chế độ chỉnh sửa
        btnAddProject.setOnAction(event -> openEditMode("new"));

        // Các dự án gần đây: mở chế độ chỉnh sửa tương ứng
        imageProject1.setOnMouseClicked(event -> openEditMode(oneurl) );
        imageProject2.setOnMouseClicked(event -> openEditMode(twourl));
        imageProject3.setOnMouseClicked(event -> openEditMode(threeurl));
    }

    private void loadImages() {
        try {
            // Đường dẫn thư mục trên hệ thống
            File folder = new File("C:\\Users\\dat\\Downloads\\savehere");
            if (!folder.exists() || !folder.isDirectory()) {
                System.out.println("Thư mục không tồn tại hoặc không phải là thư mục!");
                return;
            }

            // Lấy danh sách file và sắp xếp theo thời gian chỉnh sửa giảm dần
            File[] imageFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".jpg"));
            if (imageFiles == null || imageFiles.length == 0) {
                System.out.println("Không tìm thấy file ảnh!");
                return;
            }

            Arrays.sort(imageFiles, Comparator.comparingLong(File::lastModified).reversed());

            // Load 3 file ảnh mới nhất
            if (imageFiles.length > 0) {
                imageProject1.setImage(new Image(imageFiles[0].toURI().toString()));
                oneurl=imageProject1.getImage().getUrl();
            }
            if (imageFiles.length > 1) {
                imageProject2.setImage(new Image(imageFiles[1].toURI().toString()));
                twourl=imageProject2.getImage().getUrl();
            }
            if (imageFiles.length > 2) {
                imageProject3.setImage(new Image(imageFiles[2].toURI().toString()));
                threeurl=imageProject3.getImage().getUrl();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void openNewView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("main-view.fxml"));
            Scene scene = new Scene(loader.load());

            // Tạo cửa sổ mới để hiển thị view mới
            Stage newStage = new Stage();
            newStage.setTitle("View Project");
            newStage.setScene(scene);
            newStage.show();
        }
        catch (Exception e) {
            System.err.println("Error" + e);
        }
    }
    private void openEditMode(String projectName) {
            String filePath = projectName.replace("file:/", "");
            MainController.default_image= filePath;
            System.out.println(MainController.default_image);
            openNewView();
            MainController.default_image= gotthis;
    }
}