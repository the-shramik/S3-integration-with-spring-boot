package com.test.service;

import com.test.model.ImageDetails;
import com.test.model.Product;
import com.test.repository.IImageDetailsRepository;
import com.test.repository.IProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.core.sync.RequestBody;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    private final IProductRepository productRepository;
    private final IImageDetailsRepository imageDetailsRepository;

    public Map<String, Object> uploadProductWithImages(String productName, Double productPrice, List<MultipartFile> imageFiles) throws Exception {
        Product product = new Product();
        product.setProductName(productName);
        product.setProductPrice(productPrice);
        productRepository.save(product);

        List<String> imageUrls = new ArrayList<>();

        for (MultipartFile imageFile : imageFiles) {
            String imageName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();

            File file = convertMultipartFileToFile(imageFile);
            String bucketName = "springboot-test-0076";

            // Upload file to S3
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(imageName)
                    .build();

            PutObjectResponse response = s3Client.putObject(putObjectRequest, RequestBody.fromFile(file));

            file.delete(); // Remove temporary file after upload

            // Get S3 URL
            String imageUrl = String.format("https://%s.s3.amazonaws.com/%s", bucketName, imageName);
            imageUrls.add(imageUrl);

            // Save image details
            ImageDetails imageDetails = new ImageDetails();
            imageDetails.setImageName(imageUrl);
            imageDetails.setProduct(product);
            imageDetailsRepository.save(imageDetails);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("productId", product.getProductId());
        response.put("productName", product.getProductName());
        response.put("productPrice", product.getProductPrice());
        response.put("imageUrls", imageUrls);

        return response;
    }

    private File convertMultipartFileToFile(MultipartFile file) throws IOException {
        File convFile = new File(file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convFile)) {
            fos.write(file.getBytes());
        }
        return convFile;
    }
}
