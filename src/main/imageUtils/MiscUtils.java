package main.imageUtils;

import static java.lang.Math.*;

public class MiscUtils {

	/**
	 * Adjusts an image to become normaldistributed, given an expected value and standard deviation value such that N(μ,σ)
	 * @param bands Greyscale (identical bands) only
	 * @param expectedValue The expected value mu, μ
	 * @param stdDeviation The standard deviation sigma, σ
	 * @return
	 */
	public static byte[][] imageNormalisation( byte[][] bands, int expectedValue, int stdDeviation) {

		int arrayLength = 256;

		double[] histogram_TargetValues = createProbabilityDensityDistributedImage(expectedValue, stdDeviation);

		double[] cumulative_TargetValues = new double[histogram_TargetValues.length];
		for (int i = 1; i < arrayLength; i++)
			cumulative_TargetValues[i] = histogram_TargetValues[i] + cumulative_TargetValues[i-1];

		int[] scaledCumulative_TargetValues = new int[arrayLength];
		for (int i = 0; i < arrayLength; i++)
			scaledCumulative_TargetValues[i] = (int)(cumulative_TargetValues[i] / cumulative_TargetValues[255] * 255);


		int[] inverseHistogram = new int[256];
		for (int i = 0; i < arrayLength; i++)
			inverseHistogram[ scaledCumulative_TargetValues[i] ] = i;
		for (int i = 1; i < arrayLength; i++)
			if (inverseHistogram[i] == 0)
				inverseHistogram[i] = inverseHistogram[i-1];

		byte[][] newBands = new byte[3][bands[0].length];
		for (int i = 0; i < bands.length; i++) {
			for (int j = 0; j < bands[0].length; j++) {
				newBands[i][j] = (byte)inverseHistogram[ Byte.toUnsignedInt( bands[i][j] ) ];
			}
		}
		return newBands;
	}
	public static double[] createProbabilityDensityDistributedImage(int expectedValue, int standardDeviation) {
		double[] returnBand = new double[256];

		int eV = expectedValue; // my, u
		int stdDev = standardDeviation; // sigma, r

		double expressionFactorOne = ( 1 / ( stdDev * sqrt(2*PI)  ) );
		for (int i = 0; i < returnBand.length; i++) {
			double eExponent = -( pow(i - eV, 2) / ( 2 * pow(stdDev, 2) ) );
			returnBand[i] = expressionFactorOne * pow(E, eExponent);
		}

		return returnBand;


	}

}
