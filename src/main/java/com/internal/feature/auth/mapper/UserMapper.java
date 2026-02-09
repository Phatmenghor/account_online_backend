package com.internal.feature.auth.mapper;

import com.internal.enumation.RoleEnum;
import com.internal.enumation.StatusData;
import com.internal.feature.auth.dto.response.AllUserResponseDto;
import com.internal.feature.auth.dto.response.UserResponseDto;
import com.internal.feature.auth.models.Role;
import com.internal.feature.auth.models.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "username", target = "idCard")
    @Mapping(source = "userPermission", target = "userPermission")
    @Mapping(source = "status", target = "userStatus", qualifiedByName = "statusToString")
    @Mapping(source = "roles", target = "userRole", qualifiedByName = "rolesToString")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "updatedAt", target = "updatedAt")
    UserResponseDto mapToDto(UserEntity user);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "idCard", target = "username")
    @Mapping(source = "userStatus", target = "status", qualifiedByName = "stringToStatus")
    @Mapping(source = "userRole", target = "roles", qualifiedByName = "stringToRoles")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "updatedAt", target = "updatedAt")
    UserEntity mapToEntity(UserResponseDto user);

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

    // StatusData â†’ String
    @Named("statusToString")
    default String statusToString(StatusData status) {
        return status != null ? status.name() : null;
    }

    // String â†’ StatusData
    @Named("stringToStatus")
    default StatusData stringToStatus(String status) {
        if (status == null || status.isEmpty()) {
            return null;
        }
        try {
            return StatusData.valueOf(status);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // List<Role> â†’ String
    @Named("rolesToString")
    default String rolesToString(List<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return null;
        }
        return roles.stream()
                .map(role -> role.getName().name())
                .collect(Collectors.joining(", "));
    }

    // String â†’ List<Role>
    @Named("stringToRoles")
    default List<Role> stringToRoles(String rolesString) {
        if (rolesString == null || rolesString.isEmpty()) {
            return null;
        }
        // If you need full Role objects with IDs, you might need to inject RoleRepository
        return Arrays.stream(rolesString.split(","))
                .map(String::trim)
                .map(roleName -> {
                    Role role = new Role();
                     role.setName(RoleEnum.valueOf(roleName));
                    return role;
                })
                .collect(Collectors.toList());
    }
}
