package io.veyralang;

import io.veyralang.ast.Ast;
import io.veyralang.lexer.Lexer;
import io.veyralang.runtime.CompiledFunction;
import io.veyralang.runtime.HostContext;
import io.veyralang.runtime.Interpreter;
import io.veyralang.typecheck.TypeChecker;
import io.veyralang.parser.Parser;
import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Veyra {
    private Veyra() {}
    public static CompiledFunction compile(String source, HostContext context) {
        return compile(source, context, null);
    }

    public static CompiledFunction compile(String source, HostContext context, String entry) {
        List<Ast.Function> functions = new Parser(new Lexer(source).tokenize()).parseFunctions();
        Ast.Function function = functions.stream()
                .filter(candidate -> entry == null || candidate.name().equals(entry))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("找不到入口函数: " + entry));
        new TypeChecker(context, functions).checkAll(functions);
        return new CompiledFunction(){
            public Object call(Object... args){return execute(args).value();}
            public io.veyralang.runtime.ScriptResult execute(Object... args){validateArguments(function, context, args);return new Interpreter(function, functions).execute(args);}
        };
    }

    public static CompiledFunction compile(Path file, HostContext context) throws IOException {
        return compile(Files.readString(file), context);
    }

    public static CompiledFunction compile(Path file, HostContext context, String entry) throws IOException {
        return compile(Files.readString(file), context, entry);
    }

    public static void check(Path file, HostContext context) throws IOException {
        check(Files.readString(file), context);
    }

    public static void check(String source, HostContext context) {
        List<Ast.Function> functions = new Parser(new Lexer(source).tokenize()).parseFunctions();
        new TypeChecker(context, functions).checkAll(functions);
    }

    private static void validateArguments(Ast.Function function, HostContext host, Object[] args) {
        if (args.length != function.parameters().size()) throw new IllegalArgumentException("入口函数参数数量不匹配");
        for (int i=0;i<args.length;i++) {
            String name=function.parameters().get(i).type();
            Class<?> expected=switch(name){case "double"->Double.class;case "int"->Integer.class;case "bool","boolean"->Boolean.class;case "string","String"->String.class;default->host.type(name);};
            if(args[i]==null || expected==null || !expected.isInstance(args[i])) throw new IllegalArgumentException("参数 "+function.parameters().get(i).name()+" 需要 "+name);
        }
    }
}
