package main;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ImageStage extends Stage {
	Scene imageScene;
	VBox imageContainer;
	ImageView imagePlane;
	Image image;

	public ImageStage(Image image, int size) {
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
	public ImageStage(Image image) {
		this(image, 600);
	}

	public void nullAndClose() {

		image = null;
		imagePlane = null;
		imageContainer = null;
		imageScene = null;

		close();
	}
}