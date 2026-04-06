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
		when(aiIntegrationService.extractMaterial(anyString(), anyString(), anyString())).thenReturn("추출 완료 텍스트");

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
		when(aiIntegrationService.extractMaterial(anyString(), anyString(), anyString())).thenReturn("추출 완료 텍스트");

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
		when(aiIntegrationService.extractMaterial(anyString(), anyString(), anyString())).thenThrow(new RuntimeException("AI 실패"));

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
		when(aiIntegrationService.extractMaterial(anyString(), anyString(), anyString())).thenReturn("재처리 완료 텍스트");

		mockMvc.perform(
			post("/api/teacher/materials/" + materialId + "/retry")
				.header("Authorization", "Bearer " + accessToken)
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("READY"));
	}
}
