package williamnogueira.dev.shortener.domain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.util.Objects.isNull;
import static org.springframework.util.CollectionUtils.isEmpty;
import static org.springframework.util.StringUtils.hasText;
import static williamnogueira.dev.shortener.infra.constants.RedisConstants.DIRTY_SET_KEY;
import static williamnogueira.dev.shortener.infra.constants.RedisConstants.getClicksKey;

@Component
@Slf4j
@RequiredArgsConstructor
class UrlSyncScheduler {

    private final StringRedisTemplate redisTemplate;
    private final UrlRepository urlRepository;
    private final SimpleAsyncTaskExecutor applicationTaskExecutor;

    private static final int MIN_BATCH_SIZE = 100;
    private static final int MEDIUM_BATCH_SIZE = 2500;
    private static final int MAX_BATCH_SIZE = 5000;

    private static final int SMALL_QUANTITY = 1000;
    private static final int LARGE_QUANTITY = 50000;

    @Scheduled(cron = "0 */5 * * * *")
    public void persistClicksEvery5mins() {
        log.info("Click sync job started.");

        while (true) {
            var codes = redisTemplate.opsForSet().pop(DIRTY_SET_KEY, dynamicBatchSize());
            if (isEmpty(codes)) {
                break;
            }
            processBatch(codes);
        }

        log.info("Click sync finished");
    }

    private void processBatch(List<String> codes) {
        var futures = codes.stream()
                .map(code -> CompletableFuture.runAsync(() -> processSingleCode(code), applicationTaskExecutor))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();
    }

    private void processSingleCode(String code) {
        String clicksKey = getClicksKey(code);
        String oldValue = redisTemplate.opsForValue().getAndSet(clicksKey, "0");

        if (!hasText(oldValue)) {
            return;
        }

        long clicks = Long.parseLong(oldValue);
        if (clicks > 0) {
            try {
                urlRepository.updateClickCount(code, clicks);
            } catch (Exception e) {
                redisTemplate.opsForValue().increment(clicksKey, clicks);
                redisTemplate.opsForSet().add(DIRTY_SET_KEY, code);
                log.error("Failed to sync clicks for {}, returned {} clicks", code, clicks, e);
            }
        }
    }

    private int dynamicBatchSize() {
        var size = redisTemplate.opsForSet().size(DIRTY_SET_KEY);
        if (isNull(size) || size < SMALL_QUANTITY) {
            return MIN_BATCH_SIZE;
        }

        if (size < LARGE_QUANTITY) {
            return MEDIUM_BATCH_SIZE;
        }

        return MAX_BATCH_SIZE;
    }
}
