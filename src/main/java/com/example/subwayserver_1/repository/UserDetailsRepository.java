/*package com.example.test.repository;

import com.example.test.entity.UserDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserDetailsRepository extends JpaRepository<UserDetails, Long> {
    Optional<UserDetails> findByUsername(String username);
    Optional<UserDetails> findByEmail(String email);
    Optional<UserDetails> findByNickname(String nickname);
    Optional<UserDetails> findByNameAndEmail(String name, String email);
   // Optional<UserDetails> findByUsernameAndEmail(String username, String email);

}*/
package com.example.subwayserver_1.repository;

import com.example.subwayserver_1.entity.UserDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserDetailsRepository extends JpaRepository<UserDetails, Long> {
    Optional<UserDetails> findByUsername(String username); // username으로 조회
    Optional<UserDetails> findByEmail(String email); // email로 조회
    Optional<UserDetails> findByNickname(String nickname); // nickname으로 조회
    Optional<UserDetails> findByNameAndEmail(String name, String email); // name과 email로 조회

}
