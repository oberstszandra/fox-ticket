package com.example.foxticket.dtos;

public class CartRequestDTO {
    private Long productId;

    public CartRequestDTO() {
    }

    public CartRequestDTO(Long productId) {
        this.productId = productId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }
}