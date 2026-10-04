# Centrifuge shared output correction — 2026-10-03

Primary and secondary fractions now share inventory slots 10–17. The machine has twenty inventory slots and the menu has nineteen machine slots plus thirty-six player slots. The compact 176-pixel screen removes the extra bank, divider, D6 label and bank tooltips. Both fraction types merge by item and components before using empty slots in ascending order.

The complete batch reserves output capacity together and commits only after validation. Existing yield, probability, duration, powder, vial return, blood income and eligibility rules are retained. Pending results survive reload and blocked-completion retry without rerolling. Extraction credit follows the destination slot and saved processing stage.

Old slots 20–27 migrate into the original outputs with exact components, counts and remaining stage credit. Overflow persists under `LegacyRecovery` and `LegacyRecoveryQuantities`, drains as output space opens, and blocks new spins and ordinary upgrade rites until recovered. Breaking drops any queued remainder exactly once. The old secondary-blocked status ordinal remains readable for saved status compatibility, with shared-output wording.

## Verification

- A new loaded regression failed against the original implementation with `centrifuge still has a second output bank` (`build/centrifuge-red.log`).
- `gradlew.bat test --tests '*VialCentrifuge*' runStationUpgradeGameTestServer --console=plain` passed: four focused JVM tests and all forty required station-upgrade GameTests (`build/centrifuge-final-check.log`). Coverage includes eight mixed samples, combined capacity exhaustion, matching-stack preference, component separation, atomic completion/retry, pending-batch reload, primary/secondary recovery, partial shift-click stage credit, ordinary menu extraction of migrated items, automation removal, legacy overflow reload, and exact overflow drops.
- `gradlew.bat test --console=plain` ran 2,423 JVM tests: 2,420 passed. Remaining failures outside this correction: `GameTestHarnessSourceContractTest.isolatedJourneyClientRunAndOperatorGuideRemainAvailable`, `GameTestHarnessSourceContractTest.devOnlyHarnessProvidesCommandsAndEarlyHarbingerScenarios`, and `MorphlingLumenlaceRenameResourceTest` through `LegacyMainTestAdapterTest` (`build/centrifuge-validation.log`).
- Updated JSON resources parse successfully; the focused source diff has no whitespace errors.

## Client review

The dev-only `CentrifugeClientReview` operator uses `hemomancy.centrifugeReview=true` and requires a game directory named `centrifuge-client-review`. It places each stage in a disposable local world, waits for the block entity to reach the client, opens the production menu, and captures each screen. It is not part of release jars.

The launch configuration is retained in `build/centrifuge-client.init.gradle`; launch with `gradlew.bat -I build/centrifuge-client.init.gradle runCentrifugeReviewClient`. The review world is a copy in `build/centrifuge-client-review/saves/CentrifugeReview`; the source disposable world was not modified. Screenshots are `build/centrifuge-client-review/screenshots/centrifuge-stage-0.png`, `centrifuge-stage-1.png`, and `centrifuge-stage-2.png`; final logs are in `build/centrifuge-client-compact.log`.

All three final screenshots were inspected at 1280×900: each shows the same single eight-slot fraction grid, compact panel, centered inventory, and no second-bank controls. The client logged fifty-five total menu slots at each stage and shut down successfully.

This is assisted native menu review and focused loaded behavior evidence. It does not establish unaided Survival pacing or multiplayer visual acceptance. Concurrent development launchers briefly rewrote the same HutosLib jar during an earlier run; the serial validation above passed after that launcher race was removed.
