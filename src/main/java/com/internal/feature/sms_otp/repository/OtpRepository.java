package com.internal.feature.sms_otp.repository;

import com.internal.feature.sms_otp.models.OtpSms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<OtpSms, Long> {

    /**
     * Get latest active OTP (status = 0)
     */
    @Query(value = "SELECT * FROM acc_online_otp_sms o " +
            "WHERE o.phone = :phone AND o.status = 0 " +
            "ORDER BY o.created_at DESC LIMIT 1", nativeQuery = true)
    Optional<OtpSms> findLatestActiveOtpByPhone(@Param("phone") String phone);

    /**
     * Validate OTP by phone and code
     */
    @Query(value = "SELECT * FROM acc_online_otp_sms o " +
            "WHERE o.phone = :phone AND o.otp_code = :otpCode " +
            "AND o.status = 0 AND o.expires_at > :now " +
            "ORDER BY o.created_at DESC LIMIT 1", nativeQuery = true)
    Optional<OtpSms> findValidOtpByPhoneAndCode(
            @Param("phone") String phone,
            @Param("otpCode") String otpCode,
            @Param("now") LocalDateTime now
    );

    /**
     * Last OTP creation time (cooldown)
     */
    @Query(value = "SELECT o.created_at FROM acc_online_otp_sms o " +
            "WHERE o.phone = :phone ORDER BY o.created_at DESC LIMIT 1",
            nativeQuery = true)
    Optional<LocalDateTime> findLastOtpCreationTime(@Param("phone") String phone);

    /**
     * Expire all active (status=0) OTPs
     */
    @Modifying
    @Query("UPDATE OtpSms o SET o.status = 2 WHERE o.phone = :phone AND o.status = 0")
    void expireAllActiveOtpsByPhone(@Param("phone") String phone);
}
