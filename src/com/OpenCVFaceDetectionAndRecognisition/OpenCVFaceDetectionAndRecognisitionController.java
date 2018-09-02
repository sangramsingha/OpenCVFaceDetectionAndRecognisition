
package com.OpenCVFaceDetectionAndRecognisition;

import com.OpenCVFaceDetectionAndRecognisition.Utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.opencv.videoio.VideoCapture;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.face.EigenFaceRecognizer;
import static org.opencv.imgcodecs.Imgcodecs.imwrite;
import static org.opencv.imgproc.Imgproc.FONT_HERSHEY_PLAIN;
import static org.opencv.imgproc.Imgproc.putText;
/**
 *
 * @author sangram
 */
public class OpenCVFaceDetectionAndRecognisitionController {
     @FXML
    private Button DetectFaceBut;
    @FXML
    private ImageView originalFrame;
    @FXML
    private Button capTrainBut;
    @FXML
    private Button trainBut;
    @FXML
    private Button recogniseBut;
    @FXML
    private Button saveTrainFileBut;
    @FXML
    private TextField txtlabel;
    @FXML
    private TextField txtName;

    private int taskID;

    private String trainingFilePath;

    // a timer for acquiring the video stream
    private ScheduledExecutorService timer;
    // the OpenCV object that performs the video capture
    private VideoCapture capture;
    // a flag to change the button behavior
    private boolean cameraActive;

    // face cascade classifier
    private CascadeClassifier faceCascade;
    private int absoluteFaceSize;

    private int frameCounter;
    private int fileName;

    private ArrayList<Mat> images;
    private ArrayList<Integer> labels;
    private EigenFaceRecognizer efr;
    private Stage primaryStage;
    HashMap<Integer, String> map;

    /**
     * Init the controller, at start time
     */
    protected void init(Stage primaryStage) {
        this.capture = new VideoCapture();
        this.faceCascade = new CascadeClassifier();
        this.absoluteFaceSize = 0;
        this.frameCounter = 0;
        this.fileName = 0;
        this.taskID = 0;
        this.primaryStage = primaryStage;

        this.images = new ArrayList<>();
        this.labels = new ArrayList<>();
        this.map = new HashMap<>();
        // set a fixed width for the frame
        this.originalFrame.setFitWidth(600);
        // preserve image ratio
        this.originalFrame.setPreserveRatio(true);
    }
    

    @FXML
    protected void reset() {

        this.DetectFaceBut.setDisable(true);
        this.capTrainBut.setDisable(true);
        this.trainBut.setDisable(true);
        this.recogniseBut.setDisable(true);
        this.saveTrainFileBut.setDisable(true);
        this.capture = new VideoCapture();
        this.faceCascade = new CascadeClassifier();
        this.absoluteFaceSize = 0;
        this.frameCounter = 0;
        this.fileName = 0;
        this.taskID = 0;
        this.txtlabel.setText("");
        this.txtName.setText("");
        this.images = new ArrayList<>();
        this.labels = new ArrayList<>();

    }
    
    @FXML
    protected void validateLabel(){
    
        String label =this.txtlabel.getText();
        if(!label.matches("[0-9]*")){
            this.txtlabel.setText("");
        }
        
    }

