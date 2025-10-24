
package com.internal.feature.open_account.service.external;

import com.internal.config.CpbProperties;
import com.internal.exceptions.error.openaccount.T24ServiceException;
import com.internal.feature.open_account.dto.request.CustomerRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class T24Service {

    private final CpbProperties properties;
    private final RestTemplate restTemplate;

    public Document createCustomer(CustomerRequest request) {
        log.info("Creating customer in T24 for Legal ID: {}", request.getLegalId());
        
        String xmlRequest = buildCustomerCreationXml(request);
        return executeT24Request(xmlRequest, "OAOCUSTOMERCREATION");
    }

    public Document createAccount(CustomerRequest request, String cif, String currency) {
        log.info("Creating {} account in T24 for CIF: {}", currency, cif);
        
        String xmlRequest = buildAccountCreationXml(request, cif, currency);
        return executeT24Request(xmlRequest, "ACCREATIONOAO");
    }

    private String buildCustomerCreationXml(CustomerRequest request) {
        try {
            String templatePath = properties.getXml().getCreateCustomer();
            File file = new File(templatePath);
            String template = new String(Files.readAllBytes(file.toPath()));
            
            String branchCode = request.getBranchCode() != null ? request.getBranchCode() : "KH0012011";
            String fullNameEn = request.getFamilyName() + " " + request.getGivenName();
            String fullNameKh = request.getLastNameKh() + " " + request.getFirstNameKh();

            return template
                .replace("{{BRANCH_CODE}}", branchCode)
                .replace("{{T24_PASSWORD}}", properties.getT24().getPassword())
                .replace("{{T24_USERNAME}}", properties.getT24().getUsername())
                .replace("{{CUS_SHORT_NAME}}", request.getGivenName() != null ? request.getGivenName() : "")
                .replace("{{CUS_FULL_NAME_EN}}", fullNameEn)
                .replace("{{CUS_FULL_NAME_KH}}", fullNameKh)
                .replace("{{CUS_FAMILY_NAME}}", request.getFamilyName())
                .replace("{{CUS_GIVEN_NAME}}", request.getGivenName() != null ? request.getGivenName() : "")
                .replace("{{CUS_SECTOR}}", "6010")
                .replace("{{CUS_INDUSTRY}}", "1000")
                .replace("{{CUS_TARGET}}", "1")
                .replace("{{CUS_NATIONALITY}}", request.getNationality())
                .replace("{{CUS_RESIDENCE}}", request.getNationality())
                .replace("{{CUS_LEGAL_ID}}", request.getLegalId())
                .replace("{{CUS_LEGAL_DOC_NAME}}", request.getLegalDocName())
                .replace("{{CUS_LANGUAGE}}", "1")
                .replace("{{CUS_GENDER}}", request.getGender())
                .replace("{{CUS_DATE_OF_BIRTH}}", request.getDateOfBirth())
                .replace("{{CUS_MARITAL_STATUS}}", request.getMaritalStatus() != null ? request.getMaritalStatus() : "")
                .replace("{{CUS_PHONE}}", request.getPhoneNumber())
                .replace("{{CUS_SMS}}", request.getPhoneNumber())
                .replace("{{CUS_PROVINCE}}", request.getCustomerProvince() != null ? request.getCustomerProvince() : "")
                .replace("{{CUS_DISTRICT}}", request.getCustomerDistrict() != null ? request.getCustomerDistrict() : "")
                .replace("{{CUS_COMMUNE}}", request.getCustomerCommune() != null ? request.getCustomerCommune() : "")
                .replace("{{CUS_VILLAGE}}", request.getCustomerVillage() != null ? request.getCustomerVillage() : "")
                .replace("{{CUS_RELEASED_BY}}", request.getReleasedBy() != null ? request.getReleasedBy() : "")
                .replace("{{CUS_OCCUPATION}}", request.getOccupation() != null ? request.getOccupation() : "")
                .replace("{{CUS_PLACE_OF_BIRTH}}", request.getPlaceOfBirth() != null ? request.getPlaceOfBirth() : "")
                .replace("{{CUS_ADDRESS}}", request.getLegalAddress() != null ? request.getLegalAddress() : "");
                
        } catch (Exception e) {
            log.error("Failed to build customer creation XML: {}", e.getMessage());
            throw new T24ServiceException("Failed to build customer XML", e);
        }
    }

    private String buildAccountCreationXml(CustomerRequest request, String cif, String currency) {
        try {
            String templatePath = properties.getXml().getOpenAcctByCustomer();
            File file = new File(templatePath);
            String template = new String(Files.readAllBytes(file.toPath()));
            
            String branchCode = request.getBranchCode() != null ? request.getBranchCode() : "KH0012011";
            String effectiveDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String fullName = request.getFamilyName() + " " + request.getGivenName();
            
            return template
                .replace("{{BRANCH_CODE}}", branchCode)
                .replace("{{T24_PASSWORD}}", properties.getT24().getPassword())
                .replace("{{T24_USERNAME}}", properties.getT24().getUsername())
                .replace("{{AA_EFFECTIVE_DATE}}", effectiveDate)
                .replace("{{AA_CUSTOMER}}", cif)
                .replace("{{AA_CURRENCY}}", currency)
                .replace("{{AA_CATEGORY}}", "6011")
                .replace("{{AA_NAME}}", fullName)
                .replace("{{AA_ACCOUNT_OFFICER}}", "1");
                
        } catch (Exception e) {
            log.error("Failed to build account creation XML: {}", e.getMessage());
            throw new T24ServiceException("Failed to build account XML", e);
        }
    }

    private Document executeT24Request(String xmlRequest, String operation) {
        int retryCount = 3;
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= retryCount; attempt++) {
            try {
                String url = properties.getT24().getUrl() + "/TWS.CPBOAO/services";
                
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.TEXT_XML);
                headers.set("SOAPAction", operation);
                
                HttpEntity<String> entity = new HttpEntity<>(xmlRequest, headers);
                
                ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
                );
                
                Document doc = parseXmlResponse(response.getBody());
                
                if (hasJmsError(doc)) {
                    log.warn("JMS error detected, attempt {}/{}", attempt, retryCount);
                    if (attempt < retryCount) {
                        Thread.sleep(1000);
                        continue;
                    }
                    throw new T24ServiceException("T24 JMS error after " + retryCount + " attempts");
                }
                
                return doc;
                
            } catch (Exception e) {
                lastException = e;
                log.error("T24 error on attempt {}/{}: {}", attempt, retryCount, e.getMessage());
                if (attempt < retryCount) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        
        throw new T24ServiceException("T24 call failed after " + retryCount + " attempts", lastException);
    }

    private Document parseXmlResponse(String xmlString) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new ByteArrayInputStream(xmlString.getBytes()));
    }

    private boolean hasJmsError(Document doc) {
        try {
            String docString = doc.toString();
            return docString.contains("JMS Message redelivered") && docString.contains("T24Error");
        } catch (Exception e) {
            return false;
        }
    }
}