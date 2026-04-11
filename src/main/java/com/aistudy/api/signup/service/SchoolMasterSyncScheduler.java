package com.aistudy.api.signup.service;

import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SchoolMasterSyncScheduler {
	private static final Logger log = LoggerFactory.getLogger(SchoolMasterSyncScheduler.class);

	private final SchoolMasterSyncService schoolMasterSyncService;
	private final boolean syncEnabled;

	public SchoolMasterSyncScheduler(
		SchoolMasterSyncService schoolMasterSyncService,
		@Value("${app.school-api.sync-enabled:true}") boolean syncEnabled
	) {
		this.schoolMasterSyncService = schoolMasterSyncService;
		this.syncEnabled = syncEnabled;
	}

	/** 서버 시작 직후 학교 마스터 동기화를 1회 수행합니다. */
	@EventListener(ApplicationReadyEvent.class)
	public void syncOnStartup() {
		CompletableFuture.runAsync(() -> runSync("startup"));
	}

	/** 매월 1일 새벽에 학교 마스터를 재동기화합니다. */
	@Scheduled(cron = "${app.school-api.sync-cron:0 0 3 1 * *}")
	public void syncMonthly() {
		runSync("monthly");
	}

	private void runSync(String trigger) {
		if (!syncEnabled) {
			log.info("학교 마스터 자동 동기화가 비활성화되어 {} 실행을 건너뜁니다.", trigger);
			return;
		}
		try {
			var result = schoolMasterSyncService.syncAll();
			log.info("학교 마스터 {} 동기화 완료 imported={}, updated={}, total={}", trigger, result.importedCount(), result.updatedCount(), result.totalCount());
		} catch (Exception exception) {
			log.warn("학교 마스터 {} 동기화 실패: {}", trigger, exception.getMessage());
		}
	}
}
