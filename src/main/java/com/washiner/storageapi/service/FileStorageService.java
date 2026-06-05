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
    // injeta o MinioService — agora o arquivo vai para o MinIO de verdade
    private final MinioService minioService;

    @PostConstruct
    // @PostConstruct é executado automaticamente depois que o Spring
    // termina de criar e injetar todas as dependências dessa classe
    // garante que o bucket existe antes do primeiro upload
    public void init() {
        minioService.createBucketIfNotExists();
    }

    public FileRecordResponse upload(MultipartFile file) throws IOException {

        // gera nome único para evitar conflito no MinIO
        String storedName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        // envia o arquivo para o MinIO e recebe a URL de volta
        String fileUrl = minioService.uploadFile(storedName, file);

        // monta a entidade com os metadados + URL do MinIO
        FileRecord record = FileRecord.builder()
                .originalName(file.getOriginalFilename())
                .storedName(storedName)
                .contentType(file.getContentType())
                .size(file.getSize())
                .uploadedAt(LocalDateTime.now())
                // agora salva a URL real do arquivo no MinIO
                .fileUrl(fileUrl)
                .build();

        FileRecord saved = repository.save(record);

        return new FileRecordResponse(
                saved.getId(),
                saved.getOriginalName(),
                saved.getContentType(),
                saved.getSize(),
                saved.getUploadedAt(),
                // inclui a URL no response para o frontend saber onde está o arquivo
                saved.getFileUrl()
        );
    }
}