package main.imageUtils;

import main.ImageUtils;

public class ContrastUtils {

	/**
	 *
	 * @param band
	 * @param percentage
	 * @return
	 */
	public static byte[] adjustContrast( byte[] band, double percentage ) {
		if (percentage < 0 || percentage > 1)
			throw new IllegalArgumentException("Percentage value is given in the range 0.0-1.0");


		/*
		 * TODO-
		 *
		 */

		return null;
	}

	/**
	 * Overload for adjustContrast()
	 * @param band
	 * @param percentage
	 * @return
	 */
	public static byte[][] adjustContrast( byte[][] band, double percentage ) {

		return null;

	}

	/**
	 * This method will convert all values within given bounds to either 0 or 255
	 * @param band the values to work on
	 * @param minThreshold defines the lower bound of values to be converted
	 * @param maxThreshold defines the upper bound of values to be converted
	 * @return the processed values
	 */
	public static byte[] otsuThresholding( byte[] band, int minThreshold, int maxThreshold) {
		if (minThreshold < 0 || minThreshold > 255)
			throw new IllegalArgumentException("Threshold must be in the range 0.0-1.0");
		if (maxThreshold < 0 || maxThreshold > 255)
			throw new IllegalArgumentException("Threshold must be in the range 0.0-1.0");

		if (minThreshold > maxThreshold)
			throw new IllegalArgumentException("Max threshold must be greater than Min threshold");

		int mainThreshold = 127;  // Due to input being byte

		for (int i = 0, value = Byte.toUnsignedInt(band[0]); i < band.length; i++, value = Byte.toUnsignedInt(band[i]) ) {
			if (value > minThreshold && value < mainThreshold)
				band[i] = ImageUtils.unsignedIntToByte(0);
			else if (value < maxThreshold && value > mainThreshold)
				band[i] = ImageUtils.unsignedIntToByte(255);
		}


		return band;
	}


}
