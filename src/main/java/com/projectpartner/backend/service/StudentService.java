package com.projectpartner.backend.service;

import com.projectpartner.backend.entity.Student;
import com.projectpartner.backend.repository.StudentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class StudentService {
    private final StudentRepository repository;

    public StudentService(StudentRepository repository) {
        this.repository = repository;
    }

    public Student registerStudent(Student student) {
        String email = student.getEmail().trim().toLowerCase();
        if (repository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This email is already registered.");
        }
        student.setEmail(email);
        student.setName(student.getName().trim());
        student.setSkills(student.getSkills().trim());
        if (student.getInterests() != null) {
            student.setInterests(student.getInterests().trim());
        }
        return repository.save(student);
    }

    public List<Student> getAllStudents() {
        return repository.findAll();
    }

    public Student getStudentById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found."));
    }
}
