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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QaService {
	private static final Logger log = LoggerFactory.getLogger(QaService.class);

	private final QALogRepository qaLogRepository;
	private final MaterialService materialService;
	private final AiIntegrationService aiIntegrationService;

	public QaService(QALogRepository qaLogRepository, MaterialService materialService, AiIntegrationService aiIntegrationService) {
		this.qaLogRepository = qaLogRepository;
		this.materialService = materialService;
		this.aiIntegrationService = aiIntegrationService;
	}

	/** 학생 질문 — schoolId로 자료 소속 검증 후 QA 로그 저장 */
	@Transactional
	public QaResponse ask(String studentId, String schoolId, String materialId, String question) {
		Material material = materialService.getSchoolMaterial(schoolId, materialId);
		QaResponse response = aiIntegrationService.ask(material.getExtractedText(), question);
		String status;
		if (!response.grounded() && !response.insufficientEvidence()) {
			status = "AI_UNAVAILABLE";
			log.warn("QA 요청 처리 실패 — AI 서버 연결 불가. schoolId={}, materialId={}, studentId={}, question={}", schoolId, materialId, studentId, question);
		} else if (response.insufficientEvidence()) {
			status = "INSUFFICIENT_EVIDENCE";
			log.info("QA 근거 부족 응답. schoolId={}, materialId={}, studentId={}", schoolId, materialId, studentId);
		} else {
			status = "SUCCESS";
			log.info("QA 성공 응답. schoolId={}, materialId={}, studentId={}", schoolId, materialId, studentId);
		}
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

	/** 학생 QA 로그 — schoolId로 자료 소속 검증 후 반환 */
	@Transactional(readOnly = true)
	public List<QALog> getStudentLogs(String schoolId, String materialId, String studentId) {
		materialService.getSchoolMaterial(schoolId, materialId);
		return qaLogRepository.findByMaterialIdAndStudentIdOrderByCreatedAtDesc(materialId, studentId);
	}

	/** 교사 QA 로그 — schoolId로 자료 소속 검증 후 반환 */
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
