package williamnogueira.dev.shortener.controller;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import williamnogueira.dev.shortener.domain.UrlDto;
import williamnogueira.dev.shortener.domain.UrlService;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class UrlController {

    private final UrlService urlService;

    @PostMapping("/api/shorten")
    public ResponseEntity<@NonNull UrlDto> create(String url) {
        var urlDto = urlService.create(url);
        var location = URI.create(urlDto.shortUrl());

        return ResponseEntity.created(location).body(urlDto);
    }

}
