package com.aistudy.api.signup.dto;

import com.aistudy.api.signup.model.SchoolMasterEntity;

public record SchoolMasterResponse(String schoolId, String officialSchoolCode, String name, String schoolLevel, String region, String emailDomain) {
	public static SchoolMasterResponse from(SchoolMasterEntity entity) {
		return new SchoolMasterResponse(entity.getId(), entity.getOfficialSchoolCode(), entity.getName(), entity.getSchoolLevel(), entity.getRegion(), entity.getEmailDomain());
	}
}
