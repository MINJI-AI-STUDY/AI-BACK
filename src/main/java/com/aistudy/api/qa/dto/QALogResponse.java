package com.aistudy.api.qa.dto;

import com.aistudy.api.qa.model.QALog;
import java.time.LocalDateTime;
import java.util.List;

public record QALogResponse(
	String qaLogId,
	String materialId,
	String studentId,
	String question,
	String answer,
	boolean grounded,
	String status,
	LocalDateTime createdAt,
	List<String> evidenceSnippets
) {
	public static QALogResponse from(QALog log) {
		return new QALogResponse(
			log.getId(),
			log.getMaterialId(),
			log.getStudentId(),
			log.getQuestion(),
			log.getAnswer(),
			log.isGrounded(),
			log.getStatus(),
			log.getCreatedAt(),
			log.getEvidenceSnippets()
		);
	}
}
