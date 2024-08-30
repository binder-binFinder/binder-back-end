package net.binder.api.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.binder.api.common.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorResponseUtil {

    public static void sendErrorResponse(HttpServletResponse response, HttpStatus httpStatus, String message,
                                         ObjectMapper objectMapper)
            throws IOException {
        response.setStatus(httpStatus.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        String content = getContent(message, objectMapper);

        response.getWriter()
                .write(content);
    }

    private static String getContent(String message, ObjectMapper objectMapper) throws JsonProcessingException {
        ErrorResponse errorResult = ErrorResponse.from(message);

        return objectMapper.writeValueAsString(errorResult);
    }
}
