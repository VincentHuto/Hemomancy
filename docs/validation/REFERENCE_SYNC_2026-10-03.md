# Lore and mechanics reference synchronization - 2026-10-03

## Baseline and scope

The latest substantial reference commit is `a4ab49edb` (2026-10-01), **feat: unify station upgrades and refine Harbinger progression**. It changed the mechanics reference by 152 additions/39 deletions and lore by 14 additions/4 deletions. It is current HEAD: there are no later commits to summarize. All subsequent work is local. The audit also checks the twelve commits after `917aa74aa` (2026-09-19), whose broad working-tree coverage index is the previous documented coverage baseline. The September 1 audit dates in the reference headers were stale metadata, not the date of their most recent substantial content change.

At entry, `git status --porcelain=v1 --untracked-files=all` listed 188 paths: 126 modified tracked files, three tracked deletions and 59 untracked files, with no staged changes. Whitespace-insensitive diff inspection separates substantive edits from line-ending-only churn. Untracked textures, test fixtures, dependency inputs and validation docs belong in the commit assessment even though ordinary `git diff --stat` omits them. This task edits documentation only and does not stage or commit anything.

## Committed history checked

| Commit / date | Work | Reference disposition |
|---|---|---|
| `9d106e490` / Sep 20 | Fargone variants and Dynasty Castle | Missing castle coverage and generic Fargone description corrected in mechanics and world wiki; lore records the content without inventing a polity or degree system. |
| `878d16ed3` / Sep 24 | Cortical Drift, Myelin Borers and Naeglerophaeon | Existing mechanics sections and lore sections 18-20 retain coverage; the current geometry resource move is added separately. |
| `623ca430e` / Sep 24 | Excoriated rename and spawning | Current Excoriated identity and Basin mechanics already documented. |
| `12f7769b9` / Sep 24 | Scriptorium stages | The current shared station contract supersedes the original separate blocks and bespoke upgrade ceremony. |
| `613d0ec53` / Sep 25 | Advanced brewing and Resonant Forge | Existing machine/recipe sections retain coverage; current staged apparatus uses the shared catalog/rite contract. |
| `0e08a5ef1` / Sep 25 | Item persistence, recipe codecs and event consolidation | Existing opening reference sections describe shared stack-backed inventories, registry-aware codec plumbing and reload publication; removed recipe caches are updated separately. |
| `57d777b7d` / Sep 26 | Guidebook rewrite and HutosLib Field Notes ownership | Existing discovery/Field Notes sections retain coverage; current book presentation and corrected rite discovery are added. |
| `c439856f2` / Sep 27 | Harbinger initiation, outposts and blood HUD | Existing onset, dormant oath, awakening and early degree sections retain coverage. |
| `80444dc12` / Sep 27 | Newcomer dialogue and NPC interactions | Existing early progression and teacher jurisdiction retain coverage. |
| `0dfe9ea60` / Sep 28 | Further newcomer guidance and NPC interactions | Existing NPC/referral and clinical teaching sections retain coverage. |
| `eba4a434c` / Sep 29 | Progression cleanup and texture revisions | Existing assignment/proof sections retain coverage; later runtime acceptance is qualified separately. |
| `a4ab49edb` / Oct 1 | Unified station upgrades, progression, staged visuals and Liber naming | Latest substantial reference baseline. Current `libersanguinium` identifiers remain canonical; historical art filenames and intentional migration strings are not renamed by this documentation task. |

## Local work and corrections

