package io.veyralang;

import io.veyralang.runtime.HostContext;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FunctionCallTest {
    @Test void callsAnotherFunction() {
        String source = "function discount(): double\n    return 0.8\n\nfunction main(): double\n    return 100 * discount()";
        var fn = Veyra.compile(source, HostContext.builder().build(), "main");
        assertEquals(80.0, (Double) fn.call(), 0.001);
    }
}
