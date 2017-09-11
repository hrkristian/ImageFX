package main;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.image.*;
import javafx.scene.paint.Color;

public class ImageProcessor {

	ProcessingImage image;
	PixelReader imageReader;
	PixelWriter imageWriter;

	int imageHeight, imageWidth;
	double imageSizeFactor;

	ImageStage imageStage;

	// Secondary tools
	Histogram histogram;

	public ImageProcessor(Image originImage) {
		imageStage = new ImageStage(originImage);

		doImageCalculations(originImage);
		updateImageAndTools( new ProcessingImage(originImage.getPixelReader(), imageWidth, imageHeight) );
	}

	public void flipImageOverX() {
		Color leftColor, rightColor;
		doImageCalculations();

		for (int i = 0; i < imageHeight; i++) {
			for (int j = 0; j < imageWidth/2; j++) {
				int rightPos = imageWidth - j - 1;
				leftColor = imageReader.getColor(j,i);
				rightColor = imageReader.getColor(rightPos, i);

				imageWriter.setColor( j, i, rightColor );
				imageWriter.setColor( rightPos, i, leftColor );
			}
		}

		imageStage.imagePlane.setImage(image);

	}
	public void flipImageOverY() {
		Color topColor, bottomColor;
		doImageCalculations();

		for (int i = 0; i < imageWidth; i++) {
			for (int j = 0; j < imageHeight/2; j++) {
				int bottomPos = imageHeight - j - 1;
				topColor = imageReader.getColor(i,j);
				bottomColor = imageReader.getColor(i, bottomPos);

				imageWriter.setColor(i, j, bottomColor);
				imageWriter.setColor(i, bottomPos, topColor);

			}
		}

		imageStage.imagePlane.setImage(image);
	}
	public void flipImageOverXY() {
		doImageCalculations();

		ProcessingImage tmpImage = new ProcessingImage(imageHeight, imageWidth);
		PixelWriter tmpWriter = tmpImage.getPixelWriter();

		for (int i = 0; i < imageWidth; i++)
			for (int j = 0; j < imageHeight; j++)
				tmpWriter.setColor(j, i, imageReader.getColor(i, j));

		updateImageAndTools(tmpImage);
	}
	public void frameImageWithBorder() {
		doImageCalculations();

		final int VAL = 50;
		int xSpacing = VAL;
		int ySpacing = (int)(VAL*imageSizeFactor);

		int newWidth = imageWidth + (xSpacing*2);
		int newHeight = imageHeight + (ySpacing*2);

		ProcessingImage tmpImage = new ProcessingImage(newWidth, newHeight);
		PixelWriter tmpWriter = tmpImage.getPixelWriter();

		for (int i = 0; i < newHeight; i++) {
			for (int j = 0; j < newWidth; j++) {

				if (i < ySpacing || i >= imageHeight+ySpacing) {
					tmpWriter.setColor(j, i, Color.WHITE);
					continue;
				}
				if (j < xSpacing || j >= imageWidth+xSpacing) {
					tmpWriter.setColor(j, i, Color.WHITE);
					continue;
				}
				tmpWriter.setColor(j, i, imageReader.getColor(j - xSpacing, i - ySpacing));
			}
		}

		updateImageAndTools(tmpImage);
	}
	private void rotateImage(int degrees) { // DO NOT USE
		switch (degrees) {
			case 90: case 180: case 270: case 360:
				break;
			default:
				throw new IllegalArgumentException();
		}


		doImageCalculations();

		ProcessingImage tmpImage = new ProcessingImage(imageHeight, imageWidth);
		PixelWriter tmpWriter = tmpImage.getPixelWriter();

		for (int i = 0; i < imageWidth; i++)
			for (int j = 0; j < imageHeight; j++)
				tmpWriter.setColor(j, i, imageReader.getColor(i, j));

		updateImageAndTools(tmpImage);
	}
	public void moveImageHorizontally(int percentage) {
		if (percentage < 0 || percentage > 100)
			throw new IllegalArgumentException();

		doImageCalculations();
		ProcessingImage tmpImage = new ProcessingImage(imageWidth, imageHeight);
		PixelWriter tmpWriter = tmpImage.getPixelWriter();

		double percentageAsDecimal = percentage/100.0;
		int xSpan = (int)(imageWidth*percentageAsDecimal);

		for (int i = 0; i < imageWidth; i++) {
			for (int j = 0; j < imageHeight; j++) {
				int xInsertionPoint = (i+xSpan >= imageWidth) ? i+xSpan-imageWidth : i+xSpan;
				Color color = imageReader.getColor(i,j);
				tmpWriter.setColor(xInsertionPoint, j, color);
			}
		}

		updateImageAndTools(tmpImage);
	}
	/**
	 * Does not work, and fuck it.
	 */
	public void animateImageHorizontally() {
		doImageCalculations();

		System.out.println("Width: "+imageWidth);
		System.out.println("Height: "+imageHeight);

		ProcessingImage tmpImage;
		PixelWriter tmpWriter;

		for (int i = 0; i < imageWidth; i++) {
			tmpImage = new ProcessingImage(imageWidth, imageHeight);
			tmpWriter = tmpImage.getPixelWriter();

			for (int j = 0; j < imageWidth; j++ )
				for (int k = 0; k < imageHeight; k++)
					tmpWriter.setColor(j, k, imageReader.getColor(increment(j, imageWidth), k));

			updateImageAndTools(tmpImage);

			try { Thread.sleep(100); }
			catch(InterruptedException e) {}

		}
		System.out.println("Done.");
	}

