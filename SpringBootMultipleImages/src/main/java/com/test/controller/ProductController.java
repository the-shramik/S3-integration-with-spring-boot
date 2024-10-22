package com.test.controller;

import com.test.service.ProductService;
import com.test.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@CrossOrigin("*")
public class ProductController {

    private final ProductService productService;
    private final S3Service s3Service;

    @PostMapping("/addProduct")
    public ResponseEntity<?> addProduct(
            @RequestParam("productName") String productName,
            @RequestParam("productPrice") Double productPrice,
            @RequestParam("productImages") List<MultipartFile> productImages
    ) throws IOException {
        try {
            // Upload product with multiple images
            Map<String, Object> response = s3Service.uploadProductWithImages(productName, productPrice, productImages);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error uploading product or images: " + e.getMessage());
        }
    }

    @GetMapping("/getAllProducts")
    public ResponseEntity<?> getAllProducts() {
        return ResponseEntity.ok(productService.getProducts());
    }
}
