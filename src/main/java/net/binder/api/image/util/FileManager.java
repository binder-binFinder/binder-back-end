package net.binder.api.image.util;

import java.io.IOException;
import java.util.UUID;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileManager {

    private final Tika tika = new Tika();

    public boolean isImage(MultipartFile file) {
        try {
            String mimeType = getMimeType(file);

            return mimeType.startsWith("image/");
        } catch (IOException e) {
            throw new RuntimeException("파일 형식 검증 중 오류가 발생하였습니다.", e);
        }
    }

    public String toStoreName(String originalName) {
        String uuid = UUID.randomUUID().toString();
        String extension = extractExtension(originalName);
        return uuid + "." + extension;
    }

    private String extractExtension(String originalName) {
        int pos = originalName.lastIndexOf(".");
        return originalName.substring(pos + 1);
    }

    private String getMimeType(MultipartFile file) throws IOException {
        return tika.detect(file.getInputStream());
    }

}
