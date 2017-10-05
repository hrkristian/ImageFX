package main.imageUtils;

@SuppressWarnings("Duplicates")

public class FilterUtils {

	/**
	 * Smoothes the picture by the means of a uniform box-filter of a given size.
	 * @param bands An array representation of the original picture.
	 * @param imageWidth The width of the given image
	 * @param filterWidth The width of the smoothing filter
	 * @param filterHeight The height of the smoothing filter
	 * @return An array representation of the new picture
	 */
	public static byte[][] smooth(byte[][] bands, int imageWidth, int filterWidth, int filterHeight) {
		byte[][] newBands = new byte[bands.length][bands[0].length];

		for (int i = 0; i < bands.length; i++)
			newBands[i] = smooth(bands[i], imageWidth, filterWidth, filterHeight);

		return newBands;
	}
	private static byte[] smooth(byte[] band, int imageWidth, int filterWidth, int filterHeight) {
		byte[] newBand = new byte[band.length];

		int imageHeight = band.length / imageWidth;

		int[][] valueBand = new int[imageHeight][imageWidth];
		double[][] transitionBand = new double[imageHeight][imageWidth];

		for (int i = 0; i < imageHeight; i++)
			for (int j = 0; j < imageWidth; j++) {
				valueBand[i][j] = Byte.toUnsignedInt( band[i*imageWidth + j] );
				transitionBand[i][j] = valueBand[i][j];
			}

		int xOffset = filterWidth / 2;
		int yOffset = filterHeight / 2;

		for (int i = 0; i < imageHeight; i++ ) { // Vertical (Y)
			for (int j = 0; j < imageWidth; j++) { // Horizontal (X)

				double sum = 0;

				int summationStart = (j < xOffset) ? -xOffset + (xOffset - j) : -xOffset;
				int summationEnd = (j >= imageWidth - xOffset) ? imageWidth - j - 1 : xOffset;
				int filterIndex = summationStart;

				while ( filterIndex <= summationEnd ) {
					sum += valueBand[i][j + filterIndex];
					filterIndex++;
				}
				transitionBand[i][j] = sum / filterWidth;
			}
		}
		for (int i = 0; i < imageWidth; i++) { // Horizontal (X)
			for (int j = 0; j < imageHeight; j++) { // Vertical (Y)
				
				double sum = 0;

				int summationStart = (j < yOffset) ? -yOffset + (yOffset - j) : -yOffset;
				int summationEnd = (j >= imageHeight - yOffset) ? imageHeight - j - 1 : yOffset;
				int filterIndex = summationStart;

				while ( filterIndex <= summationEnd ) {
					sum += transitionBand[j + filterIndex][i];
					filterIndex++;
				}
				newBand[i + (j*imageWidth)] = (byte)(sum / filterWidth);
			}
		}

		return newBand;
	}

	public static byte[][] weightedMedianFilter(byte[][] bands, int filterWidth, int filterHeight) {
		byte[][] newBands = new byte[bands.length][bands[0].length];
		for (int i = 0; i < bands.length; i++)
			newBands[i] = weightedMedianFilter(bands[i], filterWidth, filterHeight);

		return newBands;
	}
	private static byte[] weightedMedianFilter(byte[] band, int filterWidth, int filterHeight) {
		byte[] newBand = new byte[band.length];
		return newBand;
	}
}
