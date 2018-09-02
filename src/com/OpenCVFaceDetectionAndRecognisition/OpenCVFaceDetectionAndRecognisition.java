
package com.OpenCVFaceDetectionAndRecognisition;

import java.io.File;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author sangram
 */
public class OpenCVFaceDetectionAndRecognisition extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // load the FXML resource
            FXMLLoader loader = new FXMLLoader(getClass().getResource("OpenCVFaceDetectionAndRecognisition.fxml"));
            BorderPane root = (BorderPane) loader.load();
            // set a whitesmoke background
            root.setStyle("-fx-background-color: whitesmoke;");
            // create and style a scene
            Scene scene = new Scene(root, 800, 600);
            scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
            // create the stage with the given title and the previously created
            // scene
            primaryStage.setTitle("Face Detection/Recognisition and Tracking");
            primaryStage.setScene(scene);
            // show the GUI
            primaryStage.show();

            // init the controller
            OpenCVFaceDetectionAndRecognisitionController controller = loader.getController();
            controller.init(primaryStage);

            // set the proper behavior on closing the application
            primaryStage.setOnCloseRequest((new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent we) {
                    controller.setClosed();
                }
            }));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        try {
            String opencvpath = args[0];
            System.load(opencvpath);
            launch(args);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Libary file path not provided as argument");

            String opencvpath = "/home/sangram/Projects/openCV/libopencv_java400.so";
            //enable this if make install is done
        //        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            File f = new File(opencvpath);
            if (f.exists()) {
                System.load(opencvpath);
                launch(args);
            }else{
                System.out.println("Provide libary file path as argument to jar");
            }
        }
    }

}
