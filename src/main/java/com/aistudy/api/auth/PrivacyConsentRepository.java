package com.aistudy.api.auth;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrivacyConsentRepository extends JpaRepository<PrivacyConsentEntity, String> {

	List<PrivacyConsentEntity> findByUserId(String userId);

	Optional<PrivacyConsentEntity> findByUserIdAndConsentType(String userId, String consentType);
}