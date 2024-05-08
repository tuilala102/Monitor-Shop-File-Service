package com.mshop.fileservice.repository;

import com.mshop.fileservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
