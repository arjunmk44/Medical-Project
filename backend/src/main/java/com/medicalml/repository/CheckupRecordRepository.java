package com.medicalml.repository;

import com.medicalml.entity.CheckupRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface CheckupRecordRepository extends JpaRepository<CheckupRecord, UUID> {
    List<CheckupRecord> findByPatientIdOrderByRecordDateDesc(UUID patientId);
    List<CheckupRecord> findByStatus(String status);
}
