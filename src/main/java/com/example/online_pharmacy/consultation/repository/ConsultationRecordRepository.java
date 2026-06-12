package com.example.online_pharmacy.consultation.repository;

import com.example.online_pharmacy.consultation.entity.ConsultationRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsultationRecordRepository extends JpaRepository<ConsultationRecord, Long> {
}
