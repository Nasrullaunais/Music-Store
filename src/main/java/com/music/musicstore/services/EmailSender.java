package com.music.musicstore.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class EmailSender {
    private final JavaMailSender mailSender;

    @Autowired
    public EmailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendReceiptEmail(String toEmail, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("nasrullaunais@gmail.com");
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }

    public void sendReceipt(BigDecimal totalAmount, StringBuilder itemList, String customerEmail, String orderId) {
        String subject = "Your Purchase Receipt - Order #" + orderId;
        String body = "Thank you for your purchase!\n\n" +
                      "Here are the details of your order:\n" +
                      itemList.toString() +
                      "\nTotal Amount: $" + totalAmount + "\n\n" +
                      "We appreciate your business and hope to see you again soon!\n\n" +
                      "Best regards,\n" +
                      "Music Store Team";

        sendReceiptEmail(customerEmail, subject, body);
    }

    public void sendUserStatusChangeEmail(String toEmail, String username, String newStatus) {
        String subject = "Account Status Update";
        String body = "Dear " + username + ",\n\n" +
                      "We would like to inform you that your account status has been changed to: " + newStatus + ".\n\n" +
                      "If you have any questions or need further assistance, please feel free to contact our support team.\n\n" +
                      "Best regards,\n" +
                      "Music Store Team";

        sendReceiptEmail(toEmail, subject, body);
    }
}
