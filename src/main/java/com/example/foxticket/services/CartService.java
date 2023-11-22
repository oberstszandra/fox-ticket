package com.example.foxticket.services;

import com.example.foxticket.dtos.CartContainerDTO;
import com.example.foxticket.dtos.CartRequestDTO;
import com.example.foxticket.dtos.CartResponseDTO;
import com.example.foxticket.models.Cart;

public interface CartService {
    CartContainerDTO findAllContentOfACart(Long userId);

    void removeAllProductsOfACart(Long cartId);

    void removeProductById(Long productId, Long cartId);

    void save(Cart cart);

    boolean requestToAddProductToCartIsEmpty(CartRequestDTO cartRequestDTO);

    boolean requestToAddNonexistentProductToCart(CartRequestDTO cartRequestDTO);

    CartResponseDTO addNewProductToCartAndGetResponseDTO(Long userId, CartRequestDTO cartRequestDTO);
}
