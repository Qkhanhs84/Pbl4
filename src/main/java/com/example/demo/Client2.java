package com.example.demo;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Client2 extends Application implements Runnable {

    private ImageView imageView;
    private boolean isRunning = false;
    private Size imgSize = new Size(700, 500);
    private int imgCompression = 100;
    private int captureTime = 1000;
    private static VideoCapture webcam;
    private static Socket imgSocket;
    private static Socket paramSocket;
    private Slider compressSlider;
    private Button switchButton;
    private TextField txtWidth, txtHeight, txtFreq;
    private Label errorLabel;
    private Button btnStart ;
    private Button btnStop ;
    private Button connectButton;
    private Button btnBack;
    private Stage primaryStage;
    private Scene startScene, clientScene;
    private TextField ipTextField;
    private TextField imgPortTextField ;
    private TextField paramPortTextField ;
    
    private static  int imgPort = 5000;
    private static  int paramPort = 5001;
    private static  String SERVER_IP = "localhost";

    public static void main(String[] args) {
        launch(args);
    }
    @Override
    public void start(Stage primaryStage) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        this.primaryStage = primaryStage;
        clientScene = createClientScene();
        startScene = createStartScene();
        primaryStage.setTitle("Image Client");
        primaryStage.setOnCloseRequest(e -> {
            System.exit(0);
        });
        primaryStage.setScene(startScene);
//        primaryStage.setResizable(false);
        primaryStage.show();

    }
    private Scene createStartScene() {

        Label titleLabel = new Label("Welcome to Client Side");
        titleLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: white;");


        ipTextField = new TextField();
        ipTextField.setPromptText("Enter Server IP...");
        ipTextField.setText("localhost");
        ipTextField.setMaxWidth(400);
        ipTextField.setStyle(
                "-fx-font-size: 14px; " +
                        "-fx-padding: 8px; " +
                        "-fx-border-radius: 10px; " +
                        "-fx-background-radius: 10px;"
        );


        imgPortTextField = new TextField();
        imgPortTextField.setPromptText("Enter Server's Image Port...");
        imgPortTextField.setText("5000");
        imgPortTextField.setMaxWidth(400);
        imgPortTextField.setStyle(
                "-fx-font-size: 14px; " +
                        "-fx-padding: 8px; " +
                        "-fx-border-radius: 10px; " +
                        "-fx-background-radius: 10px;"
        );
        paramPortTextField = new TextField();
        paramPortTextField.setPromptText("Enter Server's Parameter Port...");
        paramPortTextField.setText("5001");
        paramPortTextField.setMaxWidth(400);
        paramPortTextField.setStyle(
                "-fx-font-size: 14px; " +
                        "-fx-padding: 8px; " +
                        "-fx-border-radius: 10px; " +
                        "-fx-background-radius: 10px;"
        );
        HBox ipPortBox = new HBox(10, ipTextField, imgPortTextField, paramPortTextField);
        ipPortBox.setAlignment(Pos.CENTER);


        connectButton = new Button("Connect");
        connectButton.setStyle(
                "-fx-background-color: #16a085; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 16px; " +
                        "-fx-background-radius: 10px;"
        );


        connectButton.setOnMouseEntered(e ->
                connectButton.setStyle(
                        "-fx-background-color: #1abc9c; " +
                                "-fx-text-fill: white; " +
                                "-fx-font-size: 16px; " +
                                "-fx-background-radius: 10px;"
                )
        );
        connectButton.setOnMouseExited(e ->
                connectButton.setStyle(
                        "-fx-background-color: #16a085; " +
                                "-fx-text-fill: white; " +
                                "-fx-font-size: 16px; " +
                                "-fx-background-radius: 10px;"
                )
        );


        connectButton.setOnAction(e -> handleConnectButton());


        VBox startLayout = new VBox(20, titleLabel, ipPortBox, connectButton);
        startLayout.setAlignment(Pos.CENTER);
        startLayout.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #2980b9, #6dd5fa, #ffffff); " +
                        "-fx-padding: 50px;"
        );
        startLayout.setEffect(new javafx.scene.effect.DropShadow(20, Color.DARKGRAY));


        startScene = new Scene(startLayout, 980, 520);


        applyFadeTransition(startLayout, 1000);
        return startScene;
    }
    private void applyFadeTransition(Pane layout, int durationMillis) {
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(durationMillis), layout);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        fadeTransition.play();
    }
    private void handleConnectButton() {
        String ip = ipTextField.getText();
        String portText = imgPortTextField.getText();
        String paramPortText = paramPortTextField.getText();

        boolean validIP = isValidIP(ip);
        boolean validPort = isValidPort(portText);
        boolean validParamPort = isValidPort(paramPortText);

        if (validIP && validPort && validParamPort) {
            SERVER_IP = ip;
            imgPort = Integer.parseInt(portText);
            paramPort = Integer.parseInt(paramPortText);

            if(!connectToServer())
            {
                flashInvalidField(ipTextField, "Can't connect to server");

                return;
            }

            clientScene = createClientScene();
            primaryStage.setScene(clientScene);
            primaryStage.show();
        } else {

            if (!validIP) {
                flashInvalidField(ipTextField, "Invalid IP Address");
            }
            if (!validPort) {
                flashInvalidField(imgPortTextField, "Invalid Image Port");
            }
            if (!validParamPort) {
                flashInvalidField(paramPortTextField, "Invalid Parameter Port");
            }
        }
    }
    private boolean connectToServer() {
        try {

            imgSocket = new Socket();
            paramSocket = new Socket();
            imgSocket.connect(new InetSocketAddress(SERVER_IP, imgPort), 2000);
            paramSocket.connect(new InetSocketAddress(SERVER_IP, paramPort), 2000);
            return true;
        } catch (Exception e) {

            return false;
        }
    }
    private void flashInvalidField(TextField textField, String message) {
        String originalStyle = "-fx-font-size: 14px; " +
                "-fx-padding: 8px; " +
                "-fx-border-radius: 10px; " +
                "-fx-background-radius: 10px;";
        textField.setText(message);

        Timeline timeline = new Timeline(

                new KeyFrame(Duration.millis(100), e -> textField.setStyle(originalStyle+"-fx-background-color: #ffcccc;")),
                new KeyFrame(Duration.millis(200), e -> textField.setStyle(originalStyle+"-fx-background-color: white;"))
        );

        timeline.setCycleCount(4);
        timeline.setOnFinished(e -> {
            textField.setStyle(originalStyle);
            textField.setText("");
        });
        timeline.play();
    }

    private boolean isValidIP(String ip) {
        if(ip.equals("localhost")) return true;
        String ipPattern =
                "^((25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)$";
        return ip.matches(ipPattern);
    }

    private boolean isValidPort(String portText) {
        try {
            int port = Integer.parseInt(portText);
            return port >= 0 && port <= 65535;
        } catch (NumberFormatException e) {
            return false;
        }
    }



    private Scene createClientScene() {

        StackPane imageContainer = new StackPane();
        imageContainer.setStyle("-fx-border-color: #3498db; -fx-border-width: 3px; -fx-border-radius: 20px;");

        imageView = new ImageView();

        imageView.setFitWidth(630);
        imageView.setFitHeight(480);
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle();
        clip.setArcWidth(40);
        clip.setArcHeight(40);
        clip.setWidth(630);
        clip.setHeight(480);
        imageView.setClip(clip);


        imageContainer.getChildren().add(imageView);
        btnStart = createButton("Start", "#3498db", "white", "#2980b9");
        btnStop = createButton("Stop", "#e74c3c", "white", "#c0392b");

        switchButton = btnStart;
        errorLabel = new Label();

        switchButton.setOnAction(e -> {
            if (switchButton.getText().equals("Start")) {
                isRunning = true;
                try {
                    if(imgSocket.isClosed()) {
                        imgSocket = new Socket();
                        imgSocket.connect(new InetSocketAddress(SERVER_IP, imgPort), 2000);
                    }
                    if(paramSocket.isClosed()) {
                        paramSocket = new Socket();
                        paramSocket.connect(new InetSocketAddress(SERVER_IP, paramPort), 2000);
                    }

                }
                catch (Exception ex) {
                    showErrorMessage("Can't connect to server!!!");
                    return;
                }
                Thread thread = new Thread(this);
                thread.start();
                new Thread(() -> {
                    try {


                        DataInputStream dis = new DataInputStream(paramSocket.getInputStream());
                        while(true) {
                            if (dis.available() > 0) {
                                String type = dis.readUTF();
                                String value = dis.readUTF();
                                Platform.runLater(() -> {
                                    if (type.equals("width")) {
                                        imgSize.width = Integer.parseInt(value);
                                        txtWidth.setText(value);
                                    } else if (type.equals("height")) {
                                        imgSize.height = Integer.parseInt(value);
                                        txtHeight.setText(value);
                                    } else if (type.equals("frequency")) {
                                        captureTime = Integer.parseInt(value);
                                        txtFreq.setText(value);
                                    } else if (type.equals("compression")) {
                                        imgCompression = Integer.parseInt(value);
                                        compressSlider.setValue(imgCompression);
                                    }
                                });

                            }
                        }


                    } catch (Exception ex) {
                        reset();
                    }
                }).start();
            } else {
                isRunning = false;
                try {
                    imgSocket.close();
                    paramSocket.close();
                    webcam.release();
                    imageView.setImage(null);
                } catch (Exception ex) {

                }
            }

            updateButtonStyle(switchButton, isRunning);

        });


        txtWidth = createTextField("Width");
        txtWidth.setText( (int)imgSize.width+ "");
        txtHeight = createTextField("Height");
        txtHeight.setText((int)imgSize.height + "");
        txtFreq = createTextField("Frequency");
        txtFreq.setText(captureTime + "");
        txtWidth.setOnAction(e -> {
            String value = txtWidth.getText();
            try{
                int valueInt = Integer.parseInt(value);
                imgSize.width = valueInt;
                sendToServer("width", value);
            }
            catch(NumberFormatException er) {
                txtWidth.setText("Invalid");
                return;
            }
        });
        txtHeight.setOnAction(e -> {
            String value = txtHeight.getText();
            try{
                int valueInt = Integer.parseInt(value);
                imgSize.height = valueInt;
                sendToServer("height", value);
            }
            catch(NumberFormatException er) {
                txtHeight.setText("Invalid");
                return;
            }
        });
        txtFreq.setOnAction(e -> {
            String value = txtFreq.getText();
            try{
                int valueInt = Integer.parseInt(value);
                captureTime = valueInt;
                sendToServer("frequency", value);
            }
            catch(NumberFormatException er) {
                txtFreq.setText("Invalid");
                return;
            }
        });
        HBox sizeBox = new HBox(10, txtWidth, new Label("x"), txtHeight);
        sizeBox.setAlignment(Pos.CENTER);
        HBox freqBox = new HBox(10, txtFreq);
        freqBox.setAlignment(Pos.CENTER);


        compressSlider = new Slider(1, 100, imgCompression);


        Label compressLabel = new Label((int) compressSlider.getValue() + "%");
        compressSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            compressLabel.setText(newValue.intValue() + "%");
            imgCompression = newValue.intValue();
            sendToServer("compression", String.valueOf(imgCompression));
        });

        HBox compressBox = new HBox(10, compressSlider, compressLabel);
        compressBox.setAlignment(Pos.CENTER);

        VBox backToStart = new VBox(20);
        backToStart.setAlignment(Pos.CENTER);
        btnBack = new Button("Back to Home");
        btnBack.setStyle("-fx-background-color: #66BB6A; -fx-text-fill: white; -fx-font-size: 16px;");
        btnBack.setOnMouseEntered(e -> btnBack.setStyle("-fx-background-color: #81C784; -fx-text-fill: white; -fx-font-size: 16px;"));
        btnBack.setOnMouseExited(e -> btnBack.setStyle("-fx-background-color: #66BB6A; -fx-text-fill: white; -fx-font-size: 16px;"));

        btnBack.setOnAction(event -> {
            try {

                imgSocket.close();
                paramSocket.close();
                webcam.release();
                imageView.setImage(null);
            } catch (Exception e) {

            }
            startScene = createStartScene();
            primaryStage.setScene(startScene);
            primaryStage.show();
        });
        backToStart.getChildren().addAll(btnBack);


        VBox propertiesBox = new VBox(20, errorLabel,switchButton, sizeBox, freqBox, compressBox, backToStart);
        propertiesBox.setAlignment(Pos.CENTER);
        propertiesBox.setMaxWidth(280);
        propertiesBox.setMinHeight(480);
        propertiesBox.setStyle("-fx-background-color: #7ec8e3; "
                + "-fx-border-radius: 15px; -fx-background-radius: 15px; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 0); "
                + "-fx-padding: 20px;");

        VBox cameraBox = new VBox( imageContainer);
        cameraBox.setAlignment(Pos.CENTER);


        HBox mainBox = new HBox(30, cameraBox, propertiesBox);
        mainBox.setAlignment(Pos.CENTER);
        mainBox.setStyle("-fx-background-color: linear-gradient(to bottom, #74ebd5, #ACB6E5); -fx-padding: 30px;");


        Scene scene = new Scene(mainBox, 980, 520);
        return scene;
    }


    private Button createButton(String text, String backgroundColor, String textColor, String hoverColor) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: " + backgroundColor + "; -fx-text-fill: " + textColor + "; "
                + "-fx-font-size: 14px; -fx-font-weight: bold; -fx-border-radius: 10px; -fx-padding: 10px 20px;");


        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: " + hoverColor + "; "
                + "-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; "
                + "-fx-border-radius: 10px; -fx-padding: 10px 20px;"));

        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: " + backgroundColor + "; "
                + "-fx-text-fill: " + textColor + "; -fx-font-size: 14px; -fx-font-weight: bold; "
                + "-fx-border-radius: 10px; -fx-padding: 10px 20px;"));

        return button;
    }
    private void updateButtonStyle(Button button, boolean isRunning) {
        String backgroundColor;
        String borderColor;
        String hoverColor;

        if (isRunning) {
            backgroundColor = "#e74c3c";
            borderColor = "#c0392b";
            hoverColor = "#c0392b";
            button.setText("Stop");
        } else {
            backgroundColor = "#3498db";
            borderColor = "#2980b9";
            hoverColor = "#2980b9";
            button.setText("Start");
        }





        button.setOnMouseEntered(e -> {
            button.setStyle("-fx-background-color: " + hoverColor + "; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-size: 14px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-border-radius: 10px; " +
                    "-fx-padding: 10px 20px;");
        });


        button.setOnMouseExited(e -> {
            button.setStyle("-fx-background-color: " + backgroundColor + "; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-size: 14px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-border-radius: 10px; " +
                    "-fx-padding: 10px 20px;");
        });
    }





    private TextField createTextField(String promptText) {
        TextField textField = new TextField();
        textField.setPromptText(promptText);
        textField.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: #333333; "
                + "-fx-border-radius: 5px; -fx-border-color: #ccc; -fx-padding: 5px 10px;"
        + "-fx-background-radius: 5px;");
        return textField;
    }


    public void sendToServer(String type, String value) {
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(paramSocket.getOutputStream());
            dataOutputStream.writeUTF(type);
            dataOutputStream.writeUTF(value);
        } catch (Exception e) {
            reset();

        }
    }


    public void showErrorMessage(String message) {
        if (errorLabel == null) {
            errorLabel = new Label();

        }
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        FadeTransition fadeTransition = new FadeTransition();
        fadeTransition.setNode(errorLabel);
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.0);
        fadeTransition.setCycleCount(8);
        fadeTransition.setAutoReverse(true);
        fadeTransition.setDuration(Duration.millis(400));
        fadeTransition.setOnFinished(event -> errorLabel.setVisible(false));


        fadeTransition.play();
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        try
        {

            OutputStream outputStream = imgSocket.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
            DataInputStream dataInputStream = new DataInputStream(imgSocket.getInputStream());

            System.out.println("Đã kết nối đến server.");
            webcam = new VideoCapture(0);
            if (!webcam.isOpened()) {
                System.out.println("Không thể mở webcam");
                return;
            }
            Mat frame = new Mat();
            Mat flipFrame = new Mat();
            Mat resizedFrame = new Mat();
            while (true) {
                if (!webcam.isOpened()) {
                    System.out.println("Không thể mở webcam");
                    reset();
                    break;
                }
                if (dataInputStream.available() > 0) {
                    String message = dataInputStream.readUTF();
                    if (message.equals("frequency")) {
                        captureTime = Integer.parseInt(dataInputStream.readUTF());
                        txtFreq.setText(String.valueOf(captureTime));

                    } else if (message.equals("imageSize")) {
                        String imageSize = dataInputStream.readUTF();
                        String[] size = imageSize.split("x");
                        int width = Integer.parseInt(size[0]);
                        int height = Integer.parseInt(size[1]);
                        imgSize = new Size(width, height);
                        txtWidth.setText(String.valueOf(width));
                        txtHeight.setText(String.valueOf(height));



                    } else if (message.equals("compression")) {
                        imgCompression = Integer.parseInt(dataInputStream.readUTF());
                        Platform.runLater(() -> {
                            compressSlider.setValue(imgCompression);
                        });
                    }
                }

                webcam.read(frame);
                Core.flip(frame, flipFrame, 1);
                Imgproc.resize(flipFrame, resizedFrame, imgSize);

                WritableImage writableImage = matToWritableImage(resizedFrame);
                imageView.setImage(writableImage);

                long currentTime = System.currentTimeMillis();
                if (currentTime - startTime > captureTime) {

                    MatOfByte matOfByte = new MatOfByte();
                    MatOfInt compressionParams = new MatOfInt(Imgcodecs.IMWRITE_JPEG_QUALITY, imgCompression);
                    Imgcodecs.imencode(".jpg", resizedFrame, matOfByte, compressionParams);
                    byte[] byteArray = matOfByte.toArray();
                    try {
                        dataOutputStream.writeInt(byteArray.length);
                        dataOutputStream.write(byteArray);
                    } catch (IOException e) {
                        imgSocket.close();
                        paramSocket.close();
                        webcam.release();
                        imageView.setImage(null);
                        System.out.println("Đóng kết nối");
                    }
                    startTime = currentTime;
                }

            }
        }
        catch (Exception e) {
            reset();



        }
    }
    private void reset(){
        isRunning = false;


        Platform.runLater(() -> {
            updateButtonStyle(switchButton, isRunning);

            showErrorMessage("Can't connect to server!!!");
        });
    }
    private WritableImage matToWritableImage(Mat mat) {

        if (mat.type() != CvType.CV_8UC3) {
            throw new IllegalArgumentException("Ảnh Mat phải là loại CV_8UC3");
        }

        int width = mat.width();
        int height = mat.height();
        WritableImage image = new WritableImage(width, height);
        PixelWriter pixelWriter = image.getPixelWriter();
        byte[] buffer = new byte[width * height * 3];
        mat.get(0, 0, buffer);


        int index = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int r = buffer[index + 2] & 0xFF;
                int g = buffer[index + 1] & 0xFF;
                int b = buffer[index] & 0xFF;
                pixelWriter.setArgb(x, y, (0xFF << 24) | (r << 16) | (g << 8) | b);
                index += 3;
            }
        }

        return image;
    }


}
