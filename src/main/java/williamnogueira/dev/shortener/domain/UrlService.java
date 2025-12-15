package williamnogueira.dev.shortener.domain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import williamnogueira.dev.shortener.infra.utils.EntityMapper;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrlService {

    private final UrlRepository urlRepository;
    private final EntityMapper mapper;

    public UrlDto create(String url) {

        // TODO: ID Generation logic

        var shortUrlEntity = UrlEntity.builder()
                .code("1") // change this
                .originalUrl(url)
                .clicks(0L)
                .createdAt(Instant.now())
                .build();

        urlRepository.save(shortUrlEntity);

        return mapper.toDto(shortUrlEntity);
    }

}
