package lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
  static boolean hadError = false;                                              // Marks if code has errors

  public static void main(String[] args) throws IOException {
    if(args.length > 1) {
      System.out.println("Usage: jlox [script]");
      System.exit(64);
    } else if(args.length == 1) {
      runFile(args[0]);                                                         // Execute specified file
    } else {
      runPrompt();                                                              // Run code interactively
    }
  }

  private static void runFile(String path) throws IOException {
    byte[] bytes = Files.readAllBytes(Paths.get(path));                         // Parse file -> bytes -> string
    run(new String(bytes, Charset.defaultCharset()));

    if(hadError) System.exit(65);                                               // Exits if error occurred while running
  }

  private static void runPrompt() throws IOException {
    InputStreamReader input = new InputStreamReader(System.in);                 // Setup for reading from command line
    BufferedReader reader = new BufferedReader(input);

    for(;;) {                                                                   // Read from command line until empty line
      System.out.print("> ");
      String line = reader.readLine();
      if(line == null) break;
      run(line);
      hadError = false;
    }
  }

  private static void run(String source) {
    Scanner scanner = new Scanner(source);
    List<Token> tokens = scanner.scanTokens();                                  // Converts string into tokens

    // For now, just print the tokens.
    for(Token token : tokens) {
      System.out.println(token);
    }
  }

  static void error(int line, String message) {                                 // Very basic error handling
    report(line, "", message);
  }

  private static void report(int line, String where, String message) {
    System.err.println("[line " + line + "] Error" + where + ": " + message);   // Displaying where error was located
    hadError = true;
  }

  static void error(Token token, String message) {
    if(token.type == TokenType.EOF) {                                           // Tracks where error occurs based on token type
      report(token.line, " at end", message);
    } else {
      report(token.line, " at '" + token.lexeme + "'", message);
    }
  }
}