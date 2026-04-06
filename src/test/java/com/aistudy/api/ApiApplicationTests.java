package com.aistudy.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ApiApplicationTests {

	@Autowired
	private MockMvc mockMvc;

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
}
