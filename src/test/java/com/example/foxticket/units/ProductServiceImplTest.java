package com.example.foxticket.units;


import com.example.foxticket.dtos.ProductUpdateRequestDTO;
import com.example.foxticket.dtos.ProductUpdateResponseDTO;
import com.example.foxticket.models.ErrorMessage;
import com.example.foxticket.models.Product;
import com.example.foxticket.models.ProductType;
import com.example.foxticket.repositories.ProductRepository;
import com.example.foxticket.repositories.ProductTypeRepository;
import com.example.foxticket.services.ProductServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

public class ProductServiceImplTest {
    private ProductRepository productRepository;

    private ProductServiceImpl productService;

    private ProductTypeRepository productTypeRepository;

    public ProductServiceImplTest() {
        productRepository = Mockito.mock(ProductRepository.class);
        productTypeRepository = Mockito.mock(ProductTypeRepository.class);
        productService = new ProductServiceImpl(productRepository, productTypeRepository);
    }


    @Test
    public void findAll_whenRequestIsValid_returnsTrue() {
        List<Product> testProducts = new ArrayList<>();
        testProducts.add(new Product("Monthly pass", 9500, 30, "You can use this pass for 30 days!"));
        testProducts.add(new Product("Quarterly pass", 28500, 90, "You can use this pass for 90 days!"));
        when(productRepository.findAll()).thenReturn(testProducts);
        List<Product> actuallyExistingProducts = productRepository.findAll();
        assertNotNull(actuallyExistingProducts);
        assertEquals(testProducts.size(), actuallyExistingProducts.size());
        assertEquals(testProducts.get(0).getName(), actuallyExistingProducts.get(0).getName());
    }

    @Test
    public void findAll_whenNoElementExists_returnsEmptyList() {
        List<Product> testProducts = new ArrayList<>();
        when(productRepository.findAll()).thenReturn(testProducts);
        List<Product> actuallyExistingProducts = productRepository.findAll();
        assertNotNull(actuallyExistingProducts);
        assertEquals(0, actuallyExistingProducts.size());
    }

    @Test
    public void listEmptyFields_whenAllFieldsAreEmpty_returnsAllFieldNames() {
        ProductUpdateRequestDTO productRequestDTO = new ProductUpdateRequestDTO(null, null, null, null, null);
        List<String> emptyFields = productService.listEmptyFields(new ProductUpdateRequestDTO());
        assertEquals(5, emptyFields.size());
        assertEquals(List.of("name", "price", "duration", "description", "type ID"), emptyFields);
    }

    @Test
    public void listEmptyFields_whenSomeFieldsAreEmpty_returnsAllEmptyFields() {
        ProductUpdateRequestDTO productRequestDTO = new ProductUpdateRequestDTO(null, 9500, null, "You can use this pass for 30 days!", 1L);
        List<String> emptyFields = productService.listEmptyFields(productRequestDTO);
        assertEquals(2, emptyFields.size());
        assertEquals(List.of("name", "duration"), emptyFields);
    }

    @Test
    public void listEmptyFields_whenNoFieldsAreEmpty_returnsEmptyList() {
        ProductUpdateRequestDTO productRequestDTO = new ProductUpdateRequestDTO("Monthly pass", 9500, 30, "You can use this pass for 30 days!", 1L);
        List<String> emptyFields = productService.listEmptyFields(productRequestDTO);
        assertEquals(0, emptyFields.size());
    }

    @Test
    public void formatErrorMessage_whenAllFieldsAreEmpty_returnsCorrectlyFormattedMessage() {
        List<String> listOfEmptyFields = Arrays.asList("name", "duration", "type");
        String formattedMessage = productService.formatErrorMessage(listOfEmptyFields);
        assertEquals("Name, duration and type", formattedMessage);
    }

    @Test
    public void formatErrorMessage_whenSingleFieldIsEmpty_returnsCorrectlyFormattedMessage() {
        List<String> listOfEmptyFields = Arrays.asList("price");
        String formattedMessage = productService.formatErrorMessage(listOfEmptyFields);
        assertEquals("Price", formattedMessage);
    }

    @Test
    public void generateErrorMessageWhenFieldsAreEmpty_whenSingleFieldIsEmpty_returnsCorrectResponseEntity() {
        ProductUpdateRequestDTO productRequestDTO = new ProductUpdateRequestDTO(null, 9500, 30, "You can use this pass for 30 days!", 1L);
        ErrorMessage message = productService.generateErrorMessageWhenFieldsAreEmpty(productRequestDTO);
        assertEquals("Name is required", message.getErrorMessage());
    }

    @Test
    public void generateErrorMessageWhenFieldsAreEmpty_whenMultipleFieldsAreEmpty_returnsCorrectResponseEntity() {
        ProductUpdateRequestDTO productRequestDTO = new ProductUpdateRequestDTO(null, null, 30, null, 1L);
        ErrorMessage message = productService.generateErrorMessageWhenFieldsAreEmpty(productRequestDTO);
        assertEquals("Name, price and description are required", message.getErrorMessage());
    }

    @Test
    public void productNameIsTaken_productNameExists_returnsTrue() {
        String existingProductName = "Existing Product";
        ProductUpdateRequestDTO productRequestDTO = new ProductUpdateRequestDTO(existingProductName, 9000, 30, "You can use this pass for 30 days!", 1L);
        List<Product> mockProducts = new ArrayList<>();
        mockProducts.add(new Product(existingProductName, 10000, 15, "Existing product description"));
        when(productRepository.findAll()).thenReturn(mockProducts);
        boolean result = productService.isProductNameTaken(productRequestDTO);
        assertTrue(result);
    }

    @Test
    public void productNameIsTaken_noProducts_returnsFalse() {
        ProductUpdateRequestDTO productRequestDTO = new ProductUpdateRequestDTO("Some Product", 9000, 30, "You can use this pass for 30 days!", 1L);
        List<Product> mockProducts = new ArrayList<>();
        when(productRepository.findAll()).thenReturn(mockProducts);
        boolean result = productService.isProductNameTaken(productRequestDTO);
        assertFalse(result);
    }

    @Test
    public void modifyProduct_WithAllNecessaryInfo_ReturnsUpdatedProduct() {
        Optional<ProductType> ticket = Optional.of(new ProductType());
        Product product1 = new Product("product", 360, 24, "desc");
        ticket.get().setProducts(product1);
        ProductUpdateRequestDTO updateRequestDTO = new ProductUpdateRequestDTO("update", 500, 24, "updated product", 1L);
        when(productTypeRepository.findById(1L)).thenReturn(ticket);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));

        ProductUpdateResponseDTO responseDTO = productService.modifyProduct(1L, updateRequestDTO);

        assertEquals("update", responseDTO.getName());
        assertEquals("updated product", responseDTO.getDescription());
        assertEquals(500, responseDTO.getPrice());
    }

    @Test
    public void generateErrorMessageWhenFieldsAreEmpty_WithInvalidUpdateRequest_ReturnsCorrectErrorMessage() {
        ProductUpdateRequestDTO updateRequestDTO = new ProductUpdateRequestDTO("", 500, 24, "", 1L);
        ErrorMessage generatedError = productService.generateErrorMessageWhenFieldsAreEmpty(updateRequestDTO);

        assertEquals("Name and description are required", generatedError.getErrorMessage());
    }

    @Test
    public void deleteProductById_ValidId_ProductRepositoryDeleteByIdCalled() {
        Long productId = 1L;
        doNothing().when(productRepository).deleteById(productId);
        productService.deleteProductById(productId);
    }
}
