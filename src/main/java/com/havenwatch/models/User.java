package com.havenwatch.models;

import java.time.LocalDateTime;

public class User {
    private int userId;
    private String username;
    private String password;
    private UserRole role;
    private String fullName;
    private String email;
    private String phone;
    private LocalDateTime createdAt;

    public enum UserRole {
        ADMIN, CAREGIVER, FAMILY, HEALTHCARE
    }

    // Default constructor
    public User() {
    }

    // Constructor with all fields except userId and createdAt
    public User(String username, String password, UserRole role, String fullName, String email, String phone) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
    }

    // Full constructor
    public User(int userId, String username, String password, UserRole role, String fullName, String email, String phone, LocalDateTime createdAt) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "User [userId=" + userId + ", username=" + username + ", role=" + role + ", fullName=" + fullName + "]";
    }
}