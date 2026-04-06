package com.aistudy.api.qa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record QaRequest(@NotBlank @Size(max = 500) String question) {
}
