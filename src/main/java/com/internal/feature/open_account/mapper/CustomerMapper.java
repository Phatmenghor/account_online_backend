package com.internal.feature.open_account.mapper;

import com.internal.feature.aml.dto.request.CustomerAmlDto;
import com.internal.feature.open_account.dto.request.CustomerRequest;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    CustomerMapper INSTANCE = Mappers.getMapper(CustomerMapper.class);

    CustomerAmlDto toDto(CustomerRequest request);
}
