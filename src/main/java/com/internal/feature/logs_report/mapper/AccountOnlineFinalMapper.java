package com.internal.feature.logs_report.mapper;


import com.internal.feature.auth.mapper.UserMapper;
import com.internal.feature.logs_report.dto.response.AccountOnlineFinalLogResponseDto;
import com.internal.feature.logs_report.model.AccountOnlineFinal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface AccountOnlineFinalMapper {

//    @Mapping(target = "customerInfo.legalId", source = "legalId")
//    @Mapping(target = "customerInfo.familyName", source = "familyName")
//    @Mapping(target = "customerInfo.givenName", source = "givenName")
//    @Mapping(target = "customerInfo.firstNameKh", source = "firstNameKh")
//    @Mapping(target = "customerInfo.lastNameKh", source = "lastNameKh")
//    @Mapping(target = "customerInfo.dateOfBirth", source = "dateOfBirth")
//    @Mapping(target = "customerInfo.gender", source = "gender")
//    @Mapping(target = "customerInfo.nationality", source = "nationality")
//    @Mapping(target = "customerInfo.legalAddress", source = "currentAddressName")
    AccountOnlineFinalLogResponseDto toDto(AccountOnlineFinal history);
}
