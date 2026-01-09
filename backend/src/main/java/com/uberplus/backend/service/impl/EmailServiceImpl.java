package com.uberplus.backend.service.impl;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.uberplus.backend.model.Passenger;
import com.uberplus.backend.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailServiceImpl implements EmailService {

    private final SendGrid sg;
    private final String fromEmail;
    @Value("${app.frontend.activation-url:http://localhost:4200/activate?token=}")
    private String activationUrl;

    public EmailServiceImpl(@Value("${sendgrid.api.key}") String apiKey, @Value("${sendgrid.from.email}") String fromEmail) {
        this.sg = new SendGrid(apiKey);
        this.fromEmail = fromEmail;
    }
    @Override
    public void sendActivationEmail(Passenger user){
        String token = user.getActivationToken();
        String activationLink = activationUrl + token;
        Email from = new Email(fromEmail);
        Email to = new Email(user.getEmail());
        Mail mail = new Mail(from, "UberPlus - Activate your account", to, new Content());
        mail.setTemplateId("d-90f8f23a45a04421b0afd9c4eff195e6");
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            sg.api(request);
        } catch (IOException e) {
            throw new RuntimeException("Failed to send activation email", e);
        }
    }
}
