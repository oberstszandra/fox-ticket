package com.example.foxticket.units;

import com.example.foxticket.dtos.TicketOrderContainerDTO;
import com.example.foxticket.dtos.TicketOrderDTO;
import com.example.foxticket.models.Cart;
import com.example.foxticket.models.Product;
import com.example.foxticket.models.TicketOrder;
import com.example.foxticket.models.User;
import com.example.foxticket.repositories.CartRepository;
import com.example.foxticket.repositories.TicketOrderRepository;
import com.example.foxticket.services.CartService;
import com.example.foxticket.services.EmailService;
import com.example.foxticket.services.TicketOrderServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

public class TicketOrderServiceImplTest {
    private TicketOrderServiceImpl ticketOrderService;
    private TicketOrderRepository ticketOrderRepository;
    private CartRepository cartRepository;
    private CartService cartService;
    private EmailService emailService;
    private Environment env;

    public TicketOrderServiceImplTest() {
        this.ticketOrderRepository = Mockito.mock(TicketOrderRepository.class);
        this.cartRepository = Mockito.mock(CartRepository.class);
        this.cartService = Mockito.mock(CartService.class);
        this.emailService = Mockito.mock(EmailService.class);
        this.env = Mockito.mock(Environment.class);
        this.ticketOrderService = new TicketOrderServiceImpl(cartService, ticketOrderRepository, emailService, env);
    }

    @Test
    public void getTicketOrderDTOsFromUser_WithExistingTicketOrders_ReturnsTicketOrdersByUser() {
        User user = new User();
        Product product = new Product();
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime yesterday = today.minusDays(1);
        TicketOrder ticketOrder1 = new TicketOrder("active", today, product, user);
        TicketOrder ticketOrder2 = new TicketOrder("not active", yesterday, product, user);
        product.addOrder(ticketOrder1);
        product.addOrder(ticketOrder2);
        user.addOrder(ticketOrder1);
        user.addOrder(ticketOrder2);
        TicketOrderContainerDTO container = ticketOrderService.getTicketOrderDTOsFromUser(user);
        assertEquals(2, container.getOrders().size());
        assertEquals("active", container.getOrders().get(0).getStatus());
        assertEquals("not active", container.getOrders().get(1).getStatus());
        assertEquals(today, container.getOrders().get(0).getExpiry());
        assertEquals(yesterday, container.getOrders().get(1).getExpiry());
    }

    @Test
    public void getTicketOrderDTOsFromUser_WithoutTicketOrders_ReturnsEmptyTicketOrderDTOList() {
        User user = new User();
        TicketOrderContainerDTO container = ticketOrderService.getTicketOrderDTOsFromUser(user);
        assertEquals(0, container.getOrders().size());
    }

    @Test
    public void getTicketOrderDTOsFromCart_WithExistingCartItems_CreateTicketOrdersAndEmptiesCart() throws IOException {
        User user = new User();
        Cart cart = new Cart(user);
        user.setCart(cart);
        List<Product> productList = new ArrayList<>();
        Product product = new Product("day ticket", 450, 24, "desc");
        productList.add(product);
        cart.setProducts(productList);
        TicketOrder ticketOrder = new TicketOrder(product, user);
        when(ticketOrderRepository.save(any(TicketOrder.class))).thenReturn(ticketOrder);
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        TicketOrderContainerDTO ticketOrderContainerDTO = ticketOrderService.getTicketOrderDTOsFromCart(user);
        assertEquals(1, ticketOrderContainerDTO.getOrders().size());
        assertEquals(null, ticketOrderContainerDTO.getOrders().get(0).getExpiry());
        assertEquals("not active", ticketOrderContainerDTO.getOrders().get(0).getStatus());
        assertEquals(0, cart.getProducts().size());
    }

    @Test
    public void checkIfUserHasTheProvidedOrder_whenUserDoesntHaveTheOrder_returnsFalse() {
        User testUser = new User("TestUser", "test@gmail.com", new BCryptPasswordEncoder().encode("test"), true);
        testUser.setId(1L);
        User testUser1 = new User("TestUser1", "test1@gmail.com", new BCryptPasswordEncoder().encode("test"), true);
        testUser1.setId(2L);
        TicketOrder testTicketOrder = new TicketOrder();
        testTicketOrder.setUser(testUser1);
        when(ticketOrderRepository.findById(testTicketOrder.getId())).thenReturn(Optional.of(testTicketOrder));
        assertFalse(ticketOrderService.checkIfUserHasTheProvidedOrder(testUser.getId(), testTicketOrder.getId()));
    }

