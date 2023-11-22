package com.example.foxticket.integrations;

import com.example.foxticket.dtos.ProductTypeRequestDTO;
import com.example.foxticket.models.ProductType;
import com.example.foxticket.models.User;
import com.example.foxticket.repositories.ProductRepository;
import com.example.foxticket.repositories.ProductTypeRepository;
import com.example.foxticket.repositories.UserRepository;
import com.example.foxticket.security.MyUserDetails;
import com.example.foxticket.security.MyUserDetailsService;
import com.example.foxticket.security.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(value = "/db/test/clear_tables.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class ProductTypeControllerTest {

    @Autowired
    private MockMvc mockMvc;
    private ProductTypeRepository productTypeRepository;
    private UserRepository userRepository;
    private MyUserDetailsService myUserDetailsService;
    private JwtUtil jwtUtil;
    private ProductRepository productRepository;
    private ObjectMapper mapper;

    @Autowired
    public ProductTypeControllerTest(ProductTypeRepository productTypeRepository, JwtUtil jwtUtil, UserRepository userRepository, MyUserDetailsService myUserDetailsService, ProductRepository productRepository, ObjectMapper objectMapper) {
        this.productTypeRepository = productTypeRepository;
        this.jwtUtil = jwtUtil;
        this.myUserDetailsService = myUserDetailsService;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.mapper = objectMapper;
    }

    @Test
    public void addProductType_WhenProductTypeNameIsMissing_returnsCorrectErrorMessage() throws Exception {
        userRepository.save(new User("TestUser", "test@gmail.com", new BCryptPasswordEncoder().encode("test"), true));
        MyUserDetails userDetails = myUserDetailsService.loadUserByUsername("test@gmail.com");
        ProductTypeRequestDTO productTypeRequestDTO = new ProductTypeRequestDTO(null);
        String productTypeDTOString = mapper.writeValueAsString(productTypeRequestDTO);
        MockHttpServletRequestBuilder requestBuilder = post("/api/product-types")
                .contentType(MediaType.APPLICATION_JSON)
                .content(productTypeDTOString)
                .header("Authorization", "Bearer " + jwtUtil.generateToken(userDetails));
        mockMvc.perform(requestBuilder)
                .andExpect(status().isNotAcceptable())
                .andExpect(jsonPath("$.errorMessage").value("Name is required."));
    }

    @Test
    public void addProductType_WhenProductTypeNameIsBlank_returnsCorrectErrorMessage() throws Exception {
        userRepository.save(new User("TestUser", "test1@gmail.com", new BCryptPasswordEncoder().encode("test"), true));
        MyUserDetails userDetails = myUserDetailsService.loadUserByUsername("test1@gmail.com");
        ProductTypeRequestDTO productTypeRequestDTO = new ProductTypeRequestDTO("  ");
        String productTypeDTOString = mapper.writeValueAsString(productTypeRequestDTO);
        MockHttpServletRequestBuilder requestBuilder = post("/api/product-types")
                .contentType(MediaType.APPLICATION_JSON)
                .content(productTypeDTOString)
                .header("Authorization", "Bearer " + jwtUtil.generateToken(userDetails));
        mockMvc.perform(requestBuilder)
                .andExpect(status().isNotAcceptable())
                .andExpect(jsonPath("$.errorMessage").value("Name is required."));
    }

    @Test
    public void addProductType_WhenProductTypeNameIsValid_returnsProductTypeResponseDTO() throws Exception {
        userRepository.save(new User("TestUser", "test2@gmail.com", new BCryptPasswordEncoder().encode("test"), true));
        MyUserDetails userDetails = myUserDetailsService.loadUserByUsername("test2@gmail.com");
        ProductTypeRequestDTO productTypeRequestDTO = new ProductTypeRequestDTO("one day ticket");
        String productTypeDTOString = mapper.writeValueAsString(productTypeRequestDTO);
        MockHttpServletRequestBuilder requestBuilder = post("/api/product-types")
                .contentType(MediaType.APPLICATION_JSON)
                .content(productTypeDTOString)
                .header("Authorization", "Bearer " + jwtUtil.generateToken(userDetails));
        mockMvc.perform(requestBuilder)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value(productTypeRequestDTO.getName()));
    }

    @Test
    public void addProductType_WhenProductTypeNameIsValidButAlreadyExist_returnsCorrectErrorMessage() throws Exception {
        userRepository.save(new User("TestUser", "test3@gmail.com", new BCryptPasswordEncoder().encode("test"), true));
        MyUserDetails userDetails = myUserDetailsService.loadUserByUsername("test3@gmail.com");
        ProductTypeRequestDTO productTypeRequestDTO = new ProductTypeRequestDTO("two day ticket");
        ProductType productType = new ProductType("two day ticket");
        productTypeRepository.save(productType);
        String productTypeDTOString = mapper.writeValueAsString(productTypeRequestDTO);
        MockHttpServletRequestBuilder requestBuilder = post("/api/product-types")
                .contentType(MediaType.APPLICATION_JSON)
                .content(productTypeDTOString)
                .header("Authorization", "Bearer " + jwtUtil.generateToken(userDetails));
        mockMvc.perform(requestBuilder)
                .andExpect(status().isNotAcceptable())
                .andExpect(jsonPath("$.errorMessage").value("Product type name already exists."));
    }
}