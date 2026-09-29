package io.veyralang.typecheck;

import io.veyralang.ast.Ast;
import io.veyralang.runtime.HostContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.IdentityHashMap;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/** Checks every script function against its peer signatures and the host contract. */
public final class TypeChecker {
    private final HostContext host;
    private final Map<String, Ast.Function> functions = new HashMap<>();
    private final Map<String, Class<?>> variables = new HashMap<>();
    private final Map<String, Boolean> mutable = new HashMap<>();
    private final Map<Ast.Expr, Class<?>> elementTypes = new IdentityHashMap<>();
    private final Map<String, Class<?>> variableElementTypes = new HashMap<>();
    private Class<?> returnType;

    public TypeChecker(HostContext host) { this(host, List.of()); }
    public TypeChecker(HostContext host, List<Ast.Function> functions) {
        this.host = host;
        for (Ast.Function function : functions) {
            if (this.functions.putIfAbsent(function.name(), function) != null) fail("重复定义函数: " + function.name());
        }
    }
    public void checkAll(List<Ast.Function> all) { for (Ast.Function f : all) check(f); }
    public void check(Ast.Function function) {
        variables.clear(); mutable.clear(); elementTypes.clear(); variableElementTypes.clear();
        for (Ast.Parameter p : function.parameters()) {
            Class<?> type = resolve(p.type(), "参数 " + p.name());
            if (variables.putIfAbsent(p.name(), type) != null) fail("重复参数: " + p.name());
            mutable.put(p.name(), false);
        }
        returnType = resolve(function.returnType(), "返回值");
        checkStatements(function.body());
        if (returnType != Void.class && !alwaysReturns(function.body())) fail("函数 " + function.name() + " 并非所有执行路径都会返回值");
    }
    private boolean alwaysReturns(List<Ast.Stmt> statements) {
        for (Ast.Stmt s : statements) {
            if (s instanceof Ast.Return) return true;
            if (s instanceof Ast.If x && !x.otherwise().isEmpty()
                    && x.branches().stream().allMatch(b -> alwaysReturns(b.body()))
                    && alwaysReturns(x.otherwise())) return true;
        }
        return false;
    }
    private void checkStatements(List<Ast.Stmt> statements) {
        for (Ast.Stmt s : statements) {
            if (s instanceof Ast.Let x) {
                if (variables.containsKey(x.name())) fail("变量已定义: " + x.name());
                Class<?> type = expressionType(x.value());
                variables.put(x.name(), type); mutable.put(x.name(), x.mutable());
                Class<?> element=elementTypes.get(x.value()); if(element!=null) variableElementTypes.put(x.name(),element);
            } else if (s instanceof Ast.Assign x) {
                Class<?> expected = variables.get(x.name());
                if (expected == null) fail("未定义变量: " + x.name());
                if (!mutable.getOrDefault(x.name(), false)) fail("不可变变量不能重新赋值: " + x.name());
                requireAssignable(expected, expressionType(x.value()), "变量 " + x.name());
            } else if (s instanceof Ast.Return x) requireAssignable(returnType, expressionType(x.value()), "返回值");
            else if (s instanceof Ast.Emit x) {
                HostContext.ActionSpec action = host.action(x.action());
                if (action == null) fail("宿主未授权业务动作: " + x.action());
                Class<?>[] expected = action.parameterTypes();
                if (expected.length != x.arguments().size()) fail("动作 " + x.action() + " 参数数量不匹配");
                for (int i = 0; i < expected.length; i++) requireAssignable(expected[i], expressionType(x.arguments().get(i)), "动作 " + x.action() + " 参数 " + (i + 1));
            } else if (s instanceof Ast.If x) {
                for (Ast.Branch b : x.branches()) { requireBoolean(expressionType(b.condition()), "if 条件"); checkStatements(b.body()); }
                checkStatements(x.otherwise());
            }
        }
    }
    private Class<?> expressionType(Ast.Expr e) {
        if (e instanceof Ast.Literal x) {
            if (x.value() == null) return Void.class;
            if (x.value() instanceof Boolean) return boolean.class;
            if (x.value() instanceof Number) return double.class;
            return String.class;
        }
        if (e instanceof Ast.Variable x) { Class<?> t = variables.get(x.name()); if (t == null) fail("未定义变量: " + x.name()); Class<?> el=variableElementTypes.get(x.name());if(el!=null)elementTypes.put(e,el);return t; }
        if (e instanceof Ast.ListLiteral x) { Class<?> element=Object.class; for(Ast.Expr value:x.elements()){Class<?> next=expressionType(value);if(element==Object.class)element=next;else if(element!=next)fail("列表元素类型必须一致");}elementTypes.put(e,element);return List.class; }
        if (e instanceof Ast.Index x) { Class<?> collection=expressionType(x.target());requireNumber(expressionType(x.index()),"列表下标");if(!List.class.isAssignableFrom(collection)&&!collection.isArray())fail("只能对列表或数组使用下标");Class<?> el=elementTypes.getOrDefault(x.target(),collection.isArray()?collection.getComponentType():Object.class);return el; }
        if (e instanceof Ast.Property x) { Class<?> target=expressionType(x.target()); if((List.class.isAssignableFrom(target)||target.isArray())&&x.name().equals("size"))return int.class; Class<?> result=propertyType(target,x.name(),x.safe()); if(List.class.isAssignableFrom(result)){Class<?> el=genericElement(x.target(),x.name());if(el!=null)elementTypes.put(e,el);} return result; }
        if (e instanceof Ast.Call x) {
            if(x.target() instanceof Ast.Property p && p.name().equals("contains")) { Class<?> owner=expressionType(p.target()); if(!List.class.isAssignableFrom(owner))fail("contains 仅支持只读列表");if(x.arguments().size()!=1)fail("contains 需要 1 个参数");Class<?> element=elementTypes.get(p.target());if(element!=null)requireAssignable(element,expressionType(x.arguments().get(0)),"contains 参数");else expressionType(x.arguments().get(0));return boolean.class; }
            if (!(x.target() instanceof Ast.Variable v)) fail("函数调用目标必须是函数名");
            Ast.Function target = functions.get(((Ast.Variable)x.target()).name());
            if (target == null) fail("未定义函数: " + ((Ast.Variable)x.target()).name());
            if (target.parameters().size() != x.arguments().size()) fail("函数 " + target.name() + " 参数数量不匹配");
            for (int i=0;i<x.arguments().size();i++) requireAssignable(resolve(target.parameters().get(i).type(), "函数参数"), expressionType(x.arguments().get(i)), "函数 " + target.name() + " 参数 " + (i+1));
            return resolve(target.returnType(), "函数返回值");
        }
        if (e instanceof Ast.Unary x) { Class<?> t=expressionType(x.value()); if(x.op().equals("!")){requireBoolean(t,"!");return boolean.class;} requireNumber(t,"一元 -");return double.class; }
        Ast.Binary x=(Ast.Binary)e; Class<?> l=expressionType(x.left());
        if(x.op().equals("&&")||x.op().equals("||")){requireBoolean(l,x.op());requireBoolean(expressionType(x.right()),x.op());return boolean.class;}
        Class<?> r=expressionType(x.right());
        if(x.op().equals("??"))return l==Void.class?r:l;
        if(x.op().equals("==")||x.op().equals("!="))return boolean.class;
        if(x.op().matches("[<>]=?")){requireNumber(l,x.op());requireNumber(r,x.op());return boolean.class;}
        if(x.op().equals("+")&&(l==String.class||r==String.class))return String.class;
        requireNumber(l,x.op());requireNumber(r,x.op());return double.class;
    }
    private Class<?> propertyType(Class<?> target,String name,boolean safe){
        if(target==Void.class){if(safe)return Object.class;fail("无法从 null 读取属性 "+name);}
        String cap=Character.toUpperCase(name.charAt(0))+name.substring(1);
        try{return target.getMethod("get"+cap).getReturnType();}catch(NoSuchMethodException ignored){}
        try{return target.getMethod("is"+cap).getReturnType();}catch(NoSuchMethodException ignored){}
        try{return target.getMethod(name).getReturnType();}catch(NoSuchMethodException ignored){}
        try{return target.getField(name).getType();}catch(NoSuchFieldException ignored){}
        fail(target.getSimpleName()+" 没有可访问属性 "+name+"，是否拼写错误？");return Object.class;
    }
    private Class<?> genericElement(Ast.Expr owner,String property){
        Class<?> target=expressionType(owner);String cap=Character.toUpperCase(property.charAt(0))+property.substring(1);
        for(String method:new String[]{"get"+cap,property})try{Type type=target.getMethod(method).getGenericReturnType();if(type instanceof ParameterizedType p&&p.getActualTypeArguments()[0] instanceof Class<?> c)return c;}catch(NoSuchMethodException ignored){}
        return null;
    }
    private Class<?> resolve(String name,String context){return switch(name){case "double"->double.class;case "int"->int.class;case "bool","boolean"->boolean.class;case "string","String"->String.class;case "void"->Void.class;default->{Class<?> t=host.type(name);if(t==null)fail(context+"使用了未注册类型 "+name);yield t;}};}
    private void requireBoolean(Class<?> t,String c){if(t!=boolean.class&&t!=Boolean.class)fail(c+"需要 bool");}
    private void requireNumber(Class<?> t,String c){if(!Number.class.isAssignableFrom(box(t)))fail(c+"需要数字");}
    private void requireAssignable(Class<?> e,Class<?> a,String c){if(e==a)return;if(e==double.class&&Number.class.isAssignableFrom(box(a)))return;if(box(e).isAssignableFrom(box(a)))return;fail(c+"类型不匹配，需要 "+e.getSimpleName()+"，实际是 "+a.getSimpleName());}
    private Class<?> box(Class<?> t){if(t.isPrimitive()){if(t==double.class)return Double.class;if(t==int.class)return Integer.class;if(t==long.class)return Long.class;if(t==boolean.class)return Boolean.class;if(t==float.class)return Float.class;if(t==short.class)return Short.class;if(t==byte.class)return Byte.class;if(t==char.class)return Character.class;}return t;}
    private void fail(String message){throw new TypeCheckException(message);}
}
