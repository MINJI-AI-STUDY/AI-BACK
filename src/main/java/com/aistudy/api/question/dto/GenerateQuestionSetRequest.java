package com.aistudy.api.question.dto;

import com.aistudy.api.question.model.Difficulty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record GenerateQuestionSetRequest(@Min(1) @Max(10) int questionCount, @NotNull Difficulty difficulty, String materialId) {
}
