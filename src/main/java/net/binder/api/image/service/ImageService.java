package net.binder.api.image.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.binder.api.common.exception.BadRequestException;
import net.binder.api.image.util.FileManager;
import net.binder.api.image.util.S3Manager;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Service
@Slf4j
public class ImageService {

    private final S3Manager s3Manager;

    private final FileManager fileManager;

    public String uploadImage(MultipartFile file) {
        validateFileExists(file);
        validateFileFormat(file);

        String originalName = file.getOriginalFilename();
        String storeName = fileManager.toStoreName(originalName);

        return s3Manager.upload(file, storeName);
    }

    private void validateFileExists(MultipartFile multipartFile) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new BadRequestException("파일이 존재하지 않습니다.");
        }
    }

    private void validateFileFormat(MultipartFile multipartFile) {
        if (!fileManager.isImage(multipartFile)) {
            throw new BadRequestException("지원하지 않는 파일 형식입니다.");
        }
    }
}
