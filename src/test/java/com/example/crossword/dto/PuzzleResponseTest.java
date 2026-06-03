package com.example.crossword.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.crossword.model.Puzzle;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

class PuzzleResponseTest {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    void fromEntityBuildsStructuredResponseAndKeepsLegacyJsonFields() throws Exception {
        Puzzle puzzle = new Puzzle();
        puzzle.setTheme("test theme");
        puzzle.setSolutionJson(mapper.writeValueAsString(List.of("ABCDE", "FGHIJ", "KLMNO", "PQRST", "UVWXY")));
        puzzle.setHintsJson(mapper.writeValueAsString(List.of(
            "across one",
            "across two",
            "across three",
            "across four",
            "across five",
            "down one",
            "down two",
            "down three",
            "down four",
            "down five"
        )));

        PuzzleResponse response = PuzzleResponse.fromEntity(puzzle);

        assertThat(response.getSchemaVersion()).isEqualTo("mini-crossword.v1");
        assertThat(response.getTheme()).isEqualTo("test theme");
        assertThat(response.getSize()).isEqualTo(5);
        assertThat(response.getRows()).containsExactly("ABCDE", "FGHIJ", "KLMNO", "PQRST", "UVWXY");
        assertThat(response.getGrid().get(0).get(0).number()).isEqualTo(1);
        assertThat(response.getGrid().get(0).get(0).starts()).containsExactly("across", "down");
        assertThat(response.getGrid().get(1).get(0).number()).isEqualTo(6);
        assertThat(response.getEntries().get("across").get(1).number()).isEqualTo(6);
        assertThat(response.getEntries().get("across").get(1).answer()).isEqualTo("FGHIJ");
        assertThat(response.getEntries().get("down").get(0).answer()).isEqualTo("AFKPU");
        assertThat(response.getEntries().get("down").get(0).clue()).isEqualTo("down one");

        List<String> legacyGrid = mapper.readValue(response.getGridJson(), new TypeReference<List<String>>() {});
        assertThat(legacyGrid).isEqualTo(response.getRows());
        assertThat(response.getCluesJson()).contains("across one", "down five");
    }
}
