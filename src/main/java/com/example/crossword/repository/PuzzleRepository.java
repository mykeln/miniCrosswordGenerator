package com.example.crossword.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.crossword.model.Puzzle;

@Repository
public interface PuzzleRepository extends JpaRepository<Puzzle, Long> {
    Optional<Puzzle> findByGeneratedFor(LocalDate generatedFor);
}
