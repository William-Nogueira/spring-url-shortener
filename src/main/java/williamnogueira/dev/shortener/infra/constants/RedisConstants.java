package williamnogueira.dev.shortener.infra.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class RedisConstants {

    public static final String DIRTY_SET_KEY = "sync:dirty_codes";
    public static final String COUNTER = "counter:global";
    private static final String CLICKS_KEY_PREFIX = "clicks:";
    private static final String URL = "url:";

    public static String getClicksKey(String code) {
        return CLICKS_KEY_PREFIX + code;
    }

    public static String getUrlCacheKey(String code) {
        return URL + code;
    }
}
