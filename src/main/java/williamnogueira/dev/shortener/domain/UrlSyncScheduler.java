package williamnogueira.dev.shortener.domain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static williamnogueira.dev.shortener.infra.constants.RedisConstants.DIRTY_SET_KEY;
import static williamnogueira.dev.shortener.infra.constants.RedisConstants.getClicksKey;

@Component
@RequiredArgsConstructor
@Slf4j
class UrlSyncScheduler {

    private final RedisTemplate<String, Object> redisTemplate;
    private final UrlRepository urlRepository;
    private final ExecutorService batchExecutor = Executors.newVirtualThreadPerTaskExecutor();

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
            if (Objects.isNull(codes) || codes.isEmpty()) {
                break;
            }
            processBatch(codes);
        }

        log.info("Click sync finished");
    }

    private void processBatch(List<Object> codes) {
        var tasks = codes.stream().map(obj -> (Callable<Void>) () -> {
            String code = obj.toString();
            String clicksKey = getClicksKey(code);

            var oldValObj = redisTemplate.opsForValue().getAndSet(clicksKey, "0");

            if (Objects.nonNull(oldValObj)) {
                long clicks = Long.parseLong(oldValObj.toString());

                if (clicks > 0) {
                    try {
                        urlRepository.updateClickCount(code, clicks);
                    } catch (Exception e) {
                        redisTemplate.opsForValue().increment(clicksKey, clicks);
                        redisTemplate.opsForSet().add(DIRTY_SET_KEY, code);
                        log.error("Failed to sync clicks for {}, returned {} clicks to cache", code, clicks, e);
                    }
                }
            }
            return null;
        }).toList();

        try {
            batchExecutor.invokeAll(tasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Batch interrupted", e);
        }
    }

    private int dynamicBatchSize() {
        var size = redisTemplate.opsForSet().size(DIRTY_SET_KEY);
        if (Objects.isNull(size) || size < SMALL_QUANTITY) {
            return MIN_BATCH_SIZE;
        }

        if (size < LARGE_QUANTITY) {
            return MEDIUM_BATCH_SIZE;
        }

        return MAX_BATCH_SIZE;
    }
}
