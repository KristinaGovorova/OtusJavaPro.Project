package ru.tele2.govorova.otus.java.pro.student_management.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.tele2.govorova.otus.java.pro.student_management.dto.PassportDTO;
import ru.tele2.govorova.otus.java.pro.student_management.entity.Passport;

@Mapper(componentModel = "spring")
public interface PassportMapper {

    @Mapping(target = "id", ignore = true)
    Passport toEntity(PassportDTO dto);

    PassportDTO toDto(Passport entity);

    @Mapping(target = "id", ignore = true)
    void updatePassportFromDto(PassportDTO dto, @MappingTarget Passport entity);
}