package ru.tele2.govorova.otus.java.pro.student_management.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.tele2.govorova.otus.java.pro.student_management.config.AbstractIntegrationTest;
import ru.tele2.govorova.otus.java.pro.student_management.dto.StudentDTO;
import ru.tele2.govorova.otus.java.pro.student_management.dto.response.GenerationResponse;
import ru.tele2.govorova.otus.java.pro.student_management.entity.Student;
import ru.tele2.govorova.otus.java.pro.student_management.exceptions.StudentNotFoundException;


import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;

@Testcontainers
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RequiredArgsConstructor
class StudentServiceImplIntegrationTest extends AbstractIntegrationTest {

    @Test
    void getStudentById_ShouldReturnStudentDto_WhenStudentExists() {
        Student student = getFreshStudentFromDb();
        StudentDTO foundStudent = studentService.getStudentById(student.getId());

        assertThat(foundStudent).isNotNull();
        assertThat(foundStudent.getFirstName()).isEqualTo(student.getFirstName());
        assertThat(foundStudent.getEmail()).isEqualTo(student.getEmail());
    }

    @Test
    @Transactional
    void createStudent_ShouldReturnStudentDTO_WhenStudentIsCreated() {
        Student testStudent = getFreshStudentFromDb();
        StudentDTO studentDTO = studentMapper.toDto(testStudent);

        StudentDTO savedStudent = studentService.createStudent(studentDTO);

        assertThat(savedStudent).isNotNull();
        assertThat(savedStudent.getId()).isNotNull();
        assertThat(savedStudent.getFirstName()).isEqualTo("Иван");
        assertThat(savedStudent.getLastName()).isEqualTo("Тестович");
    }

    @Test
    @Transactional
    void updateStudent_ShouldReturnUpdatedStudentDTO_WhenStudentExists() {
        Student student = getFreshStudentFromDb();

        StudentDTO studentDTO = studentMapper.toDto(student);
        studentDTO.setFirstName("UpdatedName");
        StudentDTO updatedStudent = studentService.updateStudent(student.getId(), studentDTO);

        assertThat(updatedStudent.getFirstName()).isEqualTo("UpdatedName");
    }

    @Test
    void getAllStudentsWithPaginationAndFiltering() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<StudentDTO> studentsPage = studentService.getAllStudents(
                "John", null, null, null, null, pageRequest);

        assertThat(studentsPage).isNotNull();
        assertThat(studentsPage.getContent()).hasSizeLessThanOrEqualTo(10);
        assertThat(studentsPage.getContent()).allMatch(student -> student.getFirstName().contains("John"));
    }

    @Test
    void deleteStudent_ShouldReturnStudentNotFoundException_WhenStudentDoesNotExist() {
        Student student = getFreshStudentFromDb();
        studentService.deleteStudent(student.getId());

        assertThatThrownBy(() -> studentService.getStudentById(student.getId()))
                .isInstanceOf(StudentNotFoundException.class);
    }

    @Test
    void uploadAndGetAvatar_ShouldSaveAndRetrieveAvatarSuccessfully() {
        Student student = getFreshStudentFromDb();
        MockMultipartFile avatar = new MockMultipartFile(
                "file", "avatar.png", "image/png", "test-image-data".getBytes(StandardCharsets.UTF_8));

        studentService.uploadAvatar(student.getId(), avatar);
        byte[] retrievedAvatar = studentService.getAvatar(student.getId());

        assertThat(retrievedAvatar).isNotEmpty();
        assertThat(new String(retrievedAvatar, StandardCharsets.UTF_8)).isEqualTo("test-image-data");
    }

    @Test
    void uploadAvatarExceedingSizeLimit_ShouldThrowIllegalArgumentException() {
        Student student = getFreshStudentFromDb();
        MockMultipartFile largeFile = new MockMultipartFile(
                "file", "large-file.png", "image/png", new byte[(int) (10 * 1024 * 1024)]);

        assertThatThrownBy(() -> studentService.uploadAvatar(student.getId(), largeFile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Файл превышает допустимый размер");
    }

    @Test
    void generateAndSaveStudents_ShouldGenerateAndSaveStudents() {
        int studentCount = 10;
        GenerationResponse response = studentService.generateAndSaveStudents(studentCount);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).contains("Generated");
        assertThat(response.getExecutionTimeMs()).isGreaterThan(0);
        assertThat(studentRepository.count()).isGreaterThanOrEqualTo(studentCount);
    }
}