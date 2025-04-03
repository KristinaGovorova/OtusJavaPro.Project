package ru.tele2.govorova.otus.java.pro.student_management.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;
import ru.tele2.govorova.otus.java.pro.student_management.dto.StudentDTO;
import ru.tele2.govorova.otus.java.pro.student_management.dto.TranscriptDTO;
import ru.tele2.govorova.otus.java.pro.student_management.dto.mapper.PassportMapper;
import ru.tele2.govorova.otus.java.pro.student_management.dto.mapper.StudentMapper;
import ru.tele2.govorova.otus.java.pro.student_management.dto.mapper.TranscriptMapper;
import ru.tele2.govorova.otus.java.pro.student_management.entity.Passport;
import ru.tele2.govorova.otus.java.pro.student_management.entity.Student;
import ru.tele2.govorova.otus.java.pro.student_management.entity.Transcript;
import ru.tele2.govorova.otus.java.pro.student_management.exceptions.StudentNotFoundException;
import ru.tele2.govorova.otus.java.pro.student_management.exceptions.VersionConflictException;
import ru.tele2.govorova.otus.java.pro.student_management.repository.StudentRepository;
import ru.tele2.govorova.otus.java.pro.student_management.dto.response.GenerationResponse;
import ru.tele2.govorova.otus.java.pro.student_management.util.StudentGenerator;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentServiceImpl implements StudentService {

    @Value("${multipart.max-file-size}")
    private DataSize maxFileSize;

    private final StudentMapper studentMapper;
    private final PassportMapper passportMapper;
    private final TranscriptMapper transcriptMapper;

    private final StudentRepository studentRepository;
    private final StudentGenerator studentGenerator;

    @Override
    public Page<StudentDTO> getAllStudents(
            String firstName, String lastName, String email,
            LocalDate enrollmentDateFrom, LocalDate enrollmentDateTo,
            Pageable pageable) {

        Page<Student> students = studentRepository.findAllWithFilters(firstName,
                lastName,
                email,
                enrollmentDateFrom,
                enrollmentDateTo,
                pageable);

        return students.map(studentMapper::toDto);
    }

    @Override
    @Transactional
    public List<StudentDTO> getAllStudents() {
        return studentRepository.findAll()
                .stream()
                .map(studentMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public StudentDTO createStudent(StudentDTO studentDTO) {
        Student student = new Student();
        student.setFirstName(studentDTO.getFirstName());
        student.setLastName(studentDTO.getLastName());
        student.setEmail(studentDTO.getEmail());
        student.setEnrollmentDate(studentDTO.getEnrollmentDate());

        Passport passport = new Passport();
        passport.setPassportNumber(studentDTO.getPassport().getPassportNumber());
        passport.setIssueDate(studentDTO.getPassport().getIssueDate());
        student.setPassport(passport);

        List<Transcript> transcripts = new ArrayList<>();
        for (TranscriptDTO transcriptDTO : studentDTO.getTranscripts()) {
            Transcript transcript = new Transcript();
            transcript.setSubject(transcriptDTO.getSubject());
            transcript.setGrade(transcriptDTO.getGrade());
            transcript.setStudent(student);
            transcripts.add(transcript);
        }
        student.setTranscripts(transcripts);

        Student savedStudent = studentRepository.save(student);
        return studentMapper.toDto(savedStudent);
    }

    @Transactional
    public void uploadAvatar(Long studentId, MultipartFile file) {
        if (file.getSize() > maxFileSize.toBytes()) {
            throw new IllegalArgumentException("Файл превышает допустимый размер");
        }
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Студент с id " + studentId + " не найден"));
        try {
            student.setAvatar(file.getBytes());
            studentRepository.save(student);
        } catch (IOException e) {
            throw new UncheckedIOException("Ошибка при загрузке аватарки", e);
        }
    }

    @Transactional(readOnly = true)
    public byte[] getAvatar(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Студент с id " + studentId + " не найден"));

        byte[] avatar = student.getAvatar();
        if (avatar == null || avatar.length == 0) {
            throw new EntityNotFoundException("Аватарка для студента с id " + studentId + " не найдена");
        }
        return avatar;
    }

    @Override
    @Transactional
    public StudentDTO updateStudent(Long studentId, StudentDTO studentDTO) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException(studentId));

        if (student.getVersion() != null && !student.getVersion().equals(studentDTO.getVersion())) {
            throw new VersionConflictException(String.format(
                    "Version conflict. Current: %s, Received: %s",
                    student.getVersion(), studentDTO.getVersion()));
        }

        studentMapper.updateStudentFromDto(studentDTO, student);

        Optional.ofNullable(studentDTO.getPassport())
                .ifPresent(passportDTO -> {
                    Passport passport = Optional.ofNullable(student.getPassport())
                            .orElseGet(() -> {
                                Passport newPassport = new Passport();
                                student.setPassport(newPassport);
                                return newPassport;
                            });
                    passportMapper.updatePassportFromDto(passportDTO, passport);
                });

        Optional.ofNullable(studentDTO.getTranscripts())
                .ifPresent(transcripts -> {
                    student.getTranscripts().clear();

                    transcripts.stream()
                            .map(dto -> {
                                Transcript transcript = transcriptMapper.toEntity(dto);
                                transcript.setStudent(student);
                                return transcript;
                            })
                            .forEach(student::addTranscript);
                });

        try {
            return studentMapper.toDto(studentRepository.save(student));
        } catch (OptimisticLockException e) {
            throw new VersionConflictException(String.format(
                    "Optimistic lock error. Current version: %s, Received version: %s",
                    ((Student) e.getEntity()).getVersion(), studentDTO.getVersion()));
        }
    }

    @Override
    @Transactional
    public StudentDTO getStudentById(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException(studentId));
        return studentMapper.toDto(student);
    }

    @Override
    @Transactional
    public void deleteStudent(Long studentId) {
        studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException(studentId));

        studentRepository.deleteById(studentId);
    }

    @Transactional
    public GenerationResponse generateAndSaveStudents(int count) {
        log.info("Начинается генерация и сохранение {} студентов...", count);
        long startGenerationTime = System.currentTimeMillis();
        List<Student> students = studentGenerator.generateStudents(count);
        long finishGenerationTime = System.currentTimeMillis();
        long generationTimeMs = finishGenerationTime - startGenerationTime;
        log.info("Время генерации: {} мс", generationTimeMs);

        long startSavingTime = System.currentTimeMillis();
        studentRepository.saveAllAndFlush(students);
        long finishSavingTime = System.currentTimeMillis();
        long savingTimeMs = finishSavingTime - startSavingTime;
        log.info("Время сохранения: {} мс", savingTimeMs);

        long totalTimeMs = (finishSavingTime - startGenerationTime);
        log.info("Общее время: {} мс", totalTimeMs);

        return new GenerationResponse("Generated " + count + " students", totalTimeMs, msToSeconds(totalTimeMs));

    }

    private long msToSeconds(long milliseconds) {
        return milliseconds / 1000;
    }
}
