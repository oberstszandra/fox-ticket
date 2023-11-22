package com.example.foxticket.controllers;

import com.example.foxticket.dtos.ProductTypeRequestDTO;
import com.example.foxticket.models.ErrorMessage;
import com.example.foxticket.services.ProductTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api")
public class ProductTypeController {

    private ProductTypeService productTypeService;

    @Autowired
    public ProductTypeController(ProductTypeService productTypeService) {
        this.productTypeService = productTypeService;
    }

    @PostMapping(path = "/product-types")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> addProductTypes(@RequestBody ProductTypeRequestDTO productTypeRequestDTO) {
        if (productTypeService.requestDataIsNotIncluded(productTypeRequestDTO)) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(new ErrorMessage("Name is required."));
        }
        if (productTypeService.productTypeExistByName(productTypeRequestDTO)) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(new ErrorMessage("Product type name already exists."));
        } else {
            return ResponseEntity.status(HttpStatus.CREATED).body(productTypeService.saveNewProductTypeAndGetResponseDTO(productTypeRequestDTO));
        }
    }
}
