package com.aistudy.api.material.controller;

import com.aistudy.api.auth.AuthService;
import com.aistudy.api.auth.AuthUser;
import com.aistudy.api.auth.Role;
import com.aistudy.api.material.dto.MaterialSummaryResponse;
import com.aistudy.api.material.service.MaterialService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student/materials")
public class StudentMaterialController {
	private final AuthService authService;
	private final MaterialService materialService;

	public StudentMaterialController(AuthService authService, MaterialService materialService) {
		this.authService = authService;
		this.materialService = materialService;
	}

	@GetMapping
	public List<MaterialSummaryResponse> list(@RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
		AuthUser student = authService.requireRole(authorizationHeader, Role.STUDENT);
		return materialService.getReadySchoolMaterials(student.schoolId()).stream().map(MaterialSummaryResponse::from).toList();
	}
}
