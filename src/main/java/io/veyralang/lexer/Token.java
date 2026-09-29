package io.veyralang.lexer;

public record Token(TokenType type, String lexeme, int line, int column) { }
