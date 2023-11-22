package com.example.foxticket.dtos;

import java.util.ArrayList;
import java.util.List;

public class TicketOrderContainerDTO {
    private List<TicketOrderDTO> orders;

    public TicketOrderContainerDTO() {
        this.orders = new ArrayList<>();
    }

    public List<TicketOrderDTO> getOrders() {
        return orders;
    }

    public void addTicketOrder(TicketOrderDTO ticketOrderDTO) {
        this.orders.add(ticketOrderDTO);
    }
}
