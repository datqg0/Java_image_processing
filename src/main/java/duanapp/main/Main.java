package duanapp.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.opencv.core.Core;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.core.Mat;
public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println("run");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("projects.fxml"));
        Scene scene = new Scene(loader.load());
        primaryStage.setTitle("MyPhoto");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {

        launch(args);
    }
}
