package com.internal.feature.openAcc.mapper;

import com.internal.feature.openAcc.dto.response.SendOtpResponse;
import com.internal.feature.openAcc.dto.response.VerifyOtpResponse;
import com.internal.feature.openAcc.models.OtpSms;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OtpMapper {

    @Mapping(target = "message", expression = "java(\"OTP sent successfully to \" + otpSms.getPhone())")
    SendOtpResponse toSendOtpResponse(OtpSms otpSms);

    @Mapping(target = "verified", constant = "true")
    @Mapping(target = "message", constant = "OTP verified successfully")
    VerifyOtpResponse toVerifyOtpResponse(OtpSms otpSms);
}