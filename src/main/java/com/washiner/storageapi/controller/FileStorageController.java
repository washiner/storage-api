package com.washiner.storageapi.controller;

import com.washiner.storageapi.dto.response.FileRecordResponse;
import com.washiner.storageapi.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
// @RestController = @Controller + @ResponseBody
// diz que essa classe é um controller REST — todas as respostas viram JSON
@RequestMapping("/v1/files")
// todas as rotas dessa classe começam com /v1/files
@RequiredArgsConstructor
public class FileStorageController {

    private final FileStorageService service;

    @PostMapping("/upload")
    // @PostMapping porque estamos enviando dados — criando um recurso novo
    public ResponseEntity<FileRecordResponse> upload(
            @RequestParam("file") MultipartFile file
            // @RequestParam pega o campo "file" do form-data que vem do frontend
            // é diferente de @RequestBody — aqui não é JSON, é multipart
    ) throws IOException {
        FileRecordResponse response = service.upload(file);

        // 201 Created — padrão REST quando um recurso é criado com sucesso
        return ResponseEntity.status(201).body(response);
    }
}
