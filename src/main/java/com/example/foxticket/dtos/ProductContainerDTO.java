package com.example.foxticket.dtos;

import java.util.ArrayList;
import java.util.List;

public class ProductContainerDTO {
    private List<ProductDTO> products;

    public ProductContainerDTO() {
        products = new ArrayList<>();
    }

    public List<ProductDTO> getProducts() {
        return products;
    }

    public void addProduct(ProductDTO product) {
        products.add(product);
    }

    public ProductDTO getProductByIndex(Integer index) {
        return products.get(index);
    }
}