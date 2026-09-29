package io.veyralang.parser;

import io.veyralang.ast.Ast;
import io.veyralang.lexer.Token;
import io.veyralang.lexer.TokenType;
import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final List<Token> tokens; private int current;
    public Parser(List<Token> tokens) { this.tokens = tokens; }
    public List<Ast.Function> parseFunctions() {
        List<Ast.Function> result = new ArrayList<>();
        while (!check(TokenType.EOF)) {
            while (match(TokenType.NEWLINE) != null) { }
            if (check(TokenType.EOF)) break;
            result.add(parseFunction());
        }
        return result;
    }
    public Ast.Function parseFunction() {
        if (match(TokenType.FUNCTION, TokenType.RULE) == null) fail("需要 function 或 rule");
        String name = consume(TokenType.IDENTIFIER, "需要函数名").lexeme(); consume(TokenType.LEFT_PAREN, "需要 (");
        List<Ast.Parameter> params = new ArrayList<>();
        if (!check(TokenType.RIGHT_PAREN)) do { String n=consume(TokenType.IDENTIFIER,"需要参数名").lexeme(); consume(TokenType.COLON,"需要参数类型"); params.add(new Ast.Parameter(n, consume(TokenType.IDENTIFIER,"需要类型名").lexeme())); } while (match(TokenType.COMMA)!=null);
        consume(TokenType.RIGHT_PAREN,"需要 )"); consume(TokenType.COLON,"需要返回类型"); String ret=consume(TokenType.IDENTIFIER,"需要返回类型").lexeme(); skipNewlines(); consume(TokenType.INDENT,"函数体需要缩进"); List<Ast.Stmt> body=block(); return new Ast.Function(name,params,ret,body);
    }
    private List<Ast.Stmt> block() { List<Ast.Stmt> result=new ArrayList<>(); while(!check(TokenType.DEDENT)&&!check(TokenType.EOF)){ if(match(TokenType.NEWLINE)!=null) continue; result.add(statement()); } if(check(TokenType.DEDENT)) advance(); return result; }
    private Ast.Stmt statement() {
        if(match(TokenType.LET)!=null) return declaration(false); if(match(TokenType.VAR)!=null) return declaration(true);
        if(match(TokenType.RETURN)!=null){ Ast.Expr e=expression(); end(); return new Ast.Return(e); }
        if(match(TokenType.EMIT)!=null){ String action=consume(TokenType.IDENTIFIER,"emit 后需要动作名").lexeme(); consume(TokenType.LEFT_PAREN,"动作参数需要 ("); List<Ast.Expr> args=new ArrayList<>(); if(!check(TokenType.RIGHT_PAREN))do{args.add(expression());}while(match(TokenType.COMMA)!=null); consume(TokenType.RIGHT_PAREN,"动作参数需要 )"); end(); return new Ast.Emit(action,args); }
        if(match(TokenType.IF)!=null) return conditional();
        Token n=consume(TokenType.IDENTIFIER,"需要语句"); consume(TokenType.EQUAL,"需要 ="); Ast.Expr e=expression(); end(); return new Ast.Assign(n.lexeme(),e);
    }
    private Ast.Stmt declaration(boolean mutable){ String n=consume(TokenType.IDENTIFIER,"需要变量名").lexeme(); if(match(TokenType.COLON)!=null) consume(TokenType.IDENTIFIER,"需要类型名"); consume(TokenType.EQUAL,"需要 ="); Ast.Expr e=expression(); end(); return new Ast.Let(n,e,mutable); }
    private Ast.Stmt conditional(){ Ast.Expr condition=expression(); end(); consume(TokenType.INDENT,"if 需要缩进体"); List<Ast.Branch> bs=new ArrayList<>(); bs.add(new Ast.Branch(condition,block())); while(match(TokenType.ELIF)!=null){ Ast.Expr c=expression(); end(); consume(TokenType.INDENT,"elif 需要缩进体"); bs.add(new Ast.Branch(c,block())); } List<Ast.Stmt> otherwise=List.of(); if(match(TokenType.ELSE)!=null){ end(); consume(TokenType.INDENT,"else 需要缩进体"); otherwise=block(); } return new Ast.If(bs,otherwise); }
    private Ast.Expr expression(){ return or(); }
    private Ast.Expr or(){ Ast.Expr e=and(); while(match(TokenType.OR_OR)!=null)e=new Ast.Binary(e,"||",and()); while(match(TokenType.QUESTION_QUESTION)!=null)e=new Ast.Binary(e,"??",and()); return e; }
    private Ast.Expr and(){ Ast.Expr e=equality(); while(match(TokenType.AND_AND)!=null)e=new Ast.Binary(e,"&&",equality()); return e; }
    private Ast.Expr equality(){ Ast.Expr e=comparison(); while(true){ if(match(TokenType.EQUAL_EQUAL)!=null)e=new Ast.Binary(e,"==",comparison()); else if(match(TokenType.BANG_EQUAL)!=null)e=new Ast.Binary(e,"!=",comparison()); else return e; } }
    private Ast.Expr comparison(){ Ast.Expr e=term(); while(true){ Token t=match(TokenType.GREATER,TokenType.GREATER_EQUAL,TokenType.LESS,TokenType.LESS_EQUAL); if(t==null)return e; e=new Ast.Binary(e,t.lexeme(),term()); } }
    private Ast.Expr term(){ Ast.Expr e=factor(); while(true){ Token t=match(TokenType.PLUS,TokenType.MINUS); if(t==null)return e; e=new Ast.Binary(e,t.lexeme(),factor()); } }
    private Ast.Expr factor(){ Ast.Expr e=unary(); while(true){ Token t=match(TokenType.STAR,TokenType.SLASH,TokenType.PERCENT); if(t==null)return e; e=new Ast.Binary(e,t.lexeme(),unary()); } }
    private Ast.Expr unary(){ Token t=match(TokenType.BANG,TokenType.MINUS); return t==null ? primary() : new Ast.Unary(t.lexeme(),unary()); }
    private Ast.Expr primary(){ Token t=advance(); Ast.Expr e=switch(t.type()){ case NUMBER->new Ast.Literal(Double.parseDouble(t.lexeme())); case STRING->new Ast.Literal(t.lexeme().substring(1,t.lexeme().length()-1)); case TRUE->new Ast.Literal(true); case FALSE->new Ast.Literal(false); case NULL->new Ast.Literal(null); case IDENTIFIER->new Ast.Variable(t.lexeme()); case LEFT_BRACKET->{List<Ast.Expr> values=new ArrayList<>();if(!check(TokenType.RIGHT_BRACKET))do{values.add(expression());}while(match(TokenType.COMMA)!=null);consume(TokenType.RIGHT_BRACKET,"列表需要 ]");yield new Ast.ListLiteral(values);} case LEFT_PAREN->{ Ast.Expr x=expression(); consume(TokenType.RIGHT_PAREN,"需要 )"); yield x; } default->throw error(t,"需要表达式"); }; while(true){ Token access=match(TokenType.DOT,TokenType.QUESTION_DOT); if(access!=null)e=new Ast.Property(e,consume(TokenType.IDENTIFIER,"需要属性名").lexeme(),access.type()==TokenType.QUESTION_DOT); else if(match(TokenType.LEFT_BRACKET)!=null){Ast.Expr index=expression();consume(TokenType.RIGHT_BRACKET,"下标需要 ]");e=new Ast.Index(e,index);} else if(match(TokenType.LEFT_PAREN)!=null){ List<Ast.Expr> args=new ArrayList<>(); if(!check(TokenType.RIGHT_PAREN))do{args.add(expression());}while(match(TokenType.COMMA)!=null); consume(TokenType.RIGHT_PAREN,"需要 )"); e=new Ast.Call(e,args); } else return e; } }
    private void end(){ if(match(TokenType.NEWLINE)==null && !check(TokenType.DEDENT) && !check(TokenType.EOF)) fail("语句末尾需要换行"); }
    private void skipNewlines(){ while(match(TokenType.NEWLINE)!=null){} }
    private Token consume(TokenType type,String msg){ if(check(type))return advance(); throw error(peek(),msg); }
    private Token match(TokenType... types){ for(TokenType t:types)if(check(t))return advance(); return null; }
    private boolean check(TokenType t){ return peek().type()==t; } private Token advance(){ if(current<tokens.size())current++; return previous(); } private Token peek(){return tokens.get(Math.min(current,tokens.size()-1));} private Token previous(){return tokens.get(current-1);}
    private void fail(String m){throw error(peek(),m);} private ParserException error(Token t,String m){return new ParserException("第 "+t.line()+" 行，第 "+t.column()+" 列："+m);}
}
