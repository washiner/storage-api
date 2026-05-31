package com.washiner.storageapi.service;

import com.washiner.storageapi.domain.entity.FileRecord;
import com.washiner.storageapi.dto.response.FileRecordResponse;
import com.washiner.storageapi.repository.FileRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
// lombok gera construtor com todos os campos final — é a injeção de dependência
@RequiredArgsConstructor
public class FileStorageService {

    private final FileRecordRepository repository;

    public FileRecordResponse upload(MultipartFile file) {

        // gera um nome único para o arquivo — evita conflito se dois usuários
        // mandarem um arquivo com o mesmo nome, ex: "foto.png"
        String storedName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        // monta a entidade com os dados do arquivo recebido
        FileRecord record = FileRecord.builder()
                .originalName(file.getOriginalFilename())
                .storedName(storedName)
                .contentType(file.getContentType())
                .size(file.getSize())
                .uploadedAt(LocalDateTime.now())
                .build();

        // salva no banco e guarda o resultado — o banco preenche o id
        FileRecord saved = repository.save(record);

        // monta e retorna o response com os dados salvos
        return new FileRecordResponse(
                saved.getId(),
                saved.getOriginalName(),
                saved.getContentType(),
                saved.getSize(),
                saved.getUploadedAt()
        );
    }
}