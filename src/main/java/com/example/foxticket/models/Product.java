package com.example.foxticket.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Integer price;
    private Integer duration;
    private String description;

    @ManyToOne
    @JsonManagedReference
    private ProductType productType;
    @ManyToMany(cascade = CascadeType.PERSIST, mappedBy = "products", fetch = FetchType.EAGER)
    private List<Cart> carts;

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH}, mappedBy = "product", fetch = FetchType.EAGER)
    @JsonBackReference
    private List<TicketOrder> orders;

    public Product() {
        this.orders = new ArrayList<>();
    }

    public Product(String name, Integer price, Integer duration, String description) {
        this();
        this.name = name;
        this.price = price;
        this.duration = duration;
        this.description = description;
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

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<TicketOrder> getOrders() {
        return orders;
    }

    public ProductType getProductType() {
        return productType;
    }

    public void setProductType(ProductType type) {
        this.productType = type;
    }

    public List<Cart> getCarts() {
        return carts;
    }

    public void setCarts(List<Cart> carts) {
        this.carts = carts;
    }

    public void addOrder(TicketOrder ticketOrder) {
        this.orders.add(ticketOrder);
        ticketOrder.setProduct(this);
    }
}