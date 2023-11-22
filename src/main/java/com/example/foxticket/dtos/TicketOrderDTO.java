package com.example.foxticket.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class TicketOrderDTO {
    private Long id;
    @JsonProperty("product_name")
    private String productName;
    private String status;
    private LocalDateTime expiry;
    @JsonProperty("product_id")
    private Long productId;

    public TicketOrderDTO() {
    }

    public TicketOrderDTO(Long id, String status, LocalDateTime expiry, Long productId) {
        this.id = id;
        this.status = status;
        this.expiry = expiry;
        this.productId = productId;
    }

    public TicketOrderDTO(Long id, String productName, String status, LocalDateTime expiry, Long productId) {
        this.id = id;
        this.productName = productName;
        this.status = status;
        this.expiry = expiry;
        this.productId = productId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getExpiry() {
        return expiry;
    }

    public void setExpiry(LocalDateTime expiry) {
        this.expiry = expiry;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setName(String productName) {
        this.productName = productName;
    }
}
