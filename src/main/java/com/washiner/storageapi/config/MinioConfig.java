package com.washiner.storageapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
// diz pro Spring que essa classe tem configurações — beans que ele vai gerenciar
public class MinioConfig {

    @Value("${minio.endpoint}")
    // injeta o valor do application.yaml — não deixamos URL hardcoded no código
    private String endpoint;

    @Value("${minio.access-key}")
    // usuário do MinIO — vem do application.yaml
    private String accessKey;

    @Value("${minio.secret-key}")
    // senha do MinIO — vem do application.yaml
    private String secretKey;

    @Bean
    // @Bean diz pro Spring: "guarda esse objeto e injeta onde precisar"
    public S3Client s3Client() {
        return S3Client.builder()
                // aponta para o MinIO local em vez da AWS real
                .endpointOverride(URI.create(endpoint))
                // credenciais fixas — usuário e senha do MinIO
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey)
                        )
                )
                // região obrigatória pelo SDK — qualquer valor funciona com MinIO
                .region(Region.US_EAST_1)
                // desativa verificação de endpoint da AWS — necessário para MinIO local
                .forcePathStyle(true)
                .build();
    }
}
