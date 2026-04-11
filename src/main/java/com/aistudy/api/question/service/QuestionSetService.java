package com.aistudy.api.question.service;

import com.aistudy.api.common.BadRequestException;
import com.aistudy.api.common.integration.AiIntegrationService;
import com.aistudy.api.common.NotFoundException;
import com.aistudy.api.material.model.Material;
import com.aistudy.api.material.model.MaterialStatus;
import com.aistudy.api.material.service.MaterialService;
import com.aistudy.api.question.dto.GenerateQuestionSetRequest;
import com.aistudy.api.question.dto.UpdateQuestionRequest;
import com.aistudy.api.question.model.Question;
import com.aistudy.api.question.model.QuestionSet;
import com.aistudy.api.question.model.QuestionSetStatus;
import com.aistudy.api.question.repository.QuestionSetRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuestionSetService {
	private final QuestionSetRepository questionSetRepository;
	private final MaterialService materialService;
	private final AiIntegrationService aiIntegrationService;
	private final String frontendBaseUrl;

	public QuestionSetService(QuestionSetRepository questionSetRepository, MaterialService materialService, AiIntegrationService aiIntegrationService, @Value("${app.frontend.base-url}") String frontendBaseUrl) {
		this.questionSetRepository = questionSetRepository;
		this.materialService = materialService;
		this.aiIntegrationService = aiIntegrationService;
		this.frontendBaseUrl = frontendBaseUrl;
	}

	/** 문제 세트 생성 — 교사 소유 자료와 학교 소속을 검증한 뒤 문항 초안을 저장합니다. */
	@Transactional
	public QuestionSet generate(String teacherId, String schoolId, String materialId, GenerateQuestionSetRequest request) {
		Material material = materialService.getOwnedMaterial(teacherId, materialId);
		if (!material.getSchoolId().equals(schoolId)) {
			throw new NotFoundException("자료를 찾을 수 없습니다.");
		}
		if (material.getStatus() != MaterialStatus.READY) {
			throw new BadRequestException("분석 완료된 자료만 문제를 생성할 수 있습니다.");
		}
		List<Question> questions = createQuestions(material, request.questionCount());
		if (questions.size() != request.questionCount()) {
			throw new BadRequestException("요청한 문항 수와 생성 결과 수가 일치하지 않습니다.");
		}
		QuestionSet questionSet = new QuestionSet(UUID.randomUUID().toString(), schoolId, materialId, teacherId, request.difficulty(), questions);
		return questionSetRepository.save(questionSet);
	}

	/** 문항 수정 — 교사 본인이 생성한 학교 범위의 문제 세트만 수정할 수 있습니다. */
	@Transactional
	public QuestionSet updateQuestion(String teacherId, String schoolId, String questionSetId, String questionId, UpdateQuestionRequest request) {
		QuestionSet questionSet = getTeacherEditableQuestionSet(teacherId, schoolId, questionSetId);
		Question question = questionSet.getQuestions().stream()
			.filter(candidate -> candidate.getId().equals(questionId))
			.findFirst()
			.orElseThrow(() -> new NotFoundException("문항을 찾을 수 없습니다."));
		question.update(request.stem(), request.options(), request.correctOptionIndex(), request.explanation(), request.conceptTags(), request.excluded());
		return questionSetRepository.save(questionSet);
	}

	/** 문제 세트 배포 — 제외되지 않은 문항이 하나 이상 있을 때만 배포 코드를 발급합니다. */
	@Transactional
	public QuestionSet publish(String teacherId, String schoolId, String questionSetId, LocalDateTime dueAt) {
		QuestionSet questionSet = getTeacherEditableQuestionSet(teacherId, schoolId, questionSetId);
		boolean hasPublishableQuestion = questionSet.getQuestions().stream().anyMatch(question -> !question.isExcluded());
		if (!hasPublishableQuestion) {
			throw new BadRequestException("배포 가능한 문항이 없습니다.");
		}
		String distributionCode = questionSet.getId().substring(Math.max(0, questionSet.getId().length() - 6)).toUpperCase();
		questionSet.publish(distributionCode, frontendBaseUrl + "/student/question-sets/" + distributionCode + "/workspace", dueAt);
		return questionSetRepository.save(questionSet);
	}

	@Transactional(readOnly = true)
	public QuestionSet getById(String questionSetId) {
		return questionSetRepository.findById(questionSetId)
			.orElseThrow(() -> new NotFoundException("문제 세트를 찾을 수 없습니다."));
	}

	@Transactional(readOnly = true)
	public QuestionSet getSchoolScopedQuestionSet(String schoolId, String questionSetId) {
		QuestionSet questionSet = getById(questionSetId);
		if (!questionSet.getSchoolId().equals(schoolId)) {
			throw new NotFoundException("문제 세트를 찾을 수 없습니다.");
		}
		return questionSet;
	}

	@Transactional(readOnly = true)
	public List<QuestionSet> getByMaterial(String schoolId, String materialId) {
		return questionSetRepository.findByMaterialIdAndSchoolIdOrderByCreatedAtDesc(materialId, schoolId);
	}

	@Transactional(readOnly = true)
	public long countByMaterial(String schoolId, String materialId) {
		return questionSetRepository.countByMaterialIdAndSchoolId(materialId, schoolId);
	}

	@Transactional(readOnly = true)
	public QuestionSet getPublishedByCode(String distributionCode) {
		return questionSetRepository.findByDistributionCodeAndStatus(distributionCode, QuestionSetStatus.PUBLISHED)
			.orElseThrow(() -> new NotFoundException("문제 세트를 찾을 수 없습니다."));
	}

	/** 학교 범위 내 문제 세트 목록을 반환합니다. */
	@Transactional(readOnly = true)
	public List<QuestionSet> getBySchoolId(String schoolId) {
		return questionSetRepository.findBySchoolId(schoolId);
	}

	@Transactional(readOnly = true)
	public List<QuestionSet> getAll() {
		return questionSetRepository.findAll();
	}

	private QuestionSet getTeacherEditableQuestionSet(String teacherId, String schoolId, String questionSetId) {
		QuestionSet questionSet = getSchoolScopedQuestionSet(schoolId, questionSetId);
		if (!questionSet.getTeacherId().equals(teacherId)) {
			throw new NotFoundException("문제 세트를 찾을 수 없습니다.");
		}
		return questionSet;
	}

	private List<Question> createQuestions(Material material, int questionCount) {
		List<Map<String, Object>> generated = aiIntegrationService.generateQuestions(material.getTitle(), material.getExtractedText(), questionCount);
		List<Question> questions = new ArrayList<>();
		for (Map<String, Object> item : generated) {
			Object options = item.get("options");
			Object conceptTags = item.get("conceptTags");
			if (!(options instanceof List<?> optionList) || !(conceptTags instanceof List<?> tagList)) {
				continue;
			}
			questions.add(new Question(
				"question-" + UUID.randomUUID(),
				String.valueOf(item.getOrDefault("stem", material.getTitle() + " 핵심 개념 확인 문제")),
				optionList.stream().map(String::valueOf).limit(4).toList(),
				Integer.parseInt(String.valueOf(item.getOrDefault("correctOptionIndex", 0))),
				String.valueOf(item.getOrDefault("explanation", material.getTitle() + " 본문 기반 해설")),
				tagList.stream().map(String::valueOf).limit(2).toList()
			));
		}
		if (questions.size() == questionCount) {
			return questions;
		}
		questions = new ArrayList<>();
		List<String> keywords = extractKeywords(material.getExtractedText(), questionCount);
		for (int index = 1; index <= questionCount; index++) {
			String keyword = keywords.size() >= index ? keywords.get(index - 1) : material.getTitle();
			questions.add(new Question(
				"question-" + UUID.randomUUID(),
				material.getTitle() + " 자료에서 '" + keyword + "'와 가장 관련된 내용을 고르세요.",
				List.of(keyword, "오답 후보 A", "오답 후보 B", "오답 후보 C"),
				0,
				material.getTitle() + " 본문에서 추출한 핵심어를 기준으로 생성한 해설입니다.",
				List.of(keyword)
			));
		}
		return questions;
	}

	private List<String> extractKeywords(String materialText, int questionCount) {
		Pattern pattern = Pattern.compile("[가-힣A-Za-z0-9·]{2,}");
		Matcher matcher = pattern.matcher(materialText == null ? "" : materialText);
		List<String> keywords = new ArrayList<>();
		while (matcher.find() && keywords.size() < Math.max(questionCount, 3)) {
			String word = matcher.group();
			if (!keywords.contains(word)) {
				keywords.add(word);
			}
		}
		return keywords;
	}
}
