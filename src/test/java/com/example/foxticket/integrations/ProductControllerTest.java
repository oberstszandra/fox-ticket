package com.example.foxticket.integrations;

import com.example.foxticket.dtos.ProductUpdateRequestDTO;
import com.example.foxticket.models.Product;
import com.example.foxticket.models.ProductType;
import com.example.foxticket.models.User;
import com.example.foxticket.repositories.ProductRepository;
import com.example.foxticket.repositories.ProductTypeRepository;
import com.example.foxticket.repositories.UserRepository;
import com.example.foxticket.security.MyUserDetails;
import com.example.foxticket.security.MyUserDetailsService;
import com.example.foxticket.security.util.JwtUtil;
import com.example.foxticket.services.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
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

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(value = "/db/test/clear_tables.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class ProductControllerTest {
    @Autowired
    private MockMvc mockMvc;
    private final JwtUtil jwtUtil;
    private final MyUserDetailsService myUserDetailsService;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductTypeRepository productTypeRepository;
    private ProductService productService;
    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public ProductControllerTest(JwtUtil jwtUtil, MyUserDetailsService myUserDetailsService, UserRepository userRepository,
                                 ProductRepository productRepository, ProductTypeRepository productTypeRepository, ProductService productService) {
        this.jwtUtil = jwtUtil;
        this.myUserDetailsService = myUserDetailsService;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.productTypeRepository = productTypeRepository;
        this.productService = productService;
    }

    @Test
    public void getProducts_WithExistingProduct_ReturnsProducts() throws Exception {
        ProductType ticket = new ProductType("ticket");
        ProductType pass = new ProductType("pass");
        Product product1 = new Product("Day ticket", 360, 24, "You can use this ticket for a whole day!");
        Product product2 = new Product("2 day ticket", 700, 48, "You can use this ticket for 2 days!");
        ticket.setProducts(product1);
        pass.setProducts(product2);
        productTypeRepository.save(ticket);
        productTypeRepository.save(pass);
        productRepository.save(product1);
        productRepository.save(product2);

        userRepository.save(new User("TestUser", "valaki@gmail.com", "cicamica", true));
        MyUserDetails userDetails = myUserDetailsService.loadUserByUsername("valaki@gmail.com");
        MockHttpServletRequestBuilder requestBuilder = get("/api/products")
                .header("Authorization", "Bearer " + jwtUtil.generateToken(userDetails));
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products", Matchers.hasSize(2)))
                .andExpect(jsonPath("$.products[0].name", is("Day ticket")))
                .andExpect(jsonPath("$.products[1].name", is("2 day ticket")));
    }

    @Test
    public void addNewProduct_WhenProductIsValid_ReturnsCreated() throws Exception {
        userRepository.save(new User("admin", "admin@admin.com", "admin", true));
        MyUserDetails userDetails = myUserDetailsService.loadUserByUsername("admin@admin.com");
        ProductType ticket = new ProductType("ticket");
        productTypeRepository.save(ticket);
        ObjectMapper mapper = new ObjectMapper();
        ProductUpdateRequestDTO productRequestDTO = new ProductUpdateRequestDTO("Blablaticket", 360, 24, "You can use this ticket for a whole day!", 1L);
        MockHttpServletRequestBuilder requestBuilder = post("/api/products")
                .header("Authorization", "Bearer " + jwtUtil.generateToken(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(productRequestDTO));
        mockMvc.perform(requestBuilder)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Blablaticket")))
                .andExpect(jsonPath("$.price", is(360)))
                .andExpect(jsonPath("$.duration", is(24)))
                .andExpect(jsonPath("$.description", is("You can use this ticket for a whole day!")))
                .andExpect(jsonPath("$.type", is("ticket")));
    }

    @Test
    public void deleteProduct_whenIdIsFound_deletesProductAndReturnsOk() throws Exception {
        productRepository.save(new Product("Day ticket", 360, 24, "You can use this ticket for a whole day!"));
        userRepository.save(new User("TestUser", "valaki@gmail.com", "cicamica", true));
        MyUserDetails userDetails = myUserDetailsService.loadUserByUsername("valaki@gmail.com");
        MockHttpServletRequestBuilder requestBuilder = delete("/api/products/" + productRepository.findAll().get(0).getId())
                .header("Authorization", "Bearer " + jwtUtil.generateToken(userDetails));
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk());
        List<Product> products = productRepository.findAll();
        Assertions.assertTrue(products.isEmpty());
    }

    @Test
    public void deleteProduct_whenIdIsNotFound_returnsNotFound() throws Exception {
        Long nonExistingId = 0L;
        userRepository.save(new User("TestUser", "valaki@gmail.com", "cicamica", true));
        MyUserDetails userDetails = myUserDetailsService.loadUserByUsername("valaki@gmail.com");
        MockHttpServletRequestBuilder requestBuilder = delete("/api/products/" + nonExistingId)
                .header("Authorization", "Bearer " + jwtUtil.generateToken(userDetails));
        mockMvc.perform(requestBuilder)
                .andExpect(status().isNotFound());
    }

    @Test
    public void modifyProduct_WithValidInput_ReturnsUpdateResponseDTO() throws Exception {
        Product testProduct = new Product("product", 400, 24, "descr");
        ProductType testProductType = new ProductType("ticket");
        testProduct.setProductType(testProductType);
        productTypeRepository.save(testProductType);
        productRepository.save(testProduct);
        ProductUpdateRequestDTO productUpdateRequestDTO = new ProductUpdateRequestDTO("updated", 500, 48, "updated descr", 1L);
        String productUpdateRequestDTOAsString = mapper.writeValueAsString(productUpdateRequestDTO);
        userRepository.save(new User("TestUser", "valaki@gmail.com", new BCryptPasswordEncoder().encode("cicamica"), true));
        MyUserDetails userDetails = myUserDetailsService.loadUserByUsername("valaki@gmail.com");
        MockHttpServletRequestBuilder requestBuilder = put("/api/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(productUpdateRequestDTOAsString)
                .header("Authorization", "Bearer " + jwtUtil.generateToken(userDetails));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value(productUpdateRequestDTO.getName()))
                .andExpect(jsonPath("$.price").value(productUpdateRequestDTO.getPrice()))
                .andExpect(jsonPath("$.duration").value(productUpdateRequestDTO.getDuration()))
                .andExpect(jsonPath("$.description").value(productUpdateRequestDTO.getDescription()))
                .andExpect(jsonPath("$.type").value(productTypeRepository.findById(productUpdateRequestDTO.getTypeId()).get().getName()));
    }

    @Test
    public void modifyProduct_WithInvalidInput_ReturnsErrorMessage() throws Exception {
        Product testProduct = new Product("product", 400, 24, "descr");
        ProductType testProductType = new ProductType("ticket");
        testProduct.setProductType(testProductType);
        productTypeRepository.save(testProductType);
        productRepository.save(testProduct);
        ProductUpdateRequestDTO productUpdateRequestDTO = new ProductUpdateRequestDTO("", 500, 48, "updated descr", 1L);
        String productUpdateRequestDTOAsString = mapper.writeValueAsString(productUpdateRequestDTO);
        userRepository.save(new User("TestUser", "valaki@gmail.com", new BCryptPasswordEncoder().encode("cicamica"), true));
        MyUserDetails userDetails = myUserDetailsService.loadUserByUsername("valaki@gmail.com");
        MockHttpServletRequestBuilder requestBuilder = put("/api/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(productUpdateRequestDTOAsString)
                .header("Authorization", "Bearer " + jwtUtil.generateToken(userDetails));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value("Name is required"));
    }
}
