package ru.tele2.govorova.otus.java.pro.student_management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GenerationResponse {
    private String status;
    private long executionTimeMs;
    private long executionTimeSec;
}
