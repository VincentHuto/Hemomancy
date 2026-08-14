# Hemomancy Alpha Readiness Analysis & Roadmap

## Context

VincentHuto's Hemomancy is a blood-magic and fungal-horror Minecraft NeoForge mod (Minecraft 1.21.1, NeoForge 21.1.x, Java 21) with two mutually exclusive player paths (Harbinger and Unstained). This roadmap assesses its current state against the requirements for a strong Minecraft mod and sets priorities for the alpha release.

> 2026-05-17 refresh: the current code is ahead of several older audit notes below. Treat `HEMOMANCY_REFERENCE.md` as the canonical status doc. Confirmed cleanup since the original roadmap includes 1.21 singular resource paths, wired skill effects, implemented Hematic Salvage tests/rules, blood crystal growth from alembic leaks, Annetta Broken Church spawning, dedicated Annetta encounter models/textures, JEI registration for Mycelial Lantern enzyme fruiting, and an alpha building fixture pass with vanilla-behavior chains, bars, walls, hematic iron door/trapdoor, recipes, loot tables, and resource coverage tests.

The mod is approximately **85-90% feature-complete**, with both core paths playable end-to-end. WIP areas are clearly documented in `HEMOMANCY_REFERENCE.md`. The gaps are primarily in **onboarding clarity**, **boss visual assets**, and **a few endgame systems**.

---

## What Makes a Good Minecraft Mod Good (Benchmark Framework)

Used for gap analysis below. Ordered from most to least impactful on alpha reception:

| # | Criterion | Why It Matters |
|---|-----------|----------------|
| 1 | **Zero progression blockers** | Players must be able to complete the core loop |
| 2 | **Smooth first hour** | Mods live or die on the initial experience |
| 3 | **In-game recipe discoverability** (JEI) | Players will not look at wikis for alpha testing |
| 4 | **Functional guidebook** | Replaces a wiki; essential for complex systems |
| 5 | **Consistent visual identity** | Placeholder models destroy immersion immediately |
| 6 | **Audio feedback** | Makes abilities feel weighty |
| 7 | **Clear "what next" signals** | Advancements, NPC dialogue, book hints |
| 8 | **Performance (no crashes/lag)** | Alpha testers will have low tolerance |
| 9 | **Balance** | Power curve should feel rewarding but not trivial |
| 10 | **Lore depth without mandatory reading** | World should feel lived-in at a glance |

---

## Current State vs. Benchmark

### STRENGTHS (Alpha-Ready)

**1. Zero progression blockers — PASSES**
- Harbinger: Full 8-degree path (Neophyte → Apotheos) is implemented with all rites, manipulations, bloodlines, scars, and skill trees.
- Unstained: Full 5-stage purity path (Corrupted → Purified → Clarity) with Still Arts is implemented.
- Both paths are mutually exclusive and properly gate each other.
- 40 blood manipulations all have `getAction()` implementations.
- 375 JSON recipes covering all crafting progressions and the alpha building/decorative fixture set.

**2. In-game recipe discoverability — LARGELY PASSES**
- Custom JEI categories exist for Chisel Station, Visceral Recaller, Blood Structure, and Morphling Incubator (`compat/jei/`).
- 375 recipes cover all paths and the alpha building/decorative fixture set.
- *Gap: JEI categories need audit to confirm all custom machine recipes surface correctly.*

**3. Functional guidebook — MOSTLY PASSES**
- Liber Sanguinum (Harbinger) and Liber Immaculatus (Unstained) both use HutosLib JSON framework.
- Books have degree-gated pages that unlock per progression.
- *Gap: The `HemoProgressionScreen.setupEntries()` is commented out; the Java renderer is WIP, but the HutosLib JSON reader works. Players can read it — but the Harbinger-side progress screen tabs may not fully surface.*

**4. Content density — PASSES**
- 323+ items, 127 registered block declarations, and 87 entities (including 4 bosses, 5 NPCs, 10+ monsters).
- 12 morphling variants, 20+ scar types, 9 fungal scar cultivars.
- 8 blood tendency trees + Unstained Still Arts.
- Qliphoth Bloom, Founding Fane, Bloodline pool system — all functional.

