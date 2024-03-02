package lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static lox.TokenType.*;

class Scanner {
  private final String source;
  private final List<Token> tokens = new ArrayList<>();
  private int start = 0;                                                        // Tracks first character in lexeme
  private int current = 0;                                                      // Tracks current character
  private int line = 1;                                                         // Tracks which source line `current` is on

  Scanner(String source) {
    this.source = source;
  }

  List<Token> scanTokens() {
    while(!isAtEnd()) {
      // beginning of next lexeme
      start = current;
      scanToken();
    }

    tokens.add(new Token(EOF, "", null, line));                                 // Adds final EOF token to signify finish
    return tokens;
  }

  private boolean isAtEnd() {
    return current >= source.length();                                          // Checks if all characters have been consumed
  }
}
