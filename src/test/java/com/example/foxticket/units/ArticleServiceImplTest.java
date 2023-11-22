package com.example.foxticket.units;

import com.example.foxticket.dtos.ArticleContainerDTO;
import com.example.foxticket.dtos.ArticleRequestDTO;
import com.example.foxticket.models.Article;
import com.example.foxticket.models.ErrorMessage;
import com.example.foxticket.repositories.ArticleRepository;
import com.example.foxticket.services.ArticleService;
import com.example.foxticket.services.ArticleServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ArticleServiceImplTest {

    private ArticleRepository articleRepository;
    private ArticleService articleService;

    public ArticleServiceImplTest() {
        articleRepository = Mockito.mock(ArticleRepository.class);
        articleService = new ArticleServiceImpl(articleRepository);
    }

    @Test
    public void findAll_WithExistingArticles_ReturnsAllArticles() {
        ArticleContainerDTO testArticleContainerDTO = new ArticleContainerDTO();
        testArticleContainerDTO.addArticle(new Article("Hello world!", "Our first article", LocalDate.of(2023, 07, 01)));
        testArticleContainerDTO.addArticle(new Article("Hello world again!", "Our second article", LocalDate.of(2023, 07, 02)));
        when(articleRepository.findAll()).thenReturn(testArticleContainerDTO.getArticles());
        ArticleContainerDTO actual = articleService.findAll();
        assertEquals(testArticleContainerDTO.getArticles(), actual.getArticles());
    }

    @Test
    public void findAll_WithNonExistentArticles_ReturnsEmptyList() {
        ArticleContainerDTO testArticleContainerDTO = new ArticleContainerDTO();
        when(articleRepository.findAll()).thenReturn(testArticleContainerDTO.getArticles());
        ArticleContainerDTO actual = articleService.findAll();
        assertEquals(0, actual.getArticles().size());
    }

    @Test
    public void getFilteredArticles_WithExistentFilteredArticles_ReturnsMatchingArticles() {
        ArticleContainerDTO testArticleContainerDTO = new ArticleContainerDTO();
        testArticleContainerDTO.addArticle(new Article("Hello world again!", "Our second article", LocalDate.of(2023, 07, 02)));
        when(articleRepository.getFilteredArticles("second")).thenReturn(testArticleContainerDTO.getArticles());
        ArticleContainerDTO actual = articleService.getFilteredArticles("second");
        assertTrue(actual.getArticles().get(0).getContent().contains("second"));
    }

    @Test
    public void getFilteredArticles_withNonExistentFilteredArticles_returnsEmptyList() {
        ArticleContainerDTO testArticleContainerDTO = new ArticleContainerDTO();
        when(articleRepository.getFilteredArticles("hi")).thenReturn(testArticleContainerDTO.getArticles());
        ArticleContainerDTO actual = articleService.getFilteredArticles("hi");
        assertEquals(0, actual.getArticles().size());
    }

    @Test
    public void listEmptyFields_whenAllFieldsAreEmpty_returnsBothFields() {
        ArticleRequestDTO articleRequestDTO = new ArticleRequestDTO("", null);
        List<String> emptyFields = articleService.listEmptyFields(articleRequestDTO);
        assertEquals(2, emptyFields.size());
        assertEquals("title", emptyFields.get(0));
        assertEquals("content", emptyFields.get(1));
    }

    @Test
    public void listEmptyFields_whenNoFieldsAreEmpty_returnsBothFields() {
        ArticleRequestDTO articleRequestDTO = new ArticleRequestDTO("title", "content");
        List<String> emptyFields = articleService.listEmptyFields(articleRequestDTO);
        assertEquals(0, emptyFields.size());
    }

    @Test
    public void generateErrorMessage_whenASingleFieldIsEmpty_returnsErrorMessageForSingleField() {
        ArticleRequestDTO articleRequestDTO = new ArticleRequestDTO(null, "content");
        ErrorMessage errorMessage = articleService.generateErrorMessageWhenFieldsAreEmpty(articleRequestDTO);
        assertEquals("Title is required", errorMessage.getErrorMessage());
    }

    @Test
    public void generateErrorMessage_whenMultipleFieldsAreEmpty_returnsErrorMessageForSingleField() {
        ArticleRequestDTO articleRequestDTO = new ArticleRequestDTO(null, "");
        ErrorMessage errorMessage = articleService.generateErrorMessageWhenFieldsAreEmpty(articleRequestDTO);
        assertEquals("Title and content are required", errorMessage.getErrorMessage());
    }

    @Test
    public void formatErrorMessage_whenASingleFieldIsEmpty_returnsFormattedMessage() {
        List<String> listOfEmptyFields = List.of("title");
        String formattedMessage = articleService.formatErrorMessage(listOfEmptyFields);
        assertEquals("Title", formattedMessage);
    }

    @Test
    public void formatErrorMessage_whenMultipleFieldsAreEmpty_returnsFormattedMessage() {
        List<String> listOfEmptyFields = Arrays.asList("title", "content");
        String formattedMessage = articleService.formatErrorMessage(listOfEmptyFields);
        assertEquals("Title and content", formattedMessage);
    }

    @Test
    public void isTitleTaken_whenTitleExists_returnsTrue() {
        String existingTitle = "Existing Title";
        ArticleRequestDTO articleRequestDTO = new ArticleRequestDTO(existingTitle, "helloka");
        List<Article> mockArticles = new ArrayList<>();
        mockArticles.add(new Article(existingTitle, "belloka", LocalDate.now()));
        when(articleRepository.findAll()).thenReturn(mockArticles);
        boolean result = articleService.isTitleTaken(articleRequestDTO);
        assertTrue(result);
    }

    @Test
    public void isTitleTaken_whenTitleNotExists_returnsFalse() {
        String nonExistingTitle = "Existing Title";
        ArticleRequestDTO articleRequestDTO = new ArticleRequestDTO(nonExistingTitle, "helloka");
        List<Article> mockArticles = new ArrayList<>();
        mockArticles.add(new Article("BKV", "belloka", LocalDate.now()));
        when(articleRepository.findAll()).thenReturn(mockArticles);
        boolean result = articleService.isTitleTaken(articleRequestDTO);
        assertFalse(result);
    }

    @Test
    public void updateArticle_withAllNecessaryInfo_returnsUpdatedArticle() {
        Article article = new Article("Bumbum", "Bimbam", LocalDate.of(2023, 07, 02));
        ArticleRequestDTO articleRequestDTO = new ArticleRequestDTO("Bimbambum", "Bambimbam");
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));
        article = articleService.updateArticle(articleRequestDTO, 1L);
        assertEquals("Bimbambum", article.getTitle());
        assertEquals("Bambimbam", article.getContent());
    }

    @Test
    public void updateArticle_withWrongInfo_throwsException() {
        ArticleRequestDTO articleRequestDTO = new ArticleRequestDTO("Bimbambum", "Bambimbam");
        when(articleRepository.findById(1L)).thenReturn(Optional.empty());
        NoSuchElementException exception = null;
        try {
            articleService.updateArticle(articleRequestDTO, 1L);
        } catch (NoSuchElementException e) {
            exception = e;
        }
        assertNotNull(exception);
        assertEquals("News not found", exception.getMessage());
    }

    @Test
    public void deleteProductById_ValidId_ProductRepositoryDeleteByIdCalled() {
        Long articleId = 1L;
        doNothing().when(articleRepository).deleteById(articleId);
        articleService.deleteNewsById(articleId);
    }
}