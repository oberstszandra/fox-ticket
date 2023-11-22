package com.example.foxticket.integrations;

import com.example.foxticket.dtos.ArticleContainerDTO;
import com.example.foxticket.dtos.ArticleRequestDTO;
import com.example.foxticket.models.Article;
import com.example.foxticket.models.User;
import com.example.foxticket.repositories.ArticleRepository;
import com.example.foxticket.repositories.UserRepository;
import com.example.foxticket.security.MyUserDetails;
import com.example.foxticket.security.MyUserDetailsService;
import com.example.foxticket.security.util.JwtUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(value = "/db/test/clear_tables.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class ArticleControllerTest {

    @Autowired
    private MockMvc mockMvc;
    private JwtUtil jwtUtil;
    private MyUserDetailsService myUserDetailsService;
    private UserRepository userRepository;
    private ArticleRepository articleRepository;

    @Autowired
    public ArticleControllerTest(JwtUtil jwtUtil, MyUserDetailsService myUserDetailsService,
                                 UserRepository userRepository, ArticleRepository articleRepository) {
        this.jwtUtil = jwtUtil;
        this.myUserDetailsService = myUserDetailsService;
        this.userRepository = userRepository;
        this.articleRepository = articleRepository;
    }

    @Test

    public void getArticles_WithExistingArticles_ReturnsAllArticles() throws Exception {
        Article testArticle1 = new Article("Hello world!", "Our first article", LocalDate.of(2023, 07, 01));
        Article testArticle2 = new Article("Hello world again!", "Our second article", LocalDate.of(2023, 07, 02));
        articleRepository.save(testArticle1);
        articleRepository.save(testArticle2);
        ArticleContainerDTO testArticleContainerDTO = new ArticleContainerDTO();
        testArticleContainerDTO.addArticle(new Article(testArticle1.getTitle(), testArticle1.getContent(), testArticle1.getPublishDate()));
        testArticleContainerDTO.addArticle(new Article(testArticle2.getTitle(), testArticle2.getContent(), testArticle2.getPublishDate()));

        mockMvc.perform(get("/api/news"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.articles[0].title").value(testArticleContainerDTO.getArticles().get(0).getTitle()))
                .andExpect(jsonPath("$.articles[1]").exists())
                .andExpect(jsonPath("$.articles.size()").value(testArticleContainerDTO.getArticles().size()));
    }

    @Test
    public void getArticles_WithGivenQueryStringIfArticleExists_ReturnsMatchingArticles() throws Exception {
        Article testArticle1 = new Article("Hello world!", "Our first article", LocalDate.of(2023, 07, 01));
        Article testArticle2 = new Article("Hello world again!", "Our second article", LocalDate.of(2023, 07, 02));
        articleRepository.save(testArticle1);
        articleRepository.save(testArticle2);
        ArticleContainerDTO testArticleContainerDTO = new ArticleContainerDTO();
        testArticleContainerDTO.addArticle(new Article(testArticle1.getTitle(), testArticle1.getContent(), testArticle1.getPublishDate()));
        testArticleContainerDTO.addArticle(new Article(testArticle2.getTitle(), testArticle2.getContent(), testArticle2.getPublishDate()));

        mockMvc.perform(get("/api/news?search=second"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.articles[0].title", is("Hello world again!")));
    }

    @Test
    public void getArticles_WithGivenQueryStringIfArticleDoesNotExist_ReturnsErrorMessage() throws Exception {
        Article testArticle1 = new Article("Hello world!", "Our first article", LocalDate.of(2023, 07, 01));
        Article testArticle2 = new Article("Hello world again!", "Our second article", LocalDate.of(2023, 07, 02));
        articleRepository.save(testArticle1);
        articleRepository.save(testArticle2);
        ArticleContainerDTO testArticleContainerDTO = new ArticleContainerDTO();
        testArticleContainerDTO.addArticle(new Article(testArticle1.getTitle(), testArticle1.getContent(), testArticle1.getPublishDate()));
        testArticleContainerDTO.addArticle(new Article(testArticle2.getTitle(), testArticle2.getContent(), testArticle2.getPublishDate()));

        mockMvc.perform(get("/api/news?search=hi"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMessage", is("There are no articles with the given keyword!")));
    }

    @Test
    public void getArticles_WithEmptyOrNullQueryString_ReturnsAllArticles() throws Exception {
        Article testArticle1 = new Article("Hello world!", "Our first article", LocalDate.of(2023, 07, 01));
        Article testArticle2 = new Article("Hello world again!", "Our second article", LocalDate.of(2023, 07, 02));
        articleRepository.save(testArticle1);
        articleRepository.save(testArticle2);
        ArticleContainerDTO testArticleContainerDTO = new ArticleContainerDTO();
        testArticleContainerDTO.addArticle(new Article(testArticle1.getTitle(), testArticle1.getContent(), testArticle1.getPublishDate()));
        testArticleContainerDTO.addArticle(new Article(testArticle2.getTitle(), testArticle2.getContent(), testArticle2.getPublishDate()));

        mockMvc.perform(get("/api/news?search="))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.articles[0].title").value(testArticleContainerDTO.getArticles().get(0).getTitle()))
                .andExpect(jsonPath("$.articles[1]").exists())
                .andExpect(jsonPath("$.articles.size()").value(testArticleContainerDTO.getArticles().size()));
    }

    @Test
    public void addNews_whenArticleIsValid_returnsCreated() throws Exception {
        userRepository.save(new User("admin", "admin@admin.com", "admin", true));
        MyUserDetails userDetails = myUserDetailsService.loadUserByUsername("admin@admin.com");
        ObjectMapper mapper = new ObjectMapper();
        ArticleRequestDTO productRequestDTO = new ArticleRequestDTO("Title", "Content");
        MockHttpServletRequestBuilder requestBuilder = post("/api/news")
                .header("Authorization", "Bearer " + jwtUtil.generateToken(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(productRequestDTO));
        MvcResult result = mockMvc.perform(requestBuilder)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("Title")))
                .andExpect(jsonPath("$.content", is("Content")))
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        JsonNode jsonNode = mapper.readTree(jsonResponse);
        LocalDate publishDate = LocalDate.parse(jsonNode.get("publish_date").asText());
        assertEquals(LocalDate.now(), publishDate);
    }

    @Test
    public void editNews_WithValidInput_ReturnsUpdatedArticle() throws Exception {
        userRepository.save(new User("admin", "admin@admin.com", "admin", true));
        MyUserDetails userDetails = myUserDetailsService.loadUserByUsername("admin@admin.com");
        ObjectMapper mapper = new ObjectMapper();
        Article testArticle = new Article("Title", "Content", LocalDate.of(2023, 07, 01));
        testArticle = articleRepository.save(testArticle);
        ArticleRequestDTO updatedArticleRequestDTO = new ArticleRequestDTO("Updated Title", "Updated Content");
        MockHttpServletRequestBuilder requestBuilder = put("/api/news/" + testArticle.getId())
                .header("Authorization", "Bearer " + jwtUtil.generateToken(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(updatedArticleRequestDTO));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Updated Title")))
                .andExpect(jsonPath("$.content", is("Updated Content")))
                .andExpect(jsonPath("$.publish_date", is(testArticle.getPublishDate().toString())));
    }

    @Test
    public void deleteArticle_whenIdIsFound_deletesArticleAndReturnsOk() throws Exception {
        articleRepository.save(new Article("News", "Blabla", LocalDate.of(2023, 07, 01)));
        userRepository.save(new User("TestUser", "valaki@gmail.com", "cicamica", true));
        MyUserDetails userDetails = myUserDetailsService.loadUserByUsername("valaki@gmail.com");
        MockHttpServletRequestBuilder requestBuilder = delete("/api/news/" + articleRepository.findAll().get(0).getId())
                .header("Authorization", "Bearer " + jwtUtil.generateToken(userDetails));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk());
        List<Article> articles = articleRepository.findAll();
        Assertions.assertTrue(articles.isEmpty());
    }

    @Test
    public void deleteProduct_whenIdIsNotFound_returnsNotFound() throws Exception {
        Long nonExistingId = 0L;
        userRepository.save(new User("TestUser", "valaki@gmail.com", "cicamica", true));
        MyUserDetails userDetails = myUserDetailsService.loadUserByUsername("valaki@gmail.com");
        MockHttpServletRequestBuilder requestBuilder = delete("/api/news/" + nonExistingId)
                .header("Authorization", "Bearer " + jwtUtil.generateToken(userDetails));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isNotFound());
    }
}
