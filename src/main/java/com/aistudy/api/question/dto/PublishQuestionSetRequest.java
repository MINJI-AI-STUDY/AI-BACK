package com.aistudy.api.question.dto;

import java.time.LocalDateTime;

public record PublishQuestionSetRequest(LocalDateTime dueAt) {
}
