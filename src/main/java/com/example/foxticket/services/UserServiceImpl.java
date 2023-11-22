package com.example.foxticket.services;

import com.example.foxticket.dtos.AuthenticationRequestDTO;
import com.example.foxticket.dtos.UserRequestDTO;
import com.example.foxticket.dtos.UserUpdateRequestDTO;
import com.example.foxticket.dtos.UserUpdateResponseDTO;
import com.example.foxticket.models.ErrorMessage;
import com.example.foxticket.models.User;
import com.example.foxticket.models.Verification;
import com.example.foxticket.repositories.UserRepository;
import com.example.foxticket.repositories.VerificationRepository;
import com.example.foxticket.models.Cart;
import com.example.foxticket.security.MyUserDetails;
import com.example.foxticket.security.MyUserDetailsService;
import com.example.foxticket.security.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MyUserDetailsService myUserDetailsService;
    private final JwtUtil jwtUtil;
    private final VerificationRepository verificationRepository;
    private final EmailService emailService;

    private final Environment env;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, MyUserDetailsService myUserDetailsService, JwtUtil jwtUtil, VerificationRepository verificationRepository, EmailService emailService, Environment env) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.myUserDetailsService = myUserDetailsService;
        this.verificationRepository = verificationRepository;
        this.emailService = emailService;
        this.env = env;
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public boolean checkIfEmailIsTaken(String email) {
        boolean emailIsTaken = false;
        List<User> userList = findAll();
        for (User user : userList) {
            if (user.getEmail().equals(email)) {
                emailIsTaken = true;
                break;
            }
        }
        return emailIsTaken;
    }

    @Override
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public User saveNewUser(UserRequestDTO userRequestDTO) throws IOException {
        User user = new User(userRequestDTO.getName(), userRequestDTO.getEmail(), passwordEncoder.encode(userRequestDTO.getPassword()), false);
        Verification verification = new Verification();
        Cart userCart = new Cart(user);
        user.setCart(userCart);
        user.addVerification(verification);
        emailService.sendEmailToUser(user, env.getProperty("VALIDATE_TEMPLATE_ID"), env.getProperty("VALIDATE_URL") + verification.getVerificationCode());
        return userRepository.save(user);
    }

    @Override
    public boolean requestDataIsIncluded(UserRequestDTO userRequestDTO) {
        return userRequestDTO.getPassword() == null && userRequestDTO.getName() == null && userRequestDTO.getEmail() == null;
    }

    @Override
    public ErrorMessage generateErrorForInvalidUpdate(UserUpdateRequestDTO userUpdateRequestDTO) {
        ErrorMessage errorMessage;
        if (userUpdateRequestDTO.getName() == null && userUpdateRequestDTO.getEmail() == null && userUpdateRequestDTO.getPassword() == null) {
            return errorMessage = new ErrorMessage("All fields are required.");
        }
        if (userUpdateRequestDTO.getEmail() != null && checkIfEmailIsTaken(userUpdateRequestDTO.getEmail())) {
            return errorMessage = new ErrorMessage("Email is already taken.");
        }
        if (userUpdateRequestDTO.getPassword() != null && userUpdateRequestDTO.getPassword().length() < 8) {
            return errorMessage = new ErrorMessage("Password must be at least 8 characters.");
        }
        return null;
    }

    @Override
    public UserUpdateResponseDTO modifyUser(UserUpdateRequestDTO userUpdateRequestDTO) {
        User selectedUser = findById(userUpdateRequestDTO.getId()).get();
        if (userUpdateRequestDTO.getName() != null) {
            selectedUser.setName(userUpdateRequestDTO.getName());
        }
        if (userUpdateRequestDTO.getEmail() != null) {
            selectedUser.setEmail(userUpdateRequestDTO.getEmail());
        }
        updateUser(selectedUser);
        UserUpdateResponseDTO updatedUser = new UserUpdateResponseDTO(selectedUser.getId(), selectedUser.getName(), selectedUser.getEmail());
        return updatedUser;
    }

    @Override
    public boolean loginUserMissingEmail(AuthenticationRequestDTO authenticationRequestDTO) {
        if (authenticationRequestDTO.getEmail() == null) {
            return true;
        } else {
            return authenticationRequestDTO.getEmail().isBlank();
        }
    }

    @Override
    public boolean loginUserMissingPassword(AuthenticationRequestDTO authenticationRequestDTO) {
        if (authenticationRequestDTO.getPassword() == null) {
            return true;
        } else {
            return authenticationRequestDTO.getPassword().isBlank();
        }
    }

    @Override
    public boolean loginUserAuthenticate(AuthenticationRequestDTO authenticationRequestDTO) {
        Optional<User> user = userRepository.findByEmail(authenticationRequestDTO.getEmail());
        if (user.isEmpty()) {
            return false;
        }
        return passwordEncoder.matches(authenticationRequestDTO.getPassword(), user.get().getPassword());
    }

    @Override
    public String createJwtToken(String email) {
        MyUserDetails userDetails = myUserDetailsService.loadUserByUsername(email);
        return jwtUtil.generateToken(userDetails);
    }

    @Override
    public boolean verificateEmail(String verificationCode) {
        Optional<Verification> actualVerification = verificationRepository.findByVerificationCode(verificationCode);
        if (actualVerification.isPresent()) {
            LocalDateTime expiryDate = actualVerification.get().getCreatedDate().plusDays(2);
            LocalDateTime nowDate = LocalDateTime.now();
            if (nowDate.isBefore(expiryDate)) {
                actualVerification.get().setVerified(true);
                verificationRepository.save(actualVerification.get());
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean authenticatedUserIsVerifiedOrNot(User user) {
        return user.getVerifications().get(user.getVerifications().size() - 1).isVerified();
    }

    @Override
    public void sendNewVerificationEmail(User user) {
        Verification newVerification = new Verification();
        newVerification.setUser(user);
        user.addVerification(newVerification);
        verificationRepository.save(newVerification);
    }
}