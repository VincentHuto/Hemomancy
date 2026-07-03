# Audit Phase-1 Guardrails Implementation Plan (mini)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Design source:** [POWER_SYSTEMS_AUDIT.md](../../POWER_SYSTEMS_AUDIT.md) §3.2 (stacking discipline) and §7 Phase 1. This is deliberately the **smallest plan in the family and the first to build** — pure shared helpers with no content changes, so the three content plans ([fungal scars](2026-07-03-fungal-scar-consolidation.md) → [morphlings](2026-07-03-morphling-fungal-strain-reframe.md) → [Wills](2026-07-03-rogue-hemomancer-wills.md)) can write against real APIs instead of `TODO(audit)` workarounds.

**Goal:** Ship four shared governors: the **circulation income cap** (one bandwidth for all passive blood income), the **Last Rite group** (one armed death-save at a time), the **borrowed-blood reserve** (the shared pool the Leeches re-role and Blood Lust overkill both write into), and **triad group caps** for speed/toughness stacking.

**Architecture:** Each governor is a small pure-logic class plus one player attachment, consumed at existing grant sites. Nothing here adds player-facing content; everything is invisible until a downstream plan routes through it. All numbers behind config.

**Tech Stack:** NeoForge 1.21.1 attachments (`HemoAttachmentTypes`), `HemoServerConfig`, existing grant sites in `common/event/ArmorSetBonusHandler.java` and `common/capability/player/harbinger/equipment/HarbingerEquipmentEntityEventHandler.java`, focused `*SourceTest`/`*ResourceTest` conventions under `src/test/java/`.

---

## Current-state anchors (verified 2026-07-03)

| Concern | Anchor |
|---|---|
| Set bonuses (Hematic Iron regen, Blood Lust lifesteal, mask modifiers) | `common/event/ArmorSetBonusHandler.java` |
| Scar synergy / equipment tick handlers | `common/capability/player/harbinger/equipment/HarbingerEquipmentEntityEventHandler.java` |
| Cradle leech/redistribution | `common/tile/crafting/MorphlingCradleBlockEntity.java` (owner blood fallback paths) |
| Morphling passive siphon | `common/item/harbinger/morphlings/` (Leeches passive; becomes Deadman's Purse) |
| Death saves today | Cuttlefish Ink Mantle (Apex ability), Silent Archon refusal (set bonus), Thanomyces Split Husk (being cut by the [scar plan](2026-07-03-fungal-scar-consolidation.md)) |
| Player state / config | `HemoAttachmentTypes`, `HemoServerConfig` |

---

### Task 1: `CirculationIncomeHelper` — one bandwidth for passive blood income

**Files:**
- Create: `common/capability/player/harbinger/bloodvolume/CirculationIncomeHelper.java`
- Create: attachment `CirculationWindow` (rolling per-second accumulator) in `HemoAttachmentTypes`

- [ ] **Step 1 — Inventory the grant sites.** Grep and list every code path that passively adds blood to a player from the audit's §3.2-5 list: Hematic Iron set regen and Lodestone mask trickle (`ArmorSetBonusHandler`), the morphling siphon passive, cradle leech redistribution to owner. Record each site in the PR description. **Explicit exemptions (document in the class javadoc):** skill regen (Sanguine Surge, Last Wind — the player's own trained body), manipulation-generated blood (`vital_reservoir`, `exsanguinate` — active casts, not passive income), and consumables. The cap governs *the world lending to you*, not your own will.
- [ ] **Step 2 — API + math.** `float requestIncome(Player p, float amount, IncomeChannel channel)` → clamps against remaining bandwidth this second and returns what was actually granted; `bandwidthPerSecond(p)` = `baseBandwidth + perDegree × degree + perCapacityLevel × Capacity skill level` (defaults: 6 + 1.5/degree + 1/level — generous enough that a *single* source never clips; only stacking does). `IncomeChannel` enum (ARMOR, MORPHLING, CRADLE, SCAR, OTHER) recorded for the debug line.
- [ ] **Step 3 — Route the sites.** Replace each inventoried site's direct blood-add with `requestIncome`. Behavior with one source equipped must be numerically identical (assert in test); two+ sources now share the pipe with diminishing effect.
- [ ] **Step 4 — Config + debug.** `circulation` config keys: `enabled` (true), `baseBandwidth`, `perDegree`, `perCapacityLevel`. Add the channel readout to an existing debug command (whichever dev command prints blood state) rather than new UI — the visible-flow-meter question stays open ([DEFERRED_IDEAS](../../DEFERRED_IDEAS.md) ledger).
- [ ] **Step 5 — Test + gate.** `CirculationIncomeHelperSourceTest`: single-source pass-through identity; two-source clamping; bandwidth scaling by degree/skill; disabled flag = pure pass-through. `./gradlew test --tests "*Circulation*"` then `./gradlew build`.

