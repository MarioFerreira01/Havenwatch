package com.havenwatch.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Resident {
    private int residentId;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String address;
    private String emergencyContact;
    private String emergencyPhone;
    private String medicalConditions;
    private String medications;
    private String allergies;
    private LocalDateTime createdAt;

    public enum Gender {
        MALE, FEMALE, OTHER
    }

    // Default constructor
    public Resident() {
    }

    // Constructor with essential fields
    public Resident(String firstName, String lastName, LocalDate dateOfBirth, Gender gender) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
    }

    // Full constructor
    public Resident(int residentId, String firstName, String lastName, LocalDate dateOfBirth, Gender gender,
                    String address, String emergencyContact, String emergencyPhone,
                    String medicalConditions, String medications, String allergies, LocalDateTime createdAt) {
        this.residentId = residentId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.address = address;
        this.emergencyContact = emergencyContact;
        this.emergencyPhone = emergencyPhone;
        this.medicalConditions = medicalConditions;
        this.medications = medications;
        this.allergies = allergies;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getResidentId() {
        return residentId;
    }

    public void setResidentId(int residentId) {
        this.residentId = residentId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    public String getEmergencyPhone() {
        return emergencyPhone;
    }

    public void setEmergencyPhone(String emergencyPhone) {
        this.emergencyPhone = emergencyPhone;
    }

    public String getMedicalConditions() {
        return medicalConditions;
    }

    public void setMedicalConditions(String medicalConditions) {
        this.medicalConditions = medicalConditions;
    }

    public String getMedications() {
        return medications;
    }

    public void setMedications(String medications) {
        this.medications = medications;
    }

    public String getAllergies() {
        return allergies;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Resident [residentId=" + residentId + ", fullName=" + getFullName() + ", dateOfBirth=" + dateOfBirth + "]";
    }
}