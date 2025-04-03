package ru.tele2.govorova.otus.java.pro.student_management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@AllArgsConstructor
@Getter
@Setter
public class ApiErrorResponse {
    private String message;
    private int status;
}
