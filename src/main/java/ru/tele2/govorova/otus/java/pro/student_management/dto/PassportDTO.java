package ru.tele2.govorova.otus.java.pro.student_management.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PassportDTO {
    private Long id;
    private String passportNumber;
    private LocalDate issueDate;
}
