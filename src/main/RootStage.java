package main;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;

public class RootStage extends Stage {

    public static ArrayList<Color> stageColors;
    private static Iterator<Color> stageColorIterator;

    static {
        stageColors = new ArrayList<>();
        stageColors.add(Color.RED);
        stageColors.add(Color.YELLOW);
        stageColors.add(Color.GREEN);
        stageColors.add(Color.BLUE);
        // TODO- Add colors
        stageColorIterator = stageColors.iterator();
    }

    private Scene rootScene;
    private VBox rootPane;
    private TabPane tabs;
    private Tab openTab, pluginTab;
    private ToolTab toolTab;
    private InfoTab infoTab;
    private HBox pluginTabPaneRoot;
    private VBox openTabPaneRoot;
    private HBox openTabPaneTop, openTabPaneBottom;

    private Button imageFileChooserButton;
    private Button createGaussianDistributedImage;
    private Button testMyShitButton;
    private TextField testMyShitInput;

    private Text statusMessage;
    private VBox statusPane;

    FileChooser imageFileChooser;
    File selectedImageFile = null;

    Image image = null;
    ImageProcessor imageProcessor;
    ImageProcessor.ImageDetails imageDetails;

    public RootStage() throws Exception {
        super();

        setElements();
        setLayout();
        setButtonActions();

        setScene(rootScene = new Scene(rootPane));
        setOnCloseRequest(closeRequest -> {
            System.exit(0);
        });
        getIcons().add(new Image(new FileInputStream("media/app.png")));
        setResizable(true);
        setTitle("ImageFX");
    }


    private void setElements() {
        imageFileChooser = new FileChooser();
        imageFileChooser.setTitle("Choose image file");
        imageFileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif", "*.ico"));

        tabs = new TabPane();
        openTab = new Tab("Home");
        toolTab = new ToolTab();
        infoTab = new InfoTab();
        pluginTab = new Tab("Plugins");

        openTabPaneTop = new HBox();
        openTabPaneBottom = new HBox();
        openTabPaneRoot = new VBox(openTabPaneTop, openTabPaneBottom);
        pluginTabPaneRoot = new HBox();
        openTab.setContent(openTabPaneRoot);
        pluginTab.setContent(pluginTabPaneRoot);



        imageFileChooserButton = new Button("Choose file:");
        createGaussianDistributedImage = new Button("Gaussian Distributed Image");
        testMyShitButton = new Button("Testing, testing, testing");
        testMyShitInput = new TextField();

        openTabPaneTop.getChildren().addAll(imageFileChooserButton, createGaussianDistributedImage);
        openTabPaneBottom.getChildren().addAll(testMyShitButton, testMyShitInput);

        tabs.getTabs().addAll(openTab, toolTab, infoTab, pluginTab);

        statusMessage = new Text();
        statusPane = new VBox(statusMessage);

        rootPane = new VBox(tabs, statusPane);
    }

    private void setLayout() {
        tabs.getStylesheets().add("/main/RootStyles.css");
        tabs.setTabMinWidth(100);

        openTabPaneRoot.setAlignment(Pos.CENTER);
        openTabPaneRoot.setPadding(new Insets(20));
        openTabPaneRoot.setSpacing(5);
        pluginTabPaneRoot.setAlignment(Pos.CENTER);
        pluginTabPaneRoot.setPadding(new Insets(20));
        pluginTabPaneRoot.setSpacing(5);

        openTabPaneTop.setSpacing(5);
        openTabPaneTop.setAlignment(Pos.CENTER);
        openTabPaneTop.setSpacing(5);
        openTabPaneBottom.setAlignment(Pos.CENTER);

        statusPane.setPadding(new Insets(10));

    }

    private void setButtonActions() {

        createGaussianDistributedImage.setOnAction(gaussians -> {
            imageProcessor = new ImageProcessor(ImageUtils.createGaussianDistributedImage(25), stageColorIterator);
            toolTab.newImageTab(imageProcessor);
            infoTab.setImageStatusPropertyListener(imageProcessor.imageDetails.getimageStateReadOnlyProperty());
        });

        imageFileChooserButton.setOnAction(choose -> {
            showFileChooser();
        });

        testMyShitButton.setOnAction(test -> {

            imageProcessor.weightedMedianFiltering();

        });

    }

    /**
     * Starts the file chooser and, if an image is opened, creates an ImageProcessor
     */
    private void showFileChooser() { // TODO- Links to image and/or the observableProperty, etc.
        selectedImageFile = imageFileChooser.showOpenDialog(new Stage());
        if (selectedImageFile != null) {
            try {
                image = new Image(new FileInputStream(selectedImageFile.getAbsolutePath()));
                statusMessage.setText("Image opened...");
            } catch (FileNotFoundException e) {
                statusMessage.setText("An error occured while reading the selected file; may not exist.");
            }
        }

        if (image != null) {

            if (imageProcessor != null)
                imageProcessor.nullAndClose();

            imageProcessor = new ImageProcessor(image, stageColorIterator);
            imageDetails = imageProcessor.imageDetails;
            toolTab.newImageTab(imageProcessor);
            infoTab.setImageStatusPropertyListener(imageProcessor.imageDetails.getimageStateReadOnlyProperty());
        }
    }


