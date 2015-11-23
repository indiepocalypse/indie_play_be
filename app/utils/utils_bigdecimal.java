package utils;

import java.math.BigDecimal;

/**
 * Created by skariel on 23/11/15.
 */
public class utils_bigdecimal {
    public static BigDecimal from_percent_or_number(String num_str) {
        if (num_str.endsWith("%")) {
            num_str = num_str.replace("%", "");
        }
        return new BigDecimal(num_str);
    }
}
