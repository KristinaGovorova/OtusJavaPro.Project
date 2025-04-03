package ru.tele2.govorova.otus.java.pro.student_management.util;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.tele2.govorova.otus.java.pro.student_management.entity.Passport;
import ru.tele2.govorova.otus.java.pro.student_management.entity.Student;
import ru.tele2.govorova.otus.java.pro.student_management.entity.Transcript;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.time.LocalDate;
import java.util.stream.Collectors;


@Component
public class StudentGenerator {

    List<String> firstNames = loadNamesFromResource("first_names.txt");
    List<String> lastNames = loadNamesFromResource("last_names.txt");

    private static List<String> loadNamesFromResource(String resourceName) {
        try (InputStream inputStream = StudentGenerator.class.getResourceAsStream("/" + resourceName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            return reader.lines()
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    String randomFirstName() {
        return firstNames.get((int) (Math.random() * firstNames.size()));
    }

    String randomLastName() {
        return lastNames.get((int) (Math.random() * lastNames.size()));
    }

    private static String generateEmail(String firstName, String lastName) {
        String formattedFirstName = TextUtil.transliterate(firstName).toLowerCase();
        String formattedLastName = TextUtil.transliterate(lastName).toLowerCase();
        return formattedFirstName + "." + formattedLastName + "@mail.ru";
    }

    private LocalDate generateEnrollmentDate() {
        Random random = new Random();
        int year = LocalDate.now().getYear() - random.nextInt(3);
        int month = random.nextInt(12) + 1;
        int day = random.nextInt(28) + 1;
        return LocalDate.of(year, month, day);
    }

    private String generatePassportNumber() {
        Random random = new Random();
        char firstLetter = (char) (random.nextInt(26) + 'A');
        char secondLetter = (char) (random.nextInt(26) + 'A');
        int numberPart = random.nextInt(900000) + 10000;
        return String.format("%c%c%d", firstLetter, secondLetter, numberPart);
    }

    private LocalDate generateIssueDate() {
        Random random = new Random();
        int year = LocalDate.now().getYear() - random.nextInt(10);
        int month = random.nextInt(12) + 1;
        int day = random.nextInt(28) + 1;
        return LocalDate.of(year, month, day);
    }

    private List<Transcript> generateTranscripts() {
        List<String> subjects = Arrays.asList("Math", "Physics", "Chemistry", "Biology", "History", "Geography", "Literature", "Foreign Language", "Computer Science");
        Random random = new Random();
        List<Transcript> transcripts = new ArrayList<>();

        Collections.shuffle(subjects);
        for (int i = 0; i < 3 + random.nextInt(subjects.size() - 3); i++) {
            Transcript transcript = new Transcript();
            transcript.setSubject(subjects.get(i));
            transcript.setGrade(3 + random.nextInt(3));
            transcripts.add(transcript);
        }

        return transcripts;
    }


    public Student generateStudent() {
        Student student = new Student();
        student.setFirstName(randomFirstName());
        student.setLastName(randomLastName());
        student.setEmail(generateEmail(student.getFirstName(), student.getLastName()));
        student.setEnrollmentDate(generateEnrollmentDate());

        Passport passport = new Passport();
        passport.setPassportNumber(generatePassportNumber());
        passport.setIssueDate(generateIssueDate());
        student.setPassport(passport);

        List<Transcript> transcripts = generateTranscripts();
        for (Transcript transcript : transcripts) {
            student.addTranscript(transcript);
        }

        return student;
    }

    @Transactional
    public List<Student> generateStudents(int count) {
        List<Student> students = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            students.add(generateStudent());
        }
        return students;
    }
}
