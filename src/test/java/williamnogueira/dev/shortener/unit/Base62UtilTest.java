package williamnogueira.dev.shortener.unit;

import org.junit.jupiter.api.Test;
import williamnogueira.dev.shortener.infra.utils.Base62Util;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;

class Base62UtilTest {

    @Test
    void encodesZeroCorrectly() {
        assertThat(Base62Util.encode(BigInteger.ZERO)).isEqualTo("0");
    }

    @Test
    void encodesPositiveNumbers() {
        assertThat(Base62Util.encode(BigInteger.valueOf(62))).isEqualTo("10");
        assertThat(Base62Util.encode(BigInteger.valueOf(12345))).isNotBlank();
    }

    @Test
    void encodesUniqueValues() {
        String a = Base62Util.encode(BigInteger.valueOf(1));
        String b = Base62Util.encode(BigInteger.valueOf(2));

        assertThat(a).isNotEqualTo(b);
    }
}
