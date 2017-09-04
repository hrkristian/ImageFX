package main;

import javafx.scene.image.*;
import javafx.scene.paint.Color;

public class ImageProcessor {


	WritableImage image;
	PixelReader imageReader;
	PixelWriter imageWriter;

	int imageHeight, imageWidth;
	double imageSizeFactor;

	ImageStage imageStage;

	boolean bright = false;

	public ImageProcessor(Image originImage) {
		imageStage = new ImageStage(originImage);

		doImageCalculations(originImage);
		updateImageAndTools( new WritableImage(originImage.getPixelReader(), imageWidth, imageHeight) );
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

		WritableImage tmpImage = new WritableImage(imageHeight, imageWidth);
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

		WritableImage tmpImage = new WritableImage(imageHeight, imageWidth);
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

		updateImageAndTools(tmpImage);
	}
	/**
	 * Does not work, and fuck it.
	 */
	public void animateImageHorizontally() {
		doImageCalculations();

		System.out.println("Width: "+imageWidth);
		System.out.println("Height: "+imageHeight);

		WritableImage tmpImage;
		PixelWriter tmpWriter;

		for (int i = 0; i < imageWidth; i++) {
			tmpImage = new WritableImage(imageWidth, imageHeight);
			tmpWriter = tmpImage.getPixelWriter();

			for (int j = 0; j < imageWidth; j++ ) {
				for (int k = 0; k < imageHeight; k++) {

					tmpWriter.setColor(j, k, imageReader.getColor(increment(j, imageWidth), k));
				}
			}
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
		byte[][] bands = ImageUtils.splitRbgaToIndividualRbg( ImageUtils.getImageAsByteArray(image) );

		for (byte[] array : bands) {
			Histogram tmp = new Histogram();
			tmp.populateHistogram(array);
		}

	}

	// Utility
	private int increment(int current, int max) {
		return (++current >= max) ? 0 : current;
	}

	private void updateImageAndTools(WritableImage tmpImage) {
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

}
