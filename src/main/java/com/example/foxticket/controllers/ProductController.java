package com.example.foxticket.controllers;


import com.example.foxticket.dtos.ProductUpdateRequestDTO;
import com.example.foxticket.dtos.ProductUpdateResponseDTO;
import com.example.foxticket.models.ErrorMessage;
import com.example.foxticket.models.Product;
import com.example.foxticket.services.ProductService;
import com.example.foxticket.services.ProductTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api")
public class ProductController {
    private final ProductService productService;
    private final ProductTypeService productTypeService;

    @Autowired
    public ProductController(ProductService productService, ProductTypeService productTypeService) {
        this.productService = productService;
        this.productTypeService = productTypeService;
    }

    @GetMapping(path = "/products")
    public ResponseEntity<?> getProducts() {
        try {
            return new ResponseEntity<>(productService.findAll(), HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping(path = "/products")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<?> addNewProduct(@RequestBody ProductUpdateRequestDTO productRequestDTO) {
        if (!productService.listEmptyFields(productRequestDTO).isEmpty()) {
            return new ResponseEntity<>(productService.generateErrorMessageWhenFieldsAreEmpty(productRequestDTO), HttpStatus.NOT_ACCEPTABLE);
        }
        if (productService.isProductNameTaken(productRequestDTO)) {
            return new ResponseEntity<>(new ErrorMessage("Product name already exists"), HttpStatus.NOT_ACCEPTABLE);
        }
        if (!productTypeService.doesProductTypeExist(productRequestDTO.getTypeId())) {
            return new ResponseEntity<>(new ErrorMessage("Product type is wrong!"), HttpStatus.NOT_ACCEPTABLE);
        } else {
            Product product = productService.saveNewProduct(productRequestDTO);
            return new ResponseEntity<>(new ProductUpdateResponseDTO(product.getId(), product.getName(), product.getPrice(), product.getDuration(), product.getDescription(), productTypeService.getProductTypeNameById(productRequestDTO.getTypeId())), HttpStatus.CREATED);
        }
    }

    @DeleteMapping(path = "/products/{productId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity deleteProduct(@PathVariable Long productId) {
        if (productService.doesProductExist(productId)) {
            productService.deleteProductById(productId);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PutMapping(path = "/products/{productId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> modifyProduct(@PathVariable Long productId, @RequestBody ProductUpdateRequestDTO productUpdateRequestDTO) {
        if (!productService.listEmptyFields(productUpdateRequestDTO).isEmpty()) {
            return new ResponseEntity<>(productService.generateErrorMessageWhenFieldsAreEmpty(productUpdateRequestDTO), HttpStatus.BAD_REQUEST);
        }
        if (productService.isProductNameTaken(productUpdateRequestDTO)) {
            return new ResponseEntity<>(productService.generateProductErrorMessage("Product name already exists"), HttpStatus.BAD_REQUEST);
        }
        if (!productTypeService.doesProductTypeExist(productUpdateRequestDTO.getTypeId())) {
            return new ResponseEntity<>(productService.generateProductErrorMessage("Product type is wrong"), HttpStatus.BAD_REQUEST);
        } else {
            try {
                return new ResponseEntity<>(productService.modifyProduct(productId, productUpdateRequestDTO), HttpStatus.OK);
            } catch (RuntimeException e) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }
        }
    }
}
