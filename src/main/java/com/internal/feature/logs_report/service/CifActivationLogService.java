package com.internal.feature.logs_report.service;

import com.internal.feature.logs_report.model.CifActivationLog;
import com.internal.feature.logs_report.repository.CifActivationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CifActivationLogService {

    private final CifActivationLogRepository cifActivationLogRepository;

    @Async
    @Transactional
    public void saveLog(CifActivationLog activationLog) {
        try {
            cifActivationLogRepository.save(activationLog);
            log.info("CIF activation log saved: CIF={}, success={}, activationCode={}",
                    activationLog.getCifNo(),
                    activationLog.getIsSuccess(),
                    activationLog.getActivationCode());
        } catch (Exception e) {
            log.error("Failed to save CIF activation log for CIF={}: {}", activationLog.getCifNo(), e.getMessage());
        }
    }
}
