# Consolidation Progress Ledger

Every consolidation session reads this file first and updates it last, following
[CONSOLIDATION_SESSION_RULES](CONSOLIDATION_SESSION_RULES.md).

## Step map

| Step | Roadmap phase(s) | Status | Commit(s) | Date |
|---|---|---|---|---|
| 1 | Urgent defect fixes (F-1, F-2, F-3) | NOT STARTED | | |
| 2 | Phase 0: green baseline and test infrastructure | NOT STARTED | | |
| 3 | Phase 1: verified dead-code removal and packaging | NOT STARTED | | |
| 4 | Phase 2: network hygiene | NOT STARTED | | |
| 5 | Phase 3: player-state plumbing | NOT STARTED | | |
| 6 | Phase 4: shared small utilities | NOT STARTED | | |
| 7 | Phase 5: blood accounting | NOT STARTED | | |
| 8 | Phase 7: registration and client registration | NOT STARTED | | |
| 9 | Phase 6: combat targeting and damage attribution | NOT STARTED | | |
| 10 | Phases 8 + 9: rendering plumbing and animation conventions | NOT STARTED | | |
| 11 | Phase 10: god-class decomposition | NOT STARTED | | |
| 12 | Phase 11: persistence authority migrations | NOT STARTED | | |
| 13 | Phases 12 + 13: content, worldgen, data, tooling, standards | NOT STARTED | | |

Statuses: `NOT STARTED`, `IN PROGRESS`, `DONE`, `DONE (uncommitted)`, `BLOCKED`.

## Baseline (2026-10-21, commit `3fae723d0`)

- **Compile:** pass.
- **`test`:** 3,908 tests, 20 failed. These 20 failures are the accepted baseline until
  step 2 clears them:
  - `HematicArmatureModelSourceTest` ×2
  - `VialCentrifugeModelSourceTest` ×2
  - `MnemonicReliquaryModelSourceTest`
  - `CardinalRitePlantedStaffModelTest`
  - `PhlegethonticBombardierResourceTest`
  - `BloodProfileMigrationTest`
  - `LivingSickleFeatureSourceTest`
  - `ClientModelTextureCoverageTest`
  - `EnglishLocalizationEncodingTest`
  - `GameTestHarnessSourceContractTest` ×2
  - `LegacyMainTestAdapterTest`, covering `ModelFirstTileRendererResourceTest`,
    `MorphlingLumenlaceRenameResourceTest`, `VestmentOfTheFinalMoltResourceTest`,
    `ArmorPreparationResourceTest`, `UtilityPolishResourceTest` and
    `LivingWeaponGraftRiteSourceTest`
  - `LegacyMainTestSuiteContractTest`
- **GameTests, `alphaCheck`, server and client start:** not run at baseline.
- **Full log:** [evidence/08-baseline-test.log](evidence/08-baseline-test.log).

## Decisions

The owner fills in the **Decision** column. A session may implement an item only when it
reads `APPROVED`. Any other value means the session leaves the item alone and lists it
under *Pending approvals*. IDs refer to [ARCHITECTURE_AUDIT](ARCHITECTURE_AUDIT.md).

| ID | Item | Step | Decision | Notes |
|---|---|---|---|---|
| F-1 | Reject non-finite or non-positive blood donations | 1 | PENDING | Step 1 prompt approves |
| F-2 | Register the 4 client-only bidirectional payloads as server-to-client only | 1 | PENDING | Step 1 prompt approves |
| F-3 | Remove the client-class call from `WillEntity` | 1 | PENDING | Step 1 prompt approves |
| M-1 | Hand-edited models vs their generators (Armature, Centrifuge, Reliquary, loom) | 2 | PENDING | Regenerate / accept edits, per model |
| M-2 | Five unregistered mobs (Abyssal Siphon, Cruor Fiend, Frozen Clot, Synapse Hound, Void Drinker) | 3 | PENDING | Delete / archive branch / keep |
| M-3 | Other "REQUIRES VERIFICATION" dead code (audit section E) | 3 | PENDING | Per item |
| F-6 | Ledger actions require the ledger item | 4 | PENDING | |
| F-7 | Bloodline chat rate limit | 4 | PENDING | Design |
| F-8 | Reject NaN pool settings; clamp client partial tick | 4 | PENDING | |
| F-9 | Structure paste requires operator permission and the held spawner | 4 | PENDING | |
| F-4 | ScaleGrip: respect cancelled deaths; persist pending returns | 5 | PENDING | |
| F-5 | OrganEcho clears the correct slot | 5 | PENDING | |
| F-19 | Station kits follow the delivery contract (alive, not observational, no Creative discard) | 5 | PENDING | |
| F-20 | Resync Ichorian knowledge on dimension change | 5 | PENDING | |
| F-12 | Projected blood honours the rite lock | 7 | PENDING | |
| F-13 | Scar upkeep becomes reserve-protected and synced | 7 | PENDING | Balance |
| F-14 | Passive scar effects survive milk | 7 | PENDING | Design |
| F-15 | Living weapons check `isActive()` and stop wiping the reserve | 7 | PENDING | Balance |
| F-22 | Hide the legacy Dictation Table from the main creative tab | 8 | PENDING | |
| G-1 / F-18 | Explicit damage stages and one death-save stage | 9 | PENDING | Balance: blood lost per hit changes |
| F-16 | Attribute AoE damage from armour, Still Arts, Sheolic and Saprovitta; respect allies and PvP | 9 | PENDING | Balance |
| F-17 | Curved Horn uses `LastRiteHelper` | 9 | PENDING | |
| G-2b | The caster's own puppets stop matching `Monster.class` filters | 9 | PENDING | |
| A-4 | Animations that would freeze under `/tick freeze` | 10 | PENDING | Per animation |
| V-3b | Ferric quad-encoder constant (128 vs 127) | 10 | PENDING | Visual check |
| F-11 | Blood-structure crafts tick once per server tick | 11 | PENDING | Crafts take their intended time |
| S-9 | Per-player `dataVersion` and gated one-shot migrations | 12 | PENDING | Save-critical |
| F-21 | Archon choice: attachment is the only authority | 12 | PENDING | Save-critical |
| S-6c | Circus progress moved into one attachment | 12 | PENDING | Save-critical |
| S-1c | Chamber access and `PendingWhispers` moved into attachments | 12 | PENDING | Save-critical |
| S-7 | Two-tier cooldown convention | 12 | PENDING | Keep each entry's tier |
| O-5 | Typed components for reworked items | 12 | PENDING | Save-critical |
| F-23 | Move tag files to singular folders (the tags start working) | 13 | PENDING | |
| F-24 | Three template-less structures and the empty mausoleum tag | 13 | PENDING | Finish / remove |
| F-25 | Register or remove the MnA config read | 13 | PENDING | |
| F-26 | Chthonian Queen build name | 13 | PENDING | Check data-fixer impact |
| F-27 | `HemolysisEffect` category consistency | 13 | PENDING | |
| T-6b | Prune unreferenced `libs/` jars; move dev-only dependencies to `localRuntime` | 13 | PENDING | |

## Guardrail tests

Each step lists the regression and guardrail tests it adds. Every later step must keep
them green.

| Test | Added in step | Protects |
|---|---|---|
| | | |

## Step entries

<!-- Append one entry per step, copying this template:

### Step N: <name>
- Status / date / commits:
- Test counts before → after:
- Metrics (classes and lines removed and added, call sites migrated, remaining legacy instances):
- Guardrails added:
- Approved items implemented:
- Pending approvals:
- Deviations from the roadmap:
- Not verified (needs in-game or visual check):
- Handoff notes for the next step:
-->
