package com.example.foxticket.integrations;

import com.example.foxticket.dtos.CartRequestDTO;
import com.example.foxticket.models.Cart;
import com.example.foxticket.models.Product;
import com.example.foxticket.models.ProductType;
import com.example.foxticket.models.User;
import com.example.foxticket.repositories.CartRepository;
import com.example.foxticket.repositories.ProductTypeRepository;
import com.example.foxticket.repositories.UserRepository;
import com.example.foxticket.security.MyUserDetails;
import com.example.foxticket.security.MyUserDetailsService;
import com.example.foxticket.security.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.hamcrest.core.Is.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(value = "/db/test/clear_tables.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class CartControllerTest {
    @Autowired
    private MockMvc mockMvc;
    private CartRepository cartRepository;
    private UserRepository userRepository;
    private ProductTypeRepository productTypeRepository;
    private MyUserDetailsService myUserDetailsService;
    private JwtUtil jwtUtil;
    private ObjectMapper mapper;

    @Autowired
    public CartControllerTest(CartRepository cartRepository, UserRepository userRepository,
                              ProductTypeRepository productTypeRepository,
                              MyUserDetailsService myUserDetailsService, JwtUtil jwtUtil,
                              ObjectMapper objectMapper) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.productTypeRepository = productTypeRepository;
        this.myUserDetailsService = myUserDetailsService;
        this.jwtUtil = jwtUtil;
        this.mapper = objectMapper;
    }

    @BeforeEach
    public void setupBeforeCartControllerTests() {
        ProductType ticket = new ProductType("ticket");
        ProductType pass = new ProductType("pass");
        Product product1 = new Product("Day ticket", 360, 24, "You can use this ticket for a whole day!");
        Product product2 = new Product("2 day ticket", 700, 48, "You can use this ticket for 2 days!");
        Product product3 = new Product("Monthly pass", 9500, 30, "You can use this pass for 30 days!");
        ticket.setProducts(product1);
        ticket.setProducts(product2);
        ticket.setProducts(product3);
        productTypeRepository.save(ticket);
        productTypeRepository.save(pass);

        User user1 = new User("John Doe", "johndoe@test.com", "Zb8gh@t5", false);
        User user2 = new User("Jane Doe", "janedoe@test.com", "juzdG83&", true);
        userRepository.save(user1);
        userRepository.save(user2);

        Cart cart1 = new Cart(user1);
        Cart cart2 = new Cart(user2);
        cartRepository.save(cart2);
        cartRepository.save(cart1);

        cart1.addProduct(product1);
        cart1.addProduct(product2);
        cart2.addProduct(product2);
        cart2.addProduct(product2);
        cartRepository.save(cart2);
        cartRepository.save(cart1);
    }

    @Test
    public void getCart_WithExistingCart_ReturnsCartWithItsContent() throws Exception {
        MyUserDetails userDetails = myUserDetailsService.loadUserByUsername("johndoe@test.com");
        MockHttpServletRequestBuilder requestBuilder = get("/api/cart")
                .header("Authorization", "Bearer " + jwtUtil.generateToken(userDetails));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cart", Matchers.hasSize(2)))
                .andExpect(jsonPath("$.cart[0].name", is("Day ticket")))
                .andExpect(jsonPath("$.cart[1].name", is("2 day ticket")))
                .andExpect(jsonPath("$.cart[0].price", is(360)))
                .andExpect(jsonPath("$.cart[1].price", is(700)));
    }

    @Test
    public void addProductToCart_WithExistingProduct_ReturnsCartResponseDTO() throws Exception {
        MyUserDetails userDetails = myUserDetailsService.loadUserByUsername("johndoe@test.com");
        CartRequestDTO cartRequestDTO = new CartRequestDTO(2L);
        String cartRequestDTOString = mapper.writeValueAsString(cartRequestDTO);
        MockHttpServletRequestBuilder requestBuilder = post("/api/cart")
                .contentType(MediaType.APPLICATION_JSON)
                .content(cartRequestDTOString)
                .header("Authorization", "Bearer " + jwtUtil.generateToken(userDetails));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.productId").value(2));
    }

    @Test
    public void addProductToCart_WithNoValue_ReturnsError() throws Exception {
        MyUserDetails userDetails = myUserDetailsService.loadUserByUsername("johndoe@test.com");
        CartRequestDTO cartRequestDTO1 = new CartRequestDTO(null);
        String cartRequestDTOString1 = mapper.writeValueAsString(cartRequestDTO1);
        MockHttpServletRequestBuilder requestBuilder1 = post("/api/cart")
                .contentType(MediaType.APPLICATION_JSON)
                .content(cartRequestDTOString1)
                .header("Authorization", "Bearer " + jwtUtil.generateToken(userDetails));

        mockMvc.perform(requestBuilder1)
                .andExpect(status().isNotAcceptable())
                .andExpect(jsonPath("$.errorMessage").value("Product ID is required."));

        CartRequestDTO cartRequestDTO2 = new CartRequestDTO();
        String cartRequestDTOString2 = mapper.writeValueAsString(cartRequestDTO2);
        MockHttpServletRequestBuilder requestBuilder2 = post("/api/cart")
                .contentType(MediaType.APPLICATION_JSON)
                .content(cartRequestDTOString2)
                .header("Authorization", "Bearer " + jwtUtil.generateToken(userDetails));

        mockMvc.perform(requestBuilder2)
                .andExpect(status().isNotAcceptable())
                .andExpect(jsonPath("$.errorMessage").value("Product ID is required."));
    }

    @Test
    public void deleteProductsFromCart_withNonExistingProduct_ReturnsErrorMessage() throws Exception {
        MyUserDetails userDetails = myUserDetailsService.loadUserByUsername("johndoe@test.com");
        MockHttpServletRequestBuilder requestBuilder = delete("/api/cart/3")
                .header("Authorization", "Bearer " + jwtUtil.generateToken(userDetails));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage", is("This item is not in the cart.")));
    }

    @Test
    public void deleteProductsFromCart_withAllProducts_ReturnsMessage() throws Exception {
        MyUserDetails userDetails = myUserDetailsService.loadUserByUsername("johndoe@test.com");
        MockHttpServletRequestBuilder requestBuilder = delete("/api/cart")
                .header("Authorization", "Bearer " + jwtUtil.generateToken(userDetails));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().string("All items are successfully deleted from cart."));
    }

    @Test
    public void deleteProductsFromCart_withExistingProduct_ReturnsMessage() throws Exception {
        MyUserDetails userDetails = myUserDetailsService.loadUserByUsername("johndoe@test.com");
        MockHttpServletRequestBuilder requestBuilder = delete("/api/cart/1")
                .header("Authorization", "Bearer " + jwtUtil.generateToken(userDetails));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().string("Item successfully deleted from cart."));
    }
}