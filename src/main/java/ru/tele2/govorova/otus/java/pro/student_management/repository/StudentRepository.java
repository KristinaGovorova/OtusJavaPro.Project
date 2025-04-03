package ru.tele2.govorova.otus.java.pro.student_management.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.tele2.govorova.otus.java.pro.student_management.entity.Student;

import java.time.LocalDate;
import java.util.Optional;


public interface StudentRepository extends JpaRepository<Student, Long> {

    @Query("""
                SELECT DISTINCT s FROM student s
                            LEFT JOIN FETCH s.passport p
                            LEFT JOIN FETCH s.transcripts t
                WHERE
                    (LOWER(s.firstName) LIKE LOWER(CONCAT('%', :firstName, '%')) OR :firstName IS NULL)
                            AND (LOWER(s.lastName) LIKE LOWER(CONCAT('%', :lastName, '%')) OR :lastName IS NULL)
                            AND (LOWER(s.email) LIKE LOWER(CONCAT('%', :email, '%')) OR :email IS NULL)
                            AND (s.enrollmentDate >= :enrollmentDateFrom OR :enrollmentDateFrom IS NULL)
                            AND (s.enrollmentDate <= :enrollmentDateTo OR :enrollmentDateTo IS NULL)
            """)
    Page<Student> findAllWithFilters(
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("email") String email,
            @Param("enrollmentDateFrom") LocalDate enrollmentDateFrom,
            @Param("enrollmentDateTo") LocalDate enrollmentDateTo,
            Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM student s WHERE s.id = :studentId")
    Optional<Student> findByIdForUpdate(@Param("studentId") Long studentId);
}
