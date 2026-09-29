# Business scripts and action plans

Veyra scripts calculate decisions from host-provided data. They do not directly
invoke arbitrary Java methods or perform I/O. Side effects are represented as
`emit` commands and returned to the host as a `ScriptResult`.

```veyra
function priceDecision(order: Order, customer: Customer): double
    let discount = 1.0
    if customer.vip && order.amount >= 1000
        discount = 0.8
        emit applyDiscount(order.id, discount)
    return order.amount * discount
```

The host explicitly registers types and permitted commands:

```java
var host = HostContext.builder()
    .allowType("Order", Order.class)
    .allowType("Customer", Customer.class)
    .allowAction("applyDiscount", String.class, double.class)
    .build();

var decision = Veyra.compile(source, host).execute(order, customer);
// Host validates/commits decision.commands(); Veyra never executes the action.
```

`emit` actions are checked at compile time against the host allowlist, including
argument count and types. Runtime limits cap call depth at 64, executed
statements at 100,000, and emitted commands at 1,000 per execution. These are
initial safety limits and can be made configurable in a later API iteration.

Read-only list literals are also supported:

```veyra
let thresholds = [100, 500, 1000]
if thresholds.contains(order.amount) && thresholds.size > 0
    return thresholds[0]
```

Lists support indexing, `.size`, and `.contains(value)`. Script-created lists
are immutable; mutation methods and arbitrary collection method calls are not
exposed.
