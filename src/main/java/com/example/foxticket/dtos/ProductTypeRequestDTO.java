package com.example.foxticket.dtos;

public class ProductTypeRequestDTO {
    private String name;

    public ProductTypeRequestDTO() {
    }

    public ProductTypeRequestDTO(String name) {
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
