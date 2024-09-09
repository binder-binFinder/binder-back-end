package net.binder.api.common.handler;

import com.fasterxml.jackson.databind.JsonMappingException;
import java.util.Objects;
import net.binder.api.common.dto.ErrorResponse;
import net.binder.api.common.exception.BizException;
import net.binder.api.common.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {

        String defaultMessage = Objects.requireNonNull(e.getFieldError()).getDefaultMessage();
        String message = e.getFieldError().getField() + "은(는) " + defaultMessage;

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.from(message));
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleBizException(BizException e) {
        HttpStatus httpStatus = null;

        if (e instanceof NotFoundException) {
            httpStatus = HttpStatus.NOT_FOUND;
        }
        if (httpStatus == null) {
            httpStatus = HttpStatus.BAD_REQUEST;
        }

        return ResponseEntity.status(httpStatus)
                .body(ErrorResponse.from(e.getMessage()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.from("존재하지 않는 페이지입니다."));
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        String message = "유효하지 않은 입력 값이 존재합니다.";

        if (e.getCause() instanceof JsonMappingException jme) {

            if (!jme.getPath().isEmpty()) {
                String field = jme.getPath().get(0).getFieldName();
                message = String.format("%s 에 유효하지 않은 값이 입력되었습니다.", field);
            }
        }

        ErrorResponse errorResponse = new ErrorResponse(message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);

    }
}