    @FXML
    protected void chooseClassifierFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select cascade File");
        fileChooser.setInitialDirectory(
                new File(System.getProperty("user.home"))
        );
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("haar Xml Files", "haarcascade_frontalface_alt.xml"),
                new ExtensionFilter("lbp Xml Files", "lbpcascade_frontalface.xml"));
        File selectedFile = fileChooser.showOpenDialog(this.primaryStage);
        if (selectedFile != null) {
            this.setCascadeClassifierFilePath(selectedFile.getAbsolutePath());
            this.DetectFaceBut.setDisable(false);
            this.capTrainBut.setDisable(false);
        }

    }

    @FXML
    protected void chooseTrainingFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Training File");
        fileChooser.setInitialDirectory(
                new File(System.getProperty("user.home"))
        );
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("training yml Files", "*.yml"));
        File selectedFile = fileChooser.showOpenDialog(this.primaryStage);
        if (selectedFile != null) {
            this.trainingFilePath = selectedFile.getAbsolutePath();
            this.DetectFaceBut.setDisable(true);
            this.capTrainBut.setDisable(true);
            this.recogniseBut.setDisable(true);
            this.trainBut.setDisable(false);
        }

    }

    @FXML
    protected void saveTrainFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Training File");
        fileChooser.setInitialDirectory(
                new File(System.getProperty("user.home"))
        );
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("training yml Files", "*.yml"));
        File file = fileChooser.showSaveDialog(primaryStage);
        if (file != null) {
                        
            this.efr.save(file.getAbsolutePath()+".yml");
            
            Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Training file");
                alert.setHeaderText("Sucesses");
                alert.setContentText("Filed Saved sucessfully!");
                alert.showAndWait().ifPresent(rs -> {
                    if (rs == ButtonType.OK) {
                        alert.close();
                    }
                });
        }
    }

    @FXML
    protected void detectFace() {
        if (!this.cameraActive) {
            // update the button content
            this.DetectFaceBut.setText("Stop Detection");
            this.capTrainBut.setDisable(true);
            this.trainBut.setDisable(true);
            this.recogniseBut.setDisable(true);
            this.saveTrainFileBut.setDisable(true);
            startCamera();
        } else {

            this.DetectFaceBut.setText("Detect Face");
            this.capTrainBut.setDisable(false);
            this.stopAcquisition();

        }
    }

    @FXML
    protected void capturetrain() {
        if (!this.cameraActive) {

            if ("".equals(this.txtlabel.getText()) || "".equals(this.txtName.getText()) || this.txtName.getText() == null || this.txtlabel.getText() == null) {

                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Error");
                alert.setContentText("Please enter text box values!");
                alert.showAndWait().ifPresent(rs -> {
                    if (rs == ButtonType.OK) {
                        alert.close();
                    }
                });
                
                return;
            }
            
            int label = Integer.parseInt(this.txtlabel.getText());
            
            if(!map.containsKey(label)){
                
                map.put(label, this.txtName.getText());
                
            }else{
                
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Label exist");
                alert.setHeaderText("Error");
                alert.setContentText("Please another label value!");
                alert.showAndWait().ifPresent(rs -> {
                    if (rs == ButtonType.OK) {
                        alert.close();
                    }
                });
                
                return;
                
            
            }
            
            this.taskID = 2;
            this.capTrainBut.setText("Stop and Add face");
            this.trainBut.setDisable(true);
            startCamera();

        } else {

            this.trainBut.setDisable(false);
            this.capTrainBut.setText("Stop and Add face");
            this.stopAcquisition();

        }
    }

    @FXML
    protected void train() {
        System.out.println("Starting training..." + this.images.size());
        this.efr = EigenFaceRecognizer.create();
        if (this.trainingFilePath == null) {

            if (this.images.size() <= 0) {

                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Training Failure..");
                alert.setHeaderText("ERROR");
                alert.setContentText("Training Failed!");
                alert.showAndWait().ifPresent(rs -> {
                    if (rs == ButtonType.OK) {
                        alert.close();
                    }
                });

                return;

            }
            MatOfInt labelsMat = new MatOfInt();
            labelsMat.fromList(this.labels);
            this.efr.train(this.images, labelsMat);

        } else {

            this.efr.read(this.trainingFilePath);

        }

        this.recogniseBut.setDisable(false);
        this.saveTrainFileBut.setDisable(false);
        this.capTrainBut.setDisable(true);
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Training Completion..");
        alert.setHeaderText("Info");
        alert.setContentText("Training is complete. Start Recognisition now!");
        alert.showAndWait().ifPresent(rs -> {
            if (rs == ButtonType.OK) {
                alert.close();
            }
        });

    }

    @FXML
    protected void reocgnise() {

        if (!this.cameraActive) {
            this.taskID = 3;
            this.recogniseBut.setText("Stop");
            this.trainBut.setDisable(true);
            startCamera();

        } else {

            this.recogniseBut.setText("Start Recognition");
            this.stopAcquisition();

        }
    }

    private void startCamera() {
        System.out.println("start camera");
        if (!this.cameraActive) {

            // start the video capture
            this.capture.open(0);

            // is the video stream available?
            if (this.capture.isOpened()) {
                this.cameraActive = true;

                // grab a frame every 100 ms (10 frames/sec)
                Runnable frameGrabber = new Runnable() {

                    @Override
                    public void run() {
                        // effectively grab and process a single frame
                        Mat frame = grabFrame();

                        // convert and show the frame
                        Image imageToShow = Utils.mat2Image(frame);
                        updateImageView(originalFrame, imageToShow);
                    }
                };

                this.timer = Executors.newSingleThreadScheduledExecutor();
                this.timer.scheduleAtFixedRate(frameGrabber, 0, 100, TimeUnit.MILLISECONDS);
            } else {
                // log the error
                System.err.println("Failed to open the camera connection...");
            }
        }
    }
    
    private Mat grabFrame() {
        Mat frame = new Mat();

        // check if the capture is open
        if (this.capture.isOpened()) {
            try {
                // read the current frame
                this.capture.read(frame);

                // if the frame is not empty, process it
                if (!frame.empty()) {
                    // face detection
                    if (this.taskID == 2) {
                        this.grabAndDisplayTrainingData(frame);
                    } else if (this.taskID == 3) {
                        this.grabAndRecognise(frame);
                    } else {
                        this.detectAndDisplay(frame);
                    }

                }

            } catch (Exception e) {
                // log the (full) error
                e.printStackTrace();
            }
        }

        return frame;
    }

    private void grabAndDisplayTrainingData(Mat frame) {
        
        MatOfRect faces = new MatOfRect();
        Mat grayFrame = new Mat();

        // convert the frame in gray scale
        Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
        // equalize the frame histogram to improve the result
        Imgproc.equalizeHist(grayFrame, grayFrame);

        // compute minimum face size (20% of the frame height, in our case)
        if (this.absoluteFaceSize == 0) {
            int height = grayFrame.rows();
            if (Math.round(height * 0.2f) > 0) {
                this.absoluteFaceSize = Math.round(height * 0.2f);
            }
        }

        // detect faces
        this.faceCascade.detectMultiScale(grayFrame, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE,
                new Size(this.absoluteFaceSize, this.absoluteFaceSize), new Size());

        // each rectangle in faces is a face: draw them!
        Rect[] facesArray = faces.toArray();

        String labelTxt = this.txtlabel.getText();

        for (int i = 0; i < facesArray.length; i++) {

            if (this.frameCounter == 0) {

                this.fileName++;
                
                // from the frame capture face only
                Mat resigedFace = new Mat(grayFrame, new Rect(facesArray[i].x - 20, facesArray[i].y - 20, facesArray[i].width + 40, facesArray[i].height + 40));

                System.out.println("Sample captured : " + this.fileName);
                
                Mat resigedFaceComplete = new Mat();
                
                //portion of frame to complete Mat object as doing recognisition on cropped image gives error
                Imgproc.resize(resigedFace, resigedFaceComplete, new Size(300, 300));

//                imwrite("/home/sangram/Desktop/test/test3/" + this.fileName + ".jpg", resigedFaceComplete);
                this.images.add(resigedFaceComplete);
                labels.add(Integer.parseInt(labelTxt));
                this.frameCounter++;

            } else if (this.frameCounter == 20) {
                this.frameCounter = 0;
            } else {
                this.frameCounter++;
            }

            Imgproc.rectangle(frame, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0), 3);
        }

    }

    private void grabAndRecognise(Mat frame) {

        MatOfRect faces = new MatOfRect();
        Mat grayFrame = new Mat();

        // convert the frame in gray scale
        Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
        // equalize the frame histogram to improve the result
        Imgproc.equalizeHist(grayFrame, grayFrame);

        // compute minimum face size (20% of the frame height, in our case)
        if (this.absoluteFaceSize == 0) {
            int height = grayFrame.rows();
            if (Math.round(height * 0.2f) > 0) {
                this.absoluteFaceSize = Math.round(height * 0.2f);
            }
        }

        // detect faces
        this.faceCascade.detectMultiScale(grayFrame, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE,
                new Size(this.absoluteFaceSize, this.absoluteFaceSize), new Size());

        // each rectangle in faces is a face: draw them!
        Rect[] facesArray = faces.toArray();
        for (int i = 0; i < facesArray.length; i++) {

            Mat resigedFace = new Mat(grayFrame, new Rect(facesArray[i].x - 20, facesArray[i].y - 20, facesArray[i].width + 40, facesArray[i].height + 40));

            Mat resigedFaceComplete = new Mat();

            Imgproc.resize(resigedFace, resigedFaceComplete, new Size(300, 300));

            int[] outLabel = new int[1];
            double[] outConf = new double[1];
            this.efr.predict(resigedFaceComplete, outLabel, outConf);

            int pos_x = (int) Math.max(facesArray[i].tl().x - 10, 0);
            int pos_y = (int) Math.max(facesArray[i].tl().y - 10, 0);

            String name = "unknown";
            int predictVal = outLabel[0];
            
            if(map.containsKey(predictVal)){
                name=map.get(predictVal);
            }

            putText(frame, name, new Point(pos_x, pos_y),
                    FONT_HERSHEY_PLAIN, 1.0, new Scalar(0, 255, 0, 2.0));

            Imgproc.rectangle(frame, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0), 3);
        }

    }
    
    private void detectAndDisplay(Mat frame) {
        MatOfRect faces = new MatOfRect();
        Mat grayFrame = new Mat();

        // convert the frame in gray scale
        Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
        
        // equalize the frame histogram to improve the result
        Imgproc.equalizeHist(grayFrame, grayFrame);
        // compute minimum face size (20% of the frame height, in our case)
        if (this.absoluteFaceSize == 0) {
            int height = grayFrame.rows();
            if (Math.round(height * 0.2f) > 0) {
                this.absoluteFaceSize = Math.round(height * 0.2f);
            }
        }

        // detect faces
        this.faceCascade.detectMultiScale(grayFrame, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE,
                new Size(this.absoluteFaceSize, this.absoluteFaceSize), new Size());

        // each rectangle in faces is a face: draw them!
        Rect[] facesArray = faces.toArray();
        for (int i = 0; i < facesArray.length; i++) {
            
            Imgproc.rectangle(frame, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0), 3);
        }

    }
    
    private void setCascadeClassifierFilePath(String classifierPath) {
        // load the classifier(s)
        CascadeClassifier faceDetector
                = new CascadeClassifier(classifierPath);
        this.faceCascade = faceDetector;

        // now the video capture can start
        this.DetectFaceBut.setDisable(false);
    }
    
    private void stopAcquisition() {
        this.cameraActive = false;
        if (this.timer != null && !this.timer.isShutdown()) {
            try {
                // stop the timer
                this.timer.shutdown();
                this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                // log any exception
                e.printStackTrace();
            }
        }

        if (this.capture.isOpened()) {
            // release the camera
            this.capture.release();
        }
    }
    
    private void updateImageView(ImageView view, Image image) {
        Utils.onFXThread(view.imageProperty(), image);
    }
    
    protected void setClosed() {
        this.stopAcquisition();
    }
}
