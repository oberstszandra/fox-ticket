package com.example.foxticket.controllers;

import com.example.foxticket.dtos.ArticleRequestDTO;
import com.example.foxticket.models.Article;
import com.example.foxticket.models.ErrorMessage;
import com.example.foxticket.services.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api")
public class ArticleController {
    private final ArticleService articleService;

    @Autowired
    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping("/news")
    public ResponseEntity<?> getArticles(@RequestParam(required = false) String search) {
        if (search == null || search.isBlank()) {
            try {
                return new ResponseEntity<>(articleService.findAll(), HttpStatus.OK);
            } catch (RuntimeException e) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }
        }
        try {
            if (articleService.getFilteredArticles(search).getArticles().size() == 0) {
                return new ResponseEntity<>(new ErrorMessage("There are no articles with the given keyword!"), HttpStatus.NOT_FOUND);
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        try {
            return new ResponseEntity<>(articleService.getFilteredArticles(search), HttpStatus.OK);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping(path = "/news")
    public ResponseEntity<?> addNews(@RequestBody ArticleRequestDTO articleRequestDTO) {
        if (!articleService.listEmptyFields(articleRequestDTO).isEmpty()) {
            return new ResponseEntity<>(articleService.generateErrorMessageWhenFieldsAreEmpty(articleRequestDTO), HttpStatus.NOT_ACCEPTABLE);
        }
        if (articleService.isTitleTaken(articleRequestDTO)) {
            return new ResponseEntity<>(new ErrorMessage("News title already exists!"), HttpStatus.NOT_ACCEPTABLE);
        } else {
            Article article = articleService.saveNewArticle(articleRequestDTO);
            return new ResponseEntity<>(article, HttpStatus.CREATED);
        }
    }

    @PutMapping(path = "/news/{newsId}")
    public ResponseEntity<?> editNews(@PathVariable Long newsId, @RequestBody ArticleRequestDTO articleRequestDTO) {
        if (!articleService.listEmptyFields(articleRequestDTO).isEmpty()) {
            return new ResponseEntity<>(articleService.generateErrorMessageWhenFieldsAreEmpty(articleRequestDTO), HttpStatus.NOT_ACCEPTABLE);
        }
        if (articleService.isTitleTaken(articleRequestDTO)) {
            return new ResponseEntity<>(new ErrorMessage("News title already exists!"), HttpStatus.NOT_ACCEPTABLE);
        } else {
            return new ResponseEntity<>(articleService.updateArticle(articleRequestDTO, newsId), HttpStatus.OK);
        }
    }

    @DeleteMapping(path = "/news/{newsId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity deleteNews(@PathVariable Long newsId) {
        if (articleService.doesArticleExist(newsId)) {
            articleService.deleteNewsById(newsId);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}