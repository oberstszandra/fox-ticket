package com.example.foxticket.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "articles")
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String content;
    @JsonProperty("publish_date")
    private LocalDate publishDate;

    public Article() {
    }

    public Article(String title, String content, LocalDate publishDate) {
        this.title = title;
        this.content = content;
        this.publishDate = publishDate;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public LocalDate getPublishDate() {
        return publishDate;
    }

    public Long getId() {
        return id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }
}