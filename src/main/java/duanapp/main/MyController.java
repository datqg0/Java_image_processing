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
public class MyController {
    @FXML
    private Button btnAddProject;

    @FXML
    private ImageView imageProject1, imageProject2, imageProject3;

    @FXML
    public void initialize() {
        // Tải hình ảnh vào các ImageView
        loadImages();

        // Nút Dự án mới: mở chế độ chỉnh sửa
        btnAddProject.setOnAction(event -> openEditMode("new"));

        // Các dự án gần đây: mở chế độ chỉnh sửa tương ứng
        imageProject1.setOnMouseClicked(event -> openEditMode("Dự án 1"));
        imageProject2.setOnMouseClicked(event -> openEditMode("Dự án 2"));
        imageProject3.setOnMouseClicked(event -> openEditMode("Dự án 3"));
    }

    private void loadImages() {
        // Sử dụng getClass().getResource() để lấy đường dẫn tài nguyên chính xác
        imageProject1.setImage(new Image(getClass().getResource("/duanapp/main/picture/1.png").toExternalForm()));
        imageProject2.setImage(new Image(getClass().getResource("/duanapp/main/picture/2.png").toExternalForm()));
        imageProject3.setImage(new Image(getClass().getResource("/duanapp/main/picture/3.png").toExternalForm()));
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
            openNewView();
    }
}