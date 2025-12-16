package williamnogueira.dev.shortener.infra.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class InvalidUrlException extends ResponseStatusException {
    public InvalidUrlException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
