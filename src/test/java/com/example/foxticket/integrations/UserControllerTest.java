package com.example.foxticket.integrations;

import com.example.foxticket.dtos.AuthenticationRequestDTO;
import com.example.foxticket.dtos.UserRequestDTO;
import com.example.foxticket.dtos.UserUpdateRequestDTO;
import com.example.foxticket.models.User;
import com.example.foxticket.models.Verification;
import com.example.foxticket.repositories.UserRepository;
import com.example.foxticket.security.MyUserDetails;
import com.example.foxticket.security.MyUserDetailsService;
import com.example.foxticket.security.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(value = "/db/test/clear_tables.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private UserRepository userRepository;

    private PasswordEncoder passwordEncoder;

    private ObjectMapper mapper;

    private JwtUtil jwtUtil;

    private MyUserDetailsService myUserDetailsService;

    @Autowired
    public UserControllerTest(UserRepository userRepository, PasswordEncoder passwordEncoder, ObjectMapper objectMapper, JwtUtil jwtUtil, MyUserDetailsService myUserDetailsService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mapper = objectMapper;
        this.jwtUtil = jwtUtil;
        this.myUserDetailsService = myUserDetailsService;
    }

    @Test
    public void registerUser_whenRequestIsValid_returnsUserDto() throws Exception {
        UserRequestDTO userRequestDTO = new UserRequestDTO("John Doe", "johndoe@test.com", "password123");
        String userRequestDTOAsString = mapper.writeValueAsString(userRequestDTO);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequestDTOAsString))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value(userRequestDTO.getEmail()))
                .andExpect(jsonPath("$.name").doesNotExist())
                .andExpect(jsonPath("$.admin").value(false))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    public void registerUser_whenPasswordIsMissing_returnsCorrectErrorResponse() throws Exception {
        UserRequestDTO userRequestDTO = new UserRequestDTO("John Doe", "johndoe@test.com", null);
        String userRequestDTOAsString = mapper.writeValueAsString(userRequestDTO);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequestDTOAsString))
                .andExpect(status().isNotAcceptable())
                .andExpect(jsonPath("$.errorMessage").value("Password is required."));
    }

    @Test
    public void registerUser_whenNameIsMissing_returnsCorrectErrorResponse() throws Exception {
        UserRequestDTO userRequestDTO = new UserRequestDTO(null, "johndoe@test.com", "password123");
        String userRequestDTOAsString = mapper.writeValueAsString(userRequestDTO);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequestDTOAsString))
                .andExpect(status().isNotAcceptable())
                .andExpect(jsonPath("$.errorMessage").value("Name is required."));
    }

    @Test
    public void registerUser_whenEmailIsMissing_returnsCorrectErrorResponse() throws Exception {
        UserRequestDTO userRequestDTO = new UserRequestDTO("John Doe", null, "password123");
        String userRequestDTOAsString = mapper.writeValueAsString(userRequestDTO);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequestDTOAsString))
                .andExpect(status().isNotAcceptable())
                .andExpect(jsonPath("$.errorMessage").value("Email is required."));

    }

    @Test
    public void registerUser_whenEverythingIsMissing_returnsCorrectErrorResponse() throws Exception {
        UserRequestDTO userRequestDTO = new UserRequestDTO(null, null, null);
        String userRequestDTOAsString = mapper.writeValueAsString(userRequestDTO);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequestDTOAsString))
                .andExpect(status().isNotAcceptable())
                .andExpect(jsonPath("$.errorMessage").value("Name, email and password are required."));
    }

    @Test
    public void registerUser_whenPasswordIsTooShort_returnsCorrectErrorResponse() throws Exception {
        UserRequestDTO userRequestDTO = new UserRequestDTO("John Doe", "johndoe@test.com", "1234");
        String userRequestDTOAsString = mapper.writeValueAsString(userRequestDTO);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequestDTOAsString))
                .andExpect(status().isNotAcceptable())
                .andExpect(jsonPath("$.errorMessage").value("Password must be at least 8 characters."));
    }

    @Test
    public void updateUser_whenRequestIsEmpty_returnsCorrectErrorResponse() throws Exception {
        UserUpdateRequestDTO userUpdateRequestDTO = new UserUpdateRequestDTO();
        String userUpdateRequestDTOAsString = mapper.writeValueAsString(userUpdateRequestDTO);

        mockMvc.perform(patch("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userUpdateRequestDTOAsString));
    }

    @Test
    public void loginWithUser_whenRequestEmailAndPasswordIsValid_returnsJwtTokenAndStatus() throws Exception {
        User userTest = new User("Doe John", "doejohn@test.com", passwordEncoder.encode("1234"), false);
        userRepository.save(userTest);
        AuthenticationRequestDTO authenticationRequestDTO = new AuthenticationRequestDTO("doejohn@test.com", "1234");
        String authenticationRequestString = mapper.writeValueAsString(authenticationRequestDTO);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/users/login").contentType(MediaType.APPLICATION_JSON).content(authenticationRequestString))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.status").value("ok"));
    }

    @Test
    public void loginWithUser_whenRequestEmailIsNotValid_returnsCorrectErrorResponse() throws Exception {
        User userTest = new User("Doe John", "doejohn@test.com", passwordEncoder.encode("1234"), false);
        userRepository.save(userTest);
        AuthenticationRequestDTO authenticationRequestDTO = new AuthenticationRequestDTO("sdfsf", "1234");
        String authenticationRequestString = mapper.writeValueAsString(authenticationRequestDTO);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/users/login").contentType(MediaType.APPLICATION_JSON).content(authenticationRequestString))
                .andExpect(status().isNotAcceptable())
                .andExpect(jsonPath("$.errorMessage").value("Email or password is incorrect."));
    }

    @Test
    public void loginWithUser_whenRequestPasswordIsNotValid_returnsCorrectErrorResponse() throws Exception {
        User userTest = new User("Doe John", "doejohn@test.com", passwordEncoder.encode("1234"), false);
        userRepository.save(userTest);
        AuthenticationRequestDTO authenticationRequestDTO = new AuthenticationRequestDTO("doejohn@test.com", "12345");
        String authenticationRequestString = mapper.writeValueAsString(authenticationRequestDTO);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/users/login").contentType(MediaType.APPLICATION_JSON).content(authenticationRequestString))
                .andExpect(status().isNotAcceptable())
                .andExpect(jsonPath("$.errorMessage").value("Email or password is incorrect."));
    }

    @Test
    public void loginWithUser_whenRequestPasswordAndEmailIsNotValid_returnsCorrectErrorResponse() throws Exception {
        User userTest = new User("Doe John", "doejohn@test.com", passwordEncoder.encode("1234"), false);
        userRepository.save(userTest);
        AuthenticationRequestDTO authenticationRequestDTO = new AuthenticationRequestDTO("johndoe@test.commmmmmm", "12345");
        String authenticationRequestString = mapper.writeValueAsString(authenticationRequestDTO);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/users/login").contentType(MediaType.APPLICATION_JSON).content(authenticationRequestString))
                .andExpect(status().isNotAcceptable())
                .andExpect(jsonPath("$.errorMessage").value("Email or password is incorrect."));
    }

    @Test
    public void loginWithUser_whenRequestEmailIsMissing_returnsCorrectErrorResponse() throws Exception {
        AuthenticationRequestDTO authenticationRequestDTO = new AuthenticationRequestDTO(null, "1234");
        String authenticationRequestString = mapper.writeValueAsString(authenticationRequestDTO);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/users/login").contentType(MediaType.APPLICATION_JSON).content(authenticationRequestString))
                .andExpect(status().isNotAcceptable())
                .andExpect(jsonPath("$.errorMessage").value("Email is required."));
    }

    @Test
    public void loginWithUser_whenRequestEmailIsBlank_returnsCorrectErrorResponse() throws Exception {
        AuthenticationRequestDTO authenticationRequestDTO = new AuthenticationRequestDTO("", "1234");
        String authenticationRequestString = mapper.writeValueAsString(authenticationRequestDTO);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/users/login").contentType(MediaType.APPLICATION_JSON).content(authenticationRequestString))
                .andExpect(status().isNotAcceptable())
                .andExpect(jsonPath("$.errorMessage").value("Email is required."));
    }

    @Test
    public void loginWithUser_whenRequestPasswordIsMissing_returnsCorrectErrorResponse() throws Exception {
        AuthenticationRequestDTO authenticationRequestDTO = new AuthenticationRequestDTO("johndoe@test.com", null);
        String authenticationRequestString = mapper.writeValueAsString(authenticationRequestDTO);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/users/login").contentType(MediaType.APPLICATION_JSON).content(authenticationRequestString))
                .andExpect(status().isNotAcceptable())
                .andExpect(jsonPath("$.errorMessage").value("Password is required."));
    }

    @Test
    public void loginWithUser_whenRequestPasswordIsBlank_returnsCorrectErrorResponse() throws Exception {
        AuthenticationRequestDTO authenticationRequestDTO = new AuthenticationRequestDTO("johndoe@test.com", " ");
        String authenticationRequestString = mapper.writeValueAsString(authenticationRequestDTO);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/users/login").contentType(MediaType.APPLICATION_JSON).content(authenticationRequestString))
                .andExpect(status().isNotAcceptable())
                .andExpect(jsonPath("$.errorMessage").value("Password is required."));
    }

    @Test
    public void loginWithUser_whenRequestPasswordAndEmailIsMissing_returnsCorrectErrorResponse() throws Exception {
        AuthenticationRequestDTO authenticationRequestDTO = new AuthenticationRequestDTO(null, null);
        String authenticationRequestString = mapper.writeValueAsString(authenticationRequestDTO);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/users/login").contentType(MediaType.APPLICATION_JSON).content(authenticationRequestString))
                .andExpect(status().isNotAcceptable())
                .andExpect(jsonPath("$.errorMessage").value("All fields are required."));
    }

    @Test
    public void updateUser_whenRequestContainsIdAndNameAndEmailAndPassword_returnsUserUpdateResponseDto() throws Exception {
        User testUser = new User("Bianka", "m@b.hu", "password", false);
        userRepository.save(testUser);
        UserUpdateRequestDTO userUpdateRequestDTO = new UserUpdateRequestDTO(testUser.getId(), "NameExample", "EmailExample", "PasswordExample");
        String userUpdateRequestDTOAsString = mapper.writeValueAsString(userUpdateRequestDTO);

        mockMvc.perform(patch("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userUpdateRequestDTOAsString))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value(userUpdateRequestDTO.getEmail()))
                .andExpect(jsonPath("$.name").value(userUpdateRequestDTO.getName()))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    public void updateUser_whenRequestContainsIdAndEmail_returnsUserUpdateResponseDTO() throws Exception {
        User testUser = new User("Bianka", "m@b.hu", "password", false);
        userRepository.save(testUser);
        UserUpdateRequestDTO userUpdateRequestDTO = new UserUpdateRequestDTO(testUser.getId(), null, "EXampleEmail@gmail.com", null);
        String userUpdateRequestDTOAsString = mapper.writeValueAsString(userUpdateRequestDTO);

        mockMvc.perform(patch("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userUpdateRequestDTOAsString))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value(userUpdateRequestDTO.getEmail()))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    public void updateUser_whenRequestContainsIdAndName_returnsUserUpdateResponseDTO() throws Exception {
        User testUser = new User("Bianka", "m@b.hu", "password", false);
        userRepository.save(testUser);
        UserUpdateRequestDTO userUpdateRequestDTO = new UserUpdateRequestDTO(testUser.getId(), "EXampleName", null, null);
        String userUpdateRequestDTOAsString = mapper.writeValueAsString(userUpdateRequestDTO);

        mockMvc.perform(patch("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userUpdateRequestDTOAsString))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value(userUpdateRequestDTO.getName()))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    public void updateUser_whenRequestContainsIdAndPassword_returnsUserUpdateResponseDTO() throws Exception {
        User testUser = new User("Bianka", "m@b.hu", "password", false);
        userRepository.save(testUser);
        UserUpdateRequestDTO userUpdateRequestDTO = new UserUpdateRequestDTO(testUser.getId(), null, null, "EXamplePassword");
        String userUpdateRequestDTOAsString = mapper.writeValueAsString(userUpdateRequestDTO);

        mockMvc.perform(patch("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userUpdateRequestDTOAsString))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    public void updateUser_whenRequestContainsIdAndPasswordAndName_returnsUserUpdateResponseDTO() throws Exception {
        User testUser = new User("Bianka", "m@b.hu", "password", false);
        userRepository.save(testUser);
        UserUpdateRequestDTO userUpdateRequestDTO = new UserUpdateRequestDTO(testUser.getId(), "EXampleName", null, "EXamplePassword");
        String userUpdateRequestDTOAsString = mapper.writeValueAsString(userUpdateRequestDTO);

        mockMvc.perform(patch("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userUpdateRequestDTOAsString))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value(userUpdateRequestDTO.getName()))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    public void updateUser_whenRequestContainsIdAndPasswordAndEmail_returnsUserUpdateResponseDTO() throws Exception {
        User testUser = new User("Bianka", "m@b.hu", "password", false);
        userRepository.save(testUser);
        UserUpdateRequestDTO userUpdateRequestDTO = new UserUpdateRequestDTO(testUser.getId(), null, "EXampleEmail", "EXamplePassword");
        String userUpdateRequestDTOAsString = mapper.writeValueAsString(userUpdateRequestDTO);

        mockMvc.perform(patch("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userUpdateRequestDTOAsString))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value(userUpdateRequestDTO.getEmail()))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    public void updateUser_whenRequestContainsIdAndNamedAndEmail_returnsUserUpdateResponseDTO() throws Exception {
        User testUser = new User("Bianka", "m@b.hu", "password", false);
        userRepository.save(testUser);
        UserUpdateRequestDTO userUpdateRequestDTO = new UserUpdateRequestDTO(testUser.getId(), "EXampleName", "EXampleEmail", null);
        String userUpdateRequestDTOAsString = mapper.writeValueAsString(userUpdateRequestDTO);

        mockMvc.perform(patch("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userUpdateRequestDTOAsString))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value(userUpdateRequestDTO.getName()))
                .andExpect(jsonPath("$.email").value(userUpdateRequestDTO.getEmail()))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    public void loginWithUser_whenRequestPasswordAndEmailIsBlank_returnsCorrectErrorResponse() throws Exception {
        AuthenticationRequestDTO authenticationRequestDTO = new AuthenticationRequestDTO(" ", "");
        String authenticationRequestString = mapper.writeValueAsString(authenticationRequestDTO);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/users/login").contentType(MediaType.APPLICATION_JSON).content(authenticationRequestString))
                .andExpect(status().isNotAcceptable())
                .andExpect(jsonPath("$.errorMessage").value("All fields are required."));
    }

    @Test
    public void emailVerification_WhenUserIsNotVerified_ReturnsCorrectSuccessResponse() throws Exception {
        User testUser = new User("TestUser", "test@gmail.com", new BCryptPasswordEncoder().encode("test"), true);
        Verification verification = new Verification();
        testUser.addVerification(verification);
        userRepository.save(testUser);
        MyUserDetails userDetails = myUserDetailsService.loadUserByUsername("test@gmail.com");
        MockHttpServletRequestBuilder requestBuilder = get("/api/email-verification/" + verification.getVerificationCode())
                .header("Authorization", "Bearer " + jwtUtil.generateToken(userDetails));
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success_message").value("Your email has been verified!"));
    }

    @Test
    public void emailVerification_WhenVerificationIsExpired_returnsCorrectErrorResponse() throws Exception {
        User testUser = new User("TestUser", "test@gmail.com", new BCryptPasswordEncoder().encode("test"), true);
        Clock clock = Clock.fixed(Instant.parse("2022-08-01T14:15:30.00Z"), ZoneId.of("Europe/Budapest"));
        LocalDateTime localDateTime = LocalDateTime.now(clock);
        Verification verification = new Verification();
        try (MockedStatic<LocalDateTime> localDateTimeMockedStatic = Mockito.mockStatic(LocalDateTime.class)) {
            localDateTimeMockedStatic.when(LocalDateTime::now).thenReturn(localDateTime);
            verification = new Verification();
        }
        testUser.addVerification(verification);
        userRepository.save(testUser);
        MyUserDetails userDetails = myUserDetailsService.loadUserByUsername("test@gmail.com");
        MockHttpServletRequestBuilder requestBuilder = get("/api/email-verification/" + verification.getVerificationCode())
                .header("Authorization", "Bearer " + jwtUtil.generateToken(userDetails));
        mockMvc.perform(requestBuilder)
                .andExpect(status().isNotAcceptable())
                .andExpect(jsonPath("$.errorMessage").value("The verification code is expired!"));
    }

    @Test
    public void sendNewVerification_WhenUserIsExist_ReturnsCorrectSuccessResponse() throws Exception {
        Optional<User> user = Optional.of(new User("TestUser", "valaki@gmail.com", "cicamica", true));
        userRepository.save(user.get());
        MyUserDetails userDetails = myUserDetailsService.loadUserByUsername("valaki@gmail.com");
        MockHttpServletRequestBuilder requestBuilder = post("/api/new-verification")
                .header("Authorization", "Bearer " + jwtUtil.generateToken(userDetails));
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success_message").value("New verification email has been sent!"));
    }
}