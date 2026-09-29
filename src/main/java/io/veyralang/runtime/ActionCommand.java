package io.veyralang.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A validated request for the host to perform a business action. */
public record ActionCommand(String name, List<Object> arguments) {
    public ActionCommand { arguments = Collections.unmodifiableList(new ArrayList<>(arguments)); }
}
