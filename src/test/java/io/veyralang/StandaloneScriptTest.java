package io.veyralang;

import io.veyralang.runtime.HostContext;
import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.assertEquals;

class StandaloneScriptTest {
    @Test void compilesAndRunsFile() throws Exception {
        var fn = Veyra.compile(Path.of("examples", "pure.veyra"), HostContext.builder().build());
        assertEquals(42.0, (Double) fn.call(), 0.001);
    }
}
