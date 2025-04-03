package ru.tele2.govorova.otus.java.pro.student_management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.tele2.govorova.otus.java.pro.student_management.entity.Transcript;

public interface TranscriptRepository extends JpaRepository<Transcript, Long> {
}
