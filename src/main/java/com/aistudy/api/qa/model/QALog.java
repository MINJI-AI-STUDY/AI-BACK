package com.aistudy.api.qa.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "qa_logs")
public class QALog {
	@Id
	private String id;

	@Column(name = "school_id", nullable = false)
	private String schoolId;

	@Column(name = "material_id", nullable = false)
	private String materialId;

	@Column(name = "student_id", nullable = false)
	private String studentId;

	@Column(columnDefinition = "text", nullable = false)
	private String question;

	@Column(columnDefinition = "text", nullable = false)
	private String answer;

	@Column(nullable = false)
	private boolean grounded;

	@Column(nullable = false)
	private String status;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "qa_log_evidence_snippets", joinColumns = @JoinColumn(name = "qa_log_id"))
	@OrderColumn(name = "snippet_order")
	@Column(name = "snippet_value", nullable = false)
	private List<String> evidenceSnippets = new ArrayList<>();

	protected QALog() {
	}

	public QALog(String id, String schoolId, String materialId, String studentId, String question, String answer, boolean grounded, String status, LocalDateTime createdAt, List<String> evidenceSnippets) {
		this.id = id;
		this.schoolId = schoolId;
		this.materialId = materialId;
		this.studentId = studentId;
		this.question = question;
		this.answer = answer;
		this.grounded = grounded;
		this.status = status;
		this.createdAt = createdAt;
		this.evidenceSnippets = new ArrayList<>(evidenceSnippets);
	}

	public String getId() { return id; }
	public String getSchoolId() { return schoolId; }
	public String getMaterialId() { return materialId; }
	public String getStudentId() { return studentId; }
	public String getQuestion() { return question; }
	public String getAnswer() { return answer; }
	public boolean isGrounded() { return grounded; }
	public String getStatus() { return status; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public List<String> getEvidenceSnippets() { return evidenceSnippets; }
}
