package net.binder.api.common.handler;

import java.util.Objects;
import net.binder.api.common.dto.ErrorResponse;
import net.binder.api.common.exception.BizException;
import net.binder.api.common.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {

        String message = Objects.requireNonNull(e.getFieldError()).getDefaultMessage();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.from(message));
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleBizException(BizException e) {
        String message = e.getMessage();

        if (e instanceof NotFoundException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.from(message));
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.from(message));
    }
}
