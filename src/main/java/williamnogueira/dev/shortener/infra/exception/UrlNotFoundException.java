package williamnogueira.dev.shortener.infra.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class UrlNotFoundException extends ResponseStatusException {
    public UrlNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
