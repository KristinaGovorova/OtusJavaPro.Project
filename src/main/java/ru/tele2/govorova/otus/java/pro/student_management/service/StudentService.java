package ru.tele2.govorova.otus.java.pro.student_management.service;

import com.opencsv.exceptions.CsvValidationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.tele2.govorova.otus.java.pro.student_management.dto.StudentDTO;
import ru.tele2.govorova.otus.java.pro.student_management.dto.response.CsvUploadResponse;
import ru.tele2.govorova.otus.java.pro.student_management.dto.response.GenerationResponse;
import ru.tele2.govorova.otus.java.pro.student_management.exceptions.StudentValidationException;


import java.io.IOException;
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

    @Transactional
    byte[] exportStudentsToExcel(String firstName, String lastName, String email, String passportNumber,
                                 LocalDate enrollmentDateFrom, LocalDate enrollmentDateTo, Pageable pageable);

    @Transactional
    byte[] exportStudentsToPDF(String firstName, String lastName, String email, String passportNumber,
                               LocalDate enrollmentDateFrom, LocalDate enrollmentDateTo, Pageable pageable);

    @Transactional
    CsvUploadResponse processStudentUpload(MultipartFile file)
            throws IOException, CsvValidationException, StudentValidationException;

    GenerationResponse generateAndSaveStudents(int count);

    void uploadAvatar(Long id, MultipartFile file);

    byte[] getAvatar(Long id);

}
