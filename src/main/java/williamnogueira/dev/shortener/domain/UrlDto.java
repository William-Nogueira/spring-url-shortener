package williamnogueira.dev.shortener.domain;

import lombok.Builder;

import java.time.Instant;

@Builder
public record UrlDto(
        String shortUrl,
        String originalUrl,
        Long clicks,
        Instant createdAt
) { }
