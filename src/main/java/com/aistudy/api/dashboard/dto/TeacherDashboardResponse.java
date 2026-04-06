package com.aistudy.api.dashboard.dto;

import java.util.List;

public record TeacherDashboardResponse(List<TeacherStudentScore> studentScores, List<QuestionAccuracy> questionAccuracy, List<WeakConceptTag> weakConceptTags) {
}
