package com.example.crossword.controller;

import java.time.LocalDate;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.crossword.dto.DailyPuzzleRequest;
import com.example.crossword.dto.PuzzleRequest;
import com.example.crossword.dto.PuzzleResponse;
import com.example.crossword.model.Puzzle;
import com.example.crossword.service.PuzzleService;

@RestController
@RequestMapping("/api/puzzles")
public class PuzzleController {
    private final PuzzleService puzzleService;

    public PuzzleController(PuzzleService puzzleService) {
        this.puzzleService = puzzleService;
    }

    @PostMapping("/generate")
    public ResponseEntity<PuzzleResponse> generatePuzzle(@RequestBody PuzzleRequest request) {
        try {
            Puzzle puzzle = puzzleService.generatePuzzle(request.getTheme(), request.isRegenerate(), request.getMaxWords(), request.getMaxAttempts(), request.getThemeWords(), request.getWordTokens(), request.getHintTokens());
            return ResponseEntity.ok(PuzzleResponse.fromEntity(puzzle));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/daily")
    public ResponseEntity<PuzzleResponse> generateDailyPuzzle(@RequestBody(required = false) DailyPuzzleRequest request) {
        try {
            DailyPuzzleRequest dailyRequest = request == null ? new DailyPuzzleRequest() : request;
            Puzzle puzzle = puzzleService.generateDailyPuzzle(
                dailyRequest.getGeneratedFor(),
                dailyRequest.getTheme(),
                dailyRequest.isForce(),
                dailyRequest.isRegenerate(),
                dailyRequest.getMaxWords(),
                dailyRequest.getMaxAttempts(),
                dailyRequest.getThemeWords(),
                dailyRequest.getWordTokens(),
                dailyRequest.getHintTokens()
            );
            return ResponseEntity.ok(PuzzleResponse.fromEntity(puzzle));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/today")
    public ResponseEntity<PuzzleResponse> getTodayPuzzle() {
        Puzzle puzzle = puzzleService.getTodayPuzzle();
        return puzzle == null
            ? ResponseEntity.notFound().build()
            : ResponseEntity.ok(PuzzleResponse.fromEntity(puzzle));
    }

    @GetMapping("/by-date")
    public ResponseEntity<PuzzleResponse> getPuzzleByDate(@RequestParam LocalDate date) {
        Puzzle puzzle = puzzleService.getPuzzleByDate(date);
        return puzzle == null
            ? ResponseEntity.notFound().build()
            : ResponseEntity.ok(PuzzleResponse.fromEntity(puzzle));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PuzzleResponse> getPuzzle(@PathVariable Long id) {
        Puzzle puzzle = puzzleService.getPuzzleById(id);
        return puzzle == null
            ? ResponseEntity.notFound().build()
            : ResponseEntity.ok(PuzzleResponse.fromEntity(puzzle));
    }
}
