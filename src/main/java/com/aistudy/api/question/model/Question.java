package com.aistudy.api.question.model;

import java.util.ArrayList;
import java.util.List;

public class Question {
	private final String id;
	private String stem;
	private List<String> options;
	private int correctOptionIndex;
	private String explanation;
	private List<String> conceptTags;
	private boolean excluded;

	public Question(String id, String stem, List<String> options, int correctOptionIndex, String explanation, List<String> conceptTags) {
		this.id = id;
		this.stem = stem;
		this.options = new ArrayList<>(options);
		this.correctOptionIndex = correctOptionIndex;
		this.explanation = explanation;
		this.conceptTags = new ArrayList<>(conceptTags);
	}

	public String getId() { return id; }
	public String getStem() { return stem; }
	public List<String> getOptions() { return options; }
	public int getCorrectOptionIndex() { return correctOptionIndex; }
	public String getExplanation() { return explanation; }
	public List<String> getConceptTags() { return conceptTags; }
	public boolean isExcluded() { return excluded; }

	/** 교사 검토 결과로 문항 내용을 갱신합니다. */
	public void update(String stem, List<String> options, int correctOptionIndex, String explanation, List<String> conceptTags, boolean excluded) {
		this.stem = stem;
		this.options = new ArrayList<>(options);
		this.correctOptionIndex = correctOptionIndex;
		this.explanation = explanation;
		this.conceptTags = new ArrayList<>(conceptTags);
		this.excluded = excluded;
	}
}
