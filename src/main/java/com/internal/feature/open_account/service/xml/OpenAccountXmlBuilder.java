package com.internal.feature.open_account.service.xml;

import com.internal.config.CpbProperties;
import com.internal.config.DefaultProperties;
import com.internal.feature.open_account.dto.request.CustomerRequest;
import com.internal.utils.constants.DefaultConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
@Slf4j
public class OpenAccountXmlBuilder {

    private final CpbProperties cpbProperties;
    private final DefaultProperties defaultProperties;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter T24_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public String buildCustomerCreationXml(CustomerRequest request) {
        log.debug("Building customer creation XML for Legal ID: {}", request.getLegalId());

        String username = cpbProperties.getT24().getUsername();
        String password = cpbProperties.getT24().getPassword();

        String branchCode = getOrDefault(request.getBranchCode(), defaultProperties.getBranchCode());
        String maritalStatus = mapMaritalStatus(request.getMaritalStatus());

        // Sanitize legalAddress to SWIFT-safe characters before sending to T24
        String legalAddress = toSwiftSafe(getOrDefault(request.getLegalAddress(), ""));

        // Current address codes
        String custProvince = getOrDefault(request.getCustomerCurrentProvince(), "");
        String custDistrict = getOrDefault(request.getCustomerCurrentDistrict(), "");
        String custCommune = getOrDefault(request.getCustomerCurrentCommune(), "");
        String custVillage = getOrDefault(request.getCustomerCurrentVillage(), "");

        // Place of birth codes (primary P fields)
        String pobProvince = getOrDefault(request.getCustomerPobProvince(), "");
        String pobDistrict = getOrDefault(request.getCustomerPobDistrict(), "");
        String pobCommune = getOrDefault(request.getCustomerPobCommune(), "");
        String pobVillage = getOrDefault(request.getCustomerPobVillage(), "");

        String referralId = getOrDefault(request.getReferralId(), "");
        String releasedBy = getOrDefault(request.getReleasedBy(), "");

        // Format dates to T24 format (YYYYMMDD)
        String dateOfBirth = formatDateForT24(request.getDateOfBirth());
        String legalIssueDate = formatDateForT24(request.getLegalIssueDate());
        String legalExpDate = formatDateForT24(request.getLegalExpireDate());

        // Determine title from gender (use request title if provided)
        String title = getOrDefault(request.getTitle(), determineTitle(request.getGender()));

        // English and Khmer full names
        String englishFullName = safe(request.getFamilyName()) + " " + safe(request.getGivenName());
        String khmerFullName = safe(request.getLastNameKh()) + " " + safe(request.getFirstNameKh());

        // UTF-8 XML declaration ensures Khmer characters are correctly interpreted by T24
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<soapenv:Envelope xmlns:soapenv=\"" + DefaultConstants.SOAP_ENV_NS + "\" "
                + "xmlns:oaow=\"" + DefaultConstants.OAOW_NS + "\" "
                + "xmlns:cus=\"" + DefaultConstants.CUSTOMER_NS + "\">"
                + "<soapenv:Header/>"
                + "<soapenv:Body>"
                + "<oaow:OAOCUSTOMERCREATION>"
                + "<WebRequestCommon>"
                + "<company>" + branchCode + "</company>"
                + "<password>" + xmlEscape(password) + "</password>"
                + "<userName>" + username + "</userName>"
                + "</WebRequestCommon>"
                + "<OfsFunction/>"
                + "<CUSTOMERCPBCREATEOAOType id=\"\">"

                // gSHORTNAME — two entries: English + Khmer
                + "<cus:gSHORTNAME g=\"1\">"
                + "<cus:ShortName>" + englishFullName + "</cus:ShortName>"
                + "<cus:ShortName>" + khmerFullName + "</cus:ShortName>"
                + "</cus:gSHORTNAME>"

                // gNAME1 — two entries: English + Khmer
                + "<cus:gNAME1 g=\"1\">"
                + "<cus:FullName>" + englishFullName + "</cus:FullName>"
                + "<cus:FullName>" + khmerFullName + "</cus:FullName>"
                + "</cus:gNAME1>"

                // STREET — sanitized to SWIFT-safe characters
                + "<cus:gSTREET g=\"1\"><cus:STREET>" + legalAddress + "</cus:STREET></cus:gSTREET>"

                // Organizational fields
                + "<cus:Sector>" + defaultProperties.getSector() + "</cus:Sector>"
                + "<cus:CostCenter>" + defaultProperties.getCostCenter() + "</cus:CostCenter>"
                + "<cus:Industry>" + defaultProperties.getIndustry() + "</cus:Industry>"
                + "<cus:Target>" + defaultProperties.getTarget() + "</cus:Target>"
                + "<cus:Nationality>" + defaultProperties.getNationality() + "</cus:Nationality>"
                + "<cus:CustomerStatus>" + defaultProperties.getCustomerStatus() + "</cus:CustomerStatus>"
                + "<cus:Residence>" + defaultProperties.getNationality() + "</cus:Residence>"

                // Legal identification
                + "<cus:gLEGALID g=\"1\"><cus:mLEGALID m=\"1\">"
                + "<cus:LegalId>" + request.getLegalId() + "</cus:LegalId>"
                + "<cus:LegalDocName>" + request.getLegalDocType() + "</cus:LegalDocName>"
                + "<cus:LegalHolderName>" + defaultProperties.getLegalHolderName() + "</cus:LegalHolderName>"
                + "<cus:LegalIssAuth>" + getOrDefault(request.getLegalIssAuth(), request.getGivenName())
                + "</cus:LegalIssAuth>"
                + "<cus:LegalIssDate>" + legalIssueDate + "</cus:LegalIssDate>"
                + "<cus:LegalExpDate>" + legalExpDate + "</cus:LegalExpDate>"
                + "</cus:mLEGALID></cus:gLEGALID>"

                // Language
                + "<cus:Language>" + defaultProperties.getLanguage() + "</cus:Language>"

                // Customer rating
                + "<cus:gCUSTOMERRATING g=\"1\"><cus:CustomerRating>" + defaultProperties.getCustomerRating()
                + "</cus:CustomerRating></cus:gCUSTOMERRATING>"

                // Personal details
                + "<cus:TITLE>" + title + "</cus:TITLE>"
                + "<cus:GIVENNAMES>" + request.getGivenName() + "</cus:GIVENNAMES>"
                + "<cus:FAMILYNAME>" + request.getFamilyName() + "</cus:FAMILYNAME>"
                + "<cus:Gender>" + request.getGender() + "</cus:Gender>"
                + "<cus:DateofBirth>" + dateOfBirth + "</cus:DateofBirth>"
                + "<cus:MaritalStatus>" + maritalStatus + "</cus:MaritalStatus>"

                // Phone details
                + "<cus:gPHONE1 g=\"1\"><cus:mPHONE1 m=\"1\">"
                + "<cus:PHONE1>" + request.getPhoneNumber() + "</cus:PHONE1>"
                + "<cus:SMS1>" + request.getPhoneNumber() + "</cus:SMS1>"
                + "<cus:EMAIL1/>"
                + "</cus:mPHONE1></cus:gPHONE1>"

                // Customer type
                + "<cus:CustomerType>" + defaultProperties.getCustomerType() + "</cus:CustomerType>"

                // Current address (administrative codes)
                + "<cus:CustProvince>" + custProvince + "</cus:CustProvince>"
                + "<cus:CustDistrict>" + custDistrict + "</cus:CustDistrict>"
                + "<cus:CustCommune>" + custCommune + "</cus:CustCommune>"
                + "<cus:CustVillage>" + custVillage + "</cus:CustVillage>"

                // Ownership and staff
                + "<cus:Ownership>" + defaultProperties.getOwnership() + "</cus:Ownership>"
                + "<cus:RelationManager></cus:RelationManager>"
                + "<cus:LoanOfficer></cus:LoanOfficer>"
                + "<cus:Staff></cus:Staff>"
                + "<cus:ReferralBy>" + releasedBy + "</cus:ReferralBy>"

                // Place of birth address (Primary P fields)
                + "<cus:CUSTPROVINCEP>" + pobProvince + "</cus:CUSTPROVINCEP>"
                + "<cus:CUSTDISTRICTP>" + pobDistrict + "</cus:CUSTDISTRICTP>"
                + "<cus:CUSTCOMMUNEP>" + pobCommune + "</cus:CUSTCOMMUNEP>"
                + "<cus:CUSTVILLAGEP>" + pobVillage + "</cus:CUSTVILLAGEP>"

                + "</CUSTOMERCPBCREATEOAOType>"
                + "</oaow:OAOCUSTOMERCREATION>"
                + "</soapenv:Body>"
                + "</soapenv:Envelope>";
    }

