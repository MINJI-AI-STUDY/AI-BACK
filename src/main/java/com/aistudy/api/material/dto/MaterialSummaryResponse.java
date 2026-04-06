package com.aistudy.api.material.dto;

import com.aistudy.api.material.model.Material;
import com.aistudy.api.material.model.MaterialStatus;

public record MaterialSummaryResponse(
	String materialId,
	String title,
	String description,
	MaterialStatus status,
	String failureReason
) {
	public static MaterialSummaryResponse from(Material material) {
		return new MaterialSummaryResponse(
			material.getId(),
			material.getTitle(),
			material.getDescription(),
			material.getStatus(),
			material.getFailureReason()
		);
	}
}
