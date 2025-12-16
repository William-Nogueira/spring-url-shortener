package williamnogueira.dev.shortener.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;
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

    @GetMapping("/{code}")
    public RedirectView redirect(@PathVariable @NotBlank String code) {
        var originalUrl = urlService.getOriginalUrl(code);
        urlService.incrementClickCount(code);

        return new RedirectView(originalUrl);
    }

    @Operation(summary = "Get URL Metadata", description = "Retrieve click counts and original URL without redirecting.")
    @GetMapping("/api/data/{code}")
    public ResponseEntity<@NonNull UrlDto> metadata(@PathVariable @NotBlank String code) {
        var urlDto = urlService.getMetadata(code);
        return ResponseEntity.ok(urlDto);
    }

}
