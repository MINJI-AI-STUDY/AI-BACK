package com.aistudy.api.auth;

import com.aistudy.api.common.BadRequestException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PrivacyService {
	private static final String PRIVACY_NOTICE = "privacy_notice";

	private final PrivacyConsentRepository privacyConsentRepository;

	public PrivacyService(PrivacyConsentRepository privacyConsentRepository) {
		this.privacyConsentRepository = privacyConsentRepository;
	}

	/** 사용자의 모든 개인정보 동의 상태를 조회합니다. */
	public List<PrivacyConsentResponse> getConsents(String userId) {
		return privacyConsentRepository.findByUserId(userId)
			.stream()
			.map(PrivacyConsentResponse::from)
			.toList();
	}

	/** 특정 동의 항목의 상태를 조회합니다. 없으면 미동의로 반환합니다. */
	public PrivacyConsentResponse getConsent(String userId, String consentType) {
		validateConsentType(consentType);
		return privacyConsentRepository.findByUserIdAndConsentType(userId, consentType)
			.map(PrivacyConsentResponse::from)
			.orElseGet(() -> new PrivacyConsentResponse(consentType, false, null, null));
	}

	/** 개인정보 동의 상태를 기록하거나 업데이트합니다. */
	@Transactional
	public PrivacyConsentResponse recordConsent(String userId, String consentType, boolean consented) {
		validateConsentType(consentType);
		var existingConsent = privacyConsentRepository.findByUserIdAndConsentType(userId, consentType);
		PrivacyConsentEntity entity = existingConsent
			.orElseGet(() -> new PrivacyConsentEntity(userId, consentType, consented));

		existingConsent.ifPresent(ignored -> entity.updateConsent(consented));

		PrivacyConsentEntity saved = privacyConsentRepository.save(entity);
		return PrivacyConsentResponse.from(saved);
	}

	private void validateConsentType(String consentType) {
		if (!PRIVACY_NOTICE.equals(consentType)) {
			throw new BadRequestException("지원하지 않는 개인정보 동의 항목입니다.");
		}
	}
}
