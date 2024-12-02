package com.example.subwayserver_1.service;

import com.example.subwayserver_1.entity.UserDetails;
import com.example.subwayserver_1.repository.UserDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserDetailsRepository userDetailsRepository;

    public String registerUser(UserDetails userDetails) {
        // 중복 체크
        if (userDetailsRepository.findByUsername(userDetails.getUsername()).isPresent()) {
            return "Username already exists";
        }
        if (userDetailsRepository.findByEmail(userDetails.getEmail()).isPresent()) {
            return "Email already exists";
        }

        // 저장
        userDetailsRepository.save(userDetails);
        return "Registration successful";
    }
}
