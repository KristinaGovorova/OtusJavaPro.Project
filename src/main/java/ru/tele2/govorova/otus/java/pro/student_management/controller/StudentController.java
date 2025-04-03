package ru.tele2.govorova.otus.java.pro.student_management.controller;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.tele2.govorova.otus.java.pro.student_management.dto.StudentDTO;
import ru.tele2.govorova.otus.java.pro.student_management.exceptions.StudentNotFoundException;
import ru.tele2.govorova.otus.java.pro.student_management.exceptions.VersionConflictException;
import ru.tele2.govorova.otus.java.pro.student_management.service.StudentService;
import ru.tele2.govorova.otus.java.pro.student_management.dto.response.GenerationResponse;

import java.time.LocalDate;


@AllArgsConstructor
@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    @ExceptionHandler(StudentNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<String> handleStudentNotFound(StudentNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @GetMapping
    public ResponseEntity<Page<StudentDTO>> getAllStudents(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate enrollmentDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate enrollmentDateTo,
            @PageableDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {

        return ResponseEntity.ok(studentService.getAllStudents(firstName, lastName, email,
                enrollmentDateFrom, enrollmentDateTo, pageable));
    }

    @PostMapping
    public ResponseEntity<StudentDTO> createStudent(@RequestBody StudentDTO studentDTO) {
        return ResponseEntity.ok(studentService.createStudent(studentDTO));
    }

    @PutMapping("/{studentId}")
    public ResponseEntity<Object> updateStudentOptimisticLock(@PathVariable Long studentId, @RequestBody StudentDTO studentDTO) {
        try {
            StudentDTO updatedStudent = studentService.updateStudent(studentId, studentDTO);
            return ResponseEntity.ok(updatedStudent);
        } catch (StudentNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (VersionConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @GetMapping("/{studentId}")
    public ResponseEntity<StudentDTO> getStudentById(@PathVariable Long studentId) {
        return ResponseEntity.ok(studentService.getStudentById(studentId));
    }

    @PostMapping("/{studentId}/avatar")
    public ResponseEntity<String> uploadAvatar(@PathVariable Long studentId, @RequestParam("file") MultipartFile file) {
        studentService.uploadAvatar(studentId, file);
        return ResponseEntity.ok("{status: Аватарка успешно загружена для студента с id " + studentId + "}");
    }

    @GetMapping(value = "/{studentId}/avatar", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> downloadStudentAvatar(@PathVariable Long studentId) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"avatar_" + studentId + ".jpg\"")
                .body(studentService.getAvatar(studentId));
    }

    @DeleteMapping("/{studentId}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long studentId) {
        studentService.deleteStudent(studentId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/generate/{count}")
    public ResponseEntity<GenerationResponse> generateStudentsJPA(@PathVariable int count) {
        GenerationResponse response = studentService.generateAndSaveStudents(count);
        return ResponseEntity.ok(response);
    }
}
