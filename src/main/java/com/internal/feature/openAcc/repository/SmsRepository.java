package com.internal.feature.openAcc.repository;

import com.internal.feature.openAcc.dto.request.TblSetting;
import com.internal.feature.openAcc.dto.response.OtpRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SmsRepository {

    private final JdbcTemplate jdbcTemplate;

    public Optional<OtpRecord> getLatestOtpRecord(String phone) {
        String sql = "SELECT TOP 1 * FROM tbl_otp_sms WHERE gendate IS NOT NULL AND sts = 0 AND phone = ? ORDER BY id DESC";
        return jdbcTemplate.query(sql, new Object[]{phone}, rs -> {
            if (rs.next()) {
                OtpRecord record = new OtpRecord();
                record.setId(rs.getInt("id"));
                record.setAttempt(rs.getInt("attempt"));
                record.setLastAttempt(rs.getTimestamp("last_attempt") != null ? rs.getTimestamp("last_attempt").toLocalDateTime() : null);
                record.setOtpCode(rs.getInt("otp_code"));
                return Optional.of(record);
            }
            return Optional.empty();
        });
    }

    public TblSetting getAcctOnlineSetting() {
        String sql = "SELECT TOP 1 * FROM tbl_Setting WHERE set_type = 'AcctOnline'";
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            TblSetting s = new TblSetting();
            s.setSetDesc(rs.getString("set_desc"));
            return s;
        });
    }

    public void resetOtpAttempt(int otpId) {
        String sql = "UPDATE tbl_otp_sms SET attempt = 0, last_attempt = GETDATE() WHERE id = ?";
        jdbcTemplate.update(sql, otpId);
    }

    public Integer insertOtpSmsAndGetId(String phone, String app, String text, int otp) {
        String sql = "exec sp_pos_Sms ?, ?, ?, ?";
        return jdbcTemplate.query(sql, new Object[]{phone, app, text, otp}, rs -> {
            if (rs.next()) {
                return rs.getInt("Rec_id");
            }
            return null;
        });
    }

    public LocalDateTime getLastGeneratedOtpTime(String phone) {
        String sql = "SELECT TOP 1 gendate FROM tbl_otp_sms WHERE phone = ? ORDER BY gendate DESC";
        return jdbcTemplate.query(sql, new Object[]{phone}, rs -> {
            if (rs.next()) {
                return rs.getTimestamp("gendate").toLocalDateTime();
            }
            return null;
        });
    }
}