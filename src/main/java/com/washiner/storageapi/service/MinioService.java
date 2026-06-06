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
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class MinioService {

    private final S3Client s3Client;
    // injeta o S3Presigner criado no MinioConfig
    private final S3Presigner s3Presigner;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    public void createBucketIfNotExists() {
        try {
            s3Client.headBucket(HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build());
        } catch (NoSuchBucketException e) {
            s3Client.createBucket(CreateBucketRequest.builder()
                    .bucket(bucketName)
                    .build());
        }
    }

    public String uploadFile(String storedName, MultipartFile file) throws IOException {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(storedName)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(request,
                RequestBody.fromBytes(file.getBytes()));

        return "http://localhost:9000/" + bucketName + "/" + storedName;
    }

    public String generatePresignedUrl(String storedName) {
        try {
            // data e hora no formato que o MinIO espera
            java.time.ZonedDateTime now = java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC);
            String dateTime = now.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'"));
            String date = dateTime.substring(0, 8);

            // dados fixos da requisição
            String method = "GET";
            String region = "us-east-1";
            String service = "s3";
            String host = "localhost:9000";
            String path = "/" + bucketName + "/" + storedName;
            long expires = 1800; // 30 minutos em segundos
            String credential = accessKey + "/" + date + "/" + region + "/" + service + "/aws4_request";

            // query string com os parâmetros de assinatura
            String queryString = "X-Amz-Algorithm=AWS4-HMAC-SHA256"
                    + "&X-Amz-Credential=" + java.net.URLEncoder.encode(credential, "UTF-8")
                    + "&X-Amz-Date=" + dateTime
                    + "&X-Amz-Expires=" + expires
                    + "&X-Amz-SignedHeaders=host";

            // canonical request — o que vai ser assinado
            String canonicalRequest = method + "\n"
                    + path + "\n"
                    + queryString + "\n"
                    + "host:" + host + "\n"
                    + "\n"
                    + "host" + "\n"
                    + "UNSIGNED-PAYLOAD";

            // string to sign
            String credentialScope = date + "/" + region + "/" + service + "/aws4_request";
            String stringToSign = "AWS4-HMAC-SHA256" + "\n"
                    + dateTime + "\n"
                    + credentialScope + "\n"
                    + sha256Hex(canonicalRequest);

            // calcula a assinatura HMAC-SHA256
            byte[] signingKey = getSigningKey(secretKey, date, region, service);
            String signature = toHex(hmacSha256(signingKey, stringToSign));

            // monta a URL final
            return "http://" + host + path + "?" + queryString + "&X-Amz-Signature=" + signature;

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar presigned URL", e);
        }
    }

    // gera a chave de assinatura derivada
    private byte[] getSigningKey(String secret, String date, String region, String service) throws Exception {
        byte[] kSecret = ("AWS4" + secret).getBytes("UTF-8");
        byte[] kDate = hmacSha256(kSecret, date);
        byte[] kRegion = hmacSha256(kDate, region);
        byte[] kService = hmacSha256(kRegion, service);
        return hmacSha256(kService, "aws4_request");
    }

    // calcula HMAC-SHA256
    private byte[] hmacSha256(byte[] key, String data) throws Exception {
        javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
        mac.init(new javax.crypto.spec.SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(data.getBytes("UTF-8"));
    }

    // converte bytes para hex
    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    // calcula SHA-256 de uma string e retorna em hex
    private String sha256Hex(String data) throws Exception {
        java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(data.getBytes("UTF-8"));
        return toHex(hash);
    }
}