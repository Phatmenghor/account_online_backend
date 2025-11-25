package com.internal.feature.logs_report.mapper;


import com.internal.feature.auth.mapper.UserMapper;
import com.internal.feature.logs_report.dto.response.AccountOnlineFinalResponseDto;
import com.internal.feature.logs_report.dto.response.AllAccountOnlineFinalResponseDto;
import com.internal.feature.logs_report.model.AccountOnlineFinal;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface AccountOnlineFinalMapper {

    AccountOnlineFinalResponseDto toDto(AccountOnlineFinal history);

    @Named("mapToListDto")
    default AllAccountOnlineFinalResponseDto mapToListDto(List<AccountOnlineFinalResponseDto> content, Page<AccountOnlineFinal> accountOnlineFinalPage) {
        AllAccountOnlineFinalResponseDto response = new AllAccountOnlineFinalResponseDto();
        response.setContent(content);
        response.setPageNo(accountOnlineFinalPage.getNumber() + 1);
        response.setPageSize(accountOnlineFinalPage.getSize());
        response.setTotalElements(accountOnlineFinalPage.getTotalElements());
        response.setTotalPages(accountOnlineFinalPage.getTotalPages());
        response.setLast(accountOnlineFinalPage.isLast());
        return response;
    }
}
