package com.example.foxticket.repositories;

import com.example.foxticket.models.TicketOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketOrderRepository extends JpaRepository<TicketOrder, Long> {
}
