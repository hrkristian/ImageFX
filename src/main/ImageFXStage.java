package main;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Abstract class for all Stage instances of the ImageFX program.
 * Each stage which works towards an image should be identifiable, this is done by means of coloring.
 *
 * A predefined iteration of colors decide which color a stage will get, when an ImageProcessor spawns a new IP,
 * it will know which color to pass to the new instance by looking it up in the table.
 * This limits the amount of colors available, however a de-facto unlimited amount of unique colors can be
 * acheived by letting the iteration loop unto itself but with new values. This probably shouldn't be done, however,
 * because it'll leave stages open to being colored too similarly.
 * Instead, a high enough amount of unique linearly chosen colors will be used. Even just using the major colors
 * should offer enough unique colors.
 */
abstract class ImageFXStage extends Stage {

	protected Color stageColor;
	protected Scene rootScene;
	protected VBox rootPane;

	/**
	 * Creates the stage with whichever size is calculated
	 * @param stageColor The color ID of the image this stage is connected to
	 */
	ImageFXStage(Color stageColor) {
		super();
		this.stageColor = stageColor;
		setScene( rootScene = new Scene(rootPane = new VBox()) );
		rootScene.getStylesheets().add("/main/RootStyles.css");
//		rootPane.setBackground(new Background(new BackgroundFill(stageColor, null, null)));
	}

	/**
	 * Creates a stage with a given size
	 * @param stageColor The color ID of the image this stage is connected to
	 * @param width the width of the stage
	 * @param height the height of the stage
	 */
	ImageFXStage(Color stageColor, int width, int height) {
		super();
		this.stageColor = stageColor;
		setScene( rootScene = new Scene(rootPane = new VBox(), width, height) );
		rootScene.getStylesheets().add("/main/RootStyles.css");
	}

	/**
	 * PromptStage is a simple
	 */
	class PromptStage extends Stage {

		private TextArea status;

		/**
		 * @param nodes message or input nodes; action decided by the caller.
		 */
		PromptStage(String promptMessage, boolean showStatusMessageField, Node... nodes) {
			Text message = new Text(promptMessage);
			/* Add static and received nodes to the stage */
			VBox inputContainer = new VBox(message);
			for (Node node : nodes)
				inputContainer.getChildren().add(node);

			if (showStatusMessageField) {
				status = new TextArea();
				status.setEditable(false);
				status.setPrefRowCount(2);
				status.setPrefColumnCount(20);
				status.setWrapText(true);
				inputContainer.getChildren().add(status);
			}

			inputContainer.setPadding(new Insets(10));
			inputContainer.setSpacing(10);
			inputContainer.setAlignment(Pos.CENTER);

			setScene(new Scene(inputContainer));
			inputContainer.setOnKeyReleased(input -> {
				System.out.println( input.getText() );
			});
			show();
		}

		void setStatusMessage(String message) {
			status.setText(message);
		}
	}
}
