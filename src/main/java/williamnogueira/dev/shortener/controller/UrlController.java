package williamnogueira.dev.shortener.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;
import williamnogueira.dev.shortener.domain.UrlDto;
import williamnogueira.dev.shortener.domain.UrlService;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@Validated
@Tag(name = "URL Management", description = "Endpoints for creating and retrieving short URLs")
public class UrlController {

    private final UrlService urlService;

    @Operation(
            summary = "Shorten a URL",
            description = "Accepts a long URL and returns a unique short code.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "URL created successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid URL format provided")
            }
    )
    @PostMapping("/api/shorten")
    public ResponseEntity<@NonNull UrlDto> create(@RequestParam
                                                  @NotBlank
                                                  @Parameter(description = "The absolute URL to shorten.", example = "https://www.google.com")
                                                  String url) {
        var urlDto = urlService.create(url);
        var location = URI.create(urlDto.shortUrl());

        return ResponseEntity.created(location).body(urlDto);
    }

    @Operation(
            summary = "Redirect to Original URL",
            description = "Finds the original URL and performs a 302 redirect. Increments click count async.",
            responses = {
                    @ApiResponse(responseCode = "302", description = "Redirects to the original URL"),
                    @ApiResponse(responseCode = "404", description = "Short code not found")
            }
    )
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
