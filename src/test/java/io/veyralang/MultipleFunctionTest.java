package io.veyralang;

import io.veyralang.runtime.HostContext;
import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MultipleFunctionTest {
    @Test void selectsEntryFunction() throws Exception {
        var fn = Veyra.compile(Path.of("examples", "multiple.veyra"), HostContext.builder().build(), "discount");
        assertEquals(0.8, (Double) fn.call(), 0.001);
    }
}
