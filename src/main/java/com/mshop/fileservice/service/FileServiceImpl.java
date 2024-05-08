package com.mshop.fileservice.service;

import io.minio.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final MinioClient minioClient;

    @Value("${storage.minio.bucket}")
    private String bucket;

    @Override
    public String saveFileString(String base64) throws Exception {
        if (ObjectUtils.isEmpty(base64)) {
            return null;
        }
        byte[] bytes = base64.getBytes();
        try (InputStream inputStream = new ByteArrayInputStream(bytes)) {
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(LocalDate.now() + "/" + LocalTime.now().format(DateTimeFormatter.ofPattern("hh_mm_ss")))
                    .stream(inputStream, bytes.length, -1)
                    .build();
            ObjectWriteResponse response = minioClient.putObject(args);
            inputStream.close();
            return response.object();
        }
    }

    @Override
    public String readFileString(String key) throws Exception {
        if (ObjectUtils.isEmpty(key)) {
            return null;
        }
        try (InputStream stream = minioClient.getObject(
                GetObjectArgs
                        .builder()
                        .bucket(bucket)
                        .object(key)
                        .build())) {
            byte[] bytes = stream.readAllBytes();
            return new String(bytes);
        }
    }

    @Override
    public Map<String, String> readFileStrings(List<String> keys) throws Exception {
        if (ObjectUtils.isEmpty(keys)) {
            return null;
        }
        Map<String, String> map = new HashMap<>();
        for (String key : keys) {
            try (InputStream stream = minioClient.getObject(
                    GetObjectArgs
                            .builder()
                            .bucket(bucket)
                            .object(key)
                            .build())) {
                byte[] bytes = stream.readAllBytes();
                map.put(key, new String(bytes));
            }
        }
        return map;
    }

    @Override
    public void deleteFile(String key) throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucket)
                        .object(key)
                        .build()
        );
    }

}
