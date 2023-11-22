package com.example.foxticket.services;

import com.example.foxticket.dtos.ProductTypeRequestDTO;
import com.example.foxticket.dtos.ProductTypeResponseDTO;
import com.example.foxticket.models.ProductType;
import com.example.foxticket.repositories.ProductTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductTypeServiceImpl implements ProductTypeService {
    private final ProductTypeRepository productTypeRepository;

    @Autowired
    public ProductTypeServiceImpl(ProductTypeRepository productTypeRepository) {
        this.productTypeRepository = productTypeRepository;
    }


    @Override
    public String getProductTypeNameById(Long id) {
        return productTypeRepository.getReferenceById(id).getName();
    }

    @Override
    public boolean doesProductTypeExist(Long id) {
        return productTypeRepository.findById(id).isPresent();
    }

    @Override
    public boolean requestDataIsNotIncluded(ProductTypeRequestDTO productTypeRequestDTO) {
        return productTypeRequestDTO.getName() == null || productTypeRequestDTO.getName().isBlank();
    }

    @Override
    public boolean productTypeExistByName(ProductTypeRequestDTO productTypeRequestDTO) {
        return productTypeRepository.findByName(productTypeRequestDTO.getName()).isPresent();
    }

    @Override
    public ProductTypeResponseDTO saveNewProductTypeAndGetResponseDTO(ProductTypeRequestDTO productTypeRequestDTO) {
        ProductType savedProductType = saveProductType(productTypeRequestDTO);
        return new ProductTypeResponseDTO(savedProductType.getId(), savedProductType.getName());
    }

    private ProductType saveProductType(ProductTypeRequestDTO productTypeRequestDTO) {
        ProductType productType = new ProductType(productTypeRequestDTO.getName());
        return productTypeRepository.save(productType);
    }
}
