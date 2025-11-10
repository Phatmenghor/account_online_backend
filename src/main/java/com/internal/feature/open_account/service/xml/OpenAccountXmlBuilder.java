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

    /**
     * Build XML for creating a customer in T24
     */
    public String buildCustomerCreationXml(CustomerRequest request) {
        String t24Username = cpbProperties.getT24().getUsername();
        String t24Password = cpbProperties.getT24().getPassword();

        return "<?xml version=\"1.0\" encoding=\"utf-16\"?>"
                + "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" "
                + "xmlns:oaow=\"http://temenos.com/OAOWAR\" "
                + "xmlns:cus=\"http://temenos.com/CUSTOMERCPBCREATEOAO\">"
                + "<soapenv:Header />"
                + "<soapenv:Body>"
                + "<oaow:OAOCUSTOMERCREATION>"
                + "<WebRequestCommon>"
                + "<company>" + (request.getBranchCode() != null ? request.getBranchCode() : "KH0012011") + "</company>"
                + "<password>" + t24Password + "</password>"
                + "<userName>" + t24Username + "</userName>"
                + "</WebRequestCommon>"
                + "<OfsFunction />"
                + "<CUSTOMERCPBCREATEOAOType id=\"\">"
                + "<cus:gSHORTNAME g=\"1\"><cus:ShortName>" + request.getGivenName() + "</cus:ShortName></cus:gSHORTNAME>"
                + "<cus:gNAME1 g=\"1\"><cus:FullName>" + request.getFamilyName() + " " + request.getGivenName() + "</cus:FullName></cus:gNAME1>"
                + "<cus:gNAME2 g=\"1\"><cus:FullName2>" + request.getLastNameKh() + " " + request.getFirstNameKh() + "</cus:FullName2></cus:gNAME2>"
                + "<cus:gSTREET g=\"1\"><cus:STREET>" + (request.getLegalAddress() != null ? request.getLegalAddress() : "") + "</cus:STREET></cus:gSTREET>"
                + "<cus:Sector>6010</cus:Sector>"
                + "<cus:CostCenter>1000</cus:CostCenter>"
                + "<cus:Industry>1000</cus:Industry>"
                + "<cus:Target>1</cus:Target>"
                + "<cus:Nationality>" + request.getNationality() + "</cus:Nationality>"
                + "<cus:CustomerStatus>1</cus:CustomerStatus>"
                + "<cus:Residence>" + request.getNationality() + "</cus:Residence>"
                + "<cus:gLEGALID g=\"1\"><cus:mLEGALID m=\"1\">"
                + "<cus:LegalId>" + request.getLegalId() + "</cus:LegalId>"
                + "<cus:LegalDocName>" + request.getLegalDocName() + "</cus:LegalDocName>"
                + "<cus:LegalHolderName></cus:LegalHolderName>"
                + "<cus:LegalIssAuth></cus:LegalIssAuth>"
                + "<cus:LegalIssDate></cus:LegalIssDate>"
                + "</cus:mLEGALID></cus:gLEGALID>"
                + "<cus:Language>1</cus:Language>"
                + "<cus:gCUSTOMERRATING g=\"1\"><cus:CustomerRating>1</cus:CustomerRating></cus:gCUSTOMERRATING>"
                + "<cus:TITLE></cus:TITLE>"
                + "<cus:GIVENNAMES>" + request.getGivenName() + "</cus:GIVENNAMES>"
                + "<cus:FAMILYNAME>" + request.getFamilyName() + "</cus:FAMILYNAME>"
                + "<cus:Gender>" + request.getGender() + "</cus:Gender>"
                + "<cus:DateofBirth>" + request.getDateOfBirth() + "</cus:DateofBirth>"
                + "<cus:MaritalStatus>" + (request.getMaritalStatus() != null ? request.getMaritalStatus() : "") + "</cus:MaritalStatus>"
                + "<cus:gPHONE1 g=\"1\"><cus:mPHONE1 m=\"1\">"
                + "<cus:PHONE1>" + request.getPhoneNumber() + "</cus:PHONE1>"
                + "<cus:SMS1>" + request.getPhoneNumber() + "</cus:SMS1>"
                + "<cus:EMAIL1/>"
                + "</cus:mPHONE1></cus:gPHONE1>"
                + "<cus:CustomerType>INDIVIDUAL</cus:CustomerType>"
                + "<cus:CustProvince>" + (request.getCustomerProvince() != null ? request.getCustomerProvince() : "") + "</cus:CustProvince>"
                + "<cus:CustDistrict>" + (request.getCustomerDistrict() != null ? request.getCustomerDistrict() : "") + "</cus:CustDistrict>"
                + "<cus:CustCommune>" + (request.getCustomerCommune() != null ? request.getCustomerCommune() : "") + "</cus:CustCommune>"
                + "<cus:CustVillage>" + (request.getCustomerVillage() != null ? request.getCustomerVillage() : "") + "</cus:CustVillage>"
                + "<cus:Ownership></cus:Ownership>"
                + "<cus:RelationManager>" + (request.getReferralId() != null ? request.getReferralId() : "") + "</cus:RelationManager>"
                + "<cus:LoanOfficer></cus:LoanOfficer>"
                + "<cus:Staff>" + (request.getReleasedBy() != null ? request.getReleasedBy() : "") + "</cus:Staff>"
                + "<cus:ReferralBy>" + (request.getReferralId() != null ? request.getReferralId() : "") + "</cus:ReferralBy>"
                + "<cus:CUSTPROVINCEP>" + (request.getPlaceOfBirth() != null ? request.getPlaceOfBirth() : "") + "</cus:CUSTPROVINCEP>"
                + "<cus:CUSTDISTRICTP></cus:CUSTDISTRICTP>"
                + "<cus:CUSTCOMMUNEP></cus:CUSTCOMMUNEP>"
                + "<cus:CUSTVILLAGEP></cus:CUSTVILLAGEP>"
                + "</CUSTOMERCPBCREATEOAOType>"
                + "</oaow:OAOCUSTOMERCREATION>"
                + "</soapenv:Body>"
                + "</soapenv:Envelope>";
    }

    /**
     * Build XML for opening a new account (KHR/USD)
     */
    public String buildAccountCreationXml(CustomerRequest request, String cif, String currency) {
        String t24Username = cpbProperties.getT24().getUsername();
        String t24Password = cpbProperties.getT24().getPassword();

        // Format current date as EffectiveDate
        String effectiveDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        return "<?xml version=\"1.0\" encoding=\"utf-16\"?>"
                + "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" "
                + "xmlns:oaow=\"http://temenos.com/OAOWAR\" "
                + "xmlns:aaar=\"http://temenos.com/AAARRANGEMENTACTIVITYAANEWOAO\">"
                + "<soapenv:Header/>"
                + "<soapenv:Body>"
                + "<oaow:ACCREATIONOAO>"
                + "<WebRequestCommon>"
                + "<company>" + (request.getBranchCode() != null ? request.getBranchCode() : "KH0012011") + "</company>"
                + "<password>" + t24Password + "</password>"
                + "<userName>" + t24Username + "</userName>"
                + "</WebRequestCommon>"
                + "<OfsFunction/>"
                + "<AAARRANGEMENTACTIVITYAANEWOAOType id=\"\">"
                + "<aaar:Arrangement>NEW</aaar:Arrangement>"
                + "<aaar:Activity>ACCOUNTS-NEW-ARRANGEMENT</aaar:Activity>"
                + "<aaar:EffectiveDate>" + effectiveDate + "</aaar:EffectiveDate>"
                + "<aaar:gCUSTOMER g=\"1\">"
                + "<aaar:mCUSTOMER m=\"1\">"
                + "<aaar:Customer>" + cif + "</aaar:Customer>"
                + "<aaar:CustomerRole>OWNER</aaar:CustomerRole>"
                + "</aaar:mCUSTOMER>"
                + "</aaar:gCUSTOMER>"
                + "<aaar:Product>SAVE.ACCT.ONLINE</aaar:Product>"
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
}
