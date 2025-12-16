package williamnogueira.dev.shortener.domain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import williamnogueira.dev.shortener.infra.IdGenerator;
import williamnogueira.dev.shortener.infra.exception.UrlNotFoundException;
import williamnogueira.dev.shortener.infra.utils.EntityMapper;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

import static williamnogueira.dev.shortener.infra.constants.RedisConstants.DIRTY_SET_KEY;
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

    public String getOriginalUrl(String code) {
        String cacheKey = getUrlCacheKey(code);

        var cachedUrl = redisTemplate.opsForValue().get(getUrlCacheKey(code));
        if (Objects.nonNull(cachedUrl)) {
            log.info("Cache HIT for code: [{}]", code);
            return cachedUrl.toString();
        }

        log.info("Cache MISS for code: [{}]. Fetching from DB.", code);

        var urlEntity = findShortUrl(code);
        String originalUrl = urlEntity.getOriginalUrl();

        redisTemplate.opsForValue().set(cacheKey, originalUrl, CACHE_TTL);

        return originalUrl;
    }

    public UrlDto getMetadata(String code) {
        var shortUrlEntity = findShortUrl(code);
        var clicksObj = Objects.requireNonNullElse(redisTemplate.opsForValue().get(getClicksKey(code)), 0L);

        shortUrlEntity.setClicks(shortUrlEntity.getClicks() + Long.parseLong(clicksObj.toString()));

        return mapper.toDto(shortUrlEntity);
    }

    @Async("clickExecutor")
    public void incrementClickCount(String code) {
        redisTemplate.opsForValue().increment(getClicksKey(code), 1);
        redisTemplate.opsForSet().add(DIRTY_SET_KEY, code);
        redisTemplate.expire(DIRTY_SET_KEY, Duration.ofHours(24));
    }

    private UrlEntity findShortUrl(String code) {
        return urlRepository.findById(code)
                .orElseThrow(() -> new UrlNotFoundException("URL not found for code: " + code));
    }

}
