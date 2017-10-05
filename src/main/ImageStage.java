package main;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class ImageStage extends ImageFXStage implements ImageObserver {

	Scene imageScene;
	VBox imageContainer;
	ImageView imagePlane;
	Image image;

	public ImageStage(Image image, int size, Color stageColor) {
		super(stageColor);
		this.image = image;

		double ratio = image.getHeight() / image.getWidth();

		imagePlane = new ImageView(image);
		imagePlane.setPreserveRatio(true);

		imageContainer = new VBox(imagePlane);
		imageContainer.setAlignment(Pos.CENTER);

		setMinHeight(600);
		imageScene = new Scene(imageContainer, size, size*ratio);

		// Resize logic
		imagePlane.fitWidthProperty().bind(imageScene.widthProperty());

		setTitle(image.getUrl());
		setScene(imageScene);

		try { Thread.sleep(100); }
		catch(InterruptedException e) {}

		imageScene.widthProperty().addListener(resize -> {
			if (widthProperty().getValue() > 0) {
				minHeightProperty().setValue( imageScene.widthProperty().getValue() * ratio + 25 );
				maxHeightProperty().setValue( imageScene.widthProperty().getValue() * ratio + 25 );
			}
		});
		setOnCloseRequest(closeRequest -> { nullAndClose(); });
		show();
	}
	public ImageStage(Image image, Color stageColor) {
		this(image, 600, stageColor);
	}

	public void nullAndClose() {

		image = null;
		imagePlane = null;
		imageContainer = null;
		imageScene = null;

		close();

	}

	@Override
	public void setImagePropertyListener(ReadOnlyObjectProperty<Image> observedImageProperty ) {
		observedImageProperty.addListener(update -> {
			imagePlane.setImage( image = observedImageProperty.getValue() );
		});
	}
}
