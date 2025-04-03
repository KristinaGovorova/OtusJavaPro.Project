package ru.tele2.govorova.otus.java.pro.student_management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.tele2.govorova.otus.java.pro.student_management.entity.Passport;

public interface PassportRepository extends JpaRepository<Passport, Long> {
}
