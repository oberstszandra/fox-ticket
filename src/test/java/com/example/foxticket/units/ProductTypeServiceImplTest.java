package com.example.foxticket.units;

import com.example.foxticket.dtos.ProductTypeRequestDTO;
import com.example.foxticket.dtos.ProductTypeResponseDTO;
import com.example.foxticket.models.ProductType;
import com.example.foxticket.repositories.ProductTypeRepository;
import com.example.foxticket.services.ProductTypeServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ProductTypeServiceImplTest {
    private ProductTypeRepository productTypeRepository;
    private ProductTypeServiceImpl productTypeService;

    public ProductTypeServiceImplTest() {
        this.productTypeRepository = Mockito.mock(ProductTypeRepository.class);
        this.productTypeService = new ProductTypeServiceImpl(productTypeRepository);
    }

    @Test
    public void doesProductTypeExist_whenIdExists_returnsTrue() {
        Long existingId = 1L;
        when(productTypeRepository.findById(existingId)).thenReturn(Optional.ofNullable(new ProductType()));
        assertTrue(productTypeService.doesProductTypeExist(existingId));
    }

    @Test
    public void doesProductTypeExist_whenIdNotExists_returnsFalse() {
        Long nonExistingId = 2L;
        Mockito.when(productTypeRepository.findById(nonExistingId)).thenReturn(Optional.empty());
        assertFalse(productTypeService.doesProductTypeExist(nonExistingId));
    }

    @Test
    public void requestDataIsNotIncluded_whenNameIsNull_returnsTrue() {
        ProductTypeRequestDTO productTypeRequestDTO = new ProductTypeRequestDTO(null);
        assertTrue(productTypeService.requestDataIsNotIncluded(productTypeRequestDTO));
    }

    @Test
    public void requestDataIsNotIncluded_whenNameIsBlank_returnsTrue() {
        ProductTypeRequestDTO productTypeRequestDTO = new ProductTypeRequestDTO("  ");
        assertTrue(productTypeService.requestDataIsNotIncluded(productTypeRequestDTO));
    }

    @Test
    public void requestDataIsNotIncluded_whenNameIsValid_returnsFalse() {
        ProductTypeRequestDTO productTypeRequestDTO = new ProductTypeRequestDTO("pass");
        assertFalse(productTypeService.requestDataIsNotIncluded(productTypeRequestDTO));
    }

    @Test
    public void productTypeExistByName_whenNameIsExisting_returnsTrue() {
        ProductTypeRequestDTO productTypeRequestDTO = new ProductTypeRequestDTO("pass");
        ProductType productType = new ProductType("pass");
        when(productTypeRepository.findByName(productTypeRequestDTO.getName())).thenReturn(Optional.of(productType));
        assertTrue(productTypeService.productTypeExistByName(productTypeRequestDTO));
    }

    @Test
    public void productTypeExistByName_whenNameIsNotExisting_returnsFalse() {
        ProductTypeRequestDTO productTypeRequestDTO = new ProductTypeRequestDTO("pass");
        when(productTypeRepository.findByName(productTypeRequestDTO.getName())).thenReturn(Optional.empty());
        assertFalse(productTypeService.productTypeExistByName(productTypeRequestDTO));
    }

    @Test
    public void saveNewProductTypeAndGetResponseDTO_whenProductTypeRequestDTOIsValid_returnsProductTypeResponseDTO() {
        ProductTypeRequestDTO productTypeRequestDTO = new ProductTypeRequestDTO("pass");
        ProductType savedProductType = new ProductType(productTypeRequestDTO.getName());
        ProductTypeResponseDTO productTypeResponseDTO = new ProductTypeResponseDTO(savedProductType.getId(), savedProductType.getName());
        when(productTypeRepository.save(any(ProductType.class))).thenReturn(savedProductType);
        ProductTypeResponseDTO actualProductTypeResponseDTO = productTypeService.saveNewProductTypeAndGetResponseDTO(productTypeRequestDTO);
        assertEquals(productTypeResponseDTO.getId(), actualProductTypeResponseDTO.getId());
        assertEquals(productTypeResponseDTO.getName(), actualProductTypeResponseDTO.getName());
    }
}

