package com.aistudy.api.storage;

import org.springframework.core.io.Resource;

public record StoredObject(Resource resource, String fileName, long contentLength) {
}
