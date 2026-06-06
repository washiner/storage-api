package com.washiner.storageapi.controller;

import com.washiner.storageapi.dto.response.FileRecordResponse;
import com.washiner.storageapi.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/v1/files")
@RequiredArgsConstructor
public class FileStorageController {

    private final FileStorageService service;

    @PostMapping("/upload")
    public ResponseEntity<FileRecordResponse> upload(
            @RequestParam("file") MultipartFile file
    ) throws Exception {
        FileRecordResponse response = service.upload(file);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/{id}/link")
    // @GetMapping porque estamos buscando um dado — não criando nada
    // {id} é o id do arquivo no banco — vem na URL
    public ResponseEntity<String> getFileLink(
            @PathVariable Long id
            // @PathVariable pega o {id} da URL e injeta aqui
    ) {
        String url = service.getFileLink(id);
        // retorna 200 com a URL temporária no body
        return ResponseEntity.ok(url);
    }
}