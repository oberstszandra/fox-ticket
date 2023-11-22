package com.example.foxticket.services;

import com.example.foxticket.dtos.ArticleContainerDTO;
import com.example.foxticket.dtos.ArticleRequestDTO;
import com.example.foxticket.models.Article;
import com.example.foxticket.models.ErrorMessage;
import com.example.foxticket.repositories.ArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ArticleServiceImpl implements ArticleService {
    private final ArticleRepository articleRepository;

    @Autowired
    public ArticleServiceImpl(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    static String getMessage(List<String> listOfEmptyFields) {
        String message = listOfEmptyFields.toString();
        message = message.replace("[", "").replace("]", "");
        message = Character.toUpperCase(message.charAt(0)) + message.substring(1);
        if (message.contains(",")) {
            int indexOfLastComa = message.lastIndexOf(",");
            String messageBeforeLastComa = message.substring(0, indexOfLastComa);
            String messageAfterLastComa = message.substring(indexOfLastComa + 1);
            message = messageBeforeLastComa + " and" + messageAfterLastComa;
        }
        return message;
    }

    @Override
    public ArticleContainerDTO findAll() {
        ArticleContainerDTO articleContainerDTO = new ArticleContainerDTO();
        for (Article article : articleRepository.findAll()) {
            articleContainerDTO.addArticle(article);
        }
        return articleContainerDTO;
    }

    @Override
    public ArticleContainerDTO getFilteredArticles(String search) {
        ArticleContainerDTO articleContainerDTO = new ArticleContainerDTO();
        List<Article> articles = articleRepository.getFilteredArticles(search);
        for (Article article : articles) {
            articleContainerDTO.addArticle(article);
        }
        return articleContainerDTO;
    }

    @Override
    public List<String> listEmptyFields(ArticleRequestDTO articleRequestDTO) {
        List<String> fieldNames = new ArrayList<>();
        if (articleRequestDTO.getTitle() == null || articleRequestDTO.getTitle().isBlank()) {
            fieldNames.add("title");
        }
        if (articleRequestDTO.getContent() == null || articleRequestDTO.getContent().isBlank()) {
            fieldNames.add("content");
        }
        return fieldNames;
    }

    @Override
    public ErrorMessage generateErrorMessageWhenFieldsAreEmpty(ArticleRequestDTO articleRequestDTO) {
        if (listEmptyFields(articleRequestDTO).size() == 1) {
            String message = formatErrorMessage(listEmptyFields(articleRequestDTO));
            ErrorMessage error = new ErrorMessage(message + " is required");
            return error;
        } else {
            String message = formatErrorMessage(listEmptyFields(articleRequestDTO));
            ErrorMessage error = new ErrorMessage(message + " are required");
            return error;
        }
    }

    @Override
    public String formatErrorMessage(List<String> listOfEmptyFields) {
        return getMessage(listOfEmptyFields);
    }

    @Override
    public boolean isTitleTaken(ArticleRequestDTO articleRequestDTO) {
        boolean productNameExist = false;
        for (Article news : articleRepository.findAll()) {
            if (news.getTitle().equals(articleRequestDTO.getTitle())) {
                productNameExist = true;
                break;
            }
        }
        return productNameExist;
    }

    @Override
    public Article saveNewArticle(ArticleRequestDTO articleRequestDTO) {
        Article article = new Article(articleRequestDTO.getTitle(), articleRequestDTO.getContent(), LocalDate.now());
        return articleRepository.save(article);
    }

    @Override
    public Article updateArticle(ArticleRequestDTO articleRequestDTO, Long id) {
        if (articleRepository.findById(id).isPresent()) {
            Article article = articleRepository.findById(id).get();
            article.setContent(articleRequestDTO.getContent());
            article.setTitle(articleRequestDTO.getTitle());
            articleRepository.save(article);
            return article;
        } else {
            throw new NoSuchElementException("News not found");
        }
    }

    @Override
    public void deleteNewsById(Long id) {
        articleRepository.deleteById(id);
    }

    @Override
    public boolean doesArticleExist(Long id) {
        return articleRepository.findById(id).isPresent();
    }
}