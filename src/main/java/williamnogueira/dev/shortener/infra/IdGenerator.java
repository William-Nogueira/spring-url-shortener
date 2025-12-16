package williamnogueira.dev.shortener.infra;

import io.lettuce.core.RedisException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import williamnogueira.dev.shortener.infra.utils.Base62Util;

import java.math.BigInteger;
import java.util.Objects;

import static williamnogueira.dev.shortener.infra.constants.RedisConstants.COUNTER;

@Component
@RequiredArgsConstructor
@Slf4j
public class IdGenerator {

    @Value("${app.salt}")
    private String saltStr;

    private final RedisTemplate<String, Object> redisTemplate;

    private static final BigInteger PRIME = BigInteger.valueOf(1099511628211L);
    private static final BigInteger MODULO = BigInteger.TWO.pow(64);
    private BigInteger salt;

    @PostConstruct
    public void init() {
        this.salt = BigInteger.valueOf(saltStr.hashCode()).abs();
        log.info("IdGenerator initialized with Salt hash: [{}]. Sequence system ready.", this.salt);
    }

    public String nextShortCode() {
        Long seq = redisTemplate.opsForValue().increment(COUNTER, 1L);
        if (Objects.isNull(seq)) {
            throw new RedisException("Redis counter failed");
        }

        var id = BigInteger.valueOf(seq);

        // FORMULA: (ID * PRIME + SALT) % 2^64
        // 1. Multiply checks uniqueness (Bijection)
        // 2. Add Salt shifts the starting point
        // 3. Remainder keeps it fitting in a URL code (wrapping)
        var obfuscatedId = id.multiply(PRIME)
                .add(salt)
                .remainder(MODULO);

        return Base62Util.encode(obfuscatedId);
    }
}
