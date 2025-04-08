package ru.tele2.govorova.otus.java.pro.student_management.exceptions;

import lombok.Getter;

import java.util.List;

@Getter
public class StudentValidationException extends RuntimeException {
    private final List<String> errors;

    public StudentValidationException(List<String> errors) {
        this.errors = errors;
    }

    public StudentValidationException(String error) {
        this(List.of(error));
    }

}
