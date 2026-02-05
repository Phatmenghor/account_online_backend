package com.internal.feature.aml.service;

import com.internal.enumation.AmlStatusEnum;
import com.internal.feature.aml.dto.response.AmlStatusDto;
import com.internal.feature.logs_report.service.CustomerImageService;
import com.internal.feature.mail.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AmlNotificationService {

    private final MailService mailService;
    private final SpringTemplateEngine templateEngine;
    private final CustomerImageService customerImageService;

    @Value("${aml.dashboard.url}")
    private String amlDashboardUrl;

    @Value("${email.sender.address:ithelpdesk@cambodiapostbank.com.kh}")
    private String senderEmail;

    @Value("${email.aml.recipients}")
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
            // Set multiple primary recipients
            String[] recipients = new String[]{
                    "menghor.phat@cambodiapostbank.com.kh",
                    "makkara.nob@cambodiapostbank.com.kh"
            };

            String subject = buildEmailSubject(amlStatus);

            // Build HTML body using Thymeleaf template
            Context context = new Context();
            context.setVariable("amlStatus", amlStatus);
            context.setVariable("timestamp", LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")));
            context.setVariable(
                    "amlDashboardUrl",
                    amlDashboardUrl + "&legalId=" + amlStatus.getCustomerInfo().getLegalId()
            );
            // Determine badge color based on AML status
            String statusColor;
            if (AmlStatusEnum.APPROVE.equals(amlStatus.getStatus())) {
                statusColor = "#22c55e"; // green
            } else if (AmlStatusEnum.REJECT.equals(amlStatus.getStatus())) {
                statusColor = "#ef4444"; // red
            } else {
                statusColor = "#f59e0b"; // pending / amber
            }

            context.setVariable("statusColor", statusColor);


            String htmlContent = templateEngine.process("aml-template.html", context);

            // Attach NID & Selfie images as files (better for Outlook)
            Map<String, Resource> attachments = new HashMap<>();
            try {
                String legalId = amlStatus.getCustomerInfo().getLegalId();
                if (customerImageService.nidImageExists(legalId)) {
                    Resource nidRes = customerImageService.getNidImageResourceForEmail(legalId);
                    if (nidRes != null) attachments.put("NID_" + legalId + ".jpg", nidRes);
                }
                if (customerImageService.selfieImageExists(legalId)) {
                    Resource selfieRes = customerImageService.getSelfieImageResourceForEmail(legalId);
                    if (selfieRes != null) attachments.put("SELFIE_" + legalId + ".jpg", selfieRes);
                }
            } catch (Exception e) {
                log.warn("Failed to attach customer images", e);
            }

            mailService.sendEmail(recipients, subject, htmlContent, true, attachments);
            log.info("AML status email sent successfully for customer: {}", amlStatus.getCustomerInfo().getLegalId());

        } catch (Exception e) {
            log.error("Failed to send AML status email for customer: {}", amlStatus.getCustomerInfo().getLegalId(), e);
        }
    }

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
