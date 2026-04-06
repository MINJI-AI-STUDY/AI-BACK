package com.aistudy.api.material.controller;

import com.aistudy.api.auth.AuthService;
import com.aistudy.api.auth.AuthUser;
import com.aistudy.api.auth.Role;
import com.aistudy.api.material.dto.MaterialSummaryResponse;
import com.aistudy.api.material.model.Material;
import com.aistudy.api.material.service.MaterialService;
import jakarta.validation.constraints.NotBlank;
import java.net.MalformedURLException;
import java.nio.file.Path;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequestMapping("/api/teacher/materials")
public class MaterialController {

	private final AuthService authService;
	private final MaterialService materialService;

	public MaterialController(AuthService authService, MaterialService materialService) {
		this.authService = authService;
		this.materialService = materialService;
	}

	/** 교사 자료 업로드를 처리합니다. */
	@PostMapping
	public MaterialSummaryResponse upload(
		@RequestHeader(name = "Authorization", required = false) String authorizationHeader,
		@RequestParam MultipartFile file,
		@RequestParam @NotBlank String title,
		@RequestParam(defaultValue = "") String description
	) {
		AuthUser teacher = authService.requireRole(authorizationHeader, Role.TEACHER);
		Material material = materialService.create(teacher.userId(), file, title, description);
		return MaterialSummaryResponse.from(material);
	}

	/** 자료 상태를 조회합니다. */
	@GetMapping("/{materialId}")
	public MaterialSummaryResponse get(
		@RequestHeader(name = "Authorization", required = false) String authorizationHeader,
		@PathVariable String materialId
	) {
		AuthUser teacher = authService.requireRole(authorizationHeader, Role.TEACHER);
		return MaterialSummaryResponse.from(materialService.getOwnedMaterial(teacher.userId(), materialId));
	}

	/** 실패 자료 재처리를 요청합니다. */
	@PostMapping("/{materialId}/retry")
	public MaterialSummaryResponse retry(
		@RequestHeader(name = "Authorization", required = false) String authorizationHeader,
		@PathVariable String materialId
	) {
		AuthUser teacher = authService.requireRole(authorizationHeader, Role.TEACHER);
		return MaterialSummaryResponse.from(materialService.retry(teacher.userId(), materialId));
	}

	/** 교사와 학생이 공통으로 자료 PDF를 조회합니다. */
	@GetMapping("/document/{materialId}")
	public ResponseEntity<Resource> document(
		@RequestHeader(name = "Authorization", required = false) String authorizationHeader,
		@PathVariable String materialId
	) throws MalformedURLException {
		authService.getCurrentUser(authorizationHeader);
		Material material = materialService.getById(materialId);
		Path filePath = Path.of(material.getFilePath());
		Resource resource = new UrlResource(filePath.toUri());

		return ResponseEntity.ok()
			.contentType(MediaType.APPLICATION_PDF)
			.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filePath.getFileName() + "\"")
			.body(resource);
	}
}
