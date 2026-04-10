package com.aistudy.api.qa.service;

import com.aistudy.api.common.integration.AiIntegrationService;
import com.aistudy.api.material.model.Material;
import com.aistudy.api.material.service.MaterialService;
import com.aistudy.api.qa.dto.QaResponse;
import com.aistudy.api.qa.model.QALog;
import com.aistudy.api.qa.repository.QALogRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QaService {
	private final QALogRepository qaLogRepository;
	private final MaterialService materialService;
	private final AiIntegrationService aiIntegrationService;

	public QaService(QALogRepository qaLogRepository, MaterialService materialService, AiIntegrationService aiIntegrationService) {
		this.qaLogRepository = qaLogRepository;
		this.materialService = materialService;
		this.aiIntegrationService = aiIntegrationService;
	}

	@Transactional
	public QaResponse ask(String studentId, String schoolId, String materialId, String question) {
		Material material = materialService.getSchoolMaterial(schoolId, materialId);
		QaResponse response = aiIntegrationService.ask(material.getExtractedText(), question);
		String status = response.insufficientEvidence() ? "INSUFFICIENT_EVIDENCE" : "SUCCESS";
		qaLogRepository.save(new QALog(
			UUID.randomUUID().toString(),
			schoolId,
			materialId,
			studentId,
			question,
			response.answer(),
			response.grounded(),
			status,
			LocalDateTime.now(),
			response.evidenceSnippets()
		));
		return response;
	}

	@Transactional(readOnly = true)
	public List<QALog> getStudentLogs(String schoolId, String materialId, String studentId) {
		materialService.getSchoolMaterial(schoolId, materialId);
		return qaLogRepository.findByMaterialIdAndStudentIdOrderByCreatedAtDesc(materialId, studentId);
	}

	@Transactional(readOnly = true)
	public List<QALog> getTeacherLogs(String schoolId, String materialId) {
		materialService.getSchoolMaterial(schoolId, materialId);
		return qaLogRepository.findByMaterialIdAndSchoolIdOrderByCreatedAtDesc(materialId, schoolId);
	}

	@Transactional(readOnly = true)
	public long countByMaterial(String schoolId, String materialId) {
		return qaLogRepository.countByMaterialIdAndSchoolId(materialId, schoolId);
	}
}
