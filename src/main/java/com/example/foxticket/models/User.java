package com.example.foxticket.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(unique = true)
    private String email;
    private String password;
    private boolean isAdmin;
    @OneToOne(cascade = CascadeType.PERSIST, mappedBy = "user")
    private Cart cart;

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH}, mappedBy = "user", fetch = FetchType.EAGER)
    @JsonBackReference
    private List<TicketOrder> orders;
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH}, mappedBy = "user", fetch = FetchType.EAGER)
    @JsonBackReference
    private List<Verification> verifications;

    public User() {
        this.orders = new ArrayList<>();
        this.verifications = new ArrayList<>();
    }

    public User(String name, String email, String password, boolean isAdmin) {
        this();
        this.name = name;
        this.email = email;
        this.password = password;
        this.isAdmin = isAdmin;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public List<TicketOrder> getOrders() {
        return orders;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public void addOrder(TicketOrder ticketOrder) {
        this.orders.add(ticketOrder);
        ticketOrder.setUser(this);
    }

    public List<Verification> getVerifications() {
        return verifications;
    }

    public void addVerification(Verification verification) {
        this.verifications.add(verification);
        verification.setUser(this);
    }
}