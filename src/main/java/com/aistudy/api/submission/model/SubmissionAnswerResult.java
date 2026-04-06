package com.aistudy.api.submission.model;

import java.util.List;

public record SubmissionAnswerResult(String questionId, int selectedOptionIndex, boolean correct, String explanation, List<String> conceptTags) {
}
