package com.aistudy.api.material.service;

import com.aistudy.api.common.BadRequestException;
import com.aistudy.api.common.NotFoundException;
import com.aistudy.api.common.integration.AiIntegrationService;
import com.aistudy.api.material.model.Material;
import com.aistudy.api.material.model.MaterialStatus;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MaterialService {

	private final Map<String, Material> materials = new ConcurrentHashMap<>();
	private final Path materialRoot;
	private final AiIntegrationService aiIntegrationService;

	public MaterialService(@Value("${app.storage.material-root:data/materials}") String materialRoot, AiIntegrationService aiIntegrationService) {
		this.materialRoot = Paths.get(materialRoot);
		this.aiIntegrationService = aiIntegrationService;
	}

	/** PDF 자료를 저장하고 분석 완료 상태까지 전환합니다. */
	public Material create(String teacherId, MultipartFile file, String title, String description) {
		if (file.isEmpty()) {
			throw new BadRequestException("빈 파일은 업로드할 수 없습니다.");
		}
		if (file.getSize() > 20L * 1024 * 1024) {
			throw new BadRequestException("20MB 이하 PDF만 업로드할 수 있습니다.");
		}
		String originalFileName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
		if (!originalFileName.endsWith(".pdf")) {
			throw new BadRequestException("PDF 파일만 업로드할 수 있습니다.");
		}

		String materialId = "material-" + UUID.randomUUID();
		Path targetPath = materialRoot.resolve(materialId + "-" + file.getOriginalFilename());
		Material material = new Material(materialId, teacherId, title, description, targetPath.toString());
		material.startProcessing();

		try {
			Files.createDirectories(materialRoot);
			file.transferTo(targetPath);
			String extractedText = aiIntegrationService.extractMaterial(title, description, file.getOriginalFilename());
			material.markReady(extractedText.isBlank() ? buildExtractedText(title, description, file.getOriginalFilename()) : extractedText);
		} catch (IOException exception) {
			material.markFailed("파일 저장 또는 분석 준비에 실패했습니다.");
		}

		materials.put(materialId, material);
		return material;
	}

	/** 자료 ID로 조회합니다. */
	public Material getById(String materialId) {
		Material material = materials.get(materialId);
		if (material == null) {
			throw new NotFoundException("자료를 찾을 수 없습니다.");
		}
		return material;
	}

	/** 실패한 자료를 재처리합니다. */
	public Material retry(String teacherId, String materialId) {
		Material material = getOwnedMaterial(teacherId, materialId);
		if (material.getStatus() != MaterialStatus.FAILED) {
			throw new BadRequestException("실패한 자료만 재처리할 수 있습니다.");
		}
		material.startProcessing();
		material.markReady(buildExtractedText(material.getTitle(), material.getDescription(), Path.of(material.getFilePath()).getFileName().toString()));
		return material;
	}

	/** 교사 소유 자료를 검증해 반환합니다. */
	public Material getOwnedMaterial(String teacherId, String materialId) {
		Material material = getById(materialId);
		if (!material.getTeacherId().equals(teacherId)) {
			throw new NotFoundException("자료를 찾을 수 없습니다.");
		}
		return material;
	}

	private String buildExtractedText(String title, String description, String originalFileName) {
		return "자료 제목: " + title + "\n설명: " + description + "\n파일명: " + originalFileName + "\n이 텍스트는 MVP용 추출 결과입니다.";
	}
}
