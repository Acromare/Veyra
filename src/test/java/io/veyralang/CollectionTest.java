package io.veyralang;

import io.veyralang.runtime.HostContext;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CollectionTest {
    @Test void supportsReadOnlyListLiteralIndexSizeAndContains() {
        String source="function main(): double\n"+
                "    let prices = [12, 30, 45]\n"+
                "    if prices.contains(30) && prices.size == 3\n"+
                "        return prices[0] + prices[1]\n"+
                "    return 0";
        var fn=Veyra.compile(source, HostContext.builder().build());
        assertEquals(42.0,(Double)fn.call(),0.001);
    }
}