    public String buildAccountCreationXml(CustomerRequest request, String cif, String currency) {

        String username = cpbProperties.getT24().getUsername();
        String password = cpbProperties.getT24().getPassword();
        String branchCode = getOrDefault(request.getBranchCode(), defaultProperties.getBranchCode());

        String englishFullName = safe(request.getFamilyName()) + " " + safe(request.getGivenName());
        String khmerFullName = safe(safe(request.getLastNameKh() + " " + request.getFirstNameKh()));

        // UTF-8 XML declaration ensures Khmer characters are correctly interpreted by T24
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<soapenv:Envelope xmlns:soapenv=\"" + DefaultConstants.SOAP_ENV_NS + "\" "
                + "xmlns:oaow=\"" + DefaultConstants.OAOW_NS + "\" "
                + "xmlns:aaar=\"" + DefaultConstants.ACCOUNT_NS + "\">"

                + "<soapenv:Header/>"
                + "<soapenv:Body>"
                + "<oaow:ACCREATIONOAO>"

                + "<WebRequestCommon>"
                + "<company>" + branchCode + "</company>"
                + "<password>" + xmlEscape(password) + "</password>"
                + "<userName>" + username + "</userName>"
                + "</WebRequestCommon>"

                + "<OfsFunction/>"

                + "<AAARRANGEMENTACTIVITYAANEWOAOType id=\"\">"
                + "<aaar:Arrangement>" + defaultProperties.getNewArrangement() + "</aaar:Arrangement>"
                + "<aaar:Activity>" + defaultProperties.getAccountActivity() + "</aaar:Activity>"

                // CUSTOMER
                + "<aaar:gCUSTOMER g=\"1\">"
                + "<aaar:mCUSTOMER m=\"1\">"
                + "<aaar:Customer>" + cif + "</aaar:Customer>"
                + "<aaar:CustomerRole>OWNER</aaar:CustomerRole>"
                + "</aaar:mCUSTOMER>"
                + "</aaar:gCUSTOMER>"

                // PRODUCT + CURRENCY
                + "<aaar:Product>" + defaultProperties.getProductCode() + "</aaar:Product>"
                + "<aaar:Currency>" + currency + "</aaar:Currency>"

                // PROPERTY BLOCK
                + "<aaar:gPROPERTY g=\"1\">"
                + "<aaar:mPROPERTY m=\"1\">"
                + "<aaar:Property>BALANCE</aaar:Property>"

                + "<aaar:sgFIELDNAME sg=\"1\">"

                // SHORT.TITLE:1 — English
                + "<aaar:FieldName s=\"1\">"
                + "<aaar:FieldName>SHORT.TITLE:1</aaar:FieldName>"
                + "<aaar:FieldValue>" + englishFullName + "</aaar:FieldValue>"
                + "</aaar:FieldName>"

                // SHORT.TITLE:2 — Khmer
                + "<aaar:FieldName s=\"1\">"
                + "<aaar:FieldName>SHORT.TITLE:2</aaar:FieldName>"
                + "<aaar:FieldValue>" + khmerFullName + "</aaar:FieldValue>"
                + "</aaar:FieldName>"

                // ACCOUNT.TITLE.1:1 — English
                + "<aaar:FieldName s=\"1\">"
                + "<aaar:FieldName>ACCOUNT.TITLE.1:1</aaar:FieldName>"
                + "<aaar:FieldValue>" + englishFullName + "</aaar:FieldValue>"
                + "</aaar:FieldName>"

                + "</aaar:sgFIELDNAME>"

                + "</aaar:mPROPERTY>"
                + "</aaar:gPROPERTY>"

                + "</AAARRANGEMENTACTIVITYAANEWOAOType>"
                + "</oaow:ACCREATIONOAO>"
                + "</soapenv:Body>"
                + "</soapenv:Envelope>";
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String formatDateForT24(String date) {
        if (date == null || date.isEmpty()) {
            return "";
        }
        try {
            if (date.matches("\\d{8}")) {
                return date;
            }
            LocalDate localDate = LocalDate.parse(date, DATE_FORMATTER);
            return localDate.format(T24_DATE_FORMATTER);
        } catch (Exception e) {
            log.warn("Could not format date {}, using as-is: {}", date, e.getMessage());
            return date;
        }
    }

    private String determineTitle(String gender) {
        if (gender == null || gender.isEmpty()) {
            return "";
        }
        String genderUpper = gender.toUpperCase();
        if (genderUpper.contains(DefaultConstants.MALE) && !genderUpper.contains(DefaultConstants.FEMALE)) {
            return DefaultConstants.MR;
        } else if (genderUpper.contains(DefaultConstants.FEMALE)) {
            return DefaultConstants.MS;
        }
        return "";
    }

    private String mapMaritalStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return "SINGLE";
        }
        return status.toUpperCase();
    }

    private String getOrDefault(String value, String defaultValue) {
        return value != null && !value.isEmpty() ? value : defaultValue;
    }

    /**
     * Sanitizes a string to contain SWIFT-allowed characters while preserving Unicode (Khmer).
     * SWIFT charset: A-Z a-z 0-9 / - ? : ( ) . , ' + space
     * Unicode characters (including Khmer) are preserved as they are valid in XML UTF-8.
     * Only control characters and certain symbols are removed.
     * Used for fields like STREET that T24 validates against SWIFT rules.
     */
    private String toSwiftSafe(String input) {
        if (input == null) return "";
        // T24 STREET field: ONLY accepts A-Z, 0-9, space, and dash
        // All other characters (including Khmer, special symbols) are removed
        String sanitized = input.replaceAll("[^A-Za-z0-9 \\-]", " ").trim();
        // Collapse multiple spaces into one
        sanitized = sanitized.replaceAll(" {2,}", " ");
        if (!sanitized.equals(input.trim())) {
            log.warn("SWIFT sanitization applied to address. Original: [{}] → Sanitized: [{}]", input, sanitized);
        }
        return sanitized;
    }

    /**
     * Escapes special XML characters — use for all values injected into XML,
     * especially passwords containing @, #, &, <, >
     */
    private String xmlEscape(String value) {
        if (value == null) return "";
        return value
                .replace("&", "&amp;")   // must be first
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}