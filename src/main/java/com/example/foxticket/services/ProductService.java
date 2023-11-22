package com.example.foxticket.services;

import com.example.foxticket.dtos.ProductContainerDTO;
import com.example.foxticket.dtos.ProductUpdateRequestDTO;
import com.example.foxticket.dtos.ProductUpdateResponseDTO;
import com.example.foxticket.models.ErrorMessage;
import com.example.foxticket.models.Product;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface ProductService {
    ProductContainerDTO findAll();

    Optional<Product> findById(Long id);

    Product saveNewProduct(ProductUpdateRequestDTO productRequestDTO);

    void deleteProductById(Long id);

    boolean doesProductExist(Long id);

    ProductUpdateResponseDTO modifyProduct(Long id, ProductUpdateRequestDTO productUpdateRequestDTO);

    Product updateProduct(Product product);

    List<String> listEmptyFields(ProductUpdateRequestDTO productUpdateRequestDTO);

    ErrorMessage generateErrorMessageWhenFieldsAreEmpty(ProductUpdateRequestDTO productUpdateRequestDTO);

    String formatErrorMessage(List<String> listOfEmptyFields);

    ErrorMessage generateProductErrorMessage(String errorMessage);

    boolean isProductNameTaken(ProductUpdateRequestDTO productUpdateRequestDTO);
}