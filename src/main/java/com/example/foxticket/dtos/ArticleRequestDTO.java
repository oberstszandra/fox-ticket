package com.example.foxticket.dtos;

public class ArticleRequestDTO {
    private String title;
    private String content;

    public ArticleRequestDTO() {
    }

    public ArticleRequestDTO(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
