package com.example.foxticket.services;

import com.example.foxticket.models.User;

import java.io.IOException;

public interface EmailService {
    String sendEmailToUser(User user, String templateId, String url) throws IOException;
}
