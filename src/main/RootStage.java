package main;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class RootStage extends Stage {

	private Scene rootScene;
	private VBox rootPane;
	private TabPane tabs;
	private Tab openTab, toolTab, infoTab, pluginTab;
	private HBox openTabPane, toolTabPane, infoTabPane, pluginTabPane;

	private Button imageFileChooserButton;
	private Button createGaussianDistributedImage;

	private Button[] imageToolButtons;
    private Button flipImageOverXButton;
    private Button flipImageOverYButton;
    private Button flipImageOverXYButton;
    private Button frameImageWithBorderButton;
    private Button moveImageHorizontallyButton;
    private Button animateImageHorizontallyButton;
    private Button splitImageIntoTilesButton;
    private Button showImageHistogramButton;
    private Button adjustBrightnessButton;


	private Text statusMessage;
	private VBox statusPane;

	FileChooser imageFileChooser;
	File selectedImageFile = null;

	Image image = null;
	ImageProcessor imageProcessor;
	ImageProcessor.ImageDetails imageDetails;

	PluginInterface pluginInterface = new PluginInterface(imageProcessor);


	public RootStage() throws Exception {
		super();

		setContent();

		setLayout();

		setButtonActions();

		setScene( rootScene = new Scene(rootPane) );
		setOnCloseRequest(closeRequest -> { System.exit(0); });
		getIcons().add(new Image(new FileInputStream("media/app.png")));
		setResizable(false);
		setTitle("ImageFX");
	}



	private void setContent() {
		imageFileChooser = new FileChooser();
		imageFileChooser.setTitle("Choose image file");
		imageFileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif", "*.ico"));

		tabs = new TabPane();
		openTab = new Tab("Open");
		toolTab = new Tab("Tools");
		infoTab = new Tab("Info");
		pluginTab = new Tab("Plugin");

		openTabPane = new HBox();
		toolTabPane = new HBox();
		infoTabPane = new HBox();
		pluginTabPane = new HBox();

		openTab.setContent(openTabPane);
		toolTab.setContent(toolTabPane);
		infoTab.setContent(infoTabPane);
		pluginTab.setContent(pluginTabPane);

		imageFileChooserButton = new Button("Choose file:");
		createGaussianDistributedImage = new Button("Gaussian Distributed Image");

		Button[] tmpImageToolButtons = {
				flipImageOverXButton = new Button("Flip X"),
				flipImageOverYButton = new Button("Flip Y"),
				flipImageOverXYButton = new Button("Flip XY"),
				frameImageWithBorderButton = new Button("Frame Image"),
				moveImageHorizontallyButton = new Button("Move ->"),
				splitImageIntoTilesButton = new Button("Split"),
				adjustBrightnessButton = new Button("Adjust Brightness"),
				showImageHistogramButton = new Button("Histogram")
		};
		imageToolButtons = tmpImageToolButtons;

		for (Button btn : imageToolButtons)
			btn.setDisable(true);

		openTabPane.getChildren().addAll(
				imageFileChooserButton,
				createGaussianDistributedImage
		);

		for (Button btn : imageToolButtons)
			toolTabPane.getChildren().add(btn);

		infoTabPane.getChildren().addAll(

		);
		pluginTabPane.getChildren().addAll(

		);

		tabs.getTabs().addAll(openTab, toolTab, infoTab, pluginTab);

		statusMessage = new Text();
		statusPane = new VBox(statusMessage);

		rootPane = new VBox(tabs, statusPane);

	}

	private void setLayout() {
		tabs.getStylesheets().add("/main/RootStyles.css");
		tabs.setTabMinWidth(100);

		openTabPane.setAlignment(Pos.CENTER);
		openTabPane.setPadding(new Insets(20));
		openTabPane.setSpacing(5);
		toolTabPane.setAlignment(Pos.CENTER);
		toolTabPane.setPadding(new Insets(20));
		toolTabPane.setSpacing(5);
		infoTabPane.setAlignment(Pos.CENTER);
		infoTabPane.setPadding(new Insets(20));
		infoTabPane.setSpacing(5);
		pluginTabPane.setAlignment(Pos.CENTER);
		pluginTabPane.setPadding(new Insets(20));
		pluginTabPane.setSpacing(5);
	}

	private void setButtonActions() {

		createGaussianDistributedImage.setOnAction(gaussians -> {
			imageProcessor = new ImageProcessor(ImageUtils.createGaussianDistributedImage(500));
			for (Button btn : imageToolButtons)
				btn.setDisable(false);
		});

		imageFileChooserButton.setOnAction(choose -> { showFileChooser(); });

		flipImageOverXButton.setOnAction(flipX -> { imageProcessor.flipImageOverX(); });
		flipImageOverYButton.setOnAction(flipY -> { imageProcessor.flipImageOverY(); });
		flipImageOverXYButton.setOnAction(flipXY -> { imageProcessor.flipImageOverXY(); });
		frameImageWithBorderButton.setOnAction(frame -> { imageProcessor.frameImageWithBorder(); });
		moveImageHorizontallyButton.setOnAction(createPrompt -> {
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
		splitImageIntoTilesButton.setOnAction(split -> { imageProcessor.displayImageTiles(imageProcessor.splitImage()); });
		adjustBrightnessButton.setOnAction(adjust -> {
			TextField input = new TextField();
			PromptStage prompt = new PromptStage("Amount to adjust by? \n Values above 255 will have no additional effect.", input  );
			input.setOnAction(value -> {
				try {
					int amount = Integer.parseInt(input.getText().trim());
					imageProcessor.adjustBrightness(amount);
				} catch (NumberFormatException e) {
					prompt.setStatusMessage("Illegal input. Must be an integer.");
				}
			});
		});
		showImageHistogramButton.setOnAction(histogram -> { imageProcessor.showImageHistogram(); });
	}

	/**
	 * Starts the file chooser and, if an image is opened, creates an ImageProcessor
	 */
	private void showFileChooser() { // TODO- Links to image and/or the observableProperty, etc.
		selectedImageFile = imageFileChooser.showOpenDialog(new Stage());
		if (selectedImageFile != null) {
			try {
				image = new Image(new FileInputStream(selectedImageFile.getAbsolutePath()));
				for (Button btn : imageToolButtons)
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
			imageDetails = imageProcessor.imageDetails;

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
}
