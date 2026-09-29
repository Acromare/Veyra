# Standalone Scripts

Veyra source files use the `.veyra` extension. A file contains one top-level
`function` or `rule` entry point and uses indentation for blocks.

## Check a file

```powershell
java -jar build/libs/veyra-1.0-SNAPSHOT.jar check examples/pure.veyra
```

## Run a pure script

```powershell
java -jar build/libs/veyra-1.0-SNAPSHOT.jar run examples/pure.veyra
```

A file may contain multiple functions. Select one explicitly with `--entry`:

```powershell
.\gradlew.bat run --args="run examples/multiple.veyra --entry discount"
```

Scripts that access Java host objects still need an embedding application to
register those types and pass the runtime arguments. The same `.veyra` file is
used in both workflows; embedding is an execution environment, not a different
language mode.
