package ru.tele2.govorova.otus.java.pro.student_management.util;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import ru.tele2.govorova.otus.java.pro.student_management.entity.Passport;
import ru.tele2.govorova.otus.java.pro.student_management.entity.Student;
import ru.tele2.govorova.otus.java.pro.student_management.exceptions.StudentValidationException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Component
public class CsvParser {
    public List<Student> parseStudentsFromCsv(MultipartFile file)
            throws IOException, CsvValidationException, StudentValidationException {

        List<Student> students = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            String[] nextLine;
            int lineNumber = 1;

            reader.readNext();

            while ((nextLine = reader.readNext()) != null) {
                lineNumber++;
                try {
                    Student student = parseStudent(nextLine, lineNumber);
                    students.add(student);
                } catch (StudentValidationException e) {
                    errors.add(e.getMessage());
                }
            }
        }

        if (!errors.isEmpty()) {
            throw new StudentValidationException(errors);
        }

        return students;
    }

    private Student parseStudent(String[] row, int lineNumber) throws StudentValidationException {
        if (row.length < 6) {
            throw new StudentValidationException("Строка " + lineNumber + ": Недостаточно данных");
        }

        String email = row[2].trim();
        if (!isValidEmail(email)) {
            throw new StudentValidationException("Строка " + lineNumber + ": Неверный email");
        }

        // Парсинг остальных полей...
        return new Student(
                null,
                row[0].trim(),
                row[1].trim(),
                email,
                parseDate(row[3].trim(), lineNumber, "дата зачисления"),
                null,
                null,
                new Passport(
                        null,
                        row[4].trim(),
                        parseDate(row[5].trim(), lineNumber, "дата выдачи паспорта")
                ),
                null
        );
    }

    private LocalDate parseDate(String dateStr, int lineNumber, String fieldName) throws StudentValidationException {
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        } catch (DateTimeParseException e) {
            throw new StudentValidationException(
                    "Строка " + lineNumber + ": Неверный формат " + fieldName + " (" + dateStr + ")"
            );
        }
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
}
