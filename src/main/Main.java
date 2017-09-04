package main;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class Main extends Application {

    File selectedImageFile = null;
    Image image = null;

    HBox topContentPane, centerContentPane, bottomContentPane;

    Text statusMessage;

    Button imageFileChooserButton;
    Button flipImageOverXButton;
    Button flipImageOverYButton;
    Button flipImageOverXYButton;
    Button frameImageWithBorderButton;
    Button moveImageHorizontallyButton;
    Button animateImageHorizontallyButton;
    Button splitImageIntoTilesButton;
    Button showImageHistogram;

    ImageProcessor imageProcessor;
    PluginInterface pluginInterface = new PluginInterface(imageProcessor);

    @Override
    public void start(Stage primaryStage) throws Exception {
        BorderPane root = new BorderPane();

        // File chooser
        FileChooser imageFileChooser = new FileChooser();
        imageFileChooser.setTitle("Choose image file");
        imageFileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"));

        // Layout panes
        topContentPane = new HBox();
        topContentPane.setAlignment(Pos.CENTER);
        topContentPane.setSpacing(10);
        topContentPane.setPadding(new Insets(10, 0, 10, 0));
        centerContentPane = new HBox();
        centerContentPane.setAlignment(Pos.CENTER);
        centerContentPane.setSpacing(10);
        centerContentPane.setPadding(new Insets(10, 0, 10, 0));
        bottomContentPane = new HBox();
        bottomContentPane.setAlignment(Pos.CENTER);
        bottomContentPane.setSpacing(10);
        bottomContentPane.setPadding(new Insets(10, 0, 10, 0));


        // Layout elements
        imageFileChooserButton = new Button("Choose file:");
        Button[] toolButtons = {
            flipImageOverXButton = new Button("Flip X"),
            flipImageOverYButton = new Button("Flip Y"),
            flipImageOverXYButton = new Button("Flip XY"),
            frameImageWithBorderButton = new Button("Frame Image"),
            moveImageHorizontallyButton = new Button("Move ->"),
            animateImageHorizontallyButton = new Button("Move ->>>"),
            splitImageIntoTilesButton = new Button("Split"),
            showImageHistogram = new Button("Histogram")
        };
        for (Button btn : toolButtons)
            btn.setDisable(true);
        statusMessage = new Text();


        // Layout inclusions
        topContentPane.getChildren().add(imageFileChooserButton);
        for (Button btn : toolButtons)
            centerContentPane.getChildren().add(btn);
        bottomContentPane.getChildren().add(statusMessage);

        // Application logic
        imageFileChooserButton.setOnAction(choose -> {

            selectedImageFile = imageFileChooser.showOpenDialog(new Stage());
            if (selectedImageFile != null) {
                try {
                    image = new Image(new FileInputStream(selectedImageFile.getAbsolutePath()));
                    for (Button btn : toolButtons)
                        btn.setDisable(false);
                    statusMessage.setText("Image opened...");
                } catch (FileNotFoundException e) {
                    statusMessage.setText("An error occured while reading the selected file; may not exist.");
                }
            }

            if (image != null) {
                if (imageProcessor != null)
                    imageProcessor.nullAndClose();

                imageProcessor = new ImageProcessor(image);
            }
        });
        flipImageOverXButton.setOnAction(flipX -> { imageProcessor.flipImageOverX(); });
        flipImageOverYButton.setOnAction(flipY -> { imageProcessor.flipImageOverY(); });
        flipImageOverXYButton.setOnAction(flipXY -> { imageProcessor.flipImageOverXY(); });
        frameImageWithBorderButton.setOnAction(frame -> { imageProcessor.frameImageWithBorder(); });
        moveImageHorizontallyButton.setOnAction(prompt -> {
            Text message = new Text("Distance to move in percentage:");
            TextField input = new TextField();
            TextArea status = new TextArea();
            status.setEditable(false);
            status.setPrefRowCount(2);
            status.setPrefColumnCount(20);
            status.setWrapText(true);

            VBox inputContainer = new VBox(message, input, status);
            inputContainer.setPadding(new Insets(10));
            inputContainer.setSpacing(10);
            inputContainer.setAlignment(Pos.CENTER);

            Stage inputStage = new Stage();
            inputStage.setScene(new Scene(inputContainer));

            input.setOnAction(value -> {
                    try {
                        int percentage = Integer.parseInt(input.getText().trim());
                        if (percentage < 0 | percentage > 100)
                            status.setText("Illegal number. Must be between 0 and 100 only.");
                        else {
                            System.out.println("Go go gadget move!");
                            imageProcessor.moveImageHorizontally(percentage);
                            inputStage.close();
                        }
                    } catch (NumberFormatException e) {
                        status.setText("Illegal input. Integer between 0 and 100 only.");
                    }
            });

            inputStage.show();

        });
        animateImageHorizontallyButton.setOnAction(cycle -> { imageProcessor.animateImageHorizontally(); });
        splitImageIntoTilesButton.setOnAction(split -> { imageProcessor.displayImageTiles(imageProcessor.splitImage()); });
        showImageHistogram.setOnAction(histogram -> { imageProcessor.showImageHistogram(); });

        root.setPadding(new Insets(20));
        root.setTop(topContentPane);
        root.setCenter(centerContentPane);
        root.setBottom(bottomContentPane);


        primaryStage.setScene(new Scene(root));
        primaryStage.setOnCloseRequest(closeRequest -> { System.exit(0); });
        primaryStage.getIcons().add(new Image(new FileInputStream("media/app.png")));
        primaryStage.setResizable(false);
        primaryStage.setTitle("ImageFX");
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
