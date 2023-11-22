package com.example.foxticket.dtos;

import com.example.foxticket.models.Article;

import java.util.ArrayList;
import java.util.List;

public class ArticleContainerDTO {
    private List<Article> articles;

    public ArticleContainerDTO() {
        this.articles = new ArrayList<>();
    }

    public List<Article> getArticles() {
        return articles;
    }

    public void addArticle(Article article) {
        this.articles.add(article);
    }
}