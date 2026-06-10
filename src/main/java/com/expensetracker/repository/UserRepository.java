package com.expensetracker.repository;

import com.expensetracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {

    User findByEmailAndPassword(String email, String password);

    User findByEmail(String email);

    boolean existsByEmail(String email);
}