package com.aistudy.api.material.service;

import com.aistudy.api.common.BadRequestException;
import com.aistudy.api.common.NotFoundException;
import com.aistudy.api.channel.service.ChannelService;
import com.aistudy.api.common.integration.AiIntegrationService;
import com.aistudy.api.material.model.Material;
import com.aistudy.api.material.model.MaterialStatus;
import com.aistudy.api.material.repository.MaterialRepository;
import com.aistudy.api.storage.StorageService;
import com.aistudy.api.storage.StoredObject;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.UUID;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MaterialService {

	private final MaterialRepository materialRepository;
	private final StorageService storageService;
	private final AiIntegrationService aiIntegrationService;
	private final ChannelService channelService;

	public MaterialService(
		MaterialRepository materialRepository,
		StorageService storageService,
		AiIntegrationService aiIntegrationService,
		ChannelService channelService
	) {
		this.materialRepository = materialRepository;
		this.storageService = storageService;
		this.aiIntegrationService = aiIntegrationService;
		this.channelService = channelService;
	}

	@Transactional
	public Material create(String teacherId, String schoolId, String channelId, MultipartFile file, String title, String description) {
		String resolvedChannelId = (channelId == null || channelId.isBlank())
			? channelService.defaultChannel(schoolId).getId()
			: channelService.get(schoolId, channelId).getId();
		if (file.isEmpty()) {
			throw new BadRequestException("빈 파일은 업로드할 수 없습니다.");
		}
		if (file.getSize() > 20L * 1024 * 1024) {
			throw new BadRequestException("20MB 이하 PDF만 업로드할 수 있습니다.");
		}
		String originalFileName = file.getOriginalFilename() == null ? "document.pdf" : file.getOriginalFilename();
		if (!originalFileName.toLowerCase().endsWith(".pdf")) {
			throw new BadRequestException("PDF 파일만 업로드할 수 있습니다.");
		}

		String materialId = UUID.randomUUID().toString();
		Long nextDocNo = materialRepository.findTopBySchoolIdOrderByDocNoDesc(schoolId)
			.map(existing -> existing.getDocNo() + 1)
			.orElse(1L);
		String storageKey = "pending://" + materialId;
		Material material = new Material(materialId, schoolId, resolvedChannelId, teacherId, nextDocNo, title, description, originalFileName, storageKey);
		material.startProcessing();
		materialRepository.save(material);

		try {
			String storedKey = storageService.store(materialId, originalFileName, file);
			material.updateFilePath(storedKey);
			byte[] pdfBytes = file.getBytes();
			String extractedText = extractPdfText(pdfBytes);
			if (extractedText.isBlank()) {
				extractedText = aiIntegrationService.extractMaterial(title, description, originalFileName, pdfBytes);
			}
			material.markReady(extractedText.isBlank() ? buildExtractedText(title, description, originalFileName) : extractedText);
		} catch (Exception exception) {
			material.markFailed("파일 저장 또는 분석 준비에 실패했습니다.");
		}

		return materialRepository.save(material);
	}

	@Transactional(readOnly = true)
	public StoredObject loadStoredObject(Material material) {
		return storageService.load(material.getFilePath(), material.getOriginalFileName());
	}

	@Transactional(readOnly = true)
	public Material getById(String materialId) {
		return materialRepository.findById(materialId)
			.orElseThrow(() -> new NotFoundException("자료를 찾을 수 없습니다."));
	}

	@Transactional(readOnly = true)
	public Material getOwnedMaterial(String teacherId, String materialId) {
		return materialRepository.findByIdAndTeacherId(materialId, teacherId)
			.orElseThrow(() -> new NotFoundException("자료를 찾을 수 없습니다."));
	}

	@Transactional(readOnly = true)
	public Material getSchoolMaterial(String schoolId, String materialId) {
		return materialRepository.findByIdAndSchoolId(materialId, schoolId)
			.orElseThrow(() -> new NotFoundException("자료를 찾을 수 없습니다."));
	}

	@Transactional(readOnly = true)
	public List<Material> getSchoolMaterials(String schoolId) {
		return materialRepository.findBySchoolIdOrderByCreatedAtDesc(schoolId);
	}

	@Transactional(readOnly = true)
	public List<Material> getReadySchoolMaterials(String schoolId) {
		return materialRepository.findBySchoolIdAndStatusOrderByCreatedAtDesc(schoolId, MaterialStatus.READY);
	}

	@Transactional(readOnly = true)
	public List<Material> getReadyChannelMaterials(String schoolId, String channelId) {
		channelService.get(schoolId, channelId);
		return materialRepository.findByChannelIdAndStatusOrderByCreatedAtDesc(channelId, MaterialStatus.READY);
	}

	@Transactional
	public Material retry(String teacherId, String materialId) {
		Material material = getOwnedMaterial(teacherId, materialId);
		if (material.getStatus() != MaterialStatus.FAILED) {
			throw new BadRequestException("실패한 자료만 재처리할 수 있습니다.");
		}
		material.startProcessing();
		material.markReady(buildExtractedText(material.getTitle(), material.getDescription(), material.getOriginalFileName()));
		return materialRepository.save(material);
	}

	private String buildExtractedText(String title, String description, String originalFileName) {
		return "자료 제목: " + title + "\n설명: " + description + "\n파일명: " + originalFileName + "\n이 텍스트는 MVP용 추출 결과입니다.";
	}

	private String extractPdfText(byte[] pdfBytes) {
		try (PDDocument document = Loader.loadPDF(new ByteArrayInputStream(pdfBytes).readAllBytes())) {
			String text = new PDFTextStripper().getText(document);
			return text == null ? "" : text.trim();
		} catch (Exception exception) {
			return "";
		}
	}
}