**5. Worldgen — PASSES**
- 3 Harbinger biomes (Fungal Gardens, Fungal Isles, Hemorrhagic Plateau) + Unstained equivalents via TerraBlender.
- Blood Temple, Harbinger Outpost, and Unstained Church. All Saint Trial Chambers, including Hemorath, are post-release WIP.
- Fungal Dimension accessible via Fungal Spine item.

**6. Lore depth — PASSES**
- The moral-ambiguity framework (Harbinger ≠ villain, Unstained ≠ hero, Fungal Entity ≠ evil) is coherently executed.
- NPC dialogue trees with 8-degree gating for Vicar, full purity-stage gating for Zealot/Acolyte.
- Fungal Whisper events (Archon+) add eldritch depth.
- 51 item inquiry dialogue entries for contextual lore.

---

### GAPS (Must Fix Before Alpha)

#### CRITICAL — Blocks Alpha Release

**G1. Annetta Visual Polish (Saint visuals deferred)**
- **Issue**: Seraphae, Putriciel, and Velorum have full AI and combat mechanics but placeholder or unfinished presentation. Annetta Knowles now has a Broken Church trigger plus dedicated Java models/textures, but her final animations, Sanguis Lancea rendering, and Phase 1 combat polish remain WIP.
- **Impact**: Annetta remains a first-release polish concern. Hemorath, Seraphae, Putriciel, and Velorum are reserved together for the first major post-release Saints update and are not alpha blockers.
- **Files**:
    - `src/main/java/com/vincenthuto/hemomancy/common/entity/boss/SeraphaeEntity.java`
    - `src/main/java/com/vincenthuto/hemomancy/common/entity/boss/PutricielEntity.java`
    - `src/main/java/com/vincenthuto/hemomancy/common/entity/boss/VelorumEntity.java`
    - `src/main/java/com/vincenthuto/hemomancy/common/entity/boss/AnnettaKnowlesEntity.java`
    - `src/main/resources/assets/hemomancy/textures/entity/` (missing textures for above)
- **Resolution**: Either create proper models + GeckoLib animations, or explicitly label these saints as "in development" in the Liber and disable their trial chamber spawning until ready.

**First Major Post-release Content Update — The Saints**
- **Issue**: Hemorath's trial prototype (blood basin puzzle, sarcophagus, boss fight) is substantially implemented, while Seraphae, Putriciel, and Velorum have boss foundations without complete bespoke chambers or tuned world placement. Releasing Hemorath alone would fragment the system's introduction.
- **Impact**: These encounters must remain outside ordinary first-release placement; their absence is intentional expansion scope.
- **Files**:
    - `src/main/resources/data/hemomancy/structure/` (saint trial NBTs)
    - `src/main/java/com/vincenthuto/hemomancy/common/worldgen/` (placement configs)
- **Resolution**: Keep all four Saints behind the WIP boundary. Ship their bespoke rooms, environmental stories, victory conditions, rewards, balance, and final presentation together in the first major post-release content update.

**G3. New Player Onboarding — First-Hour Clarity**
- **Issue**: No "Welcome to Hemomancy" guidance. Players finding their first Blood Temple have no hint of what to do. The Hermit dialogue starts abruptly without context. Breadcrumbs to Harbinger Outpost don't exist. The Unstained Church discovery path is unclear.
- **Impact**: Most alpha testers will quit in the first 10 minutes without guidance.
- **Files**:
    - `src/main/resources/data/hemomancy/advancement/` (advancement chain)
    - `src/main/java/com/vincenthuto/hemomancy/common/event/CommonEvents.java`
    - `src/main/java/com/vincenthuto/hemomancy/client/screen/` (any tutorial overlays)
- **Resolution**:
    1. Audit advancement JSON chain — confirm degree_1 through degree_8 all fire and display correctly.
    2. Add a "root" advancement that fires on first world join and points players toward exploring.
    3. Add a Hermit interaction advancement that explains the choice to be made.
    4. Ensure Liber Sanguinum opens automatically on achieving Degree 1.

**G4. Advancements Folder Audit**
- **Issue**: The exploration agent noted the advancements directory structure may not be complete. `HarbingerAdvancementGranter` references degree advancements but their presence in the resource pack needs verification.
- **Impact**: If advancement JSON files are missing or malformed, the entire progression signposting system silently fails.
- **Files**: `src/main/resources/data/hemomancy/advancement/`
- **Resolution**: Run `./gradlew runData` and inspect generated output; or manually verify each referenced advancement JSON exists and is valid.

---

