package com.aistudy.api.signup.service;

import com.aistudy.api.common.BadRequestException;
import com.aistudy.api.signup.dto.SchoolMasterSyncResponse;
import com.aistudy.api.signup.model.SchoolMasterEntity;
import com.aistudy.api.signup.repository.SchoolMasterRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class SchoolMasterSyncService {
	private final SchoolMasterRepository schoolMasterRepository;
	private final RestClient restClient;
	private final String apiKey;
	private final String endpoint;
	private final int pageSize;

	public SchoolMasterSyncService(
		SchoolMasterRepository schoolMasterRepository,
		@Value("${app.school-api.base-url}") String baseUrl,
		@Value("${app.school-api.key}") String apiKey,
		@Value("${app.school-api.endpoint}") String endpoint,
		@Value("${app.school-api.page-size}") int pageSize
	) {
		this.schoolMasterRepository = schoolMasterRepository;
		this.restClient = RestClient.builder().baseUrl(baseUrl).build();
		this.apiKey = apiKey;
		this.endpoint = endpoint;
		this.pageSize = pageSize;
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
					existing.get().updateFromApi(name, level, address, region, emailDomain, true);
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
		Map<String, Object> payload = restClient.get()
			.uri(uriBuilder -> uriBuilder.pathSegment(apiKey, endpoint).queryParam("Type", "json").queryParam("pIndex", page).queryParam("pSize", pageSize).build())
			.accept(MediaType.APPLICATION_JSON)
			.retrieve()
			.body(Map.class);
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
}
