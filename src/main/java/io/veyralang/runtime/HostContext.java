package io.veyralang.runtime;

import java.util.LinkedHashMap;
import java.util.Map;

public final class HostContext {
    private final Map<String, Class<?>> types;
    private final Map<String, ActionSpec> actions;
    private HostContext(Map<String, Class<?>> types, Map<String, ActionSpec> actions) { this.types = Map.copyOf(types); this.actions = Map.copyOf(actions); }
    public static Builder builder() { return new Builder(); }
    public static HostContext of(Class<?>... classes) { Builder b=builder(); for(Class<?> c:classes)b.allowType(c.getSimpleName(),c); return b.build(); }
    public Class<?> type(String name) { return types.get(name); }
    public ActionSpec action(String name) { return actions.get(name); }
    public static final class ActionSpec {
        private final Class<?>[] parameterTypes;
        public ActionSpec(Class<?>... parameterTypes) { this.parameterTypes = parameterTypes.clone(); }
        public Class<?>[] parameterTypes() { return parameterTypes.clone(); }
    }
    public static final class Builder {
        private final Map<String,Class<?>> types=new LinkedHashMap<>();
        private final Map<String,ActionSpec> actions=new LinkedHashMap<>();
        public Builder allowType(String name,Class<?> type){types.put(name,type);return this;}
        public Builder allowAction(String name,Class<?>... parameterTypes){actions.put(name,new ActionSpec(parameterTypes));return this;}
        public HostContext build(){return new HostContext(types, actions);}
    }
}
