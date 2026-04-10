package com.aistudy.api.signup.dto;

import com.aistudy.api.signup.model.SignupRequestEntity;
import com.aistudy.api.signup.model.SignupRole;
import com.aistudy.api.signup.model.SignupStatus;

public record SignupRequestResponse(String signupRequestId, String schoolId, String classroomId, String requesterName, String loginId, SignupRole role, String schoolEmail, String studentRealName, SignupStatus status, String rejectionReason, String provisionedLoginId, String provisionedTempPassword) {
	public static SignupRequestResponse from(SignupRequestEntity entity) {
		return new SignupRequestResponse(entity.getId(), entity.getSchoolId(), entity.getClassroomId(), entity.getRequesterName(), entity.getLoginId(), entity.getRole(), entity.getSchoolEmail(), entity.getStudentRealName(), entity.getStatus(), entity.getRejectionReason(), entity.getProvisionedLoginId(), entity.getProvisionedTempPassword());
	}
}
