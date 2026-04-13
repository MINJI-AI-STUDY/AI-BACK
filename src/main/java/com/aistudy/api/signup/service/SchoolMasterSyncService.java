package com.aistudy.api.signup.service;

import com.aistudy.api.admin.School;
import com.aistudy.api.admin.SchoolRepository;
import com.aistudy.api.common.BadRequestException;
import com.aistudy.api.signup.dto.SchoolMasterSyncResponse;
import com.aistudy.api.signup.model.SchoolMasterEntity;
import com.aistudy.api.signup.repository.SchoolMasterRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class SchoolMasterSyncService {
	private static final Logger log = LoggerFactory.getLogger(SchoolMasterSyncService.class);
	private final SchoolMasterRepository schoolMasterRepository;
	private final SchoolRepository schoolRepository;
	private final ObjectMapper objectMapper;
	private final String baseUrl;
	private final String apiKey;
	private final String endpoint;
	private final int pageSize;
	private final int maxRetries;
	private final long retryDelayMs;
	private final TransactionTemplate transactionTemplate;

	public SchoolMasterSyncService(
		SchoolMasterRepository schoolMasterRepository,
		SchoolRepository schoolRepository,
		ObjectMapper objectMapper,
		@Value("${app.school-api.base-url}") String baseUrl,
		@Value("${app.school-api.key}") String apiKey,
		@Value("${app.school-api.endpoint}") String endpoint,
		@Value("${app.school-api.page-size}") int pageSize,
		@Value("${app.school-api.max-retries:3}") int maxRetries,
		@Value("${app.school-api.retry-delay-ms:1000}") long retryDelayMs,
		PlatformTransactionManager transactionManager
	) {
		this.schoolMasterRepository = schoolMasterRepository;
		this.schoolRepository = schoolRepository;
		this.objectMapper = objectMapper;
		this.baseUrl = baseUrl;
		this.apiKey = apiKey;
		this.endpoint = endpoint;
		this.pageSize = pageSize;
		this.maxRetries = Math.max(1, maxRetries);
		this.retryDelayMs = Math.max(0L, retryDelayMs);
		this.transactionTemplate = new TransactionTemplate(transactionManager);
	}

	public SchoolMasterSyncResponse syncAll() {
		if (apiKey == null || apiKey.isBlank()) {
			throw new BadRequestException("학교 Open API 키가 설정되지 않았습니다.");
		}
		int imported = 0;
		int updated = 0;
		int page = 1;
		while (true) {
			List<Map<String, Object>> rows = fetchPage(page);
			if (rows.isEmpty()) break;
			SyncCounts counts = transactionTemplate.execute(status -> syncRows(rows));
			if (counts == null) {
				throw new BadRequestException("학교 마스터 저장 트랜잭션을 시작하지 못했습니다.");
			}
			imported += counts.imported();
			updated += counts.updated();
			page++;
		}
		return new SchoolMasterSyncResponse(imported, updated, imported + updated);
	}

	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> fetchPage(int page) {
		Map<String, Object> payload = fetchPageWithRetry(page);
		if (payload == null) return List.of();
		Object root = payload.get(endpoint);
		if (!(root instanceof List<?> list) || list.isEmpty()) {
			throw new BadRequestException("학교 Open API 응답 형식이 올바르지 않습니다.");
		}
		Map<String, Object> headSection = list.stream()
			.filter(Map.class::isInstance)
			.map(item -> (Map<String, Object>) item)
			.filter(item -> item.containsKey("head"))
			.findFirst()
			.orElse(Map.of());
		validateResult(headSection.get("head"));
		if (list.size() < 2) {
			return List.of();
		}
		Object rows = ((Map<String, Object>) list.get(1)).get("row");
		if (!(rows instanceof List<?> rowList)) {
			return List.of();
		}
		return rowList.stream().filter(Map.class::isInstance).map(item -> (Map<String, Object>) item).toList();
	}

	private Map<String, Object> fetchPageWithRetry(int page) {
		Exception lastJavaException = null;
		for (int attempt = 1; attempt <= maxRetries; attempt++) {
			try {
				return fetchPageWithJava(page);
			} catch (Exception exception) {
				lastJavaException = exception;
				log.warn("학교 Open API Java 호출 실패 page={}, attempt={}/{}: {}", page, attempt, maxRetries, exception.getMessage());
				if (attempt < maxRetries) {
					sleepBeforeRetry(page, attempt);
				}
			}
		}
		log.warn("학교 Open API Java 호출이 모두 실패하여 curl fallback을 시도합니다. page={}", page, lastJavaException);
		return fetchPageWithCurl(page, lastJavaException);
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> fetchPageWithJava(int page) throws IOException {
		String url = buildUrl(page);
		HttpURLConnection connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
		connection.setConnectTimeout(10000);
		connection.setReadTimeout(120000);
		connection.setRequestProperty("User-Agent", "Mozilla/5.0");
		connection.setRequestProperty("Accept", "application/json");
		connection.setRequestMethod("GET");
		int status = connection.getResponseCode();
		if (status >= 400) {
			String errorBody = readStream(connection.getErrorStream());
			throw new IOException("HTTP " + status + " body=" + abbreviate(errorBody));
		}
		try (InputStream inputStream = new BufferedInputStream(connection.getInputStream())) {
			return objectMapper.readValue(inputStream, Map.class);
		}
	}

	private String value(Map<String, Object> row, String primary, String fallback) {
		Object first = row.get(primary);
		if (first != null) return String.valueOf(first).trim();
		Object second = row.get(fallback);
		return second == null ? "" : String.valueOf(second).trim();
	}

	private String deriveDomain(String schoolName) {
		if (schoolName == null || schoolName.isBlank()) return null;
		String normalized = schoolName.replaceAll("\\s+", "").toLowerCase();
		return normalized + ".school.kr";
	}

	private void syncTenantSchool(SchoolMasterEntity schoolMaster) {
		School school = schoolRepository.findById(schoolMaster.getId())
			.orElseGet(() -> schoolRepository.findByName(schoolMaster.getName()).orElse(new School(schoolMaster.getId(), schoolMaster.getName(), schoolMaster.isActive())));
		school.update(schoolMaster.getName(), schoolMaster.isActive());
		schoolRepository.save(school);
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> fetchPageWithCurl(int page, Exception originalException) {
		try {
			Process process = new ProcessBuilder(
				"curl",
				"--silent",
				"--show-error",
				"--fail-with-body",
				"--location",
				"--connect-timeout", "10",
				"--max-time", "120",
				buildUrl(page)
			).start();
			String body;
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
				body = reader.lines().collect(Collectors.joining());
			}
			String errorBody;
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {
				errorBody = reader.lines().collect(Collectors.joining());
			}
			int exitCode = process.waitFor();
			if (exitCode != 0) {
				throw new IOException("curl exitCode=" + exitCode + " error=" + abbreviate(errorBody));
			}
			return objectMapper.readValue(body, Map.class);
		} catch (Exception curlException) {
			log.error("학교 Open API curl fallback도 실패", curlException);
			String originalMessage = originalException == null ? "unknown" : originalException.getClass().getSimpleName() + " - " + originalException.getMessage();
			throw new BadRequestException("학교 Open API 호출에 실패했습니다: " + originalMessage);
		}
	}

	private SyncCounts syncRows(List<Map<String, Object>> rows) {
		int imported = 0;
		int updated = 0;
		for (Map<String, Object> row : rows) {
			String schoolCode = value(row, "SD_SCHUL_CODE", "학교코드");
			if (schoolCode.isBlank()) continue;
			Optional<SchoolMasterEntity> existing = schoolMasterRepository.findByOfficialSchoolCode(schoolCode);
			String name = value(row, "SCHUL_NM", "학교명");
			String level = value(row, "SCHUL_KND_SC_NM", "학교급구분");
			String address = value(row, "ORG_RDNMA", "소재지도로명주소");
			String region = value(row, "ATPT_OFCDC_SC_NM", "시도교육청명");
			String emailDomain = deriveDomain(name);
			if (existing.isPresent()) {
				SchoolMasterEntity entity = existing.get();
				entity.updateFromApi(name, level, address, region, emailDomain, true);
				schoolMasterRepository.save(entity);
				syncTenantSchool(entity);
				updated++;
			} else {
				SchoolMasterEntity created = schoolMasterRepository.save(new SchoolMasterEntity(schoolCode, name, level, address, region, emailDomain, true));
				syncTenantSchool(created);
				imported++;
			}
		}
		return new SyncCounts(imported, updated);
	}

	@SuppressWarnings("unchecked")
	private void validateResult(Object headObject) {
		if (!(headObject instanceof List<?> headList)) {
			return;
		}
		for (Object item : headList) {
			if (!(item instanceof Map<?, ?> rawMap)) {
				continue;
			}
			Object resultObject = rawMap.get("RESULT");
			if (!(resultObject instanceof Map<?, ?> resultMap)) {
				continue;
			}
			Object codeValue = resultMap.get("CODE");
			Object messageValue = resultMap.get("MESSAGE");
			String code = codeValue == null ? "" : String.valueOf(codeValue);
			String message = messageValue == null ? "" : String.valueOf(messageValue);
			if ("INFO-000".equals(code) || "INFO-200".equals(code)) {
				return;
			}
			throw new BadRequestException("학교 Open API 오류: " + code + " - " + message);
		}
	}

	private String buildUrl(int page) {
		return baseUrl + "/" + endpoint
			+ "?KEY=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8)
			+ "&Type=json&pIndex=" + page + "&pSize=" + pageSize;
	}

	private String readStream(InputStream inputStream) throws IOException {
		if (inputStream == null) {
			return "";
		}
		try (InputStream stream = inputStream; BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
			return reader.lines().collect(Collectors.joining());
		}
	}

	private String abbreviate(String value) {
		if (value == null || value.isBlank()) {
			return "";
		}
		return value.length() <= 300 ? value : value.substring(0, 300) + "...";
	}

	private void sleepBeforeRetry(int page, int attempt) {
		if (retryDelayMs <= 0) {
			return;
		}
		try {
			Thread.sleep(retryDelayMs);
		} catch (InterruptedException interruptedException) {
			Thread.currentThread().interrupt();
			throw new BadRequestException("학교 Open API 재시도 대기 중 인터럽트가 발생했습니다. page=" + page + ", attempt=" + attempt);
		}
	}

	private record SyncCounts(int imported, int updated) {}
}
