package com.account_sell.feature.auth.service.impl;

import com.account_sell.feature.auth.dto.request.LogActionCreateDTO;
import com.account_sell.feature.auth.dto.response.LogActionDTO;
import com.account_sell.feature.auth.mapper.LogActionMapper;
import com.account_sell.feature.auth.models.LogAction;
import com.account_sell.feature.auth.models.UserEntity;
import com.account_sell.feature.auth.repository.LogActionRepository;
import com.account_sell.feature.auth.service.LogActionService;
import com.account_sell.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LogActionServiceImpl implements LogActionService {

    private final LogActionRepository logActionRepository;
    private final LogActionMapper logActionMapper;
    private final SecurityUtils securityUtils;

    @Override
    public LogActionDTO createLogAction(LogActionCreateDTO createDTO) {
        log.info("Creating log action for with action type: {}",
                 createDTO.getActionType());

        final UserEntity user = securityUtils.getCurrentUser();

        LogAction logAction = logActionMapper.toEntity(createDTO);
        logAction.setUser(user);

        LogAction savedLogAction = logActionRepository.save(logAction);

        log.info("Log action created successfully with ID: {}", savedLogAction.getId());
        return logActionMapper.toDTO(savedLogAction);
    }
}