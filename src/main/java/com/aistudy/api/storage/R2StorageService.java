package com.aistudy.api.storage;

import com.aistudy.api.common.BadRequestException;
import com.aistudy.api.common.NotFoundException;
import java.io.ByteArrayInputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@ConditionalOnProperty(name = "app.storage.type", havingValue = "r2")
public class R2StorageService implements StorageService {
	private final S3Client s3Client;
	private final String bucket;

	public R2StorageService(
		@Value("${app.storage.r2.account-id}") String accountId,
		@Value("${app.storage.r2.access-key}") String accessKey,
		@Value("${app.storage.r2.secret-key}") String secretKey,
		@Value("${app.storage.r2.bucket}") String bucket
	) {
		this.bucket = bucket;
		this.s3Client = S3Client.builder()
			.region(Region.of("auto"))
			.endpointOverride(java.net.URI.create("https://" + accountId + ".r2.cloudflarestorage.com"))
			.credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
			.forcePathStyle(true)
			.build();
	}

	@Override
	public String store(String materialId, String originalFileName, MultipartFile file) {
		String key = "materials/" + materialId + "/" + originalFileName;
		try {
			s3Client.putObject(
				PutObjectRequest.builder()
					.bucket(bucket)
					.key(key)
					.contentType(file.getContentType())
					.build(),
				RequestBody.fromBytes(file.getBytes())
			);
			return key;
		} catch (Exception exception) {
			throw new BadRequestException("R2 파일 저장에 실패했습니다.");
		}
	}

	@Override
	public StoredObject load(String storageKey, String originalFileName) {
		try {
			ResponseInputStream<GetObjectResponse> objectStream = s3Client.getObject(
				GetObjectRequest.builder().bucket(bucket).key(storageKey).build()
			);
			byte[] bytes = objectStream.readAllBytes();
			return new StoredObject(new InputStreamResource(new ByteArrayInputStream(bytes)), originalFileName, bytes.length);
		} catch (NoSuchKeyException exception) {
			throw new NotFoundException("자료 파일을 찾을 수 없습니다.");
		} catch (Exception exception) {
			throw new NotFoundException("자료 파일을 찾을 수 없습니다.");
		}
	}
}
