package io.veyralang.runtime;

public interface CompiledFunction {
    Object call(Object... arguments);
    ScriptResult execute(Object... arguments);
}
