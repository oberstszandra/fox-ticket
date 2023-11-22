package com.example.foxticket.services;

import com.example.foxticket.dtos.ProductTypeRequestDTO;
import com.example.foxticket.dtos.ProductTypeResponseDTO;
import org.springframework.stereotype.Service;

@Service
public interface ProductTypeService {
    boolean doesProductTypeExist(Long id);

    String getProductTypeNameById(Long id);

    boolean requestDataIsNotIncluded(ProductTypeRequestDTO productTypeRequestDTO);

    boolean productTypeExistByName(ProductTypeRequestDTO productTypeRequestDTO);

    ProductTypeResponseDTO saveNewProductTypeAndGetResponseDTO(ProductTypeRequestDTO productTypeRequestDTO);

}