    @Test
    public void checkIfUserHasTheProvidedOrder_whenOrderNotExist_returnsFalse() {
        User testUser = new User("TestUser", "test@gmail.com", new BCryptPasswordEncoder().encode("test"), true);
        testUser.setId(1L);
        TicketOrder testTicketOrder = new TicketOrder();
        testTicketOrder.setUser(testUser);
        when(ticketOrderRepository.findById(testTicketOrder.getId())).thenReturn(Optional.empty());
        assertFalse(ticketOrderService.checkIfUserHasTheProvidedOrder(testUser.getId(), testTicketOrder.getId()));
    }

    @Test
    public void checkIfUserHasTheProvidedOrder_whenUserHasTheOrder_returnsTrue() {
        User testUser = new User("TestUser", "test@gmail.com", new BCryptPasswordEncoder().encode("test"), true);
        testUser.setId(1L);
        TicketOrder testTicketOrder = new TicketOrder();
        testTicketOrder.setUser(testUser);
        when(ticketOrderRepository.findById(testTicketOrder.getId())).thenReturn(Optional.of(testTicketOrder));
        assertTrue(ticketOrderService.checkIfUserHasTheProvidedOrder(testUser.getId(), testTicketOrder.getId()));
    }

    @Test
    public void checkOrderIsActiveOrNot_whenOrderIsNotActive_returnsTrue() {
        TicketOrder testTicketOrder = new TicketOrder();
        testTicketOrder.setStatus("not active");
        when(ticketOrderRepository.findById(testTicketOrder.getId())).thenReturn(Optional.of(testTicketOrder));
        assertTrue(ticketOrderService.checkOrderIsActiveOrNot(testTicketOrder.getId()));
    }

    @Test
    public void checkOrderIsActiveOrNot_whenOrderIsActive_returnsFalse() {
        TicketOrder testTicketOrder = new TicketOrder();
        testTicketOrder.setStatus("active");
        when(ticketOrderRepository.findById(testTicketOrder.getId())).thenReturn(Optional.of(testTicketOrder));
        assertFalse(ticketOrderService.checkOrderIsActiveOrNot(testTicketOrder.getId()));
    }

    @Test
    public void checkOrderIsActiveOrNot_whenOrderIsNotExist_returnsFalse() {
        TicketOrder testTicketOrder = new TicketOrder();
        testTicketOrder.setStatus("active");
        when(ticketOrderRepository.findById(testTicketOrder.getId())).thenReturn(Optional.empty());
        assertFalse(ticketOrderService.checkOrderIsActiveOrNot(testTicketOrder.getId()));
    }

    @Test
    public void activateOrder_whenEverythingIsOK_returnsTicketOrderDTO() {
        Clock clock = Clock.fixed(Instant.parse("2023-08-01T14:15:30.00Z"), ZoneId.of("Europe/Budapest"));
        LocalDateTime localDateTime = LocalDateTime.now(clock);
        User testUser = new User("TestUser", "test@gmail.com", new BCryptPasswordEncoder().encode("test"), true);
        Product testProduct = new Product("Day ticket", 360, 24, "You can use this ticket for a whole day!");
        TicketOrder testTicketOrder = new TicketOrder();
        testTicketOrder.setStatus("not active");
        testTicketOrder.setUser(testUser);
        testTicketOrder.setProduct(testProduct);
        TicketOrderDTO ticketOrderDTO = new TicketOrderDTO(testTicketOrder.getId(), "active", localDateTime.plusDays(testProduct.getDuration() / 24), testProduct.getId());
        when(ticketOrderRepository.findById(testTicketOrder.getId())).thenReturn(Optional.of(testTicketOrder));
        try (MockedStatic<LocalDateTime> localDateTimeMockedStatic = Mockito.mockStatic(LocalDateTime.class)) {
            localDateTimeMockedStatic.when(LocalDateTime::now).thenReturn(localDateTime);
            TicketOrderDTO actualTicketOrderDTO = ticketOrderService.activateOrder(testTicketOrder.getId());
            assertEquals(ticketOrderDTO.getId(), actualTicketOrderDTO.getId());
            assertEquals(ticketOrderDTO.getStatus(), actualTicketOrderDTO.getStatus());
            assertEquals(ticketOrderDTO.getExpiry(), actualTicketOrderDTO.getExpiry());
            assertEquals(ticketOrderDTO.getProductId(), actualTicketOrderDTO.getProductId());
        }
    }
}