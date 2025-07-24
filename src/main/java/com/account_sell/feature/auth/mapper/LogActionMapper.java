package com.account_sell.feature.auth.mapper;

import com.account_sell.feature.auth.dto.request.LogActionCreateDTO;
import com.account_sell.feature.auth.dto.response.LogActionDTO;
import com.account_sell.feature.auth.models.LogAction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LogActionMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "statusLog", target = "statusLog")
    LogActionDTO toDTO(LogAction logAction);
    
    @Mapping(source = "userId", target = "user.id")
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "statusLog", target = "statusLog")
    LogAction toEntity(LogActionCreateDTO createDTO);

}