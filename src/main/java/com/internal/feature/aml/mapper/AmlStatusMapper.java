package com.internal.feature.aml.mapper;

import com.internal.enumation.AmlStatusEnum;
import com.internal.feature.aml.dto.request.CreateAmlRequestDto;
import com.internal.feature.aml.dto.request.CustomerAmlDto;
import com.internal.feature.aml.dto.response.AllAmlResponseDto;
import com.internal.feature.aml.dto.response.AmlStatusDto;
import com.internal.feature.aml.model.AmlStatus;
import com.internal.feature.auth.mapper.UserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface AmlStatusMapper {

    default AmlStatus fromCreateDto(CreateAmlRequestDto request, UserMapper userMapper) {
        if (request == null) return null;

        AmlStatus status = new AmlStatus();
        status.setOriginalRequest(request.getOriginalRequest());
        status.setOriginalResponse(request.getOriginalResponse());
        status.setStatus(request.getStatus() != null ? request.getStatus() : AmlStatusEnum.PENDING);
        status.setIdDisplay(request.getIdDisplay());
        status.setGender(request.getGender());
        status.setFamilyName(request.getFamilyName());
        status.setDateOfBirth(request.getDateOfBirth());
        status.setGivenName(request.getGivenName());
        status.setNationality(request.getNationality());
        status.setLegalAddress(request.getLegalAddress());
        status.setFirstNameKh(request.getFirstNameKh());
        status.setLastNameKh(request.getLastNameKh());

        if (request.getApprovedBy() != null) {
            status.setApprovedBy(userMapper.mapToEntity(request.getApprovedBy()));
        }
        if (request.getRejectedBy() != null) {
            status.setRejectedBy(userMapper.mapToEntity(request.getRejectedBy()));
        }

        return status;
    }
//
//    default void updateFromDto(UpdateAmlRequestDto request, @MappingTarget AmlStatus status, UserMapper userMapper) {
//        if (request == null || status == null) return;
//
//        // ✅ Updatable status change
//        if (request.getStatus() != null) status.setStatus(request.getStatus());
//
//        // ✅ Update new customer info fields
//        if (request.getIdDisplay() != null) status.setIdDisplay(request.getIdDisplay());
//        if (request.getGender() != null) status.setGender(request.getGender());
//        if (request.getFamilyName() != null) status.setFamilyName(request.getFamilyName());
//        if (request.getGivenName() != null) status.setGivenName(request.getGivenName());
//        if (request.getFirstNameKh() != null) status.setFirstNameKh(request.getFirstNameKh());
//        if (request.getLastNameKh() != null) status.setLastNameKh(request.getLastNameKh());
//        if (request.getDateOfBirth() != null) status.setDateOfBirth(request.getDateOfBirth());
//        if (request.getNationality() != null) status.setNationality(request.getNationality());
//        if (request.getLegalAddress() != null) status.setLegalAddress(request.getLegalAddress());
//
//        // ✅ If business later allows these fields to update as well, include accordingly
//
//        // ✅ Map approvedBy / rejectedBy if changed
//        if (request.getApprovedBy() != null) {
//            status.setApprovedBy(userMapper.mapToEntity(request.getApprovedBy()));
//        }
//        if (request.getRejectedBy() != null) {
//            status.setRejectedBy(userMapper.mapToEntity(request.getRejectedBy()));
//        }
//    }

    default AmlStatusDto toStatusDto(AmlStatus status, UserMapper userMapper) {
        if (status == null) return null;

        // Map customer info
        CustomerAmlDto customer = new CustomerAmlDto();
        customer.setIdDisplay(status.getIdDisplay());
        customer.setFamilyName(status.getFamilyName());
        customer.setGivenName(status.getGivenName());
        customer.setFirstNameKh(status.getFirstNameKh());
        customer.setLastNameKh(status.getLastNameKh());
        customer.setDateOfBirth(status.getDateOfBirth());
        customer.setGender(status.getGender());
        customer.setNationality(status.getNationality());
        customer.setLegalAddress(status.getLegalAddress());

        AmlStatusDto dto = new AmlStatusDto();
        dto.setId(status.getId());
        dto.setOriginalRequest(status.getOriginalRequest());
        dto.setOriginalResponse(status.getOriginalResponse());
        dto.setStatus(status.getStatus());
        dto.setApprovedBy(status.getApprovedBy() != null ? userMapper.mapToDto(status.getApprovedBy()) : null);
        dto.setRejectedBy(status.getRejectedBy() != null ? userMapper.mapToDto(status.getRejectedBy()) : null);
        dto.setCustomerInfo(customer); // ✅ set customerInfo

        return dto;
    }

    @Named("mapToListDto")
    default AllAmlResponseDto mapToListDto(List<AmlStatusDto> content, Page<AmlStatus> statuses) {
        AllAmlResponseDto amlDtoList = new AllAmlResponseDto();
        amlDtoList.setContent(content);
        amlDtoList.setPageNo(statuses.getNumber() + 1);
        amlDtoList.setPageSize(statuses.getSize());
        amlDtoList.setTotalElements(statuses.getTotalElements());
        amlDtoList.setTotalPages(statuses.getTotalPages());
        amlDtoList.setLast(statuses.isLast());
        return amlDtoList;
    }
}
