package io.veyralang.lexer;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LexerTest {
    @Test void functionAndIndentation() {
        String s = "function calc(order: Order): double\n    let amount = order.amount\n    return amount * 0.8";
        assertEquals(List.of(TokenType.FUNCTION, TokenType.IDENTIFIER, TokenType.LEFT_PAREN, TokenType.IDENTIFIER,
                TokenType.COLON, TokenType.IDENTIFIER, TokenType.RIGHT_PAREN, TokenType.COLON, TokenType.IDENTIFIER,
                TokenType.NEWLINE, TokenType.INDENT, TokenType.LET, TokenType.IDENTIFIER, TokenType.EQUAL,
                TokenType.IDENTIFIER, TokenType.DOT, TokenType.IDENTIFIER, TokenType.NEWLINE, TokenType.RETURN,
                TokenType.IDENTIFIER, TokenType.STAR, TokenType.NUMBER, TokenType.NEWLINE, TokenType.DEDENT, TokenType.EOF),
                new Lexer(s).tokenize().stream().map(Token::type).toList());
    }

    @Test void operatorsAndString() {
        assertEquals(List.of(TokenType.IF, TokenType.IDENTIFIER, TokenType.DOT, TokenType.IDENTIFIER, TokenType.AND_AND,
                TokenType.IDENTIFIER, TokenType.GREATER_EQUAL, TokenType.NUMBER, TokenType.COLON, TokenType.NEWLINE,
                TokenType.INDENT, TokenType.RETURN, TokenType.STRING, TokenType.NEWLINE, TokenType.DEDENT, TokenType.EOF),
                new Lexer("if user.vip && amount >= 1000:\n    return \"ok\"").tokenize().stream().map(Token::type).toList());
    }

    @Test void rejectsBadIndentation() {
        assertThrows(LexerException.class, () -> new Lexer("\tlet x = 1").tokenize());
        assertThrows(LexerException.class, () -> new Lexer("  let x = 1").tokenize());
    }
}
