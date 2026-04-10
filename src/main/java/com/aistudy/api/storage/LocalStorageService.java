package com.aistudy.api.storage;

import com.aistudy.api.common.BadRequestException;
import com.aistudy.api.common.NotFoundException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@ConditionalOnProperty(name = "app.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalStorageService implements StorageService {
	private final Path materialRoot;

	public LocalStorageService(@Value("${app.storage.material-root:data/materials}") String materialRoot) {
		this.materialRoot = Paths.get(materialRoot);
	}

	@Override
	public String store(String materialId, String originalFileName, MultipartFile file) {
		try {
			Files.createDirectories(materialRoot);
			Path targetPath = materialRoot.resolve(materialId + "-" + originalFileName);
			file.transferTo(targetPath);
			return targetPath.toString();
		} catch (Exception exception) {
			throw new BadRequestException("파일 저장에 실패했습니다.");
		}
	}

	@Override
	public StoredObject load(String storageKey, String originalFileName) {
		try {
			Path filePath = Path.of(storageKey);
			Resource resource = new UrlResource(filePath.toUri());
			if (!resource.exists()) {
				throw new NotFoundException("자료 파일을 찾을 수 없습니다.");
			}
			return new StoredObject(resource, originalFileName, Files.size(filePath));
		} catch (MalformedURLException exception) {
			throw new NotFoundException("자료 파일을 찾을 수 없습니다.");
		} catch (Exception exception) {
			throw new NotFoundException("자료 파일을 찾을 수 없습니다.");
		}
	}
}
