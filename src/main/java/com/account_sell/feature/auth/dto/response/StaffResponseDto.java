package com.account_sell.feature.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class StaffResponseDto {
    private String name;
    private String idCard;
    private String sex;
    private String status;
    private String position;
    private String department;
    private String location;
    private String startingDate;
    private String phoneNumber;
    private String probationDate;
    private String email;
}
