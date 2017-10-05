package main.imageUtils;

import main.ImageUtils;

public class ContrastUtils {

/* Auto contrast */

	/**
	 * --Do not use--
	 * @param bands
	 * @return
	 */
	public static byte[][] autoContrast(byte[][] bands) {
		return modifiedAutoContrast(bands, 0);
	}
	public static byte[] autoContrast(byte[] band) {
		return modifiedAutoContrast(band, 0);
	}

/* Modified auto contrast */
	public static byte[][] modifiedAutoContrast(byte[][] bands, int percentage) {
		byte[][] newBands = new byte[ bands.length ][ bands[0].length ];

		for (int i = 0; i < bands.length; i++)
			newBands[i] = modifiedAutoContrast(bands[i], percentage);

		return newBands;
	}
	public static byte[] modifiedAutoContrast(byte[] band, int percentage) {

		int total = band.length;
		int target = (int)( total * (percentage / 200.0) );

		int[] histogram = ImageUtils.createHistogramData(band);

		// Finds the the lowest and highest pixel-values within the given pixel-amount
		int lo = 0, hi = 255;
		for (int cumulativeLo = 0; lo < hi; lo++)
			if ( (cumulativeLo += histogram[lo]) >= target)
				break;

		for (int cumulativeHi = 0; hi > lo; hi--) // i > lo: because we don't want lo and hi to cross each other
			if ( (cumulativeHi += histogram[hi]) >= target)
				break;

		int[] lookUpHistogram = new int[ histogram.length ];
		float stretchFactor = histogram.length / (float)(hi - lo);

		for (int i = histogram.length/2; i < histogram.length; i++)
			lookUpHistogram[i] = 255; // To ensure the high end is populated by 255, not 0

		for (int i = lo, j = 0; i < hi; i++, j++)
			lookUpHistogram[i] = (int)(j * stretchFactor);

		byte[] newBand = stretchBand(band, lookUpHistogram);

		return newBand;
	}
	private static byte[] stretchBand(byte[] band, int[] lookUpHistogram) {
		byte[] newBand = new byte[band.length];

		for (int i = 0; i < band.length; i++)
			newBand[i] = (byte)lookUpHistogram[ Byte.toUnsignedInt(band[i]) ];

		return newBand;
	}

/* Manual thresholding */
	public static byte[][] manualThreshold(byte[][] bands, double low, double high) {
		byte[][] newBands = new byte[bands.length][bands[0].length];

		for (int i = 0; i < newBands.length; i++)
			newBands[i] = manualThreshold( bands[i], low, high );

		return newBands;
	}
	public static byte[] manualThreshold(byte[] band, double low, double high) {
		byte[] newBand = new byte[band.length];

		int[] lookUpHistogram = new int[256];
		for (int i = 0; i < lookUpHistogram.length; i++) {
			if (i <= low)
				lookUpHistogram[i] = 0;
			else if (i >= high)
				lookUpHistogram[i] = 255;
			else
				lookUpHistogram[i] = i;
		}

		for (int i = 0; i < band.length; i++)
			newBand[i] = (byte)lookUpHistogram[ Byte.toUnsignedInt(band[i]) ];



		return newBand;
	}

/* Otsu thresholding */
	/**
	 * TODO - All of it
	 * This method will convert all values within given bounds to either 0 or 255
	 * @param band the values to work on
	 * @param minThreshold defines the lower bound of values to be converted
	 * @param maxThreshold defines the upper bound of values to be converted
	 * @return the processed values
	 */
	public static byte[] thresholding( byte[] band, int minThreshold, int maxThreshold) {
		if (minThreshold < 0 || minThreshold > 255)
			throw new IllegalArgumentException("Threshold must be in the range 0.0-1.0");
		if (maxThreshold < 0 || maxThreshold > 255)
			throw new IllegalArgumentException("Threshold must be in the range 0.0-1.0");

		if (minThreshold > maxThreshold)
			throw new IllegalArgumentException("Max threshold must be greater than Min threshold");

		int mainThreshold = 127;  // Due to input being byte

		for (int i = 0, value = ImageUtils.byteToInteger(band[0]); i < band.length; i++, value = ImageUtils.byteToInteger(band[i]) ) {
			if (value > minThreshold && value < mainThreshold)
				band[i] = -128;
			else if (value < maxThreshold && value > mainThreshold)
				band[i] = 127;
		}


		return band;
	}



}