| Area | Current behavior / documentation correction | Inspected evidence |
|---|---|---|
| Centrifuge | Primary/secondary fractions share slots 10-17; whole-batch reservation and saved retry remain atomic. Old extra-bank outputs migrate with components and stage credit through a persistent recovery queue. The compact menu has 19 machine slots plus 36 player slots. | `VialCentrifugeBlockEntity`, menu/screen/startup status; [focused record](CENTRIFUGE_SHARED_OUTPUTS_2026-10-03.md). Existing reference and Harbinger wiki edits preserved. |
| Whale ecology | Living submerged squid/glow squid and Prism Cuttles are hunted within 16 blocks. A lethal whale bite drops one ambergris above the whale, retaining prey loot and independent bottle/shedding cooldown. Reef cuttle spawns and pickup discovery accompany teaching. | `MnemonicWhaleEntity`, tuning, reef biome tag, `LiberEntryDefinitions` and discovery event; [feeding record](MNEMONIC_WHALE_FEEDING.md). Existing resource/wiki/lore edits preserved. |
| Shared Fane | Eligible D5 members earn their own covenant proof from positive personal blood deposition at the usable heart inside its Fane. Nearby observers earn none. Blood-tool input routes to item use while pool-menu mastery remains separate. | `ConsecratedBloodwellBlock`, `MachineAccessEvents`, projection handler and loaded/member client evidence. Fane lore moved out of the Naeglerophaeon tail into the Fane section. |
| Scars | Scar Resonance adds purchased capacity to the shared degree limit; Effigy and dynamic pattern storage support seven IDs. The item renderer still has four overlay quadrants. | `ScarBrazierRite`, `ItemScarPattern`, `MasonsEffigyMenu`/screen and `ScarPatternItemRenderer`. Pending skill status, four-ID storage claims and base-only capacity wording corrected. |
| Endings and pomes | Ledger uses canonical `EnumArchonPath`, recognizes both completed endings and distinguishes pending Apotheos; the overall bar counts visible sections. Pome ownership applies on both sides and lifecycle sync repairs the count display. | Ledger packet/item/screen, `QliphothPomeItem`, `InitiatoryDegreeEvents`. No new ending or rank gate introduced. |
| Recipes and discovery | No static Scar or memory-weaving serializer cache; current recipe-manager lookup controls templates and Loom preview, including removed recipes and missing catalysts. Synthetic distillation recipes live only in GameTest resources. Historical Record uses `sanctified_rite`. | Both serializers, Scar Pattern lookup, Loom block entity/renderer, main/test resource paths and Liber entry definitions. |
| Rite input | Dead/spectator participants cannot inscribe assistance; empty-main-hand clicks suppress duplicate predicted offhand support. | `CardinalRiteAllyService`, `CardinalRiteInteractionHandler`. |
| Sampling | Current resources contain 138 entity profiles, 80 requiring the Living Syringe. Chitinite is restricted; Hemomancy Armadillo, Desiccant, Mortarbound and Naeglerophaeon allow ordinary vials. | Actual profile JSON count and five changed profiles; existing blood-profile wiki correction preserved. |
| Packaged assets | NeoForge loaders, complete Engram/Abocipher states, missing item sprites and texture paths, atlas/font/animation/shader metadata and geometry scan isolation corrected. Archives are retained in source and excluded from runtime; the font license stays packaged. | Model/resource diff, Naeglerophaeon model loader path and `build.gradle`; packaged acceptance evidence remains dated. |
| Dependencies and gates | CI hashes nine local JARs and the modified sibling HutosLib snapshot. Composite HutosLib is 7.4.0, fallback remains 7.3.5. MnA/Curios development dependencies exist while Hemomancy compat stays excluded. Packaged asset validation joins `alphaCheck`. | `build.gradle`, `settings.gradle`, manifest/verifier/workflow and [provisioning](../DEPENDENCY_PROVISIONING.md). Stale dependency-availability explanations corrected without enabling integration. |
| Review harness and documentation | Opt-in client operators, whale/centrifuge fixtures, repaired loaded assertions and priority acceptance records are development evidence. Immaculatus's washed search count uses shared book presentation. | GameTest source/resource diff, `SuccessionClientReview`, `CentrifugeClientReview`, `FixtureEntityRemovalProbe`, `HemomancyBookPresentation`, testing/agent docs and dated records. |

No MnA design was changed; the absent `MNA_COMPATIBILITY_BRAINSTORM.md` was not recreated. The active/dormant dependency distinction is corrected in the mechanics reference and developer wiki. Existing station, testing, agent and player-wiki edits are preserved. Alchemist station teaching now sits with clinical jurisdiction rather than under Naeglerophaeon. Canon governance and the amoral/found-family framing remain intact.

## Evidence boundaries

The priority repair record reports an earlier passing `alphaCheck build` with 2,423 JVM tests and 705 required GameTests. Later October 3 records report focused successes but red full JVM runs: centrifuge records three failures; whale records four, including source/resource text encoding and the legacy Sporitic Thurible assertion. These are different snapshots. Do not combine the earlier green gate with later focused successes into a claim that current HEAD plus all local changes passes the full gate.

The existing records distinguish supplied scenes and memberships, loaded fixtures, orderly logout, flushed-checkpoint recovery and packaged smoke tests from natural Survival progression, partial-write/crash safety, wider multiplayer ownership/reward cases, locale/display coverage and encounter balance. This synchronization adds no runtime acceptance of its own.

## Fresh documentation verification

- Java 21 source-file execution passed all four existing contracts: `LoreCanonAuthoritySourceTest`, `SettledLoreSynchronizationSourceTest`, `SupportingLoreCanonSourceTest` and `WikiLoreDownstreamSourceTest`. Command pattern: `java src/test/java/com/vincenthuto/hemomancy/common/lore/<test>.java`. These check settled canon and downstream wording against current files without rebuilding or modifying live development classes.
- All 19 added local Markdown file links resolve across the two references, three wiki pages and this audit. All six files decode as UTF-8; added lines have no whitespace-only lines.
- `git diff --check -- docs/HEMOMANCY_REFERENCE.md docs/LORE_REFERENCE.md wiki/Developer-Reference.md wiki/Lore-and-Story.md wiki/World-Content.md` passed. The new untracked audit was checked directly for whitespace.
- The final documentation scope is six files. Current status is 191 dirty paths (128 modified, three deleted, 60 untracked), with zero staged paths. This task added two modified wiki paths and the new audit; the other documentation paths were already dirty.
- Full Gradle build/JVM/GameTest/client suites were not rerun for this documentation-only refresh. The historical full-suite failures above remain reported rather than treated as repaired.
