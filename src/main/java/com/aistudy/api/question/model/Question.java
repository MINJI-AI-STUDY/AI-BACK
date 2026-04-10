package com.aistudy.api.question.model;

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
@Table(name = "questions")
public class Question {
	@Id
	private String id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "question_set_id", nullable = false)
	private QuestionSet questionSet;

	@Column(columnDefinition = "text", nullable = false)
	private String stem;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "question_options", joinColumns = @JoinColumn(name = "question_id"))
	@OrderColumn(name = "option_order")
	@Column(name = "option_value", nullable = false)
	private List<String> options = new ArrayList<>();

	@Column(name = "correct_option_index", nullable = false)
	private int correctOptionIndex;

	@Column(columnDefinition = "text", nullable = false)
	private String explanation;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "question_concept_tags", joinColumns = @JoinColumn(name = "question_id"))
	@OrderColumn(name = "tag_order")
	@Column(name = "tag_value", nullable = false)
	private List<String> conceptTags = new ArrayList<>();

	@Column(nullable = false)
	private boolean excluded;

	protected Question() {
	}

	public Question(String id, String stem, List<String> options, int correctOptionIndex, String explanation, List<String> conceptTags) {
		this.id = id;
		this.stem = stem;
		this.options = new ArrayList<>(options);
		this.correctOptionIndex = correctOptionIndex;
		this.explanation = explanation;
		this.conceptTags = new ArrayList<>(conceptTags);
	}

	void attachTo(QuestionSet questionSet) {
		this.questionSet = questionSet;
	}

	public String getId() { return id; }
	public String getStem() { return stem; }
	public List<String> getOptions() { return options; }
	public int getCorrectOptionIndex() { return correctOptionIndex; }
	public String getExplanation() { return explanation; }
	public List<String> getConceptTags() { return conceptTags; }
	public boolean isExcluded() { return excluded; }

	public void update(String stem, List<String> options, int correctOptionIndex, String explanation, List<String> conceptTags, boolean excluded) {
		this.stem = stem;
		this.options = new ArrayList<>(options);
		this.correctOptionIndex = correctOptionIndex;
		this.explanation = explanation;
		this.conceptTags = new ArrayList<>(conceptTags);
		this.excluded = excluded;
	}
}
