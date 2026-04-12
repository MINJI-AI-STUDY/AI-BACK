package com.aistudy.api.common.integration;

import com.aistudy.api.qa.dto.QaResponse;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClient;
@Service
public class AiIntegrationService {
	private static final Logger log = LoggerFactory.getLogger(AiIntegrationService.class);
	private static final Pattern CONTROL_CHAR_PATTERN = Pattern.compile("[\\u0000-\\u0008\\u000b\\u000c\\u000e-\\u001f]");

	private final RestClient restClient;
	public AiIntegrationService(@Value("${app.ai.base-url}") String aiBaseUrl) {
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(10000);
		requestFactory.setReadTimeout(120000);
		this.restClient = RestClient.builder().requestFactory(requestFactory).baseUrl(aiBaseUrl).build();
	}

	/** AI 서버에 자료 추출을 요청합니다. */
	public String extractMaterial(String title, String description, String filename, byte[] pdfBytes) {
		try {
			Map<String, Object> response = restClient.post()
				.uri("/extract-material")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.body(Map.of(
					"title", title,
					"description", description,
					"filename", filename,
					"pdfBase64", Base64.getEncoder().encodeToString(pdfBytes)
				))
				.retrieve()
				.body(Map.class);
			return String.valueOf(response == null ? "" : response.getOrDefault("extractedText", ""));
		} catch (RestClientException e) {
			log.error("AI 자료 추출 서버 연결 실패: {}", e.getMessage(), e);
			return "AI 추출 실패로 기본 추출 텍스트를 사용합니다.";
		} catch (Exception e) {
			log.error("AI 자료 추출 중 예외 발생: {}", e.getMessage(), e);
			return "AI 추출 실패로 기본 추출 텍스트를 사용합니다.";
		}
	}

	public List<Map<String, Object>> generateQuestions(String materialTitle, String materialText, int questionCount) {
		try {
			String normalizedText = materialText == null ? "" : materialText.substring(0, Math.min(materialText.length(), 6000));
			Map<String, Object> response = restClient.post()
				.uri("/generate-questions")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.body(Map.of("material_title", materialTitle, "material_text", normalizedText, "question_count", questionCount))
				.retrieve()
				.body(Map.class);
			if (response == null || !(response.get("questions") instanceof List<?> questions)) {
				return List.of();
			}
			return questions.stream()
				.filter(Map.class::isInstance)
				.map(item -> (Map<String, Object>) item)
				.toList();
		} catch (RestClientException e) {
			log.error("AI 문항 생성 서버 연결 실패: {}", e.getMessage(), e);
			return List.of();
		} catch (Exception e) {
			log.error("AI 문항 생성 중 예외 발생: {}", e.getMessage(), e);
			return List.of();
		}
	}

	/** AI 서버에 질문을 전달하고 응답을 반환합니다. */
	public QaResponse ask(String context, String question) {
		try {
			String normalizedContext = normalizeContext(context);
			Map<String, Object> response = restClient.post()
				.uri("/qa")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.body(Map.of("context", normalizedContext, "question", question))
				.retrieve()
				.body(Map.class);
			if (response == null) {
				log.warn("AI QA 응답이 비어 있습니다. context길이={}, question={}", normalizedContext.length(), question);
				return new QaResponse("AI 응답이 비어 있습니다.", List.of(), false, true);
			}
			return new QaResponse(
				String.valueOf(response.getOrDefault("answer", "AI 응답이 비어 있습니다.")),
				(response.get("evidenceSnippets") instanceof List<?> snippets) ? snippets.stream().map(String::valueOf).toList() : List.of(),
				Boolean.TRUE.equals(response.get("grounded")),
				Boolean.TRUE.equals(response.get("insufficientEvidence"))
			);
		} catch (RestClientException e) {
			log.error("AI QA 서버 연결 실패: {}", e.getMessage(), e);
			return new QaResponse("AI 서버에 연결할 수 없습니다. 잠시 후 다시 시도해주세요.", List.of(), false, false);
		} catch (Exception e) {
			log.error("AI QA 처리 중 예외 발생: {}", e.getMessage(), e);
			return new QaResponse("AI 응답 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.", List.of(), false, false);
		}
	}

	private String normalizeContext(String context) {
		String safe = context == null ? "" : CONTROL_CHAR_PATTERN.matcher(context).replaceAll(" ");
		safe = safe.replace("\r", " ").replace("\n", " ");
		safe = safe.replaceAll("\\s+", " ").trim();
		return safe.substring(0, Math.min(safe.length(), 6000));
	}
}
