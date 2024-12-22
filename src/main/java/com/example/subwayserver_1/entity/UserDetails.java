package com.example.subwayserver_1.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user_details") // 매핑된 테이블 이름
public class UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username; // 사용자 아이디

    @Column(nullable = false, unique = true)
    private String email; // 사용자 이메일

    @Column(nullable = false)
    private String password; // 사용자 비밀번호

    @Column(nullable = false)
    private String name; // 사용자 이름

    @Column(nullable = false)
    private String nickname; // 사용자 닉네임

    @Column(name = "is_climate_card_eligible", nullable = false)
    private Boolean isClimateCardEligible; // 기후 동행 카드 사용 가능 여부

    @Column(name = "refresh_token", nullable = true)
    private String refreshToken;

    @Column(name = "profile_picture", nullable = false, columnDefinition = "INT DEFAULT 1")
    private Integer profilePicture = 1; // Java에서 기본값 설정


    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Boolean getIsClimateCardEligible() {
        return isClimateCardEligible;
    }

    public void setIsClimateCardEligible(Boolean isClimateCardEligible) {
        this.isClimateCardEligible = isClimateCardEligible;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Integer getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(Integer profilePicture) {
        this.profilePicture = profilePicture;
    }
}
