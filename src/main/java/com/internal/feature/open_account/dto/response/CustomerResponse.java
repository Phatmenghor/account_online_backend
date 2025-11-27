package com.internal.feature.open_account.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {
    private String cif;
    private String khrAccount;
    private String usdAccount;
    private String mnemonic;
}
