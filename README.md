# Mini Crossword Generator
An AI-powered crossword puzzle generator
## Description
A web-app based off of the New York Times' Mini Crossword which allows the user to generate a custom themed crossword.

Built with built with Next.JS, Python, and Spring Boot, it works by (*(optionally)* repeatedly) generating words from a LLM (Perplexity or a fine tuned GPT model were used) where a backtracking algorithm would attempt to find a crossword with the goal amount of themed words included alongside an existing word list. The LLM is used again for generating hints.

https://github.com/user-attachments/assets/e5dc5ae0-9bbc-4c73-994d-c373f6f05b34

### Features
- Theme-based puzzle generation
- Interactive crossword interface
- NYT Mini-style clues and answers
- Real-time puzzle validation
- Debug mode for customizing generation parameters

### Tech Stack
- Frontend: Next.js, TypeScript, Tailwind CSS
- Backend: Spring Boot, Java
- AI: OpenAI/Perplexity APIs for word and clue generation
- ~~Database: JPA/Hibernate~~ (originally was used when this service was up on my website (taken down because api calls expensive), taken off the source)
## Usage
Requirements:
- [Python OpenAPI module](https://pypi.org/project/openai/)
- npm

Run the web application with `npm run dev` at root, and run the backend service with `./mvnw spring-boot:run`

Configure LLM credentials in `src/puzzle_gen/key.env`:

```env
OPENAI_API_KEY=your_openai_key_here
```

The generator can run with only `OPENAI_API_KEY`. If `PERPLEXITY_API_KEY` is also set, it uses Perplexity for generation by default:

```env
PERPLEXITY_API_KEY=your_perplexity_key_here
```

Optional model overrides:

```env
OPENAI_MODEL=gpt-4o-mini
PERPLEXITY_MODEL=sonar-pro
```

## Service API
The Spring Boot backend exposes a JSON API for generating mini crosswords:

```bash
curl -X POST http://localhost:8080/api/puzzles/generate \
  -H 'Content-Type: application/json' \
  -d '{
    "theme": "space travel",
    "regenerate": false,
    "maxWords": 100,
    "maxAttempts": 15,
    "themeWords": 3,
    "wordTokens": 500,
    "hintTokens": 300
  }'
```

Only `theme` is required. Generation settings default to the values shown above.

The response includes a stable structured payload under schema version `mini-crossword.v1`:

```json
{
  "schemaVersion": "mini-crossword.v1",
  "id": 1,
  "theme": "space travel",
  "size": 5,
  "rows": ["ABCDE", "FGHIJ", "KLMNO", "PQRST", "UVWXY"],
  "grid": [
    [
      {
        "row": 0,
        "column": 0,
        "solution": "A",
        "number": 1,
        "starts": ["across", "down"]
      }
    ]
  ],
  "entries": {
    "across": [
      {
        "number": 1,
        "direction": "across",
        "row": 0,
        "column": 0,
        "answer": "ABCDE",
        "clue": "Example clue"
      }
    ],
    "down": [
      {
        "number": 1,
        "direction": "down",
        "row": 0,
        "column": 0,
        "answer": "AFKPU",
        "clue": "Example clue"
      }
    ]
  }
}
```

`gridJson` and `cluesJson` are still returned for compatibility with the current frontend, but new API consumers should use `rows`, `grid`, and `entries`.

## Limitations
- Currently limited to a 5x5 grid size unlike the real NYT Mini-Crossword
- It can be a slow generation process, as it can take up to 2 minutes depending on the settings you establish. This is due to the API calls and the use of ~~Python~~.
- Theme matching isn't perfect, you're usually getting 4-5 theme words at best when you change that parameter.
- Some generated clues may not be coherant or may be inappropriate. I recommend using Perplexity for word and hint generation because it's been the most reliable during testing and has the most recent knowledge.

## Next Steps
- [ ] Implement more dynamic grid sizes.
