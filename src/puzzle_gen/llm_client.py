from openai import OpenAI
from typing import List
import os
import json
from pathlib import Path
from dotenv import load_dotenv
import logging
import sys
import traceback

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

current_dir = Path(__file__).parent
env_path = current_dir / 'key.env'

load_dotenv(env_path, override=True)

OPENAI_API_KEY = os.getenv('OPENAI_API_KEY')
PERPLEXITY_API_KEY = os.getenv('PERPLEXITY_API_KEY') # Alternative for more expensive model, better for recent events
OPENAI_MODEL = os.getenv('OPENAI_MODEL', 'gpt-4o-mini')
PERPLEXITY_MODEL = os.getenv('PERPLEXITY_MODEL', 'sonar-pro')

def mask_key(api_key):
    if not api_key:
        return "<missing>"
    return f"{'*' * max(len(api_key) - 4, 0)}{api_key[-4:]}"

logger.info(f"Perplexity API Key: {mask_key(PERPLEXITY_API_KEY)}")
logger.info(f"OpenAI API Key: {mask_key(OPENAI_API_KEY)}")

# Add better validation for API keys
if not OPENAI_API_KEY and not PERPLEXITY_API_KEY:
    logger.error("Missing required API keys")
    raise ValueError("Missing required API keys. Set OPENAI_API_KEY or PERPLEXITY_API_KEY in src/puzzle_gen/key.env or the process environment.")

try:
    perplexity_client = OpenAI(api_key=PERPLEXITY_API_KEY, base_url="https://api.perplexity.ai") if PERPLEXITY_API_KEY else None
    openai_client = OpenAI(api_key=OPENAI_API_KEY, base_url="https://api.openai.com/v1") if OPENAI_API_KEY else None
    generation_client = perplexity_client or openai_client
    generation_model = PERPLEXITY_MODEL if perplexity_client else OPENAI_MODEL
    logger.info(f"Using generation model: {generation_model}")
except Exception as e:
    logger.error(f"Failed to initialize API clients: {str(e)}")
    raise

def get_candidate_words(theme: str = "", num_words: int = 100, words_to_avoid: List[str] = [], tokens: int = 500) -> List[str]:
    try:
        if not isinstance(theme, str):
            raise ValueError(f"Theme must be a string, got {type(theme)}")
        if not isinstance(num_words, int):
            raise ValueError(f"num_words must be an integer, got {type(num_words)}")
        if not isinstance(words_to_avoid, list):
            raise ValueError(f"words_to_avoid must be a list, got {type(words_to_avoid)}")

        prompt_text = (
            (f"Generate exactly {num_words} different " if num_words != -1 else "Generate as many as you can different ") +
            f" real, 5-letter (must be an actual word or commonly used abbreviation) English words that could be used in a crossword puzzle." +
            (f" Must be related to '{theme}'." if theme else "") +
            " Use common English words that often appear in crosswords." +
            " Return only uppercase words, separated by commas, nothing else." +
            " Try to reference recent events, popular culture, and other current events." +
            (f" Exclude: {', '.join(words_to_avoid[:20])}..." if words_to_avoid else "")
        )
        
        try:
            response = generation_client.chat.completions.create(
                model=generation_model,
                messages=[
                    {"role": "user", "content": prompt_text}
                ],
                max_tokens=tokens,
                temperature=0.9,
                top_p=0.95,
                n=1
            )
        except Exception as e:
            logger.error(f"API call failed: {str(e)}")
            raise

        words_text = response.choices[0].message.content
        words = [word.strip().upper() for word in words_text.split(',') if word.strip()]
        
        if num_words != -1:
            logger.info(f"Requested {num_words} words, got {len(words)} words")
        else:
            logger.info(f"Generated {len(words)} words")
            
        if not words:
            raise ValueError("No valid words were generated")
            
        return words
    except Exception as e:
        logger.error(f"Error in get_candidate_words: {str(e)}")
        logger.error(f"Full traceback: {traceback.format_exc()}")
        raise

def generate_hints(crossword: List[str], theme: str = "", tokens: int = 300) -> List[str]:
    try:
        if not isinstance(crossword, list):
            raise ValueError(f"crossword must be a list, got {type(crossword)}")
        if not crossword:
            raise ValueError("Empty crossword provided")
        if not all(isinstance(word, str) and len(word) == 5 for word in crossword):
            raise ValueError("All words must be 5-letter strings")
            
        #Get Down Words
        down_words = ["".join(col) for col in zip(*crossword)]
        words = crossword + down_words # Full Word List
        logger.info(f"Words: {words}")

        prompt_text = (
            "Write concise clues in the style of the NYT Mini Crossword for these 10 answers. "
            "Return only a valid JSON array of exactly 10 strings, in the same order as the answers. "
            "Do not include numbering, answer words, markdown, explanations, or introductory text. "
            "Each string must be only the clue text. "
            + (f"If an answer can relate to '{theme}', make that clue fit the theme. " if theme else "")
            + f"Answers: {json.dumps(words)}"
        )

        try:
            response = generation_client.chat.completions.create(
                model=generation_model,
                messages=[{"role": "user", "content": prompt_text}],
                max_tokens=tokens,
                temperature=0.8,
                top_p=0.95,
                n=1
            )
        except Exception as e:
            logger.error(f"API call failed: {str(e)}")
            raise
        
        hints_text = response.choices[0].message.content.strip()
        try:
            hints = json.loads(hints_text)
        except json.JSONDecodeError:
            hints = hints_text.splitlines()

        hints = [hint.strip() for hint in hints if isinstance(hint, str) and hint.strip()]
        hints = [hint[4:] if len(hint) > 4 and hint[:2].isdigit() and hint[2] == '.' and hint[3] == ' '
                else hint[3:] if len(hint) > 3 and hint[0].isdigit() and hint[1] == '.' and hint[2] == ' '
                else hint for hint in hints]
        hints = [hint.split(":", 1)[1].strip() if ":" in hint and hint.split(":", 1)[0].strip().upper() in words else hint for hint in hints]
                
        if len(hints) != len(words):
            raise ValueError(f"Expected {len(words)} hints, got {len(hints)}")

        if not hints:
            raise ValueError("No valid hints were generated")
            
        return hints
    except Exception as e:
        logger.error(f"Error in generate_hints: {str(e)}")
        logger.error(f"Full traceback: {traceback.format_exc()}")
        raise

# Improve the test case to catch and log errors
if __name__ == "__main__":
    try:
        logger.info("Starting test case")
        words = get_candidate_words("car brands")
        logger.info(f"Generated words: {words}")

        hints = generate_hints(words, "car brands")
        logger.info(f"Generated hints: {hints}")
        
        logger.info("Test case completed successfully")
        sys.exit(0)
    except Exception as e:
        logger.error(f"Test case failed: {str(e)}")
        logger.error(f"Full traceback: {traceback.format_exc()}")
        sys.exit(1)
