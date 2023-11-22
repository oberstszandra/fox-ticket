package com.example.foxticket.services;

import com.example.foxticket.dtos.ArticleContainerDTO;
import com.example.foxticket.dtos.ArticleRequestDTO;
import com.example.foxticket.models.Article;
import com.example.foxticket.models.ErrorMessage;

import java.util.List;

public interface ArticleService {
    ArticleContainerDTO findAll();

    ArticleContainerDTO getFilteredArticles(String search);

    List<String> listEmptyFields(ArticleRequestDTO articleRequestDTO);

    ErrorMessage generateErrorMessageWhenFieldsAreEmpty(ArticleRequestDTO articleRequestDTO);

    String formatErrorMessage(List<String> listOfEmptyFields);

    boolean isTitleTaken(ArticleRequestDTO articleRequestDTO);

    Article saveNewArticle(ArticleRequestDTO articleRequestDTO);

    Article updateArticle(ArticleRequestDTO articleRequestDTO, Long id);

    void deleteNewsById(Long id);

    boolean doesArticleExist(Long id);
}
