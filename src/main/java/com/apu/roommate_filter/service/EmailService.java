package com.apu.roommate_filter.service;

import com.apu.roommate_filter.dto.users.HelpSupportRequest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Value("${app.support.recipient}")
    private String supportEmailAddress;

    public void sendEmail(String to, String subject, String verificationCode) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);

            InputStream templateStream = getClass().getClassLoader()
                    .getResourceAsStream("templates/email-template.html");
            if (templateStream == null) throw new IOException("Email template not found");
            String htmlTemplate = new String(templateStream.readAllBytes(), StandardCharsets.UTF_8);

            String htmlContent = htmlTemplate.replace("{{verification_code}}", verificationCode);

            helper.setText(htmlContent, true);

            javaMailSender.send(mimeMessage);

        } catch (MessagingException | IOException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    public void sendSupportEmail(HelpSupportRequest request) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");

            // Gmail only accepts a From matching the authenticated account; the submitter's
            // address goes on Reply-To so replying reaches them directly.
            helper.setFrom(fromAddress.trim());
            helper.setTo(supportEmailAddress.trim());
            helper.setReplyTo(request.getEmail());
            helper.setSubject("[Help & Support] " + request.getTitle());
            helper.setText(buildSupportMessage(request), false);

            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send help and support email", e);
        }
    }

    private String buildSupportMessage(HelpSupportRequest request) {
        return "Help and Support Request\n\n"
                + "Username: " + request.getUsername() + "\n"
                + "Phone Number: " + request.getPhoneNumber() + "\n"
                + "Email: " + request.getEmail() + "\n"
                + "Title: " + request.getTitle() + "\n\n"
                + "Body:\n" + request.getBody();
    }

}
