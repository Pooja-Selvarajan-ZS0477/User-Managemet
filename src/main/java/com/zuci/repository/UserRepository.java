package com.zuci.repository;

import com.zuci.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    User findByUsername(String username);
    User findByOtp(String otp);


}

