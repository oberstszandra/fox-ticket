package com.example.foxticket.controllers;

import com.example.foxticket.dtos.CartRequestDTO;
import com.example.foxticket.models.ErrorMessage;
import com.example.foxticket.models.Product;
import com.example.foxticket.models.User;
import com.example.foxticket.security.MyUserDetails;
import com.example.foxticket.services.CartService;
import com.example.foxticket.services.ProductService;
import com.example.foxticket.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping(path = "/api")
public class CartController {
    private CartService cartService;
    private ProductService productService;
    private UserService userService;

    @Autowired
    public CartController(CartService cartService, UserService userService, ProductService productService) {
        this.cartService = cartService;
        this.userService = userService;
        this.productService = productService;
    }

    @GetMapping(path = "/cart")
    public ResponseEntity<?> getCart(@AuthenticationPrincipal MyUserDetails myUserDetails) {
        try {
            return new ResponseEntity<>(cartService.findAllContentOfACart(myUserDetails.getId()), HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping(path = "/cart/{itemId}")
    public ResponseEntity<?> deleteItemFromCartById(@PathVariable Long itemId, @AuthenticationPrincipal MyUserDetails myUserDetails) {
        Optional<User> user = userService.findById(myUserDetails.getId());
        Optional<Product> product = productService.findById(itemId);
        Long cartId = user.get().getCart().getId();
        if (!product.isPresent() || !user.get().getCart().getProducts().contains(product.get())) {
            return new ResponseEntity<>(new ErrorMessage("This item is not in the cart."), HttpStatus.BAD_REQUEST);
        }
        if (productService.findById(itemId).isEmpty()) {
            return new ResponseEntity<>(new ErrorMessage("Please provide a valid id!"), HttpStatus.BAD_REQUEST);
        } else {
            cartService.removeProductById(itemId, cartId);
            return new ResponseEntity<>("Item successfully deleted from cart.", HttpStatus.OK);
        }
    }

    @DeleteMapping(path = "/cart")
    public ResponseEntity<String> deleteAllItemsFromCart(@AuthenticationPrincipal MyUserDetails myUserDetails) {
        Optional<User> user = userService.findById(myUserDetails.getId());
        Long cartId = user.get().getCart().getId();
        cartService.removeAllProductsOfACart(cartId);
        return new ResponseEntity<>("All items are successfully deleted from cart.", HttpStatus.OK);
    }

    @PostMapping(path = "/cart")
    public ResponseEntity<?> addProductToCart(@AuthenticationPrincipal MyUserDetails myUserDetails, @RequestBody CartRequestDTO cartRequestDTO) {
        if (cartService.requestToAddProductToCartIsEmpty(cartRequestDTO)) {
            return new ResponseEntity<>(new ErrorMessage("Product ID is required."), HttpStatus.NOT_ACCEPTABLE);
        }
        if (cartService.requestToAddNonexistentProductToCart(cartRequestDTO)) {
            return new ResponseEntity<>(new ErrorMessage("Product doesn't exists."), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(cartService.addNewProductToCartAndGetResponseDTO(myUserDetails.getId(), cartRequestDTO), HttpStatus.OK);
    }
}