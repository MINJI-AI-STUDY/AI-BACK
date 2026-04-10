package com.aistudy.api.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateClassroomRequest(@NotBlank String name, Integer grade) {
}
