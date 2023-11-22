package com.example.foxticket.dtos;

public class ProductUpdateResponseDTO {
    private Long id;
    private String name;
    private Integer price;
    private Integer duration;
    private String description;
    private String type;

    public ProductUpdateResponseDTO(Long id, String name, Integer price, Integer duration, String description, String type) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.duration = duration;
        this.description = description;
        this.type = type;
    }

    public ProductUpdateResponseDTO() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getPrice() {
        return price;
    }

    public Integer getDuration() {
        return duration;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }
}
