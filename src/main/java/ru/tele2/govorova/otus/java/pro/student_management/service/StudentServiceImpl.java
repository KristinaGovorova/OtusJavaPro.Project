package ru.tele2.govorova.otus.java.pro.student_management.service;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.opencsv.exceptions.CsvValidationException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
import ru.tele2.govorova.otus.java.pro.student_management.dto.response.CsvUploadResponse;
import ru.tele2.govorova.otus.java.pro.student_management.entity.Passport;
import ru.tele2.govorova.otus.java.pro.student_management.entity.Student;
import ru.tele2.govorova.otus.java.pro.student_management.entity.Transcript;
import ru.tele2.govorova.otus.java.pro.student_management.exceptions.StudentNotFoundException;
import ru.tele2.govorova.otus.java.pro.student_management.exceptions.StudentValidationException;
import ru.tele2.govorova.otus.java.pro.student_management.exceptions.VersionConflictException;
import ru.tele2.govorova.otus.java.pro.student_management.repository.StudentRepository;
import ru.tele2.govorova.otus.java.pro.student_management.dto.response.GenerationResponse;
import ru.tele2.govorova.otus.java.pro.student_management.util.CsvParser;
import ru.tele2.govorova.otus.java.pro.student_management.util.StudentGenerator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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

    private final CsvParser csvParser;

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
    @Override
    public byte[] exportStudentsToExcel(String firstName, String lastName, String email, String passportNumber,
                                        LocalDate enrollmentDateFrom, LocalDate enrollmentDateTo, Pageable pageable) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Students");

            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(
                    workbook.getCreationHelper().createDataFormat().getFormat("dd.MM.yyyy")
            );

            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("ID");
            headerRow.createCell(1).setCellValue("Имя");
            headerRow.createCell(2).setCellValue("Фамилия");
            headerRow.createCell(3).setCellValue("Email");
            headerRow.createCell(4).setCellValue("Дата поступления");
            headerRow.createCell(5).setCellValue("Номер паспорта");
            headerRow.createCell(6).setCellValue("Количество оценок");

            int rowNum = 1;
            Page<Student> students = studentRepository.findAllWithFilters(firstName,
                    lastName,
                    email,
                    enrollmentDateFrom,
                    enrollmentDateTo,
                    pageable);
            for (Student student : students) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(student.getId());
                row.createCell(1).setCellValue(student.getFirstName());
                row.createCell(2).setCellValue(student.getLastName());
                row.createCell(3).setCellValue(student.getEmail());
                Cell dateCell = row.createCell(4);
                dateCell.setCellValue(student.getEnrollmentDate());
                dateCell.setCellStyle(dateStyle);
                row.createCell(5).setCellValue(student.getPassport().getPassportNumber());
                row.createCell(6).setCellValue(student.getTranscripts().size());
            }

            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при экспорте в Excel", e);
        }
    }

    @Transactional
    @Override
    public byte[] exportStudentsToPDF(String firstName, String lastName, String email, String passportNumber,
                                      LocalDate enrollmentDateFrom, LocalDate enrollmentDateTo, Pageable pageable) {
        try(Document document = new Document()) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            InputStream fontStream = getClass().getResourceAsStream("/fonts/times.ttf");
            if (fontStream == null) {
                throw new IOException("Файл шрифта не найден!");
            }

            BaseFont baseFont = BaseFont.createFont(
                    "times.ttf",
                    BaseFont.IDENTITY_H,
                    BaseFont.EMBEDDED,
                    true,
                    fontStream.readAllBytes(),
                    null
            );

            PdfWriter.getInstance(document, baos);
            document.open();

            com.lowagie.text.Font titleFont = new com.lowagie.text.Font(baseFont, 18);
            document.add(new Paragraph("Отчёт по студентам", titleFont));


            com.lowagie.text.Font subtitleFont = new com.lowagie.text.Font(baseFont, 14);
            document.add(new Paragraph("Автоматически сгенерировано " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) + " в " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")), subtitleFont));

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            float[] columnWidths = {10f, 15f, 15f, 30f, 15f, 15f, 10f};
            table.setWidths(columnWidths);
            table.setSpacingBefore(20f);
            table.setSpacingAfter(20f);

            com.lowagie.text.Font headerFont = new com.lowagie.text.Font(baseFont, 14, com.lowagie.text.Font.BOLD);
            table.addCell(new Phrase("ID", headerFont));
            table.addCell(new Phrase("Имя", headerFont));
            table.addCell(new Phrase("Фамилия", headerFont));
            table.addCell(new Phrase("Email", headerFont));
            table.addCell(new Phrase("Дата зачисления", headerFont));
            table.addCell(new Phrase("Паспорт", headerFont));
            table.addCell(new Phrase("Кол-во оценок", headerFont));

            com.lowagie.text.Font dataFont = new com.lowagie.text.Font(baseFont, 12);
            Page<Student> students = studentRepository.findAllWithFilters(firstName,
                    lastName,
                    email,
                    enrollmentDateFrom,
                    enrollmentDateTo,
                    pageable);

            for (Student student : students) {
                table.addCell(new Phrase(String.valueOf(student.getId()), dataFont));
                table.addCell(new Phrase(student.getFirstName(), dataFont));
                table.addCell(new Phrase(student.getLastName(), dataFont));
                table.addCell(new Phrase(student.getEmail(), dataFont));
                table.addCell(new Phrase(student.getEnrollmentDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")), dataFont));
                table.addCell(new Phrase(student.getPassport().getPassportNumber(), dataFont));
                table.addCell(new Phrase(String.valueOf(student.getTranscripts().size()), dataFont));
            }

            document.add(table);
            document.close();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при экспорте в PDF", e);
        }
    }

    @Transactional
    @Override
    public CsvUploadResponse processStudentUpload(MultipartFile file)
            throws IOException, CsvValidationException, StudentValidationException {

        List<Student> students = csvParser.parseStudentsFromCsv(file);
        List<Student> savedStudents = studentRepository.saveAll(students);

        return new CsvUploadResponse(savedStudents.size(), null);
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
