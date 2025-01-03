package duanapp.main;
import java.io.*;
import javax.swing.*;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Button;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import java.io.IOException;

import javafx.util.Duration;
import org.opencv.core.Core;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import java.text.SimpleDateFormat;
import java.util.Date;
public class MyController {
    @FXML
    private Button btnAddProject;

    @FXML
    private ImageView imageProject1, imageProject2, imageProject3;
    public Label time1,time2,time3;
    public int got_somthing=0;
    public String gotthis="src/main/resources/duanapp/main/icon/anhsuademo.jpg";
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy");
    public String oneurl=gotthis,twourl=gotthis,threeurl=gotthis;
    @FXML
    public void initialize() {
        // Tải hình ảnh vào các ImageView
        loadImages();

        // Nút Dự án mới: mở chế độ chỉnh sửa
        btnAddProject.setOnAction(event -> openEditMode(gotthis));

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
                time1.setText(sdf.format(new Date(imageFiles[0].lastModified())));
            }
            if (imageFiles.length > 1) {
                imageProject2.setImage(new Image(imageFiles[1].toURI().toString()));
                twourl=imageProject2.getImage().getUrl();
                time2.setText(sdf.format(new Date(imageFiles[1].lastModified())));
            }
            if (imageFiles.length > 2) {
                imageProject3.setImage(new Image(imageFiles[2].toURI().toString()));
                threeurl=imageProject3.getImage().getUrl();
                time3.setText(sdf.format(new Date(imageFiles[2].lastModified())));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void openNewView() {
        try {
            // Tải giao diện chỉnh sửa ảnh từ file FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/duanapp/main/main-view.fxml"));
            Scene newScene = new Scene(loader.load());

            // Lấy stage hiện tại
            Stage currentStage = (Stage) btnAddProject.getScene().getWindow();

            // Tạo hiệu ứng Fade Out cho Scene hiện tại
            FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), currentStage.getScene().getRoot());
            fadeOut.setFromValue(1.0); // Độ mờ ban đầu
            fadeOut.setToValue(0.0);   // Độ mờ cuối cùng
            fadeOut.setOnFinished(event -> {
                // Chuyển sang Scene mới
                currentStage.setScene(newScene);
                // Tạo hiệu ứng Fade In cho Scene mới
                FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), newScene.getRoot());
                fadeIn.setFromValue(0.0); // Độ mờ ban đầu
                fadeIn.setToValue(1.0);   // Độ mờ cuối cùng
                fadeIn.play();
            });
            fadeOut.play(); // Bắt đầu hiệu ứng Fade Out
        } catch (Exception e) {
            System.err.println("Error: " + e);
        }
    }
    private void openEditMode(String projectName) {
            String filePath =gotthis;
            if(projectName!=gotthis) {
                filePath = projectName.replace("file:/", "");
                System.out.println(filePath);
            }
            MainController.default_image= filePath;
            openNewView();
            MainController.default_image= gotthis;
    }
}