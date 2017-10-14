package main;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;

public class RootStage extends ImageFXStage {

    private static ArrayList<Color> stageColors;
    private static Iterator<Color> stageColorIterator;
    static {
        stageColors = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            stageColors.add( Color.rgb(
                    (int)(Math.random()*255),
                    (int)(Math.random()*255),
                    (int)(Math.random()*255)
            ) );
        }
        // TODO- Ensure a higher difference between the colors.
        stageColorIterator = stageColors.iterator();
    }

    private TabPane tabs;
    private Tab openTab, pluginTab;
    private ToolTab toolTab;
    private InfoTab infoTab;
    private HBox pluginTabPaneRoot;
    private VBox openTabPaneRoot;
    private HBox openTabPane;

    private ScrollPane imageLegendPane;
    private VBox legendContainer;

    private Button imageFileChooserButton;
    private Button createGaussianDistributedImage;

    private Text statusMessage;
    private VBox statusPane;

    private FileChooser imageFileChooser;

    private ArrayList<ImageProcessor> imageProcessors;

    public RootStage() throws Exception {
        super(null, 700, 200);

        imageProcessors = new ArrayList<>();

        addElements();
        addLayout();
        addActionHandlers();

        setOnCloseRequest(closeRequest -> {
            System.exit(0);
        });
        getIcons().add(new Image(new FileInputStream("media/app.png")));
        setResizable(false);
        setTitle("ImageFX");
    }


    private void addElements() {
        imageFileChooser = new FileChooser();
        imageFileChooser.setTitle("Choose image file");
        imageFileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif", "*.ico"));

        tabs = new TabPane();
        openTab = new Tab("Home");
        toolTab = new ToolTab();
        infoTab = new InfoTab();
        pluginTab = new Tab("Plugins");

        openTabPane = new HBox();
        imageLegendPane = new ScrollPane();
        legendContainer = new VBox();
        openTabPaneRoot = new VBox(openTabPane, imageLegendPane);
        pluginTabPaneRoot = new HBox();
        openTab.setContent(openTabPaneRoot);
        pluginTab.setContent(pluginTabPaneRoot);

        imageFileChooserButton = new Button("Choose file:");
        createGaussianDistributedImage = new Button("Gaussian Distributed Image");

        openTabPane.getChildren().addAll(imageFileChooserButton, createGaussianDistributedImage/*, testMyShitButton, testMyShitInput*/);
        imageLegendPane.setContent(legendContainer);

        tabs.getTabs().addAll(openTab, toolTab, infoTab, pluginTab);

        statusMessage = new Text();
        statusPane = new VBox(statusMessage);

        rootPane.getChildren().addAll(tabs, statusPane);
    }
    private void addLayout() {
        tabs.setTabMinWidth(100);

        openTabPaneRoot.setAlignment(Pos.CENTER);
        openTabPaneRoot.setPadding(new Insets(20));
        openTabPaneRoot.setSpacing(5);
        pluginTabPaneRoot.setAlignment(Pos.CENTER);
        pluginTabPaneRoot.setPadding(new Insets(20));
        pluginTabPaneRoot.setSpacing(5);

        openTabPane.setSpacing(5);
        openTabPane.setAlignment(Pos.CENTER);
        openTabPane.setSpacing(5);

        statusPane.setPadding(new Insets(10));

        imageLegendPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        imageLegendPane.prefViewportHeightProperty().set(50);
        legendContainer.getChildren().addListener(new ListChangeListener<Node>() {
            @Override
            public void onChanged(Change<? extends Node> c) {
                if (c.getList().size() > 1)
                    imageLegendPane.prefViewportHeightProperty().set(100);
                else
                    imageLegendPane.prefViewportHeightProperty().set(50);

                imageLegendPane.requestLayout();
                imageLegendPane.getParent().requestLayout();
            }
        });
    }
    private void addActionHandlers() {
        createGaussianDistributedImage.setOnAction(gaussians -> {
            newImage(ImageUtils.createGaussianDistributedImage(25));
        });
        imageFileChooserButton.setOnAction(choose -> { showFileChooser(); });
    }

    /**
     * Starts the file chooser and, if an image is opened, creates an ImageProcessor
     */
    private void showFileChooser() { // TODO- Links to image and/or the observableProperty, etc.
        Image image = null;
        File selectedImageFile = imageFileChooser.showOpenDialog(new Stage());
        if (selectedImageFile != null) {
            try {
                image = new Image(new FileInputStream(selectedImageFile.getAbsolutePath()));
                statusMessage.setText("Image opened...");
            } catch (FileNotFoundException e) {
                statusMessage.setText("An error occured while reading the selected file; may not exist.");
            }
        }

        if (image != null)
            newImage(image);
    }

    private void newImage(Image image) {
        ImageProcessor ip = new ImageProcessor(image, stageColorIterator);
        imageProcessors.add(ip);
        toolTab.newImageTab(ip);
        infoTab.newImageInfoTab(ip);
        legendContainer.getChildren().add(new Legend(ip));
    }

    private class Legend extends HBox {
        private Text imageName;
        private ScrollPane legend;
        private HBox legendEntryContainer;
        private Button applyButton, resetButton, removeButton;

        private ImageProcessor ip;

        private Legend(ImageProcessor ip) {
            super();
            this.ip = ip;

            imageName = new Text("Placeholder");
            legendEntryContainer = new HBox();
            legend = new ScrollPane(legendEntryContainer);
            applyButton = new Button("Apply");
            resetButton = new Button("Reset");
            removeButton = new Button("X");

            for (double i = 0, end = 6; i < end; i++) {
                Rectangle rect = new Rectangle(i*50,0,40,40);
                rect.setFill(Color.gray(i / end));
                legendEntryContainer.getChildren().add(rect);
            }

            this.getChildren().addAll(
                    imageName,
                    legend,
                    applyButton,
                    resetButton,
                    removeButton
            );
            addLayout();
            setActions();
        }
        private void addLayout() {
            this.setSpacing(10);
            this.setAlignment(Pos.CENTER);
            this.setPadding(new Insets(10));
            this.setBackground(new Background(new BackgroundFill(ip.stageColor, null, null)));

//            legend.getStyleClass().forEach( System.out::println );

            legend.setVmax(0);
            legend.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            legend.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

            legendEntryContainer.setSpacing(5);
            legendEntryContainer.setPadding(new Insets(5));
            legendEntryContainer.setAlignment(Pos.CENTER_LEFT);

            legend.getStyleClass().add("root-scroll-legend");
            legendEntryContainer.getStyleClass().add("root-scroll-legend-container");
        }
        private void setActions() {
            applyButton.setOnAction(apply->{});
            resetButton.setOnAction(reset->{});
            removeButton.setOnAction(remove->{
                String message = "Abandon this image?";
                Button abandonImageButton = new Button("Yes");
                Button regretDecisionButton = new Button("No");
                HBox buttonContainer = new HBox(abandonImageButton, regretDecisionButton);
                buttonContainer.setSpacing(20);
                buttonContainer.setAlignment(Pos.CENTER);
                PromptStage prompt = new PromptStage(message, false, buttonContainer);
                regretDecisionButton.setOnAction(no -> { prompt.close(); });
                abandonImageButton.setOnAction(yes -> {
                    ip.nullAndClose();
                    prompt.close();
                    ((Pane)getParent()).getChildren().remove(this);
                    requestLayout();
                });
            });

            legend.setOnScroll( event -> { // TODO - Make this work.
                legend.setHvalue(legend.getHvalue() - event.getDeltaY()*(legendEntryContainer.getWidth()*0.000005) );
                event.consume();
            });
        }
    }

    private class InfoTab extends Tab {
        ScrollPane infoTabRootPane;
        VBox infoPaneContent;

        private InfoTab() {
            super("Info");

            infoPaneContent = new VBox();
            infoPaneContent.getStyleClass().add("infoPaneContent");

            infoTabRootPane = new ScrollPane();
            infoTabRootPane.setContent(infoPaneContent);

            setContent(infoTabRootPane);
        }

        private void newImageInfoTab(ImageProcessor ip) {
            ImageInfoPane info = new ImageInfoPane(ip.imageDetails, ip.stageColor);
            infoPaneContent.getChildren().add(info);
        }
        private void removeImageInfoTab(ImageInfoPane node) {
            infoPaneContent.getChildren().remove(node);
        }

        private class ImageInfoPane extends VBox implements ImageStatusObserver {
            ImageProcessor.ImageDetails imageDetails;

            Text infoTabNameText;
            Text infoTabStatusText;
            Text infoTabWidthText;
            Text infoTabHeightText;

            private ImageInfoPane(ImageProcessor.ImageDetails imageDetails, Color stageColor) {
                this.imageDetails = imageDetails;
                setImageStatusPropertyListener(imageDetails.getimageStateReadOnlyProperty());

                infoTabNameText = new Text("Image "+imageProcessors.size() + 1);
                infoTabStatusText = new Text("Status: AVAILABLE" );
                infoTabWidthText = new Text("Width:  "+imageDetails.imageWidth);
                infoTabHeightText = new Text("Height:  "+imageDetails.imageHeight);

                HBox infoTabTextContainer = new HBox(infoTabStatusText, infoTabWidthText, infoTabHeightText);
                infoTabTextContainer.setSpacing(20);
                infoTabTextContainer.getStyleClass().add("infoTabTextContainer");

                if (stageColor.getRed()+stageColor.getGreen()+stageColor.getBlue() < 1.2)
                    infoTabTextContainer.getStyleClass().add("darkNode");

                this.getChildren().add(infoTabTextContainer);
                this.setPadding(new Insets(10));
                this.setBackground(new Background(new BackgroundFill(stageColor, null, null)));
            }

            @Override
            public void setImageStatusPropertyListener(ReadOnlyObjectProperty<ImageProcessor.ImageStatus> observedStatusProperty) {
                observedStatusProperty.addListener((observable, oldValue, newValue) -> {
                    infoTabStatusText.setText("Status: " + newValue.name());
                    switch (newValue) {
                        case PROCESSING:
                            System.out.println("InfoTab: Processing");
                            break;
                        case CLOSING:
                            System.out.println("InfoTab: Closing?");
                            removeImageInfoTab(this);
                            break;
                        case AVAILABLE:
                            System.out.println("InfoTab: Available");
                            infoTabWidthText.setText("Width:  " + imageDetails.imageWidth);
                            infoTabHeightText.setText("Height: " + imageDetails.imageHeight);
                            break;
                    }
                });
            }
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

            final int R = (int)( ip.stageColor.getRed() * 255 );
            final int G = (int)( ip.stageColor.getGreen() * 255 );
            final int B = (int)( ip.stageColor.getBlue() * 255 );
            String color = String.format( "#%02X%02X%02X", R, G, B );

            String borderWidth = "-fx-border-width: 3px 3px 0 3px; ";
            String borderRadius = "-fx-border-radius: 5% 5% 0% 0%;";
            String borderColor = "-fx-border-color: ".concat( color.concat( ";" ));

            String style = borderRadius.concat( borderWidth.concat( borderColor));

            /*
            if (R + G + B > 300) { // A bright color should have dark text, and vice versa
                imageTab.setGraphic(new Label( "Image " + (imageTabList.size() + 1) ));
                imageTab.getGraphic().setStyle("-fx-text-fill: #000000");
            } else {
                imageTab.setGraphic(new Label( "Image " + (imageTabList.size() + 1) ));
                imageTab.getGraphic().setStyle("-fx-text-fill: #ffffff");
            }
            */

            imageTab.setStyle(style);
            imageTab.getStyleClass().add("imageTab");
            imageTab.setContent(new ImageToolTabPane(ip));

            imageTabList.add(imageTab);
            imageTabPane.getTabs().add(imageTab);
        }

        private class ImageToolTabPane extends TabPane implements ImageStatusObserver {

            private ImageProcessor ip;
            private Tab generalTab, filterTab, pixelTab, miscTab;
            private ArrayList<Control> buttons;

            private ImageToolTabPane(ImageProcessor ip) {
                this.ip = ip;
                setImageStatusPropertyListener(ip.imageDetails.getimageStateReadOnlyProperty());

                buttons = new ArrayList<>();

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
                buttons.add(splitButton);
                buttons.add(histogramButton);

                HBox generalTabRoot = new HBox();
                generalTabRoot.getChildren().addAll(splitButton, histogramButton);
                generalTabRoot.setMinHeight(100);

                generalTab = new Tab("General", generalTabRoot);
                getTabs().add(generalTab);

                splitButton.setOnAction(split -> {
                    ip.displayImageTiles(ip.splitImage());
                });
                histogramButton.setOnAction(histogram -> {
                    ip.showImageHistogram();
                });
            }

            private void filterTabContent() {
                Button smoothImageButton = new Button("Smooth");
                Button weightedMedianButton = new Button("Weighted median filter");
                Button pseudoMedianButton = new Button("Pseudo median filter");
                buttons.add(smoothImageButton);
                buttons.add(weightedMedianButton);
                buttons.add(pseudoMedianButton);

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
                    buttons.add(commitButton);

                    PromptStage prompt = new PromptStage("This filter smooths the image", true,
                            filterWidthText, filterWidthInput, filterHeightText, filterHeightInput, commitButton);

                    commitButton.setOnAction(commit -> {
                        try {
//					if (filterWidthInput.getText().isEmpty() || filterHeightInput.getText().isEmpty())
//						prompt.setStatusMessage("Both fields must be specified.");

//					int width = Integer.parseInt(filterWidthInput.getText().trim());
//					int height = Integer.parseInt(filterHeightInput.getText().trim());

                            ip.smoothImage(3, 3);
                            buttons.remove(commitButton);
                        } catch (NumberFormatException e) {
                            prompt.setStatusMessage("Wrong number format, integers only.");
                        }
                    });

                });
                weightedMedianButton.setOnAction(median -> {
                    HBox inputPane = new HBox();

                    TextField filterSizeInput = new TextField("3");
                    filterSizeInput.setEditable(false);
                    filterSizeInput.setPrefColumnCount(5);
                    filterSizeInput.setAlignment(Pos.CENTER);
                    Button plusButton = new Button("+");
                    Button minusButton = new Button("-");
                    inputPane.getChildren().addAll(minusButton, filterSizeInput, plusButton);
                    inputPane.setSpacing(5);
                    inputPane.setAlignment(Pos.CENTER);

                    Button actionButton = new Button("Go");
                    buttons.add(actionButton);

                    PromptStage prompt = new PromptStage("Give filter size", true, inputPane, actionButton);

                    plusButton.setOnAction(plus -> {
                        try {
                            int current = Integer.parseInt(filterSizeInput.getText().trim());
                            current += 2;
                            filterSizeInput.setText( ""+current );
                        } catch (NumberFormatException e) {} // Just cannot realistically happen.
                    });
                    minusButton.setOnAction(minus -> {
                        try {
                            int current = Integer.parseInt(filterSizeInput.getText().trim());
                            current = (current < 5) ? 3 : current - 2;

                            filterSizeInput.setText( ""+current );
                        } catch (NumberFormatException e) {} // Just cannot realistically happen.
                    });
                    actionButton.setOnAction(go -> {
                        try {
                            int filterSize = Integer.parseInt(filterSizeInput.getText().trim());
                            ip.weightedMedianFiltering(filterSize);
                            buttons.remove(actionButton);
                        } catch (NumberFormatException e) {} // Just cannot realistically happen.
                    });

                    prompt.show();
                    actionButton.requestFocus();
                });
                pseudoMedianButton.setOnAction(pseudoMedian -> {});
            }

            private void pixelTabContent() {
                Button brightnessContrastButton = new Button("Brightness & Contrast");
                Button normaliseImageButton = new Button("Normalise image");
                buttons.add(brightnessContrastButton);
                buttons.add(normaliseImageButton);

                HBox pixelTabRoot = new HBox();
                pixelTabRoot.getChildren().addAll(brightnessContrastButton, normaliseImageButton);

                pixelTab = new Tab("Pixels", pixelTabRoot);
                getTabs().add(pixelTab);

                normaliseImageButton.setOnAction(normalise -> {
                    TextField expectedValueInput = new TextField();
                    TextField standardDeviationInput = new TextField();
                    Button commitButton = new Button("Do.");
                    buttons.add(commitButton);

                    String message = "Give the expected value ( μ∈(0,255) ) and standard deviation ( σ∈(0,255) ).\nBlank fields will result in N(μ=127, σ=50)";
                    PromptStage prompt = new PromptStage(message, true, expectedValueInput, standardDeviationInput, commitButton);

                    commitButton.setOnAction(action -> {
                        try {
                            if (expectedValueInput.getText().isEmpty() && standardDeviationInput.getText().isEmpty()) {
                                ip.imageNormalisation(127, 50);
                                prompt.close();
                            }
                            int eV = Integer.parseInt(expectedValueInput.getText().trim());
                            int stdDev = Integer.parseInt(standardDeviationInput.getText().trim());

                            if ((eV > 0 && eV < 256) && (stdDev >= 0 && stdDev <= 255)) {
                                ip.imageNormalisation(eV, stdDev);
                                buttons.remove(commitButton);
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
                    ip.showBrightnessContrastStage();
                });
            }

            private void miscTabContent() {
                Button flipXButton = new Button("Flip X");
                Button flipYButton = new Button("Flip Y");
                Button flipXYButton = new Button("Flip XY");
                Button frameImageButton = new Button("Frame image");
                Button moveImageButton = new Button("Cyclic move");
                buttons.add(flipXButton);
                buttons.add(flipYButton);
                buttons.add(flipXYButton);
                buttons.add(frameImageButton);
                buttons.add(moveImageButton);

                HBox miscTabRoot = new HBox();
                miscTabRoot.getChildren().addAll(flipXButton, flipYButton, flipXYButton, frameImageButton, moveImageButton);

                miscTab = new Tab("Misc", miscTabRoot);
                getTabs().add(miscTab);

                flipXButton.setOnAction(flipX -> {
                    ip.flipImageOverX();
                });
                flipYButton.setOnAction(flipY -> {
                    ip.flipImageOverY();
                });
                flipXYButton.setOnAction(flipXY -> {
                    ip.flipImageOverXY();
                });
                frameImageButton.setOnAction(frame -> {
                    ip.frameImageWithBorder();
                });
                moveImageButton.setOnAction(createPrompt -> {

                    TextField input = new TextField();
                    buttons.add(input);
                    PromptStage prompt = new PromptStage("How many percentages to move the picture?", true, input);

                    input.setOnAction(value -> {
                        try {
                            int percentage = Integer.parseInt(input.getText().trim());
                            if (percentage < 0 | percentage > 100)
                                prompt.setStatusMessage("Illegal number. Must be between 0 and 100 only.");
                            else {
                                System.out.println("Go go gadget move!");
                                ip.moveImageHorizontally(percentage);
                                buttons.remove(input);
                                prompt.close();
                            }
                        } catch (NumberFormatException e) {
                            prompt.setStatusMessage("Illegal input. Integer between 0 and 100 only.");
                        }
                    });

                });
            }

            private void disableButtons() {
                for (Control ctrl : buttons)
                    ctrl.setDisable(true);
            }
            private void enableButtons() {
                for (Control ctrl : buttons)
                    ctrl.setDisable(false);
            }

            @Override
            public void setImageStatusPropertyListener(ReadOnlyObjectProperty<ImageProcessor.ImageStatus> observedStatusProperty) {
                observedStatusProperty.addListener((observable, oldValue, newValue) -> {
                    switch (newValue) {
                        case INITIALISING:
                            disableButtons();
                            break;
                        case PROCESSING:
                            disableButtons();
                            break;
                        case AVAILABLE:
                            enableButtons();
                            break;
                        case CLOSING:
                            imageTabList.remove(getParent());
                            imageTabPane.getTabs().remove(getParent());
                            break;
                    }
                });
            }
        }
    }
}
