package io.veyralang.ast;

import java.util.List;

public final class Ast {
    private Ast() {}
    public sealed interface Expr permits Literal, Variable, Property, Call, Index, ListLiteral, Unary, Binary {}
    public record Literal(Object value) implements Expr {}
    public record Variable(String name) implements Expr {}
    public record Property(Expr target, String name, boolean safe) implements Expr {}
    public record Call(Expr target, List<Expr> arguments) implements Expr {}
    public record Index(Expr target, Expr index) implements Expr {}
    public record ListLiteral(List<Expr> elements) implements Expr {}
    public record Unary(String op, Expr value) implements Expr {}
    public record Binary(Expr left, String op, Expr right) implements Expr {}

    public sealed interface Stmt permits Let, Assign, Return, If, Emit {}
    public record Let(String name, Expr value, boolean mutable) implements Stmt {}
    public record Assign(String name, Expr value) implements Stmt {}
    public record Return(Expr value) implements Stmt {}
    public record If(List<Branch> branches, List<Stmt> otherwise) implements Stmt {}
    public record Emit(String action, List<Expr> arguments) implements Stmt {}
    public record Branch(Expr condition, List<Stmt> body) {}
    public record Parameter(String name, String type) {}
    public record Function(String name, List<Parameter> parameters, String returnType, List<Stmt> body) {}
}
