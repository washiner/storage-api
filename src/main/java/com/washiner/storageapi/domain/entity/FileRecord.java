package com.washiner.storageapi.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
// diz pro JPA que essa classe representa uma tabela no banco
@Table(name = "files")
// lombok gera getters, setters, equals, hashCode e toString
@Data
// lombok gera construtor com todos os campos
@AllArgsConstructor
// lombok gera construtor vazio — o JPA exige isso
@NoArgsConstructor
// lombok permite usar o padrão builder: FileRecord.builder().nome("x").build()
@Builder
public class FileRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // chave primária gerada automaticamente pelo banco
    private Long id;

    @Column(nullable = false)
    // nome original do arquivo que o usuário enviou
    private String originalName;

    @Column(nullable = false)
    // nome único gerado por nós para evitar conflito no MinIO
    private String storedName;

    @Column(nullable = false)
    // tipo do arquivo: image/png, application/pdf etc
    private String contentType;

    @Column(nullable = false)
    // tamanho em bytes
    private Long size;

    @Column(nullable = false)
    // momento exato do upload
    private LocalDateTime uploadedAt;
}