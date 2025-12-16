package williamnogueira.dev.shortener.domain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import williamnogueira.dev.shortener.infra.IdGenerator;
import williamnogueira.dev.shortener.infra.utils.EntityMapper;

import java.time.Duration;
import java.time.Instant;

import static williamnogueira.dev.shortener.infra.constants.RedisConstants.getClicksKey;
import static williamnogueira.dev.shortener.infra.constants.RedisConstants.getUrlCacheKey;
import static williamnogueira.dev.shortener.infra.utils.UrlSanitizer.sanitizeUrl;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrlService {

    private final UrlRepository urlRepository;
    private final IdGenerator idGenerator;
    private final RedisTemplate<String, Object> redisTemplate;
    private final EntityMapper mapper;

    private static final Duration CACHE_TTL = Duration.ofHours(12);

    public UrlDto create(String originalUrl) {
        log.info("Attempting to shorten URL: {}", originalUrl);

        String finalUrl = sanitizeUrl(originalUrl);
        String code = idGenerator.nextShortCode();

        var shortUrlEntity = UrlEntity.builder()
                .code(code)
                .originalUrl(finalUrl)
                .clicks(0L)
                .createdAt(Instant.now())
                .build();

        urlRepository.save(shortUrlEntity);

        redisTemplate.opsForValue().set(getClicksKey(code), "0");
        redisTemplate.opsForValue().set(getUrlCacheKey(code), finalUrl, CACHE_TTL);

        log.info("URL shortened successfully. Code: [{}], Original: [{}]", code, finalUrl);

        return mapper.toDto(shortUrlEntity);
    }

}
