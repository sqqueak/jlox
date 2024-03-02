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

  private void scanToken() {
    char c = advance();
    switch(c) {
      case '(': addToken(LEFT_PAREN); break;
      case ')': addToken(RIGHT_PAREN); break;
      case '{': addToken(LEFT_BRACE); break;
      case '}': addToken(RIGHT_BRACE); break;
      case ',': addToken(COMMA); break;
      case '.': addToken(DOT); break;
      case '-': addToken(MINUS); break;
      case '+': addToken(PLUS); break;
      case ';': addToken(SEMICOLON); break;
      case '*': addToken(STAR); break; 
      case '!':
        addToken(match('=') ? BANG_EQUAL : BANG);                               // Match token based on second character
        break;
      case '=':
        addToken(match('=') ? EQUAL_EQUAL : EQUAL);
        break;
      case '<':
        addToken(match('=') ? LESS_EQUAL : LESS);
        break;
      case '>':
        addToken(match('=') ? GREATER_EQUAL : GREATER);
        break;
      
      default:
        Lox.error(line, "Unexpected character.");                               // Reads invalid character but keeps scanning
        break;
    }
  }

  private boolean match(char expected) {
    if(isAtEnd()) return false;
    if(source.charAt(current) != expected) return false;

    current++;                                                                  // Moving forward one more character if it matches
    return true;                                                                // The second character is now part of the token
  }

  private boolean isAtEnd() {
    return current >= source.length();                                          // Checks if all characters have been consumed
  }

  private char advance() {
    return source.charAt(current++);                                            // Gets next character in source file
  }

  private void addToken(TokenType type) {
    addToken(type, null);
  }

  private void addToken(TokenType type, Object literal) {
    String text = source.substring(start, current);                             // Gets substring from source file and creates token
    tokens.add(new Token(type, text, literal, line));
  }
}
