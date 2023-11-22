package com.example.foxticket.integrations;

import com.example.foxticket.models.*;
import com.example.foxticket.repositories.TicketOrderRepository;
import com.example.foxticket.repositories.UserRepository;
import com.example.foxticket.security.MyUserDetails;
import com.example.foxticket.security.MyUserDetailsService;
import com.example.foxticket.security.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(value = "/db/test/clear_tables.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class TicketOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;
    private JwtUtil jwtUtil;
    private MyUserDetailsService myUserDetailsService;
    private UserRepository userRepository;
    private ObjectMapper mapper;
    private TicketOrderRepository ticketOrderRepository;

    @Autowired
    public TicketOrderControllerTest(JwtUtil jwtUtil, MyUserDetailsService myUserDetailsService, UserRepository userRepository, ObjectMapper objectMapper, TicketOrderRepository ticketOrderRepository) {
        this.mapper = objectMapper;
        this.jwtUtil = jwtUtil;
        this.myUserDetailsService = myUserDetailsService;
        this.userRepository = userRepository;
        this.ticketOrderRepository = ticketOrderRepository;
    }

    @Test
    public void getPurchases_WithExistingPurchases_ReturnsAllTicketOrders() throws Exception {
        Optional<User> user = Optional.of(new User("TestUser", "valaki@gmail.com", "cicamica", true));
        Product product = new Product();
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime yesterday = today.minusDays(1);
        TicketOrder ticketOrder1 = new TicketOrder("active", today, product, user.get());
        TicketOrder ticketOrder2 = new TicketOrder("not active", yesterday, product, user.get());
        product.addOrder(ticketOrder1);
        product.addOrder(ticketOrder2);
        user.get().addOrder(ticketOrder1);
        user.get().addOrder(ticketOrder2);
        userRepository.save(user.get());
        MyUserDetails userDetails = myUserDetailsService.loadUserByUsername("valaki@gmail.com");
        MockHttpServletRequestBuilder requestBuilder = get("/api/orders")
                .header("Authorization", "Bearer " + jwtUtil.generateToken(userDetails));
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders", Matchers.hasSize(2)));
    }

    @Test
    public void getPurchases_WithoutExistingPurchases_ReturnsEmptyContainer() throws Exception {
        Optional<User> user = Optional.of(new User("TestUser", "valaki@gmail.com", "cicamica", true));
        userRepository.save(user.get());
        MyUserDetails userDetails = myUserDetailsService.loadUserByUsername("valaki@gmail.com");
        MockHttpServletRequestBuilder requestBuilder = get("/api/orders")
                .header("Authorization", "Bearer " + jwtUtil.generateToken(userDetails));
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders", Matchers.hasSize(0)));
    }

    @Test
    public void createNewOrdersByCart_WithExistingOrdersAndUserIsVerified_ReturnsAllOrders() throws Exception {
        Optional<User> user = Optional.of(new User("TestUser", "valaki@gmail.com", "cicamica", true));
        Cart cart = new Cart(user.get());
        Verification verification = new Verification();
        verification.setVerified(true);
        user.get().setCart(cart);
        user.get().addVerification(verification);
        List<Product> productList = new ArrayList<>();
        Product product1 = new Product();
        Product product2 = new Product();
        productList.add(product1);
        productList.add(product2);
        cart.setProducts(productList);
        userRepository.save(user.get());
        MyUserDetails userDetails = myUserDetailsService.loadUserByUsername("valaki@gmail.com");
        MockHttpServletRequestBuilder requestBuilder = post("/api/orders")
                .header("Authorization", "Bearer " + jwtUtil.generateToken(userDetails));
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders", Matchers.hasSize(2)));
    }

    @Test
    public void createNewOrdersByCart_WithoutExistingOrdersAndUserIsVerified_ReturnsEmptyContainer() throws Exception {
        Optional<User> user = Optional.of(new User("TestUser", "valaki@gmail.com", "cicamica", true));
        Verification verification = new Verification();
        verification.setVerified(true);
        Cart cart = new Cart(user.get());
        user.get().setCart(cart);
        user.get().addVerification(verification);
        userRepository.save(user.get());
        MyUserDetails userDetails = myUserDetailsService.loadUserByUsername("valaki@gmail.com");
        MockHttpServletRequestBuilder requestBuilder = post("/api/orders")
                .header("Authorization", "Bearer " + jwtUtil.generateToken(userDetails));
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders", Matchers.hasSize(0)));
    }

    @Test
    public void createNewOrdersByCart_WhenUserIsNotVerified_ReturnsCorrectErrorMessage() throws Exception {
        Optional<User> user = Optional.of(new User("TestUser", "valaki@gmail.com", "cicamica", true));
        Verification verification = new Verification();
        Cart cart = new Cart(user.get());
        user.get().setCart(cart);
        user.get().addVerification(verification);
        userRepository.save(user.get());
        MyUserDetails userDetails = myUserDetailsService.loadUserByUsername("valaki@gmail.com");
        MockHttpServletRequestBuilder requestBuilder = post("/api/orders")
                .header("Authorization", "Bearer " + jwtUtil.generateToken(userDetails));
        mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value("Please verify your email before your purchase!"));
    }

    @Test
    public void activatePurchasedItem_WhenOrderIdIsNotValid_returnsCorrectErrorMessage() throws Exception {
        User testUser = new User("TestUser", "test@gmail.com", new BCryptPasswordEncoder().encode("test"), true);
        Product testProduct = new Product("Day ticket", 360, 24, "You can use this ticket for a whole day!");
        TicketOrder testTicketOrder = new TicketOrder();
        testTicketOrder.setProduct(testProduct);
        testTicketOrder.setUser(testUser);
        ticketOrderRepository.save(testTicketOrder);
        MyUserDetails userDetails = myUserDetailsService.loadUserByUsername("test@gmail.com");
        String userDetailsString = mapper.writeValueAsString(userDetails);
        MockHttpServletRequestBuilder requestBuilder = patch("/api/orders/m")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userDetailsString)
                .header("Authorization", "Bearer " + jwtUtil.generateToken(userDetails));
        mockMvc.perform(requestBuilder)
                .andExpect(status().isNotAcceptable())
                .andExpect(jsonPath("$.errorMessage").value("This is not a valid id, you should use number!"));
    }

    @Test
    public void activatePurchasedItem_WhenOrderIdIsValidButAuthenticatedUserDoesntHaveTheOrder_returnsCorrectErrorMessage() throws Exception {
        User testUser = new User("TestUser", "test@gmail.com", new BCryptPasswordEncoder().encode("test"), true);
        userRepository.save(testUser);
        User testUser1 = new User("TestUser1", "test1@gmail.com", new BCryptPasswordEncoder().encode("test"), true);
        Product testProduct = new Product("Day ticket", 360, 24, "You can use this ticket for a whole day!");
        TicketOrder testTicketOrder = new TicketOrder();
        testTicketOrder.setProduct(testProduct);
        testTicketOrder.setUser(testUser1);
        ticketOrderRepository.save(testTicketOrder);
        MyUserDetails userDetails = myUserDetailsService.loadUserByUsername("test@gmail.com");
        String userDetailsString = mapper.writeValueAsString(userDetails);
        MockHttpServletRequestBuilder requestBuilder = patch("/api/orders/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userDetailsString)
                .header("Authorization", "Bearer " + jwtUtil.generateToken(userDetails));
        mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value("This is not your order!"));
    }

    @Test
    public void activatePurchasedItem_WhenOrderIdIsValidAndUserHasTheOrderAndOrderIsNotActivated_returnsActivatedOrderResponse() throws Exception {
        User testUser = new User("TestUser", "test@gmail.com", new BCryptPasswordEncoder().encode("test"), true);
        Product testProduct = new Product("Day ticket", 360, 24, "You can use this ticket for a whole day!");
        TicketOrder testTicketOrder = new TicketOrder();
        testTicketOrder.setProduct(testProduct);
        testTicketOrder.setUser(testUser);
        ticketOrderRepository.save(testTicketOrder);
        MyUserDetails userDetails = myUserDetailsService.loadUserByUsername("test@gmail.com");
        String userDetailsString = mapper.writeValueAsString(userDetails);
        MockHttpServletRequestBuilder requestBuilder = patch("/api/orders/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userDetailsString)
                .header("Authorization", "Bearer " + jwtUtil.generateToken(userDetails));
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testTicketOrder.getId()))
                .andExpect(jsonPath("$.status").value("active"))
                .andExpect(jsonPath("$.expiry").exists())
                .andExpect(jsonPath("$.product_id").value(testProduct.getId()));
    }

    @Test
    public void activatePurchasedItem_WhenOrderIdIsValidButOrderIsAlreadyActivated_returnsCorrectErrorMessage() throws Exception {
        User testUser = new User("TestUser", "test@gmail.com", new BCryptPasswordEncoder().encode("test"), true);
        Product testProduct = new Product("Day ticket", 360, 24, "You can use this ticket for a whole day!");
        TicketOrder testTicketOrder = new TicketOrder();
        testTicketOrder.setProduct(testProduct);
        testTicketOrder.setUser(testUser);
        testTicketOrder.setStatus("active");
        ticketOrderRepository.save(testTicketOrder);
        MyUserDetails userDetails = myUserDetailsService.loadUserByUsername("test@gmail.com");
        String userDetailsString = mapper.writeValueAsString(userDetails);
        MockHttpServletRequestBuilder requestBuilder = patch("/api/orders/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userDetailsString)
                .header("Authorization", "Bearer " + jwtUtil.generateToken(userDetails));
        mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value("This order is already active!"));
    }
}