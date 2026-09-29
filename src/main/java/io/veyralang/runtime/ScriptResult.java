package io.veyralang.runtime;

import java.util.List;

/** Pure decision output; the host decides whether and how to execute commands. */
public record ScriptResult(Object value, List<ActionCommand> commands) {
    public ScriptResult { commands = List.copyOf(commands); }
}
