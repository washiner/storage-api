package com.washiner.storageapi.service;

import com.washiner.storageapi.domain.entity.FileRecord;
import com.washiner.storageapi.dto.response.FileRecordResponse;
import com.washiner.storageapi.repository.FileRecordRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final FileRecordRepository repository;
    private final MinioService minioService;

    @PostConstruct
    public void init() {
        minioService.createBucketIfNotExists();
    }

    public FileRecordResponse upload(MultipartFile file) throws IOException {

        String storedName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String fileUrl = minioService.uploadFile(storedName, file);

        FileRecord record = FileRecord.builder()
                .originalName(file.getOriginalFilename())
                .storedName(storedName)
                .contentType(file.getContentType())
                .size(file.getSize())
                .uploadedAt(LocalDateTime.now())
                .fileUrl(fileUrl)
                .build();

        FileRecord saved = repository.save(record);

        return new FileRecordResponse(
                saved.getId(),
                saved.getOriginalName(),
                saved.getContentType(),
                saved.getSize(),
                saved.getUploadedAt(),
                saved.getFileUrl()
        );
    }

    // busca o arquivo no banco pelo id e gera um link temporário de 30 minutos
    public String getFileLink(Long id) {
        // busca o registro no banco — lança exceção se não encontrar
        FileRecord record = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Arquivo não encontrado: " + id));

        // pega o storedName — é o nome do arquivo dentro do MinIO
        // e pede pro MinioService gerar a URL assinada
        return minioService.generatePresignedUrl(record.getStoredName());
    }
}