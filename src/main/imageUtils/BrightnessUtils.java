package main.imageUtils;

import static main.ImageUtils.unsignedIntToByte;

public class BrightnessUtils {

	public static byte[] adjustBrightness( byte[] band, int amount ) {
		if (amount == 0)
			return band;
		else if (amount > 0) {
			System.out.println("Amount is larger than 0: "+amount);
			System.out.println("Band length is: "+band.length);
			for (int i = 0, newValue = Byte.toUnsignedInt(band[0]) + amount; i < band.length - 4; i++, newValue = Byte.toUnsignedInt(band[i]) + unsignedIntToByte(amount) ) {

				if (newValue > 255)
					band[i] = unsignedIntToByte(255);
			}
		}
		else if (amount < 0) {
			System.out.println("Amount is smaller than 0: "+amount);
			System.out.println("Band length is: "+band.length);
			for (int i = 0, newValue = Byte.toUnsignedInt(band[0]); i < band.length - 4; i++, newValue = Byte.toUnsignedInt(band[i]) + unsignedIntToByte(amount) ) {

				if (newValue < 0)
					band[i] = unsignedIntToByte(0);
			}
		}

		return band;
	}

	/**
	 *
	 * @param bands typically an RGB band, will not handle alpha-values. Another method will be needed for that.
	 * @param amounts the amount to increase brightness by.
	 * @return
	 */
	public static byte[][] adjustBrightness( byte[][] bands, int[] amounts ) {
		// Because we need to know the value for each band.
		if (bands.length != amounts.length)
			throw new IllegalArgumentException("Number of bands and amounts must not differ in length.");

		// Because bands need to have the same range in the picture.
		for (int i = 1, initLength = bands[0].length; i < bands.length; i++)
			if (bands[i].length != initLength)
				throw new IllegalArgumentException("Individual bands must be of identical length.");

		byte[][] returnBands = new byte[ bands.length ][ bands[0].length ];

		for (int i = 0; i < bands.length; i++)
			returnBands[i] = adjustBrightness(bands[i], amounts[i]);


		return returnBands;
	}
}
