package com.example.foxticket.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "verifications")
public class Verification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private boolean isVerified;
    private LocalDateTime createdDate;
    private String verificationCode;
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH})
    @JsonManagedReference
    private User user;

    public Verification() {
        this.createdDate = LocalDateTime.now();
        this.verificationCode = UUID.randomUUID().toString();
        this.isVerified = false;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public String getVerificationCode() {
        return verificationCode;
    }
}
