package com.internal.feature.mail.service;

import com.internal.feature.aml.dto.response.AmlStatusDto;
import com.internal.feature.logs_report.service.CustomerImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final CustomerImageService customerImageService;

    @Value("${email.sender.address:ithelpdesk@cambodiapostbank.com.kh}")
    private String senderEmail;

    @Value("${email.aml.recipients:aml@yourbank.com.kh,compliance@yourbank.com.kh}")
    private String amlRecipients;

    /**
     * Send AML status notification email
     */
    public void sendAmlStatusNotification(AmlStatusDto amlStatus) {
        if (amlStatus == null) {
            log.warn("AML Status is null, email not sent.");
            return;
        }

        log.info("Preparing AML status notification email for customer: {}", amlStatus.getCustomerInfo().getLegalId());

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Set multiple primary recipients
            String[] recipients = new String[]{
                    "menghor.phat@cambodiapostbank.com.kh",
                    "makkara.nob@cambodiapostbank.com.kh",
                    "complaint@cambodiapostbank.com.kh",
                    "Nara.Im@cambodiapostbank.com.kh"
            };

            helper.setFrom(senderEmail);
            helper.setTo(recipients);
            helper.setSubject(buildEmailSubject(amlStatus));

            // Build HTML body using Thymeleaf template
            Context context = new Context();
            context.setVariable("amlStatus", amlStatus);
            context.setVariable("timestamp", LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")));

            String htmlContent = templateEngine.process("aml-template.html", context);
            helper.setText(htmlContent, true);

            // Attach NID & Selfie images as files (better for Outlook)
            try {
                String legalId = amlStatus.getCustomerInfo().getLegalId();
                if (customerImageService.nidImageExists(legalId)) {
                    var nidRes = customerImageService.getNidImageResourceForEmail(legalId);
                    if (nidRes != null) helper.addAttachment("NID_" + legalId + ".jpg", nidRes);
                }
                if (customerImageService.selfieImageExists(legalId)) {
                    var selfieRes = customerImageService.getSelfieImageResourceForEmail(legalId);
                    if (selfieRes != null) helper.addAttachment("SELFIE_" + legalId + ".jpg", selfieRes);
                }
            } catch (Exception e) {
                log.warn("Failed to attach customer images", e);
            }

            mailSender.send(message);
            log.info("AML status email sent successfully for customer: {}", amlStatus.getCustomerInfo().getLegalId());

        } catch (MessagingException e) {
            log.error("Failed to send AML status email for customer: {}", amlStatus.getCustomerInfo().getLegalId(), e);
        }
    }

    /**
     * Send AML approval notification
     */
//    public void sendAmlApprovalNotification(AmlStatusDto amlStatus) {
//        log.info("Sending AML approval notification for: {}",
//                amlStatus.getCustomerInfo().getIdDisplay());
//        sendAmlStatusNotification(amlStatus);
//    }

    /**
     * Send AML rejection notification
     */
//    public void sendAmlRejectionNotification(AmlStatusDto amlStatus) {
//        log.info("Sending AML rejection notification for: {}",
//                amlStatus.getCustomerInfo().getIdDisplay());
//        sendAmlStatusNotification(amlStatus);
//    }

    /**
     * Send AML pending review notification
     */
//    public void sendAmlPendingNotification(AmlStatusDto amlStatus) {
//        log.info("Sending AML pending review notification for: {}",
//                amlStatus.getCustomerInfo().getIdDisplay());
//        sendAmlStatusNotification(amlStatus);
//    }

    private String buildEmailSubject(AmlStatusDto amlStatus) {
        if (amlStatus == null) return "[AML UNKNOWN] Customer: N/A";

        String status = amlStatus.getStatus() != null ? amlStatus.getStatus().name() : "UNKNOWN";
        String legalId = amlStatus.getCustomerInfo().getLegalId() != null ? amlStatus.getCustomerInfo().getLegalId() : "N/A";
        String fullName = joinNonNull(amlStatus.getCustomerInfo().getFamilyName(), amlStatus.getCustomerInfo().getGivenName());

        return String.format("[AML %s] Customer: %s - %s", status, legalId, fullName);
    }

    /**
     * Helper to join non-null strings with a space
     */
    private String joinNonNull(String... parts) {
        return Arrays.stream(parts)
                .filter(p -> p != null && !p.isEmpty())
                .collect(Collectors.joining(" "));
    }

}