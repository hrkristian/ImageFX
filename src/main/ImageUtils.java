package main;

import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;

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

//		printImageByteArrayValues(array);

		return array;
	}

	public static byte[] convertFromRbgaToGreyscale(byte[] array) {
		byte[] newArray = new byte[array.length / 4];

		int value = 0;
		for (int i = 0; i < array.length / 4; i++) {
			value += Byte.toUnsignedInt(array[i*4]);
			value += Byte.toUnsignedInt(array[i*4+1]);
			value += Byte.toUnsignedInt(array[i*4+2]);

			newArray[i] = (byte)((value / 3) & 0xff);
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
	public static int[] createHistogrammaticalData(byte[] array) {
		int[] histogram = new int[256];

		for (int i = 0; i < array.length; i++)
			histogram[ Byte.toUnsignedInt(array[i]) ]++;

		for (int i : histogram)
			System.out.println(i);

		return histogram;
	}

	public static void printImageByteArrayValues(byte[] array) {
		for (int i = 1; i <= array.length; i++)
			if (i % 4 == 0)
				System.out.println( array[i-1] );
			else
				System.out.print( Byte.toUnsignedInt(array[i-1]) + " ");
	}

}
