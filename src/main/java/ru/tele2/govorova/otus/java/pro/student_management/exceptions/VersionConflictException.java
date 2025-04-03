package ru.tele2.govorova.otus.java.pro.student_management.exceptions;

public class VersionConflictException extends RuntimeException {
    public VersionConflictException(String message) {
        super(message);
    }
}
