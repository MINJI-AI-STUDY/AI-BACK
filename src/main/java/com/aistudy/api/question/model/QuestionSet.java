package com.aistudy.api.question.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "question_sets")
public class QuestionSet {
	@Id
	private String id;

	@Column(name = "school_id", nullable = false)
	private String schoolId;

	@Column(name = "material_id", nullable = false)
	private String materialId;

	@Column(name = "teacher_id", nullable = false)
	private String teacherId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Difficulty difficulty;

	@OneToMany(mappedBy = "questionSet", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	private List<Question> questions = new ArrayList<>();

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private QuestionSetStatus status;

	@Column(name = "distribution_code")
	private String distributionCode;

	@Column(name = "distribution_link")
	private String distributionLink;

	private LocalDateTime dueAt;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	protected QuestionSet() {
	}

	public QuestionSet(String id, String schoolId, String materialId, String teacherId, Difficulty difficulty, List<Question> questions) {
		this.id = id;
		this.schoolId = schoolId;
		this.materialId = materialId;
		this.teacherId = teacherId;
		this.difficulty = difficulty;
		this.status = QuestionSetStatus.REVIEW_REQUIRED;
		this.createdAt = LocalDateTime.now();
		questions.forEach(this::addQuestion);
	}

	@PrePersist
	void onCreate() {
		this.createdAt = LocalDateTime.now();
	}

	private void addQuestion(Question question) {
		question.attachTo(this);
		this.questions.add(question);
	}

	public String getId() { return id; }
	public String getSchoolId() { return schoolId; }
	public String getMaterialId() { return materialId; }
	public String getTeacherId() { return teacherId; }
	public Difficulty getDifficulty() { return difficulty; }
	public List<Question> getQuestions() { return questions; }
	public QuestionSetStatus getStatus() { return status; }
	public String getDistributionCode() { return distributionCode; }
	public String getDistributionLink() { return distributionLink; }
	public LocalDateTime getDueAt() { return dueAt; }
	public LocalDateTime getCreatedAt() { return createdAt; }

	public void publish(String distributionCode, String distributionLink, LocalDateTime dueAt) {
		this.status = QuestionSetStatus.PUBLISHED;
		this.distributionCode = distributionCode;
		this.distributionLink = distributionLink;
		this.dueAt = dueAt;
	}
}
