package ru.tele2.govorova.otus.java.pro.student_management.exceptions;

public class StudentNotFoundException extends RuntimeException {

    public StudentNotFoundException(String message) {
        super(message);
    }

    public StudentNotFoundException(Long id) {
        super("Student not found with id: " + id);
    }
}
