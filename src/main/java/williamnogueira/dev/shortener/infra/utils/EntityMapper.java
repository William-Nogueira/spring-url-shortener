package williamnogueira.dev.shortener.infra.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import williamnogueira.dev.shortener.domain.UrlDto;
import williamnogueira.dev.shortener.domain.UrlEntity;

@Component
public class EntityMapper {

    @Value("${app.base-url}")
    private String baseUrl;

    public UrlDto toDto(UrlEntity entity) {
        return UrlDto.builder()
                .shortUrl(baseUrl + entity.getCode())
                .originalUrl(entity.getOriginalUrl())
                .clicks(entity.getClicks())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
