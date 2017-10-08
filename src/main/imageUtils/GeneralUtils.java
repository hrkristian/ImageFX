package main.imageUtils;

import static java.lang.Math.*;

public class GeneralUtils {

    public static byte[][] histogramNormalisation(byte[][] bands) {

        return null;
    }
    public static double getNormalDistributedNumber(int expectedValue, int standardDeviation, int value) {

        double eExponent = -( pow(value - expectedValue, 2) / ( 2 * pow(standardDeviation, 2) ) );

        double expressionFactorOne = ( 1 / ( standardDeviation * sqrt(2*PI)  ) );
        double expressionFactorTwo = Math.pow(E, eExponent);

        return expressionFactorOne * expressionFactorTwo;
    }
}