    class PromptStage extends Stage {

        private TextArea status;

        /**
         * @param nodes message or input nodes; action decided by the caller.
         */
        public PromptStage(String promptMessage, Node... nodes) {
            Text message = new Text(promptMessage);

            status = new TextArea();
            status.setEditable(false);
            status.setPrefRowCount(2);
            status.setPrefColumnCount(20);
            status.setWrapText(true);

			/* Add static and received nodes to the stage */
            VBox inputContainer = new VBox(message);
            for (Node node : nodes)
                inputContainer.getChildren().add(node);
            inputContainer.getChildren().add(status);

            inputContainer.setPadding(new Insets(10));
            inputContainer.setSpacing(10);
            inputContainer.setAlignment(Pos.CENTER);

            setScene(new Scene(inputContainer));

            show();
        }

        public void setStatusMessage(String message) {
            status.setText(message);
        }
    }

    private class InfoTab extends Tab implements ImageStatusObserver {
        private ImageProcessor.ImageDetails imageDetails;
        VBox infoTabRootPane;
        Text infoTabWidthText;
        Text infoTabHeightText;
        Text infoTabStatusText;

        private InfoTab() {
            super("Info");

            setElements();
            setLayout();

            setContent(infoTabRootPane);
        }

        private void setElements() {
            infoTabRootPane = new VBox();
            infoTabRootPane.getStylesheets().add("/main/RootStyles.css");

            infoTabStatusText = new Text("Status: AVAILABLE" );
            infoTabWidthText = new Text("Width:  ");
            infoTabHeightText = new Text("Height:  ");

            infoTabRootPane.getChildren().addAll(
                    infoTabStatusText,
                    infoTabWidthText,
                    infoTabHeightText
            );
        }

        private void setLayout() {
            infoTabRootPane.setSpacing(10);
            infoTabRootPane.setPadding(new Insets(20));
            infoTabStatusText.getStyleClass().add("infoText");
            infoTabWidthText.getStyleClass().add("infoText");
            infoTabHeightText.getStyleClass().add("infoText");
        }

        @Override
        public void setImageStatusPropertyListener(ReadOnlyObjectProperty<ImageProcessor.ImageStatus> observedStatusProperty) {
            observedStatusProperty.addListener((observable, oldValue, newValue) -> {
                infoTabStatusText.setText("Status: " + newValue.name());
                infoTabWidthText.setText("Width:  " + imageDetails.imageWidth);
                infoTabHeightText.setText("Height: " + imageDetails.imageHeight);
            });
        }
    }

    // TODO - ToolTab should just be a method, which then defines the ImageToolTabPane like now.
    private class ToolTab extends Tab {
        private ArrayList<Tab> imageTabList;
        private TabPane imageTabPane;
        private HBox toolTabRoot;

        private ToolTab() {
            super("Tools");

            imageTabList = new ArrayList<>();
            imageTabPane = new TabPane();
            toolTabRoot = new HBox(imageTabPane);

            setContent(toolTabRoot);
        }

        private void newImageTab(ImageProcessor ip) {
            Tab imageTab = new Tab("Image " + (imageTabList.size() + 1));
            imageTab.setContent(new ImageToolTabPane());

            imageTabList.add(imageTab);
            imageTabPane.getTabs().add(imageTab);

            sizeToScene();
        }

        private class ImageToolTabPane extends TabPane {

            private Tab generalTab, filterTab, pixelTab, miscTab;

            private ImageToolTabPane() {
                generalTabContent();
                filterTabContent();
                pixelTabContent();
                miscTabContent();

                for (Tab t : this.getTabs()) {
                    t.getContent().getStyleClass().add("toolTabRootPane");
                    ((HBox)t.getContent()).setAlignment(Pos.TOP_CENTER);
                    ((HBox)t.getContent()).setSpacing(5);
                }


            }

            private void generalTabContent() {
                Button splitButton = new Button("Split image");
                Button histogramButton = new Button("Histogram");

                HBox generalTabRoot = new HBox();
                generalTabRoot.getChildren().addAll(splitButton, histogramButton);
                generalTabRoot.setMinHeight(100);

                generalTab = new Tab("General", generalTabRoot);
                getTabs().add(generalTab);

                splitButton.setOnAction(split -> {
                    imageProcessor.displayImageTiles(imageProcessor.splitImage());
                });
                histogramButton.setOnAction(histogram -> {
                    imageProcessor.showImageHistogram();
                });
            }