#### SIGNIFICANT — Should Fix Before Alpha

**G5. Liber Sanguinum Accessibility**
- **Issue**: The Harbinger progress screen's `setupEntries()` is commented out. It's unclear whether the in-game guidebook is easily openable (keybind, auto-open on degree gain) or if players must manually right-click the item.
- **Files**:
    - `src/main/java/com/vincenthuto/hemomancy/client/screen/HemoProgressionScreen.java`
    - `src/main/java/com/vincenthuto/hemomancy/client/ClientModBusEvents.java` (keybind registration)
- **Resolution**: Verify there's a keybind or right-click-item path to open the Liber. If `setupEntries()` being commented blocks content display, uncomment it or route to the HutosLib JSON reader fallback.

**G6. Fungal Dimension Polish**
- **Issue**: The dimension is accessible via Fungal Spine (Votary+), mobs spawn, and the Fungal Podium choice fork (Silent vs 8th Degree) works. But terrain feature population depth is thin and the dimension doesn't match its lore description (vast meatball, hyphae tendrils, alien aesthetics).
- **Impact**: Endgame players reaching this will find it underwhelming vs. expectation set by lore.
- **Files**: `src/main/java/com/vincenthuto/hemomancy/common/worldgen/` (fungal dimension features)
- **Resolution**: Add at minimum 3-4 biome feature variants (fungal trees, hyphae columns, meat-terrain surface variety) before alpha. The choice fork and mob spawning already working is a strong base.

**G7. Annetta Knowles Encounter Polish**
- **Issue**: Her two-route encounter now spawns through `BrokenChurchStructure`, but final presentation and Phase 1 identity still need polish before being treated as final content.
- **Files**: `src/main/java/com/vincenthuto/hemomancy/common/entity/boss/AnnettaKnowlesEntity.java`
- **Resolution**: Keep the Broken Church flow available for alpha testers, but label it partial and avoid presenting it as final boss content until animation/projectile polish lands.

---

#### POLISH — Nice to Have for Alpha

**G8. Blood Moon Ritual Trigger**
- The random Blood Moon works. A player-triggered ritual is designed but unimplemented. Low priority for alpha.

**G9. Manipulation Rank Forcing**
- Rank system exists (HUMILIS → SUMMA) but no ritual to force rank advancement. The auto-rank-up mechanic is sufficient for alpha.

**G10. Skill Wiring Audit**
- `HEMOMANCY_REFERENCE.md` §9 lists 4 skills (Iron Will, Scar Affinity, Scar Resonance, Scar Mastery) with no event-handler callers. Players can spend points in them but they do nothing. Should be flagged in UI or wired.
- **File**: `src/main/java/com/vincenthuto/hemomancy/common/capability/player/skillpoint/SkillPointHelper.java`

**G11. Morphling System Tutorial**
- The morphling crafting pipeline (Living Syringe → blood sample → Incubator → cultivate organism → Morphling Cradle → equip) is functional but there's no in-game explanation. A Liber page or advancement chain covering morphlings would close this gap.

---

## Prioritized Alpha Roadmap

### Phase A — Blocker Fixes (Must ship before any alpha)

| Priority | Task | Effort |
|----------|------|--------|
| A1 | Audit + fix advancement JSON chain (verify all degree_1-8 fire) | Low |
| A2 | Confirm Liber Sanguinum opens correctly for new players | Low-Medium |
| A3 | Add "root" onboarding advancement + first Blood Temple hint | Medium |
| A4 | Keep every Saint trial, including Hemorath, out of first-release placement | Settled: post-release Saints update |
| A5 | Keep all final Saint presentation and rewards out of first-release scope | Settled: post-release Saints update |

### Phase B — Pre-Alpha Polish (Ship with alpha if possible)

| Priority | Task | Effort |
|----------|------|--------|
| B1 | Re-audit skill UI wording now that all 21 skills have event/effect wiring | Low |
| B2 | Add Morphling system page to Liber Sanguinum | Low |
| B3 | Fungal Dimension: add 3-4 terrain feature variants | Medium-High |
| B4 | Annetta Knowles: define spawn condition or disable | Low (disable) |
| B5 | JEI category audit: verify all custom machines show recipes correctly | Low-Medium |

### Phase C — Post-Alpha (Next minor version)

