package main.imageUtils;

import main.ImageUtils;

public class BrightnessUtils {

	public static byte[] adjustBrightness( byte[] originalBand, int amount ) {

		if (amount == 0) {
			return originalBand;

		} else if (amount > 0) {
//			System.out.println("Amount is larger than 0: " + amount);

			for (int i = 0, newValue; i < originalBand.length; i++ ) {

				newValue = Byte.toUnsignedInt(originalBand[i]) + amount;
				if (newValue > 255)
					originalBand[i] = (byte)255;
				else
					originalBand[i] = (byte)(newValue);
			}
		} else if (amount < 0) { // That's just not true, IntelliJ!
//			System.out.println("Amount is smaller than 0: " + amount);

			for (int i = 0, newValue; i < originalBand.length; i++ ) {

				newValue = Byte.toUnsignedInt(originalBand[i]) + amount;
				if (newValue < 0)
					originalBand[i] = (byte)0;
				else
					originalBand[i] = (byte)(newValue);
			}
		}

		return originalBand;
	}

	/**
	 *
	 * @param originalBands typically an RGB band, will not handle alpha-values. Another method will be needed for that.
	 * @param amounts the amount to increase brightness by.
	 * @return
	 */
	public static byte[][] adjustBrightness( byte[][] originalBands, int[] amounts ) {
/*
		System.out.println("Number of originalBands: "+originalBands.length);
		System.out.println("Length of individual originalBands: ");
		int count = 1;
		for (byte[] band : originalBands)
			System.out.println("Band " + (count++) + ":" + band.length);
		System.out.print("Amount to adjust: ");
		for (int i : amounts)
			System.out.print(i + " ");
		System.out.println("\n---------------------------");
*/

		// Because we need to know the value for each band.
		if (originalBands.length != amounts.length)
			throw new IllegalArgumentException("Number of originalBands and amounts must not differ in length.");

		// Because bands need to have the same range in the picture.
		for (int i = 1, initLength = originalBands[0].length; i < originalBands.length; i++)
			if (originalBands[i].length != initLength)
				throw new IllegalArgumentException("Individual originalBands must be of identical length.");

		byte[][] returnBands = new byte[ originalBands.length ][ originalBands[0].length ];

		for (int i = 0; i < originalBands.length; i++) {
/*
			System.out.println("Band "+i);

			for (int j = 1; j <= 100; j++)
				if (j % 20 == 0)
					System.out.println(originalBands[i][j-1] + " ");
				else
					System.out.print(originalBands[i][j-1] + " ");
			System.out.println("\n-----------------split--------------------");
*/
				returnBands[i] = adjustBrightness(originalBands[i], amounts[i]);
/*
			for (int j = 1; j <= 100; j++)
				if (j % 20 == 0)
					System.out.println(returnBands[i][j-1] + " ");
				else
					System.out.print(returnBands[i][j-1] + " ");

			System.out.println("\n------------------end--------------------");
*/
		}

		return returnBands;
	}
}
