package com.washiner.storageapi.dto.response;

import java.time.LocalDateTime;

public record FileRecordResponse(
        Long id,
        String originalName,
        String contentType,
        Long size,
        LocalDateTime uploadedAt,
        // URL do arquivo guardado no MinIO
        String fileUrl
) {}