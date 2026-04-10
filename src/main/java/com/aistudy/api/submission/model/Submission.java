package com.aistudy.api.submission.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "submissions")
public class Submission {
	@Id
	private String id;

	@Column(name = "school_id", nullable = false)
	private String schoolId;

	@Column(name = "material_id", nullable = false)
	private String materialId;

	@Column(name = "question_set_id", nullable = false)
	private String questionSetId;

	@Column(name = "student_id", nullable = false)
	private String studentId;

	@Column(nullable = false)
	private int score;

	@Column(name = "submitted_at", nullable = false)
	private LocalDateTime submittedAt;

	@OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	private List<SubmissionAnswerResult> questionResults = new ArrayList<>();

	protected Submission() {
	}

	public Submission(String id, String schoolId, String materialId, String questionSetId, String studentId, int score, LocalDateTime submittedAt, List<SubmissionAnswerResult> questionResults) {
		this.id = id;
		this.schoolId = schoolId;
		this.materialId = materialId;
		this.questionSetId = questionSetId;
		this.studentId = studentId;
		this.score = score;
		this.submittedAt = submittedAt;
		questionResults.forEach(this::addQuestionResult);
	}

	private void addQuestionResult(SubmissionAnswerResult result) {
		result.attachTo(this);
		this.questionResults.add(result);
	}

	public String getId() { return id; }
	public String getSchoolId() { return schoolId; }
	public String getMaterialId() { return materialId; }
	public String getQuestionSetId() { return questionSetId; }
	public String getStudentId() { return studentId; }
	public int getScore() { return score; }
	public LocalDateTime getSubmittedAt() { return submittedAt; }
	public List<SubmissionAnswerResult> getQuestionResults() { return questionResults; }
}
