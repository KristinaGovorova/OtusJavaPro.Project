package ru.tele2.govorova.otus.java.pro.student_management.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import ru.tele2.govorova.otus.java.pro.student_management.dto.StudentDTO;
import ru.tele2.govorova.otus.java.pro.student_management.dto.response.GenerationResponse;


import java.time.LocalDate;
import java.util.List;

public interface StudentService {

    Page<StudentDTO> getAllStudents(
            String firstName, String lastName, String email,
            LocalDate enrollmentDateFrom, LocalDate enrollmentDateTo,
            Pageable pageable);

    List<StudentDTO> getAllStudents();

    StudentDTO createStudent(StudentDTO studentDTO);

    StudentDTO updateStudent(Long studentId, StudentDTO studentDTO);

    StudentDTO getStudentById(Long studentId);

    void deleteStudent(Long studentId);

    GenerationResponse generateAndSaveStudents(int count);

    void uploadAvatar(Long id, MultipartFile file);

    byte[] getAvatar(Long id);

}
