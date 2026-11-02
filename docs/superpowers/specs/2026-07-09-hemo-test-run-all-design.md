# `/hemo test run_all` design

## Goal

Run every registered development test scenario from one in-game command without requiring the operator to invoke each scenario separately.

## Command behavior

`/hemo test run_all` requires the same operator permission and player command source as the existing `setup`, `verify`, and `run` commands. It first clears an active fixture, then iterates `HemoTestScenarioCatalog.all()` in its declared order. For each scenario it runs setup, verification, and cleanup in a `finally` block before moving to the next scenario.

The command does not stop after a failed verification or setup exception. It records each failed id and continues. It prints a result line for every scenario and a final `passed/total` summary. The Brigadier result is `1` only when every scenario passes and `0` otherwise, allowing command blocks/functions to treat any failure as an error.

## State safety

Only one scenario is active at a time. The active marker is set for the scenario being processed and removed after cleanup, including on an exception. Existing scenario cleanup remains authoritative for restoration of its own player state.

## Testing

Extend the source contract to require the `run_all` command and a `runAll` implementation. Add a pure package-visible execution helper accepting a player and result sink so JUnit can prove it continues after a failure, cleans each scenario, and returns failure when any scenario fails. The command delegates to that helper.
