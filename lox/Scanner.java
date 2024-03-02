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
      case '/':
        if(match('/')) {                                                        // Matched single line comment
          while(peek() != '\n' && !isAtEnd()) advance();                        // Keep consuming until newlinen is reached
        } else {
          addToken(SLASH);
        }
        break;

      case ' ':                                                                 // Ignoring whitespace characters
      case '\r':
      case '\t':
        break;

      case '\n':
        line++;
        break;

      case '"': string(); break;
      
      default:
        if(isDigit(c)) {
          number();                                                             // Matching an integer or decimal literal
        } else {
          Lox.error(line, "Unexpected character.");                             // Reads invalid character but keeps scanning
        }
        break;
    }
  }

  private void number() {
    while(isDigit(peek())) advance();                                           // Getting digits before the decimal point

    if(peek() == '.' && isDigit(peekNext())) {
      advance();                                                                // If there's a decimal point, consume it
      while(isDigit(peek())) advance();                                         // Getting digits after the decimal point
    }

    addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
  }

  private void string() {
    while(peek() != '"' && !isAtEnd()) {                                        // Keep consuming characters that belong to the string literal
      if(peek() == '\n') line++;                                                // Hitting newline inside a string (for multiline strings)
      advance();
    }

    if(isAtEnd()) {
      Lox.error(line, "Unterminated string.");                                  // Throws error if scanner reaches end of file before the string is terminated
      return;
    }

    advance();                                                                  // Consume the ending "
    String value = source.substring(start + 1, current - 1);                     // Get the string literal without the quotation marks on either side
    addToken(STRING, value);
  }

  private boolean match(char expected) {
    if(isAtEnd()) return false;
    if(source.charAt(current) != expected) return false;

    current++;                                                                  // Moving forward one more character if it matches
    return true;                                                                // The second character is now part of the token
  }

  private char peek() {
    if(isAtEnd()) return '\0';
    return source.charAt(current);                                              // Getting next character without consuming it
  }

  private char peekNext() {
    if(current + 1 >= source.length()) return '\0';
    return source.charAt(current + 1);                                          // Getting next next character without consuming it
  }

  private boolean isDigit(char c) {
    return c >= '0' && c <= '9';
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
