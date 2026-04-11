package com.aistudy.api.signup.service;

import com.aistudy.api.common.BadRequestException;
import com.aistudy.api.signup.dto.SchoolMasterSyncResponse;
import com.aistudy.api.signup.model.SchoolMasterEntity;
import com.aistudy.api.signup.repository.SchoolMasterRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
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
import org.springframework.transaction.annotation.Transactional;

@Service
public class SchoolMasterSyncService {
	private static final Logger log = LoggerFactory.getLogger(SchoolMasterSyncService.class);
	private final SchoolMasterRepository schoolMasterRepository;
	private final ObjectMapper objectMapper;
	private final String baseUrl;
	private final String apiKey;
	private final String endpoint;
	private final int pageSize;

	public SchoolMasterSyncService(
		SchoolMasterRepository schoolMasterRepository,
		ObjectMapper objectMapper,
		@Value("${app.school-api.base-url}") String baseUrl,
		@Value("${app.school-api.key}") String apiKey,
		@Value("${app.school-api.endpoint}") String endpoint,
		@Value("${app.school-api.page-size}") int pageSize
	) {
		this.schoolMasterRepository = schoolMasterRepository;
		this.objectMapper = objectMapper;
		this.baseUrl = baseUrl;
		this.apiKey = apiKey;
		this.endpoint = endpoint;
		this.pageSize = pageSize;
	}

	@Transactional
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
					updated++;
				} else {
					schoolMasterRepository.save(new SchoolMasterEntity(schoolCode, name, level, address, region, emailDomain, true));
					imported++;
				}
			}
			page++;
		}
		return new SchoolMasterSyncResponse(imported, updated, imported + updated);
	}

	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> fetchPage(int page) {
		Map<String, Object> payload;
		try {
			String url = baseUrl + "/" + endpoint
				+ "?KEY=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8)
				+ "&Type=json&pIndex=" + page + "&pSize=" + pageSize;
			HttpURLConnection connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
			connection.setConnectTimeout(10000);
			connection.setReadTimeout(120000);
			connection.setRequestProperty("User-Agent", "Mozilla/5.0");
			connection.setRequestProperty("Accept", "application/json");
			connection.setRequestMethod("GET");
			try (InputStream inputStream = new BufferedInputStream(connection.getInputStream())) {
				payload = objectMapper.readValue(inputStream, Map.class);
			}
		} catch (Exception exception) {
			log.warn("학교 Open API Java 호출 실패, curl fallback 시도", exception);
			payload = fetchPageWithCurl(page, exception);
		}
		if (payload == null) return List.of();
		Object root = payload.get(endpoint);
		if (!(root instanceof List<?> list) || list.size() < 2) return List.of();
		Object rows = ((Map<String, Object>) list.get(1)).get("row");
		if (!(rows instanceof List<?> rowList)) return List.of();
		return rowList.stream().filter(Map.class::isInstance).map(item -> (Map<String, Object>) item).toList();
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

	@SuppressWarnings("unchecked")
	private Map<String, Object> fetchPageWithCurl(int page, Exception originalException) {
		try {
			String url = baseUrl + "/" + endpoint
				+ "?KEY=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8)
				+ "&Type=json&pIndex=" + page + "&pSize=" + pageSize;
			Process process = new ProcessBuilder("curl", "-s", url).start();
			String body;
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
				body = reader.lines().collect(Collectors.joining());
			}
			process.waitFor();
			return objectMapper.readValue(body, Map.class);
		} catch (Exception curlException) {
			log.error("학교 Open API curl fallback도 실패", curlException);
			throw new BadRequestException("학교 Open API 호출에 실패했습니다: " + originalException.getClass().getSimpleName() + " - " + originalException.getMessage());
		}
	}
}