	public TiledImage splitImage() {
		return ImageUtils.splitImage(image);
	}
	public void displayImageTiles(TiledImage tiledImage) {
		for ( Image img : tiledImage.getImageTiles() )
			new ImageStage(img, 200);
	}

	public void showImageHistogram() {
		histogram = new Histogram();

		byte[][] bands = ImageUtils.splitRbgaToIndividualRbg( ImageUtils.getImageAsByteArray(image) );
		String[] desc = {"red", "green", "blue"};

		histogram.populateHistogram(bands, desc);

		byte[] greyscale = ImageUtils.convertFromRbgaToAveragedGreyscale(ImageUtils.getImageAsByteArray(image));
		histogram.populateHistogram(greyscale, "greyscale");
	}


	// Utility
	private int increment(int current, int max) {
		return (++current >= max) ? 0 : current;
	}

	/**
	 * TODO- Deprecate; use Observables instead.
	 * @param tmpImage
	 */
	private void updateImageAndTools(ProcessingImage tmpImage) {
		imageStage.imagePlane.setImage(image = tmpImage);
		imageReader = image.getPixelReader();
		imageWriter = image.getPixelWriter();
	}

	private void doImageCalculations(Image tmpImage) {
		imageHeight = (int)tmpImage.getHeight();
		imageWidth = (int)tmpImage.getWidth();
		imageSizeFactor = (double)imageHeight / (double)imageWidth;
	}
	private void doImageCalculations() {
		doImageCalculations(image);
	}

	public void nullAndClose() {
		imageStage.nullAndClose();
		imageStage = null;
	}


	class ProcessingImageHandler {

		/* Observables */
		private ReadOnlyObjectWrapper<main.ProcessingImage.ImageStatus> imageStateProperty;
		private ReadOnlyObjectWrapper<Image> currentImageProperty;

		/* Shit */
		private WritableImage image;
		private WritableImage backupImage;


		/* Details */
		private String filePath;
		private main.ProcessingImage.ImageType type;


		public ProcessingImageHandler(Image image) {
			this.image = new WritableImage( image.getPixelReader(), (int)image.getWidth(), (int)image.getHeight() );
			sharedConstructorTasks();
		}
		public ProcessingImageHandler(int width, int height) {
			this.image = new WritableImage( width, height );
			sharedConstructorTasks();
		}

		private void sharedConstructorTasks() {
			imageStateProperty = new ReadOnlyObjectWrapper<>(main.ProcessingImage.ImageStatus.INITIALISING);
			// TODO: Utilize ImageUtils to judge the content of the image; create details.

			setProcessingProperty(main.ProcessingImage.ImageStatus.AVAILABLE);
		}

		/* Exposed methods */

		/**
		 *
		 * @return the read only property of the Image's status
		 */
		public ReadOnlyObjectProperty<main.ProcessingImage.ImageStatus> getimageStateReadOnlyProperty() {
			return imageStateProperty.getReadOnlyProperty();
		}

		/**
		 * Only to be used for displaying/updating the image, [b]not processing[/b].
		 * @return a type-forced Image
		 */
		public Image getImage() {
			return image;
		}

		/**
		 * TODO- Throw exception instead of null?
		 * @return the image for processing, or null if image is being processed
		 */
		synchronized public WritableImage getImageForProcessing() {
			if (imageStateProperty.getValue() == main.ProcessingImage.ImageStatus.AVAILABLE) {
				imageStateProperty.set(main.ProcessingImage.ImageStatus.PROCESSING);
				backupImage = image;
				return image;
			}
			return null;
		}

		synchronized public void setImage(Image newImage) { // TODO- Necessary?
			setImage(new WritableImage(newImage.getPixelReader(), (int)newImage.getWidth(), (int)newImage.getHeight()));
		}
		synchronized public void setImage(WritableImage newImage) {
			image = newImage;
			currentImageProperty.set(image);
			imageStateProperty.set(main.ProcessingImage.ImageStatus.AVAILABLE);
		}

		private void setProcessingProperty(main.ProcessingImage.ImageStatus status) {
			imageStateProperty.set(status);
		}
	}
	public enum ImageStatus {
		AVAILABLE,
		PROCESSING,
		INITIALISING,
		CLOSING;
	}
	public enum ImageType {
		GREYSCALE,
		RGB;
	}
}
