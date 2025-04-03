package ru.tele2.govorova.otus.java.pro.student_management.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity(name = "passport")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Passport {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "passport_seq")
    @SequenceGenerator(name = "passport_seq", sequenceName = "passport_seq")
    private Long id;

    @Column(nullable = false)
    private String passportNumber;

    @Column(nullable = false)
    private LocalDate issueDate;
}
