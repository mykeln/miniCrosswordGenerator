package com.example.crossword.dto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.example.crossword.model.Puzzle;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PuzzleResponse {
    private String schemaVersion = "mini-crossword.v1";
    private Long id;
    private String theme;
    private int size;
    private List<String> rows;
    private List<List<Cell>> grid;
    private Map<String, List<Entry>> entries;
    private String gridJson;
    private String cluesJson;

    private static final ObjectMapper mapper = new ObjectMapper();

    public static PuzzleResponse fromEntity(Puzzle puzzle) {
        PuzzleResponse response = new PuzzleResponse();
        response.id = puzzle.getId();
        response.theme = puzzle.getTheme();
        try {
            List<String> solution = mapper.readValue(puzzle.getSolutionJson(), new TypeReference<List<String>>() {});
            List<String> hints = mapper.readValue(puzzle.getHintsJson(), new TypeReference<List<String>>() {});

            response.size = solution.size();
            response.rows = solution;
            response.grid = buildGrid(solution);
            response.entries = buildEntries(solution, hints);

            // Legacy fields used by the current frontend.
            response.gridJson = mapper.writeValueAsString(solution);
            response.cluesJson = mapper.writeValueAsString(buildLegacyClues(hints));

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing JSON from database", e);
        }
        return response;
    }

    private static List<List<Cell>> buildGrid(List<String> solution) {
        List<List<Cell>> grid = new ArrayList<>();
        for (int row = 0; row < solution.size(); row++) {
            List<Cell> rowCells = new ArrayList<>();
            String answerRow = solution.get(row);
            for (int column = 0; column < answerRow.length(); column++) {
                rowCells.add(new Cell(row, column, String.valueOf(answerRow.charAt(column)), clueNumberForCell(row, column), startsForCell(row, column)));
            }
            grid.add(rowCells);
        }
        return grid;
    }

    private static Map<String, List<Entry>> buildEntries(List<String> solution, List<String> hints) {
        int size = solution.size();
        Map<String, List<Entry>> entries = new LinkedHashMap<>();
        List<Entry> across = new ArrayList<>();
        List<Entry> down = new ArrayList<>();

        for (int row = 0; row < size; row++) {
            across.add(new Entry(acrossClueNumber(row), "across", row, 0, solution.get(row), hintAt(hints, row)));
        }

        for (int column = 0; column < size; column++) {
            StringBuilder answer = new StringBuilder();
            for (String row : solution) {
                answer.append(row.charAt(column));
            }
            down.add(new Entry(column + 1, "down", 0, column, answer.toString(), hintAt(hints, size + column)));
        }

        entries.put("across", across);
        entries.put("down", down);
        return entries;
    }

    private static Map<String, Map<String, String>> buildLegacyClues(List<String> hints) {
        Map<String, Map<String, String>> clues = new LinkedHashMap<>();
        Map<String, String> acrossClues = new LinkedHashMap<>();
        Map<String, String> downClues = new LinkedHashMap<>();

        for (int i = 0; i < 5; i++) {
            acrossClues.put(String.valueOf(i + 1), hintAt(hints, i));
        }
        for (int i = 5; i < 10; i++) {
            downClues.put(String.valueOf(i - 4), hintAt(hints, i));
        }

        clues.put("across", acrossClues);
        clues.put("down", downClues);
        return clues;
    }

    private static String hintAt(List<String> hints, int index) {
        return index < hints.size() ? hints.get(index) : "";
    }

    private static int clueNumberForCell(int row, int column) {
        if (row == 0) {
            return column + 1;
        }
        if (column == 0) {
            return acrossClueNumber(row);
        }
        return 0;
    }

    private static int acrossClueNumber(int row) {
        return row == 0 ? 1 : row + 5;
    }

    private static List<String> startsForCell(int row, int column) {
        List<String> starts = new ArrayList<>();
        if (column == 0) {
            starts.add("across");
        }
        if (row == 0) {
            starts.add("down");
        }
        return starts;
    }

    // Getters and Setters
    public String getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(String schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<String> getRows() {
        return rows;
    }

    public void setRows(List<String> rows) {
        this.rows = rows;
    }

    public List<List<Cell>> getGrid() {
        return grid;
    }

    public void setGrid(List<List<Cell>> grid) {
        this.grid = grid;
    }

    public Map<String, List<Entry>> getEntries() {
        return entries;
    }

    public void setEntries(Map<String, List<Entry>> entries) {
        this.entries = entries;
    }

    public String getGridJson() {
        return gridJson;
    }

    public void setGridJson(String gridJson) {
        this.gridJson = gridJson;
    }

    public String getCluesJson() {
        return cluesJson;
    }

    public void setCluesJson(String cluesJson) {
        this.cluesJson = cluesJson;
    }

    public record Cell(int row, int column, String solution, int number, List<String> starts) {
    }

    public record Entry(int number, String direction, int row, int column, String answer, String clue) {
    }
}
