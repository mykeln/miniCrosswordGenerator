package com.example.crossword.dto;

import java.time.LocalDate;

public class DailyPuzzleRequest extends PuzzleRequest {
    private LocalDate generatedFor;
    private boolean force;

    public LocalDate getGeneratedFor() {
        return generatedFor;
    }

    public void setGeneratedFor(LocalDate generatedFor) {
        this.generatedFor = generatedFor;
    }

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }
}
