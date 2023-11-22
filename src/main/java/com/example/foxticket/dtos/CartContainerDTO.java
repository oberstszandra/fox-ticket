package com.example.foxticket.dtos;

import java.util.ArrayList;
import java.util.List;

public class CartContainerDTO {
    private List<CartItemDTO> cart;

    public CartContainerDTO() {
        cart = new ArrayList<>();
    }

    public List<CartItemDTO> getCart() {
        return cart;
    }

    public void addCart(CartItemDTO product) {
        cart.add(product);
    }
}