package com.aistudy.api.signup.repository;

import com.aistudy.api.signup.model.ApprovalAuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalAuditLogRepository extends JpaRepository<ApprovalAuditLogEntity, String> {}
