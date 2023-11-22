package com.example.foxticket.dtos;

public class ProductTypeResponseDTO {
    private Long id;
    private String name;

    public ProductTypeResponseDTO() {
    }

    public ProductTypeResponseDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public ProductTypeResponseDTO(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
