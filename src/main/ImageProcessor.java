package main;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.image.*;
import javafx.scene.paint.Color;
import main.imageUtils.BrightnessUtils;

public class ImageProcessor implements ImageObserver {

	protected ImageDetails imageDetails;

	/* Display */
	private ImageStage imageStage;

	// Secondary tools
	private Histogram histogram;

	/* New Observable-centric variables */
	private WritableImage image;
	private int imageWidth, imageHeight;
	private double imageSizeFactor;
	private PixelReader imageReader;
	private PixelWriter imageWriter;

	public ImageProcessor(Image originImage) {
		imageDetails = new ImageDetails();

		setImagePropertyListener(imageDetails.getImageReadOnlyProperty());

		imageDetails.setImage(originImage);

		imageStage = new ImageStage(originImage);
		imageStage.setImagePropertyListener(imageDetails.getImageReadOnlyProperty());
	}
	public ImageProcessor(Image originImage, String filePath) {
		this(originImage);
		imageDetails.filePath = filePath;
	}

	public void setImagePropertyListener(ReadOnlyObjectProperty<Image> observedImageProperty) {
		observedImageProperty.addListener(imageUpdate -> {
			image = (WritableImage)observedImageProperty.getValue();
			imageWidth = (int)image.getWidth();
			imageHeight = (int)image.getHeight();
			imageSizeFactor = imageWidth / imageHeight;
			imageReader = image.getPixelReader();
			imageWriter = image.getPixelWriter();
		});
	}


	/* Mostly pointless shit */
	public void flipImageOverX() {
		Color leftColor, rightColor;

		WritableImage tmpImage = new WritableImage(imageWidth, imageHeight);
		PixelWriter tmpWriter = tmpImage.getPixelWriter();

		for (int i = 0; i < imageHeight; i++) {
			for (int j = 0; j < imageWidth/2; j++) {
				int rightPos = imageWidth - j - 1;
				leftColor = imageReader.getColor(j,i);
				rightColor = imageReader.getColor(rightPos, i);

				tmpWriter.setColor( j, i, rightColor );
				tmpWriter.setColor( rightPos, i, leftColor );
			}
		}

		imageDetails.setImage(tmpImage);
	}
	public void flipImageOverY() {
		Color topColor, bottomColor;

		WritableImage tmpImage = new WritableImage(imageWidth, imageHeight);
		PixelWriter tmpWriter = tmpImage.getPixelWriter();

		for (int i = 0; i < imageWidth; i++) {
			for (int j = 0; j < imageHeight/2; j++) {
				int bottomPos = imageHeight - j - 1;
				topColor = imageReader.getColor(i,j);
				bottomColor = imageReader.getColor(i, bottomPos);

				tmpWriter.setColor(i, j, bottomColor);
				tmpWriter.setColor(i, bottomPos, topColor);

			}
		}

		imageDetails.setImage(tmpImage);
	}
	public void flipImageOverXY() {

		WritableImage tmpImage = new WritableImage(imageHeight, imageWidth);
		PixelWriter tmpWriter = tmpImage.getPixelWriter();

		for (int i = 0; i < imageWidth; i++)
			for (int j = 0; j < imageHeight; j++)
				tmpWriter.setColor(j, i, imageReader.getColor(i, j));

		imageDetails.setImage(tmpImage);
	}
	public void frameImageWithBorder() {

		final int VAL = 50;
		int xSpacing = VAL;
		int ySpacing = (int)(VAL*imageSizeFactor);

		int newWidth = imageWidth + (xSpacing*2);
		int newHeight = imageHeight + (ySpacing*2);

		WritableImage tmpImage = new WritableImage(newWidth, newHeight);
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

		imageDetails.setImage(tmpImage);
	}
	private void rotateImage(int degrees) { // DO NOT USE
		switch (degrees) {
			case 90: case 180: case 270: case 360:
				break;
			default:
				throw new IllegalArgumentException();
		}



		WritableImage tmpImage = new WritableImage(imageHeight, imageWidth);
		PixelWriter tmpWriter = tmpImage.getPixelWriter();

		for (int i = 0; i < imageWidth; i++)
			for (int j = 0; j < imageHeight; j++)
				tmpWriter.setColor(j, i, imageReader.getColor(i, j));

	}
	public void moveImageHorizontally(int percentage) {
		if (percentage < 0 || percentage > 100)
			throw new IllegalArgumentException();

		WritableImage tmpImage = new WritableImage(imageWidth, imageHeight);
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

		imageDetails.setImage(tmpImage);
	}

