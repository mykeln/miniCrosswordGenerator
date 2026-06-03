package com.example.crossword.service;

import java.time.Clock;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.crossword.model.Puzzle;
import com.example.crossword.repository.PuzzleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class PuzzleService {

    private final PuzzleRepository puzzleRepository;
    private final PythonPuzzleClient pythonPuzzleClient;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Clock clock;

    @Value("${app.daily.default-theme:daily mini crossword}")
    private String defaultDailyTheme;

    public PuzzleService(PuzzleRepository puzzleRepository,
                         PythonPuzzleClient pythonPuzzleClient,
                         Clock clock) {
        this.puzzleRepository = puzzleRepository;
        this.pythonPuzzleClient = pythonPuzzleClient;
        this.clock = clock;
    }

    public Puzzle generatePuzzle(String theme, boolean regenerate, int maxWords, int maxAttempts, int themeWords, int wordTokens, int hintTokens) {
        return generateAndSavePuzzle(null, theme, regenerate, maxWords, maxAttempts, themeWords, wordTokens, hintTokens, null);
    }

    public Puzzle generateDailyPuzzle(LocalDate generatedFor, String theme, boolean force, boolean regenerate, int maxWords, int maxAttempts, int themeWords, int wordTokens, int hintTokens) {
        LocalDate puzzleDate = generatedFor == null ? LocalDate.now(clock) : generatedFor;
        Puzzle existingPuzzle = puzzleRepository.findByGeneratedFor(puzzleDate).orElse(null);
        if (existingPuzzle != null && !force) {
            return existingPuzzle;
        }

        String dailyTheme = (theme == null || theme.isBlank()) ? defaultDailyTheme : theme.trim();
        return generateAndSavePuzzle(puzzleDate, dailyTheme, regenerate, maxWords, maxAttempts, themeWords, wordTokens, hintTokens, existingPuzzle);
    }

    public Puzzle getTodayPuzzle() {
        return getPuzzleByDate(LocalDate.now(clock));
    }

    public Puzzle getPuzzleByDate(LocalDate generatedFor) {
        return puzzleRepository.findByGeneratedFor(generatedFor).orElse(null);
    }

    private Puzzle generateAndSavePuzzle(LocalDate generatedFor, String theme, boolean regenerate, int maxWords, int maxAttempts, int themeWords, int wordTokens, int hintTokens, Puzzle existingPuzzle) {
        // Request puzzle data from python client
        GeneratedPuzzleData data = pythonPuzzleClient.requestPuzzle(theme, regenerate, maxWords, maxAttempts, themeWords, wordTokens, hintTokens);

        if (!data.isSuccess()) {
            throw new RuntimeException("Failed to generate puzzle");
        }

        // Build a Puzzle entity
        Puzzle puzzle = existingPuzzle == null ? new Puzzle() : existingPuzzle;
        puzzle.setTheme(theme);
        puzzle.setGeneratedFor(generatedFor);
        try {
            puzzle.setSolutionJson(mapper.writeValueAsString(data.getSolution()));
            puzzle.setHintsJson(mapper.writeValueAsString(data.getHints()));
        } catch (Exception e) {
            throw new RuntimeException("Error converting puzzle data to JSON", e);
        }

        // Save to database (not included in this version of source code)
        return puzzleRepository.save(puzzle);
    }

    // Get a puzzle by id (not included in this version of source code)
    public Puzzle getPuzzleById(Long id) {
        return puzzleRepository.findById(id).orElse(null);
    }
}
