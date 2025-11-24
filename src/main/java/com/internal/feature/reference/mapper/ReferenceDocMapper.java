package com.internal.feature.reference.mapper;


import com.internal.feature.reference.dto.request.ReferenceDocCreateRequestDto;
import com.internal.feature.reference.dto.request.ReferenceDocUpdateRequestDto;
import com.internal.feature.reference.dto.response.AllReferenceDocResponseDto;
import com.internal.feature.reference.dto.response.ReferenceDocDto;
import com.internal.feature.reference.models.ReferenceDoc;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;

import java.util.List;
@Mapper(componentModel = "spring")
public interface ReferenceDocMapper {
    /** Entity → DTO with optional language filter */
    default ReferenceDocDto toDto(ReferenceDoc doc) {
        if (doc == null) return null;

        ReferenceDocDto dto = new ReferenceDocDto();
        dto.setId(doc.getId());
        dto.setStatus(doc.getStatus());
        dto.setNameKh(doc.getNameKh());
        dto.setNameEn(doc.getNameEn());

        return dto;
    }

    /** Map List of Entities to List of DTOs */
    List<ReferenceDocDto> toDtoList(List<ReferenceDoc> referenceDocs);

    @Named("mapToListDto")
    default AllReferenceDocResponseDto mapToListDto(List<ReferenceDocDto> content, Page<ReferenceDoc> referenceDoc) {
        AllReferenceDocResponseDto referenceDocList = new AllReferenceDocResponseDto();
        referenceDocList.setContent(content);
        referenceDocList.setPageNo(referenceDoc.getNumber() + 1);
        referenceDocList.setPageSize(referenceDoc.getSize());
        referenceDocList.setTotalElements(referenceDoc.getTotalElements());
        referenceDocList.setTotalPages(referenceDoc.getTotalPages());
        referenceDocList.setLast(referenceDoc.isLast());
        return referenceDocList;
    }

    default ReferenceDoc fromCreateDto(ReferenceDocCreateRequestDto request) {
        if (request == null) return null;
        ReferenceDoc doc = new ReferenceDoc();
        doc.setNameEn(request.getNameEn());
        doc.setNameKh(request.getNameKh());
        doc.setStatus(request.getStatus());
        return doc;
    }

    /** Update entity from update request (partial update supported) */
    default void updateFromDto(ReferenceDocUpdateRequestDto request, @MappingTarget ReferenceDoc doc) {
        if (request == null || doc == null) return;

        if (request.getNameEn() != null) {
            doc.setNameEn(request.getNameEn());
        }

        if (request.getNameKh() != null) {
            doc.setNameKh(request.getNameKh());
        }

        if (request.getStatus() != null) {
            doc.setStatus(request.getStatus());
        }
    }

}