	/* The beginning of multithreading capabilities and shit */
	public TiledImage splitImage() {
		return ImageUtils.splitImage(image);
	}
	public void displayImageTiles(TiledImage tiledImage) {
		for ( Image img : tiledImage.getImageTiles() )
			new ImageStage(img, 200);
	}


	/* Useful shit */
	public void showImageHistogram() {
		histogram = new Histogram();

		byte[][] bands = ImageUtils.splitRbgaToIndividualRbg( ImageUtils.getImageAsByteArray(image) );
		String[] desc = {"red", "green", "blue"};

		histogram.populateHistogram(bands, desc);

		byte[] greyscale = ImageUtils.convertFromRbgaToAveragedGreyscale(ImageUtils.getImageAsByteArray(image));
		histogram.populateHistogram(greyscale, "greyscale");

		histogram.setImagePropertyListener(imageDetails.getImageReadOnlyProperty());
	}

	/* Brightness shit */
	public void adjustBrightness(int amount) {
		byte[][] theByteThing = ImageUtils.splitRbgaToIndividualRbg(ImageUtils.getImageAsByteArray(image));
		int[] theAmountThing = {amount, amount, amount};

		theByteThing = ImageUtils.adjustBrightness(theByteThing, theAmountThing);
		imageDetails.setImage(ImageUtils.createImageFromRgbByteArray(theByteThing, imageWidth, imageHeight));
	}


	/* Utility */
	private int increment(int current, int max) {
		return (++current >= max) ? 0 : current;
	}

	public void nullAndClose() {
		imageStage.nullAndClose();
		imageStage = null;
	}

	/**
	 *
	 */
	class ImageDetails {

		/* Observables */
		private ReadOnlyObjectWrapper<ImageStatus> imageStateProperty;
		private ReadOnlyObjectWrapper<Image> currentImageProperty;

		/* Shit */
		private WritableImage image;
		private WritableImage backupImage;


		/* Details */
		private String filePath;
		private ImageType type;

		public ImageDetails() {
			imageStateProperty = new ReadOnlyObjectWrapper<>(ImageStatus.INITIALISING);
			currentImageProperty = new ReadOnlyObjectWrapper<Image>(null);
		}
		public ImageDetails(Image image) {
			this.image = new WritableImage( image.getPixelReader(), (int)image.getWidth(), (int)image.getHeight() );
			sharedConstructorTasks();
		}
		public ImageDetails(int width, int height) {
			this.image = new WritableImage( width, height );
			sharedConstructorTasks();
		}

		private void sharedConstructorTasks() {
			imageStateProperty = new ReadOnlyObjectWrapper<>(ImageStatus.INITIALISING);
			currentImageProperty = new ReadOnlyObjectWrapper<>(image);

			// TODO- Utilize ImageUtils to judge the content of the image; create details.

			setProcessingProperty(ImageStatus.AVAILABLE);
		}
		/**
		 *
		 * @return the read only property of the Image's status
		 */
		public ReadOnlyObjectProperty<ImageStatus> getimageStateReadOnlyProperty() {
			return imageStateProperty.getReadOnlyProperty();
		}
		public ReadOnlyObjectProperty<Image> getImageReadOnlyProperty() {
			return currentImageProperty.getReadOnlyProperty();
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
		public WritableImage getImageForProcessing() {
			if (imageStateProperty.getValue() == ImageStatus.AVAILABLE) {
				imageStateProperty.set(ImageStatus.PROCESSING);
				backupImage = image;
				return image;
			}
			return null;
		}

		public void setImage(Image newImage) { // TODO- Necessary?
			setImage(new WritableImage(newImage.getPixelReader(), (int)newImage.getWidth(), (int)newImage.getHeight()));
		}

		public void setImage(WritableImage newImage) {
			image = newImage;
			currentImageProperty.set(image);
			imageStateProperty.set(ImageStatus.AVAILABLE);
		}


		/* Private functions */
		private void setProcessingProperty(ImageStatus status) {
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
