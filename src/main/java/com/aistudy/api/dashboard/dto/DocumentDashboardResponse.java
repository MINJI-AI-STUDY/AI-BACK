package com.aistudy.api.dashboard.dto;

import com.aistudy.api.material.dto.MaterialSummaryResponse;
import com.aistudy.api.qa.dto.QALogResponse;
import java.util.List;

public record DocumentDashboardResponse(
	MaterialSummaryResponse material,
	long questionSetCount,
	long questionCount,
	long submissionCount,
	long participantCount,
	double averageScore,
	long qaCount,
	List<DocumentQuestionSetSummary> generatedQuestionSets,
	List<QALogResponse> recentQaLogs
) {
}
