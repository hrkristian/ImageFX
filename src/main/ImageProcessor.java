package main;

import javafx.beans.property.*;
import javafx.scene.image.*;
import javafx.scene.paint.Color;
import main.imageUtils.ContrastUtils;
import main.imageUtils.FilterUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class ImageProcessor implements ImageObserver {

	protected ImageDetails imageDetails;

	private WritableImage image;

	/* Secondary variables */
	private int imageWidth, imageHeight;
	private double imageSizeFactor;
	private PixelReader imageReader;
	private PixelWriter imageWriter;

	/* Display */
	private ImageStage imageStage;

	// Secondary tools
	private Histogram histogram;
	private BrightnessContrastStage brightnessContrastStage;

	public ImageProcessor(Image originImage, Iterator<Color> stageColorIterator) {

		// The order ensures all variables are initialised by the listener
		imageDetails = new ImageDetails();
		setImagePropertyListener(imageDetails.getImageReadOnlyProperty());
		imageDetails.commitImage(
				new WritableImage(
						originImage.getPixelReader(), (int)originImage.getWidth(), (int)originImage.getHeight()));

		imageStage = new ImageStage(originImage, stageColorIterator.next());
		imageStage.setImagePropertyListener(imageDetails.getImageReadOnlyProperty());

	}
	public ImageProcessor(Image originImage, String filePath, Iterator<Color> stageColorIterator) {
		this(originImage, stageColorIterator);
		imageDetails.imageFilePath = filePath;
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


	/* Mostly pointless stuff */
	void flipImageOverX() {
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
	void flipImageOverY() {
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
	void flipImageOverXY() {

		WritableImage tmpImage = new WritableImage(imageHeight, imageWidth);
		PixelWriter tmpWriter = tmpImage.getPixelWriter();

		for (int i = 0; i < imageWidth; i++)
			for (int j = 0; j < imageHeight; j++)
				tmpWriter.setColor(j, i, imageReader.getColor(i, j));

		imageDetails.setImage(tmpImage);
	}
	void frameImageWithBorder() {

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
	void moveImageHorizontally(int percentage) {
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

	/* The beginning of multithreading capabilities and stuff */
	TiledImage splitImage() {
		return ImageUtils.splitImage(image);
	}
	void displayImageTiles(TiledImage tiledImage) {
		for ( Image img : tiledImage.getImageTiles() )
			new ImageStage(img, 200, Color.BLACK);
	}


	/* Useful stuff */
	void showImageHistogram() {

		histogram = new Histogram(this, Color.BLACK);

		// TODO- new Thread?
		byte[] greyscale = ImageUtils.convertFromBgraToAveragedGreyscale(ImageUtils.getImageAsByteArray(image));
		histogram.populateHistogram(greyscale, "greyscale");

		byte[][] bands = ImageUtils.splitRbgaToIndividualRbg( ImageUtils.getImageAsByteArray(image) );
		String[] desc = {"red", "green", "blue"};

		histogram.populateHistogram(bands, desc);

		histogram.setImagePropertyListener(imageDetails.getImageReadOnlyProperty());
	}
	void showBrightnessContrastStage() {
		if (brightnessContrastStage == null)
			brightnessContrastStage = new BrightnessContrastStage(this, Color.BLACK);
	}

	/* Brightness stuff */
	void adjustBrightness(int amount) {
		int[] theAmountThing = {amount, amount, amount};

		byte[][] theNewByteThing = ImageUtils.adjustBrightness(imageDetails.getOriginalImageRgbArray(), theAmountThing);
		imageDetails.setImage(ImageUtils.createImageFromRgbByteArray(theNewByteThing, imageWidth, imageHeight));
	}

	/* Contrast stuff */
	void autoContrast() {
		byte[][] newByteArray = ContrastUtils.autoContrast(imageDetails.getOriginalImageRgbArray());
		imageDetails.setImage(ImageUtils.createImageFromRgbByteArray(newByteArray, imageWidth, imageHeight));
	}
	void manualAutoContrast(int percentage) {
		byte[][] newByteArray = ContrastUtils.modifiedAutoContrast(imageDetails.getOriginalImageRgbArray(), percentage);
		imageDetails.setImage(ImageUtils.createImageFromRgbByteArray(newByteArray, imageWidth, imageHeight));
	}
	void manualThreshold(double low, double high) {
		byte[][] newByteArray = ContrastUtils.manualThreshold(imageDetails.getOriginalImageRgbArray(), low, high);
		imageDetails.setImage(ImageUtils.createImageFromRgbByteArray(newByteArray, imageWidth, imageHeight));
	}
	void imageNormalisation(int expectedValue, int standardDeviation) {
		byte[][] newByteArray = ImageUtils.imageNormalisation(imageDetails.getOriginalImageRgbArray(), expectedValue, standardDeviation);
		imageDetails.setImage(ImageUtils.createImageFromRgbByteArray(newByteArray, imageWidth, imageHeight));
	}
	void smoothImage(int filterLength, int filterHeight) {
		byte[][] newByteArray = FilterUtils.smooth(imageDetails.getOriginalImageRgbArray(), imageWidth, 3, 3);
		imageDetails.setImage(ImageUtils.createImageFromRgbByteArray(newByteArray, imageWidth, imageHeight));
	}
	void weightedMedianFiltering() {
		byte[][] newByteArray = FilterUtils.weightedMedianFilter(imageDetails.getOriginalImageRgbArray(), imageWidth, 5);
		imageDetails.setImage(ImageUtils.createImageFromRgbByteArray(newByteArray, imageWidth, imageHeight));
	}

	/* Utility */
	private int increment(int current, int max) {
		return (++current >= max) ? 0 : current;
	}

	void nullAndClose() {
		if (imageStage != null) {
			imageStage.nullAndClose();
			imageStage = null;
		}
		if (histogram != null) {
			histogram.close();
			histogram = null;
		}
	}

	/**
	 *
	 */
	class ImageDetails {

		/* Observables */
		private ReadOnlyObjectWrapper<ImageStatus> imageStateProperty;
		private ReadOnlyObjectWrapper<Image> currentImageProperty;

		/* Shit */
		private WritableImage originalImage; // TODO- Pull out of ImageProcessor, should not at any point be directly accessible.
		private WritableImage processingImage; // TODO- Should not be directly accessible either.

		private byte[] originalImageByteArray;
		private byte[][] originalImageRgbArray;

		/* Details */
		private String imageFilePath; // TODO- Pass from RootStage upon creation
		private ImageType imageType;

		public int imageWidth, imageHeight;

		public ImageDetails() {
			imageStateProperty = new ReadOnlyObjectWrapper<>(ImageStatus.INITIALISING);
			currentImageProperty = new ReadOnlyObjectWrapper<>(null);
		}
		public ImageDetails(Image image) {
			this.originalImage = new WritableImage( image.getPixelReader(), (int)image.getWidth(), (int)image.getHeight() );
			sharedConstructorTasks();
		}
		public ImageDetails(int width, int height) {
			this.originalImage = new WritableImage( width, height );
			sharedConstructorTasks();
		}

		private void sharedConstructorTasks() {
			imageStateProperty = new ReadOnlyObjectWrapper<>(ImageStatus.INITIALISING);
			currentImageProperty = new ReadOnlyObjectWrapper<>(originalImage);

			originalImageByteArray = ImageUtils.getImageAsByteArray(originalImage);
			originalImageRgbArray = ImageUtils.splitRbgaToIndividualRbg(originalImageByteArray);

			imageType = ImageUtils.getImageColorType(originalImage);
			imageWidth = (int)processingImage.getWidth();
			imageHeight = (int)processingImage.getHeight();

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
			return originalImage;
		}

		/**
		 * TODO- Throw exception instead of null?
		 * @return the image for processing, or null if image is being processed
		 */
		public WritableImage getImageForProcessing() {
			if (imageStateProperty.getValue() == ImageStatus.AVAILABLE) {
				imageStateProperty.set(ImageStatus.PROCESSING);
				return processingImage;
			}
			return null;
		}

		private WritableImage getOriginalImage() {
			return new WritableImage(originalImage.getPixelReader(), (int)originalImage.getWidth(), (int)originalImage.getHeight());
		}
		private byte[] getOriginalImageByteArray() {
			return Arrays.copyOf(originalImageByteArray, originalImageByteArray.length);
		}

		private byte[][] getOriginalImageRgbArray() {
			byte[][] returnArray = new byte[originalImageRgbArray.length][0];
			for (int i = 0; i < originalImageRgbArray.length; i++)
				returnArray[i] = Arrays.copyOf(originalImageRgbArray[i], originalImageRgbArray[i].length);

			return returnArray;
		}

		public void setImage(Image newImage) { // TODO- Necessary?
			setImage(new WritableImage(newImage.getPixelReader(), (int)newImage.getWidth(), (int)newImage.getHeight()));
		}

		synchronized private void setImage(WritableImage newImage) {
			processingImage = newImage;
			imageWidth = (int)processingImage.getWidth();
			imageHeight = (int)processingImage.getHeight();

			currentImageProperty.set(processingImage);
			imageStateProperty.set(ImageStatus.AVAILABLE);
		}
		synchronized private void commitImage(WritableImage newImage) {
			originalImage = processingImage = newImage;
			imageWidth = (int)processingImage.getWidth();
			imageHeight = (int)processingImage.getHeight();


			originalImageByteArray = ImageUtils.getImageAsByteArray(originalImage);
			originalImageRgbArray = ImageUtils.splitRbgaToIndividualRbg(originalImageByteArray);

			currentImageProperty.set(originalImage);
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
