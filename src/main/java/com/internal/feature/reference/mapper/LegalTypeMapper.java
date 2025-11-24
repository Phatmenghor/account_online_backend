package com.internal.feature.reference.mapper;


import com.internal.feature.reference.dto.request.LegalTypeCreateRequestDto;
import com.internal.feature.reference.dto.request.LegalTypeUpdateRequestDto;
import com.internal.feature.reference.dto.response.AllLegalTypeResponseDto;
import com.internal.feature.reference.dto.response.LegalTypeDto;
import com.internal.feature.reference.models.LegalType;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;

import java.util.List;
@Mapper(componentModel = "spring")
public interface LegalTypeMapper {
    /** Entity → DTO with optional language filter */
    default LegalTypeDto toDto(LegalType doc) {
        if (doc == null) return null;

        LegalTypeDto dto = new LegalTypeDto();
        dto.setId(doc.getId());
        dto.setStatus(doc.getStatus());
        dto.setNameKh(doc.getNameKh());
        dto.setNameEn(doc.getNameEn());
        dto.setLegalTypeValue(doc.getLegalTypeValue());

        return dto;
    }

    /** Map List of Entities to List of DTOs */
    List<LegalTypeDto> toDtoList(List<LegalType> legalTypes);

    @Named("mapToListDto")
    default AllLegalTypeResponseDto mapToListDto(List<LegalTypeDto> content, Page<LegalType> legalType) {
        AllLegalTypeResponseDto legalTypeList = new AllLegalTypeResponseDto();
        legalTypeList.setContent(content);
        legalTypeList.setPageNo(legalType.getNumber() + 1);
        legalTypeList.setPageSize(legalType.getSize());
        legalTypeList.setTotalElements(legalType.getTotalElements());
        legalTypeList.setTotalPages(legalType.getTotalPages());
        legalTypeList.setLast(legalType.isLast());
        return legalTypeList;
    }

    default LegalType fromCreateDto(LegalTypeCreateRequestDto request) {
        if (request == null) return null;
        LegalType doc = new LegalType();
        doc.setNameEn(request.getNameEn());
        doc.setNameKh(request.getNameKh());
        doc.setLegalTypeValue(request.getLegalTypeValue());
        doc.setStatus(request.getStatus());
        return doc;
    }

    /** Update entity from update request (partial update supported) */
    default void updateFromDto(LegalTypeUpdateRequestDto request, @MappingTarget LegalType doc) {
        if (request == null || doc == null) return;

        if (request.getNameEn() != null) {
            doc.setNameEn(request.getNameEn());
        }

        if (request.getNameKh() != null) {
            doc.setNameKh(request.getNameKh());
        }

        if (request.getLegalTypeValue() != null) {
            doc.setLegalTypeValue(request.getLegalTypeValue());
        }

        if (request.getStatus() != null) {
            doc.setStatus(request.getStatus());
        }
    }

}
