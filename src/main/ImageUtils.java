package main;

import javafx.scene.image.*;
import javafx.scene.paint.Color;
import main.imageUtils.BrightnessUtils;

import java.util.Random;

public class ImageUtils {

	public static void printColorValues(Image image) {

		PixelReader imageReader = image.getPixelReader();
		int imageHeight = (int)image.getHeight();
		int imageWidth = (int)image.getWidth();

		for (int i = 0; i < imageHeight; i++) {
			for (int j = 0; j < imageWidth; j++) {
				System.out.print(imageReader.getColor(j,i));
			}
			System.out.print("\n");
		}
	}

	/**
	 * Splits an image into four tiles, the tiles are ordered clockwise starting at the Top Left.
	 * @throws IllegalArgumentException When the origin image is too small
	 * @param originImage The image being split
	 * @return An array of ImageTile-objects
	 */
	public static TiledImage splitImage(Image originImage) {
		if (originImage.getWidth() < 2 || originImage.getHeight() < 2)
			throw new IllegalArgumentException("Fuck off nigga.");

		return new TiledImage(originImage);
	}

	public static Image mergeImage(TiledImage tiledImage) {
		return tiledImage.getMergedImage();
	}

	/**
	 * Turns an image's pixels into readable RGBA-values in byte format
	 * @param image the image to be processed
	 * @return the array containing the values
	 */
	public static byte[] getImageAsByteArray(Image image) {
		int width = (int)(image.getWidth());
		int height = (int)(image.getHeight());

		byte[] array = new byte[width * height * 4];
		image.getPixelReader().getPixels(0, 0, width, height, PixelFormat.getByteBgraInstance(), array, 0, width * 4);

		return array;
	}

	public static byte[] convertFromRbgaToAveragedGreyscale(byte[] array) {
		byte[] newArray = new byte[array.length / 4];

		int value = 0;
		for (int i = 0; i < array.length / 4; i++) {
			value += Byte.toUnsignedInt(array[i*4]);
			value += Byte.toUnsignedInt(array[i*4+1]);
			value += Byte.toUnsignedInt(array[i*4+2]);

			newArray[i] = (byte)((value / 3) & 0xff);

			value = 0;
		}

		return newArray;
	}

	public static byte[][] splitRbgaToIndividualRbg(byte[] originArray) {
		byte[][] rgbArray = new byte[3][originArray.length / 4 + 4];

		for (int i = 0; i < originArray.length / 4; i++) {
			rgbArray[0][i] = originArray[i*4];
			rgbArray[1][i+1] = originArray[i*4+1];
			rgbArray[2][i+2] = originArray[i*4+2];
		}

		return rgbArray;
	}
	public static WritableImage createImageFromRgbByteArray(byte[][] originArray, int width, int height) {
		byte[] mergedArray = new byte[ originArray[0].length*3 ];

		for (int i = 0, pos = 0; i < originArray.length; i++, pos = i * 3) {
			mergedArray[i*4] = originArray[0][i]; // red
			mergedArray[i*4+1] = originArray[1][i]; // green
			mergedArray[i*4+2] = originArray[2][i]; // blue
		}

		WritableImage image = new WritableImage(width, height);
		image.getPixelWriter().setPixels(0, 0, width, height, PixelFormat.getByteRgbInstance(), mergedArray, 0, width * 3);

		return image;
	}

	/**
	 * @param sizeInPixels the size of the (square) image
	 * @return a drawn image
	 */
	public static WritableImage createGaussianDistributedImage(int sizeInPixels) {

		int gaussianNumber = 0;
		Random rnd = new Random();
		WritableImage newImage = new WritableImage(sizeInPixels, sizeInPixels);
		PixelWriter newImageWriter = newImage.getPixelWriter();

		for (int i = 0; i < sizeInPixels; i++) {
			for (int j = 0; j < sizeInPixels; j++) {
				gaussianNumber = (int)(rnd.nextGaussian() * 50 + 128);

				if (gaussianNumber < 0) gaussianNumber = 0;
				else if (gaussianNumber > 255) gaussianNumber = 255;

				newImageWriter.setColor(i, j, Color.grayRgb(gaussianNumber));
			}
		}
		return newImage;
	}

	public static int[] createCumulativeHistogramData(byte[] originBand) {
		if (originBand == null)
			throw new NullPointerException("Nigga you null?");

		int[] histogram = new int[originBand.length];
		for (int i = 0; i < originBand.length; i++)
			histogram[ Byte.toUnsignedInt(originBand[i]) ]++;

		int[] cumulative = new int[histogram.length];
		for (int i = 0; i < originBand.length; i++)
			cumulative[i] = (i == 0) ? histogram[i] : histogram[i] + cumulative[i-1];

		return cumulative;
	}

	public static int[] createNormalisedHistogramData(byte[] originBand) {
		if (originBand == null)
			throw new NullPointerException("Nigga you null?");

		int[] histogram = new int[originBand.length];
		for (int i = 0; i < originBand.length; i++)
			histogram[ Byte.toUnsignedInt(originBand[i]) ]++;

		return histogram;
	}

	public static byte[][] adjustBrightness(byte[][] bands, int[] values) {
		return BrightnessUtils.adjustBrightness(bands, values);
	}
	public static byte[] adjustBrightness(byte[] band, int value) {
		return BrightnessUtils.adjustBrightness(band, value);
	}

	public static void printImageByteArrayValues(byte[] array) {
		for (int i = 1; i <= array.length; i++)
			if (i % 4 == 0)
				System.out.println( array[i-1] );
			else
				System.out.print( Byte.toUnsignedInt(array[i-1]) + " ");
	}

	public static byte unsignedIntToByte(int intByte) {
		return (byte)(intByte & 0xff);
	}
}