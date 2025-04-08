package ru.tele2.govorova.otus.java.pro.student_management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CsvUploadResponse {
    private int savedCount;
    private List<String> warnings;
}
