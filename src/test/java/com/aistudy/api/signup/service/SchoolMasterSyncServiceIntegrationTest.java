package com.aistudy.api.signup.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.aistudy.api.admin.SchoolRepository;
import com.aistudy.api.common.BadRequestException;
import com.aistudy.api.signup.repository.SchoolMasterRepository;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
class SchoolMasterSyncServiceIntegrationTest {

	private static final HttpServer SERVER = startServer();
	private static final AtomicInteger PAGE_ONE_ATTEMPTS = new AtomicInteger();
	private static volatile Scenario scenario = Scenario.RETRY_ONCE_THEN_SUCCESS;

	@Autowired
	private SchoolMasterSyncService schoolMasterSyncService;

	@Autowired
	private SchoolMasterRepository schoolMasterRepository;

	@Autowired
	private SchoolRepository schoolRepository;

	@DynamicPropertySource
	static void registerProperties(DynamicPropertyRegistry registry) {
		registry.add("app.school-api.base-url", () -> "http://127.0.0.1:" + SERVER.getAddress().getPort());
		registry.add("app.school-api.key", () -> "integration-test-key");
		registry.add("app.school-api.endpoint", () -> "schoolInfo");
		registry.add("app.school-api.page-size", () -> "1");
		registry.add("app.school-api.max-retries", () -> scenario == Scenario.RETRY_ONCE_THEN_SUCCESS ? "2" : "1");
		registry.add("app.school-api.retry-delay-ms", () -> "0");
		registry.add("app.school-api.sync-enabled", () -> "false");
	}

	@BeforeEach
	void setUp() {
		PAGE_ONE_ATTEMPTS.set(0);
		var testSchools = schoolMasterRepository.findAll().stream()
			.filter(school -> school.getOfficialSchoolCode().startsWith("901"))
			.toList();
		schoolRepository.deleteAllById(testSchools.stream().map(school -> school.getId()).toList());
		schoolMasterRepository.deleteAll(testSchools);
	}

	@AfterAll
	static void tearDown() {
		SERVER.stop(0);
	}

	@Test
	void 일시적인_500_응답은_재시도_후_학교_목록을_저장한다() {
		scenario = Scenario.RETRY_ONCE_THEN_SUCCESS;

		var result = schoolMasterSyncService.syncAll();

		assertThat(result.importedCount()).isEqualTo(1);
		assertThat(result.updatedCount()).isZero();
		assertThat(result.totalCount()).isEqualTo(1);
		assertThat(PAGE_ONE_ATTEMPTS.get()).isEqualTo(2);
		assertThat(schoolMasterRepository.findByOfficialSchoolCode("9010001")).isPresent();
		assertThat(schoolMasterRepository.findByOfficialSchoolCode("9010001").orElseThrow().getName()).isEqualTo("테스트학교동기화A");
		assertThat(schoolRepository.findById(schoolMasterRepository.findByOfficialSchoolCode("9010001").orElseThrow().getId())).isPresent();
	}

	@Test
	void 다음_페이지_호출이_실패해도_이전_페이지_저장은_보존된다() {
		scenario = Scenario.SECOND_PAGE_FAILURE;

		assertThatThrownBy(() -> schoolMasterSyncService.syncAll())
			.isInstanceOf(BadRequestException.class)
			.hasMessageContaining("학교 Open API 호출에 실패했습니다");

		assertThat(schoolMasterRepository.findByOfficialSchoolCode("9010002")).isPresent();
		assertThat(schoolRepository.findById(schoolMasterRepository.findByOfficialSchoolCode("9010002").orElseThrow().getId())).isPresent();
	}

	private static HttpServer startServer() {
		try {
			HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
			server.createContext("/schoolInfo", SchoolMasterSyncServiceIntegrationTest::handleRequest);
			server.start();
			return server;
		} catch (IOException exception) {
			throw new IllegalStateException("테스트용 학교 API 서버를 시작하지 못했습니다.", exception);
		}
	}

	private static void handleRequest(HttpExchange exchange) throws IOException {
		String query = exchange.getRequestURI().getQuery();
		int pageIndex = parsePageIndex(query);
		if (scenario == Scenario.RETRY_ONCE_THEN_SUCCESS && pageIndex == 1 && PAGE_ONE_ATTEMPTS.incrementAndGet() == 1) {
			writeResponse(exchange, 500, "{\"error\":\"temporary\"}");
			return;
		}
		if (scenario == Scenario.SECOND_PAGE_FAILURE && pageIndex == 2) {
			writeResponse(exchange, 500, "{\"error\":\"page2\"}");
			return;
		}
		if (pageIndex == 1) {
			if (scenario == Scenario.RETRY_ONCE_THEN_SUCCESS) {
				writeResponse(exchange, 200, successPayload("9010001", "테스트학교동기화A"));
				return;
			}
			writeResponse(exchange, 200, successPayload("9010002", "테스트학교동기화B"));
			return;
		}
		writeResponse(exchange, 200, emptyPayload());
	}

	private static int parsePageIndex(String query) {
		if (query == null || query.isBlank()) {
			return 1;
		}
		for (String token : query.split("&")) {
			if (token.startsWith("pIndex=")) {
				return Integer.parseInt(token.substring("pIndex=".length()));
			}
		}
		return 1;
	}

	private static void writeResponse(HttpExchange exchange, int status, String body) throws IOException {
		byte[] payload = body.getBytes(StandardCharsets.UTF_8);
		exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
		exchange.sendResponseHeaders(status, payload.length);
		try (OutputStream outputStream = exchange.getResponseBody()) {
			outputStream.write(payload);
		}
	}

	private static String successPayload(String schoolCode, String schoolName) {
		return """
			{
			  "schoolInfo": [
			    {
			      "head": [
			        { "list_total_count": 1 },
			        { "RESULT": { "CODE": "INFO-000", "MESSAGE": "정상 처리되었습니다." } }
			      ]
			    },
			    {
			      "row": [
			        {
			          "SD_SCHUL_CODE": "%s",
			          "SCHUL_NM": "%s",
			          "SCHUL_KND_SC_NM": "고등학교",
			          "ORG_RDNMA": "서울특별시 테스트로 1",
			          "ATPT_OFCDC_SC_NM": "서울특별시교육청"
			        }
			      ]
			    }
			  ]
			}
			""".formatted(schoolCode, schoolName);
	}

	private static String emptyPayload() {
		return """
			{
			  "schoolInfo": [
			    {
			      "head": [
			        { "list_total_count": 1 },
			        { "RESULT": { "CODE": "INFO-200", "MESSAGE": "해당하는 데이터가 없습니다." } }
			      ]
			    }
			  ]
			}
			""";
	}

	private enum Scenario {
		RETRY_ONCE_THEN_SUCCESS,
		SECOND_PAGE_FAILURE
	}
}
