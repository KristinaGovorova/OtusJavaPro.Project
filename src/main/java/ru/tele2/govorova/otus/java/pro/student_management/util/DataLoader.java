package ru.tele2.govorova.otus.java.pro.student_management.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.tele2.govorova.otus.java.pro.student_management.service.StudentService;


@Component
@Slf4j
public class DataLoader implements CommandLineRunner {

    public DataLoader(StudentService studentService) {}

    @Override
    public void run(String... args) throws Exception {
        log.info("App started!");
    }
}
