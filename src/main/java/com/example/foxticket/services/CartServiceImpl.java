package com.example.foxticket.services;

import com.example.foxticket.dtos.CartContainerDTO;
import com.example.foxticket.dtos.CartItemDTO;
import com.example.foxticket.dtos.CartRequestDTO;
import com.example.foxticket.dtos.CartResponseDTO;
import com.example.foxticket.models.Cart;
import com.example.foxticket.models.Product;
import com.example.foxticket.models.User;
import com.example.foxticket.repositories.CartRepository;
import com.example.foxticket.repositories.ProductRepository;
import com.example.foxticket.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {
    private CartRepository cartRepository;
    private UserRepository userRepository;
    private ProductRepository productRepository;

    @Autowired
    public CartServiceImpl(CartRepository cartRepository, UserRepository userRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Override
    public CartContainerDTO findAllContentOfACart(Long userId) {
        CartContainerDTO cartContainerDTO = new CartContainerDTO();
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            Cart cart = user.get().getCart();
            for (Product product : cart.getProducts()) {
                cartContainerDTO.addCart(new CartItemDTO(product.getId(), product.getName(), product.getPrice()));
            }
        } else {
            throw new NoSuchElementException("User not existing!");
        }
        return cartContainerDTO;
    }

    @Override
    public void removeAllProductsOfACart(Long cartId) {
        Optional<Cart> cart = cartRepository.findById(cartId);
        if (cart.isPresent()) {
            cart.get().getProducts().clear();
            cartRepository.save(cart.get());
        } else {
            throw new NoSuchElementException("Cart does not exist!");
        }
    }

    @Override
    public void removeProductById(Long productId, Long cartId) {
        Optional<Cart> cart = cartRepository.findById(cartId);
        List<Product> updatedProductList = new ArrayList<>();
        if (!cart.isPresent()) {
            throw new NoSuchElementException("Cart does not exist!");
        } else {
            for (Product currentProduct : cart.get().getProducts()) {
                if (currentProduct.getId() != productId) {
                    updatedProductList.add(currentProduct);
                }
            }
            cart.get().setProducts(updatedProductList);
            cartRepository.save(cart.get());
        }
    }

    public void save(Cart cart) {
        cartRepository.save(cart);
    }

    public boolean requestToAddProductToCartIsEmpty(CartRequestDTO cartRequestDTO) {
        return cartRequestDTO.getProductId() == null || cartRequestDTO.getProductId().toString().isBlank();
    }

    @Override
    public boolean requestToAddNonexistentProductToCart(CartRequestDTO cartRequestDTO) {
        return !productRepository.findById(cartRequestDTO.getProductId()).isPresent();
    }

    @Override
    public CartResponseDTO addNewProductToCartAndGetResponseDTO(Long userId, CartRequestDTO cartRequestDTO) {
        CartResponseDTO cartResponseDTO = new CartResponseDTO();
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            Cart cart = user.get().getCart();
            cart.addProduct(productRepository.findById(cartRequestDTO.getProductId()).get());
            cartRepository.save(cart);
            cartResponseDTO.setId(cart.getId());
            cartResponseDTO.setProductId(cartRequestDTO.getProductId());
        } else {
            throw new NoSuchElementException("User not existing!");
        }
        return cartResponseDTO;
    }
}