| Priority | Task | Effort |
|----------|------|--------|
| C1 | Complete all Saint models + GeckoLib animations in the Saints update | Post-release |
| C2 | Finish and jointly release all four Saint trial chambers in the Saints update | Post-release |
| C3 | Blood Moon ritual trigger | Medium |
| C4 | Manipulation rank forcing ritual | Medium |
| C5 | MnA + Curios compatibility (pending upstream builds) | Medium (once deps exist) |

---

## Technical Debt & Stability Concerns (from code audit)

From grepping 23 TODO/FIXME/HACK comments across the codebase:

### Stability Risks
- **6 mixins touching vanilla** (`MixinLivingEntity`, `MixinLocalPlayer`, `MixinHUDOverlayRenderer`, `MixinScreenRenderState`). The `requires=0` flag on some suggests known fragility. Risk: mod compatibility and future update brittleness.
- **PacketHandler incomplete refactor**: `// TODO: Every packet class must be converted to implement X` — 85 packets exist but this suggests the refactor is mid-flight. Needs resolution before alpha or packet desync bugs may appear.
- **`VillageEvents.java`**: StructureTemplatePool encapsulation incomplete (1.21 port compatibility issue).
- **`FeatureLogic.java`**: Material whitelist hardcoded (`FIXME: turn this into a tag list`).

### Known UX Bugs to Fix Before Alpha
- **Centrifuge screen background animation too fast** (known, minor).
- **Locked recipes show full recipe details** (should be hidden until unlocked — progression spoiler).
- **`HemoProgressionScreen.setupEntries()` commented out** (confirmed — Java renderer WIP, HutosLib JSON fallback works but needs verification).

### Confirmed Placeholder Assets
- `Annetta Knowles` — no longer uses the old `blank.png` placeholder path; dedicated encounter models/textures exist, with animation/final polish still pending.
- `Putriciel` — placeholder renderer confirmed.
- `Spectral Companion` — placeholder texture.
- Curved Horn armor + Blood Gourd armor — placeholder models.

### Balance/Design Issues (from WHATS NEXT developer notes)
- **Mind Spike manipulation**: Currently assigned to Degree 1 — developer notes say it should be Degree 4.
- **Dendritic Distributor**: Placed in early-game — developer notes say it should be late-game.
- **Hematic Iron Scrap**: Hematic Salvage rules/tests now cover salvage behavior; use `HematicSalvageRulesTest` and current loot docs as source of truth.
- **Blood Crystal growth**: Implemented through alembic leak growth behavior; see `GhastlyAlembicBlockEntity.tryLeakBloodOntoBlock()`.
- **Natural sanguine progression**: Players should find scrap when hurt early-game as organic hook — not implemented.

---

## Critical Files to Audit First

- `src/main/resources/data/hemomancy/advancement/` — verify JSON chain
- `src/main/java/com/vincenthuto/hemomancy/client/screen/HemoProgressionScreen.java` — `setupEntries()` commented state
- `src/main/java/com/vincenthuto/hemomancy/common/capability/player/skillpoint/SkillPointHelper.java` — skill math and any UI wording drift
- `src/main/java/com/vincenthuto/hemomancy/common/init/StructureInit.java` — which saint trial structures are placed
- `src/main/java/com/vincenthuto/hemomancy/common/entity/boss/AnnettaKnowlesEntity.java` — encounter trigger status
- `src/main/resources/assets/hemomancy/textures/entity/` — confirm which bosses have real textures vs blank.png

-- -

## Verification Plan (End-to-End Alpha Test)

1. **Fresh world test** — create new world, confirm no crash on load
2. **Harbinger path** — find Blood Temple → meet Hermit → Degree 1 → verify Liber opens → complete Sanguine Initiation rite → advance to Degree 3 via crafting/rites → unlock manipulations → verify blood economy feels correct → reach Archon (Degree 7) → Fungal Spine → enter dimension → Qliphoth Bloom → Apotheos
3. **Unstained path** — find Unstained Church → meet Zealot → drink Hemolytic Solution → Purification stages → Clarity → Still Arts
4. **Saint encounters** — first major post-release update; excluded from the launch playthrough
5. **JEI audit** — open JEI and verify every custom machine recipe category is populated
6. **Advancement pop audit** — confirm each degree advancement fires + displays on screen
7. **Performance test** — 30 minutes of play in a world with Blood Temple, Harbinger Outpost, and fungal biome loaded simultaneously
