package com.example.foxticket.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product_types")
public class ProductType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH}, mappedBy = "productType", fetch = FetchType.EAGER)
    @JsonBackReference
    private List<Product> products;

    public ProductType() {
        this.products = new ArrayList<>();
    }

    public ProductType(String name) {
        this();
        this.name = name;
    }

    public ProductType(Long id, String name) {
        this();
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setProducts(Product product) {
        this.products.add(product);
        product.setProductType(this);
    }

    public Long getId() {
        return id;
    }
}