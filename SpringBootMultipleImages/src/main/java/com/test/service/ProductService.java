package com.test.service;

import com.test.model.ImageDetails;
import com.test.model.Product;
import com.test.model.dto.ProductDto;
import com.test.repository.IImageDetailsRepository;
import com.test.repository.IProductRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final IProductRepository productRepository;
    private final IImageDetailsRepository imageDetailsRepository;

    @Value("${image.upload.dir}")
    String imageUploadDir;

    public List<ProductDto> getProducts() {
        List<ProductDto> products = new ArrayList<>();
        productRepository.findAll().forEach(product -> {

            List<String> imageBase64s = new ArrayList<>();
            ProductDto productDto = new ProductDto();

            productDto.setProductId(product.getProductId());
            productDto.setProductName(product.getProductName());
            productDto.setProductPrice(product.getProductPrice());

            product.getImageDetails().forEach(imageDetail -> {
                String imageName = imageDetail.getImageName();
                String path_dir = imageUploadDir + File.separator + imageName;

                try (FileInputStream imageStream = new FileInputStream(path_dir)) {
                    byte[] imageBytes = IOUtils.toByteArray(imageStream);
                    String imageBase64 = Base64.getEncoder().encodeToString(imageBytes);
                    imageBase64s.add(imageBase64);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            productDto.setImageBase64(imageBase64s);
            products.add(productDto);
        });

        return products;
    }
}
