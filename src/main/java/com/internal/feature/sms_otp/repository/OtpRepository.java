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
     * Find the latest active OTP for a phone number
     * Status 0 = active, ordered by newest first
     */
    @Query("SELECT o FROM OtpSms o WHERE o.phone = :phone AND o.status = 0 ORDER BY o.createdAt DESC")
    Optional<OtpSms> findLatestActiveOtpByPhone(@Param("phone") String phone);

    /**
     * Find valid OTP by phone and code
     * Must be active (status=0) and not expired
     */
    @Query("SELECT o FROM OtpSms o WHERE o.phone = :phone AND o.otpCode = :otpCode " +
           "AND o.status = 0 AND o.expiresAt > :now ORDER BY o.createdAt DESC")
    Optional<OtpSms> findValidOtpByPhoneAndCode(
            @Param("phone") String phone,
            @Param("otpCode") String otpCode,
            @Param("now") LocalDateTime now
    );

    /**
     * Get the creation time of the last OTP sent to a phone
     * Used for cooldown checking
     */
    @Query("SELECT o.createdAt FROM OtpSms o WHERE o.phone = :phone ORDER BY o.createdAt DESC")
    Optional<LocalDateTime> findLastOtpCreationTime(@Param("phone") String phone);

    /**
     * Mark all active OTPs as expired for a phone number
     * Called when generating a new OTP
     */
    @Modifying
    @Query("UPDATE OtpSms o SET o.status = 2 WHERE o.phone = :phone AND o.status = 0")
    void expireAllActiveOtpsByPhone(@Param("phone") String phone);
}