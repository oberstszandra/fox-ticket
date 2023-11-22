package com.example.foxticket.repositories;

import com.example.foxticket.models.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    @Query(value = "SELECT * FROM articles WHERE title LIKE %?1% OR content LIKE %?1%", nativeQuery = true)
    List<Article> getFilteredArticles(String search);
}
