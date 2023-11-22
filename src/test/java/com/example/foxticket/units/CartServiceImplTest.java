package com.example.foxticket.units;

import com.example.foxticket.dtos.CartContainerDTO;
import com.example.foxticket.dtos.CartRequestDTO;
import com.example.foxticket.dtos.CartResponseDTO;
import com.example.foxticket.models.Cart;
import com.example.foxticket.models.Product;
import com.example.foxticket.models.ProductType;
import com.example.foxticket.models.User;
import com.example.foxticket.repositories.CartRepository;
import com.example.foxticket.repositories.ProductRepository;
import com.example.foxticket.repositories.UserRepository;
import com.example.foxticket.services.CartServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class CartServiceImplTest {
    private UserRepository userRepository;
    private ProductRepository productRepository;
    private CartRepository cartRepository;
    private CartServiceImpl cartService;

    public CartServiceImplTest() {
        productRepository = Mockito.mock(ProductRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        cartRepository = Mockito.mock(CartRepository.class);
        cartService = new CartServiceImpl(cartRepository, userRepository, productRepository);
    }

    @Test
    public void findAllContentOfACart_WithValidNotEmptyCart_ReturnsTrue() {
        List<Product> testProducts = new ArrayList<>();
        testProducts.add(new Product("Monthly pass", 9500, 30, "You can use this pass for 30 days!"));
        ProductType testProductType = new ProductType("pass");
        testProducts.get(0).setProductType(testProductType);
        User testUser = new User("Nagy Béla", "belanagy@gmail.com", "Wert5z@ghko", false);
        List<Cart> testCarts = new ArrayList<>();
        testCarts.add(new Cart(testUser));
        testCarts.get(0).addProduct(testProducts.get(0));
        testUser.setCart(testCarts.get(0));

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        CartContainerDTO actuallyExistingCart = cartService.findAllContentOfACart(testUser.getId());
        assertThat(actuallyExistingCart).isNotNull();
        assertThat(actuallyExistingCart.getCart().size()).isEqualTo(1);
        assertEquals(actuallyExistingCart.getCart().get(0).getName(), testProducts.get(0).getName());
    }

    @Test
    public void findAllContentOfACart_WithNoElement_ReturnsEmptyList() {
        User testUser = new User("Nagy Béla", "belanagy@gmail.com", "Wert5z@ghko", false);
        List<Cart> testCarts = new ArrayList<>();
        testCarts.add(new Cart(testUser));
        testUser.setCart(testCarts.get(0));

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        CartContainerDTO actuallyExistingCart = cartService.findAllContentOfACart(testUser.getId());
        assertEquals(0, actuallyExistingCart.getCart().size());
    }

    @Test
    public void removeAllProductsOfACart_whenCartHadTwoProducts_Success() {
        Cart testCart = new Cart();
        Product testProduct1 = new Product("testName1", 1, 1, "testDescription1");
        Product testProduct2 = new Product("testName2", 2, 2, "testDescription2");
        List<Product> productList = new ArrayList<>();
        productList.add(testProduct1);
        productList.add(testProduct2);
        testCart.setProducts(productList);

        when(cartRepository.findById(testCart.getId())).thenReturn(Optional.of(testCart));

        cartService.removeAllProductsOfACart(testCart.getId());
        assertEquals(0, testCart.getProducts().size());
    }

    @Test
    public void removeProductFromCart_byId_Success() {
        Cart testCart = new Cart();
        testCart.setId(1L);
        Product testProduct1 = new Product("testName1", 1, 1, "testDescription1");
        Product testProduct2 = new Product("testName2", 2, 2, "testDescription2");
        testProduct1.setId(1L);
        testProduct2.setId(2L);
        productRepository.save(testProduct1);
        productRepository.save(testProduct2);
        cartRepository.save(testCart);
        testCart.addProduct(testProduct1);
        testCart.addProduct(testProduct2);

        when(cartRepository.findById(testCart.getId())).thenReturn(Optional.of(testCart));

        cartService.removeProductById(testProduct1.getId(), testCart.getId());
        assertEquals(1, testCart.getProducts().size());
    }

    @Test
    public void requestToAddProductToCartIsEmpty_WhenProductIsNull_ReturnsTrue() {
        CartRequestDTO cartRequestDTO = new CartRequestDTO(null);
        assertTrue(cartService.requestToAddProductToCartIsEmpty(cartRequestDTO));
    }

    @Test
    public void requestToAddProductToCartIsEmpty_WhenProductIsEmpty_ReturnsTrue() {
        CartRequestDTO cartRequestDTO = new CartRequestDTO();
        assertTrue(cartService.requestToAddProductToCartIsEmpty(cartRequestDTO));
    }

    @Test
    public void requestToAddNonexistentProductToCart_WhenProductIdIsNotExist_ReturnsTrue() {
        CartRequestDTO cartRequestDTO = new CartRequestDTO(5L);
        assertTrue(cartService.requestToAddNonexistentProductToCart(cartRequestDTO));
    }

    @Test
    public void addNewProductToCartAndGetResponseDTO_WhenCartRequestDTOIsValid_ReturnsCartResponseDTO() {
        List<Product> testProducts = new ArrayList<>();
        testProducts.add(new Product("Monthly pass", 9500, 30, "You can use this pass for 30 days!"));
        ProductType testProductType = new ProductType("pass");
        testProducts.get(0).setProductType(testProductType);
        testProducts.get(0).setId(1L);
        User testUser = new User("Nagy Béla", "belanagy@gmail.com", "Wert5z@ghko", false);
        testUser.setId(1L);
        List<Cart> testCarts = new ArrayList<>();
        testCarts.add(new Cart(testUser));
        testCarts.get(0).addProduct(testProducts.get(0));
        testUser.setCart(testCarts.get(0));
        CartRequestDTO cartRequestDTO = new CartRequestDTO(testProducts.get(0).getId());

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(productRepository.findById(testUser.getId())).thenReturn(Optional.of(testProducts.get(0)));

        CartResponseDTO cartResponseDTO = cartService.addNewProductToCartAndGetResponseDTO(testUser.getId(), cartRequestDTO);
        assertThat(cartResponseDTO).isNotNull();
        assertEquals(cartResponseDTO.getId(), testCarts.get(0).getId());
        assertEquals(cartResponseDTO.getProductId(), testProducts.get(0).getId());
    }
}