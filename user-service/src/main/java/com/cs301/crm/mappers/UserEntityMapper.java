package com.cs301.crm.mappers;

import com.cs301.crm.dtos.requests.CreateUserRequestDTO;
import com.cs301.crm.models.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserEntityMapper {
    UserEntityMapper INSTANCE = Mappers.getMapper(UserEntityMapper.class);

    UserEntity createUserRequestDTOtoUserEntity(CreateUserRequestDTO createUserRequestDTO);

}
