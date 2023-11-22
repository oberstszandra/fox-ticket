package com.example.foxticket.services;

import com.example.foxticket.dtos.TicketOrderContainerDTO;
import com.example.foxticket.dtos.TicketOrderDTO;
import com.example.foxticket.models.Cart;
import com.example.foxticket.models.Product;
import com.example.foxticket.models.TicketOrder;
import com.example.foxticket.models.User;
import com.example.foxticket.repositories.TicketOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class TicketOrderServiceImpl implements TicketOrderService {
    private TicketOrderRepository ticketOrderRepository;
    private CartService cartService;
    private EmailService emailService;
    private Environment env;


    @Autowired
    public TicketOrderServiceImpl(CartService cartService, TicketOrderRepository ticketOrderRepository, EmailService emailService, Environment env) {
        this.cartService = cartService;
        this.ticketOrderRepository = ticketOrderRepository;
        this.emailService = emailService;
        this.env = env;
    }

    @Override
    public TicketOrderContainerDTO getTicketOrderDTOsFromUser(User user) {
        TicketOrderContainerDTO ticketOrderContainerDTO = new TicketOrderContainerDTO();
        for (TicketOrder ticketOrder : user.getOrders()) {
            TicketOrderDTO ticketOrderDTO = new TicketOrderDTO(ticketOrder.getId(), ticketOrder.getProduct().getName(), ticketOrder.getStatus(), ticketOrder.getExpiry(), ticketOrder.getProduct().getId());
            ticketOrderContainerDTO.addTicketOrder(ticketOrderDTO);
        }
        return ticketOrderContainerDTO;
    }

    @Override
    public TicketOrderContainerDTO getTicketOrderDTOsFromCart(User user) throws IOException {
        Cart cart = user.getCart();
        TicketOrderContainerDTO ticketOrderContainerDTO = new TicketOrderContainerDTO();
        for (Product product : cart.getProducts()) {
            TicketOrder ticketOrder = new TicketOrder(product, user);
            user.addOrder(ticketOrder);
            ticketOrderRepository.save(ticketOrder);
            TicketOrderDTO ticketOrderDTO = new TicketOrderDTO(ticketOrder.getId(), ticketOrder.getProduct().getName(), ticketOrder.getStatus(), ticketOrder.getExpiry(), ticketOrder.getProduct().getId());
            ticketOrderContainerDTO.addTicketOrder(ticketOrderDTO);
        }
        cart.getProducts().clear();
        cartService.save(cart);
        emailService.sendEmailToUser(user, env.getProperty("PURCHASE_TEMPLATE_ID"), env.getProperty("PURCHASE_URL"));
        return ticketOrderContainerDTO;
    }

    public boolean checkIfUserHasTheProvidedOrder(Long userId, Long orderId) {
        TicketOrder actualOrder = getActualOrder(orderId);
        if (actualOrder != null) {
            return actualOrder.getUser().getId().equals(userId);
        }
        return false;
    }

    @Override
    public boolean checkOrderIsActiveOrNot(Long orderId) {
        TicketOrder actualOrder = getActualOrder(orderId);
        if (actualOrder != null) {
            return actualOrder.getStatus().equals("not active");
        }
        return false;
    }

    @Override
    public TicketOrderDTO activateOrder(Long orderId) {
        TicketOrder actualOrder = getActualOrder(orderId);
        Integer productDuration = actualOrder.getProduct().getDuration();
        LocalDateTime actualDateTime = LocalDateTime.now();
        actualOrder.setExpiry(actualDateTime.plusDays(productDuration / 24));
        actualOrder.setStatus("active");
        ticketOrderRepository.save(actualOrder);
        return new TicketOrderDTO(actualOrder.getId(), actualOrder.getStatus(), actualOrder.getExpiry(), actualOrder.getProduct().getId());
    }

    private TicketOrder getActualOrder(Long orderId) {
        Optional<TicketOrder> actualOrder = ticketOrderRepository.findById(orderId);
        return actualOrder.orElse(null);
    }
}
