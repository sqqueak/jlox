package lox;

import java.util.HashMap;
import java.util.Map;

public class Environment {
  final Environment enclosing;
  private final Map<String, Object> values = new HashMap<>();

  Environment() {
    enclosing = null;
  }

  Environment(Environment enclosing) {
    this.enclosing = enclosing;
  }

  Object get(Token name) {
    if(values.containsKey(name.lexeme)) {
      return values.get(name.lexeme);
    }

    // If the variable isn't defined in the current environment, then go to the
    // parent environment to find the variable. Repeat until we reach the
    // outermost environment. 
    if(enclosing != null) return enclosing.get(name);

    throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
  }

  void assign(Token name, Object value) {
    // If we're assigning a value to a variable, that means it should already
    // exist in the environment's list of variables. If it doesn't exist then it
    // means we can't assign a value to it without creating a new entry, which
    // would be a definition.
    if(values.containsKey(name.lexeme)) {
      values.put(name.lexeme, value);
      return;
    }

    if(enclosing != null) {
      enclosing.assign(name, value);
      return;
    }

    throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
  }

  void define(String name, Object value) {
    values.put(name, value);
  }
}
