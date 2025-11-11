package com.internal.feature.open_account.service.xml;

import com.internal.config.CpbProperties;
import com.internal.feature.open_account.dto.request.CustomerRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class OpenAccountXmlBuilder {

    private final CpbProperties cpbProperties;

    // === Constants ===
    private static final String DEFAULT_BRANCH_CODE = "KH0012011";
    private static final String DEFAULT_SECTOR = "6010";
    private static final String DEFAULT_COST_CENTER = "1000";
    private static final String DEFAULT_INDUSTRY = "1000";
    private static final String DEFAULT_TARGET = "1";
    private static final String DEFAULT_LANGUAGE = "1";
    private static final String DEFAULT_CUSTOMER_RATING = "1";
    private static final String DEFAULT_CUSTOMER_STATUS = "1";
    private static final String DEFAULT_CUSTOMER_TYPE = "INDIVIDUAL";

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

    /**
     * Build XML for creating a customer in T24
     */
    public String buildCustomerCreationXml(CustomerRequest request) {
        String username = cpbProperties.getT24().getUsername();
        String password = cpbProperties.getT24().getPassword();

        String branchCode = getOrDefault(request.getBranchCode(), DEFAULT_BRANCH_CODE);
        String maritalStatus = getOrDefault(request.getMaritalStatus(), "");
        String legalAddress = getOrDefault(request.getLegalAddress(), "");
        String custProvince = getOrDefault(request.getCustomerProvince(), "");
        String custDistrict = getOrDefault(request.getCustomerDistrict(), "");
        String custCommune = getOrDefault(request.getCustomerCommune(), "");
        String custVillage = getOrDefault(request.getCustomerVillage(), "");
        String referralId = getOrDefault(request.getReferralId(), "");
        String releasedBy = getOrDefault(request.getReleasedBy(), "");
        String placeOfBirth = getOrDefault(request.getPlaceOfBirth(), "");

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
                + "<cus:gSHORTNAME g=\"1\"><cus:ShortName>" + request.getGivenName() + "</cus:ShortName></cus:gSHORTNAME>"
                + "<cus:gNAME1 g=\"1\"><cus:FullName>" + request.getFamilyName() + " " + request.getGivenName() + "</cus:FullName></cus:gNAME1>"
                + "<cus:gNAME2 g=\"1\"><cus:FullName2>" + request.getLastNameKh() + " " + request.getFirstNameKh() + "</cus:FullName2></cus:gNAME2>"
                + "<cus:gSTREET g=\"1\"><cus:STREET>" + legalAddress + "</cus:STREET></cus:gSTREET>"
                + "<cus:Sector>" + DEFAULT_SECTOR + "</cus:Sector>"
                + "<cus:CostCenter>" + DEFAULT_COST_CENTER + "</cus:CostCenter>"
                + "<cus:Industry>" + DEFAULT_INDUSTRY + "</cus:Industry>"
                + "<cus:Target>" + DEFAULT_TARGET + "</cus:Target>"
                + "<cus:Nationality>" + request.getNationality() + "</cus:Nationality>"
                + "<cus:CustomerStatus>" + DEFAULT_CUSTOMER_STATUS + "</cus:CustomerStatus>"
                + "<cus:Residence>" + request.getNationality() + "</cus:Residence>"
                + "<cus:gLEGALID g=\"1\"><cus:mLEGALID m=\"1\">"
                + "<cus:LegalId>" + request.getLegalId() + "</cus:LegalId>"
                + "<cus:LegalDocName>" + request.getLegalDocName() + "</cus:LegalDocName>"
                + "<cus:LegalHolderName/>"
                + "<cus:LegalIssAuth/>"
                + "<cus:LegalIssDate/>"
                + "</cus:mLEGALID></cus:gLEGALID>"
                + "<cus:Language>" + DEFAULT_LANGUAGE + "</cus:Language>"
                + "<cus:gCUSTOMERRATING g=\"1\"><cus:CustomerRating>" + DEFAULT_CUSTOMER_RATING + "</cus:CustomerRating></cus:gCUSTOMERRATING>"
                + "<cus:TITLE/>"
                + "<cus:GIVENNAMES>" + request.getGivenName() + "</cus:GIVENNAMES>"
                + "<cus:FAMILYNAME>" + request.getFamilyName() + "</cus:FAMILYNAME>"
                + "<cus:Gender>" + request.getGender() + "</cus:Gender>"
                + "<cus:DateofBirth>" + request.getDateOfBirth() + "</cus:DateofBirth>"
                + "<cus:MaritalStatus>" + maritalStatus + "</cus:MaritalStatus>"
                + "<cus:gPHONE1 g=\"1\"><cus:mPHONE1 m=\"1\">"
                + "<cus:PHONE1>" + request.getPhoneNumber() + "</cus:PHONE1>"
                + "<cus:SMS1>" + request.getPhoneNumber() + "</cus:SMS1>"
                + "<cus:EMAIL1/>"
                + "</cus:mPHONE1></cus:gPHONE1>"
                + "<cus:CustomerType>" + DEFAULT_CUSTOMER_TYPE + "</cus:CustomerType>"
                + "<cus:CustProvince>" + custProvince + "</cus:CustProvince>"
                + "<cus:CustDistrict>" + custDistrict + "</cus:CustDistrict>"
                + "<cus:CustCommune>" + custCommune + "</cus:CustCommune>"
                + "<cus:CustVillage>" + custVillage + "</cus:CustVillage>"
                + "<cus:Ownership/>"
                + "<cus:RelationManager>" + referralId + "</cus:RelationManager>"
                + "<cus:LoanOfficer/>"
                + "<cus:Staff>" + releasedBy + "</cus:Staff>"
                + "<cus:ReferralBy>" + referralId + "</cus:ReferralBy>"
                + "<cus:CUSTPROVINCEP>" + placeOfBirth + "</cus:CUSTPROVINCEP>"
                + "<cus:CUSTDISTRICTP/>"
                + "<cus:CUSTCOMMUNEP/>"
                + "<cus:CUSTVILLAGEP/>"
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

    private String getOrDefault(String value, String defaultValue) {
        return value != null ? value : defaultValue;
    }
}