            private void filterTabContent() {
                Button smoothImageButton = new Button("Smooth");
                Button weightedMedianButton = new Button("Weighted median");

                HBox filterTabRoot = new HBox();
                filterTabRoot.getChildren().addAll(smoothImageButton, weightedMedianButton);

                filterTab = new Tab("Filters", filterTabRoot);
                getTabs().add(filterTab);

                smoothImageButton.setOnAction(smooth -> {
                    Text filterWidthText = new Text("Filter width (3-5): ");
                    TextField filterWidthInput = new TextField();
                    Text filterHeightText = new Text("Filter height (3-5): ");
                    TextField filterHeightInput = new TextField();
                    Button commitButton = new Button("Smooth...");

                    PromptStage prompt = new PromptStage("This filter smooths the image",
                            filterWidthText, filterWidthInput, filterHeightText, filterHeightInput, commitButton);

                    commitButton.setOnAction(commit -> {
                        try {
//					if (filterWidthInput.getText().isEmpty() || filterHeightInput.getText().isEmpty())
//						prompt.setStatusMessage("Both fields must be specified.");

//					int width = Integer.parseInt(filterWidthInput.getText().trim());
//					int height = Integer.parseInt(filterHeightInput.getText().trim());

                            imageProcessor.smoothImage(3, 3);
                        } catch (NumberFormatException e) {
                            prompt.setStatusMessage("Wrong number format, integers only.");
                        }
                    });

                });
                weightedMedianButton.setOnAction(median -> {
                    // TODO
                });
            }

            private void pixelTabContent() {
                Button brightnessContrastButton = new Button("Brightness & Contrast");
                Button normaliseImageButton = new Button("Normalise image");

                HBox pixelTabRoot = new HBox();
                pixelTabRoot.getChildren().addAll(brightnessContrastButton, normaliseImageButton);

                pixelTab = new Tab("Pixels", pixelTabRoot);
                getTabs().add(pixelTab);

                normaliseImageButton.setOnAction(normalise -> {
                    TextField expectedValueInput = new TextField();
                    TextField standardDeviationInput = new TextField();
                    Button commitButton = new Button("Do.");
                    String message = "Give the expected value ( μ∈(0,255) ) and standard deviation ( σ∈(0,255) ).\nBlank fields will result in N(μ=127, σ=50)";
                    PromptStage prompt = new PromptStage(message, expectedValueInput, standardDeviationInput, commitButton);

                    commitButton.setOnAction(action -> {
                        try {
                            if (expectedValueInput.getText().isEmpty() && standardDeviationInput.getText().isEmpty()) {
                                imageProcessor.imageNormalisation(127, 50);
                                prompt.close();
                            }
                            int eV = Integer.parseInt(expectedValueInput.getText().trim());
                            int stdDev = Integer.parseInt(standardDeviationInput.getText().trim());

                            if ((eV > 0 && eV < 256) && (stdDev >= 0 && stdDev <= 255)) {
                                imageProcessor.imageNormalisation(eV, stdDev);
                                prompt.close();
                            } else {
                                prompt.setStatusMessage("Numbers must both be in the range 0-255.");
                            }
                        } catch (NumberFormatException e) {
                            prompt.setStatusMessage("Wrong number format, integers only.");
                        }
                    });
                });
                brightnessContrastButton.setOnAction(adjust -> {
                    imageProcessor.showBrightnessContrastStage();
                });
            }

            private void miscTabContent() {
                Button flipXButton = new Button("Flip X");
                Button flipYButton = new Button("Flip Y");
                Button flipXYButton = new Button("Flip XY");
                Button frameImageButton = new Button("Frame image");
                Button moveImageButton = new Button("Cyclic move");

                HBox miscTabRoot = new HBox();
                miscTabRoot.getChildren().addAll(flipXButton, flipYButton, flipXYButton, frameImageButton, moveImageButton);

                miscTab = new Tab("Misc", miscTabRoot);
                getTabs().add(miscTab);

                flipXButton.setOnAction(flipX -> {
                    imageProcessor.flipImageOverX();
                });
                flipYButton.setOnAction(flipY -> {
                    imageProcessor.flipImageOverY();
                });
                flipXYButton.setOnAction(flipXY -> {
                    imageProcessor.flipImageOverXY();
                });
                frameImageButton.setOnAction(frame -> {
                    imageProcessor.frameImageWithBorder();
                });
                moveImageButton.setOnAction(createPrompt -> {

                    TextField input = new TextField();
                    PromptStage prompt = new PromptStage("How many percentages to move the picture?", input);

                    input.setOnAction(value -> {
                        try {
                            int percentage = Integer.parseInt(input.getText().trim());
                            if (percentage < 0 | percentage > 100)
                                prompt.setStatusMessage("Illegal number. Must be between 0 and 100 only.");
                            else {
                                System.out.println("Go go gadget move!");
                                imageProcessor.moveImageHorizontally(percentage);
                                prompt.close();
                            }
                        } catch (NumberFormatException e) {
                            prompt.setStatusMessage("Illegal input. Integer between 0 and 100 only.");
                        }
                    });

                });
            }
        }
    }
}
