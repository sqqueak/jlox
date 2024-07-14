package lox;

import java.util.ArrayList;
import java.util.List;

import static lox.TokenType.*;

class Parser {
  private static class ParseError extends RuntimeException {}

  private final List<Token> tokens;
  private int current = 0;

  Parser(List<Token> tokens) {
    this.tokens = tokens;
  }

  List<Stmt> parse() {
    List<Stmt> statements = new ArrayList<>();
    while(!isAtEnd()) {
      statements.add(declaration());
    }

    return statements;
  }

  private Expr expression() {                                                   // expression -> equality
    return assignment();
  }

  private Stmt declaration() {
    try {
      if(match(VAR)) return varDeclaration();                                   // declaration -> varDecl | statement
      return statement();
    } catch (ParseError error) {
      synchronize();
      return null;
    }
  }

  private Stmt statement() {                                                    // statement -> exprStmt | printStmt
    if(match(PRINT)) return printStatement();
    if(match(LEFT_BRACE)) return new Stmt.Block(block());

    return expressionStatement();
  }

  private Stmt printStatement() {                                               // printStmt -> "print" expression ";"
    Expr value = expression();
    consume(SEMICOLON, "Expect ';' after value.");
    return new Stmt.Print(value);
  }

  private Stmt varDeclaration() {                                               // varDecl -> "var" IDENTIFIER ( "=" expression )? ";"
    Token name = consume(IDENTIFIER, "Expect variable name.");

    Expr initializer = null;
    if(match(EQUAL)) {
      initializer = expression();
    }

    consume(SEMICOLON, "Expect ';' after variable declaration.");
    return new Stmt.Var(name, initializer);
  }

  private Stmt expressionStatement() {                                          // exprStmt -> expression ";"
    Expr expr = expression();
    consume(SEMICOLON, "Expect ';' after value.");
    return new Stmt.Expression(expr);
  }

  private List<Stmt> block() {
    // These are all the statements that are encompassed in this block.
    List<Stmt> statements = new ArrayList<>();

    // While we haven't reached the end of the block (denoted by the closing
    // curly), keep adding each statement to the list of statements that lie in
    // this block.
    while(!check(RIGHT_BRACE) && !isAtEnd()) {
      statements.add(declaration());
    }

    // If the last character isn't a closing curly, throw an error.
    consume(RIGHT_BRACE, "Expect '}' after block.");

    // Return list of statements in the block after successful parsing.
    return statements;
  }

  private Expr assignment() {
    Expr expr = equality();

    if(match(EQUAL)) {
      Token equals = previous();
      Expr value = assignment();

      if(expr instanceof Expr.Variable) {
        Token name = ((Expr.Variable)expr).name;
        return new Expr.Assign(name, value);
      }

      error(equals, "Invalid assignment target.");
    }

    return expr;
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
    if(match(FALSE)) return new Expr.Literal(false);                            // Terminals for states
    if(match(TRUE)) return new Expr.Literal(true);
    if(match(NIL)) return new Expr.Literal(null);

    if (match(NUMBER, STRING)) {                                                // Terminals for literals
      return new Expr.Literal(previous().literal);
    }

    if(match(IDENTIFIER)) {
      return new Expr.Variable(previous());
    }

    if(match(LEFT_PAREN)) {                                                     // Grouping
      Expr expr = expression();
      consume(RIGHT_PAREN, "Expect ')' after expression.");
      return new Expr.Grouping(expr);
    }

    throw error(peek(), "Expect expression.");
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

  private Token consume(TokenType type, String message) {
    if(check(type)) return advance();

    throw error(peek(), message);
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

  private ParseError error(Token token, String message) {
    Lox.error(token, message);
    return new ParseError();
  }

  private void synchronize() {
    advance();

    while(!isAtEnd()) {                                                         // Semicolon (usually) indicates new statement
      if(previous().type == SEMICOLON) return;                                  // So everything after this is good code

      switch(peek().type) {                                                     // Discard tokens until statement boundary is reached
        case CLASS:
        case FUN:
        case VAR:
        case FOR:
        case IF:
        case WHILE:
        case PRINT:
        case RETURN:
          return;
      }

      advance();
    }
  }
}
