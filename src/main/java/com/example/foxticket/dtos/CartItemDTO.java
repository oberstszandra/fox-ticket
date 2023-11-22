package com.example.foxticket.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"productId", "name", "price"})
public class CartItemDTO {
    @JsonProperty("product_id")
    private Long productId;
    private String name;
    private Integer price;

    public CartItemDTO() {
    }

    public CartItemDTO(Long productId, String name, Integer price) {
        this.productId = productId;
        this.name = name;
        this.price = price;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }
}