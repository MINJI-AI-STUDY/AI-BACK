package com.aistudy.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import com.aistudy.api.common.integration.AiIntegrationService;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootTest
@AutoConfigureMockMvc
class ApiApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private AiIntegrationService aiIntegrationService;

	@Test
	void contextLoads() {
	}

	/**
	 * F1 로그인 성공 계약을 고정합니다.
	 */
	@Test
	void 로그인_성공을_반환한다() throws Exception {
		mockMvc.perform(
			post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"loginId":"teacher","password":"teacher123"}
					""")
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.accessToken").isNotEmpty())
			.andExpect(jsonPath("$.role").value("TEACHER"))
			.andExpect(jsonPath("$.displayName").value("교사 데모"));
	}

	/**
	 * F1 로그인 실패 계약을 고정합니다.
	 */
	@Test
	void 로그인_실패시_401을_반환한다() throws Exception {
		mockMvc.perform(
			post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"loginId":"teacher","password":"wrong"}
					""")
		)
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("AUTH_UNAUTHORIZED"));
	}

	/**
	 * F2 PDF 업로드 성공 계약을 고정합니다.
	 */
	@Test
	void PDF_업로드_성공을_반환한다() throws Exception {
		when(aiIntegrationService.extractMaterial(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.any(byte[].class))).thenReturn("추출 완료 텍스트");

		String token = mockMvc.perform(
			post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"loginId":"teacher","password":"teacher123"}
					""")
		)
			.andReturn()
			.getResponse()
			.getContentAsString();

		String accessToken = token.replaceAll(".*\"accessToken\":\"([^\"]+)\".*", "$1");
		MockMultipartFile file = new MockMultipartFile("file", "sample.pdf", "application/pdf", "test".getBytes());

		mockMvc.perform(
			MockMvcRequestBuilders.multipart("/api/teacher/materials")
				.file(file)
				.param("title", "자료 업로드")
				.param("description", "설명")
				.header("Authorization", "Bearer " + accessToken)
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.materialId").isNotEmpty())
			.andExpect(jsonPath("$.status").value("READY"));
	}

	/**
	 * F2 비 PDF 업로드 실패 계약을 고정합니다.
	 */
	@Test
	void PDF_아닌_파일은_거부한다() throws Exception {
		when(aiIntegrationService.extractMaterial(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.any(byte[].class))).thenReturn("추출 완료 텍스트");

		String token = mockMvc.perform(
			post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"loginId":"teacher","password":"teacher123"}
					""")
		)
			.andReturn()
			.getResponse()
			.getContentAsString();

		String accessToken = token.replaceAll(".*\"accessToken\":\"([^\"]+)\".*", "$1");
		MockMultipartFile file = new MockMultipartFile("file", "sample.txt", "text/plain", "test".getBytes());

		mockMvc.perform(
			MockMvcRequestBuilders.multipart("/api/teacher/materials")
				.file(file)
				.param("title", "자료 업로드")
				.param("description", "설명")
				.header("Authorization", "Bearer " + accessToken)
		)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("BAD_REQUEST"));
	}

	/**
	 * F2 FAILED 상태 자료만 재처리 가능하다는 계약을 고정합니다.
	 */
	@Test
	void 실패한_자료만_재처리할_수_있다() throws Exception {
		when(aiIntegrationService.extractMaterial(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.any(byte[].class))).thenThrow(new RuntimeException("AI 실패"));

		String token = mockMvc.perform(
			post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"loginId":"teacher","password":"teacher123"}
					""")
		)
			.andReturn()
			.getResponse()
			.getContentAsString();

		String accessToken = token.replaceAll(".*\"accessToken\":\"([^\"]+)\".*", "$1");
		MockMultipartFile file = new MockMultipartFile("file", "sample.pdf", "application/pdf", "test".getBytes());

		String uploadResponse = mockMvc.perform(
			MockMvcRequestBuilders.multipart("/api/teacher/materials")
				.file(file)
				.param("title", "재처리 대상")
				.param("description", "설명")
				.header("Authorization", "Bearer " + accessToken)
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("FAILED"))
			.andReturn()
			.getResponse()
			.getContentAsString();

		String materialId = uploadResponse.replaceAll(".*\"materialId\":\"([^\"]+)\".*", "$1");
		reset(aiIntegrationService);
		when(aiIntegrationService.extractMaterial(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.any(byte[].class))).thenReturn("재처리 완료 텍스트");

		mockMvc.perform(
			post("/api/teacher/materials/" + materialId + "/retry")
				.header("Authorization", "Bearer " + accessToken)
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("READY"));
	}

	/**
	 * F3 문제 생성 성공 계약을 고정합니다.
	 */
	@Test
	void 문제_세트_생성을_반환한다() throws Exception {
		when(aiIntegrationService.extractMaterial(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.any(byte[].class))).thenReturn("추출 완료 텍스트");

		String accessToken = teacherAccessToken();
		String materialId = uploadReadyMaterial(accessToken, "문제 생성 자료", "설명");

		mockMvc.perform(
			post("/api/teacher/materials/" + materialId + "/question-sets/generate")
				.header("Authorization", "Bearer " + accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"questionCount":2,"difficulty":"EASY"}
					""")
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.questionSetId").isNotEmpty())
			.andExpect(jsonPath("$.status").value("REVIEW_REQUIRED"))
			.andExpect(jsonPath("$.questions.length()").value(2));
	}

	/**
	 * F3 제외되지 않은 문항이 없으면 배포를 차단합니다.
	 */
	@Test
	void 배포_가능한_문항이_없으면_배포를_거부한다() throws Exception {
		when(aiIntegrationService.extractMaterial(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.any(byte[].class))).thenReturn("추출 완료 텍스트");

		String accessToken = teacherAccessToken();
		String materialId = uploadReadyMaterial(accessToken, "배포 차단 자료", "설명");
        String questionSetId = mockMvc.perform(
			post("/api/teacher/materials/" + materialId + "/question-sets/generate")
				.header("Authorization", "Bearer " + accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"questionCount":1,"difficulty":"EASY"}
					""")
		)
			.andReturn()
			.getResponse()
			.getContentAsString()
			.replaceAll(".*\"questionSetId\":\"([^\"]+)\".*", "$1");

		String questionId = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/teacher/question-sets/" + questionSetId)
				.header("Authorization", "Bearer " + accessToken)
		)
			.andReturn()
			.getResponse()
			.getContentAsString()
			.replaceAll(".*\"id\":\"([^\"]+)\".*", "$1");

		mockMvc.perform(
			MockMvcRequestBuilders.patch("/api/teacher/question-sets/" + questionSetId + "/questions/" + questionId)
				.header("Authorization", "Bearer " + accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"stem":"수정 문항","options":["A","B","C","D"],"correctOptionIndex":0,"explanation":"해설","conceptTags":["태그1"],"excluded":true}
					""")
		)
			.andExpect(status().isOk());

		mockMvc.perform(
			post("/api/teacher/question-sets/" + questionSetId + "/publish")
				.header("Authorization", "Bearer " + accessToken)
		)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("BAD_REQUEST"));
	}

	/**
	 * F3 생성 결과 수가 요청 수와 다르면 생성 자체를 거부합니다.
	 */
	@Test
	void 생성_결과_수가_요청_수와_다르면_거부한다() throws Exception {
		when(aiIntegrationService.extractMaterial(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.any(byte[].class))).thenReturn("추출 완료 텍스트");

		String accessToken = teacherAccessToken();
		String materialId = uploadReadyMaterial(accessToken, "생성 수 검증 자료", "설명");

		mockMvc.perform(
			post("/api/teacher/materials/" + materialId + "/question-sets/generate")
				.header("Authorization", "Bearer " + accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"questionCount":0,"difficulty":"EASY"}
					""")
		)
			.andExpect(status().isBadRequest());
	}

	/**
	 * F4 학생 제출과 결과 조회 계약을 고정합니다.
	 */
	@Test
	void 학생이_문제를_제출하고_결과를_조회할_수_있다() throws Exception {
		when(aiIntegrationService.extractMaterial(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.any(byte[].class))).thenReturn("추출 완료 텍스트");

		String teacherToken = teacherAccessToken();
		String materialId = uploadReadyMaterial(teacherToken, "학생 제출 자료", "설명");
		String generateResponse = mockMvc.perform(
			post("/api/teacher/materials/" + materialId + "/question-sets/generate")
				.header("Authorization", "Bearer " + teacherToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"questionCount":2,"difficulty":"EASY"}
					""")
		)
			.andReturn().getResponse().getContentAsString();

		String questionSetId = generateResponse.replaceAll(".*\"questionSetId\":\"([^\"]+)\".*", "$1");
		String questionSetDetail = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/teacher/question-sets/" + questionSetId)
				.header("Authorization", "Bearer " + teacherToken)
		)
			.andReturn().getResponse().getContentAsString();

		List<String> questionIds = extractQuestionIds(questionSetDetail);
		String firstQuestionId = questionIds.get(0);
		String secondQuestionId = questionIds.get(1);

		String publishResponse = mockMvc.perform(
			post("/api/teacher/question-sets/" + questionSetId + "/publish")
				.header("Authorization", "Bearer " + teacherToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"dueAt":"2030-12-31T23:59:00"}
					""")
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.dueAt").value("2030-12-31T23:59:00"))
			.andReturn().getResponse().getContentAsString();

		String distributionCode = publishResponse.replaceAll(".*\"distributionCode\":\"([^\"]+)\".*", "$1");
		String studentToken = studentAccessToken();

		mockMvc.perform(
			MockMvcRequestBuilders.get("/api/student/question-sets/" + distributionCode)
				.header("Authorization", "Bearer " + studentToken)
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.questions.length()").value(2));

		String submissionResponse = mockMvc.perform(
			post("/api/student/question-sets/" + distributionCode + "/submissions")
				.header("Authorization", "Bearer " + studentToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"answers":[{"questionId":"%s","selectedOptionIndex":0},{"questionId":"%s","selectedOptionIndex":0}]}
					""".formatted(firstQuestionId, secondQuestionId))
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.submissionId").isNotEmpty())
			.andReturn().getResponse().getContentAsString();

		String submissionId = submissionResponse.replaceAll(".*\"submissionId\":\"([^\"]+)\".*", "$1");

		mockMvc.perform(
			MockMvcRequestBuilders.get("/api/student/submissions/" + submissionId + "/result")
				.header("Authorization", "Bearer " + studentToken)
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.score").exists());
	}

	/**
	 * F4 중복 제출 차단 계약을 고정합니다.
	 */
	@Test
	void 이미_제출한_세트는_다시_제출할_수_없다() throws Exception {
		when(aiIntegrationService.extractMaterial(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.any(byte[].class))).thenReturn("추출 완료 텍스트");

		String teacherToken = teacherAccessToken();
		String materialId = uploadReadyMaterial(teacherToken, "중복 제출 자료", "설명");
		String generateResponse = mockMvc.perform(
			post("/api/teacher/materials/" + materialId + "/question-sets/generate")
				.header("Authorization", "Bearer " + teacherToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"questionCount":2,"difficulty":"EASY"}
					""")
		)
			.andReturn().getResponse().getContentAsString();

		String questionSetId = generateResponse.replaceAll(".*\"questionSetId\":\"([^\"]+)\".*", "$1");
		String questionSetDetail = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/teacher/question-sets/" + questionSetId)
				.header("Authorization", "Bearer " + teacherToken)
		)
			.andReturn().getResponse().getContentAsString();

		List<String> questionIds = extractQuestionIds(questionSetDetail);
		String publishResponse = mockMvc.perform(
			post("/api/teacher/question-sets/" + questionSetId + "/publish")
				.header("Authorization", "Bearer " + teacherToken)
		)
			.andReturn().getResponse().getContentAsString();

		String distributionCode = publishResponse.replaceAll(".*\"distributionCode\":\"([^\"]+)\".*", "$1");
		String studentToken = studentAccessToken();
		String submitPayload = """
			{"answers":[{"questionId":"%s","selectedOptionIndex":0},{"questionId":"%s","selectedOptionIndex":0}]}
			""".formatted(questionIds.get(0), questionIds.get(1));

		mockMvc.perform(
			post("/api/student/question-sets/" + distributionCode + "/submissions")
				.header("Authorization", "Bearer " + studentToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(submitPayload)
		)
			.andExpect(status().isOk());

		mockMvc.perform(
			post("/api/student/question-sets/" + distributionCode + "/submissions")
				.header("Authorization", "Bearer " + studentToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(submitPayload)
		)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("BAD_REQUEST"));
	}

	/**
	 * F5 교사/운영자 대시보드 계약을 고정합니다.
	 */
	@Test
	void 교사와_운영자가_대시보드를_조회할_수_있다() throws Exception {
		when(aiIntegrationService.extractMaterial(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.any(byte[].class))).thenReturn("추출 완료 텍스트");

		String teacherToken = teacherAccessToken();
		String materialId = uploadReadyMaterial(teacherToken, "대시보드 자료", "설명");
		String generateResponse = mockMvc.perform(
			post("/api/teacher/materials/" + materialId + "/question-sets/generate")
				.header("Authorization", "Bearer " + teacherToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"questionCount":2,"difficulty":"EASY"}
					""")
		)
			.andReturn().getResponse().getContentAsString();

		String questionSetId = generateResponse.replaceAll(".*\"questionSetId\":\"([^\"]+)\".*", "$1");
		String questionSetDetail = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/teacher/question-sets/" + questionSetId)
				.header("Authorization", "Bearer " + teacherToken)
		)
			.andReturn().getResponse().getContentAsString();
		List<String> questionIds = extractQuestionIds(questionSetDetail);

		String publishResponse = mockMvc.perform(
			post("/api/teacher/question-sets/" + questionSetId + "/publish")
				.header("Authorization", "Bearer " + teacherToken)
		)
			.andReturn().getResponse().getContentAsString();
		String distributionCode = publishResponse.replaceAll(".*\"distributionCode\":\"([^\"]+)\".*", "$1");

		String studentToken = studentAccessToken();
		mockMvc.perform(
			post("/api/student/question-sets/" + distributionCode + "/submissions")
				.header("Authorization", "Bearer " + studentToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"answers":[{"questionId":"%s","selectedOptionIndex":0},{"questionId":"%s","selectedOptionIndex":0}]}
					""".formatted(questionIds.get(0), questionIds.get(1)))
		)
			.andExpect(status().isOk());

		mockMvc.perform(
			MockMvcRequestBuilders.get("/api/teacher/question-sets/" + questionSetId + "/dashboard")
				.header("Authorization", "Bearer " + teacherToken)
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.studentScores.length()").value(1));

		String operatorToken = operatorAccessToken();
		mockMvc.perform(
			MockMvcRequestBuilders.get("/api/operator/overview")
				.header("Authorization", "Bearer " + operatorToken)
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.averageScore").exists());
	}

	/**
	 * F6 자료 기반 질의응답 계약을 고정합니다.
	 */
	@Test
	void 학생이_자료기반_질문을_할_수_있다() throws Exception {
		when(aiIntegrationService.extractMaterial(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.any(byte[].class))).thenReturn("자료 핵심 개념 설명");
		when(aiIntegrationService.ask(anyString(), anyString())).thenReturn(new com.aistudy.api.qa.dto.QaResponse("자료 기반 답변입니다.", List.of("자료 핵심 개념 설명"), true, false));

		String teacherToken = teacherAccessToken();
		String materialId = uploadReadyMaterial(teacherToken, "질의응답 자료", "설명");
		String studentToken = studentAccessToken();

		mockMvc.perform(
			post("/api/student/materials/" + materialId + "/qa")
				.header("Authorization", "Bearer " + studentToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"question":"핵심 개념이 뭐야?"}
					""")
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.answer").value("자료 기반 답변입니다."))
			.andExpect(jsonPath("$.grounded").value(true));
	}

	/**
	 * F6 AI 실패 시 fallback 응답 계약을 고정합니다.
	 */
	@Test
	void AI_실패시_fallback_답변을_반환한다() throws Exception {
		when(aiIntegrationService.extractMaterial(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.any(byte[].class))).thenReturn("자료 핵심 개념 설명");
		when(aiIntegrationService.ask(anyString(), anyString())).thenReturn(new com.aistudy.api.qa.dto.QaResponse("AI 실패", List.of(), false, true));

		String teacherToken = teacherAccessToken();
		String materialId = uploadReadyMaterial(teacherToken, "fallback 자료", "설명");
		String studentToken = studentAccessToken();

		mockMvc.perform(
			post("/api/student/materials/" + materialId + "/qa")
				.header("Authorization", "Bearer " + studentToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"question":"근거를 못 찾으면 어떻게 돼?"}
					""")
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.grounded").value(false))
			.andExpect(jsonPath("$.insufficientEvidence").value(true))
			.andExpect(jsonPath("$.answer").value("AI 실패"));
	}

	private String teacherAccessToken() throws Exception {
		return mockMvc.perform(
			post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"loginId":"teacher","password":"teacher123"}
					""")
		)
			.andReturn()
			.getResponse()
			.getContentAsString()
			.replaceAll(".*\"accessToken\":\"([^\"]+)\".*", "$1");
	}

	private String studentAccessToken() throws Exception {
		return mockMvc.perform(
			post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"loginId":"student","password":"student123"}
					""")
		)
			.andReturn()
			.getResponse()
			.getContentAsString()
			.replaceAll(".*\"accessToken\":\"([^\"]+)\".*", "$1");
	}

	private String operatorAccessToken() throws Exception {
		return mockMvc.perform(
			post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"loginId":"operator","password":"operator123"}
					""")
		)
			.andReturn()
			.getResponse()
			.getContentAsString()
			.replaceAll(".*\"accessToken\":\"([^\"]+)\".*", "$1");
	}

	private String uploadReadyMaterial(String accessToken, String title, String description) throws Exception {
		MockMultipartFile file = new MockMultipartFile("file", "sample.pdf", "application/pdf", "test".getBytes());
		return mockMvc.perform(
			MockMvcRequestBuilders.multipart("/api/teacher/materials")
				.file(file)
				.param("title", title)
				.param("description", description)
				.header("Authorization", "Bearer " + accessToken)
		)
			.andReturn()
			.getResponse()
			.getContentAsString()
			.replaceAll(".*\"materialId\":\"([^\"]+)\".*", "$1");
	}

	private List<String> extractQuestionIds(String payload) {
		Matcher matcher = Pattern.compile("\\\"id\\\":\\\"(question-[^\\\"]+)\\\"").matcher(payload);
		List<String> ids = new ArrayList<>();
		while (matcher.find()) {
			ids.add(matcher.group(1));
		}
		return ids;
	}
}
