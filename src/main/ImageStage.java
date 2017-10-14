package main;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class ImageStage extends ImageFXStage implements ImageObserver {

	private ImageView imagePlane;

	public ImageStage(Image image, int size, Color stageColor) {
		super(stageColor, size, (int)(size*(image.getHeight() / image.getWidth()))+10);

		double ratio = image.getHeight() / image.getWidth();

		imagePlane = new ImageView(image);
		imagePlane.setPreserveRatio(true);
		imagePlane.setSmooth(false);

		HBox imagePane = new HBox(imagePlane);
		imagePane.setBorder(new Border(new BorderStroke(
				stageColor, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(5)
		)));

		rootPane.getChildren().addAll(imagePane);
		rootPane.setAlignment(Pos.CENTER);

		setMinHeight(600);

		// Resize logic
		imagePlane.fitWidthProperty().bind(rootScene.widthProperty());

		setTitle(image.getUrl());

		// TODO- This is fucking bullshit. Fix.
		try { Thread.sleep(100); }
		catch(InterruptedException e) {}

		rootScene.widthProperty().addListener(resize -> {
			if (widthProperty().getValue() > 0) {
				minHeightProperty().setValue( rootScene.widthProperty().getValue() * ratio + 35 );
				maxHeightProperty().setValue( rootScene.widthProperty().getValue() * ratio + 35 );
			}
		});
		setOnCloseRequest(closeRequest -> {
			String message = "Security feature.\nClose via the root window";
			Button okButton = new Button("\"Ok? :/\"");
			PromptStage prompt = new PromptStage(message, false, okButton);
			okButton.setOnAction(close -> { prompt.close(); });
			closeRequest.consume();
		});
		show();
	}
	public ImageStage(Image image, Color stageColor) {
		this(image, 600, stageColor);
	}

	void nullAndClose() {
		imagePlane = null;
		rootPane = null;
		rootScene = null;

		close();
	}

	@Override
	public void setImagePropertyListener(ReadOnlyObjectProperty<Image> observedImageProperty ) {
		observedImageProperty.addListener(update -> {
			imagePlane.setImage( observedImageProperty.getValue() );
		});
	}
}