### Task 2: `LastRiteRules` — one armed death-save

**Files:**
- Create: `common/capability/player/harbinger/LastRiteRules.java` + `LastRiteState` attachment (armed source id + shared cooldown timestamp)

- [ ] **Step 1 — Provider registry.** Death-save sources register a `ResourceLocation` id: `hemomancy:ink_mantle` (Cuttlefish/Foxfire Apex), `hemomancy:silent_refusal` (Silent Archon set), and — future — `hemomancy:cryptobiosis` (Winter Shroud Primal, per the morphling plan). Thanomyces is *not* registered (it is cut by the scar plan); if the scar plan hasn't run yet, register it too and delete the line there — order-tolerant either way.
- [ ] **Step 2 — Arming rule.** Most-recently-equipped provider arms (`arm(player, id)` called from each source's equip/apply path); others read as dormant. `boolean tryConsume(player, id)` → false unless `id` is armed **and** `sharedCooldownUntil` has passed; on success stamps the shared cooldown (`lastRiteSharedCooldownTicks`, default 12,000 — matches the Silent Archon cadence). Doctrine string for tooltips: *"your blood may refuse the return only once."*
- [ ] **Step 3 — Wire the two live sources.** Ink Mantle and Silent Archon refusal check `tryConsume` before firing and show an "(dormant — another rite is armed)" tooltip line when unarmed. Their individual cooldowns remain as-is *inside* the shared gate.
- [ ] **Step 4 — Test + gate.** `LastRiteRulesSourceTest`: arming precedence, shared-cooldown mutual exclusion, unregistered id always false. Gate: `runClient` — equip both sources, verify only the most recent fires and the other reports dormant.

### Task 3: `BorrowedBloodReserve` — the shared handshake pool

**Files:**
- Create: attachment + helper `common/capability/player/harbinger/bloodvolume/BorrowedBloodReserve.java`

- [ ] **Step 1 — State + API.** Float reserve on the player (cap `borrowedBloodCap`, default 500), `deposit(player, amount)` / `drain(player, amount)`. This is the audit §6.1 loop's *storage only* — the manipulation-discount spend side is Phase 4 and out of scope here.
- [ ] **Step 2 — First two writers.** (a) Blood Lust set lifesteal overkill: in `ArmorSetBonusHandler`, lifesteal beyond the player's max health deposits instead of vanishing. (b) Leave a documented seam for the Deadman's Purse feed-banking to call `deposit` (erases the morphling plan's `TODO(audit §6.1)` — its Task 4 Step 2 should now write here instead of stack data).
- [ ] **Step 3 — First reader.** Emergency only for now: when blood volume would hit 0 from a manipulation cast, drain borrowed blood first (small, safe, immediately useful). Tooltip/HUD surfacing deferred.
- [ ] **Step 4 — Test + gate.** Deposit/cap/drain math test; overkill-lifesteal deposit verified in `runClient` against a high-HP target.

### Task 4: Triad speed/toughness group caps

**Files:**
- Create: `common/capability/player/harbinger/TriadAttributeCaps.java` (shared constants + clamp helpers)

- [ ] **Step 1 — Scope honestly.** No generic attribute interception. Define cap constants (`maxTriadMoveSpeedBonus` = +30%, `maxTriadToughnessBonus` = +4.0, config-backed) and a helper that the **known grant sites** call to pre-clamp their contribution given what the other triad layers already granted (sites: `ArmorSetBonusHandler` set/mask modifiers, morphling passive amplifier application, scar synergy in `HarbingerEquipmentEntityEventHandler.checkScarSynergy`).
- [ ] **Step 2 — Wire + rename.** Apply at the three sites. (The audit's "rename Chitinite morphling passive" item is superseded — the morphling plan cuts that species outright; note this in the audit checklist.)
- [ ] **Step 3 — Test + gate.** Clamp math test (sum from three synthetic layers never exceeds cap; single layer unaffected). `./gradlew build` green.

### Task 5: Closeout

- [ ] **Step 1:** Tick the four Phase-1 boxes in [POWER_SYSTEMS_AUDIT.md](../../POWER_SYSTEMS_AUDIT.md) §7 and correct its §3.2-6 provider list (Thanomyces → Cryptobiosis).
- [ ] **Step 2:** Document the three helpers + config keys in `HEMOMANCY_REFERENCE.md` (short subsection under player capabilities).
- [ ] **Step 3:** Update the [morphling plan](2026-07-03-morphling-fungal-strain-reframe.md) TODO references to point at the now-real APIs (its Task 4 Steps 2–3).
- [ ] **Step 4:** Full `./gradlew build`; note in the PR that behavior with any *single* income/death-save/buff source is unchanged — this plan only disciplines *stacks*.
