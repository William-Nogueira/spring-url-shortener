package williamnogueira.dev.shortener.infra.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import williamnogueira.dev.shortener.domain.UrlDto;
import williamnogueira.dev.shortener.domain.UrlEntity;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EntityMapper {

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
