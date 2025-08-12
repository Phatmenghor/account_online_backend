package com.internal.feature.report_staging.mapper;

import com.internal.feature.report_staging.dto.response.*;
import com.internal.feature.report_staging.dto.update.CbcUpdateRequestDto;
import com.internal.feature.report_staging.models.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CbcMapper {

    // Main Record Mappings
    @Mapping(source = "personalInfo", target = "personalInfo")
    @Mapping(source = "idInformation", target = "idInformation")
    @Mapping(source = "addressInformation", target = "addressInformation")
    @Mapping(source = "contactInformation", target = "contactInformation")
    @Mapping(source = "employmentInformation", target = "employmentInformation")
    @Mapping(source = "securityInformation", target = "securityInformation")
    @Mapping(source = "loanInformation", target = "loanInformation")
    CbcMainRecordResponseDto toMainRecordResponseDto(CbcMainRecordEntity entity);

    // Personal Info Mappings
    CbcPersonalInfoDto toPersonalInfoDto(CbcPersonalInfoEntity entity);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "mainRecord", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    CbcPersonalInfoEntity toPersonalInfoEntity(CbcPersonalInfoDto dto);

    // ID Information Mappings
    CbcIdInformationDto toIdInformationDto(CbcIdInformationEntity entity);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "mainRecord", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    CbcIdInformationEntity toIdInformationEntity(CbcIdInformationDto dto);

    // Address Information Mappings
    CbcAddressInformationDto toAddressInformationDto(CbcAddressInformationEntity entity);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "mainRecord", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    CbcAddressInformationEntity toAddressInformationEntity(CbcAddressInformationDto dto);

    // Contact Information Mappings
    CbcContactInformationDto toContactInformationDto(CbcContactInformationEntity entity);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "mainRecord", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    CbcContactInformationEntity toContactInformationEntity(CbcContactInformationDto dto);

    // Employment Information Mappings
    CbcEmploymentInformationDto toEmploymentInformationDto(CbcEmploymentInformationEntity entity);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "mainRecord", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    CbcEmploymentInformationEntity toEmploymentInformationEntity(CbcEmploymentInformationDto dto);

    // Security Information Mappings
    CbcSecurityInformationDto toSecurityInformationDto(CbcSecurityInformationEntity entity);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "mainRecord", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    CbcSecurityInformationEntity toSecurityInformationEntity(CbcSecurityInformationDto dto);

    // Loan Information Mappings
    CbcLoanInformationDto toLoanInformationDto(CbcLoanInformationEntity entity);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "mainRecord", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    CbcLoanInformationEntity toLoanInformationEntity(CbcLoanInformationDto dto);

    // Update mappings
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "personalInfo", ignore = true)
    @Mapping(target = "idInformation", ignore = true)
    @Mapping(target = "addressInformation", ignore = true)
    @Mapping(target = "contactInformation", ignore = true)
    @Mapping(target = "employmentInformation", ignore = true)
    @Mapping(target = "securityInformation", ignore = true)
    @Mapping(target = "loanInformation", ignore = true)
    void updateMainRecordFromDto(CbcUpdateRequestDto dto, @MappingTarget CbcMainRecordEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "mainRecord", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    void updatePersonalInfoFromDto(CbcUpdateRequestDto dto, @MappingTarget CbcPersonalInfoEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "mainRecord", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    void updateIdInformationFromDto(CbcUpdateRequestDto dto, @MappingTarget CbcIdInformationEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "mainRecord", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    void updateAddressInformationFromDto(CbcUpdateRequestDto dto, @MappingTarget CbcAddressInformationEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "mainRecord", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    void updateContactInformationFromDto(CbcUpdateRequestDto dto, @MappingTarget CbcContactInformationEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "mainRecord", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    void updateEmploymentInformationFromDto(CbcUpdateRequestDto dto, @MappingTarget CbcEmploymentInformationEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "mainRecord", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    void updateSecurityInformationFromDto(CbcUpdateRequestDto dto, @MappingTarget CbcSecurityInformationEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "mainRecord", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    void updateLoanInformationFromDto(CbcUpdateRequestDto dto, @MappingTarget CbcLoanInformationEntity entity);
}