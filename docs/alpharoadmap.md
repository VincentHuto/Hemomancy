# Hemomancy Alpha Readiness Analysis & Roadmap

## Context

VincentHuto's Hemomancy is a sophisticated blood-magic + fungal-horror Minecraft NeoForge mod (Minecraft 1.21.1, NeoForge 21.1.x, Java 21) with two mutually exclusive player paths (Harbinger and Unstained). The goal is to assess its current state against "what makes a good Minecraft mod good" and produce a prioritized roadmap for alpha release.

> 2026-05-15 refresh: the current code is ahead of several older audit notes below. Treat `HEMOMANCY_REFERENCE.md` as the canonical status doc. Confirmed cleanup since the original roadmap includes 1.21 singular resource paths, wired skill effects, implemented Hematic Salvage tests/rules, blood crystal growth from alembic leaks, Annetta Broken Church spawning, dedicated Annetta encounter models/textures, and JEI registration for Mycelial Lantern enzyme fruiting.

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
- 342 JSON recipes covering all crafting progressions.

**2. In-game recipe discoverability — LARGELY PASSES**
- Custom JEI categories exist for Chisel Station, Visceral Recaller, Blood Structure, and Morphling Incubator (`compat/jei/`).
- 342 recipes cover all paths.
- *Gap: JEI categories need audit to confirm all custom machine recipes surface correctly.*

**3. Functional guidebook — MOSTLY PASSES**
- Liber Sanguinum (Harbinger) and Liber Immaculatus (Unstained) both use HutosLib JSON framework.
- Books have degree-gated pages that unlock per progression.
- *Gap: The `HemoProgressionScreen.setupEntries()` is commented out; the Java renderer is WIP, but the HutosLib JSON reader works. Players can read it — but the Harbinger-side progress screen tabs may not fully surface.*

**4. Content density — PASSES**
- 323 items, 113 blocks, 87 entities (including 4 bosses, 5 NPCs, 10+ monsters).
- 12 morphling variants, 20+ scar types, 9 fungal scar cultivars.
- 8 blood tendency trees + Unstained Still Arts.
- Qliphoth Bloom, Founding Sanctum, Bloodline pool system — all functional.

**5. Worldgen — PASSES**
- 3 Harbinger biomes (Fungal Gardens, Fungal Isles, Hemorrhagic Plateau) + Unstained equivalents via TerraBlender.
- Blood Temple, Harbinger Outpost, Unstained Church, Saint Trial Chamber (Hemorath complete).
- Fungal Dimension accessible via Fungal Spine item.

**6. Lore depth — PASSES**
- The moral-ambiguity framework (Harbinger ≠ villain, Unstained ≠ hero, Fungal Entity ≠ evil) is coherently executed.
- NPC dialogue trees with 8-degree gating for Vicar, full purity-stage gating for Zealot/Acolyte.
- Fungal Whisper events (Archon+) add eldritch depth.
- 51 item inquiry dialogue entries for contextual lore.

---

### GAPS (Must Fix Before Alpha)

#### CRITICAL — Blocks Alpha Release

**G1. Boss Visual Assets (3 of 4 Saints + Annetta)**
- **Issue**: Seraphae, Putriciel, and Velorum have full AI and combat mechanics but placeholder or unfinished presentation. Annetta Knowles now has a Broken Church trigger plus dedicated Java models/textures, but her final animations, Sanguis Lancea rendering, and Phase 1 combat polish remain WIP.
- **Impact**: Placeholder humanoid models in boss fights destroy first impressions. This is the single highest-visibility gap.
- **Files**:
    - `src/main/java/com/vincenthuto/hemomancy/common/entity/boss/SeraphaeEntity.java`
    - `src/main/java/com/vincenthuto/hemomancy/common/entity/boss/PutricielEntity.java`
    - `src/main/java/com/vincenthuto/hemomancy/common/entity/boss/VelorumEntity.java`
    - `src/main/java/com/vincenthuto/hemomancy/common/entity/boss/AnnettaKnowlesEntity.java`
    - `src/main/resources/assets/hemomancy/textures/entity/` (missing textures for above)
- **Resolution**: Either create proper models + GeckoLib animations, or explicitly label these saints as "in development" in the Liber and disable their trial chamber spawning until ready.

**G2. Saint Trial Chambers — 3 of 4 Incomplete**
- **Issue**: Hemorath's full trial (blood basin puzzle, sarcophagus, boss fight) is complete. Seraphae, Putriciel, Velorum have boss AI but no bespoke trial chamber NBT structures and untuned world placement.
- **Impact**: Players who find these structures will hit undefined content.
- **Files**:
    - `src/main/resources/data/hemomancy/structure/` (saint trial NBTs)
    - `src/main/java/com/vincenthuto/hemomancy/common/worldgen/` (placement configs)
- **Resolution**: Option A — complete the three chambers. Option B — disable Seraphae/Putriciel/Velorum trial chamber world placement for alpha, keeping Hemorath as the only complete saint encounter.

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
| A4 | Decision on G2: complete 3 saint chambers OR disable trial spawning for incomplete saints | High (complete) / Low (disable) |
| A5 | Decision on G1: model work for 3 saints OR explicitly mark WIP in-world | Very High (model) / Low (disable) |

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
| C1 | Complete all 4 saint boss models + GeckoLib animations | Very High |
| C2 | Complete Seraphae/Putriciel/Velorum trial chambers | High |
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
4. **Hemorath encounter** — locate Saint Trial Chamber → complete trial → boss fight → residuum rewards
5. **JEI audit** — open JEI and verify every custom machine recipe category is populated
6. **Advancement pop audit** — confirm each degree advancement fires + displays on screen
7. **Performance test** — 30 minutes of play in a world with Blood Temple, Harbinger Outpost, and fungal biome loaded simultaneously
