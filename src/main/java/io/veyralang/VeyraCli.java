package io.veyralang;

import io.veyralang.runtime.HostContext;
import java.nio.file.Path;

/** Minimal standalone command line entry point for .veyra files. */
public final class VeyraCli {
    private VeyraCli() {}

    public static void main(String[] args) {
        if (args.length < 2 || args.length > 4 || !(args[1].endsWith(".veyra"))) {
            System.err.println("用法: veyra <check|run> <file.veyra> [--entry name]");
            System.exit(2);
        }
        try {
            Path file = Path.of(args[1]);
            String entry = null;
            if (args.length == 4 && args[2].equals("--entry")) entry = args[3];
            switch (args[0]) {
                case "check" -> {
                    Veyra.compile(file, HostContext.builder().build(), entry);
                    System.out.println("OK: " + file);
                }
                case "run" -> {
                    var result = Veyra.compile(file, HostContext.builder().build(), entry).execute();
                    if (result.value() != null) System.out.println(result.value());
                    result.commands().forEach(command -> System.out.println("EMIT " + command.name() + " " + command.arguments()));
                }
                default -> {
                    System.err.println("未知命令: " + args[0]);
                    System.exit(2);
                }
            }
        } catch (Exception e) {
            System.err.println("Veyra 错误: " + e.getMessage());
            System.exit(1);
        }
    }
}
