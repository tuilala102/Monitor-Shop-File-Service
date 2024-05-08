package com.mshop.fileservice.config;

import com.mshop.fileservice.model.Product;
import com.mshop.fileservice.model.User;
import com.mshop.fileservice.repository.ProductRepository;
import com.mshop.fileservice.repository.UserRepository;
import io.minio.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class StorageConfig {

    private final MinioClient minioClient;

    private final UserRepository userRepository;

    private final ProductRepository productRepository;


    @Value("${storage.minio.bucket}")
    private String bucket;

    @PostConstruct
    private void initStorage() throws Exception {
        createBucket();
        migrateFile();
    }

    private void createBucket() throws Exception {
        boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!bucketExists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }
    }

    private void migrateFile() throws Exception {
        migrateUserImage();
        migrateProductImage();
    }

    private void migrateUserImage() throws Exception {
        List<User> users = userRepository.findAll();
        if (ObjectUtils.isEmpty(users)) {
            return;
        }
        List<User> updatedUsers = new ArrayList<>();
        for (User user : users) {
            if (user.getImage() != null && user.getImage().startsWith("data:image")) {
                String key = saveFile(user.getImage());
                user.setImage(key);
                updatedUsers.add(user);
            }
        }
        if (ObjectUtils.isNotEmpty(updatedUsers)) {
            userRepository.saveAll(updatedUsers);
        }
    }

    private void migrateProductImage() throws Exception {
        List<Product> products = productRepository.findAll();
        if (ObjectUtils.isEmpty(products)) {
            return;
        }
        List<Product> updatedProducts = new ArrayList<>();
        for (Product product : products) {
            if (product.getImage() != null && product.getImage().startsWith("data:image")) {
                String key = saveFile(product.getImage());
                product.setImage(key);
                updatedProducts.add(product);
            }
        }
        if (ObjectUtils.isNotEmpty(updatedProducts)) {
            productRepository.saveAll(updatedProducts);
        }
    }

    private String saveFile(String base64) throws Exception {
        byte[] bytes = base64.getBytes();
        try (InputStream inputStream = new ByteArrayInputStream(bytes)) {
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(LocalDate.now() + "/" + LocalTime.now().format(DateTimeFormatter.ofPattern("hh_mm_ss_SSS")))
                    .stream(inputStream, bytes.length, -1)
                    .build();
            ObjectWriteResponse response = minioClient.putObject(args);
            inputStream.close();
            return response.object();
        }
    }


}
