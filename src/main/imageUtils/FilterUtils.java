package main.imageUtils;

import java.util.Arrays;

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
		if (!GeneralUtils.checkRgbBandLengths(bands))
			throw new IllegalArgumentException("The bands are not of equal length.");

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

	public static byte[][] weightedMedianFilter(byte[][] bands, int imageWidth, int filterSize) {
		if (!GeneralUtils.checkRgbBandLengths(bands))
			throw new IllegalArgumentException("The bands are not of equal length.");
		if (filterSize % 2 != 1 || filterSize < 3)
			throw new IllegalArgumentException("The filter size must be an off value greater than 3");

		byte[][] newBands = new byte[bands.length][bands[0].length];
		for (int i = 0; i < bands.length; i++)
			newBands[i] = weightedMedianFilter(bands[i], imageWidth, filterSize);

		return newBands;
	}
	private static byte[] weightedMedianFilter(byte[] band, int imageWidth, int filterSize) {

		try {
			System.out.println("Sleeping...");
			Thread.sleep(5000);
			System.out.println("Continuing.");
		} catch (InterruptedException e) {}

		byte[] newBand = new byte[band.length];

		int[][] matrix = weightedMatrix(filterSize);

		int imageHeight = band.length / imageWidth;
		int offset = filterSize / 2;

		int[][] valueBand = new int[imageHeight][imageWidth];

		for (int i = 0; i < imageHeight; i++)
			for (int j = 0; j < imageWidth; j++)
				valueBand[i][j] = Byte.toUnsignedInt( band[ (i * imageWidth) + j ] );
		// Values are correct.

		for (int y = 0; y < imageHeight; y++) {

			final int yMatrixStart = (y < offset) ? -offset + ( offset - y )  : -offset;
			final int yMatrixEnd = (y < imageHeight - offset) ? offset : imageHeight - y - 1;

			for (int x = 0; x < imageWidth; x++) { // I(y,x)

				final int xMatrixStart = (x < offset) ? -offset + (offset - x) : -offset;
				final int xMatrixEnd = (x < imageWidth - offset) ? offset : imageWidth - x - 1;

				int weightsTotal = 0;
				for (int i = yMatrixStart; i <= yMatrixEnd; i++)
					for (int j = xMatrixStart; j <= xMatrixEnd; j++)
						weightsTotal += matrix[i + offset][j + offset];

				int[] valueStore = new int[weightsTotal];
				int valueStoreCounter = 0;

				for (int i = yMatrixStart; i <= yMatrixEnd; i++) {
					for (int j = xMatrixStart; j <= xMatrixEnd; j++) {

						for (int k = 0; k < matrix[i + offset][j + offset]; k++) {
							int value = valueBand[y + i][x + j];
							valueStore[valueStoreCounter] = value;
							valueStoreCounter++;
						}
					}
				}

				Arrays.sort(valueStore);

				int theMedianValue;
				if (weightsTotal % 2 == 0)
					theMedianValue = (valueStore[valueStore.length / 2] + valueStore[valueStore.length / 2 + 1]) / 2;
				else
					theMedianValue = valueStore[valueStore.length / 2];

				newBand[y*imageWidth + x] = (byte)theMedianValue;

			}
		}
		return newBand;
	}

	public static byte[][] pseudoMedianFilter(byte [][] bands, int imageWidth, int filterSize) {
		if (!GeneralUtils.checkRgbBandLengths(bands))
			throw new IllegalArgumentException("The bands are not of equal length.");
		if (filterSize % 2 != 1 || filterSize < 3)
			throw new IllegalArgumentException("The filter size must be an off value greater than 3");

		byte[][] newBands = new byte[bands.length][bands[0].length];
		for (int i = 0; i < bands.length; i++)
			newBands[i] = pseudoMedianFilter(bands[i], imageWidth, filterSize);

		return newBands;
	}
	private static byte[] pseudoMedianFilter(byte [] band, int imageWidth, int filterSize) {
		byte[] newBand = new byte[band.length];

		int imageHeight = band.length / imageWidth;
		int offset = filterSize / 2;
		final int[] horizontalFilter = new int[filterSize];
		final int[] verticalFilter = new int[filterSize];

		{
			int[][] throwAwayMatrix = weightedMatrix(filterSize);
			for (int i = 0; i < filterSize; i++) {
				horizontalFilter[i] = throwAwayMatrix[offset][i];
				verticalFilter[i] = throwAwayMatrix[i][offset];
			} // There's no point having them separate, it's just future-proofing.
		}

		for (int y = 0; y < imageHeight; y++) {
			for (int x = 0; x < imageWidth; x++) { // Parses horizontally
				int filterStart = (x < offset) ? -offset + ( offset - x ) : -offset;
				int filterEnd = (x > imageWidth - offset) ? imageWidth - x - 1 : offset;

				int weightsTotal = 0;
				for (int i = filterStart; i <= filterEnd; i++)
						weightsTotal += horizontalFilter[i + offset];

				int valueStoreCounter = 0;
				int[] valueStore = new int[weightsTotal];
				for (int i = filterStart; i <= filterEnd; i++)
					valueStore[valueStoreCounter++] = Byte.toUnsignedInt(band[y * imageWidth + x]);

				Arrays.sort(valueStore);

				if (weightsTotal % 2 == 0)
					band[y * imageWidth + x ] = (byte)((valueStore[weightsTotal / 2] + valueStore[weightsTotal / 2]) / 2);
				else
					band[y * imageWidth + x ] = (byte)valueStore[weightsTotal / 2];
			}
		}
		for (int x = 0; x < imageWidth; x++) {
			for (int y = 0; y < imageHeight; y++) { // Parses vertically
				int filterStart = (y < offset) ? -offset + ( offset - y ) : -offset;
				int filterEnd = (y > imageHeight - offset) ? imageHeight - y - 1 : offset;

				int weightsTotal = 0;
				for (int i = filterStart; i <= filterEnd; i++)
					weightsTotal += horizontalFilter[i + offset];

				int valueStoreCounter = 0;
				int[] valueStore = new int[weightsTotal];
				for (int i = filterStart; i <= filterEnd; i++) {

				}
			}
		}


		return newBand;
	}

	/**
	 * Returns a weighted matrix of the given size.
	 * The matrix is weighted by vector subtraction from the center of the matrix.
	 * @param size The size of the matrix, must be an odd number 3 or higher.
	 * @return The weighted matrix.
	 */
	private static int[][] weightedMatrix(int size) {
		if (size % 2 != 1 || size < 3)
			throw new IllegalArgumentException("The size must be an odd size 3 or higher.");

		int[][] matrix = new int[size][size];
		int centerXY = size / 2;

		for (int i = 0; i < size; i++)
			for (int j = 0; j < size; j++)
				matrix[i][j] = size - ( Math.abs(i - centerXY) + Math.abs(j - centerXY) );

		return matrix;
	}
	private static int matrixWeightTotal(int[][] matrix) {
		if (matrix.length != matrix[0].length)
			throw new IllegalArgumentException("Matrix size must be equal in both dimensions.");

		int totalWeight = 0;
		for (int i = 0; i < matrix.length; i++)
			for (int j = 0; j < matrix.length; j++)
				totalWeight += matrix[i][j];

		return totalWeight;
	}

}
