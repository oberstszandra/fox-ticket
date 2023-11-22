package com.example.foxticket.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class TicketOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String status;
    private LocalDateTime expiry;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH})
    @JsonManagedReference
    private User user;
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH})
    @JsonManagedReference
    private Product product;

    public TicketOrder() {
        this.status = "not active";
        this.expiry = null;
    }

    public TicketOrder(Product product, User user) {
        this();
        this.product = product;
        this.user = user;
    }

    public TicketOrder(String status, LocalDateTime expiry, Product product, User user) {
        this.status = status;
        this.expiry = expiry;
        this.product = product;
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getExpiry() {
        return expiry;
    }

    public Product getProduct() {
        return product;
    }

    public User getUser() {
        return user;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setExpiry(LocalDateTime expiry) {
        this.expiry = expiry;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
