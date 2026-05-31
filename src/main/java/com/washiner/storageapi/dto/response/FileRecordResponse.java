package com.washiner.storageapi.dto.response;

import java.time.LocalDateTime;

// Record é imutável — perfeito para response, ninguém precisa mudar o que já foi respondido
public record FileRecordResponse(
        Long id,
        String originalName,
        String contentType,
        Long size,
        LocalDateTime uploadedAt
) {}