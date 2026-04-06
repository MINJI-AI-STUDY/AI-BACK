package com.aistudy.api.qa.service;

import com.aistudy.api.common.integration.AiIntegrationService;
import com.aistudy.api.material.model.Material;
import com.aistudy.api.material.service.MaterialService;
import com.aistudy.api.qa.dto.QaResponse;
import com.aistudy.api.qa.model.QALog;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class QaService {
	private final Map<String, QALog> logs = new ConcurrentHashMap<>();
	private final MaterialService materialService;
	private final AiIntegrationService aiIntegrationService;

	public QaService(MaterialService materialService, AiIntegrationService aiIntegrationService) {
		this.materialService = materialService;
		this.aiIntegrationService = aiIntegrationService;
	}

	/** 자료 기반 질문을 AI에 전달하고 로그를 저장합니다. */
	public QaResponse ask(String studentId, String materialId, String question) {
		Material material = materialService.getById(materialId);
		QaResponse response = aiIntegrationService.ask(material.getTitle() + "\n" + material.getExtractedText() + "\n질문: " + question);
		String status = response.insufficientEvidence() ? "INSUFFICIENT_EVIDENCE" : "SUCCESS";
		if (!response.grounded()) {
			response = new QaResponse(
				material.getTitle() + " 자료 기준 기본 답변입니다: " + question,
				List.of(material.getExtractedText()),
				true,
				false
			);
			status = "FALLBACK";
		}
		QALog log = new QALog(
			"qa-" + UUID.randomUUID(),
			materialId,
			studentId,
			question,
			response.answer(),
			response.grounded(),
			status,
			LocalDateTime.now()
		);
		logs.put(log.getId(), log);
		return response;
	}
}
