#!/usr/bin/env bash
set -euo pipefail

BACKEND_URL="${CROSSWORD_BACKEND_URL:-http://localhost:8080}"
THEME="${CROSSWORD_DAILY_THEME:-}"

if [ -n "$THEME" ]; then
  BODY="{\"theme\":\"$THEME\"}"
else
  BODY="{}"
fi

curl -fsS -X POST "$BACKEND_URL/api/puzzles/daily" \
  -H 'Content-Type: application/json' \
  -d "$BODY"
