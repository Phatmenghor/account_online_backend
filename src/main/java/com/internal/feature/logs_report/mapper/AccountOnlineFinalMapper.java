package com.internal.feature.logs_report.mapper;


import com.internal.feature.auth.mapper.UserMapper;
import com.internal.feature.logs_report.dto.response.AccountOnlineFinalResponseDto;
import com.internal.feature.logs_report.model.AccountOnlineFinal;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface AccountOnlineFinalMapper {

    AccountOnlineFinalResponseDto toDto(AccountOnlineFinal history);
}
