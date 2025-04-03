package ru.tele2.govorova.otus.java.pro.student_management.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "transcript")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Transcript {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "transcript_seq")
    @SequenceGenerator(name = "transcript_seq", sequenceName = "transcript_seq")
    private Long id;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false)
    private int grade;

    @ManyToOne(optional = false)
    private Student student;

}
