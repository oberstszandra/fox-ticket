package com.example.foxticket.services;

import com.example.foxticket.interceptors.GeneralInterceptor;
import com.example.foxticket.models.User;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailServiceImpl implements EmailService {
    private Environment env;
    private Logger logger = LoggerFactory.getLogger(GeneralInterceptor.class);

    @Autowired
    public EmailServiceImpl(Environment env) {
        this.env = env;
    }

    @Override
    public String sendEmailToUser(User user, String templateId, String url) throws IOException {
        Email from = new Email(env.getProperty("EMAIL_FROM"));
        Email to = new Email(user.getEmail());
        Mail mail = new Mail();
        DynamicTemplatePersonalization personalization = new DynamicTemplatePersonalization();
        personalization.addTo(to);
        personalization.addDynamicTemplateData("name", user.getName());
        personalization.addDynamicTemplateData("url", url);
        mail.setFrom(from);
        mail.addPersonalization(personalization);
        mail.setTemplateId(templateId);
        SendGrid sg = new SendGrid(env.getProperty("SENDGRID_API_KEY"));
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            if (response.getStatusCode() >= 400) {
                logger.error(response.getStatusCode() + response.getBody() + response.getHeaders());
                throw new IOException();
            } else {
                logger.info(response.getStatusCode() + response.getBody() + response.getHeaders());
            }
        } catch (IOException ex) {
            throw ex;
        }
        return "It works!";
    }
}
