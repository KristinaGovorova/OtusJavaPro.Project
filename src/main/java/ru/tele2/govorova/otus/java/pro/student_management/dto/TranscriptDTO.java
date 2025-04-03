package ru.tele2.govorova.otus.java.pro.student_management.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TranscriptDTO {
    private Long id;
    private String subject;
    private int grade;
}
