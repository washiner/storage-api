package com.washiner.storageapi.repository;

import com.washiner.storageapi.domain.entity.FileRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// @Repository marca essa interface como componente do Spring
// JpaRepository já traz save, findById, findAll, delete — sem escrever nada
@Repository
public interface FileRecordRepository extends JpaRepository<FileRecord, Long> {
}
