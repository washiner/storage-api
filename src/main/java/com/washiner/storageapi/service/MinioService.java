package com.washiner.storageapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class MinioService {

    private final S3Client s3Client;

    @Value("${minio.bucket-name}")
    // pega o nome do bucket do application.yaml
    private String bucketName;

    // esse método é chamado quando a aplicação sobe
    // garante que o bucket existe antes de qualquer upload
    public void createBucketIfNotExists() {
        try {
            // tenta verificar se o bucket já existe
            s3Client.headBucket(HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build());
        } catch (NoSuchBucketException e) {
            // bucket não existe — cria agora
            s3Client.createBucket(CreateBucketRequest.builder()
                    .bucket(bucketName)
                    .build());
        }
    }

    // envia o arquivo para o MinIO e retorna a URL pública
    public String uploadFile(String storedName, MultipartFile file) throws IOException {
        // monta as informações do objeto que vai ser criado no bucket
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                // storedName é o nome único gerado com UUID
                .key(storedName)
                // informa o tipo do arquivo para o MinIO
                .contentType(file.getContentType())
                .build();

        // envia os bytes do arquivo para o MinIO
        s3Client.putObject(request,
                RequestBody.fromBytes(file.getBytes()));

        // monta e retorna a URL pública do arquivo no MinIO
        // formato: http://localhost:9000/nome-do-bucket/nome-do-arquivo
        return "http://localhost:9000/" + bucketName + "/" + storedName;
    }
}