package com.example.demo;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Server extends Application implements Runnable {
    private Stage primaryStage;
    private Scene startScene;
    private Scene serverScene;
    private VBox imageBox;
    private Button startButton;
    private Button backButton;
    private TextField imagePortTextField;
    private TextField paramPortTextField;
    private String serverIP;
    private int imagePort;
    private int parameterPort;
    private ServerSocket imageSocket;
    private ServerSocket paramSocket;
    private List<ImgClient> imgClientList = new ArrayList<>();

    private static Server instance;
    private Thread serverThread;
    public Server() {
        instance = this;
    }

    public static Server getInstance() {

        return instance;
    }



    public static void main(String[] args) {
        Server server = new Server();
        Server.instance = server;
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Server Application - Image Receiver");

        // Tạo các Scene và gán cho thuộc tính của lớp
        startScene = createStartScene();
        serverScene = createServerScene();

        // Thiết lập scene ban đầu là Start Scene
        primaryStage.setScene(startScene);
        primaryStage.setOnCloseRequest(e -> System.exit(0));
        primaryStage.show();
    }

    // Hàm tạo Start Scene

    private Scene createStartScene() {
        VBox startLayout = new VBox(20);
        startLayout.setAlignment(Pos.TOP_CENTER);
        startLayout.setStyle("-fx-background-color: linear-gradient(to bottom, #141E30, #243B55, #1D4E89); -fx-padding: 30px;");
        Image universityImage = new Image("D:\\TERM5\\PBL4\\logo.png");  // Cập nhật đường dẫn tới ảnh của bạn
        ImageView universityImageView = new ImageView(universityImage);
        universityImageView.setFitWidth(500);
        universityImageView.setFitHeight(250);
        universityImageView.setPreserveRatio(true);



        // Tiêu đề chính
        Label titlePbl4 = new Label("PBL4: DỰ ÁN HỆ ĐIỀU HÀNH\n" +
                "          MẠNG MÁY TÍNH");
        titlePbl4.setStyle(
                "-fx-padding: 20px ;" +
                "-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.75), 5, 0.5, 0, 1);"
        );
        Label titleLabel = new Label("Xây dựng hệ thống thu nhận hình ảnh từ webcam qua Internet");
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF;");




        // Địa chỉ IP của server
        try {
            serverIP = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            System.out.println("Error: " + e.getMessage());
        }

        Label infoLabel = new Label("Server IP: " + serverIP);
        infoLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #A9C4EB; -fx-padding: 5px;");

        // TextField để nhập Port, chiều dài ngắn hơn
        Label portLabel = new Label("Nhập Port:");
        portLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #A9C4EB;");


        imagePortTextField = new TextField();
        imagePortTextField.setText("5000");
        imagePortTextField.setMaxSize(400, 40);
        imagePortTextField.setPromptText("Nhập cổng (Port)");
        imagePortTextField.setStyle(
                "-fx-font-size: 16px; -fx-background-color: #3A4A61; " +
                        "-fx-text-fill: #FFFFFF; -fx-border-color: #A9C4EB; -fx-border-radius: 5px; -fx-background-radius: 5px; " +
                        "-fx-padding: 5px; "
        );
        paramPortTextField = new TextField();
        paramPortTextField.setText("5001");
        paramPortTextField.setMaxSize(400, 40);
        paramPortTextField.setPromptText("Nhập cổng (Port)");
        paramPortTextField.setStyle(
                "-fx-font-size: 16px; -fx-background-color: #3A4A61; " +
                        "-fx-text-fill: #FFFFFF; -fx-border-color: #A9C4EB; -fx-border-radius: 5px; -fx-background-radius: 5px; " +
                        "-fx-padding: 5px; "
        );
        HBox portBox = new HBox(20);
        portBox.setAlignment(Pos.CENTER);
        portBox.getChildren().addAll(imagePortTextField, paramPortTextField);



        startButton = new Button("Start Server");
        startButton.setPrefSize(150, 40);
        startButton.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #4CAF50, #388E3C); -fx-text-fill: white; " +
                        "-fx-font-size: 16px; -fx-background-radius: 20px; -fx-border-radius: 20px; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 5, 0.5, 0, 1);"
        );
        startButton.setOnMouseEntered(e -> startButton.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #45A049, #2E7D32); -fx-text-fill: white; " +
                        "-fx-font-size: 16px; -fx-background-radius: 20px; -fx-border-radius: 20px; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.75), 8, 0.7, 0, 2);"
        ));
        startButton.setOnMouseExited(e -> startButton.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #4CAF50, #388E3C); -fx-text-fill: white; " +
                        "-fx-font-size: 16px; -fx-background-radius: 20px; -fx-border-radius: 20px; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 5, 0.5, 0, 1);"
        ));
        startButton.setOnAction(event -> {
            handleStartButton();
        });


        startLayout.getChildren().addAll(universityImageView , titlePbl4,titleLabel, infoLabel, portLabel, portBox, startButton);
        return new Scene(startLayout, 1200, 800);
    }
    private void handleStartButton(){
        String imagePortInput = imagePortTextField.getText();
        String paramPortInput = paramPortTextField.getText();
        if (isValidPort(imagePortInput) && isValidPort(paramPortInput)) {
            imagePort = Integer.parseInt(imagePortInput);
            parameterPort = Integer.parseInt(paramPortInput);
            if(imagePort == parameterPort) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Port không được trùng nhau!");
                alert.showAndWait();
                return;
            }
            try {
                imageSocket = new ServerSocket(imagePort);
                paramSocket = new ServerSocket(parameterPort);
                serverThread = new Thread(this);
                serverThread.start();
                serverScene = createServerScene();
                primaryStage.setScene(serverScene);
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Không thể khởi động server!");
                alert.showAndWait();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Port không hợp lệ!");
            alert.showAndWait();
        }

    }
    private boolean isValidPort(String port) {
        try {
            int portNumber = Integer.parseInt(port);
            return portNumber > 0 && portNumber <= 65535;
        } catch (NumberFormatException e) {
            return false;
        }
    }



    private Scene createServerScene() {


        VBox serverLayout = new VBox(20);
        serverLayout.setAlignment(Pos.CENTER);
        serverLayout.setStyle("-fx-background-color: linear-gradient(to bottom, #141E30, #243B55,#1D4E89);");

        VBox headerBox = new VBox(10);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setStyle("-fx-padding: 15px;");




        Label serverInfoLabel = new Label("Server IP: " + serverIP);
        serverInfoLabel.setStyle("-fx-font-size: 19px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF;");

        Label serverPortLabel = new Label("Image Port: " + imagePort + "    |   Parameter Port: " + parameterPort);
        serverPortLabel.setStyle("-fx-font-size: 17px; -fx-text-fill: #FFFFFF;");




        headerBox.getChildren().addAll( serverInfoLabel, serverPortLabel);
        imageBox = new VBox(10);
        imageBox.setAlignment(Pos.TOP_CENTER);
        imageBox.setStyle(
                "-fx-background-color: #073A4B; " +
                "-fx-padding: 20px; "



        );



        ScrollPane scrollPane = new ScrollPane(imageBox);
        scrollPane.setPrefSize(800, 900);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);





        HBox footerBox = new HBox();
        footerBox.setAlignment(Pos.CENTER);
        footerBox.setStyle("-fx-padding: 15px;");

        backButton = new Button("Back to Home");
        backButton.setPrefSize(150, 40);
        backButton.setStyle("-fx-background-color: #FF5722; -fx-text-fill: white; -fx-font-size: 16px;");
        backButton.setOnMouseEntered(e -> backButton.setStyle("-fx-background-color: #E64A19; -fx-text-fill: white; -fx-font-size: 16px;"));
        backButton.setOnMouseExited(e -> backButton.setStyle("-fx-background-color: #FF5722; -fx-text-fill: white; -fx-font-size: 16px;"));
        backButton.setOnAction(event -> {
            try {
                imageSocket.close();
                paramSocket.close();
                serverThread = null;

                for(ImgClient imgClient : imgClientList){
                    imgClient.getImgSocket().close();
                    imgClient.getParamSocket().close();

                }
            } catch (Exception e) {

            }
            imageBox.getChildren().clear();
            startScene = createStartScene();
            primaryStage.setScene(startScene);
        });
        footerBox.getChildren().add(backButton);



        serverLayout.getChildren().addAll(headerBox, scrollPane, footerBox);

        return new Scene(serverLayout, 1200, 800);
    }
    private HBox createClientScene(ImgClient imgClient) {
        VBox propertyBox = new VBox(15);
        propertyBox.setAlignment(Pos.TOP_CENTER);
        propertyBox.setMaxWidth(280);
        propertyBox.setMinHeight(480);
        propertyBox.setMinWidth(300);
        propertyBox.setStyle(
                "-fx-padding: 20px; " +
                        "-fx-background-color: #0C6478;"  +
                        "-fx-border-color: #09D1C7; " + "-fx-border-width: 2px; " +
                        "-fx-border-radius: 15px; " +
                        "-fx-background-radius: 15px; " +
                        "-fx-effect: dropshadow(three-pass-box);"
        );

        imgClient.setTxtWidth(createTextField("Width"));
        imgClient.getTxtWidth().setText("700");

        imgClient.setTxtHeight(createTextField("Height"));
        imgClient.getTxtHeight().setText("500");
        imgClient.setTxtFrequency(createTextField("Frequency"));
        imgClient.getTxtFrequency().setText("1000");
        imgClient.getTxtFrequency().setMaxWidth(120);
        imgClient.setTxtCompression(createTextField("Compression"));
        imgClient.getTxtCompression().setText("100");
        imgClient.getTxtCompression().setMaxWidth(120);

        imgClient.getTxtWidth().setOnAction(e -> {
            String value = imgClient.getTxtWidth().getText().trim();
            try{
                int width = Integer.parseInt(value);
                sendToClient(imgClient, "width", value);

            }
            catch (Exception ex){
                imgClient.getTxtWidth().setText("");
            }

        });
        imgClient.getTxtHeight().setOnAction(e -> {
            String value = imgClient.getTxtHeight().getText().trim();
            try{
                int height = Integer.parseInt(value);
                sendToClient(imgClient, "height", value);

            }
            catch (Exception ex){
                imgClient.getTxtHeight().setText("");
            }
        });
        imgClient.getTxtFrequency().setOnAction(e -> {
            String value = imgClient.getTxtFrequency().getText().trim();
            try{
                int frequency = Integer.parseInt(value);
                sendToClient(imgClient, "frequency", value);

            }
            catch (Exception ex){
                imgClient.getTxtFrequency().setText("");
            }
        });
        imgClient.getTxtCompression().setOnAction(e -> {
            String value = imgClient.getTxtCompression().getText().trim();
            try{
                int compression = Integer.parseInt(value);
                if(compression < 0 || compression > 100){
                    throw new Exception();
                }
                sendToClient(imgClient, "compression", value);

            }
            catch (Exception ex){
                imgClient.getTxtCompression().setText("");
            }
        });



        imgClient.getTitle().setStyle("-fx-font-size: 16px; -fx-text-fill: #F4E7FB;"+
                "-fx-padding: 0px 0px 50px 0px;");

        Label labelX = new Label("x");
        labelX.setStyle("-fx-font-size: 16px; -fx-text-fill: #F4E7FB;");
        HBox sizeBox = new HBox(10, imgClient.getTxtWidth(), labelX, imgClient.getTxtHeight());
        sizeBox.setAlignment(Pos.CENTER);
        Label frequencyLabel = new Label("Frequency (ms):   ");
        frequencyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #F4E7FB;");

        HBox freqBox = new HBox(10,frequencyLabel, imgClient.getTxtFrequency());
        freqBox.setAlignment(Pos.BASELINE_LEFT);

        Label compressionLabel = new Label("Compression (%):");
        compressionLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #F4E7FB;");
        HBox compressionBox = new HBox(10,compressionLabel, imgClient.getTxtCompression());

        compressionBox.setAlignment(Pos.BASELINE_LEFT);


        propertyBox.getChildren().addAll(imgClient.getTitle(),sizeBox, freqBox,compressionBox);
        imgClient.setImageView(new ImageView(imgClient.getImage()));
        imgClient.getImageView().setStyle("-fx-border-color: #09D1C7; -fx-border-width: 2px; -fx-border-radius: 15px; -fx-background-radius: 15px;");
        VBox imageBox = new VBox(10, imgClient.getImageView());
        imageBox.setAlignment(Pos.CENTER);
        HBox mainBox = new HBox(30, imageBox, propertyBox);
        mainBox.setAlignment(Pos.CENTER);
        mainBox.setStyle("-fx-padding: 20px; -fx-background-color: #213A58;" +
                "-fx-border-color: #06D7A0; " + "-fx-border-width: 2px; " + "-fx-border-radius: 15px;"
        + "-fx-background-radius: 15px;");
        return mainBox;
    }
    private void sendToClient( ImgClient imgClient,String type, String value) {

        try {
            DataOutputStream dos = new DataOutputStream(imgClient.getParamSocket().getOutputStream());
            dos.writeUTF(type);
            dos.writeUTF(value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void insertOrUpdateClient(ImgClient imgClient) {
        synchronized (this) {
            if(!imgClientList.contains(imgClient)){
                imgClientList.add(imgClient);
                HBox clientBox = createClientScene(imgClient);
                imgClient.setMainBox(clientBox);
                imageBox.getChildren().add(clientBox);
            }
            else{
                imgClient.getImageView().setImage(imgClient.getImage());
            }



        }


    }
    public void removeClient(ImgClient imgClient) {
        synchronized (this) {
            imageBox.getChildren().remove(imgClient.getMainBox());
            imgClientList.remove(imgClient);
        }
    }
    public void setParam(ImgClient imgClient,String type, String value){
        synchronized (this){
            if(type.equals("width")){
                imgClient.getTxtWidth().setText(value);
            }
            else if(type.equals("height")){
                imgClient.getTxtHeight().setText(value);
            }
            else if(type.equals("frequency")){
                imgClient.getTxtFrequency().setText(value);
            }
            else if(type.equals("compression")){
                imgClient.getTxtCompression().setText(value);

            }
        }
    }
    private TextField createTextField(String promptText) {
        TextField textField = new TextField();
        textField.setPromptText(promptText);
        textField.setStyle("-fx-background-color: #ECEFF1; " +
                "-fx-text-fill: #2C2C2C; " +
                "-fx-border-color: #09D1C7; " +
                "-fx-border-width: 2px; " +
                "-fx-border-radius: 5px; " +
                "-fx-background-radius: 5px; " +
                "-fx-padding: 5px 10px;"  );
        return textField;
    }


    @Override
    public void run() {
        try {
            while(true){
                Socket clentImgSocket = imageSocket.accept();
                Socket clientParamSocket = paramSocket.accept();
                ImgClient imgClient = new ImgClient(clentImgSocket, clientParamSocket);
                new Thread(imgClient).start();
            }
        } catch (Exception e) {
            System.out.println("hel");
        }
    }
}
class ImgClient implements Runnable{
    private Socket imgSocket;
    private Socket paramSocket;
    public ImgClient(Socket imgSocket, Socket paramSocket) {
        this.imgSocket = imgSocket;
        this.paramSocket = paramSocket;
        Date date = new Date(System.currentTimeMillis());
        String timeConnect = "     Connected at: \n       " + date.getDate() + "/" + (date.getMonth() + 1)
                + "/" + (date.getYear() + 1900) + "\n          " + date.getHours() + ":"
                + date.getMinutes() + ":" + date.getSeconds();
        title = new Label("   Client IP: " + imgSocket.getLocalAddress() +"\n"+
                 timeConnect);
    }
    private ImageView imageView;
    private Image image;

    public Label getTitle() {
        return title;
    }

    public void setTitle(Label title) {
        this.title = title;
    }
    private HBox mainBox;

    public HBox getMainBox() {
        return mainBox;
    }

    public void setMainBox(HBox mainBox) {
        this.mainBox = mainBox;
    }

    private TextField txtWidth;
    private TextField txtHeight;
    private TextField txtFrequency;
    private TextField txtCompression;

    public TextField getTxtCompression() {
        return txtCompression;
    }

    public void setTxtCompression(TextField txtCompression) {
        this.txtCompression = txtCompression;
    }

    private Label title;



    public Socket getImgSocket() {
        return imgSocket;
    }

    public void setImgSocket(Socket imgSocket) {
        this.imgSocket = imgSocket;
    }

    public Socket getParamSocket() {
        return paramSocket;
    }

    public void setParamSocket(Socket paramSocket) {
        this.paramSocket = paramSocket;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    public TextField getTxtWidth() {
        return txtWidth;
    }

    public void setTxtWidth(TextField txtWidth) {
        this.txtWidth = txtWidth;
    }

    public TextField getTxtHeight() {
        return txtHeight;
    }

    public void setTxtHeight(TextField txtHeight) {
        this.txtHeight = txtHeight;
    }

    public TextField getTxtFrequency() {
        return txtFrequency;
    }

    public void setTxtFrequency(TextField txtFrequency) {
        this.txtFrequency = txtFrequency;
    }





    @Override
    public void run() {
        new Thread(() -> {
            try {
                DataInputStream dis = new DataInputStream(imgSocket.getInputStream());
                while(true){
                    int len = dis.readInt();
                    System.out.println(len);
                    byte[] data = new byte[len];
                    dis.readFully(data);
                    image = new Image(new ByteArrayInputStream(data));
                    Platform.runLater(() -> {

                        Server.getInstance().insertOrUpdateClient(this);
                    });



                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    Server.getInstance().removeClient(this);
                });

            }
        }).start();
        new Thread(() -> {
            try {
                DataInputStream dis = new DataInputStream(paramSocket.getInputStream());
                while(true){
                    if (dis.available() > 0) {
                        String type = dis.readUTF();
                        String value = dis.readUTF();
                        Platform.runLater(() -> {
                            Server.getInstance().setParam(this, type, value);
                        });
                    }
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    Server.getInstance().removeClient(this);
                });
            }
        }).start();
    }
}

