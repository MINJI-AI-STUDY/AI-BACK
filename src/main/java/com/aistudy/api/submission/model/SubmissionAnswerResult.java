package com.aistudy.api.submission.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "submission_answer_results")
public class SubmissionAnswerResult {
	@Id
	private String id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "submission_id", nullable = false)
	private Submission submission;

	@Column(name = "question_id", nullable = false)
	private String questionId;

	@Column(name = "selected_option_index", nullable = false)
	private int selectedOptionIndex;

	@Column(nullable = false)
	private boolean correct;

	@Column(columnDefinition = "text", nullable = false)
	private String explanation;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "submission_answer_result_tags", joinColumns = @JoinColumn(name = "submission_answer_result_id"))
	@OrderColumn(name = "tag_order")
	@Column(name = "tag_value", nullable = false)
	private List<String> conceptTags = new ArrayList<>();

	protected SubmissionAnswerResult() {
	}

	public SubmissionAnswerResult(String id, String questionId, int selectedOptionIndex, boolean correct, String explanation, List<String> conceptTags) {
		this.id = id;
		this.questionId = questionId;
		this.selectedOptionIndex = selectedOptionIndex;
		this.correct = correct;
		this.explanation = explanation;
		this.conceptTags = new ArrayList<>(conceptTags);
	}

	void attachTo(Submission submission) {
		this.submission = submission;
	}

	public String getId() { return id; }
	public String questionId() { return questionId; }
	public int selectedOptionIndex() { return selectedOptionIndex; }
	public boolean correct() { return correct; }
	public String explanation() { return explanation; }
	public List<String> conceptTags() { return conceptTags; }
}
