package com.aistudy.api.common;

public record ApiErrorResponse(String code, String message, boolean retryable) {
}