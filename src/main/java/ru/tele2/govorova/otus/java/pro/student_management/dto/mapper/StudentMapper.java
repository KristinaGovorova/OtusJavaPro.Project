package ru.tele2.govorova.otus.java.pro.student_management.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import ru.tele2.govorova.otus.java.pro.student_management.dto.StudentDTO;
import ru.tele2.govorova.otus.java.pro.student_management.dto.TranscriptDTO;
import ru.tele2.govorova.otus.java.pro.student_management.entity.Student;
import ru.tele2.govorova.otus.java.pro.student_management.entity.Transcript;

import java.util.Base64;
import java.util.List;

@Mapper(componentModel = "spring",
        uses = {PassportMapper.class, TranscriptMapper.class})
public interface StudentMapper {

    @Mapping(source = "avatar", target = "avatar", qualifiedByName = "byteArrayToBase64")
    StudentDTO toDto(Student student);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "avatar", ignore = true)
    @Mapping(target = "passport", ignore = true)
    @Mapping(target = "transcripts", ignore = true)
    void updateStudentFromDto(StudentDTO dto, @MappingTarget Student entity);

    @Named("byteArrayToBase64")
    default String byteArrayToBase64(byte[] bytes) {
        return bytes == null ? null : Base64.getEncoder().encodeToString(bytes);
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "student", ignore = true)
    Transcript toTranscriptEntity(TranscriptDTO dto);

    default List<Transcript> toTranscriptEntities(List<TranscriptDTO> dtos, Student student) {
        return dtos == null ? List.of() : dtos.stream()
                .map(dto -> {
                    Transcript transcript = toTranscriptEntity(dto);
                    transcript.setStudent(student);
                    return transcript;
                })
                .toList();
    }
}
