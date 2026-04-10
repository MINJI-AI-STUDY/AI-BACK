package com.aistudy.api.storage;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
	String store(String materialId, String originalFileName, MultipartFile file);
	StoredObject load(String storageKey, String originalFileName);
}
