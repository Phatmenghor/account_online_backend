package com.internal.feature.mail.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${email.sender.address:ithelpdesk@cambodiapostbank.com.kh}")
    private String senderEmail;

    /**
     * Send a generic email
     *
     * @param to          Recipients
     * @param subject     Email subject
     * @param content     Email content (text or HTML)
     * @param isHtml      True if content is HTML
     * @param attachments Map of filename -> Resource for attachments (optional)
     */
    public void sendEmail(String[] to, String subject, String content, boolean isHtml, Map<String, Resource> attachments) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, isHtml);

            if (attachments != null) {
                for (Map.Entry<String, Resource> entry : attachments.entrySet()) {
                    helper.addAttachment(entry.getKey(), entry.getValue());
                }
            }

            mailSender.send(message);
            log.info("Email sent successfully to: {}", (Object) to);

        } catch (MessagingException e) {
            log.error("Failed to send email to: {}", (Object) to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
