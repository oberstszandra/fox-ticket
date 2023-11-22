package com.example.foxticket.services;

import com.example.foxticket.dtos.AuthenticationRequestDTO;
import com.example.foxticket.dtos.UserRequestDTO;
import com.example.foxticket.dtos.UserUpdateRequestDTO;
import com.example.foxticket.dtos.UserUpdateResponseDTO;
import com.example.foxticket.models.ErrorMessage;
import com.example.foxticket.models.User;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface UserService {

    List<User> findAll();

    Optional<User> findById(Long id);

    boolean checkIfEmailIsTaken(String email);

    User updateUser(User user);

    User saveNewUser(UserRequestDTO userRequestDTO) throws IOException;

    UserUpdateResponseDTO modifyUser(UserUpdateRequestDTO userUpdateRequestDTO);

    boolean requestDataIsIncluded(UserRequestDTO userRequestDTO);

    ErrorMessage generateErrorForInvalidUpdate(UserUpdateRequestDTO userUpdateRequestDTO);

    boolean loginUserMissingEmail(AuthenticationRequestDTO authenticationRequestDTO);

    boolean loginUserAuthenticate(AuthenticationRequestDTO authenticationRequestDTO);

    boolean loginUserMissingPassword(AuthenticationRequestDTO authenticationRequestDTO);

    String createJwtToken(String email);

    boolean verificateEmail(String verificationCode);

    boolean authenticatedUserIsVerifiedOrNot(User user);

    void sendNewVerificationEmail(User user);
}