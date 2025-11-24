package com.internal.feature.reference.mapper;

import com.internal.feature.reference.dto.request.ReferenceCreateRequestDto;
import com.internal.feature.reference.dto.request.ReferenceUpdateRequestDto;
import com.internal.feature.reference.dto.response.AllReferenceResponseDto;
import com.internal.feature.reference.dto.response.ReferenceDto;
import com.internal.feature.reference.models.Reference;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReferenceMapper {

    ReferenceMapper INSTANCE = Mappers.getMapper(ReferenceMapper.class);

    /** Entity → DTO with optional language filter */
    default ReferenceDto toDto(Reference bank) {
        if (bank == null) return null;

        ReferenceDto dto = new ReferenceDto();
        dto.setId(bank.getId());
        dto.setStatus(bank.getStatus());
            dto.setNameEn(bank.getNameEn());
            dto.setNameKh(bank.getNameKh());

        return dto;
    }

    /** Map List of Entities to List of DTOs */
    List<ReferenceDto> toDtoList(List<Reference> references);

    @Named("mapToListDto")
    default AllReferenceResponseDto mapToListDto(List<ReferenceDto> content, Page<Reference> referenceBank) {
        AllReferenceResponseDto referenceBankList = new AllReferenceResponseDto();
        referenceBankList.setContent(content);
        referenceBankList.setPageNo(referenceBank.getNumber() + 1);
        referenceBankList.setPageSize(referenceBank.getSize());
        referenceBankList.setTotalElements(referenceBank.getTotalElements());
        referenceBankList.setTotalPages(referenceBank.getTotalPages());
        referenceBankList.setLast(referenceBank.isLast());
        return referenceBankList;
    }

    /** Create request → entity */
    default Reference fromCreateDto(ReferenceCreateRequestDto request) {
        if (request == null) return null;
        Reference bank = new Reference();
        bank.setNameEn(request.getNameEn());
        bank.setNameKh(request.getNameKh());
        bank.setStatus(request.getStatus());
        return bank;
    }

    /** Update entity from update request (partial update supported) */
    default void updateFromDto(ReferenceUpdateRequestDto request, @MappingTarget Reference bank) {
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
