package com.aistudy.api.common.integration;

import com.aistudy.api.qa.dto.QaResponse;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class AiIntegrationService {

	private final RestClient restClient;

	public AiIntegrationService(@Value("${app.ai.base-url}") String aiBaseUrl) {
		this.restClient = RestClient.builder().baseUrl(aiBaseUrl).build();
	}

	/** AI 서버에 자료 추출을 요청합니다. */
	public String extractMaterial(String title, String description, String filename) {
		try {
			Map<String, Object> response = restClient.post()
				.uri("/extract-material")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.body(Map.of("title", title, "description", description, "filename", filename))
				.retrieve()
				.body(Map.class);
			return String.valueOf(response == null ? "" : response.getOrDefault("extractedText", ""));
		} catch (Exception exception) {
			return "AI 추출 실패로 기본 추출 텍스트를 사용합니다.";
		}
	}

	/** AI 서버에 질문을 전달하고 응답을 반환합니다. */
	public QaResponse ask(String question) {
		try {
			Map<String, Object> response = restClient.post()
				.uri("/qa")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.body(Map.of("question", question))
				.retrieve()
				.body(Map.class);
			if (response == null) {
				return new QaResponse("AI 응답이 비어 있습니다.", List.of(), false, true);
			}
			return new QaResponse(
				String.valueOf(response.getOrDefault("answer", "AI 응답이 비어 있습니다.")),
				(response.get("evidenceSnippets") instanceof List<?> snippets) ? snippets.stream().map(String::valueOf).toList() : List.of(),
				Boolean.TRUE.equals(response.get("grounded")),
				Boolean.TRUE.equals(response.get("insufficientEvidence"))
			);
		} catch (Exception exception) {
			return new QaResponse("AI 서버 연결에 실패했습니다. 잠시 후 다시 시도해주세요.", List.of(), false, true);
		}
	}
}
