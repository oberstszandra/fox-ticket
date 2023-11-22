package com.example.foxticket.services;

import com.example.foxticket.dtos.TicketOrderContainerDTO;
import com.example.foxticket.dtos.TicketOrderDTO;
import com.example.foxticket.models.User;

import java.io.IOException;

public interface TicketOrderService {
    TicketOrderContainerDTO getTicketOrderDTOsFromUser(User user);

    TicketOrderContainerDTO getTicketOrderDTOsFromCart(User user) throws IOException;

    boolean checkIfUserHasTheProvidedOrder(Long userId, Long orderId);

    TicketOrderDTO activateOrder(Long orderId);

    boolean checkOrderIsActiveOrNot(Long orderId);
}
