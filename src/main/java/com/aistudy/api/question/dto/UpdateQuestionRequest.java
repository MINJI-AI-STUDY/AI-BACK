package com.aistudy.api.question.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record UpdateQuestionRequest(
	@NotBlank String stem,
	@NotEmpty @Size(min = 4, max = 4) List<String> options,
	@Min(0) @Max(3) int correctOptionIndex,
	@NotBlank String explanation,
	@NotEmpty @Size(min = 1, max = 2) List<String> conceptTags,
	boolean excluded
) {
}
