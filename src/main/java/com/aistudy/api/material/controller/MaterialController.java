package com.aistudy.api.material.controller;

import com.aistudy.api.auth.AuthService;
import com.aistudy.api.auth.AuthUser;
import com.aistudy.api.auth.Role;
import com.aistudy.api.material.dto.MaterialSummaryResponse;
import com.aistudy.api.material.model.Material;
import com.aistudy.api.material.service.MaterialService;
import com.aistudy.api.storage.StoredObject;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
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

	@PostMapping
	public MaterialSummaryResponse upload(
		@RequestHeader(name = "Authorization", required = false) String authorizationHeader,
		@RequestParam MultipartFile file,
		@RequestParam(defaultValue = "") String channelId,
		@RequestParam @NotBlank String title,
		@RequestParam(defaultValue = "") String description
	) {
		AuthUser teacher = authService.requireRole(authorizationHeader, Role.TEACHER);
		Material material = materialService.create(teacher.userId(), teacher.schoolId(), channelId, file, title, description);
		return MaterialSummaryResponse.from(material);
	}

	@GetMapping
	public List<MaterialSummaryResponse> list(@RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
		AuthUser teacher = authService.requireRole(authorizationHeader, Role.TEACHER);
		return materialService.getSchoolMaterials(teacher.schoolId()).stream().map(MaterialSummaryResponse::from).toList();
	}

	@GetMapping("/{materialId}")
	public MaterialSummaryResponse get(
		@RequestHeader(name = "Authorization", required = false) String authorizationHeader,
		@PathVariable String materialId
	) {
		AuthUser teacher = authService.requireRole(authorizationHeader, Role.TEACHER);
		return MaterialSummaryResponse.from(materialService.getSchoolMaterial(teacher.schoolId(), materialId));
	}

	@PostMapping("/{materialId}/retry")
	public MaterialSummaryResponse retry(
		@RequestHeader(name = "Authorization", required = false) String authorizationHeader,
		@PathVariable String materialId
	) {
		AuthUser teacher = authService.requireRole(authorizationHeader, Role.TEACHER);
		return MaterialSummaryResponse.from(materialService.retry(teacher.userId(), materialId));
	}

	@GetMapping("/document/{materialId}")
	public ResponseEntity<Resource> document(
		@RequestHeader(name = "Authorization", required = false) String authorizationHeader,
		@PathVariable String materialId
	) {
		AuthUser user = authService.getCurrentUser(authorizationHeader);
		Material material = user.role() == Role.OPERATOR
			? materialService.getById(materialId)
			: materialService.getSchoolMaterial(user.schoolId(), materialId);
		StoredObject storedObject = materialService.loadStoredObject(material);
		return ResponseEntity.ok()
			.contentType(MediaType.APPLICATION_PDF)
			.contentLength(storedObject.contentLength())
			.header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename(storedObject.fileName(), java.nio.charset.StandardCharsets.UTF_8).build().toString())
			.body(storedObject.resource());
	}
}
