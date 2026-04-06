package com.internal.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "cpb.defaults")
@Data
public class DefaultProperties {

    private String branchCode = "KH0012011";
    private String sector = "4501";
    private String costCenter = "";
    private String industry = "4500";
    private String target = "220";
    private String language = "2";
    private String customerRating = "1";
    private String customerStatus = "1";
    private String customerType = "ACTIVE";
    private String ownership = "304";
    private String legalHolderName = "NATIONAL.ID";
    private String nationality = "KH";
    private String productCode = "SAVE.ACCT.ONLINE";
    private String accountActivity = "ACCOUNTS-NEW-ARRANGEMENT";
    private String newArrangement = "NEW";
}
