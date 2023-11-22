package com.example.foxticket.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DisplayController {

    @GetMapping(path = "/home")
    public String displayHome() {
        return "home";
    }

    @GetMapping(path = "/login")
    public String displayLogin() {
        return "login";
    }

    @GetMapping(path = "/shop")
    public String displayShop() {
        return "shop";
    }

    @GetMapping(path = "/cart")
    public String displayCart() {
        return "cart";
    }

    @GetMapping(path = "/purchases")
    public String displayPurchases() {
        return "purchases";
    }

    @GetMapping(path = "/registration")
    public String displayRegistration() {
        return "registration";
    }

    @GetMapping(path = "/verification/{verification-id}")
    public String displayVerification() {
        return "verification";
    }
}
