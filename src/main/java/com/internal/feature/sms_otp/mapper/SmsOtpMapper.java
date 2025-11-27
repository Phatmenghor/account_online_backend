package com.internal.feature.sms_otp.mapper;

import com.internal.feature.sms_otp.dto.response.SendOtpResponse;
import com.internal.feature.sms_otp.dto.response.VerifyOtpResponse;
import com.internal.feature.sms_otp.models.OtpSms;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SmsOtpMapper {

    @Mapping(target = "message", expression = "java(\"OTP sent successfully to \" + otpSms.getPhone())")
    SendOtpResponse toSendOtpResponse(OtpSms otpSms);

    @Mapping(target = "verified", constant = "true")
    @Mapping(target = "message", constant = "OTP verified successfully")
    VerifyOtpResponse toVerifyOtpResponse(OtpSms otpSms);
}
