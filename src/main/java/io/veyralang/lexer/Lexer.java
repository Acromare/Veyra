package io.veyralang.lexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class Lexer {

    private final String source;

    public Lexer(String source) {
        this.source = source;
    }

    private static final Map<String, TokenType> KEYWORDS = Map.ofEntries(
            Map.entry("let", TokenType.LET), Map.entry("var", TokenType.VAR),
            Map.entry("function", TokenType.FUNCTION), Map.entry("rule", TokenType.RULE),
            Map.entry("if", TokenType.IF), Map.entry("elif", TokenType.ELIF),
            Map.entry("else", TokenType.ELSE), Map.entry("return", TokenType.RETURN),
            Map.entry("emit", TokenType.EMIT), Map.entry("true", TokenType.TRUE),
            Map.entry("false", TokenType.FALSE), Map.entry("null", TokenType.NULL));

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        List<Integer> levels = new ArrayList<>(List.of(0));
        String[] lines = source == null ? new String[]{""} : source.split("\\R", -1);
        for (int n = 0; n < lines.length; n++) {
            String line = lines[n];
            int first = 0;
            while (first < line.length() && line.charAt(first) == ' ') first++;
            if (first == line.length() || line.substring(first).startsWith("//")) continue;
            if (line.startsWith("\t")) throw error(n + 1, 1, "禁止使用 Tab 缩进，请使用 4 个空格");
            if (first % 4 != 0) throw error(n + 1, 1, "缩进必须是 4 个空格的倍数");
            int current = levels.get(levels.size() - 1);
            if (first > current) {
                levels.add(first); tokens.add(new Token(TokenType.INDENT, line.substring(0, first), n + 1, 1));
            } else {
                while (first < current) {
                    levels.remove(levels.size() - 1); current = levels.get(levels.size() - 1);
                    tokens.add(new Token(TokenType.DEDENT, "", n + 1, first + 1));
                }
                if (first != current) throw error(n + 1, 1, "缩进层级与前面的代码块不匹配");
            }
            scan(line, first, n + 1, tokens);
            tokens.add(new Token(TokenType.NEWLINE, "\\n", n + 1, line.length() + 1));
        }
        int line = Math.max(1, lines.length);
        while (levels.size() > 1) { levels.remove(levels.size() - 1); tokens.add(new Token(TokenType.DEDENT, "", line, 1)); }
        tokens.add(new Token(TokenType.EOF, "", line, 1));
        return List.copyOf(tokens);
    }

    private void scan(String line, int start, int lineNo, List<Token> out) {
        int i = start;
        while (i < line.length()) {
            char c = line.charAt(i);
            if (Character.isWhitespace(c)) { i++; continue; }
            if (c == '/' && i + 1 < line.length() && line.charAt(i + 1) == '/') break;
            int column = i + 1;
            if (Character.isJavaIdentifierStart(c)) {
                int begin = i++;
                while (i < line.length() && Character.isJavaIdentifierPart(line.charAt(i))) i++;
                String text = line.substring(begin, i);
                out.add(new Token(KEYWORDS.getOrDefault(text, TokenType.IDENTIFIER), text, lineNo, column)); continue;
            }
            if (Character.isDigit(c)) {
                int begin = i++;
                while (i < line.length() && Character.isDigit(line.charAt(i))) i++;
                if (i < line.length() && line.charAt(i) == '.') { i++; while (i < line.length() && Character.isDigit(line.charAt(i))) i++; }
                out.add(new Token(TokenType.NUMBER, line.substring(begin, i), lineNo, column)); continue;
            }
            if (c == '"') {
                int begin = i++; boolean closed = false;
                while (i < line.length()) { if (line.charAt(i) == '\\') i += Math.min(2, line.length() - i); else if (line.charAt(i++) == '"') { closed = true; break; } }
                if (!closed) throw error(lineNo, column, "字符串缺少结束引号");
                out.add(new Token(TokenType.STRING, line.substring(begin, i), lineNo, column)); continue;
            }
            String two = i + 1 < line.length() ? line.substring(i, i + 2) : "";
            TokenType type = switch (two) {
                case "==" -> TokenType.EQUAL_EQUAL; case "!=" -> TokenType.BANG_EQUAL;
                case ">=" -> TokenType.GREATER_EQUAL; case "<=" -> TokenType.LESS_EQUAL;
                case "&&" -> TokenType.AND_AND; case "||" -> TokenType.OR_OR;
                case "?." -> TokenType.QUESTION_DOT; case "??" -> TokenType.QUESTION_QUESTION;
                case "->" -> TokenType.ARROW; default -> null;
            };
            int length = type == null ? 1 : 2;
            if (type == null) type = switch (c) {
                case '+' -> TokenType.PLUS; case '-' -> TokenType.MINUS; case '*' -> TokenType.STAR;
                case '/' -> TokenType.SLASH; case '%' -> TokenType.PERCENT; case '=' -> TokenType.EQUAL;
                case '!' -> TokenType.BANG; case '>' -> TokenType.GREATER; case '<' -> TokenType.LESS;
                case '(' -> TokenType.LEFT_PAREN; case ')' -> TokenType.RIGHT_PAREN;
                case '[' -> TokenType.LEFT_BRACKET; case ']' -> TokenType.RIGHT_BRACKET; case ':' -> TokenType.COLON;
                case ',' -> TokenType.COMMA; case '.' -> TokenType.DOT; default -> null;
            };
            if (type == null) throw error(lineNo, column, "无法识别字符: " + c);
            out.add(new Token(type, line.substring(i, i + length), lineNo, column)); i += length;
        }
    }

    private LexerException error(int line, int column, String message) { return new LexerException("第 " + line + " 行，第 " + column + " 列：" + message); }
}
