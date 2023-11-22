package com.example.foxticket.units;

import com.example.foxticket.dtos.AuthenticationRequestDTO;
import com.example.foxticket.dtos.UserRequestDTO;
import com.example.foxticket.dtos.UserUpdateRequestDTO;
import com.example.foxticket.dtos.UserUpdateResponseDTO;
import com.example.foxticket.models.User;
import com.example.foxticket.models.Verification;
import com.example.foxticket.repositories.UserRepository;
import com.example.foxticket.repositories.VerificationRepository;
import com.example.foxticket.security.MyUserDetails;
import com.example.foxticket.security.MyUserDetailsService;
import com.example.foxticket.security.util.JwtUtil;
import com.example.foxticket.services.EmailService;
import com.example.foxticket.services.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;


public class UserServiceImplTest {
    private final PasswordEncoder passwordEncoder;
    private final MyUserDetailsService myUserDetailsService;
    private final JwtUtil jwtUtil;
    private UserRepository userRepository;
    private UserServiceImpl userService;
    private VerificationRepository verificationRepository;
    private EmailService emailService;
    private Environment env;

    public UserServiceImplTest() {
        userRepository = Mockito.mock(UserRepository.class);
        passwordEncoder = Mockito.mock(PasswordEncoder.class);
        myUserDetailsService = Mockito.mock(MyUserDetailsService.class);
        jwtUtil = Mockito.mock(JwtUtil.class);
        verificationRepository = Mockito.mock(VerificationRepository.class);
        emailService = Mockito.mock(EmailService.class);
        env = Mockito.mock(Environment.class);
        userService = new UserServiceImpl(userRepository, passwordEncoder, myUserDetailsService, jwtUtil, verificationRepository, emailService, env);
    }

    @Test
    public void modifyUser_WithAllInfo_ReturnsUpdatedUser() {
        User existingUser = new User("oldName", "oldEmail", null, false);
        UserUpdateRequestDTO requestDTO = new UserUpdateRequestDTO(existingUser.getId(), "newName", "newEmail", null);
        when(userRepository.findById(requestDTO.getId())).thenReturn(Optional.of(existingUser));

        UserUpdateResponseDTO responseDTO = userService.modifyUser(requestDTO);
        assertEquals(requestDTO.getId(), responseDTO.getId());
        assertEquals(requestDTO.getName(), responseDTO.getName());
        assertEquals(requestDTO.getEmail(), responseDTO.getEmail());
        assertEquals(requestDTO.getEmail(), existingUser.getEmail());
        assertEquals(requestDTO.getName(), existingUser.getName());
    }

    @Test
    public void checkIfEmailIsTaken_whenEmailAlreadyExists_returnsTrue() throws Exception {
        List<User> userList = new ArrayList<>();
        userList.add(new User("John Doe", "johndoe@test.com", "password123", false));
        userList.add(new User("Jane Smith", "janesmith@test.com", "password456", false));
        when(userService.findAll()).thenReturn(userList);
        boolean emailTaken = userService.checkIfEmailIsTaken("johndoe@test.com");
        assertTrue(emailTaken);
    }

    @Test
    public void getAllUsers_ReturnsAllUsers() throws Exception {
        List<User> expectedUsers = new ArrayList<>();
        expectedUsers.add(new User("John Doe", "johndoe@test.com", "password123", false));
        expectedUsers.add(new User("Jane Smith", "janesmith@test.com", "password456", false));
        when(userRepository.findAll()).thenReturn(expectedUsers);
        List<User> actualUsers = userService.findAll();
        assertNotNull(actualUsers);
        assertFalse(actualUsers.isEmpty());
        assertEquals(expectedUsers.size(), actualUsers.size());
        assertEquals(expectedUsers.get(0), actualUsers.get(0));
        assertEquals(expectedUsers.get(1), actualUsers.get(1));
    }

