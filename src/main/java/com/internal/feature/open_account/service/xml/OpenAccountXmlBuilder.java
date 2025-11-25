package com.internal.feature.open_account.service.xml;

import com.internal.config.CpbProperties;
import com.internal.feature.open_account.dto.request.CustomerRequest;
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

    // === Constants - Updated to match production example ===
    private static final String DEFAULT_BRANCH_CODE = "KH0012011";
    private static final String DEFAULT_SECTOR = "4501";
    private static final String DEFAULT_COST_CENTER = "1000";
    private static final String DEFAULT_INDUSTRY = "4500";
    private static final String DEFAULT_TARGET = "220";
    private static final String DEFAULT_LANGUAGE = "2";
    private static final String DEFAULT_CUSTOMER_RATING = "1";
    private static final String DEFAULT_CUSTOMER_STATUS = "1";
    private static final String DEFAULT_CUSTOMER_TYPE = "ACTIVE";
    private static final String DEFAULT_OWNERSHIP = "304";
    private static final String DEFAULT_LEGAL_HOLDER_NAME = "NATIONAL.ID";
    private static final String DEFAULT_NATIONALITY = "KH";

    // Namespace URIs
    private static final String SOAP_ENV_NS = "http://schemas.xmlsoap.org/soap/envelope/";
    private static final String OAOW_NS = "http://temenos.com/OAOWAR";
    private static final String CUSTOMER_NS = "http://temenos.com/CUSTOMERCPBCREATEOAO";
    private static final String ACCOUNT_NS = "http://temenos.com/AAARRANGEMENTACTIVITYAANEWOAO";

    // Product and activity constants
    private static final String NEW_ARRANGEMENT = "NEW";
    private static final String ACCOUNT_ACTIVITY = "ACCOUNTS-NEW-ARRANGEMENT";
    private static final String PRODUCT_CODE = "SAVE.ACCT.ONLINE";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter T24_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * Build XML for creating a customer in T24
     */
    public String buildCustomerCreationXml(CustomerRequest request) {
        log.debug("Building customer creation XML for Legal ID: {}", request.getLegalId());

        String username = cpbProperties.getT24().getUsername();
        String password = cpbProperties.getT24().getPassword();

        String branchCode = getOrDefault(request.getBranchCode(), DEFAULT_BRANCH_CODE);
        String maritalStatus = getOrDefault(request.getMaritalStatus(), "");
        String legalAddress = getOrDefault(request.getLegalAddress(), "");

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

        String referralId = getOrDefault("", "");
        String releasedBy = getOrDefault("", "");

        // Format dates to T24 format (YYYYMMDD)
        String dateOfBirth = formatDateForT24(request.getDateOfBirth());
        String legalIssueDate = formatDateForT24(request.getLegalIssueDate());

        // Determine title from gender
        String title = determineTitle(request.getGender());

        return "<soapenv:Envelope xmlns:soapenv=\"" + SOAP_ENV_NS + "\" "
                + "xmlns:oaow=\"" + OAOW_NS + "\" "
                + "xmlns:cus=\"" + CUSTOMER_NS + "\">"
                + "<soapenv:Header/>"
                + "<soapenv:Body>"
                + "<oaow:OAOCUSTOMERCREATION>"
                + "<WebRequestCommon>"
                + "<company>" + branchCode + "</company>"
                + "<password><![CDATA[" + password + "]]></password>"
                + "<userName>" + username + "</userName>"
                + "</WebRequestCommon>"
                + "<OfsFunction/>"
                + "<CUSTOMERCPBCREATEOAOType id=\"\">"

                // Name fields
                + "<cus:gSHORTNAME g=\"1\"><cus:ShortName>" + request.getGivenName() + "</cus:ShortName></cus:gSHORTNAME>"
                + "<cus:gNAME1 g=\"1\"><cus:FullName>" + request.getFamilyName() + " " + request.getGivenName() + "</cus:FullName></cus:gNAME1>"
                + "<cus:gNAME2 g=\"1\"><cus:FullName2>" + request.getLastNameKh() + " " + request.getFirstNameKh() + "</cus:FullName2></cus:gNAME2>"
                + "<cus:gSTREET g=\"1\"><cus:STREET>" + legalAddress + "</cus:STREET></cus:gSTREET>"

                // Organizational fields
                + "<cus:Sector>" + DEFAULT_SECTOR + "</cus:Sector>"
                + "<cus:CostCenter>" + DEFAULT_COST_CENTER + "</cus:CostCenter>"
                + "<cus:Industry>" + DEFAULT_INDUSTRY + "</cus:Industry>"
                + "<cus:Target>" + DEFAULT_TARGET + "</cus:Target>"
                + "<cus:Nationality>" + DEFAULT_NATIONALITY + "</cus:Nationality>"
                + "<cus:CustomerStatus>" + DEFAULT_CUSTOMER_STATUS + "</cus:CustomerStatus>"
                + "<cus:Residence>" + DEFAULT_NATIONALITY + "</cus:Residence>"

                // Legal identification
                + "<cus:gLEGALID g=\"1\"><cus:mLEGALID m=\"1\">"
                + "<cus:LegalId>" + request.getLegalId() + "</cus:LegalId>"
                + "<cus:LegalDocName>" + request.getLegalDocType() + "</cus:LegalDocName>"
                + "<cus:LegalHolderName>" + DEFAULT_LEGAL_HOLDER_NAME + "</cus:LegalHolderName>"
                + "<cus:LegalIssAuth>" + request.getGivenName() + "</cus:LegalIssAuth>"
                + "<cus:LegalIssDate>" + legalIssueDate + "</cus:LegalIssDate>"
                + "</cus:mLEGALID></cus:gLEGALID>"

                // Language
                + "<cus:Language>" + DEFAULT_LANGUAGE + "</cus:Language>"

                // Customer rating
                + "<cus:gCUSTOMERRATING g=\"1\"><cus:CustomerRating>" + DEFAULT_CUSTOMER_RATING + "</cus:CustomerRating></cus:gCUSTOMERRATING>"

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
                + "<cus:CustomerType>" + DEFAULT_CUSTOMER_TYPE + "</cus:CustomerType>"

                // Current address (administrative codes)
                + "<cus:CustProvince>" + custProvince + "</cus:CustProvince>"
                + "<cus:CustDistrict>" + custDistrict + "</cus:CustDistrict>"
                + "<cus:CustCommune>" + custCommune + "</cus:CustCommune>"
                + "<cus:CustVillage>" + custVillage + "</cus:CustVillage>"

                // Ownership and staff
                + "<cus:Ownership>" + DEFAULT_OWNERSHIP + "</cus:Ownership>"
                + "<cus:RelationManager>" + referralId + "</cus:RelationManager>"
                + "<cus:LoanOfficer/>"
                + "<cus:Staff>" + releasedBy + "</cus:Staff>"
                + "<cus:ReferralBy>" + referralId + "</cus:ReferralBy>"

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

    /**
     * Build XML for opening a new account (KHR/USD)
     */
    public String buildAccountCreationXml(CustomerRequest request, String cif, String currency) {
        String username = cpbProperties.getT24().getUsername();
        String password = cpbProperties.getT24().getPassword();
        String branchCode = getOrDefault(request.getBranchCode(), DEFAULT_BRANCH_CODE);
        String effectiveDate = LocalDate.now().format(DATE_FORMATTER);

        return "<soapenv:Envelope xmlns:soapenv=\"" + SOAP_ENV_NS + "\" "
                + "xmlns:oaow=\"" + OAOW_NS + "\" "
                + "xmlns:aaar=\"" + ACCOUNT_NS + "\">"
                + "<soapenv:Header/>"
                + "<soapenv:Body>"
                + "<oaow:ACCREATIONOAO>"
                + "<WebRequestCommon>"
                + "<company>" + branchCode + "</company>"
                + "<password><![CDATA[" + password + "]]></password>"
                + "<userName>" + username + "</userName>"
                + "</WebRequestCommon>"
                + "<OfsFunction/>"
                + "<AAARRANGEMENTACTIVITYAANEWOAOType id=\"\">"
                + "<aaar:Arrangement>" + NEW_ARRANGEMENT + "</aaar:Arrangement>"
                + "<aaar:Activity>" + ACCOUNT_ACTIVITY + "</aaar:Activity>"
                + "<aaar:EffectiveDate>" + effectiveDate + "</aaar:EffectiveDate>"
                + "<aaar:gCUSTOMER g=\"1\">"
                + "<aaar:mCUSTOMER m=\"1\">"
                + "<aaar:Customer>" + cif + "</aaar:Customer>"
                + "<aaar:CustomerRole>OWNER</aaar:CustomerRole>"
                + "</aaar:mCUSTOMER>"
                + "</aaar:gCUSTOMER>"
                + "<aaar:Product>" + PRODUCT_CODE + "</aaar:Product>"
                + "<aaar:Currency>" + currency + "</aaar:Currency>"
                + "<aaar:gPROPERTY g=\"1\">"
                + "<aaar:mPROPERTY m=\"1\">"
                + "<aaar:Property>BALANCE</aaar:Property>"
                + "<aaar:sgFIELDNAME sg=\"1\">"
                + "<aaar:FieldName s=\"1\">"
                + "<aaar:FieldName>SHORT.TITLE</aaar:FieldName>"
                + "<aaar:FieldValue>" + request.getGivenName() + "</aaar:FieldValue>"
                + "</aaar:FieldName>"
                + "</aaar:sgFIELDNAME>"
                + "</aaar:mPROPERTY>"
                + "</aaar:gPROPERTY>"
                + "</AAARRANGEMENTACTIVITYAANEWOAOType>"
                + "</oaow:ACCREATIONOAO>"
                + "</soapenv:Body>"
                + "</soapenv:Envelope>";
    }

    // === Utility Methods ===

    /**
     * Format date to T24 format (YYYYMMDD)
     */
    private String formatDateForT24(String date) {
        if (date == null || date.isEmpty()) {
            return "";
        }
        try {
            // If already in YYYYMMDD format
            if (date.matches("\\d{8}")) {
                return date;
            }
            // Try parsing from YYYY-MM-DD format
            LocalDate localDate = LocalDate.parse(date, DATE_FORMATTER);
            return localDate.format(T24_DATE_FORMATTER);
        } catch (Exception e) {
            log.warn("Could not format date {}, using as-is: {}", date, e.getMessage());
            return date;
        }
    }

    /**
     * Determine title based on gender
     */
    private String determineTitle(String gender) {
        if (gender == null || gender.isEmpty()) {
            return "";
        }
        String genderUpper = gender.toUpperCase();
        if (genderUpper.contains("MALE") && !genderUpper.contains("FEMALE")) {
            return "MR";
        } else if (genderUpper.contains("FEMALE")) {
            return "MS";
        }
        return "";
    }

    private String getOrDefault(String value, String defaultValue) {
        return value != null && !value.isEmpty() ? value : defaultValue;
    }
}