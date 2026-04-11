package com.aistudy.api.material.controller;

import com.aistudy.api.auth.AuthService;
import com.aistudy.api.auth.AuthUser;
import com.aistudy.api.auth.Role;
import com.aistudy.api.material.model.Material;
import com.aistudy.api.material.service.MaterialService;
import com.aistudy.api.storage.StoredObject;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/materials")
public class SharedMaterialController {
	private final AuthService authService;
	private final MaterialService materialService;

	public SharedMaterialController(AuthService authService, MaterialService materialService) {
		this.authService = authService;
		this.materialService = materialService;
	}

	/** 공통 PDF 조회 — 인증된 사용자는 자신의 학교 범위 자료 PDF를 inline으로 조회합니다. */
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
			.header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename(storedObject.fileName(), StandardCharsets.UTF_8).build().toString())
			.body(storedObject.resource());
	}
}
