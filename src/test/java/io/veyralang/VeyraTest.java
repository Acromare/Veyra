package io.veyralang;

import io.veyralang.runtime.HostContext;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import io.veyralang.typecheck.TypeCheckException;

class VeyraTest {
    record Order(double amount) {}
    record User(boolean vip) {}
    @Test void executesBusinessRule() {
        String s="function calc(order: Order, user: User): double\n"+
                "    let base = order.amount\n"+
                "    if user.vip && base > 1000\n"+
                "        return base * 0.8\n"+
                "    return base";
        var fn=Veyra.compile(s,HostContext.of(Order.class,User.class));
        assertEquals(960.0,(Double)fn.call(new Order(1200),new User(true)),0.001);
        assertEquals(800.0,(Double)fn.call(new Order(800),new User(true)),0.001);
    }

    @Test void returnsBusinessActionPlanWithoutExecutingIt() {
        String source = "function decide(order: Order, user: User): double\n"+
                "    if user.vip\n"+
                "        emit applyDiscount(order.amount, 0.8)\n"+
                "    return order.amount";
        var host = HostContext.builder().allowType("Order", Order.class).allowType("User", User.class)
                .allowAction("applyDiscount", double.class, double.class).build();
        var result = Veyra.compile(source, host).execute(new Order(1200), new User(true));
        assertEquals(1200.0, (Double) result.value(), 0.001);
        assertEquals("applyDiscount", result.commands().get(0).name());
        assertEquals(2, result.commands().get(0).arguments().size());
    }

    @Test void refusesActionsNotGrantedByTheHost() {
        String source = "function decide(): double\n    emit sendEmail(\"x\")\n    return 1";
        assertThrows(TypeCheckException.class, () -> Veyra.compile(source, HostContext.builder().build()));
    }

    @Test void checksPeerFunctionSignatures() {
        String source = "function helper(amount: double): double\n    return amount\n\nfunction main(): double\n    return helper(true)";
        assertThrows(TypeCheckException.class, () -> Veyra.compile(source, HostContext.builder().build(), "main"));
    }
}
