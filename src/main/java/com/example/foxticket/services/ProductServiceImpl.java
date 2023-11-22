package com.example.foxticket.services;

import com.example.foxticket.dtos.ProductContainerDTO;
import com.example.foxticket.dtos.ProductDTO;
import com.example.foxticket.dtos.ProductUpdateRequestDTO;
import com.example.foxticket.dtos.ProductUpdateResponseDTO;
import com.example.foxticket.models.ErrorMessage;
import com.example.foxticket.models.Product;
import com.example.foxticket.repositories.ProductRepository;
import com.example.foxticket.repositories.ProductTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static com.example.foxticket.services.ArticleServiceImpl.getMessage;

@Service
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private ProductTypeRepository productTypeRepository;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository, ProductTypeRepository productTypeRepository) {
        this.productRepository = productRepository;
        this.productTypeRepository = productTypeRepository;
    }

    @Override
    public ProductContainerDTO findAll() {
        ProductContainerDTO productContainerDTO = new ProductContainerDTO();
        for (Product product : productRepository.findAll()) {
            productContainerDTO.addProduct(new ProductDTO(product.getId(), product.getName(), product.getPrice(), product.getDuration(), product.getDescription(), product.getProductType().getName()));
        }
        return productContainerDTO;
    }

    @Override
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    public void deleteProductById(Long id) {
        productRepository.deleteById(id);
    }

    @Override
    public boolean doesProductExist(Long id) {
        return productRepository.findById(id).isPresent();
    }

    @Override
    public Product updateProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    public List<String> listEmptyFields(ProductUpdateRequestDTO productUpdateRequestDTO) {
        List<String> fieldNames = new ArrayList<>();
        if (productUpdateRequestDTO.getName() == null || productUpdateRequestDTO.getName().isBlank()) {
            fieldNames.add("name");
        }
        if (productUpdateRequestDTO.getPrice() == null) {
            fieldNames.add("price");
        }
        if (productUpdateRequestDTO.getDuration() == null) {
            fieldNames.add("duration");
        }
        if (productUpdateRequestDTO.getDescription() == null || productUpdateRequestDTO.getDescription().isBlank()) {
            fieldNames.add("description");
        }
        if (productUpdateRequestDTO.getTypeId() == null) {
            fieldNames.add("type ID");
        }
        return fieldNames;
    }

    @Override
    public ErrorMessage generateErrorMessageWhenFieldsAreEmpty(ProductUpdateRequestDTO productUpdateRequestDTO) {
        if (listEmptyFields(productUpdateRequestDTO).size() == 1) {
            String message = formatErrorMessage(listEmptyFields(productUpdateRequestDTO));
            ErrorMessage error = new ErrorMessage(message + " is required");
            return error;
        } else {
            String message = formatErrorMessage(listEmptyFields(productUpdateRequestDTO));
            ErrorMessage error = new ErrorMessage(message + " are required");
            return error;
        }
    }

    @Override
    public String formatErrorMessage(List<String> listOfEmptyFields) {

        return getMessage(listOfEmptyFields);
    }

    @Override
    public ErrorMessage generateProductErrorMessage(String errorMessage) {
        return new ErrorMessage(errorMessage);
    }

    @Override
    public boolean isProductNameTaken(ProductUpdateRequestDTO productUpdateRequestDTO) {
        boolean productNameExist = false;
        for (Product product : productRepository.findAll()) {
            if (product.getName().equals(productUpdateRequestDTO.getName())) {
                return true;
            }
        }
        return productNameExist;
    }

    @Override
    public Product saveNewProduct(ProductUpdateRequestDTO productRequestDTO) throws NumberFormatException {
        Product product = new Product(productRequestDTO.getName(), productRequestDTO.getPrice(), productRequestDTO.getDuration(), productRequestDTO.getDescription());
        return productRepository.save(product);
    }

    @Override
    public ProductUpdateResponseDTO modifyProduct(Long id, ProductUpdateRequestDTO productUpdateRequestDTO) {
        Optional<Product> selectedProduct = findById(id);
        if (selectedProduct.isPresent()) {
            if (productUpdateRequestDTO.getName() != null) {
                selectedProduct.get().setName(productUpdateRequestDTO.getName());
            }
            if (productUpdateRequestDTO.getPrice() != null) {
                selectedProduct.get().setPrice(productUpdateRequestDTO.getPrice());
            }
            if (productUpdateRequestDTO.getDuration() != null) {
                selectedProduct.get().setDuration(productUpdateRequestDTO.getDuration());
            }
            if (productUpdateRequestDTO.getDescription() != null) {
                selectedProduct.get().setDescription(productUpdateRequestDTO.getDescription());
            }
            if (productUpdateRequestDTO.getTypeId() != null) {
                selectedProduct.get().setProductType(productTypeRepository.findById(productUpdateRequestDTO.getTypeId()).get());
            }

            updateProduct(selectedProduct.get());
            ProductUpdateResponseDTO updatedProduct = new ProductUpdateResponseDTO(selectedProduct.get().getId(), selectedProduct.get().getName(), selectedProduct.get().getPrice(), selectedProduct.get().getDuration(), selectedProduct.get().getDescription(), selectedProduct.get().getProductType().getName());
            return updatedProduct;

        } else {
            throw new NoSuchElementException("Product not found");
        }
    }
}