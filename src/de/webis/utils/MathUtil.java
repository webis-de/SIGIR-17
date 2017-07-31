package de.webis.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Class for basic mathematical operations like rounding.
 */
public class MathUtil {
    public static double roundDouble(double value){
        return roundDouble(value, 3);
    }

    public static double roundDouble(double value, int places){
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);

        return bd.doubleValue();
    }
}
