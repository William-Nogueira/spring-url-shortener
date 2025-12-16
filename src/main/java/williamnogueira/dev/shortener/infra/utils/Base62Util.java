package williamnogueira.dev.shortener.infra.utils;

import lombok.experimental.UtilityClass;

import java.math.BigInteger;

@UtilityClass
public class Base62Util {

    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final BigInteger BASE = BigInteger.valueOf(62);

    public static String encode(BigInteger value) {
        if (value.equals(BigInteger.ZERO)) {
            return "0";
        }

        var sb = new StringBuilder();

        while (value.compareTo(BigInteger.ZERO) > 0) {
            BigInteger[] result = value.divideAndRemainder(BASE);
            value = result[0]; // Quotient (for next loop)
            int remainder = result[1].intValue(); // Remainder (for char)

            sb.append(ALPHABET.charAt(remainder));
        }

        return sb.reverse().toString();
    }
}
