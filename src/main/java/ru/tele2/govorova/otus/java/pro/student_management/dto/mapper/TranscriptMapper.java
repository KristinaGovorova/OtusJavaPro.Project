package ru.tele2.govorova.otus.java.pro.student_management.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.tele2.govorova.otus.java.pro.student_management.dto.TranscriptDTO;
import ru.tele2.govorova.otus.java.pro.student_management.entity.Transcript;

@Mapper(componentModel = "spring")
public interface TranscriptMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "student", ignore = true)
    Transcript toEntity(TranscriptDTO dto);

    TranscriptDTO toDto(Transcript entity);
}