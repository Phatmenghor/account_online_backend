package com.internal.feature.auth.mapper;


import com.internal.feature.auth.dto.response.UserResponseDto;
import com.internal.feature.auth.dto.response.AllUserResponseDto;
import com.internal.feature.auth.models.UserEntity;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

/**
 * MapStruct mapper for user entity-DTO conversions.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Maps a UserEntity to UserResponseDto.
     * @param user the user entity
     * @return UserResponseDto
     */
    @Mapping(source = "id", target = "id")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "username", target = "idCard")
    @Mapping(source = "branch", target = "branch")
    @Mapping(source = "status", target = "userStatus", qualifiedByName = "mapStatus")
    @Mapping(source = "roles", target = "userRole", qualifiedByName = "mapRoles")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "updatedAt", target = "updatedAt")
    UserResponseDto mapToDto(UserEntity user);

    /**
     * Maps a list of UserResponseDto and Page<UserEntity> to AllUserResponseDto.
     * @param content list of UserResponseDto
     * @param user Page of UserEntity
     * @return AllUserResponseDto
     */
    @Named("mapToListDto")
    default AllUserResponseDto mapToListDto(List<UserResponseDto> content, Page<UserEntity> user) {
        AllUserResponseDto userResponse = new AllUserResponseDto();
        userResponse.setContent(content);
        userResponse.setPageNo(user.getNumber() + 1);
        userResponse.setPageSize(user.getSize());
        userResponse.setTotalElements(user.getTotalElements());
        userResponse.setTotalPages(user.getTotalPages());
        userResponse.setLast(user.isLast());
        return userResponse;
    }

    /**
     * Maps a status enum to its string representation.
     * @param status the status enum
     * @return String representation of status
     */
    @Named("mapStatus")
    default String mapStatus(com.internal.enumation.StatusData status) {
        return status != null ? status.name() : null;
    }

    /**
     * Maps a list of roles to a comma-separated string of role names.
     * @param roles list of roles
     * @return comma-separated string of role names
     */
    @Named("mapRoles")
    default String mapRoles(List<com.internal.feature.auth.models.Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return null;
        }
        return roles.stream()
                .map(role -> role.getName().name())
                .collect(Collectors.joining(", "));
    }
}