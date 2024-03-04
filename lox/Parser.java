package lox;

import java.util.List;

import static lox.TokenType.*;

class Parser {
  private final List<Token> tokens;
  private int current = 0;

  Parser(List<Token> tokens) {
    this.tokens = tokens;
  }

  private Expr expression() {                                                   // expression -> equality
    return equality();
  }

  private Expr equality() {                                                     // equality -> comparison ( ( "!=" | "==" ) comparison )*
    Expr expr = comparison();

    while(match(BANG_EQUAL, EQUAL_EQUAL)) {
      Token operator = previous();
      Expr right = comparison();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr comparison() {                                                   // comparison -> term ( ( ">" | ">=" | "<" | "<=" ) term )*
    Expr expr = term();

    while(match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
      Token operator = previous();
      Expr right = term();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr term() {                                                         // term -> factor ( ( "-" | "+" ) factor )*
    Expr expr = factor();

    while(match(MINUS, PLUS)) {
      Token operator = previous();
      Expr right = factor();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr factor() {                                                       // factor -> unary ( ( "/" | "*" ) unary )*
    Expr expr = unary();

    while (match(SLASH, STAR)) {
      Token operator = previous();
      Expr right = unary();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr unary() {                                                        // unary -> ( "!" | "-" ) unary 
    if(match(BANG, MINUS)) {                                                    //        | primary
      Token operator = previous();
      Expr right = unary();
      return new Expr.Unary(operator, right);
    }

    return primary();
  }

  private Expr primary() {                                                      // primary -> NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")"
    if (match(FALSE)) return new Expr.Literal(false);                     // Terminals for states
    if (match(TRUE)) return new Expr.Literal(true);
    if (match(NIL)) return new Expr.Literal(null);

    if (match(NUMBER, STRING)) {                                                // Terminals for literals
      return new Expr.Literal(previous().literal);
    }

    if (match(LEFT_PAREN)) {                                                    // Grouping
      Expr expr = expression();
      consume(RIGHT_PAREN, "Expect ')' after expression.");
      return new Expr.Grouping(expr);
    }
  }

  private boolean match(TokenType... types) {
    for(TokenType type : types) {
      if(check(type)) {                                                         // Checks to see if current token's type is in the list of given types
        advance();
        return true;
      }
    }

    return false;
  }

  private boolean check(TokenType type) {
    if(isAtEnd()) return false;
    return peek().type == type;                                                 // Checks if current token is of given type
  }

  private Token advance() {
    if(!isAtEnd()) current++;                                                   // Consume current token
    return previous();
  }

  private boolean isAtEnd() {
    return peek().type == EOF;
  }

  private Token peek() {
    return tokens.get(current);
  }

  private Token previous() {
    return tokens.get(current - 1);
  }
}
