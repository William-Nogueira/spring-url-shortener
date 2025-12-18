package williamnogueira.dev.shortener.infra.exception;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<@NonNull Object> handleResponseStatusException(ResponseStatusException ex, WebRequest request) {
        log.error(ex.getMessage(), ex);
        return buildErrorResponse(ex.getMessage(), ex.getStatusCode().value(), ex.getReason(), request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<@NonNull Object> handleNoResourceFoundException(NoResourceFoundException ex, WebRequest request) {
        log.debug("Static resource not found: {}", ex.getResourcePath());
        return buildErrorResponse("Resource not found", HttpStatus.NOT_FOUND.value(), "Not Found", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<@NonNull Object> handleGenericException(Exception ex, WebRequest request) {
        log.error(ex.getMessage(), ex);
        var status = HttpStatus.INTERNAL_SERVER_ERROR;
        return buildErrorResponse(ex.getMessage(), status.value(), status.getReasonPhrase(), request);
    }

    private ResponseEntity<@NonNull Object> buildErrorResponse(String message, Integer code, String reason, WebRequest request) {
        var body = ErrorResponseDto.builder()
                .status(code)
                .error(reason)
                .message(message)
                .path(request.getDescription(false).replace("uri=", ""))
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(body, HttpStatus.valueOf(code));
    }
}
