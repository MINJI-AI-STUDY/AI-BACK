package com.aistudy.api.qa.dto;

import java.util.List;

public record QaResponse(String answer, List<String> evidenceSnippets, boolean grounded, boolean insufficientEvidence) {
}