    @Test
    public void getAllUsers_ReturnsEmptyList() throws Exception {
        List<User> expectedUsers = new ArrayList<>();
        when(userRepository.findAll()).thenReturn(expectedUsers);
        List<User> actualUsers = userService.findAll();
        assertNotNull(actualUsers);
        assertTrue(actualUsers.isEmpty());
        assertEquals(expectedUsers.size(), actualUsers.size());
    }

    @Test
    public void save_validUser_returnsSavedUser() throws IOException {
        String name = "John Doe";
        String email = "johndoe@test.com";
        String password = "password123";
        UserRequestDTO userRequestDTO = new UserRequestDTO(name, email, password);
        User savedUser = new User(name, email, password, false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        User actualUser = userService.saveNewUser(userRequestDTO);
        assertEquals(savedUser, actualUser);
    }

    @Test
    public void saveUser_whenExceptionOccurs_returnsNull() throws IOException {
        String name = "John Doe";
        String email = "johndoe@test.com";
        String password = "password123";
        UserRequestDTO userRequestDTO = new UserRequestDTO(name, email, password);
        User user = new User(name, email, password, false);
        when(userRepository.save(user)).thenThrow(new RuntimeException("Failed to save user"));
        User actualUser = userService.saveNewUser(userRequestDTO);
        assertNull(actualUser);
    }

    @Test
    public void loginUserMissingEmail_whenEmailIsNull_returnsTrue() {
        AuthenticationRequestDTO authenticationRequestDTO = new AuthenticationRequestDTO(null, "89");
        assertTrue(userService.loginUserMissingEmail(authenticationRequestDTO));
    }

    @Test
    public void loginUserMissingEmail_whenEmailIsBlank_returnsTrue() {
        AuthenticationRequestDTO authenticationRequestDTO = new AuthenticationRequestDTO(" ", "89");
        assertTrue(userService.loginUserMissingEmail(authenticationRequestDTO));
    }

    @Test
    public void loginUserMissingEmail_whenEmailExist_returnsFalse() {
        AuthenticationRequestDTO authenticationRequestDTO = new AuthenticationRequestDTO("email@email.com", "123");
        assertFalse(userService.loginUserMissingEmail(authenticationRequestDTO));
    }

    @Test
    public void loginUserMissingPassword_whenPasswordIsNull_returnsTrue() {
        AuthenticationRequestDTO authenticationRequestDTO = new AuthenticationRequestDTO("email@email.com", null);
        assertTrue(userService.loginUserMissingPassword(authenticationRequestDTO));
    }

    @Test
    public void loginUserMissingPassword_whenPasswordIsBlank_returnsTrue() {
        AuthenticationRequestDTO authenticationRequestDTO = new AuthenticationRequestDTO("email@email.com", "");
        assertTrue(userService.loginUserMissingPassword(authenticationRequestDTO));
    }

    @Test
    public void loginUserMissingPassword_whenPasswordExist_returnsFalse() {
        AuthenticationRequestDTO authenticationRequestDTO = new AuthenticationRequestDTO("email@email.com", "123");
        assertFalse(userService.loginUserMissingPassword(authenticationRequestDTO));
    }

    @Test
    public void loginUserAuthenticate_whenEmailIsNotValid_returnsFalse() {
        AuthenticationRequestDTO authenticationRequestDTO = new AuthenticationRequestDTO("doejohn@test.com", "1234");
        when(userRepository.findByEmail(authenticationRequestDTO.getEmail())).thenReturn(Optional.empty());
        assertFalse(userService.loginUserAuthenticate(authenticationRequestDTO));
    }

    @Test
    public void loginUserAuthenticate_whenPasswordIsNotValid_returnsFalse() {
        User user = new User("John", "doejohn@test.com", "1234", false);
        AuthenticationRequestDTO authenticationRequestDTO = new AuthenticationRequestDTO("doejohn@test.com", "1234");
        when(userRepository.findByEmail(authenticationRequestDTO.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(user.getPassword(), authenticationRequestDTO.getPassword())).thenReturn(false);
        assertFalse(userService.loginUserAuthenticate(authenticationRequestDTO));
    }

    @Test
    public void loginUserAuthenticate_whenPasswordAndEmailAreValid_returnsTrue() {
        User user = new User("John", "doejohn@test.com", "1234", false);
        AuthenticationRequestDTO authenticationRequestDTO = new AuthenticationRequestDTO("doejohn@test.com", "1234");
        when(userRepository.findByEmail(authenticationRequestDTO.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(user.getPassword(), authenticationRequestDTO.getPassword())).thenReturn(true);
        assertTrue(userService.loginUserAuthenticate(authenticationRequestDTO));
    }

    @Test
    public void createJwtToken_whenUserIsValid_returnsJwtToken() {
        User user = new User("John", "doejohn@test.com", "1234", false);
        MyUserDetails myUserDetails = new MyUserDetails(user);
        when(myUserDetailsService.loadUserByUsername(user.getEmail())).thenReturn(myUserDetails);
        when(jwtUtil.generateToken(myUserDetails)).thenReturn("token");
        assertEquals(userService.createJwtToken(user.getEmail()), "token");
    }

    @Test
    public void verificateEmail_whenVerificationIsNotVerified_returnsTrue() {
        Verification verification = new Verification();
        when(verificationRepository.findByVerificationCode(verification.getVerificationCode())).thenReturn(Optional.of(verification));
        assertTrue(userService.verificateEmail(verification.getVerificationCode()));
    }

    @Test
    public void verificateEmail_whenVerificationIsNotExist_returnsFalse() {
        Verification verification = new Verification();
        when(verificationRepository.findByVerificationCode(verification.getVerificationCode())).thenReturn(Optional.empty());
        assertFalse(userService.verificateEmail(verification.getVerificationCode()));
    }

    @Test
    public void verificateEmail_whenVerificationIsExpired_returnsFalse() {
        Verification verification = new Verification();
        Clock clock = Clock.fixed(Instant.parse("2043-08-01T14:15:30.00Z"), ZoneId.of("Europe/Budapest"));
        LocalDateTime localDateTime = LocalDateTime.now(clock);
        try (MockedStatic<LocalDateTime> localDateTimeMockedStatic = Mockito.mockStatic(LocalDateTime.class)) {
            localDateTimeMockedStatic.when(LocalDateTime::now).thenReturn(localDateTime);
            when(verificationRepository.findByVerificationCode(verification.getVerificationCode())).thenReturn(Optional.of(verification));
            assertFalse(userService.verificateEmail(verification.getVerificationCode()));
        }
    }

    @Test
    public void authenticatedUserIsVerifiedOrNot_whenUserIsNotVerificated_returnsFalse() {
        User user = new User("John", "doejohn@test.com", "1234", false);
        Verification verification = new Verification();
        user.addVerification(verification);
        assertFalse(userService.authenticatedUserIsVerifiedOrNot(user));
    }

    @Test
    public void authenticatedUserIsVerifiedOrNot_whenUserIsVerificated_returnsTrue() {
        User user = new User("John", "doejohn@test.com", "1234", false);
        Verification verification = new Verification();
        verification.setVerified(true);
        user.addVerification(verification);
        assertTrue(userService.authenticatedUserIsVerifiedOrNot(user));
    }

    @Test
    public void sendNewVerificationEmail_whenUserHasAlreadyVerification_saveNewVerification() {
        User user = new User("John", "doejohn@test.com", "1234", false);
        Verification verification = new Verification();
        user.addVerification(verification);
        userService.sendNewVerificationEmail(user);
        assertEquals(2, user.getVerifications().size());
    }

    @Test
    public void sendNewVerificationEmail_whenUserDoesntHaveVerification_saveNewVerification() {
        User user = new User("John", "doejohn@test.com", "1234", false);
        userService.sendNewVerificationEmail(user);
        assertEquals(1, user.getVerifications().size());
    }
}