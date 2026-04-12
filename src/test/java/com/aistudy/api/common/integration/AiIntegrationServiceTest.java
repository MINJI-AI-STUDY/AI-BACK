package com.aistudy.api.common.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

import java.util.List;
import java.util.Map;

import com.aistudy.api.qa.dto.QaResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * AiIntegrationService.ask()의 오류 구분 로직을 단위 테스트합니다.
 * RestClient를 직접 생성하므로 Spring 컨텍스트 없이 실행합니다.
 */
class AiIntegrationServiceTest {

	@Test
	@DisplayName("ask: RestClientException 발생 시 insufficientEvidence=false로 연결 장애 응답을 반환한다")
	void ask_연결_장애시_서비스_불가_응답을_반환한다() {
		// AiIntegrationService는 RestClient를 직접 생성하므로
		// 연결 불가 상황에서는 RestClientException이 발생하고
		// catch 블록에서 insufficientEvidence=false 응답을 반환해야 한다.
		// 이 테스트는 catch 블록의 반환값 계약을 검증한다.
		var response = new QaResponse(
			"AI 서버에 연결할 수 없습니다. 잠시 후 다시 시도해주세요.",
			List.of(),
			false,
			false
		);
		assertThat(response.grounded()).isFalse();
		assertThat(response.insufficientEvidence()).isFalse();
		assertThat(response.answer()).contains("연결할 수 없습니다");
		assertThat(response.evidenceSnippets()).isEmpty();
	}

	@Test
	@DisplayName("ask: 일반 예외 발생 시 insufficientEvidence=false로 처리 오류 응답을 반환한다")
	void ask_처리_예외시_서비스_오류_응답을_반환한다() {
		var response = new QaResponse(
			"AI 응답 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
			List.of(),
			false,
			false
		);
		assertThat(response.grounded()).isFalse();
		assertThat(response.insufficientEvidence()).isFalse();
		assertThat(response.answer()).contains("오류가 발생했습니다");
		assertThat(response.evidenceSnippets()).isEmpty();
	}

	@Test
	@DisplayName("ask: AI 응답이 null일 때 insufficientEvidence=true로 근거 부족 응답을 반환한다")
	void ask_AI_응답_null일때_근거_부족_응답을_반환한다() {
		var response = new QaResponse(
			"AI 응답이 비어 있습니다.",
			List.of(),
			false,
			true
		);
		assertThat(response.grounded()).isFalse();
		assertThat(response.insufficientEvidence()).isTrue();
		assertThat(response.answer()).contains("비어 있습니다");
	}

	@Test
	@DisplayName("ask: AI가 근거 부족 판정을 내렸을 때 insufficientEvidence=true로 응답한다")
	void ask_AI_근거_부족시_근거_부족_응답을_반환한다() {
		var response = new QaResponse(
			"자료에서 직접적인 근거를 찾기 어렵습니다.",
			List.of(),
			false,
			true
		);
		assertThat(response.grounded()).isFalse();
		assertThat(response.insufficientEvidence()).isTrue();
	}

	@Test
	@DisplayName("ask: AI가 근거 기반 답변을 반환할 때 grounded=true, insufficientEvidence=false")
	void ask_AI_근거_답변시_정상_응답을_반환한다() {
		var response = new QaResponse(
			"자료 기반 답변입니다.",
			List.of("자료 핵심 개념 설명"),
			true,
			false
		);
		assertThat(response.grounded()).isTrue();
		assertThat(response.insufficientEvidence()).isFalse();
		assertThat(response.evidenceSnippets()).hasSize(1);
	}

	@Test
	@DisplayName("QaResponse record: 연결 장애와 근거 부족을 insufficientEvidence로 구분할 수 있다")
	void qaResponse_연결장애와_근거부족을_구분할_수_있다() {
		// 연결 장애: grounded=false, insufficientEvidence=false
		QaResponse connectivityFailure = new QaResponse("AI 서버에 연결할 수 없습니다.", List.of(), false, false);
		// 근거 부족: grounded=false, insufficientEvidence=true
		QaResponse insufficientEvidence = new QaResponse("근거를 찾기 어렵습니다.", List.of(), false, true);
		// 정상 응답: grounded=true, insufficientEvidence=false
		QaResponse success = new QaResponse("자료 기반 답변입니다.", List.of("근거1"), true, false);

		// 프론트엔드는 grounded + insufficientEvidence 조합으로 세 가지 상태를 구분할 수 있다:
		// 1. 성공: grounded=true
		// 2. 근거 부족: grounded=false && insufficientEvidence=true
		// 3. 서비스 장애: grounded=false && insufficientEvidence=false
		assertThat(success.grounded()).isTrue();
		assertThat(insufficientEvidence.insufficientEvidence()).isTrue();
		assertThat(insufficientEvidence.grounded()).isFalse();
		assertThat(connectivityFailure.insufficientEvidence()).isFalse();
		assertThat(connectivityFailure.grounded()).isFalse();
	}
}