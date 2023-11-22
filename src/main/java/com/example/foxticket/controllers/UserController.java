package com.example.foxticket.controllers;

import com.example.foxticket.dtos.*;
import com.example.foxticket.models.ErrorMessage;
import com.example.foxticket.models.SuccessMessage;
import com.example.foxticket.models.User;
import com.example.foxticket.security.MyUserDetails;
import com.example.foxticket.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping(path = "/api")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(path = "/users")
    public ResponseEntity<?> registerTheUser(@RequestBody UserRequestDTO userRequestDTO) {
        if (userService.requestDataIsIncluded(userRequestDTO)) {
            return new ResponseEntity<>(new ErrorMessage("Name, email and password are required."), HttpStatus.NOT_ACCEPTABLE);
        }
        if (userRequestDTO.getPassword() == null) {
            return new ResponseEntity<>(new ErrorMessage("Password is required."), HttpStatus.NOT_ACCEPTABLE);
        }
        if (userRequestDTO.getName() == null) {
            return new ResponseEntity<>(new ErrorMessage("Name is required."), HttpStatus.NOT_ACCEPTABLE);
        }
        if (userRequestDTO.getEmail() == null) {
            return new ResponseEntity<>(new ErrorMessage("Email is required."), HttpStatus.NOT_ACCEPTABLE);
        }
        if (userService.checkIfEmailIsTaken(userRequestDTO.getEmail())) {
            return new ResponseEntity<>(new ErrorMessage("Email is already taken."), HttpStatus.NOT_ACCEPTABLE);
        }
        if (userRequestDTO.getPassword().length() < 8) {
            return new ResponseEntity<>(new ErrorMessage("Password must be at least 8 characters."), HttpStatus.NOT_ACCEPTABLE);
        } else {
            User user;
            try {
                user = userService.saveNewUser(userRequestDTO);
            } catch (IOException ioException) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorMessage("Something went wrong with sending verification email, please try again later!"));
            }
            UserResponseDTO userDTO = new UserResponseDTO(user.getId(), user.getEmail(), user.isAdmin());
            return new ResponseEntity<>(userDTO, HttpStatus.CREATED);
        }
    }

    @PatchMapping(path = "/users")
    public ResponseEntity<?> updateUserInformation(@RequestBody UserUpdateRequestDTO userUpdateRequestDTO) {
        ErrorMessage generatedError = userService.generateErrorForInvalidUpdate(userUpdateRequestDTO);
        if (generatedError != null) {
            return new ResponseEntity<>(generatedError, HttpStatus.NOT_ACCEPTABLE);
        } else {
            return new ResponseEntity<>(userService.modifyUser(userUpdateRequestDTO), HttpStatus.OK);
        }
    }

    @PostMapping(path = "/users/login")
    public ResponseEntity<?> loginUser(@RequestBody AuthenticationRequestDTO authenticationRequestDTO) {
        if (userService.loginUserMissingEmail(authenticationRequestDTO) && userService.loginUserMissingPassword(authenticationRequestDTO)) {
            return new ResponseEntity<>(new ErrorMessage("All fields are required."), HttpStatus.NOT_ACCEPTABLE);
        }
        if (userService.loginUserMissingPassword(authenticationRequestDTO)) {
            return new ResponseEntity<>(new ErrorMessage("Password is required."), HttpStatus.NOT_ACCEPTABLE);
        }
        if (userService.loginUserMissingEmail(authenticationRequestDTO)) {
            return new ResponseEntity<>(new ErrorMessage("Email is required."), HttpStatus.NOT_ACCEPTABLE);
        }
        if (!userService.loginUserAuthenticate(authenticationRequestDTO)) {
            return new ResponseEntity<>(new ErrorMessage("Email or password is incorrect."), HttpStatus.NOT_ACCEPTABLE);
        }
        return ResponseEntity.status(HttpStatus.OK).body(new AuthenticationResponseDTO("ok", userService.createJwtToken(authenticationRequestDTO.getEmail())));
    }

    @GetMapping(path = "/email-verification/{verification-code}")
    public ResponseEntity<?> emailVerification(@PathVariable(name = "verification-code") String verificationCode) {
        if (userService.verificateEmail(verificationCode)) {
            return ResponseEntity.status(HttpStatus.OK).body(new SuccessMessage("Your email has been verified!"));
        }
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(new ErrorMessage("The verification code is expired!"));
    }

    @PostMapping("/new-verification")
    public ResponseEntity<?> sendNewVerification(@AuthenticationPrincipal MyUserDetails myUserDetails) {
        Optional<User> loggedInUser = userService.findById(myUserDetails.getId());
        if (loggedInUser.isPresent()) {
            userService.sendNewVerificationEmail(loggedInUser.get());
            return ResponseEntity.status(HttpStatus.OK).body(new SuccessMessage("New verification email has been sent!"));
        } else {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
    }
}