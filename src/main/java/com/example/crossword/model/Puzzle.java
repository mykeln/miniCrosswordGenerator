package com.example.crossword.model;

import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

@Entity
public class Puzzle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String theme;

    @Column(unique = true)
    private LocalDate generatedFor;

    @Lob
    private String solutionJson;

    @Lob
    private String hintsJson;

    private Instant createdAt;

    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    // Constructors, getters, and setters
    public Long getId() {
        return id;
    }
    public String getTheme() {
        return theme;
    }
    public LocalDate getGeneratedFor() {
        return generatedFor;
    }
    public String getSolutionJson() {
        return solutionJson;
    }
    public String getHintsJson() {
        return hintsJson;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public void setGeneratedFor(LocalDate generatedFor) {
        this.generatedFor = generatedFor;
    }

    public void setSolutionJson(String solutionJson) {
        this.solutionJson = solutionJson;
    }

    public void setHintsJson(String hintsJson) {
        this.hintsJson = hintsJson;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
