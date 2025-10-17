package com.internal.feature.reference.mapper;

import com.internal.enumation.LanguageEnum;
import com.internal.feature.reference.dto.request.ReferenceBankCreateRequestDto;
import com.internal.feature.reference.dto.request.ReferenceBankUpdateRequestDto;
import com.internal.feature.reference.dto.response.AllReferenceBankResponseDto;
import com.internal.feature.reference.dto.response.ReferenceBankDto;
import com.internal.feature.reference.models.ReferenceBank;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReferenceBankMapper {

    ReferenceBankMapper INSTANCE = Mappers.getMapper(ReferenceBankMapper.class);

    /** Entity → DTO with optional language filter */
    default ReferenceBankDto toDto(ReferenceBank bank, LanguageEnum language) {
        if (bank == null) return null;

        ReferenceBankDto dto = new ReferenceBankDto();
        dto.setId(bank.getId());
        dto.setStatus(bank.getStatus());

        if (LanguageEnum.EN.equals(language)) {
            dto.setNameEn(bank.getNameEn());
        } else if (LanguageEnum.KH.equals(language)) {
            dto.setNameKh(bank.getNameKh());
        } else {
            dto.setNameEn(bank.getNameEn());
            dto.setNameKh(bank.getNameKh());
        }

        return dto;
    }

    @Named("mapToListDto")
    default AllReferenceBankResponseDto mapToListDto(List<ReferenceBankDto> content, Page<ReferenceBank> referenceBank) {
        AllReferenceBankResponseDto referenceBankList = new AllReferenceBankResponseDto();
        referenceBankList.setContent(content);
        referenceBankList.setPageNo(referenceBank.getNumber() + 1);
        referenceBankList.setPageSize(referenceBank.getSize());
        referenceBankList.setTotalElements(referenceBank.getTotalElements());
        referenceBankList.setTotalPages(referenceBank.getTotalPages());
        referenceBankList.setLast(referenceBank.isLast());
        return referenceBankList;
    }

    /** Create request → entity */
    default ReferenceBank fromCreateDto(ReferenceBankCreateRequestDto request) {
        if (request == null) return null;
        ReferenceBank bank = new ReferenceBank();
        bank.setNameEn(request.getNameEn());
        bank.setNameKh(request.getNameKh());
        bank.setStatus(request.getStatus());
        return bank;
    }

    /** Update entity from update request (partial update supported) */
    default void updateFromDto(ReferenceBankUpdateRequestDto request, @MappingTarget ReferenceBank bank) {
        if (request == null || bank == null) return;

        if (request.getNameEn() != null) {
            bank.setNameEn(request.getNameEn());
        }

        if (request.getNameKh() != null) {
            bank.setNameKh(request.getNameKh());
        }

        if (request.getStatus() != null) {
            bank.setStatus(request.getStatus());
        }
    }
}
