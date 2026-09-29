package io.veyralang;

import io.veyralang.runtime.HostContext;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NullAndInterpolationTest {
    record User(String name) {}
    @Test void supportsInterpolation() {
        var fn = Veyra.compile("function main(user: User): string\n    return \"Hello ${user.name}\"", HostContext.of(User.class));
        assertEquals("Hello Ada", fn.call(new User("Ada")));
    }
}
