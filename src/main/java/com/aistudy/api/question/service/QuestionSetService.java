package com.aistudy.api.question.service;

import com.aistudy.api.common.BadRequestException;
import com.aistudy.api.common.NotFoundException;
import com.aistudy.api.material.model.Material;
import com.aistudy.api.material.model.MaterialStatus;
import com.aistudy.api.material.service.MaterialService;
import com.aistudy.api.question.dto.GenerateQuestionSetRequest;
import com.aistudy.api.question.dto.UpdateQuestionRequest;
import com.aistudy.api.question.model.Question;
import com.aistudy.api.question.model.QuestionSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class QuestionSetService {
	private final Map<String, QuestionSet> questionSets = new ConcurrentHashMap<>();
	private final MaterialService materialService;

	public QuestionSetService(MaterialService materialService) {
		this.materialService = materialService;
	}

	/** 자료를 기준으로 검토용 문제 세트를 생성합니다. */
	public QuestionSet generate(String teacherId, String materialId, GenerateQuestionSetRequest request) {
		Material material = materialService.getOwnedMaterial(teacherId, materialId);
		if (material.getStatus() != MaterialStatus.READY) {
			throw new BadRequestException("분석 완료된 자료만 문제를 생성할 수 있습니다.");
		}
		List<Question> questions = createQuestions(material, request.questionCount());
		if (questions.size() != request.questionCount()) {
			throw new BadRequestException("요청한 문항 수와 생성 결과 수가 일치하지 않습니다.");
		}
		String questionSetId = "qset-" + UUID.randomUUID();
		QuestionSet questionSet = new QuestionSet(questionSetId, materialId, teacherId, request.difficulty(), questions);
		questionSets.put(questionSetId, questionSet);
		return questionSet;
	}

	/** 교사 검토로 문항을 수정합니다. */
	public QuestionSet updateQuestion(String teacherId, String questionSetId, String questionId, UpdateQuestionRequest request) {
		QuestionSet questionSet = getOwnedQuestionSet(teacherId, questionSetId);
		Question question = questionSet.getQuestions().stream().filter(candidate -> candidate.getId().equals(questionId)).findFirst().orElseThrow(() -> new NotFoundException("문항을 찾을 수 없습니다."));
		question.update(request.stem(), request.options(), request.correctOptionIndex(), request.explanation(), request.conceptTags(), request.excluded());
		return questionSet;
	}

	/** 검토 완료된 문제 세트를 배포합니다. */
	public QuestionSet publish(String teacherId, String questionSetId, LocalDateTime dueAt) {
		QuestionSet questionSet = getOwnedQuestionSet(teacherId, questionSetId);
		boolean hasPublishableQuestion = questionSet.getQuestions().stream().anyMatch(question -> !question.isExcluded());
		if (!hasPublishableQuestion) {
			throw new BadRequestException("배포 가능한 문항이 없습니다.");
		}
		String distributionCode = questionSet.getId().substring(Math.max(0, questionSet.getId().length() - 6)).toUpperCase();
		questionSet.publish(distributionCode, "http://localhost:5173/student/question-sets/" + distributionCode, dueAt);
		return questionSet;
	}

	/** 문제 세트 ID로 조회합니다. */
	public QuestionSet getById(String questionSetId) {
		QuestionSet questionSet = questionSets.get(questionSetId);
		if (questionSet == null) {
			throw new NotFoundException("문제 세트를 찾을 수 없습니다.");
		}
		return questionSet;
	}

	private QuestionSet getOwnedQuestionSet(String teacherId, String questionSetId) {
		QuestionSet questionSet = getById(questionSetId);
		if (!questionSet.getTeacherId().equals(teacherId)) {
			throw new NotFoundException("문제 세트를 찾을 수 없습니다.");
		}
		return questionSet;
	}

	private List<Question> createQuestions(Material material, int questionCount) {
		List<Question> questions = new ArrayList<>();
		for (int index = 1; index <= questionCount; index++) {
			questions.add(new Question(
				"question-" + UUID.randomUUID(),
				material.getTitle() + " 핵심 개념 확인 문제 " + index,
				List.of("정답 후보 " + index, "오답 후보 A", "오답 후보 B", "오답 후보 C"),
				0,
				material.getTitle() + "의 핵심 개념을 확인하기 위한 해설입니다.",
				List.of("핵심개념" + index)
			));
		}
		return questions;
	}
}
