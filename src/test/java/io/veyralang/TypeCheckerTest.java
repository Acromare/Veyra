package io.veyralang;

import io.veyralang.runtime.HostContext;
import io.veyralang.typecheck.TypeCheckException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TypeCheckerTest {
    record Order(double amount) {}
    record User(boolean vip) {}
    private static final HostContext HOST = HostContext.of(Order.class, User.class);
    @Test void rejectsMisspelledPropertyBeforeCall() {
        String source = "function calc(order: Order): double\n    return order.amout";
        assertThrows(TypeCheckException.class, () -> Veyra.compile(source, HOST));
    }
    @Test void rejectsNonBooleanCondition() {
        String source = "function calc(order: Order): double\n    if order.amount\n        return 1\n    return 0";
        assertThrows(TypeCheckException.class, () -> Veyra.compile(source, HOST));
    }

    @Test void rejectsFunctionWithoutReturnOnEveryPath() {
        String source = "function price(order: Order): double\n    if order.amount > 0\n        return order.amount";
        assertThrows(TypeCheckException.class, () -> Veyra.compile(source, HOST));
    }
}
