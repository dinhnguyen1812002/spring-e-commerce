package com.app.e_commerce.services;

import com.app.e_commerce.entity.Order;
import com.app.e_commerce.entity.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendOrderConfirmationMail(User user, Order order) {
        String subject = "Order Confirmation -" +order.getId();
        String confirmationUrl ="http://localhost:8080/orders/confirm/"+order.getId();
        String message = "<p>Dear"+ user.getUsername()
                        + "</p><p>Thank you for your order. " +
                        "Please confirm your order by clicking the link below:</p><a href='"
                        +confirmationUrl +"'>Confirm Order</a>";

        sendHtmlMessage(user.getEmail(), subject, message);
    }

    private void sendHtmlMessage(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
