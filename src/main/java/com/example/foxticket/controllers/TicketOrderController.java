package com.example.foxticket.controllers;

import com.example.foxticket.dtos.TicketOrderContainerDTO;
import com.example.foxticket.models.ErrorMessage;
import com.example.foxticket.models.User;
import com.example.foxticket.security.MyUserDetails;
import com.example.foxticket.services.TicketOrderService;
import com.example.foxticket.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping(path = "/api")
public class TicketOrderController {
    private TicketOrderService ticketOrderService;
    private UserService userService;

    @Autowired
    public TicketOrderController(TicketOrderService ticketOrderService, UserService userService) {
        this.ticketOrderService = ticketOrderService;
        this.userService = userService;
    }

    @GetMapping("/orders")
    public ResponseEntity<?> getPurchases(@AuthenticationPrincipal MyUserDetails myUserDetails) {
        Optional<User> loggedInUser = userService.findById(myUserDetails.getId());
        if (loggedInUser.isPresent()) {
            return new ResponseEntity<>(ticketOrderService.getTicketOrderDTOsFromUser(loggedInUser.get()), HttpStatus.OK);
        } else {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
    }

    @PostMapping("/orders")
    public ResponseEntity<?> createNewOrdersByCart(@AuthenticationPrincipal MyUserDetails myUserDetails) {
        Optional<User> loggedInUser = userService.findById(myUserDetails.getId());
        if (loggedInUser.isPresent()) {
            if (!userService.authenticatedUserIsVerifiedOrNot(loggedInUser.get())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorMessage("Please verify your email before your purchase!"));
            }
            TicketOrderContainerDTO ticketOrderContainerDTO;
            try {
                ticketOrderContainerDTO = ticketOrderService.getTicketOrderDTOsFromCart(loggedInUser.get());
            } catch (IOException ioException) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorMessage("Email sending not working properly, but you can see your purchases at http://localhost:8080/purchases"));
            }
            return new ResponseEntity<>(ticketOrderContainerDTO, HttpStatus.OK);
        } else {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
    }

    @PatchMapping("/orders/{id}")
    public ResponseEntity<?> activatePurchasedItem(@PathVariable String id, @AuthenticationPrincipal MyUserDetails myUserDetails) {
        Long orderId;
        try {
            orderId = Long.parseLong(id);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(new ErrorMessage("This is not a valid id, you should use number!"));
        }
        if (ticketOrderService.checkIfUserHasTheProvidedOrder(myUserDetails.getId(), orderId)) {
            if (!ticketOrderService.checkOrderIsActiveOrNot(orderId)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorMessage("This order is already active!"));
            }
            return ResponseEntity.status(HttpStatus.OK).body(ticketOrderService.activateOrder(orderId));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorMessage("This is not your order!"));
    }
}