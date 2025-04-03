package ru.tele2.govorova.otus.java.pro.student_management.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.tele2.govorova.otus.java.pro.student_management.dto.mapper.PassportMapper;
import ru.tele2.govorova.otus.java.pro.student_management.dto.mapper.StudentMapper;
import ru.tele2.govorova.otus.java.pro.student_management.dto.mapper.TranscriptMapper;
import ru.tele2.govorova.otus.java.pro.student_management.entity.Passport;
import ru.tele2.govorova.otus.java.pro.student_management.entity.Student;
import ru.tele2.govorova.otus.java.pro.student_management.entity.Transcript;
import ru.tele2.govorova.otus.java.pro.student_management.repository.StudentRepository;
import ru.tele2.govorova.otus.java.pro.student_management.service.StudentService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractIntegrationTest {

    @Autowired
    protected StudentMapper studentMapper;

    @Autowired
    protected PassportMapper passportMapper;

    @Autowired
    protected TranscriptMapper transcriptMapper;

    @Autowired
    protected StudentRepository studentRepository;

    @Autowired
    protected StudentService studentService;

    @Container
    @ServiceConnection
    public static PostgreSQLContainer<?> postgresContainer =
            new PostgreSQLContainer<>("postgres:17-alpine");

    @BeforeAll
    public static void setUp() {
        postgresContainer.start();
        if (postgresContainer.isRunning()) {
            log.info("PostgreSQL контейнер запущен и доступен по адресу: {}", postgresContainer.getJdbcUrl());
        }
    }

    @AfterAll
    public static void tearDown() {
        postgresContainer.stop();
        if (!postgresContainer.isRunning()) {
            log.info("PostgreSQL контейнер остановлен.");
        }
    }

    @BeforeEach
    public void populateDatabase() {
        Student student = createTestStudent();
        studentRepository.save(student);
        studentRepository.flush();
        log.info("Saved student: {}", student.getLastName());
        if (studentRepository.count() > 0) {
            log.info("База данных успешно заполнена.");
        }
    }

    @AfterEach
    public void clearDatabase() {
        studentRepository.deleteAll();
        log.info("База данных успешно очищена.");
    }

    protected Student createTestStudent() {
        Student student = new Student();
        student.setFirstName("Иван");
        student.setLastName("Тестович");
        student.setEmail("ivan.testovich@mail.ru");
        student.setEnrollmentDate(LocalDate.of(2023, 9, 1));

        Passport passport = new Passport();
        passport.setPassportNumber("CD987654");
        passport.setIssueDate(LocalDate.of(2021, 1, 15));
        student.setPassport(passport);

        List<Transcript> transcripts = new ArrayList<>();
        transcripts.add(createTranscript("Math", 3, student));
        transcripts.add(createTranscript("Physics", 4, student));
        transcripts.add(createTranscript("Literature", 5, student));
        student.setTranscripts(transcripts);

        return student;
    }

    private Transcript createTranscript(String subject, int grade, Student student) {
        Transcript transcript = new Transcript();
        transcript.setSubject(subject);
        transcript.setGrade(grade);
        transcript.setStudent(student);
        return transcript;
    }

    protected Student getFreshStudentFromDb() {
        return studentRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Test student not found in DB"));
    }
}