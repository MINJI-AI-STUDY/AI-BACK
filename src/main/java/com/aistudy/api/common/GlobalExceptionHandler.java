package com.aistudy.api.common;

import com.aistudy.api.auth.AuthException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	/** 인증 예외를 공통 응답 형식으로 변환합니다. */
	@ExceptionHandler(AuthException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public ApiErrorResponse handleAuthException(AuthException exception) {
		return new ApiErrorResponse("AUTH_UNAUTHORIZED", exception.getMessage(), false);
	}

	/** 잘못된 요청을 공통 응답 형식으로 변환합니다. */
	@ExceptionHandler(BadRequestException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiErrorResponse handleBadRequest(BadRequestException exception) {
		return new ApiErrorResponse("BAD_REQUEST", exception.getMessage(), false);
	}

	/** 권한 부족 예외를 공통 응답 형식으로 변환합니다. */
	@ExceptionHandler(ForbiddenException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public ApiErrorResponse handleForbidden(ForbiddenException exception) {
		return new ApiErrorResponse("FORBIDDEN", exception.getMessage(), false);
	}

	/** 조회 실패 예외를 공통 응답 형식으로 변환합니다. */
	@ExceptionHandler(NotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ApiErrorResponse handleNotFound(NotFoundException exception) {
		return new ApiErrorResponse("NOT_FOUND", exception.getMessage(), false);
	}

	/** 검증 실패를 간단한 메시지로 변환합니다. */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Map<String, Object> handleValidation(MethodArgumentNotValidException exception) {
		String message = exception.getBindingResult().getFieldErrors().stream()
			.findFirst()
			.map(error -> error.getField() + ": " + error.getDefaultMessage())
			.orElse("요청 값이 올바르지 않습니다.");
		return Map.of("code", "VALIDATION_ERROR", "message", message, "retryable", false);
	}
}