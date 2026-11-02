# Cardinal Rite World Escalation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make a Harbinger Cardinal Rite visibly escalate the world around it, one lasting change per milestone, and replace the top-of-screen rite panel with in-world cues.

**Architecture:** The server keeps a stage that only rises (`CardinalRiteEscalation`), saves it with the rite, syncs it in `PacketSyncActiveRites`, and announces each step with a new `ESCALATION` rite beat. On the client, `RiteWorldState` eases each rite's world level and its endings. Separate layer classes read only that state: air (fog), trench and creep (floor membrane shader), sky wound, braziers, blood rain, sound bed and socket markers. The panel becomes opt-in through client config, and guidance moves to action-bar lines built from structured, translatable problems.

**Tech Stack:** NeoForge 1.21.1 (21.1.251), Java 21, Minecraft core shaders (GLSL 150), JUnit 5 (`src/test`), NeoForge GameTests (`src/gameTest`), Gradle (`./gradlew.bat`).

**Spec:** `docs/superpowers/specs/2026-10-13-cardinal-rite-world-escalation-design.md`

## Global Constraints

- NeoForge imports only (`net.neoforged.*`); network payloads use `CustomPacketPayload` with a static `TYPE` and `STREAM_CODEC`; never add `SimpleChannel` code.
- Harbinger interactive rites only. Legacy (`CardinalRitePhase.LEGACY`) and Unstained rites get no world layers.
- Visual and sound only: nothing edits blocks and nothing changes gameplay.
- No vanilla `ParticleTypes` in new or touched code; particles come from `common/particle/HemoParticleData`.
- No brass, bronze, copper or silver colours; every layer takes its colours from the floor palette (`BaseColor`, `VeinColor`, `GlowColor`).
- Do not commit. Leave every change in the working tree (project preference). Where a superpowers template says "Commit", run the task's checks instead.
- Milestones: each anchor ring completed for the first time; each station-upgrade circuit completed; the seal or a puppeteer trial manifesting; reaching `OFFERING_PROCESSION` (only rites with offerings); reaching `CULMINATION`. Total = rings + circuits + 1 + (1 if offerings) + 1. Succession: `ticks / 200`, capped at 5, total 5.
- Layer thresholds: air, trench, braziers, drone from step 1; creep and heartbeat from level 1/3; sky wound from level 1/2; rain, wind and overflow from level 3/4; the wound opens in `CULMINATION`.
- Viewer weight: 1 within footprint radius + 4 blocks, falling linearly to 0 at footprint radius + 32.
- Air: fog colour mix up to 0.25 (faint), 0.45 (dense), 0.6 (storm) × level × weight; far plane × 0.85 / 0.75 / 0.65 at full level; strain × 0.9 strained, × 0.8 rupturing; never below 24 blocks; skipped when the far plane is already below 24.
- Creep reach: `clamp(0.6 × footprint radius, 4, 12)` blocks past the floor edge; it arrives at level 1/3 a quarter grown and is full at level 1; rupturing pulls it back to 70%.
- Sky wound: Focus height + 30 + 0.6 × footprint radius, kept 8 blocks under the build limit; radius 0.8× footprint at formation to 1.6× at full; drawn to 256 blocks.
- Braziers within footprint radius + 2: flame height up to +50%, lean up to 20° toward the Focus; strained flicker and 25% shorter; rupturing gutters to embers between heartbeats.
- Rain: spawned 12–16 blocks above the floor inside the boundary radius; `round(rainLevel × (2 + 0.5 × footprint radius))` per tick, capped at 30; × 1 / 0.5 / 0 for all / decreased / minimal particles; strain × 1.25.
- Sound events: `rite.escalate` → `minecraft:entity.warden.sonic_charge`; `rite.ambient.drone` → `minecraft:ambient.crimson_forest.loop`; `rite.ambient.heartbeat` → `minecraft:entity.warden.heartbeat`; `rite.ambient.storm` → `minecraft:ambient.basalt_deltas.loop`.
- Endings (ghost lasts 80 ticks): completed closes the wound with a flash, clears fog and sinks creep over 3 s (60 ticks), stops rain at once, fades sound over 2 s (40 ticks); collapsed or broken tears creep back in 1 s (20 ticks), collapses the wound with 3–5 lightning strikes, surges the fog 20% closer then thins it over 4 s (80 ticks); cancelled fades everything over 4 s.
- Config: `HemoClientConfig.CARDINAL_RITE_HUD`, enum `OFF` (default) or `FULL`.
- Guidance only for the rite's owner, in the action bar, and only while the panel is `OFF`; after a line, the next waits 70 ticks.

## Review Focus

1. **Arriving mid-rite.** A player who logs in, respawns or walks up to a rite already at stage 4 should see stage 4 at once, not watch the world climb from nothing and not hear step stings for old steps. Pinned in Task 5 (`firstSightingSnapsToTheSyncedLevel`).
2. **Two rites close together.** Two Foci 20 blocks apart must not double the fog or stack two sound beds. The air layer takes the strongest weighted rite, and the sound bed follows the single nearest one. Pinned in Task 6 (`strongestRiteWinsInsteadOfSumming`) and Task 12 (`soundBedFollowsTheNearestRite`).
3. **Leaving, logging out, changing dimension.** Fog tint, rain and loops must never stick after a rite has ended or the player has left. State clears on logout and dimension change, and ghosts expire. Pinned in Task 5 (`ghostsExpireAndClearEmptiesEverything`).
4. **Missing or broken floor.** A rite whose Focus has no formed floor, or whose recipe is unknown on the client, must not crash. It falls back to the default palette and skips the trench and creep. Pinned in Task 5 (`missingFloorFallsBackToTheDefaultPalette`, which also checks that no floor means no creep).
5. **Extreme heights and sizes.** A rite near the build limit or the bottom of the world, or a grand footprint, must keep the sky wound inside the world and the creep mesh bounded. Pinned in Task 5 (`woundAltitudeStaysUnderTheBuildLimit`) and Task 8 (`creepMeshIsBoundedForGrandFootprints`).

## Adjustments made while planning

Each of these follows from code the spec did not look at closely. None changes what the player sees.

1. **Creep cells live in `FloorMesh`**, which owns the trench loop they must stay outside, with a pure `CreepCells` helper. There is no separate `CreepMesh` class.
2. **Layers show on their own step.** The creep arrives at a quarter of its reach, and the rain and overflow at 30%; each grows to full by level 1. Read literally, "from 0 at level 1/3" would show nothing until the following step.
3. **Brazier colour.** The flame shader takes fixed palettes, not a colour. Braziers "lean toward glow red" by switching to the existing sanguine palette from halfway.
4. **Heartbeat timing.** The thump is timed by game time: every 36, 24 or 18 ticks, matching the floor's tempo. A tick-rate sample of the beat curve can miss the beat.
5. **Guidance runs only while the panel is `OFF`.** With `FULL`, the panel already says the same thing. Lines last as long as vanilla shows the action bar (about 3 s), and the next waits 70 ticks.
6. **The fog's near plane** scales with its far plane.
7. **A sixth problem key, `finale_held`,** covers the finale refusal hold, which the old checklist also reported.
8. **No offline preview renders.** Tuning happens in game.
9. **A step's arrival** is the surge value (the shock ring reused from the staff impact, and the wound's flash), not an overshoot of the level. An overshoot could briefly show the next step's layer.
10. **Rain speed.** Drops fall at -0.4, which the blood-cell particle doubles, so they cover about 14 blocks in their short life.
11. **The GameTest** covers the save with real offerings. Beat wiring through the live tick loop is covered by a source test, because driving that loop needs a full consecrated station.

---

## File Structure

**Server and shared (`src/main/java/com/vincenthuto/hemomancy/`)**

| File | Responsibility |
|---|---|
| `common/rite/CardinalRiteEscalation.java` (new) | Milestone plan, marks, stage arithmetic, per-tick update and old-save rebuild. Pure apart from reading `ActiveCardinalRite`. |
| `common/rite/ActiveCardinalRite.java` | Saves the escalation marks and stage. |
| `common/rite/CardinalRiteEventKind.java` | New `ESCALATION` beat, appended last. |
| `common/rite/CardinalRiteProblems.java` (new) | Structured, translatable problems (`RiteProblem`) built from rite state. Pure. |
| `common/rite/harbinger/HarbingerCardinalRiteEvents.java` | Calls the escalation update each tick and announces steps; syncs the stage, total, problems and socket markers. |
| `common/rite/harbinger/CardinalRiteInteractionHandler.java` | Public helper for the world positions of required, sleeping support sockets. |
| `common/network/capa/harbinger/PacketSyncActiveRites.java` | Writes and reads the new entry fields. |
| `common/init/SoundInit.java`, `assets/hemomancy/sounds.json`, `assets/hemomancy/lang/en_us.json` | New sound events, subtitles, guidance and problem lines. |
| `config/HemoClientConfig.java` | `CARDINAL_RITE_HUD` option. |

**Client (`src/main/java/com/vincenthuto/hemomancy/client/`)**

| File | Responsibility |
|---|---|
| `data/ActiveRiteClientData.java` | `RiteEntry` carries escalation, problems and socket markers. |
| `render/world/rite/escalation/RiteWorldMath.java` (new) | Every threshold, falloff and size formula, as pure static functions. |
| `render/world/rite/escalation/RiteWorldState.java` (new) | Per-rite eased level, step surge, strain, palette, endings, clearing. |
| `render/world/rite/escalation/RiteFloor.java` (new) | The floor under a Focus: its palette, whether it is formed, its size; a default when there is none. |
| `render/world/rite/escalation/RiteAirLayer.java` (new) | Fog colour and fog distance events. |
| `render/world/floor/FloorRiteResponse.java`, `floor/CardinalFloorRenderTypes.java`, shader `cardinal_floor_membrane.*` | `RiteBlood` uniform: trench fill, overflow, creep radius and strength; step surge through the staff-shock ring. |
| `render/world/floor/FloorMesh.java`, `render/world/floor/CreepCells.java` (new) | Creep cells beyond the floor, each at its own ground height, emitted into the floor buffer. |
| `render/world/rite/escalation/RiteSkyWound.java` (new), shader `cardinal_rite_sky_wound.*` (new) | The sky wound geometry, its shader and lightning. |
| `render/world/BrazierFlames.java` | Reads `RiteWorldState` for lean, height, colour and gutter. |
| `render/world/rite/escalation/RiteBloodRain.java` (new) | Client-side blood-cell rain and splashes. |
| `sound/RiteSoundBed.java` (new), `sound/RiteLoopSound.java` (new) | Drone, heartbeat and storm layers. |
| `render/world/rite/escalation/RiteSocketMarkerRenderer.java` (new) | Blood threads over sleeping required sockets. |
| `render/world/rite/escalation/RiteGuidance.java` (new) | Owner-only action-bar lines. |
| `screen/overlay/CardinalRiteOverlay.java` | Draws only when `CARDINAL_RITE_HUD` is `FULL`. |
| `event/ClientEvents.java` | Hooks the new renderers, ticks and clears. |

**Tests**

| File | Covers |
|---|---|
| `src/test/java/com/vincenthuto/hemomancy/common/rite/CardinalRiteEscalationTest.java` (new) | Totals, monotonic stage, single step for simultaneous milestones, station circuits, trials, succession, NBT, old-save rebuild. |
| `src/test/java/com/vincenthuto/hemomancy/common/rite/CardinalRiteProblemsTest.java` (new) | Problem selection. |
| `src/test/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/PacketSyncActiveRitesEscalationTest.java` (new) | Packet round trip of the new fields. |
| `src/test/java/com/vincenthuto/hemomancy/client/render/world/rite/escalation/RiteWorldMathTest.java` (new) | Thresholds, falloff, fog, creep, wound, rain, brazier, sound maths. |
| `src/test/java/com/vincenthuto/hemomancy/client/render/world/rite/escalation/RiteWorldStateTest.java` (new) | Snapping on first sight, easing, ghosts, clearing, palette fallback. |
| `src/test/java/com/vincenthuto/hemomancy/client/render/world/rite/escalation/RiteGuidanceTest.java` (new) | Phase lines, problem change detection, owner only. |
| `src/test/java/com/vincenthuto/hemomancy/client/render/world/rite/escalation/RiteAirLayerTest.java` (new) | The strongest rite wins; no air before the first step or out of range. |
| `src/test/java/com/vincenthuto/hemomancy/client/render/world/rite/escalation/RiteSkyWoundTest.java` (new) | Formation, growth, opening, closing and collapse of the wound. |
| `src/test/java/com/vincenthuto/hemomancy/client/render/world/floor/CreepCellsTest.java` (new) | Ground search for creep cells; the cell bound for grand floors. |
| `src/test/java/com/vincenthuto/hemomancy/client/sound/RiteSoundBedTest.java` (new) | Nearest rite, silence before the first step, heartbeat tempo. |
| `src/test/java/com/vincenthuto/hemomancy/client/render/world/rite/escalation/RiteWorldSourceTest.java` (new) | Beat wiring, config gate, shader uniform, creep and wound hooks, no vanilla particles. |
| `src/gameTest/java/com/vincenthuto/hemomancy/gametest/CardinalRiteEscalationGameTests.java` (new) | A rite saved with real offerings keeps its stage and its procession step. |

---
### Task 1: Escalation stage on the server

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/rite/CardinalRiteEscalation.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/rite/ActiveCardinalRite.java` (fields after `private int finaleHoldTicks;`, accessors after `getSacrificeMl()`, `serialize(HolderLookup.Provider)` after `tag.putInt("FinaleHoldTicks", finaleHoldTicks);`, end of `deserialize(CompoundTag, HolderLookup.Provider)`)
- Test: `src/test/java/com/vincenthuto/hemomancy/common/rite/CardinalRiteEscalationTest.java`

**Interfaces:**
- Consumes: `ActiveCardinalRite` (`totalAnchorRings()`, `completedRings()`, `upgrade()`, `succession()`, `getOfferingItinerary()`, `isAltarSealed()`, `isPuppeteerTrialManifested()`, `getPhase()`, `getRecipeId()`), `StationUpgradeRites.circuits(ResourceLocation)`.
- Produces:
  - `CardinalRiteEscalation.Plan(int rings, int circuits, boolean procession, boolean succession)` with `int total()`
  - `CardinalRiteEscalation.Marks(int rings, int circuits, int flags)` with `Marks NONE` and `Marks raise(Reached, Plan)`
  - `CardinalRiteEscalation.Reached(int rings, int circuits, boolean opening, boolean procession, boolean culmination, int successionTicks)`
  - `static int stage(Plan, Marks, int successionTicks)`, `static Plan plan(ActiveCardinalRite)`, `static Reached reached(ActiveCardinalRite)`, `static boolean update(ActiveCardinalRite)`, `static void rebuild(ActiveCardinalRite)`
  - `ActiveCardinalRite.getEscalationMarks()`, `setEscalationMarks(Marks)`, `getEscalationStage()`, `setEscalationStage(int)`, `getEscalationTotal()`

- [ ] **Step 1: Write the failing test**

```java
package com.vincenthuto.hemomancy.common.rite;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

final class CardinalRiteEscalationTest {
	private static final ResourceLocation PLAIN = ResourceLocation.parse("hemomancy:cardinal_rite/escalation_test");
	private static final ResourceLocation STATION = ResourceLocation.parse("hemomancy:cardinal_rite/first_condensation");

	private static ActiveCardinalRite rite(ResourceLocation id, int anchors, int waves) {
		return ActiveCardinalRite.interactive(UUID.randomUUID(), BlockPos.ZERO, id, 1200, 5, 4, false, waves, anchors);
	}

	private static void fill(ActiveCardinalRite rite, int from, int to) {
		for (int i = from; i < to; i++) rite.fillAnchor(i, 50);
	}

	@Test
	void totalsCountRingsCircuitsOpeningProcessionAndCulmination() {
		assertEquals(4, new CardinalRiteEscalation.Plan(1, 0, true, false).total());
		assertEquals(3, new CardinalRiteEscalation.Plan(1, 0, false, false).total());
		assertEquals(5, new CardinalRiteEscalation.Plan(2, 0, true, false).total());
		assertEquals(6, new CardinalRiteEscalation.Plan(3, 0, true, false).total());
		assertEquals(11, new CardinalRiteEscalation.Plan(8, 0, true, false).total());
		assertEquals(2, new CardinalRiteEscalation.Plan(0, 0, false, false).total());
		assertEquals(6, new CardinalRiteEscalation.Plan(1, 2, true, false).total());
		assertEquals(5, new CardinalRiteEscalation.Plan(3, 0, true, true).total(), "succession always has five steps");
	}

	@Test
	void eachRingCountsOnceAndTheStageNeverFalls() {
		ActiveCardinalRite rite = rite(PLAIN, 8, 1);
		fill(rite, 0, 4);
		assertTrue(CardinalRiteEscalation.update(rite));
		assertEquals(1, rite.getEscalationStage());
		assertFalse(CardinalRiteEscalation.update(rite), "a counted ring does not step again");

		rite.drainAnchor(0, 30);
		assertFalse(CardinalRiteEscalation.update(rite));
		assertEquals(1, rite.getEscalationStage(), "a drained anchor never lowers the stage");

		rite.fillAnchor(0, 30);
		assertFalse(CardinalRiteEscalation.update(rite), "refilling a counted ring is not a new step");
		fill(rite, 4, 8);
		assertTrue(CardinalRiteEscalation.update(rite));
		assertEquals(2, rite.getEscalationStage());
	}

	@Test
	void sealProcessionAndCulminationEachCountOnceAndLandingTogetherStepsOnce() {
		ActiveCardinalRite rite = rite(PLAIN, 4, 0);
		rite.captureOfferingItinerary(List.of(new ActiveCardinalRite.RiteOffering(new BlockPos(2, 0, 0), null, true)));
		fill(rite, 0, 4);
		assertTrue(CardinalRiteEscalation.update(rite));
		assertTrue(rite.enterInscription());
		assertFalse(CardinalRiteEscalation.update(rite), "inscription alone is not a milestone");

		assertTrue(rite.sealAltar(false));
		assertEquals(CardinalRitePhase.OFFERING_PROCESSION, rite.getPhase());
		assertTrue(CardinalRiteEscalation.update(rite), "seal and procession arrive in one update");
		assertEquals(3, rite.getEscalationStage());
		assertFalse(CardinalRiteEscalation.update(rite));

		assertTrue(rite.absorbCurrentOffering());
		assertTrue(rite.finishOfferingProcession());
		assertTrue(CardinalRiteEscalation.update(rite));
		assertEquals(4, rite.getEscalationStage());
		assertEquals(4, rite.getEscalationTotal());
	}

	@Test
	void riteWithoutOfferingsHasNoProcessionStep() {
		ActiveCardinalRite rite = rite(PLAIN, 4, 0);
		fill(rite, 0, 4);
		rite.enterInscription();
		rite.sealAltar(false);
		assertEquals(CardinalRitePhase.CULMINATION, rite.getPhase());
		assertTrue(CardinalRiteEscalation.update(rite));
		assertEquals(3, rite.getEscalationStage());
		assertEquals(3, rite.getEscalationTotal());
	}

	@Test
	void stationCircuitsAreSteps() {
		ActiveCardinalRite rite = rite(STATION, 4, 0);
		fill(rite, 0, 4);
		rite.enterInscription();
		assertTrue(rite.sealAltar(true));
		assertEquals(CardinalRitePhase.STATION_PROJECTION, rite.getPhase());
		assertTrue(CardinalRiteEscalation.update(rite));
		assertEquals(2, rite.getEscalationStage(), "ring and seal");

		assertTrue(rite.fillUpgradeCircuit(0, rite.upgradeBloodNeeded()));
		assertTrue(CardinalRiteEscalation.update(rite));
		assertEquals(3, rite.getEscalationStage());
		assertEquals(5, rite.getEscalationTotal(), "one ring, two circuits, seal and culmination");
	}

	@Test
	void puppeteerTrialStepsOnManifestAndDefeat() {
		UUID entity = UUID.randomUUID();
		ActiveCardinalRite rite = ActiveCardinalRite.puppeteerTrial(UUID.randomUUID(), BlockPos.ZERO,
				ResourceLocation.parse("hemomancy:cardinal_rite/puppeteer_trial_marrow_spitter"), 5);
		assertFalse(CardinalRiteEscalation.update(rite), "nothing before the puppet manifests");
		rite.beginPuppeteerTrial("marrow_spitter", entity, UUID.randomUUID());
		assertTrue(CardinalRiteEscalation.update(rite));
		assertTrue(rite.markPuppeteerTrialDefeated(entity));
		assertTrue(CardinalRiteEscalation.update(rite));
		assertEquals(2, rite.getEscalationStage());
		assertEquals(2, rite.getEscalationTotal());
	}

	@Test
	void successionStepsWithItsCues() {
		ActiveCardinalRite rite = rite(PLAIN, 4, 0);
		CompoundTag context = new CompoundTag();
		context.putInt("Ticks", 0);
		rite.beginSuccession(context);
		assertFalse(CardinalRiteEscalation.update(rite), "culmination does not count for succession");
		rite.succession().putInt("Ticks", 450);
		assertTrue(CardinalRiteEscalation.update(rite));
		assertEquals(2, rite.getEscalationStage());
		rite.succession().putInt("Ticks", 5000);
		assertTrue(CardinalRiteEscalation.update(rite));
		assertEquals(5, rite.getEscalationStage());
		assertEquals(5, rite.getEscalationTotal());
	}

	@Test
	void legacyRitesNeverEscalate() {
		ActiveCardinalRite rite = new ActiveCardinalRite(UUID.randomUUID(), BlockPos.ZERO, PLAIN, 100, 5);
		assertFalse(CardinalRiteEscalation.update(rite));
		assertEquals(0, rite.getEscalationStage());
	}

	@Test
	void marksAndStageSurviveSaving() {
		ActiveCardinalRite rite = rite(PLAIN, 8, 1);
		fill(rite, 0, 4);
		CardinalRiteEscalation.update(rite);
		rite.drainAnchor(1, 50);

		ActiveCardinalRite copy = ActiveCardinalRite.deserialize(rite.serialize());

		assertEquals(1, copy.getEscalationStage());
		assertEquals(rite.getEscalationMarks(), copy.getEscalationMarks());
		assertFalse(CardinalRiteEscalation.update(copy), "the drained ring stays counted after loading");
	}

	@Test
	void olderSavesRebuildTheStageFromTheirState() {
		ActiveCardinalRite rite = rite(PLAIN, 4, 1);
		fill(rite, 0, 4);
		rite.enterInscription();
		rite.sealAltar();
		CompoundTag saved = rite.serialize();
		saved.remove("EscalationRings");
		saved.remove("EscalationCircuits");
		saved.remove("EscalationFlags");
		saved.remove("EscalationStage");

		ActiveCardinalRite copy = ActiveCardinalRite.deserialize(saved);

		assertEquals(2, copy.getEscalationStage(), "full ring plus the seal");
		assertFalse(CardinalRiteEscalation.update(copy), "rebuilding announces nothing");
	}
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew.bat test --tests "com.vincenthuto.hemomancy.common.rite.CardinalRiteEscalationTest"`
Expected: compilation FAILS with `cannot find symbol` for `CardinalRiteEscalation` and `getEscalationStage()`.

- [ ] **Step 3: Create `CardinalRiteEscalation`**

```java
package com.vincenthuto.hemomancy.common.rite;

import com.vincenthuto.hemomancy.common.rite.harbinger.StationUpgradeRites;

/**
 * How far a Harbinger rite has pushed the world around it. Each milestone counts once, the first time
 * it is reached: a ring of anchors filled, a station circuit completed, the seal (or a puppeteer trial
 * manifesting), the offering procession and culmination. The stage is the sum of those marks, so it
 * never falls when instability drains an anchor. Succession steps with its five cues instead.
 */
public final class CardinalRiteEscalation {
	public static final int FLAG_OPENING = 1;
	public static final int FLAG_PROCESSION = 2;
	public static final int FLAG_CULMINATION = 4;
	public static final int SUCCESSION_CUE_TICKS = 200;
	public static final int SUCCESSION_STEPS = 5;

	private CardinalRiteEscalation() {
	}

	/** What a rite can reach. */
	public record Plan(int rings, int circuits, boolean procession, boolean succession) {
		public int total() {
			if (succession) return SUCCESSION_STEPS;
			return Math.max(0, rings) + Math.max(0, circuits) + 1 + (procession ? 1 : 0) + 1;
		}
	}

	/** Where the rite stands this tick, read from its live state. */
	public record Reached(int rings, int circuits, boolean opening, boolean procession, boolean culmination,
			int successionTicks) {
	}

	/** Milestones already counted; every part only rises. */
	public record Marks(int rings, int circuits, int flags) {
		public static final Marks NONE = new Marks(0, 0, 0);

		public Marks raise(Reached reached, Plan plan) {
			int raised = flags;
			if (reached.opening()) raised |= FLAG_OPENING;
			// Reaching culmination means the procession has passed, even inside one tick.
			if (plan.procession() && (reached.procession() || reached.culmination())) raised |= FLAG_PROCESSION;
			if (reached.culmination()) raised |= FLAG_CULMINATION;
			return new Marks(Math.max(rings, Math.min(reached.rings(), plan.rings())),
					Math.max(circuits, Math.min(reached.circuits(), plan.circuits())), raised);
		}
	}

	public static int stage(Plan plan, Marks marks, int successionTicks) {
		if (plan.succession()) {
			return Math.min(SUCCESSION_STEPS, Math.max(0, successionTicks) / SUCCESSION_CUE_TICKS);
		}
		return marks.rings() + marks.circuits()
				+ Integer.bitCount(marks.flags() & (FLAG_OPENING | FLAG_PROCESSION | FLAG_CULMINATION));
	}

	public static Plan plan(ActiveCardinalRite rite) {
		return new Plan(rite.totalAnchorRings(), StationUpgradeRites.circuits(rite.getRecipeId()),
				!rite.getOfferingItinerary().isEmpty(), !rite.succession().isEmpty());
	}

	public static Reached reached(ActiveCardinalRite rite) {
		CardinalRitePhase phase = rite.getPhase();
		return new Reached(rite.completedRings(), rite.upgrade().getInt("Stage"),
				rite.isAltarSealed() || rite.isPuppeteerTrialManifested(),
				phase == CardinalRitePhase.OFFERING_PROCESSION, phase == CardinalRitePhase.CULMINATION,
				rite.succession().getInt("Ticks"));
	}

	/**
	 * Raises the rite's marks from its live state.
	 *
	 * @return true when the stage rose, so the caller announces the step
	 */
	public static boolean update(ActiveCardinalRite rite) {
		if (rite.getPhase() == CardinalRitePhase.LEGACY) return false;
		Plan plan = plan(rite);
		Marks marks = rite.getEscalationMarks().raise(reached(rite), plan);
		rite.setEscalationMarks(marks);
		int stage = stage(plan, marks, rite.succession().getInt("Ticks"));
		if (stage <= rite.getEscalationStage()) return false;
		rite.setEscalationStage(stage);
		return true;
	}

	/** Rebuilds the marks of a rite saved before escalation existed, without announcing anything. */
	public static void rebuild(ActiveCardinalRite rite) {
		if (rite.getPhase() == CardinalRitePhase.LEGACY) return;
		Plan plan = plan(rite);
		Marks marks = Marks.NONE.raise(reached(rite), plan);
		rite.setEscalationMarks(marks);
		rite.setEscalationStage(stage(plan, marks, rite.succession().getInt("Ticks")));
	}
}
```

- [ ] **Step 4: Store the marks on `ActiveCardinalRite`**

After `private int finaleHoldTicks;` add:

```java
	/** Milestones already counted toward the rite's world stage; see {@link CardinalRiteEscalation}. */
	private CardinalRiteEscalation.Marks escalationMarks = CardinalRiteEscalation.Marks.NONE;
	private int escalationStage;
```

After `getSacrificeMl()` add:

```java
	public CardinalRiteEscalation.Marks getEscalationMarks() {
		return escalationMarks;
	}

	public void setEscalationMarks(CardinalRiteEscalation.Marks marks) {
		escalationMarks = marks == null ? CardinalRiteEscalation.Marks.NONE : marks;
	}

	public int getEscalationStage() {
		return escalationStage;
	}

	public void setEscalationStage(int stage) {
		escalationStage = Math.max(0, stage);
	}

	public int getEscalationTotal() {
		return CardinalRiteEscalation.plan(this).total();
	}
```

In `serialize(HolderLookup.Provider provider)`, after `tag.putInt("FinaleHoldTicks", finaleHoldTicks);` add:

```java
		tag.putInt("EscalationRings", escalationMarks.rings());
		tag.putInt("EscalationCircuits", escalationMarks.circuits());
		tag.putInt("EscalationFlags", escalationMarks.flags());
		tag.putInt("EscalationStage", escalationStage);
```

In `deserialize(CompoundTag tag, HolderLookup.Provider provider)`, replace the closing lines

```java
		if (removedProfessionPhase) rite.phase = rite.finalePhase();
		return rite;
```

with

```java
		if (removedProfessionPhase) rite.phase = rite.finalePhase();
		if (tag.contains("EscalationFlags")) {
			rite.escalationMarks = new CardinalRiteEscalation.Marks(tag.getInt("EscalationRings"),
					tag.getInt("EscalationCircuits"), tag.getInt("EscalationFlags"));
			rite.escalationStage = tag.getInt("EscalationStage");
		} else {
			CardinalRiteEscalation.rebuild(rite);
		}
		return rite;
```

- [ ] **Step 5: Run the tests to verify they pass**

Run: `./gradlew.bat test --tests "com.vincenthuto.hemomancy.common.rite.CardinalRiteEscalationTest" --tests "com.vincenthuto.hemomancy.common.rite.ActiveCardinalRiteCeremonyTest" --tests "com.vincenthuto.hemomancy.common.rite.PuppeteerTrialRiteStateTest"`
Expected: PASS for all three classes.

- [ ] **Step 6: Checkpoint (no commit)**

Run: `./gradlew.bat compileJava compileTestJava compileGameTestJava`
Expected: BUILD SUCCESSFUL. Deprecation warnings are pre-existing.

### Task 2: Announce each step and sync the stage

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/rite/CardinalRiteEventKind.java` (append `ESCALATION` after `CANCELLED`)
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/init/SoundInit.java` (after `RITE_BACKLASH`)
- Modify: `src/main/resources/assets/hemomancy/sounds.json` (after the `"rite.backlash"` entry)
- Modify: `src/main/resources/assets/hemomancy/lang/en_us.json` (next to `"subtitle.hemomancy.rite.backlash"`)
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/HarbingerCardinalRiteEvents.java` (new `escalate` helper; calls in the succession branch and after `emitAnchorBeats`; `toClientEntry` adds `.withEscalation(...)`)
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/data/ActiveRiteClientData.java` (`RiteEntry` fields, getters, `withEscalation`)
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/PacketSyncActiveRites.java` (encode and decode)
- Test: `src/test/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/PacketSyncActiveRitesEscalationTest.java`
- Test: `src/test/java/com/vincenthuto/hemomancy/client/render/world/rite/escalation/RiteWorldSourceTest.java`

**Interfaces:**
- Consumes: `CardinalRiteEscalation.update(ActiveCardinalRite)`, `ActiveCardinalRite.getEscalationStage()`, `getEscalationTotal()` (Task 1).
- Produces:
  - `CardinalRiteEventKind.ESCALATION`, whose `index` is the new stage
  - `SoundInit.RITE_ESCALATE`
  - `ActiveRiteClientData.RiteEntry.getEscalation()`, `getEscalationTotal()`, `withEscalation(int stage, int total)`

- [ ] **Step 1: Write the failing tests**

`PacketSyncActiveRitesEscalationTest.java`:

```java
package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.client.data.ActiveRiteClientData;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class PacketSyncActiveRitesEscalationTest {
	static ActiveRiteClientData.RiteEntry entry() {
		return new ActiveRiteClientData.RiteEntry(new BlockPos(4, 64, -9), 5, 0.5D,
				ResourceLocation.parse("hemomancy:cardinal_rite/escalation_test"), false,
				"ORDEAL", 0, 0, 1, 1, 2, 0, 0, 0, 0, 0, "",
				List.of(), List.of(), List.of());
	}

	static ActiveRiteClientData.RiteEntry roundTrip(ActiveRiteClientData.RiteEntry entry) {
		FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
		PacketSyncActiveRites.encode(buffer, new PacketSyncActiveRites(List.of(entry)));
		ActiveRiteClientData.RiteEntry copy = PacketSyncActiveRites.decode(buffer).entries().getFirst();
		assertEquals(0, buffer.readableBytes(), "the decoder reads every byte the encoder wrote");
		return copy;
	}

	@Test
	void escalationRoundTrips() {
		ActiveRiteClientData.RiteEntry copy = roundTrip(entry().withEscalation(3, 6));

		assertEquals(3, copy.getEscalation());
		assertEquals(6, copy.getEscalationTotal());
	}

	@Test
	void stageIsClampedToItsTotal() {
		ActiveRiteClientData.RiteEntry entry = entry().withEscalation(9, 4);

		assertEquals(4, entry.getEscalation());
		assertEquals(0, entry().withEscalation(-2, 4).getEscalation());
	}
}
```

`RiteWorldSourceTest.java` (later tasks add more checks to this class):

```java
package com.vincenthuto.hemomancy.client.render.world.rite.escalation;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

final class RiteWorldSourceTest {
	static String read(String path) throws IOException {
		return Files.readString(Path.of(path)).replace("\r\n", "\n");
	}

	@Test
	void everyRiteTickAnnouncesItsStep() throws IOException {
		String events = read("src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/HarbingerCardinalRiteEvents.java");

		int calls = events.split("phaseChanged \\|= escalate\\(sLevel, rite\\);", -1).length - 1;
		assertEquals(2, calls, "succession cues and ceremony ticks both escalate");
		assertTrue(events.contains("CardinalRiteEventKind.ESCALATION, rite.getEscalationStage()"),
				"each step is sent as a beat carrying the new stage");
		assertTrue(events.contains(".withEscalation("), "the stage is synced");
	}
}
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `./gradlew.bat test --tests "com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncActiveRitesEscalationTest" --tests "com.vincenthuto.hemomancy.client.render.world.rite.escalation.RiteWorldSourceTest"`
Expected: compilation FAILS with `cannot find symbol: method withEscalation(int,int)`.

- [ ] **Step 3: Add the beat and the sound**

In `CardinalRiteEventKind`, replace

```java
	/** The caster drew the rite back into the staff. */
	CANCELLED;
```

with

```java
	/** The caster drew the rite back into the staff. */
	CANCELLED,
	/** The rite pushed the world a step further; {@code index} is its new stage. */
	ESCALATION;
```

In `SoundInit`, after the `RITE_BACKLASH` line add:

```java
	public static final DeferredHolder<SoundEvent, SoundEvent> RITE_ESCALATE = registerSoundEvent("rite.escalate");
```

In `sounds.json`, add a comma after the `"rite.backlash"` entry and then this entry, before the closing `}`:

```json
  "rite.escalate": {"subtitle": "subtitle.hemomancy.rite.escalate", "sounds": [{"name": "minecraft:entity.warden.sonic_charge", "type": "event", "pitch": 0.6, "volume": 0.9}]}
```

In `en_us.json`, after `"subtitle.hemomancy.rite.backlash": "Rite backlash roars",` add:

```json
  "subtitle.hemomancy.rite.escalate": "The rite deepens",
```

- [ ] **Step 4: Carry the stage on `RiteEntry`**

In `ActiveRiteClientData.RiteEntry`, after `private int[] anchorStates = new int[0];` add:

```java
		/** The rite's world stage and how many steps it has; see {@code CardinalRiteEscalation}. */
		private int escalation;
		private int escalationTotal;
```

After `withAnchorStates(int[] states)` add:

```java
		public int getEscalation() { return escalation; }
		public int getEscalationTotal() { return escalationTotal; }
		public RiteEntry withEscalation(int stage, int total) {
			escalationTotal = Math.max(0, total);
			escalation = Math.clamp(stage, 0, escalationTotal);
			return this;
		}
```

In `PacketSyncActiveRites.encode`, after `buf.writeVarIntArray(entry.getAnchorStates());` add:

```java
			buf.writeVarInt(entry.getEscalation());
			buf.writeVarInt(entry.getEscalationTotal());
```

In `PacketSyncActiveRites.decode`, replace `fogProfile, fogLightning, boundaryDome, phaseTicks).withAnchorStates(buf.readVarIntArray()));` with:

```java
					fogProfile, fogLightning, boundaryDome, phaseTicks).withAnchorStates(buf.readVarIntArray())
					.withEscalation(buf.readVarInt(), buf.readVarInt()));
```

- [ ] **Step 5: Escalate in the rite loop**

In `HarbingerCardinalRiteEvents`, add this helper next to `usable(ServerPlayer)`:

```java
	/** Raises the rite's world stage when a milestone lands, and lets nearby clients feel the step. */
	private static boolean escalate(ServerLevel level, ActiveCardinalRite rite) {
		if (!CardinalRiteEscalation.update(rite)) return false;
		CardinalRiteBeats.send(level, rite, CardinalRiteEventKind.ESCALATION, rite.getEscalationStage(), "",
				List.of(), ItemStack.EMPTY);
		CardinalRiteBeats.sound(level, rite.getCenterPos(), SoundInit.RITE_ESCALATE, 1.2F, 1.0F);
		return true;
	}
```

In the succession branch, replace

```java
                    toRemove.add(playerUUID);
                }
                savedData.setDirty();
                continue;
```

with

```java
                    toRemove.add(playerUUID);
                } else {
                    phaseChanged |= escalate(sLevel, rite);
                }
                savedData.setDirty();
                continue;
```

Replace

```java
			if (recipe != null && !unstained) emitAnchorBeats(sLevel, rite, recipe);
			phaseChanged |= rite.getPhase() != phaseBeforeTick;
```

with

```java
			if (recipe != null && !unstained) emitAnchorBeats(sLevel, rite, recipe);
			if (recipe != null && !unstained && recipe.hasInteractiveCeremony()) {
				phaseChanged |= escalate(sLevel, rite);
			}
			phaseChanged |= rite.getPhase() != phaseBeforeTick;
```


In `toClientEntry`, replace `fogProfile, fogLightning, boundaryDome, rite.getPhaseTicks()).withAnchorStates(anchorStates(rite));` with:

```java
				fogProfile, fogLightning, boundaryDome, rite.getPhaseTicks()).withAnchorStates(anchorStates(rite))
				.withEscalation(unstained ? 0 : rite.getEscalationStage(), unstained ? 0 : rite.getEscalationTotal());
```

- [ ] **Step 6: Run the tests to verify they pass**

Run: `./gradlew.bat test --tests "com.vincenthuto.hemomancy.common.network.capa.harbinger.*" --tests "com.vincenthuto.hemomancy.client.render.world.rite.escalation.RiteWorldSourceTest"`
Expected: PASS, including the existing `ActiveRiteRoleSyncPacketTest`.

- [ ] **Step 7: Checkpoint (no commit)**

Run: `./gradlew.bat compileJava compileTestJava compileGameTestJava`
Expected: BUILD SUCCESSFUL.

### Task 3: Structured problems and socket markers

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/rite/CardinalRiteProblems.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/data/ActiveRiteClientData.java` (new `SocketMarker` record next to `SigilSegment`; `RiteEntry` fields, getters, `withGuidance`)
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/CardinalRiteInteractionHandler.java` (new `sleepingRequiredSockets`)
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/HarbingerCardinalRiteEvents.java` (`problemFacts` helper; `toClientEntry` adds `.withGuidance(...)`)
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/PacketSyncActiveRites.java`
- Modify: `src/main/resources/assets/hemomancy/lang/en_us.json`
- Test: `src/test/java/com/vincenthuto/hemomancy/common/rite/CardinalRiteProblemsTest.java`
- Test: `src/test/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/PacketSyncActiveRitesEscalationTest.java` (add a test)

**Interfaces:**
- Consumes: `RiteEntry.withEscalation` (Task 2).
- Produces:
  - `CardinalRiteProblems.RiteProblem(String key, List<String> args)`
  - `CardinalRiteProblems.Facts(CardinalRitePhase phase, List<String> sleepingRequiredSigils, int requiredAllies, int availableAllies, boolean mediumReady, int damagedAnchors, int sacrificeOwedMl, boolean heldBeforeFinale)`
  - `static List<RiteProblem> CardinalRiteProblems.of(Facts)`, `static String CardinalRiteProblems.readable(String id)`
  - Keys `CardinalRiteProblems.SIGIL_REQUIRED`, `ALLIES`, `MEDIUM`, `ANCHOR_DAMAGED`, `SACRIFICE`, `FINALE_HELD`
  - `ActiveRiteClientData.SocketMarker(BlockPos pos, String sigil)`
  - `RiteEntry.getProblems()`, `getSocketMarkers()`, `withGuidance(List<RiteProblem>, List<SocketMarker>)`
  - `CardinalRiteInteractionHandler.sleepingRequiredSockets(ServerLevel, ActiveCardinalRite, CardinalRiteRecipe)`

- [ ] **Step 1: Write the failing tests**

`CardinalRiteProblemsTest.java`:

```java
package com.vincenthuto.hemomancy.common.rite;

import org.junit.jupiter.api.Test;

import java.util.List;

import static com.vincenthuto.hemomancy.common.rite.CardinalRiteProblems.*;
import static org.junit.jupiter.api.Assertions.*;

final class CardinalRiteProblemsTest {
	private static Facts calm(CardinalRitePhase phase) {
		return new Facts(phase, List.of(), 0, 0, true, 0, 0, false);
	}

	private static List<String> keys(List<RiteProblem> problems) {
		return problems.stream().map(RiteProblem::key).toList();
	}

	@Test
	void aReadyRiteHasNoProblems() {
		for (CardinalRitePhase phase : CardinalRitePhase.values()) {
			assertTrue(of(calm(phase)).isEmpty(), phase.name());
		}
	}

	@Test
	void inscriptionNamesEachSleepingSigilThenAlliesThenMedium() {
		List<RiteProblem> problems = of(new Facts(CardinalRitePhase.INSCRIPTION,
				List.of("Bastion", "Hematic Lattice"), 3, 1, false, 0, 0, false));

		assertEquals(List.of(SIGIL_REQUIRED, SIGIL_REQUIRED, ALLIES, MEDIUM), keys(problems));
		assertEquals(List.of("Bastion"), problems.get(0).args());
		assertEquals(List.of("3", "1"), problems.get(2).args());
	}

	@Test
	void sigilsAlliesAndMediumOnlyMatterDuringInscription() {
		assertTrue(of(new Facts(CardinalRitePhase.ORDEAL, List.of("Bastion"), 3, 0, false, 0, 0, false)).isEmpty());
	}

	@Test
	void damagedAnchorsMatterWhileAnchorsCanBeFed() {
		for (CardinalRitePhase phase : List.of(CardinalRitePhase.CONSECRATION, CardinalRitePhase.ORDEAL,
				CardinalRitePhase.STILL_INTERVAL)) {
			assertEquals(List.of(ANCHOR_DAMAGED),
					keys(of(new Facts(phase, List.of(), 0, 0, true, 2, 0, false))), phase.name());
		}
		assertTrue(of(new Facts(CardinalRitePhase.CULMINATION, List.of(), 0, 0, true, 2, 0, false)).isEmpty());
	}

	@Test
	void anOwedSacrificeLeadsUntilTheSeal() {
		List<RiteProblem> problems = of(new Facts(CardinalRitePhase.INSCRIPTION, List.of(), 0, 0, false, 0, 120, false));

		assertEquals(List.of(SACRIFICE, MEDIUM), keys(problems));
		assertEquals(List.of("120"), problems.getFirst().args());
		assertTrue(of(new Facts(CardinalRitePhase.ORDEAL, List.of(), 0, 0, true, 0, 120, false)).isEmpty());
	}

	@Test
	void aHeldFinaleIsReported() {
		assertEquals(List.of(FINALE_HELD),
				keys(of(new Facts(CardinalRitePhase.STILL_INTERVAL, List.of(), 0, 0, true, 0, 0, true))));
	}

	@Test
	void problemsAreCappedAtSix() {
		List<RiteProblem> problems = of(new Facts(CardinalRitePhase.INSCRIPTION,
				List.of("a", "b", "c", "d", "e", "f", "g"), 0, 0, true, 0, 0, false));

		assertEquals(6, problems.size());
	}

	@Test
	void sigilIdsReadAsTitleCase() {
		assertEquals("Hematic Lattice", readable("hematic_lattice"));
		assertEquals("Bastion", readable("bastion"));
		assertEquals("", readable(""));
	}
}
```

Add to `PacketSyncActiveRitesEscalationTest`:

```java
	@Test
	void problemsAndSocketMarkersRoundTrip() {
		ActiveRiteClientData.RiteEntry entry = entry().withEscalation(1, 4).withGuidance(
				List.of(new com.vincenthuto.hemomancy.common.rite.CardinalRiteProblems.RiteProblem(
						com.vincenthuto.hemomancy.common.rite.CardinalRiteProblems.ALLIES, List.of("3", "1"))),
				List.of(new ActiveRiteClientData.SocketMarker(new BlockPos(10, 65, -9), "bastion")));

		ActiveRiteClientData.RiteEntry copy = roundTrip(entry);

		assertEquals(entry.getProblems(), copy.getProblems());
		assertEquals(entry.getSocketMarkers(), copy.getSocketMarkers());
		assertEquals(1, copy.getEscalation());
	}
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `./gradlew.bat test --tests "com.vincenthuto.hemomancy.common.rite.CardinalRiteProblemsTest" --tests "com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncActiveRitesEscalationTest"`
Expected: compilation FAILS with `cannot find symbol: class CardinalRiteProblems`.

- [ ] **Step 3: Create `CardinalRiteProblems`**

```java
package com.vincenthuto.hemomancy.common.rite;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * What stands between the caster and the rite's next step, as translatable lines. The client shows the
 * first new one in the action bar instead of a panel of English text.
 */
public final class CardinalRiteProblems {
	public static final String SIGIL_REQUIRED = "hemomancy.rite.problem.sigil_required";
	public static final String ALLIES = "hemomancy.rite.problem.allies";
	public static final String MEDIUM = "hemomancy.rite.problem.medium";
	public static final String ANCHOR_DAMAGED = "hemomancy.rite.problem.anchor_damaged";
	public static final String SACRIFICE = "hemomancy.rite.problem.sacrifice";
	public static final String FINALE_HELD = "hemomancy.rite.problem.finale_held";
	public static final int MAX_PROBLEMS = 6;

	private CardinalRiteProblems() {
	}

	public record RiteProblem(String key, List<String> args) {
		public RiteProblem {
			key = key == null ? "" : key;
			args = args == null ? List.of() : List.copyOf(args);
		}
	}

	/** The live facts the problems are read from, gathered by the server. */
	public record Facts(CardinalRitePhase phase, List<String> sleepingRequiredSigils, int requiredAllies,
			int availableAllies, boolean mediumReady, int damagedAnchors, int sacrificeOwedMl,
			boolean heldBeforeFinale) {
		public Facts {
			sleepingRequiredSigils = sleepingRequiredSigils == null ? List.of() : List.copyOf(sleepingRequiredSigils);
		}
	}

	public static List<RiteProblem> of(Facts facts) {
		List<RiteProblem> problems = new ArrayList<>();
		CardinalRitePhase phase = facts.phase();
		boolean beforeSeal = phase == CardinalRitePhase.CONSECRATION || phase == CardinalRitePhase.INSCRIPTION;
		if (beforeSeal && facts.sacrificeOwedMl() > 0) {
			problems.add(new RiteProblem(SACRIFICE, List.of(Integer.toString(facts.sacrificeOwedMl()))));
		}
		if (phase == CardinalRitePhase.INSCRIPTION) {
			for (String sigil : facts.sleepingRequiredSigils()) {
				problems.add(new RiteProblem(SIGIL_REQUIRED, List.of(sigil)));
			}
			if (facts.availableAllies() < facts.requiredAllies()) {
				problems.add(new RiteProblem(ALLIES, List.of(Integer.toString(facts.requiredAllies()),
						Integer.toString(facts.availableAllies()))));
			}
			if (!facts.mediumReady()) problems.add(new RiteProblem(MEDIUM, List.of()));
		}
		boolean feeding = phase == CardinalRitePhase.CONSECRATION || phase == CardinalRitePhase.ORDEAL
				|| phase == CardinalRitePhase.STILL_INTERVAL;
		if (feeding && facts.damagedAnchors() > 0) problems.add(new RiteProblem(ANCHOR_DAMAGED, List.of()));
		if (facts.heldBeforeFinale()) problems.add(new RiteProblem(FINALE_HELD, List.of()));
		return List.copyOf(problems.size() > MAX_PROBLEMS ? problems.subList(0, MAX_PROBLEMS) : problems);
	}

	/** {@code hematic_lattice} reads as "Hematic Lattice". */
	public static String readable(String id) {
		if (id == null || id.isBlank()) return "";
		StringBuilder result = new StringBuilder();
		for (String word : id.toLowerCase(Locale.ROOT).split("_")) {
			if (word.isEmpty()) continue;
			if (!result.isEmpty()) result.append(' ');
			result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
		}
		return result.toString();
	}
}
```

- [ ] **Step 4: Carry problems and markers on `RiteEntry`**

In `ActiveRiteClientData`, next to the `SigilSegment` record add:

```java
	/** A required support socket whose sigil still sleeps; the client raises a blood thread over it. */
	public record SocketMarker(BlockPos pos, String sigil) {
		public SocketMarker {
			sigil = sigil == null ? "" : sigil;
		}
	}
```

In `RiteEntry`, after the escalation fields add:

```java
		private List<com.vincenthuto.hemomancy.common.rite.CardinalRiteProblems.RiteProblem> problems = List.of();
		private List<SocketMarker> socketMarkers = List.of();
```

After `withEscalation` add:

```java
		public List<com.vincenthuto.hemomancy.common.rite.CardinalRiteProblems.RiteProblem> getProblems() {
			return problems;
		}
		public List<SocketMarker> getSocketMarkers() { return socketMarkers; }
		public RiteEntry withGuidance(List<com.vincenthuto.hemomancy.common.rite.CardinalRiteProblems.RiteProblem> problems,
				List<SocketMarker> markers) {
			this.problems = problems == null ? List.of() : List.copyOf(problems);
			this.socketMarkers = markers == null ? List.of() : List.copyOf(markers);
			return this;
		}
```

In `PacketSyncActiveRites.encode`, after the two escalation `writeVarInt` lines add:

```java
			buf.writeVarInt(entry.getProblems().size());
			for (var problem : entry.getProblems()) {
				buf.writeUtf(problem.key());
				buf.writeVarInt(problem.args().size());
				for (String arg : problem.args()) buf.writeUtf(arg);
			}
			buf.writeVarInt(entry.getSocketMarkers().size());
			for (ActiveRiteClientData.SocketMarker marker : entry.getSocketMarkers()) {
				buf.writeBlockPos(marker.pos());
				buf.writeUtf(marker.sigil());
			}
```

In `decode`, replace `.withEscalation(buf.readVarInt(), buf.readVarInt()));` with:

```java
					.withEscalation(buf.readVarInt(), buf.readVarInt())
					.withGuidance(readProblems(buf), readSocketMarkers(buf)));
```

and add to the class:

```java
	private static final int MAX_SYNCED_PROBLEMS = 16;
	private static final int MAX_SYNCED_MARKERS = 32;

	private static List<com.vincenthuto.hemomancy.common.rite.CardinalRiteProblems.RiteProblem> readProblems(
			FriendlyByteBuf buf) {
		int count = buf.readVarInt();
		List<com.vincenthuto.hemomancy.common.rite.CardinalRiteProblems.RiteProblem> problems = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			String key = buf.readUtf();
			int argCount = buf.readVarInt();
			List<String> args = new ArrayList<>(Math.min(argCount, 8));
			for (int arg = 0; arg < argCount; arg++) args.add(buf.readUtf());
			if (i < MAX_SYNCED_PROBLEMS) {
				problems.add(new com.vincenthuto.hemomancy.common.rite.CardinalRiteProblems.RiteProblem(key, args));
			}
		}
		return problems;
	}

	private static List<ActiveRiteClientData.SocketMarker> readSocketMarkers(FriendlyByteBuf buf) {
		int count = buf.readVarInt();
		List<ActiveRiteClientData.SocketMarker> markers = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			BlockPos pos = buf.readBlockPos();
			String sigil = buf.readUtf();
			if (i < MAX_SYNCED_MARKERS) markers.add(new ActiveRiteClientData.SocketMarker(pos, sigil));
		}
		return markers;
	}
```

(The loops read every element even past the caps, so a malformed count never desynchronizes the buffer.)

- [ ] **Step 5: Gather the facts and markers on the server**

In `CardinalRiteInteractionHandler`, add (with imports `com.vincenthuto.hemomancy.client.data.ActiveRiteClientData`, `com.vincenthuto.hemomancy.common.rite.sigil.CardinalRiteSigilRules` and `com.vincenthuto.hemomancy.common.rite.CardinalRiteCeremonyDefinition` if missing):

```java
	/** World positions of required support sockets whose sigil still sleeps, for the client's markers. */
	public static List<ActiveRiteClientData.SocketMarker> sleepingRequiredSockets(ServerLevel level,
			ActiveCardinalRite rite, CardinalRiteRecipe recipe) {
		if (rite.getPhase() != CardinalRitePhase.INSCRIPTION || recipe == null || recipe.getCeremony() == null) {
			return List.of();
		}
		List<CardinalRiteCeremonyDefinition.SupportSocket> sockets = recipe.getCeremony().supportSockets();
		List<SigilPlacement> placements = supportSigils(recipe);
		List<ActiveRiteClientData.SocketMarker> markers = new ArrayList<>();
		for (int i = 0; i < sockets.size() && i < placements.size(); i++) {
			SigilPlacement placement = placements.get(i);
			if (!sockets.get(i).required() || rite.isSigilAwakened(placement.progressKey())) continue;
			BlockPos surface = CardinalRiteSigilRules.surfaceAirPosition(level,
					rite.getCenterPos().offset(0, placement.y(), 0), placement.x(), placement.z());
			markers.add(new ActiveRiteClientData.SocketMarker(surface, sockets.get(i).suggestedSigil()));
		}
		return markers;
	}
```

In `HarbingerCardinalRiteEvents`, add next to `buildChecklist`:

```java
	private static CardinalRiteProblems.Facts problemFacts(ServerLevel level, ActiveCardinalRite rite,
			CardinalRiteRecipe recipe) {
		List<String> sleeping = new ArrayList<>();
		int requiredAllies = 0;
		int availableAllies = 0;
		boolean mediumReady = true;
		if (rite.getPhase() == CardinalRitePhase.INSCRIPTION && recipe != null && recipe.getCeremony() != null) {
			for (var socket : recipe.getCeremony().supportSockets()) {
				if (socket.required() && !rite.isSigilAwakened(Hemomancy.rloc(socket.suggestedSigil()).toString())) {
					sleeping.add(CardinalRiteProblems.readable(socket.suggestedSigil()));
				}
			}
			requiredAllies = recipe.getCeremony().requiredHelpers();
			availableAllies = (int) rite.getAllyRoles().keySet().stream()
					.filter(ally -> CardinalRiteAllyService.isAvailable(level, rite, ally)).count();
			mediumReady = CardinalRiteStationMatcher.mediumMatches(level, rite.getCenterPos(), recipe);
		}
		int owed = CardinalRiteThreatRules.allowsPassiveSacrifice(rite.getRecipeId().getPath())
				? Math.max(0, CardinalRiteThreatRules.SACRIFICE_REQUIRED_ML - rite.getSacrificeMl()) : 0;
		return new CardinalRiteProblems.Facts(rite.getPhase(), sleeping, requiredAllies, availableAllies, mediumReady,
				rite.getBrokenInstabilityAnchors().size(), owed,
				rite.getPhase() == CardinalRitePhase.STILL_INTERVAL && rite.isHeldBeforeFinale());
	}
```

In `toClientEntry`, replace the `.withEscalation(...)` line added in Task 2 with:

```java
				.withEscalation(unstained ? 0 : rite.getEscalationStage(), unstained ? 0 : rite.getEscalationTotal())
				.withGuidance(unstained || succession ? List.of() : CardinalRiteProblems.of(problemFacts(level, rite, recipe)),
						unstained ? List.of() : CardinalRiteInteractionHandler.sleepingRequiredSockets(level, rite, recipe));
```

- [ ] **Step 6: Add the problem lines to `en_us.json`**

Next to the other `hemomancy.rite.` keys add:

```json
  "hemomancy.rite.problem.sigil_required": "A required sigil sleeps: %s",
  "hemomancy.rite.problem.allies": "The rite needs %s attendants; %s stand ready.",
  "hemomancy.rite.problem.medium": "The Focus holds the wrong medium.",
  "hemomancy.rite.problem.anchor_damaged": "A broken sore must be refilled.",
  "hemomancy.rite.problem.sacrifice": "The circle wants %s mL more from a named creature.",
  "hemomancy.rite.problem.finale_held": "The rite waits. Resolve what it lacks, or recall it with Blood Absorption.",
```

- [ ] **Step 7: Run the tests to verify they pass**

Run: `./gradlew.bat test --tests "com.vincenthuto.hemomancy.common.rite.CardinalRiteProblemsTest" --tests "com.vincenthuto.hemomancy.common.network.capa.harbinger.*"`
Expected: PASS.

- [ ] **Step 8: Checkpoint (no commit)**

Run: `./gradlew.bat compileJava compileTestJava compileGameTestJava`
Expected: BUILD SUCCESSFUL.

### Task 4: Panel off by default, owner guidance in the action bar

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/config/HemoClientConfig.java` (enum, field, definition in the `world_rendering` block, accessor)
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/screen/overlay/CardinalRiteOverlay.java` (gate at the top of `renderHUD`)
- Create: `src/main/java/com/vincenthuto/hemomancy/client/render/world/rite/escalation/RiteGuidance.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java` (`onClientTickPost`, `onClientPlayerLogout`, `onClientLevelUnload`)
- Modify: `src/main/resources/assets/hemomancy/lang/en_us.json`
- Test: `src/test/java/com/vincenthuto/hemomancy/client/render/world/rite/escalation/RiteGuidanceTest.java`
- Test: `src/test/java/com/vincenthuto/hemomancy/client/render/world/rite/escalation/RiteWorldSourceTest.java` (add a check)

**Interfaces:**
- Consumes: `RiteEntry.getProblems()`, `CardinalRiteProblems.RiteProblem`, `CardinalRiteProblems.SIGIL_REQUIRED` (Task 3).
- Produces:
  - `HemoClientConfig.CardinalRiteHudMode { OFF, FULL }`, `HemoClientConfig.CARDINAL_RITE_HUD`, `static CardinalRiteHudMode HemoClientConfig.cardinalRiteHud()`
  - `RiteGuidance.tick(Minecraft)`, `RiteGuidance.clear()`
  - Package-private for tests: `RiteGuidance.Line(String key, List<String> args)`, `RiteGuidance.Seen`, `RiteGuidance.Decision(Seen seen, Line line)`, `static Decision decide(Seen, String phase, String cue, boolean succession, List<RiteProblem>, long now)`, `static String phaseKey(String phase)`, `static boolean listensTo(RiteEntry, UUID)`, `QUIET_TICKS`

- [ ] **Step 1: Write the failing tests**

`RiteGuidanceTest.java`:

```java
package com.vincenthuto.hemomancy.client.render.world.rite.escalation;

import com.vincenthuto.hemomancy.client.data.ActiveRiteClientData;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteProblems;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteProblems.RiteProblem;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

final class RiteGuidanceTest {
	private static final RiteProblem MEDIUM = new RiteProblem(CardinalRiteProblems.MEDIUM, List.of());

	private static RiteGuidance.Decision decide(RiteGuidance.Seen seen, String phase, List<RiteProblem> problems,
			long now) {
		return RiteGuidance.decide(seen, phase, "", false, problems, now);
	}

	@Test
	void aNewPhaseSpeaksFirstAndProblemsWaitTheirTurn() {
		RiteGuidance.Decision first = decide(RiteGuidance.Seen.NONE, "INSCRIPTION", List.of(MEDIUM), 0);
		assertEquals("hemomancy.rite.guide.inscription", first.line().key());

		RiteGuidance.Decision waiting = decide(first.seen(), "INSCRIPTION", List.of(MEDIUM), 10);
		assertNull(waiting.line(), "the phase line keeps the action bar for a while");

		RiteGuidance.Decision problem = decide(waiting.seen(), "INSCRIPTION", List.of(MEDIUM),
				RiteGuidance.QUIET_TICKS + 1);
		assertEquals(CardinalRiteProblems.MEDIUM, problem.line().key());
	}

	@Test
	void aProblemIsAnnouncedOnceUntilItIsResolved() {
		RiteGuidance.Seen seen = decide(RiteGuidance.Seen.NONE, "INSCRIPTION", List.of(MEDIUM), 0).seen();
		seen = decide(seen, "INSCRIPTION", List.of(MEDIUM), 100).seen();
		assertNull(decide(seen, "INSCRIPTION", List.of(MEDIUM), 300).line(), "no repeats");

		seen = decide(seen, "INSCRIPTION", List.of(), 400).seen();
		assertEquals(CardinalRiteProblems.MEDIUM, decide(seen, "INSCRIPTION", List.of(MEDIUM), 500).line().key(),
				"a problem that comes back is announced again");
	}

	@Test
	void sacrificeProgressDoesNotRepeatTheLine() {
		RiteProblem owed = new RiteProblem(CardinalRiteProblems.SACRIFICE, List.of("200"));
		RiteGuidance.Seen seen = decide(RiteGuidance.Seen.NONE, "OTHER", List.of(owed), 0).seen();
		RiteProblem less = new RiteProblem(CardinalRiteProblems.SACRIFICE, List.of("150"));

		assertNull(decide(seen, "OTHER", List.of(less), 500).line());
	}

	@Test
	void eachSleepingSigilIsItsOwnProblem() {
		RiteProblem bastion = new RiteProblem(CardinalRiteProblems.SIGIL_REQUIRED, List.of("Bastion"));
		RiteProblem lattice = new RiteProblem(CardinalRiteProblems.SIGIL_REQUIRED, List.of("Hematic Lattice"));
		RiteGuidance.Seen seen = decide(RiteGuidance.Seen.NONE, "OTHER", List.of(bastion, lattice), 0).seen();

		assertEquals(List.of("Hematic Lattice"), decide(seen, "OTHER", List.of(bastion, lattice), 500).line().args());
	}

	@Test
	void successionCuesSpeakWhenTheyChange() {
		RiteGuidance.Decision first = RiteGuidance.decide(RiteGuidance.Seen.NONE, "SUCCESSION",
				"hemomancy.succession.phase.1", true, List.of(), 0);
		assertEquals("hemomancy.succession.phase.1", first.line().key());
		assertNull(RiteGuidance.decide(first.seen(), "SUCCESSION", "hemomancy.succession.phase.1", true,
				List.of(), 500).line());
		assertEquals("hemomancy.succession.phase.2", RiteGuidance.decide(first.seen(), "SUCCESSION",
				"hemomancy.succession.phase.2", true, List.of(), 500).line().key());
	}

	@Test
	void onlyPhasesWithAnInstructionHaveALine() {
		assertEquals("hemomancy.rite.guide.consecration", RiteGuidance.phaseKey("CONSECRATION"));
		assertEquals("hemomancy.rite.guide.offering_procession", RiteGuidance.phaseKey("OFFERING_PROCESSION"));
		assertNull(RiteGuidance.phaseKey("ORDEAL"), "waves keep their own cues");
		assertNull(RiteGuidance.phaseKey("COMPLETE"));
		assertNull(RiteGuidance.phaseKey("SUCCESSION"));
	}

	@Test
	void onlyTheRitesOwnerHearsIt() {
		UUID owner = UUID.randomUUID();
		ActiveRiteClientData.RiteEntry entry = new ActiveRiteClientData.RiteEntry(BlockPos.ZERO, 5, 0.0D,
				ResourceLocation.parse("hemomancy:cardinal_rite/guidance_test"), false, "INSCRIPTION", 0, 0, 0, 1, 1,
				0, 0, 0, 0, 0, "", 6.0F, List.of(), List.of(), List.of(), List.of(), true, owner, 0, -1,
				"storm", true, true, 0);

		assertTrue(RiteGuidance.listensTo(entry, owner));
		assertFalse(RiteGuidance.listensTo(entry, UUID.randomUUID()));
		assertFalse(RiteGuidance.listensTo(entry, null));
	}
}
```

Add to `RiteWorldSourceTest`:

```java
	@Test
	void thePanelOnlyDrawsWhenTheConfigAsksForIt() throws IOException {
		String overlay = read("src/main/java/com/vincenthuto/hemomancy/client/screen/overlay/CardinalRiteOverlay.java");

		assertTrue(overlay.contains("if (HemoClientConfig.cardinalRiteHud() != HemoClientConfig.CardinalRiteHudMode.FULL) return;"));
	}
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `./gradlew.bat test --tests "com.vincenthuto.hemomancy.client.render.world.rite.escalation.*"`
Expected: compilation FAILS with `cannot find symbol: class RiteGuidance`.

- [ ] **Step 3: Add the config option**

In `HemoClientConfig`, after `public static ModConfigSpec.BooleanValue USE_LOW_POLY_VESPER_FIGHT_OUTER_RING;` add:

```java
	public static ModConfigSpec.EnumValue<CardinalRiteHudMode> CARDINAL_RITE_HUD;
```

After the `MorphlingHudMode` enum add:

```java
	public enum CardinalRiteHudMode {
		OFF,
		FULL
	}
```

In the `world_rendering` block, after the `CARDINAL_RITE_FOG_VERTICAL_OFFSET` definition add:

```java
		CARDINAL_RITE_HUD = CLIENT_BUILDER
				.comment("Cardinal Rite status panel. OFF (default) lets the world show the rite: its sores, the world stage, visible strain and short action-bar lines. FULL restores the panel at the top of the screen.")
				.defineEnum("cardinalRiteHud", CardinalRiteHudMode.OFF);
```

After `useTexturedDialogueStyle()` add:

```java
	public static CardinalRiteHudMode cardinalRiteHud() {
		return CARDINAL_RITE_HUD == null ? CardinalRiteHudMode.OFF : CARDINAL_RITE_HUD.get();
	}
```

- [ ] **Step 4: Gate the panel**

In `CardinalRiteOverlay.renderHUD`, make the first line of the method body:

```java
		if (HemoClientConfig.cardinalRiteHud() != HemoClientConfig.CardinalRiteHudMode.FULL) return;
```

and add `import com.vincenthuto.hemomancy.config.HemoClientConfig;`.

- [ ] **Step 5: Create `RiteGuidance`**

```java
package com.vincenthuto.hemomancy.client.render.world.rite.escalation;

import com.vincenthuto.hemomancy.client.data.ActiveRiteClientData;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteProblems;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteProblems.RiteProblem;
import com.vincenthuto.hemomancy.config.HemoClientConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * With the rite panel off, the rite's owner hears from it in the action bar instead: one line when a phase
 * begins, then each new problem in turn. A line keeps the action bar for {@link #QUIET_TICKS} before the next.
 */
public final class RiteGuidance {
	/** Long enough for the vanilla action bar to show a line and fade it. */
	static final int QUIET_TICKS = 70;
	private static final Map<BlockPos, Seen> SEEN = new HashMap<>();
	private static long ticks;

	private RiteGuidance() {
	}

	record Line(String key, List<String> args) {
	}

	/** What the owner has already been told about one rite. Problems are remembered by identity. */
	record Seen(String phase, String cue, List<String> announced, long quietUntil) {
		static final Seen NONE = new Seen("", "", List.of(), Long.MIN_VALUE);
	}

	record Decision(Seen seen, Line line) {
	}

	public static void tick(Minecraft minecraft) {
		ticks++;
		if (minecraft.player == null || minecraft.level == null
				|| HemoClientConfig.cardinalRiteHud() == HemoClientConfig.CardinalRiteHudMode.FULL) {
			SEEN.clear();
			return;
		}
		UUID player = minecraft.player.getUUID();
		List<BlockPos> live = new ArrayList<>();
		for (ActiveRiteClientData.RiteEntry entry : ActiveRiteClientData.getActiveRites()) {
			if (!listensTo(entry, player)) continue;
			live.add(entry.getCenter());
			Decision decision = decide(SEEN.getOrDefault(entry.getCenter(), Seen.NONE), entry.getPhase(),
					entry.getCue(), entry.isSuccession(), entry.getProblems(), ticks);
			SEEN.put(entry.getCenter(), decision.seen());
			if (decision.line() != null) {
				minecraft.player.displayClientMessage(Component.translatable(decision.line().key(),
						decision.line().args().toArray()).withStyle(ChatFormatting.DARK_RED), true);
			}
		}
		SEEN.keySet().retainAll(live);
	}

	public static void clear() {
		SEEN.clear();
	}

	static boolean listensTo(ActiveRiteClientData.RiteEntry entry, UUID player) {
		return player != null && player.equals(entry.getOwner()) && !entry.isUnstained() && !entry.isLegacy();
	}

	/** The guide line for a phase, or null when the phase speaks for itself. */
	static String phaseKey(String phase) {
		return switch (phase == null ? "" : phase) {
			case "CONSECRATION", "INSCRIPTION", "STATION_PROJECTION", "PUPPET_TRIAL", "STILL_INTERVAL",
					"OFFERING_PROCESSION", "CULMINATION" -> "hemomancy.rite.guide." + phase.toLowerCase(Locale.ROOT);
			default -> null;
		};
	}

	/** Each sleeping sigil is its own problem; any other problem is the same however its numbers move. */
	private static String identity(RiteProblem problem) {
		return CardinalRiteProblems.SIGIL_REQUIRED.equals(problem.key())
				? problem.key() + problem.args() : problem.key();
	}

	static Decision decide(Seen seen, String phase, String cue, boolean succession, List<RiteProblem> problems,
			long now) {
		List<String> current = problems.stream().map(RiteGuidance::identity).toList();
		// Forget problems that were resolved, so they are announced again if they return.
		List<String> announced = seen.announced().stream().filter(current::contains).toList();
		Seen kept = new Seen(seen.phase(), seen.cue(), announced, seen.quietUntil());
		if (now < seen.quietUntil()) return new Decision(kept, null);
		String safePhase = phase == null ? "" : phase;
		if (!safePhase.equals(kept.phase())) {
			String key = phaseKey(safePhase);
			kept = new Seen(safePhase, kept.cue(), announced, key == null ? kept.quietUntil() : now + QUIET_TICKS);
			if (key != null) return new Decision(kept, new Line(key, List.of()));
		}
		String safeCue = cue == null ? "" : cue;
		if (succession && !safeCue.isBlank() && !safeCue.equals(kept.cue())) {
			return new Decision(new Seen(kept.phase(), safeCue, announced, now + QUIET_TICKS),
					new Line(safeCue, List.of()));
		}
		for (int i = 0; i < problems.size(); i++) {
			if (announced.contains(current.get(i))) continue;
			List<String> next = new ArrayList<>(announced);
			next.add(current.get(i));
			return new Decision(new Seen(kept.phase(), kept.cue(), List.copyOf(next), now + QUIET_TICKS),
					new Line(problems.get(i).key(), problems.get(i).args()));
		}
		return new Decision(kept, null);
	}
}
```

- [ ] **Step 6: Hook it into the client lifecycle**

In `ClientEvents.onClientTickPost`, after `com.vincenthuto.hemomancy.client.data.CardinalRiteEventClientState.tick();` add:

```java
        com.vincenthuto.hemomancy.client.render.world.rite.escalation.RiteGuidance.tick(Minecraft.getInstance());
```

In both `onClientPlayerLogout` and `onClientLevelUnload`, after `com.vincenthuto.hemomancy.client.data.CardinalRiteEventClientState.clear();` add:

```java
		com.vincenthuto.hemomancy.client.render.world.rite.escalation.RiteGuidance.clear();
```

- [ ] **Step 7: Add the guide lines to `en_us.json`**

Next to the `hemomancy.rite.problem.` keys add:

```json
  "hemomancy.rite.guide.consecration": "The sores hunger. Project blood into them.",
  "hemomancy.rite.guide.inscription": "The circle is fed. Trace its sigils, then seal it at the Focus.",
  "hemomancy.rite.guide.station_projection": "Feed the station's circuits through the Focus.",
  "hemomancy.rite.guide.puppet_trial": "Break the manifested puppet.",
  "hemomancy.rite.guide.still_interval": "Be still. The daemon gathers.",
  "hemomancy.rite.guide.offering_procession": "The floor draws your offerings in.",
  "hemomancy.rite.guide.culmination": "The rite culminates.",
```

- [ ] **Step 8: Run the tests to verify they pass**

Run: `./gradlew.bat test --tests "com.vincenthuto.hemomancy.client.render.world.rite.escalation.*" --tests "com.vincenthuto.hemomancy.client.screen.overlay.*"`
Expected: PASS. Any existing overlay test still passes, because nothing changed below the new gate.

- [ ] **Step 9: Checkpoint (no commit)**

Run: `./gradlew.bat compileJava compileTestJava compileGameTestJava`
Expected: BUILD SUCCESSFUL.

### Task 5: World state and the layers' maths

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/client/render/world/rite/escalation/RiteWorldMath.java`
- Create: `src/main/java/com/vincenthuto/hemomancy/client/render/world/rite/escalation/RiteFloor.java`
- Create: `src/main/java/com/vincenthuto/hemomancy/client/render/world/rite/escalation/RiteWorldState.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java` (`onClientTickPost`, `onClientPlayerLogout`, `onClientLevelUnload`)
- Test: `src/test/java/com/vincenthuto/hemomancy/client/render/world/rite/escalation/RiteWorldMathTest.java`
- Test: `src/test/java/com/vincenthuto/hemomancy/client/render/world/rite/escalation/RiteWorldStateTest.java`

**Interfaces:**
- Consumes: `RiteEntry.getEscalation()`, `getEscalationTotal()` (Task 2); `CardinalRiteEventClientState.Ghost`; `CardinalRiteFloorVisualRules.Palette`; `CardinalRiteCeremonyRules.instabilityBand(int)`; `CardinalRiteMath.heartbeat(float)`.
- Produces:
  - `RiteWorldMath`, every function listed in the code below, plus `enum RiteWorldMath.Ending { NONE, COMPLETED, COLLAPSED, CANCELLED, VANISHED }`
  - `record RiteFloor(Palette palette, boolean formed, int half)` with `RiteFloor.NONE`, `static RiteFloor at(Level, BlockPos)`, `float edge()`
  - `RiteWorldState.World(...)` with `level(float partialTick)`, `ended()`, `still()`, `culminating()`
  - `RiteWorldState.tick(List<RiteEntry>, List<Ghost>, Function<BlockPos, RiteFloor>)`, `worlds()`, `at(BlockPos)`, `clear()`
  - `RiteWorldState.Flame(float height, float leanX, float leanZ, boolean sanguine)` with `Flame.CALM`; `static Flame flameAt(BlockPos, double time)`
  - `static float[] RiteWorldState.bloodUniform(World, float partialTick)`, which Task 7 loads into the floor shader

- [ ] **Step 1: Write the failing tests**

`RiteWorldMathTest.java`:

```java
package com.vincenthuto.hemomancy.client.render.world.rite.escalation;

import com.vincenthuto.hemomancy.client.render.world.rite.escalation.RiteWorldMath.Ending;
import org.junit.jupiter.api.Test;

import static com.vincenthuto.hemomancy.common.rite.CardinalRiteInstability.*;
import static org.junit.jupiter.api.Assertions.*;

final class RiteWorldMathTest {
	@Test
	void aFourStepRiteBringsEachLayerOnItsStep() {
		float ring = RiteWorldMath.level(1, 4), seal = RiteWorldMath.level(2, 4), procession = RiteWorldMath.level(3, 4);
		assertEquals(0.0F, RiteWorldMath.arrival(ring, RiteWorldMath.CREEP_FROM), "no creep at the first ring");
		assertEquals(1.0F, RiteWorldMath.arrival(seal, RiteWorldMath.CREEP_FROM), 1e-6F);
		assertEquals(1.0F, RiteWorldMath.woundPresence(seal), 1e-6F, "the wound forms at the seal");
		assertEquals(0.0F, RiteWorldMath.rainLevel(seal));
		assertEquals(0.3F, RiteWorldMath.rainLevel(procession), 1e-6F, "rain arrives already falling");
	}

	@Test
	void anElevenStepRiteBuildsMoreGradually() {
		assertEquals(0.0F, RiteWorldMath.arrival(RiteWorldMath.level(3, 11), RiteWorldMath.CREEP_FROM));
		assertEquals(1.0F, RiteWorldMath.arrival(RiteWorldMath.level(4, 11), RiteWorldMath.CREEP_FROM), 1e-6F);
		assertEquals(0.0F, RiteWorldMath.woundPresence(RiteWorldMath.level(5, 11)));
		assertEquals(1.0F, RiteWorldMath.woundPresence(RiteWorldMath.level(6, 11)), 1e-6F);
	}

	@Test
	void viewerWeightFallsOffBeyondTheBoundary() {
		assertEquals(1.0F, RiteWorldMath.viewerWeight(10.0D, 8.0F));
		assertEquals(0.5F, RiteWorldMath.viewerWeight(26.0D, 8.0F), 1e-6F);
		assertEquals(0.0F, RiteWorldMath.viewerWeight(40.0D, 8.0F));
	}

	@Test
	void fogNeverClosesInsideTwentyFourBlocks() {
		float scale = RiteWorldMath.farPlaneScale(1.0F, "storm", RUPTURING);
		assertEquals(0.52F, scale, 1e-6F);
		assertEquals(24.0F, RiteWorldMath.applyFarPlane(30.0F, scale, 1.0F), 1e-6F);
		assertEquals(104.0F, RiteWorldMath.applyFarPlane(200.0F, scale, 1.0F), 1e-4F);
		assertEquals(20.0F, RiteWorldMath.applyFarPlane(20.0F, scale, 1.0F), "fog already closer is left alone");
		assertEquals(200.0F, RiteWorldMath.applyFarPlane(200.0F, scale, 0.0F), 1e-4F);
		assertEquals(0.85F, RiteWorldMath.farPlaneScale(1.0F, "faint", STABLE), 1e-6F);
		assertEquals(0.25F, RiteWorldMath.fogMix("none"), 1e-6F);
	}

	@Test
	void creepReachesFurtherForBiggerRitesAndRecoilsUnderRupture() {
		assertEquals(4.0F, RiteWorldMath.creepReach(3.0F));
		assertEquals(6.0F, RiteWorldMath.creepReach(10.0F), 1e-6F);
		assertEquals(12.0F, RiteWorldMath.creepReach(40.0F));
		assertEquals(0.0F, RiteWorldMath.creepRadius(5.5F, 10.0F, 0.25F, STABLE));
		assertEquals(5.5F + 6.0F * 0.25F, RiteWorldMath.creepRadius(5.5F, 10.0F, RiteWorldMath.CREEP_FROM, STABLE),
				1e-5F, "the creep arrives a quarter grown");
		assertEquals(11.5F, RiteWorldMath.creepRadius(5.5F, 10.0F, 1.0F, STABLE), 1e-5F);
		assertEquals(5.5F + 6.0F * 0.7F, RiteWorldMath.creepRadius(5.5F, 10.0F, 1.0F, RUPTURING), 1e-5F);
	}

	@Test
	void woundAltitudeStaysUnderTheBuildLimit() {
		assertEquals(100.0D, RiteWorldMath.woundAltitude(64, 10.0F, 320), 1e-9);
		assertEquals(312.0D, RiteWorldMath.woundAltitude(290, 10.0F, 320), 1e-9);
		assertEquals(317.0D, RiteWorldMath.woundAltitude(305, 10.0F, 320), 1e-9,
				"never closer than twelve blocks over the Focus");
		assertEquals(-24.0D, RiteWorldMath.woundAltitude(-60, 10.0F, 320), 1e-9);
	}

	@Test
	void woundGrowsFromItsFormation() {
		assertEquals(8.0F, RiteWorldMath.woundRadius(10.0F, 0.5F), 1e-6F);
		assertEquals(16.0F, RiteWorldMath.woundRadius(10.0F, 1.0F), 1e-6F);
		assertEquals(1.6F, RiteWorldMath.woundRadius(0.0F, 0.5F), 1e-6F, "a tiny footprint still gets a wound");
	}

	@Test
	void rainFollowsSizeStrainAndTheParticleSetting() {
		assertEquals(7, RiteWorldMath.rainPerTick(1.0F, 10.0F, 1.0F, STABLE));
		assertEquals(9, RiteWorldMath.rainPerTick(1.0F, 10.0F, 1.0F, STRAINED));
		assertEquals(30, RiteWorldMath.rainPerTick(1.0F, 100.0F, 1.0F, STABLE));
		assertEquals(4, RiteWorldMath.rainPerTick(1.0F, 10.0F, 0.5F, STABLE));
		assertEquals(0, RiteWorldMath.rainPerTick(1.0F, 10.0F, 0.0F, STABLE));
		assertEquals(0, RiteWorldMath.rainPerTick(0.0F, 10.0F, 1.0F, STABLE));
	}

	@Test
	void braziersGrowLeanAndGutter() {
		assertEquals(1.5F, RiteWorldMath.flameHeight(1.0F, STABLE, 0.0F, 1.0F), 1e-6F);
		assertEquals(1.125F, RiteWorldMath.flameHeight(1.0F, STRAINED, 0.0F, 1.0F), 1e-6F);
		assertEquals(0.225F, RiteWorldMath.flameHeight(1.0F, RUPTURING, 0.0F, 1.0F), 1e-6F, "embers between beats");
		assertEquals(1.5F, RiteWorldMath.flameHeight(1.0F, RUPTURING, 1.0F, 1.0F), 1e-6F, "flaring on the beat");
		assertEquals(20.0F, RiteWorldMath.flameLeanDegrees(1.0F), 1e-6F);
		assertFalse(RiteWorldMath.flameSanguine(0.25F));
		assertTrue(RiteWorldMath.flameSanguine(0.5F));
	}

	@Test
	void soundLayersComeWithTheirLayersAndFallSilentWhenStill() {
		assertEquals(0.0F, RiteWorldMath.droneVolume(0.0F, 1.0F, false));
		assertEquals(0.45F, RiteWorldMath.droneVolume(0.25F, 1.0F, false), 1e-6F);
		assertEquals(0.0F, RiteWorldMath.droneVolume(1.0F, 1.0F, true), "still intervals are silent");
		assertEquals(0.0F, RiteWorldMath.heartbeatVolume(0.25F, 1.0F, false));
		assertEquals(0.9F, RiteWorldMath.heartbeatVolume(0.5F, 1.0F, false), 1e-6F);
		assertEquals(0.6F, RiteWorldMath.stormVolume(0.75F, 1.0F, false), 1e-6F);
		assertEquals(0.75F, RiteWorldMath.strainPitch(RUPTURING), 1e-6F);
	}

	@Test
	void endingsLetGoAtTheirOwnPace() {
		assertEquals(1.0F, RiteWorldMath.groundEnding(Ending.NONE, 50.0F));
		assertEquals(0.5F, RiteWorldMath.groundEnding(Ending.COMPLETED, 30.0F), 1e-6F);
		assertEquals(0.0F, RiteWorldMath.groundEnding(Ending.COLLAPSED, 20.0F));
		assertEquals(0.0F, RiteWorldMath.rainEnding(Ending.COMPLETED, 0.0F), "completion stops the rain at once");
		assertEquals(0.8F, RiteWorldMath.collapseSurge(Ending.COLLAPSED, 10.0F), 1e-6F);
		assertEquals(1.0F, RiteWorldMath.collapseSurge(Ending.COLLAPSED, 80.0F), 1e-6F);
		assertEquals(0.0F, RiteWorldMath.soundEnding(Ending.COLLAPSED, 0.0F), "a collapse cuts the bed");
		assertEquals(0.5F, RiteWorldMath.soundEnding(Ending.COMPLETED, 20.0F), 1e-6F);
		assertEquals(60, RiteWorldMath.endingTicks(Ending.COMPLETED));
		assertEquals(1.0F, RiteWorldMath.woundFlash(Ending.COMPLETED, 0.0F, 0.0F), 1e-6F);
		assertEquals(4, RiteWorldMath.COLLAPSE_STRIKE_TICKS.length);
	}
}
```

`RiteWorldStateTest.java`:

```java
package com.vincenthuto.hemomancy.client.render.world.rite.escalation;

import com.vincenthuto.hemomancy.client.data.ActiveRiteClientData;
import com.vincenthuto.hemomancy.client.data.CardinalRiteEventClientState;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteEventKind;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteInstability;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorVisualRules;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

final class RiteWorldStateTest {
	private static final BlockPos FOCUS = new BlockPos(10, 64, -3);
	private static final RiteFloor WORKING = new RiteFloor(
			CardinalRiteFloorVisualRules.palette(CardinalRiteFloorVisualRules.STYLE_WORKING), true, 4);

	@BeforeEach
	void reset() {
		RiteWorldState.clear();
	}

	static ActiveRiteClientData.RiteEntry entry(BlockPos center, String phase, int stage, int total,
			boolean unstained, int instability) {
		return new ActiveRiteClientData.RiteEntry(center, 5, 0.0D,
				ResourceLocation.parse("hemomancy:cardinal_rite/world_test"), unstained, phase, instability, 0, 0, 1, 1,
				0, 0, 0, 0, 0, "", 8.0F, List.of(), List.of(), List.of(), List.of(), true, UUID.randomUUID(), 0, -1,
				"storm", true, true, 0).withEscalation(stage, total);
	}

	static ActiveRiteClientData.RiteEntry entry(BlockPos center, String phase, int stage, int total) {
		return entry(center, phase, stage, total, false, 0);
	}

	private static void tick(List<ActiveRiteClientData.RiteEntry> active,
			List<CardinalRiteEventClientState.Ghost> ghosts) {
		RiteWorldState.tick(active, ghosts, center -> WORKING);
	}

	@Test
	void firstSightingSnapsToTheSyncedLevel() {
		tick(List.of(entry(FOCUS, "CULMINATION", 4, 4)), List.of());

		RiteWorldState.World world = RiteWorldState.at(FOCUS);
		assertEquals(1.0F, world.level(1.0F), 1e-6F);
		assertEquals(0.0F, world.surge(), "steps taken before the player arrived do not surge");
		assertTrue(world.culminating());
	}

	@Test
	void laterStepsEaseInWithASurge() {
		tick(List.of(entry(FOCUS, "CONSECRATION", 1, 4)), List.of());
		tick(List.of(entry(FOCUS, "INSCRIPTION", 2, 4)), List.of());

		RiteWorldState.World world = RiteWorldState.at(FOCUS);
		assertTrue(world.level(1.0F) > 0.25F && world.level(1.0F) < 0.5F, "the level eases toward the new step");
		assertTrue(world.surge() > 0.8F, "the step lands with a surge");
		for (int i = 0; i < 200; i++) tick(List.of(entry(FOCUS, "INSCRIPTION", 2, 4)), List.of());
		assertEquals(0.5F, RiteWorldState.at(FOCUS).level(1.0F), 1e-3F);
		assertTrue(RiteWorldState.at(FOCUS).surge() < 0.01F);
	}

	@Test
	void ghostsDecideHowTheWorldLetsGo() {
		ActiveRiteClientData.RiteEntry live = entry(FOCUS, "CULMINATION", 4, 4);
		tick(List.of(live), List.of());
		var ghost = new CardinalRiteEventClientState.Ghost(live, CardinalRiteEventKind.COMPLETED, "ascension", 0L);
		tick(List.of(), List.of(ghost));

		RiteWorldState.World ending = RiteWorldState.at(FOCUS);
		assertEquals(RiteWorldMath.Ending.COMPLETED, ending.ending());
		assertEquals(1.0F, ending.level(1.0F), 1e-6F, "the world holds its level while it lets go");
		for (int i = 0; i <= RiteWorldMath.endingTicks(RiteWorldMath.Ending.COMPLETED); i++) {
			tick(List.of(), List.of(ghost));
		}
		assertNull(RiteWorldState.at(FOCUS));
	}

	@Test
	void aLateEndingBeatStillNamesTheEnding() {
		ActiveRiteClientData.RiteEntry live = entry(FOCUS, "ORDEAL", 2, 5);
		tick(List.of(live), List.of());
		tick(List.of(), List.of());
		assertEquals(RiteWorldMath.Ending.VANISHED, RiteWorldState.at(FOCUS).ending());

		tick(List.of(), List.of(new CardinalRiteEventClientState.Ghost(live, CardinalRiteEventKind.COLLAPSED, "", 0L)));
		assertEquals(RiteWorldMath.Ending.COLLAPSED, RiteWorldState.at(FOCUS).ending());
	}

	@Test
	void ghostsExpireAndClearEmptiesEverything() {
		tick(List.of(entry(FOCUS, "ORDEAL", 2, 5)), List.of());
		for (int i = 0; i <= RiteWorldMath.endingTicks(RiteWorldMath.Ending.VANISHED) + 1; i++) {
			tick(List.of(), List.of());
		}
		assertNull(RiteWorldState.at(FOCUS), "a rite that vanished lets go quickly");

		tick(List.of(entry(FOCUS, "ORDEAL", 2, 5)), List.of());
		RiteWorldState.clear();
		assertTrue(RiteWorldState.worlds().isEmpty());
	}

	@Test
	void missingFloorFallsBackToTheDefaultPalette() {
		RiteWorldState.tick(List.of(entry(FOCUS, "ORDEAL", 2, 5)), List.of(), center -> null);

		RiteWorldState.World world = RiteWorldState.at(FOCUS);
		assertFalse(world.floor().formed());
		assertEquals(RiteFloor.NONE.palette(), world.floor().palette());
		float[] blood = RiteWorldState.bloodUniform(world, 1.0F);
		assertEquals(0.0F, blood[2], "no floor, no creep");
		assertEquals(0.0F, blood[3]);
	}

	@Test
	void unstainedAndLegacyRitesAreIgnored() {
		tick(List.of(entry(FOCUS, "ORDEAL", 2, 5, true, 0), entry(FOCUS.east(40), "LEGACY", 0, 3)), List.of());

		assertTrue(RiteWorldState.worlds().isEmpty());
	}

	@Test
	void strainComesFromTheSyncedInstability() {
		tick(List.of(entry(FOCUS, "ORDEAL", 2, 5, false, 75)), List.of());

		assertEquals(CardinalRiteInstability.RUPTURING, RiteWorldState.at(FOCUS).strain());
	}

	@Test
	void bloodUniformFillsTheTrenchAndSpreadsTheCreep() {
		tick(List.of(entry(FOCUS, "CULMINATION", 4, 4)), List.of());

		float[] blood = RiteWorldState.bloodUniform(RiteWorldState.at(FOCUS), 1.0F);
		assertEquals(1.0F, blood[0], 1e-6F);
		assertEquals(1.0F, blood[1], 1e-6F);
		assertEquals(WORKING.edge() + RiteWorldMath.creepReach(8.0F), blood[2], 1e-5F);
		assertEquals(1.0F, blood[3], 1e-6F);
	}

	@Test
	void braziersInsideTheFootprintLeanTowardTheFocus() {
		tick(List.of(entry(FOCUS, "CULMINATION", 4, 4)), List.of());

		RiteWorldState.Flame near = RiteWorldState.flameAt(FOCUS.east(5), 0.0D);
		assertTrue(near.leanX() < 0.0F, "leans back toward the Focus");
		assertEquals(0.0F, near.leanZ(), 1e-6F);
		assertTrue(near.sanguine());
		assertSame(RiteWorldState.Flame.CALM, RiteWorldState.flameAt(FOCUS.east(20), 0.0D));
	}
}
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `./gradlew.bat test --tests "com.vincenthuto.hemomancy.client.render.world.rite.escalation.*"`
Expected: compilation FAILS with `cannot find symbol: class RiteWorldMath`.

- [ ] **Step 3: Create `RiteWorldMath`**

```java
package com.vincenthuto.hemomancy.client.render.world.rite.escalation;

import com.vincenthuto.hemomancy.common.rite.CardinalRiteInstability;

/**
 * Every number the rite's world layers use: when each layer arrives, how far it reaches, how strain bends it and
 * how it lets go when the rite ends. Pure, so it can be tested and tuned in one place.
 */
public final class RiteWorldMath {
	/** Fractions of the world level at which layers arrive. Air, trench, braziers and the drone come with step 1. */
	public static final float CREEP_FROM = 1.0F / 3.0F;
	public static final float WOUND_FROM = 0.5F;
	public static final float RAIN_FROM = 0.75F;
	/**
	 * A layer fades in over this much level just below its threshold, so the eased level brings it in smoothly. It
	 * is smaller than one step of the longest rite (1/11), so a layer never shows on the step before its own.
	 */
	public static final float ARRIVAL_SOFTNESS = 0.04F;
	public static final float FULL_WEIGHT_MARGIN = 4.0F;
	public static final float ZERO_WEIGHT_MARGIN = 32.0F;
	public static final float MIN_FAR_PLANE = 24.0F;
	public static final int MAX_RAIN_PER_TICK = 30;
	public static final double WOUND_DRAW_DISTANCE = 256.0D;
	public static final int BUILD_LIMIT_MARGIN = 8;
	/** The wound never hangs lower than this over its Focus, even when the build limit is close. */
	public static final int WOUND_MIN_CLEARANCE = 12;
	/** Ticks after a collapse begins at which its wound throws lightning. */
	public static final int[] COLLAPSE_STRIKE_TICKS = {0, 7, 15, 24};

	public enum Ending {
		NONE, COMPLETED, COLLAPSED, CANCELLED, VANISHED
	}

	private RiteWorldMath() {
	}

	public static float clamp01(float value) {
		return Math.max(0.0F, Math.min(1.0F, value));
	}

	private static float smoothstep(float edge0, float edge1, float value) {
		float t = clamp01((value - edge0) / (edge1 - edge0));
		return t * t * (3.0F - 2.0F * t);
	}

	private static boolean ruptured(CardinalRiteInstability strain) {
		return strain == CardinalRiteInstability.RUPTURING || strain == CardinalRiteInstability.COLLAPSED;
	}

	public static float level(int stage, int total) {
		return total <= 0 ? 0.0F : clamp01(stage / (float) total);
	}

	/** 0 below a layer's threshold, 1 from it on. */
	public static float arrival(float level, float from) {
		return smoothstep(from - ARRIVAL_SOFTNESS, from, level);
	}

	/** How far past its threshold the level has gone, 0 to 1. */
	public static float ramp(float level, float from) {
		return clamp01((level - from) / (1.0F - from));
	}

	/** A layer that arrives already showing {@code start} of itself and grows to the whole by level 1. */
	public static float growth(float level, float from, float start) {
		return arrival(level, from) * (start + (1.0F - start) * ramp(level, from));
	}

	public static float viewerWeight(double distanceXZ, float footprint) {
		double full = footprint + FULL_WEIGHT_MARGIN;
		double zero = footprint + ZERO_WEIGHT_MARGIN;
		if (distanceXZ <= full) return 1.0F;
		if (distanceXZ >= zero) return 0.0F;
		return (float) ((zero - distanceXZ) / (zero - full));
	}

	public static float fogMix(String profile) {
		return switch (profile == null ? "" : profile) {
			case "storm" -> 0.6F;
			case "dense" -> 0.45F;
			default -> 0.25F;
		};
	}

	public static float farPlaneAtFull(String profile) {
		return switch (profile == null ? "" : profile) {
			case "storm" -> 0.65F;
			case "dense" -> 0.75F;
			default -> 0.85F;
		};
	}

	public static float strainFarScale(CardinalRiteInstability strain) {
		return ruptured(strain) ? 0.8F : strain == CardinalRiteInstability.STRAINED ? 0.9F : 1.0F;
	}

	/** The far-plane multiplier a rite asks for at full viewer weight. */
	public static float farPlaneScale(float level, String profile, CardinalRiteInstability strain) {
		return (1.0F + (farPlaneAtFull(profile) - 1.0F) * clamp01(level)) * strainFarScale(strain);
	}

	/** Applies a far-plane multiplier at a viewer weight, never pulling the fog inside {@link #MIN_FAR_PLANE}. */
	public static float applyFarPlane(float far, float scale, float weight) {
		if (far <= MIN_FAR_PLANE) return far;
		return Math.max(MIN_FAR_PLANE, far * (1.0F + (scale - 1.0F) * clamp01(weight)));
	}

	public static float trenchFill(float level) {
		return clamp01(level);
	}

	public static float overflow(float level) {
		return growth(level, RAIN_FROM, 0.3F);
	}

	public static float creepReach(float footprint) {
		return Math.max(4.0F, Math.min(12.0F, 0.6F * footprint));
	}

	/** The creep's radius from the Focus centre: from the floor's edge outward, or 0 before it arrives. */
	public static float creepRadius(float floorEdge, float footprint, float level, CardinalRiteInstability strain) {
		float grown = growth(level, CREEP_FROM, 0.25F);
		if (grown <= 0.0F) return 0.0F;
		return floorEdge + creepReach(footprint) * grown * (ruptured(strain) ? 0.7F : 1.0F);
	}

	public static float woundPresence(float level) {
		return arrival(level, WOUND_FROM);
	}

	public static float woundRadius(float footprint, float level) {
		return Math.max(2.0F, footprint) * (0.8F + 0.8F * ramp(level, WOUND_FROM));
	}

	public static double woundAltitude(int focusY, float footprint, int maxBuildHeight) {
		double wanted = focusY + 30.0D + 0.6D * footprint;
		double capped = Math.min(wanted, maxBuildHeight - BUILD_LIMIT_MARGIN);
		return Math.max(focusY + WOUND_MIN_CLEARANCE, capped);
	}

	/** Ticks between a storm rite's ambient strikes from its wound: rarer early, more frequent as it deepens. */
	public static int woundStrikeInterval(float level) {
		return Math.round(80.0F - 50.0F * clamp01(level));
	}

	public static float rainLevel(float level) {
		return growth(level, RAIN_FROM, 0.3F);
	}

	public static int rainPerTick(float rainLevel, float footprint, float particleScale,
			CardinalRiteInstability strain) {
		float thick = strain == CardinalRiteInstability.STABLE ? 1.0F : 1.25F;
		int count = Math.round(clamp01(rainLevel) * (2.0F + 0.5F * footprint) * thick);
		return Math.round(Math.min(MAX_RAIN_PER_TICK, count) * Math.max(0.0F, particleScale));
	}

	/** A brazier fire's height multiplier: fed taller as the rite deepens, guttered by strain. */
	public static float flameHeight(float level, CardinalRiteInstability strain, float beat, float flicker) {
		float height = 1.0F + 0.5F * clamp01(level);
		if (strain == CardinalRiteInstability.STRAINED) return height * 0.75F * flicker;
		if (ruptured(strain)) return height * (0.15F + 0.85F * clamp01(beat));
		return height;
	}

	public static float flameLeanDegrees(float level) {
		return 20.0F * clamp01(level);
	}

	/** Braziers burn in the sanguine palette once the rite is halfway through. */
	public static boolean flameSanguine(float level) {
		return level >= WOUND_FROM;
	}

	public static float droneVolume(float level, float weight, boolean still) {
		if (still || level <= 0.0F) return 0.0F;
		return weight * (0.35F + 0.4F * clamp01(level));
	}

	public static float heartbeatVolume(float level, float weight, boolean still) {
		return still ? 0.0F : weight * arrival(level, CREEP_FROM) * 0.9F;
	}

	public static float stormVolume(float level, float weight, boolean still) {
		return still ? 0.0F : weight * arrival(level, RAIN_FROM) * 0.6F;
	}

	public static float strainPitch(CardinalRiteInstability strain) {
		return ruptured(strain) ? 0.75F : strain == CardinalRiteInstability.STRAINED ? 0.88F : 1.0F;
	}

	public static int endingTicks(Ending ending) {
		return switch (ending) {
			case NONE -> 0;
			case COMPLETED -> 60;
			case COLLAPSED, CANCELLED -> 80;
			case VANISHED -> 16;
		};
	}

	/** How much of the creep, trench and braziers remains as a rite ends. */
	public static float groundEnding(Ending ending, float age) {
		return switch (ending) {
			case NONE -> 1.0F;
			case COMPLETED -> 1.0F - clamp01(age / 60.0F);
			case COLLAPSED -> 1.0F - clamp01(age / 20.0F);
			case CANCELLED -> 1.0F - clamp01(age / 80.0F);
			case VANISHED -> 1.0F - clamp01(age / 16.0F);
		};
	}

	/** How much of the air's tint remains as a rite ends. */
	public static float airEnding(Ending ending, float age) {
		return switch (ending) {
			case NONE -> 1.0F;
			case COMPLETED -> 1.0F - clamp01(age / 60.0F);
			case COLLAPSED, CANCELLED -> 1.0F - clamp01(age / 80.0F);
			case VANISHED -> 1.0F - clamp01(age / 16.0F);
		};
	}

	/** A collapse pulls the fog 20% closer over its first 10 ticks, then lets it thin back over 70. */
	public static float collapseSurge(Ending ending, float age) {
		if (ending != Ending.COLLAPSED || age < 0.0F) return 1.0F;
		if (age < 10.0F) return 1.0F - 0.2F * (age / 10.0F);
		return 0.8F + 0.2F * clamp01((age - 10.0F) / 70.0F);
	}

	public static float rainEnding(Ending ending, float age) {
		return switch (ending) {
			case NONE -> 1.0F;
			case CANCELLED -> 1.0F - clamp01(age / 80.0F);
			default -> 0.0F;
		};
	}

	public static float soundEnding(Ending ending, float age) {
		return switch (ending) {
			case NONE -> 1.0F;
			case COMPLETED -> 1.0F - clamp01(age / 40.0F);
			case COLLAPSED -> 0.0F;
			case CANCELLED -> 1.0F - clamp01(age / 80.0F);
			case VANISHED -> 1.0F - clamp01(age / 16.0F);
		};
	}

	/** The wound's size as a rite ends: a completion closes it behind a flash, a collapse shudders it shut. */
	public static float woundEnding(Ending ending, float age) {
		return switch (ending) {
			case NONE -> 1.0F;
			case COMPLETED -> 1.0F - smoothstep(0.0F, 14.0F, age);
			case COLLAPSED -> (1.0F - smoothstep(4.0F, 30.0F, age)) * (1.0F + 0.12F * (float) Math.sin(age * 2.1F));
			case CANCELLED -> 1.0F - clamp01(age / 80.0F);
			case VANISHED -> 1.0F - clamp01(age / 16.0F);
		};
	}

	/** A flash in the wound: a step's surge, or a completion closing it. */
	public static float woundFlash(Ending ending, float age, float surge) {
		float closing = ending == Ending.COMPLETED ? Math.max(0.0F, 1.0F - age / 8.0F) : 0.0F;
		return Math.max(clamp01(surge), closing);
	}
}
```

- [ ] **Step 4: Create `RiteFloor`**

```java
package com.vincenthuto.hemomancy.client.render.world.rite.escalation;

import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorGeometry;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorRegistry;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorVisualRules;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.CardinalFocusBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/** The consecrated floor under a rite's Focus, as the world layers need it: its colours and its size. */
public record RiteFloor(CardinalRiteFloorVisualRules.Palette palette, boolean formed, int half) {
	/** No formed floor: the default Harbinger palette, and no trench or creep to draw. */
	public static final RiteFloor NONE = new RiteFloor(
			CardinalRiteFloorVisualRules.palette(CardinalRiteFloorVisualRules.STYLE_THRESHOLD), false, 0);

	public static RiteFloor at(Level level, BlockPos focus) {
		if (level == null || !(level.getBlockEntity(focus) instanceof CardinalFocusBlockEntity entity)
				|| entity.getFormedFloor() == null) {
			return NONE;
		}
		return CardinalRiteFloorRegistry.get(entity.getFormedFloor())
				.map(floor -> new RiteFloor(CardinalRiteFloorVisualRules.palette(
						CardinalRiteFloorVisualRules.styleIndex(floor.style())), true,
						CardinalRiteFloorGeometry.halfWidth(floor)))
				.orElse(NONE);
	}

	/** The floor's edge, in blocks from the Focus centre, where the creep begins. */
	public float edge() {
		return half + 0.5F;
	}
}
```

- [ ] **Step 5: Create `RiteWorldState`**

```java
package com.vincenthuto.hemomancy.client.render.world.rite.escalation;

import com.vincenthuto.hemomancy.client.data.ActiveRiteClientData;
import com.vincenthuto.hemomancy.client.data.CardinalRiteEventClientState;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteCeremonyRules;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteInstability;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteMath;
import com.vincenthuto.hemomancy.common.rite.CardinalRitePhase;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * Each Harbinger rite's hold on the world around it, eased from its synced stage. A rite seen for the first time
 * snaps to its stage, so arriving mid-rite shows the world as it already is; later steps ease in behind a surge.
 * When a rite stops being synced, its ghost decides how the world lets go.
 */
public final class RiteWorldState {
	static final float EASE = 0.08F;
	static final float SURGE_DECAY = 0.88F;

	/** One rite's world, as the layers read it. */
	public record World(BlockPos center, UUID owner, float level, float previousLevel, float surge, int stage,
			int total, CardinalRiteInstability strain, float footprint, String fogProfile, boolean lightning,
			CardinalRitePhase phase, RiteFloor floor, RiteWorldMath.Ending ending, float endingAge) {
		/** The eased level between ticks. */
		public float level(float partialTick) {
			return previousLevel + (level - previousLevel) * RiteWorldMath.clamp01(partialTick);
		}

		public boolean ended() {
			return ending != RiteWorldMath.Ending.NONE;
		}

		public boolean still() {
			return phase == CardinalRitePhase.STILL_INTERVAL;
		}

		public boolean culminating() {
			return phase == CardinalRitePhase.CULMINATION;
		}
	}

	/** How a rite's world bends a brazier's fire. */
	public record Flame(float height, float leanX, float leanZ, boolean sanguine) {
		public static final Flame CALM = new Flame(1.0F, 0.0F, 0.0F, false);
	}

	private static final class Tracked {
		private ActiveRiteClientData.RiteEntry entry;
		private float target;
		private float level;
		private float previous;
		private float surge;
		private int stage;
		private RiteWorldMath.Ending ending = RiteWorldMath.Ending.NONE;
		private float endingAge;
		private RiteFloor floor = RiteFloor.NONE;
		private boolean seen;
	}

	private static final Map<BlockPos, Tracked> TRACKED = new HashMap<>();
	private static List<World> worlds = List.of();

	private RiteWorldState() {
	}

	/** Advances one client tick from the synced rites, the ghosts of ended ones, and the floors under them. */
	public static void tick(List<ActiveRiteClientData.RiteEntry> active,
			List<CardinalRiteEventClientState.Ghost> ghosts, Function<BlockPos, RiteFloor> floors) {
		for (Tracked tracked : TRACKED.values()) tracked.seen = false;
		for (ActiveRiteClientData.RiteEntry entry : active) {
			if (entry.isUnstained() || entry.isLegacy() || entry.getEscalationTotal() <= 0) continue;
			float target = RiteWorldMath.level(entry.getEscalation(), entry.getEscalationTotal());
			Tracked tracked = TRACKED.get(entry.getCenter());
			if (tracked == null || tracked.ending != RiteWorldMath.Ending.NONE) {
				// First sight, or a new rite on a Focus whose last rite is still letting go: the world is already as
				// far as the rite has pushed it.
				tracked = new Tracked();
				tracked.level = target;
				tracked.previous = target;
				tracked.stage = entry.getEscalation();
				TRACKED.put(entry.getCenter(), tracked);
			} else if (entry.getEscalation() > tracked.stage) {
				tracked.surge = 1.0F;
				tracked.stage = entry.getEscalation();
			}
			tracked.entry = entry;
			tracked.target = target;
			tracked.seen = true;
		}
		List<World> next = new ArrayList<>(TRACKED.size());
		Iterator<Map.Entry<BlockPos, Tracked>> iterator = TRACKED.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<BlockPos, Tracked> mapped = iterator.next();
			Tracked tracked = mapped.getValue();
			if (!tracked.seen) {
				RiteWorldMath.Ending ending = endingOf(ghostAt(ghosts, mapped.getKey()));
				if (tracked.ending == RiteWorldMath.Ending.NONE) {
					tracked.ending = ending;
					tracked.endingAge = 0.0F;
				} else {
					// The beat that names an ending can arrive just after the snapshot that dropped the rite.
					if (tracked.ending == RiteWorldMath.Ending.VANISHED) tracked.ending = ending;
					tracked.endingAge++;
				}
				if (tracked.endingAge > RiteWorldMath.endingTicks(tracked.ending)) {
					iterator.remove();
					continue;
				}
			}
			tracked.previous = tracked.level;
			if (tracked.ending == RiteWorldMath.Ending.NONE) tracked.level += (tracked.target - tracked.level) * EASE;
			tracked.surge *= SURGE_DECAY;
			RiteFloor floor = floors.apply(mapped.getKey());
			tracked.floor = floor == null ? RiteFloor.NONE : floor;
			next.add(world(mapped.getKey(), tracked));
		}
		worlds = List.copyOf(next);
	}

	public static List<World> worlds() {
		return worlds;
	}

	public static World at(BlockPos center) {
		for (World world : worlds) {
			if (world.center().equals(center)) return world;
		}
		return null;
	}

	public static void clear() {
		TRACKED.clear();
		worlds = List.of();
	}

	/**
	 * The floor shader's {@code RiteBlood}: the trench's fill, its overflow, the creep's radius from the Focus
	 * centre and the creep's strength. A collapse tears the creep back to the floor; a completion sinks it.
	 */
	public static float[] bloodUniform(World world, float partialTick) {
		float[] blood = new float[4];
		if (world == null) return blood;
		float level = world.level(partialTick);
		float ground = RiteWorldMath.groundEnding(world.ending(), world.endingAge());
		blood[0] = RiteWorldMath.trenchFill(level) * ground;
		blood[1] = RiteWorldMath.overflow(level) * ground;
		if (!world.floor().formed()) return blood;
		float radius = RiteWorldMath.creepRadius(world.floor().edge(), world.footprint(), level, world.strain());
		if (radius <= 0.0F) return blood;
		float edge = world.floor().edge();
		float tearing = world.ending() == RiteWorldMath.Ending.COLLAPSED ? ground : 1.0F;
		blood[2] = edge + (radius - edge) * tearing;
		blood[3] = world.ending() == RiteWorldMath.Ending.COLLAPSED ? 1.0F : ground;
		return blood;
	}

	/** Braziers within a rite's footprint lean toward its Focus, burn taller and redder, and gutter under strain. */
	public static Flame flameAt(BlockPos pos, double time) {
		for (World world : worlds) {
			double dx = world.center().getX() - pos.getX();
			double dz = world.center().getZ() - pos.getZ();
			double distance = Math.sqrt(dx * dx + dz * dz);
			if (distance > world.footprint() + 2.0D || Math.abs(world.center().getY() - pos.getY()) > 4) continue;
			float level = world.level(1.0F) * RiteWorldMath.groundEnding(world.ending(), world.endingAge());
			if (level <= 0.0F) return Flame.CALM;
			float beat = CardinalRiteMath.heartbeat((float) time);
			float flicker = world.strain() == CardinalRiteInstability.STRAINED
					? 0.85F + 0.15F * (float) Math.sin(time * 0.9D + (pos.asLong() & 1023L)) : 1.0F;
			float height = RiteWorldMath.flameHeight(level, world.strain(), beat, flicker);
			double lean = Math.tan(Math.toRadians(RiteWorldMath.flameLeanDegrees(level)));
			float leanX = distance < 0.5D ? 0.0F : (float) (dx / distance * lean);
			float leanZ = distance < 0.5D ? 0.0F : (float) (dz / distance * lean);
			return new Flame(height, leanX, leanZ, RiteWorldMath.flameSanguine(level));
		}
		return Flame.CALM;
	}

	private static World world(BlockPos center, Tracked tracked) {
		ActiveRiteClientData.RiteEntry entry = tracked.entry;
		return new World(center, entry.getOwner(), tracked.level, tracked.previous, tracked.surge, tracked.stage,
				entry.getEscalationTotal(), CardinalRiteCeremonyRules.instabilityBand(entry.getInstability()),
				entry.getFootprintRadius(), entry.getFogProfile(), entry.hasFogLightning(), entry.ritePhase(),
				tracked.floor, tracked.ending, tracked.endingAge);
	}

	private static CardinalRiteEventClientState.Ghost ghostAt(List<CardinalRiteEventClientState.Ghost> ghosts,
			BlockPos center) {
		for (CardinalRiteEventClientState.Ghost ghost : ghosts) {
			if (ghost.center().equals(center)) return ghost;
		}
		return null;
	}

	private static RiteWorldMath.Ending endingOf(CardinalRiteEventClientState.Ghost ghost) {
		if (ghost == null) return RiteWorldMath.Ending.VANISHED;
		return switch (ghost.kind()) {
			case COMPLETED -> RiteWorldMath.Ending.COMPLETED;
			case COLLAPSED, BROKEN -> RiteWorldMath.Ending.COLLAPSED;
			case CANCELLED -> RiteWorldMath.Ending.CANCELLED;
			default -> RiteWorldMath.Ending.VANISHED;
		};
	}
}
```

- [ ] **Step 6: Tick and clear it on the client**

In `ClientEvents.onClientTickPost`, after the `RiteGuidance.tick(...)` line from Task 4, add:

```java
        if (Minecraft.getInstance().level != null && !Minecraft.getInstance().isPaused()) {
            com.vincenthuto.hemomancy.client.render.world.rite.escalation.RiteWorldState.tick(
                    ActiveRiteClientData.getActiveRites(),
                    com.vincenthuto.hemomancy.client.data.CardinalRiteEventClientState.ghosts(),
                    center -> com.vincenthuto.hemomancy.client.render.world.rite.escalation.RiteFloor.at(
                            Minecraft.getInstance().level, center));
        }
```

In `onClientPlayerLogout` and `onClientLevelUnload`, after the `RiteGuidance.clear();` line add:

```java
		com.vincenthuto.hemomancy.client.render.world.rite.escalation.RiteWorldState.clear();
```

- [ ] **Step 7: Run the tests to verify they pass**

Run: `./gradlew.bat test --tests "com.vincenthuto.hemomancy.client.render.world.rite.escalation.*"`
Expected: PASS.

- [ ] **Step 8: Checkpoint (no commit)**

Run: `./gradlew.bat compileJava compileTestJava compileGameTestJava`
Expected: BUILD SUCCESSFUL.

### Task 6: The air: fog colour and distance

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/client/render/world/rite/escalation/RiteAirLayer.java`
- Test: `src/test/java/com/vincenthuto/hemomancy/client/render/world/rite/escalation/RiteAirLayerTest.java`

**Interfaces:**
- Consumes: `RiteWorldState.worlds()`, `RiteWorldState.World`, `RiteWorldMath.viewerWeight`, `fogMix`, `airEnding`, `farPlaneScale`, `collapseSurge`, `applyFarPlane` (Task 5).
- Produces: `RiteAirLayer.Choice(RiteWorldState.World world, float strength, float weight)` and `static Choice strongest(List<World>, Vec3 camera, float partialTick)` (package-private), plus the two event handlers.

- [ ] **Step 1: Write the failing test**

```java
package com.vincenthuto.hemomancy.client.render.world.rite.escalation;

import com.vincenthuto.hemomancy.common.rite.CardinalRiteInstability;
import com.vincenthuto.hemomancy.common.rite.CardinalRitePhase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

final class RiteAirLayerTest {
	private static RiteWorldState.World world(BlockPos center, float level, String fog) {
		return new RiteWorldState.World(center, null, level, level, 0.0F, 2, 4, CardinalRiteInstability.STABLE, 8.0F,
				fog, true, CardinalRitePhase.ORDEAL, RiteFloor.NONE, RiteWorldMath.Ending.NONE, 0.0F);
	}

	@Test
	void strongestRiteWinsInsteadOfSumming() {
		BlockPos storm = new BlockPos(0, 64, 0);
		BlockPos faint = new BlockPos(20, 64, 0);
		Vec3 between = new Vec3(10.5D, 65.0D, 0.5D);

		RiteAirLayer.Choice choice = RiteAirLayer.strongest(
				List.of(world(faint, 1.0F, "faint"), world(storm, 1.0F, "storm")), between, 1.0F);

		assertEquals(storm, choice.world().center());
		assertEquals(0.6F, choice.strength(), 1e-6F, "the stronger rite's own strength, not the sum");
	}

	@Test
	void noAirBeforeTheFirstStepOrFarAway() {
		BlockPos center = new BlockPos(0, 64, 0);

		assertNull(RiteAirLayer.strongest(List.of(world(center, 0.0F, "storm")), new Vec3(0.5D, 65.0D, 0.5D), 1.0F));
		assertNull(RiteAirLayer.strongest(List.of(world(center, 1.0F, "storm")), new Vec3(200.0D, 65.0D, 0.0D), 1.0F));
	}
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew.bat test --tests "com.vincenthuto.hemomancy.client.render.world.rite.escalation.RiteAirLayerTest"`
Expected: compilation FAILS with `cannot find symbol: class RiteAirLayer`.

- [ ] **Step 3: Create `RiteAirLayer`**

```java
package com.vincenthuto.hemomancy.client.render.world.rite.escalation;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

import java.util.List;

/**
 * The air around a deepening rite: the fog leans toward the floor's blood colour and closes in a little with each
 * step, and further under strain. Only the strongest rite around the camera counts, so neighbouring rites never
 * stack, and the fog is never pulled inside {@link RiteWorldMath#MIN_FAR_PLANE}.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class RiteAirLayer {
	private RiteAirLayer() {
	}

	record Choice(RiteWorldState.World world, float strength, float weight) {
	}

	static Choice strongest(List<RiteWorldState.World> worlds, Vec3 camera, float partialTick) {
		Choice best = null;
		for (RiteWorldState.World world : worlds) {
			double dx = world.center().getX() + 0.5D - camera.x;
			double dz = world.center().getZ() + 0.5D - camera.z;
			float weight = RiteWorldMath.viewerWeight(Math.sqrt(dx * dx + dz * dz), world.footprint());
			float strength = RiteWorldMath.fogMix(world.fogProfile()) * world.level(partialTick) * weight
					* RiteWorldMath.airEnding(world.ending(), world.endingAge());
			if (strength <= 0.0F) continue;
			if (best == null || strength > best.strength()) best = new Choice(world, strength, weight);
		}
		return best;
	}

	@SubscribeEvent
	public static void colour(ViewportEvent.ComputeFogColor event) {
		Choice choice = strongest(RiteWorldState.worlds(), event.getCamera().getPosition(),
				(float) event.getPartialTick());
		if (choice == null) return;
		int vein = choice.world().floor().palette().vein();
		float red = ((vein >> 16) & 255) / 255.0F * 0.35F;
		float green = ((vein >> 8) & 255) / 255.0F * 0.35F;
		float blue = (vein & 255) / 255.0F * 0.35F;
		float mix = Math.min(1.0F, choice.strength());
		event.setRed(event.getRed() + (red - event.getRed()) * mix);
		event.setGreen(event.getGreen() + (green - event.getGreen()) * mix);
		event.setBlue(event.getBlue() + (blue - event.getBlue()) * mix);
	}

	@SubscribeEvent
	public static void distance(ViewportEvent.RenderFog event) {
		if (event.getMode() != FogRenderer.FogMode.FOG_TERRAIN) return;
		float partialTick = (float) event.getPartialTick();
		Choice choice = strongest(RiteWorldState.worlds(), event.getCamera().getPosition(), partialTick);
		if (choice == null) return;
		RiteWorldState.World world = choice.world();
		float level = world.level(partialTick) * RiteWorldMath.airEnding(world.ending(), world.endingAge());
		float scale = RiteWorldMath.farPlaneScale(level, world.fogProfile(), world.strain())
				* RiteWorldMath.collapseSurge(world.ending(), world.endingAge());
		float far = event.getFarPlaneDistance();
		float pulled = RiteWorldMath.applyFarPlane(far, scale, choice.weight());
		if (pulled >= far) return;
		event.setNearPlaneDistance(event.getNearPlaneDistance() * pulled / far);
		event.setFarPlaneDistance(pulled);
		event.setCanceled(true);
	}
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew.bat test --tests "com.vincenthuto.hemomancy.client.render.world.rite.escalation.RiteAirLayerTest"`
Expected: PASS.

- [ ] **Step 5: Checkpoint (no commit)**

Run: `./gradlew.bat compileJava compileTestJava`
Expected: BUILD SUCCESSFUL.

### Task 7: Trench fill, overflow and the step's shock

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/init/ShaderInit.java` (the `CARDINAL_FLOOR_MEMBRANE` holder's uniform list)
- Modify: `src/main/resources/assets/hemomancy/shaders/core/world/cardinal_floor_membrane.json` (one uniform)
- Modify: `src/main/resources/assets/hemomancy/shaders/core/world/cardinal_floor_membrane.fsh` (declare `RiteBlood`; fill and spill in `gash`)
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/render/world/floor/FloorRiteResponse.java` (`blood` array, `creepRadius()`, the step shock)
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/render/world/floor/CardinalFloorRenderTypes.java` (load `RiteBlood`)
- Test: `src/test/java/com/vincenthuto/hemomancy/client/render/world/rite/escalation/RiteWorldSourceTest.java` (add a check)

**Interfaces:**
- Consumes: `RiteWorldState.at(BlockPos)`, `RiteWorldState.bloodUniform(World, float)` (Task 5), `CardinalRiteEventKind.ESCALATION` (Task 2).
- Produces: `FloorRiteResponse.blood` (package-private `float[4]`), `public float creepRadius()`; shader uniform `RiteBlood` (x trench fill, y overflow, z creep radius, w creep strength), which Task 8 reads.

- [ ] **Step 1: Write the failing test**

Add to `RiteWorldSourceTest`:

```java
	@Test
	void theFloorShaderReceivesTheRitesBlood() throws IOException {
		String shaders = read("src/main/java/com/vincenthuto/hemomancy/common/init/ShaderInit.java");
		String json = read("src/main/resources/assets/hemomancy/shaders/core/world/cardinal_floor_membrane.json");
		String fragment = read("src/main/resources/assets/hemomancy/shaders/core/world/cardinal_floor_membrane.fsh");
		String types = read("src/main/java/com/vincenthuto/hemomancy/client/render/world/floor/CardinalFloorRenderTypes.java");
		String response = read("src/main/java/com/vincenthuto/hemomancy/client/render/world/floor/FloorRiteResponse.java");

		assertTrue(shaders.contains("\"WoundAngles\", \"WoundLevels\", \"RiteBlood\""), "uniform cached by the holder");
		assertTrue(json.contains("{ \"name\": \"RiteBlood\", \"type\": \"float\", \"count\": 4"), "uniform declared to Minecraft");
		assertTrue(fragment.contains("uniform vec4 RiteBlood;"), "uniform declared to GLSL");
		assertTrue(types.contains("set(shader, \"RiteBlood\", blood);"), "uniform loaded per Focus");
		assertTrue(response.contains("CardinalRiteEventKind.ESCALATION"), "each step strikes the floor like the staff");
		assertTrue(response.contains("RiteWorldState.bloodUniform("), "the trench and creep follow the world stage");
	}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew.bat test --tests "com.vincenthuto.hemomancy.client.render.world.rite.escalation.RiteWorldSourceTest"`
Expected: FAIL in `theFloorShaderReceivesTheRitesBlood` ("uniform cached by the holder").

- [ ] **Step 3: Declare the uniform**

In `ShaderInit`, change the `CARDINAL_FLOOR_MEMBRANE` holder's last line from

```java
			"RiteRings", "RiteSurge", "RiteDraw", "RiteMood", "RiteSwell", "WoundAngles", "WoundLevels");
```

to

```java
			"RiteRings", "RiteSurge", "RiteDraw", "RiteMood", "RiteSwell", "WoundAngles", "WoundLevels", "RiteBlood");
```

In `cardinal_floor_membrane.json`, replace the last uniform line

```json
    { "name": "WoundLevels", "type": "float", "count": 4, "values": [ 0.0, 0.0, 0.0, 0.0 ] }
```

with

```json
    { "name": "WoundLevels", "type": "float", "count": 4, "values": [ 0.0, 0.0, 0.0, 0.0 ] },
    { "name": "RiteBlood", "type": "float", "count": 4, "values": [ 0.0, 0.0, 0.0, 0.0 ] }
```

In `cardinal_floor_membrane.fsh`, after `uniform vec4 WoundLevels;` add:

```glsl
// The world stage (RiteWorldState.bloodUniform): x how full the brazier trench is, y how far it spills over its lip,
// z the creep's radius from the Focus centre in blocks, w the creep's strength. All zero with no rite.
uniform vec4 RiteBlood;
```

- [ ] **Step 4: Fill and spill the trench in `gash`**

In `gash(...)`, replace

```glsl
    vec2 creep = cellular(p * 3.2 + 17.0);
    float capillaries = line(creep.y, 0.014) * smoothstep(edge + 0.28, edge + 0.06, across) * step(edge, across)
            * closing;
    if (opening + shadow + capillaries + seam <= 0.003) {
        discard;
    }
```

with

```glsl
    vec2 creep = cellular(p * 3.2 + 17.0);
    float capillaries = line(creep.y, 0.014) * smoothstep(edge + 0.28, edge + 0.06, across) * step(edge, across)
            * closing;
    // Late in a rite the brimming trench spills over its lip in runnels that soak into the torn ground.
    float spill = clamp(RiteBlood.y, 0.0, 1.0) * closing;
    float wet = 0.0;
    if (spill > 0.001) {
        float runnel = line(cellular(p * 1.7 + 41.0).y, 0.02 + 0.03 * spill)
                * smoothstep(edge + 0.08 + 0.32 * spill, edge, across) * step(edge, across);
        float soak = smoothstep(edge + 0.02 + 0.12 * spill, edge, across) * step(edge, across) * spill;
        wet = max(runnel * spill, soak);
    }
    if (opening + shadow + capillaries + seam + wet <= 0.003) {
        discard;
    }
```

Then replace the end of the outside-the-cut branch,

```glsl
        float alpha = max(max(max(crust * 0.9, shadow * 0.45), capillaries * 0.8), seam);
        return vec4(lit(color, capillaries + seam + dig), alpha);
    }
```

with

```glsl
        float alpha = max(max(max(crust * 0.9, shadow * 0.45), capillaries * 0.8), seam);
        color = mix(color, VeinColor * (0.5 + 0.2 * Beat) + GlowColor * 0.05, wet);
        alpha = max(alpha, wet * 0.95);
        return vec4(lit(color, capillaries + seam + dig + wet * 0.4), alpha);
    }
```

Then, in the inside-the-cut path, replace

```glsl
    vec3 ray = normalize(view);
    float depth = (0.34 + 0.18 * swell) * mix(0.3, 1.0, taper);
```

with

```glsl
    vec3 ray = normalize(view);
    float depth = (0.34 + 0.18 * swell) * mix(0.3, 1.0, taper);
    // The world stage fills the trench: the pooled blood rises toward the lip, and the view ray meets its surface
    // before it reaches the walls below. The walls narrow from the lip to the floor (0.45 of the lip), so the pool
    // widens as it rises. Strain darkens and clots it.
    float fill = clamp(RiteBlood.x, 0.0, 1.0);
    if (fill > 0.01) {
        float surfaceDepth = depth * (1.0 - 0.92 * fill);
        vec2 toSurface = ray.xz / max(-ray.y, 0.12) * surfaceDepth;
        float acrossPool = sAcross + dot(side, toSurface);
        float poolHalf = edge * (0.45 + 0.55 * (1.0 - surfaceDepth / max(depth, 0.001)));
        if (abs(acrossPool) < poolHalf) {
            vec2 at = p + toSurface;
            float sheen = pow(noise(at * 4.0 + vec2(HemoTime * 0.02, -HemoTime * 0.013)), 3.0);
            float ripple = 0.5 + 0.5 * sin(dot(at, side) * 9.0 - HemoTime * 0.25);
            vec3 pool = VeinColor * (0.42 + 0.18 * fbm(at * 5.0)) + GlowColor * (sheen * 0.3 + ripple * Beat * 0.06);
            pool = mix(pool, VeinColor * 0.25 + BaseColor * 0.2, clamp(RiteMood.y, 0.0, 1.0) * 0.6);
            pool += GlowColor * smoothstep(poolHalf * 0.8, poolHalf, abs(acrossPool)) * fill * 0.15;
            return vec4(lit(pool, 0.4 + 0.3 * fill), 1.0);
        }
    }
```

- [ ] **Step 5: Feed the uniform from the world stage, and shock the floor on each step**

In `FloorRiteResponse`, next to the other frame arrays add:

```java
	final float[] blood = new float[4];
```

Add the accessor next to `groundRingStrength()`:

```java
	/** The world stage's creep radius from the Focus centre this frame, or 0 when it has none. */
	public float creepRadius() {
		return blood[3] > 0.01F ? blood[2] : 0.0F;
	}
```

In `update(...)`, replace

```java
		// The staff strikes: one hard shock out from the Focus.
		float impact = CardinalRiteEventClientState.latestAge(origin, CardinalRiteEventKind.STAFF_IMPACT, partialTick);
```

with

```java
		// The staff strikes, or the rite pushes the world a step further: one hard shock out from the Focus.
		float impact = youngest(
				CardinalRiteEventClientState.latestAge(origin, CardinalRiteEventKind.STAFF_IMPACT, partialTick),
				CardinalRiteEventClientState.latestAge(origin, CardinalRiteEventKind.ESCALATION, partialTick));
```

and at the end of `update(...)`, after `updateWounds(origin, entry, partialTick, dt, cx, cz);`, add:

```java
		// The world stage: blood rising in the trench, spilling over late on, and the creep out over the ground.
		System.arraycopy(com.vincenthuto.hemomancy.client.render.world.rite.escalation.RiteWorldState.bloodUniform(
				com.vincenthuto.hemomancy.client.render.world.rite.escalation.RiteWorldState.at(origin), partialTick),
				0, blood, 0, 4);
```

In `CardinalFloorRenderTypes.withRite`, after `float[] woundStrengths = rite.woundStrengths.clone();` add `float[] blood = rite.blood.clone();`, and after `set(shader, "WoundLevels", woundStrengths);` add:

```java
					set(shader, "RiteBlood", blood);
```

- [ ] **Step 6: Validate the shader**

Run (Git Bash, from the project root):

```bash
mkdir -p build/shader-check && sed 's|#moj_import <fog.glsl>|vec4 linear_fog(vec4 c, float d, float s, float e, vec4 f) { return c; }\nfloat fog_distance(vec3 p, int s) { return length(p); }|' src/main/resources/assets/hemomancy/shaders/core/world/cardinal_floor_membrane.fsh > build/shader-check/cardinal_floor_membrane.frag && glslangValidator -S frag build/shader-check/cardinal_floor_membrane.frag
```

Expected: no output and exit code 0. (The `fog.glsl` import is replaced by stubs because the validator cannot resolve Minecraft's include.)

- [ ] **Step 7: Run the tests to verify they pass**

Run: `./gradlew.bat test --tests "com.vincenthuto.hemomancy.client.render.world.rite.escalation.*" --tests "com.vincenthuto.hemomancy.client.render.world.floor.*"`
Expected: PASS, including the existing floor tests.

- [ ] **Step 8: Checkpoint (no commit)**

Run: `./gradlew.bat compileJava compileTestJava`
Expected: BUILD SUCCESSFUL.

### Task 8: Terrain creep

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/client/render/world/floor/CreepCells.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/render/world/floor/FloorMesh.java` (creep cells and `emitCreep`)
- Modify: `src/main/resources/assets/hemomancy/shaders/core/world/cardinal_floor_membrane.fsh` (`creepGround`; the ground-layer branch of `main`)
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/render/world/CardinalFloorConsecrationRenderer.java` (emit the creep; widen `renderRadius`)
- Test: `src/test/java/com/vincenthuto/hemomancy/client/render/world/floor/CreepCellsTest.java`
- Test: `src/test/java/com/vincenthuto/hemomancy/client/render/world/rite/escalation/RiteWorldSourceTest.java` (add a check)

**Interfaces:**
- Consumes: `FloorRiteResponse.creepRadius()`, uniform `RiteBlood.zw` (Task 7); `RiteWorldState.at`, `RiteWorldMath.creepReach` (Task 5).
- Produces: `CreepCells.groundOffset(IntPredicate carries, int drop)`, `CreepCells.NO_GROUND`, `CreepCells.maxCells(int half, int margin)`, `FloorMesh.emitCreep(VertexConsumer, Matrix4f, Level, BlockPos, float radius, int alpha)`, `FloorMesh.CREEP_MARGIN`.

- [ ] **Step 1: Write the failing tests**

`CreepCellsTest.java`:

```java
package com.vincenthuto.hemomancy.client.render.world.floor;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CreepCellsTest {
	@Test
	void groundAtTheFocusLevelWins() {
		assertEquals(0, CreepCells.groundOffset(dy -> dy == 0 || dy == 1, 3));
	}

	@Test
	void theNearestGroundIsFoundUpBeforeDown() {
		assertEquals(1, CreepCells.groundOffset(Set.of(1, -1)::contains, 3));
		assertEquals(-2, CreepCells.groundOffset(Set.of(-2, 3)::contains, 3));
	}

	@Test
	void cliffsAndCavesCarryNoCreep() {
		assertEquals(CreepCells.NO_GROUND, CreepCells.groundOffset(Set.of(4, -4)::contains, 3));
	}

	@Test
	void creepMeshIsBoundedForGrandFootprints() {
		int grand = CreepCells.maxCells(9, FloorMesh.CREEP_MARGIN);
		assertTrue(grand < 2000, "a grand floor's creep stays under two thousand cells, was " + grand);
		assertTrue(CreepCells.maxCells(2, FloorMesh.CREEP_MARGIN) < grand);
	}
}
```

Add to `RiteWorldSourceTest`:

```java
	@Test
	void theCreepIsDrawnWithTheFloor() throws IOException {
		String fragment = read("src/main/resources/assets/hemomancy/shaders/core/world/cardinal_floor_membrane.fsh");
		String renderer = read("src/main/java/com/vincenthuto/hemomancy/client/render/world/CardinalFloorConsecrationRenderer.java");

		assertTrue(fragment.contains("vec4 ring = vertexColor.r < 0.5 ? creepGround(p) : groundRing(p);"),
				"red 0 marks creep cells on the ground layer");
		assertTrue(renderer.contains("mesh.emitCreep(consumer, matrix, level, origin, rite.creepRadius(), 255);"));
		assertTrue(renderer.contains("RiteWorldMath.creepReach("), "the Focus's render bounds cover the creep");
	}
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `./gradlew.bat test --tests "com.vincenthuto.hemomancy.client.render.world.floor.CreepCellsTest"`
Expected: compilation FAILS with `cannot find symbol: class CreepCells`.

- [ ] **Step 3: Create `CreepCells`**

```java
package com.vincenthuto.hemomancy.client.render.world.floor;

import java.util.function.IntPredicate;

/** Where the world stage's creep can lie: pure helpers for {@link FloorMesh}'s creep cells. */
public final class CreepCells {
	public static final int NO_GROUND = Integer.MIN_VALUE;

	private CreepCells() {
	}

	/**
	 * The nearest level to the Focus's own whose block can carry the creep, searching 0, +1, -1, +2, -2 ... out to
	 * {@code drop}, so the membrane follows gentle terrain but stops at cliffs and caves.
	 */
	public static int groundOffset(IntPredicate carries, int drop) {
		if (carries.test(0)) return 0;
		for (int step = 1; step <= drop; step++) {
			if (carries.test(step)) return step;
			if (carries.test(-step)) return -step;
		}
		return NO_GROUND;
	}

	/** An upper bound on the creep cells around a floor of this half-width: the disc out to its furthest reach. */
	public static int maxCells(int half, int margin) {
		double limit = half + 0.5D + margin + 1.0D;
		return (int) Math.ceil(Math.PI * limit * limit);
	}
}
```

- [ ] **Step 4: Build and emit creep cells in `FloorMesh`**

After `private List<float[]> ringCells;` add:

```java
	private List<float[]> creepCells;
```

Next to `RING_BAND_AHEAD` / `RING_BAND_BEHIND` add:

```java
	/** The creep's furthest reach past the floor's edge (RiteWorldMath.creepReach's ceiling) plus a margin. */
	public static final int CREEP_MARGIN = 13;
	/** How far above or below the Focus's level a creep cell may find its ground. */
	private static final int CREEP_DROP = 3;
	/** Red on a ground-layer vertex: 0 marks the world stage's creep, 255 the grounding ring's passing pulse. */
	private static final int CREEP_RED = 0;
```

After `emitGroundRing(...)` add:

```java
	/**
	 * The world stage's creep: grounded cells beyond the trench out to {@code radius} from the Focus centre, each at
	 * its own ground height so the membrane follows the terrain. The cells are found once per mesh, out to the
	 * furthest the creep can reach; the shader draws its ragged front.
	 */
	public void emitCreep(VertexConsumer consumer, Matrix4f matrix, Level level, BlockPos origin, float radius,
			int alpha) {
		if (radius <= 0.0F) return;
		if (creepCells == null) creepCells = buildCreepCells(level, origin);
		for (float[] cell : creepCells) {
			if (cell[2] > radius + 1.5F) continue;
			float x0 = cell[0];
			float z0 = cell[1];
			float y = cell[7];
			creepVertex(consumer, matrix, x0, y, z0, cell[3], alpha);
			creepVertex(consumer, matrix, x0, y, z0 + 1.0F, cell[4], alpha);
			creepVertex(consumer, matrix, x0 + 1.0F, y, z0 + 1.0F, cell[5], alpha);
			creepVertex(consumer, matrix, x0 + 1.0F, y, z0, cell[6], alpha);
		}
	}

	private static void creepVertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z, float blue,
			int alpha) {
		consumer.addVertex(matrix, x, y, z).setUv(x - 0.5F, z - 0.5F)
				.setColor(CREEP_RED, RING_LAYER, (int) blue, alpha);
	}

	/** Cells beyond the trench line (or beyond the floor, with no trench), out to the creep's furthest reach. */
	private List<float[]> buildCreepCells(Level level, BlockPos origin) {
		List<float[]> cells = new ArrayList<>();
		float limit = half + 0.5F + CREEP_MARGIN;
		int reach = (int) Math.ceil(limit);
		for (int x = -reach; x <= reach; x++) {
			for (int z = -reach; z <= reach; z++) {
				float radius = (float) Math.hypot(x + 0.5F, z + 0.5F);
				if (radius > limit) continue;
				if (loop != null ? !loop.reachesOutside(x, z) : Math.abs(x) <= half && Math.abs(z) <= half) continue;
				int cellX = x;
				int cellZ = z;
				int ground = CreepCells.groundOffset(dy -> spillable(level, origin.offset(cellX, dy, cellZ)),
						CREEP_DROP);
				if (ground == CreepCells.NO_GROUND) continue;
				cells.add(new float[] {x, z, radius, ringBlue(x, z), ringBlue(x, z + 1), ringBlue(x + 1, z + 1),
						ringBlue(x + 1, z), ground + SURFACE_Y});
			}
		}
		return cells;
	}
```

- [ ] **Step 5: Draw the creep in the shader**

In `cardinal_floor_membrane.fsh`, after the `groundRing` function add:

```glsl
// The world stage's creep (RiteBlood.z, .w): veined membrane spread over the ground beyond the floor, out to
// RiteBlood.z blocks from the Focus behind a ragged front. A step's shock (RiteRings.xy) runs out across it, and
// strain (RiteMood.y) bruises it toward purple.
vec4 creepGround(vec2 p) {
    float r = length(p);
    float frontNoise = (fbm(p * 0.7 + 19.0) - 0.5) * 2.2 + (noise(p * 2.3 + 4.0) - 0.5) * 0.5;
    float covered = smoothstep(RiteBlood.z + frontNoise + 0.1, RiteBlood.z + frontNoise - 1.4, r)
            * clamp(RiteBlood.w, 0.0, 1.0);
    if (covered <= 0.003) {
        return vec4(0.0);
    }
    vec2 warp = vec2(fbm(p * 0.5 + 3.0), fbm(p * 0.5 + 8.0)) - 0.5;
    float vessel = line(cellular(p * 0.8 + warp * 1.6 + 23.0).y, 0.02 + 0.04 * fbm(p * 1.3));
    float fine = line(cellular(p * 2.2 + 5.0).y, 0.012) * 0.6;
    float body = max(vessel, fine);
    float film = 0.22 + 0.25 * fbm(p * 1.4 + 2.0);
    float pulse = Beat * exp(-max(r - FloorHalf, 0.0) * 0.15);
    float shock = RiteRings.y * ringBand(r, RiteRings.x, 0.9);
    vec3 color = mix(BaseColor * 0.7, VeinColor * (0.8 + 0.4 * pulse), body);
    color = mix(color, color * vec3(0.55, 0.45, 0.75), clamp(RiteMood.y, 0.0, 1.0));
    color += GlowColor * shock * body;
    float alpha = covered * (film * (1.0 - body) + body);
    return vec4(lit(color, body * (0.4 + 0.6 * pulse) + shock), clamp(alpha, 0.0, 1.0));
}
```

In `main()`, replace

```glsl
        vec4 ring = groundRing(p);
        fragColor = linear_fog(vec4(ring.rgb * ColorModulator.rgb, ring.a * vertexColor.a * ColorModulator.a),
                vertexDistance, FogStart, FogEnd, FogColor);
        return;
```

with

```glsl
        // Red 0 marks the world stage's creep; red 255 the grounding ring's passing pulse.
        vec4 ring = vertexColor.r < 0.5 ? creepGround(p) : groundRing(p);
        if (ring.a <= 0.004) {
            discard;
        }
        fragColor = linear_fog(vec4(ring.rgb * ColorModulator.rgb, ring.a * vertexColor.a * ColorModulator.a),
                vertexDistance, FogStart, FogEnd, FogColor);
        return;
```

- [ ] **Step 6: Draw it with the floor and widen the Focus's bounds**

In `CardinalFloorConsecrationRenderer.render`, after

```java
		if (rite.groundRingStrength() > 0.01F) {
			mesh.emitGroundRing(consumer, matrix, level, origin, rite.groundRingRadius(), 255);
		}
```

add

```java
		if (!collapsing && rite.creepRadius() > 0.01F) {
			mesh.emitCreep(consumer, matrix, level, origin, rite.creepRadius(), 255);
		}
```

In `renderRadius(CardinalFocusBlockEntity focus)`, replace

```java
		int beyond = FloorRiteResponse.groundRingLive(focus.getBlockPos())
				? (int) Math.ceil(CardinalRiteFloorVisualRules.GROUND_RING_BEYOND) + 3 : 0;
```

with

```java
		int ring = FloorRiteResponse.groundRingLive(focus.getBlockPos())
				? (int) Math.ceil(CardinalRiteFloorVisualRules.GROUND_RING_BEYOND) + 3 : 0;
		// A deepening rite's creep spreads past the floor's edge into the ground.
		var world = com.vincenthuto.hemomancy.client.render.world.rite.escalation.RiteWorldState.at(focus.getBlockPos());
		int creep = world == null ? 0 : (int) Math.ceil(com.vincenthuto.hemomancy.client.render.world.rite.escalation
				.RiteWorldMath.creepReach(world.footprint())) + 2;
		int beyond = Math.max(ring, creep);
```

(`beyond` stays effectively final, because the `map` lambda below captures it.)

- [ ] **Step 7: Validate the shader**

Run (Git Bash, from the project root):

```bash
mkdir -p build/shader-check && sed 's|#moj_import <fog.glsl>|vec4 linear_fog(vec4 c, float d, float s, float e, vec4 f) { return c; }\nfloat fog_distance(vec3 p, int s) { return length(p); }|' src/main/resources/assets/hemomancy/shaders/core/world/cardinal_floor_membrane.fsh > build/shader-check/cardinal_floor_membrane.frag && glslangValidator -S frag build/shader-check/cardinal_floor_membrane.frag
```

Expected: no output and exit code 0.

- [ ] **Step 8: Run the tests to verify they pass**

Run: `./gradlew.bat test --tests "com.vincenthuto.hemomancy.client.render.world.floor.*" --tests "com.vincenthuto.hemomancy.client.render.world.rite.escalation.*"`
Expected: PASS.

- [ ] **Step 9: Checkpoint (no commit)**

Run: `./gradlew.bat compileJava compileTestJava`
Expected: BUILD SUCCESSFUL.

### Task 9: The sky wound

**Files:**
- Create: `src/main/resources/assets/hemomancy/shaders/core/world/cardinal_rite_sky_wound.json`
- Create: `src/main/resources/assets/hemomancy/shaders/core/world/cardinal_rite_sky_wound.vsh`
- Create: `src/main/resources/assets/hemomancy/shaders/core/world/cardinal_rite_sky_wound.fsh`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/init/ShaderInit.java` (holder and registration)
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/render/HemoRenderTypes.java` (`cardinalRiteSkyWound`)
- Create: `src/main/java/com/vincenthuto/hemomancy/client/render/world/rite/escalation/RiteSkyWound.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java` (render at `AFTER_TRANSLUCENT_BLOCKS`, tick, clear)
- Test: `src/test/java/com/vincenthuto/hemomancy/client/render/world/rite/escalation/RiteSkyWoundTest.java`
- Test: `src/test/java/com/vincenthuto/hemomancy/client/render/world/rite/escalation/RiteWorldSourceTest.java` (add a check)

**Interfaces:**
- Consumes: `RiteWorldState.worlds()`, `World`, `RiteWorldMath.woundPresence`, `woundRadius`, `woundAltitude`, `woundEnding`, `woundFlash`, `woundStrikeInterval`, `ramp`, `COLLAPSE_STRIKE_TICKS`, `WOUND_DRAW_DISTANCE` (Task 5); `CardinalRiteFogLightning.strike(Vec3, Vec3, long, float)`.
- Produces: `ShaderInit.CARDINAL_RITE_SKY_WOUND`; `HemoRenderTypes.cardinalRiteSkyWound(float gameTime, float seed, float grow, float open, float flash, float strain, int baseColor, int veinColor, int glowColor)`; `RiteSkyWound.render(PoseStack, float)`, `tick(Minecraft)`, `clear()`; package-private `RiteSkyWound.Shape` and `static Shape shape(World, float)`.

- [ ] **Step 1: Write the failing tests**

`RiteSkyWoundTest.java`:

```java
package com.vincenthuto.hemomancy.client.render.world.rite.escalation;

import com.vincenthuto.hemomancy.common.rite.CardinalRiteInstability;
import com.vincenthuto.hemomancy.common.rite.CardinalRitePhase;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class RiteSkyWoundTest {
	private static RiteWorldState.World world(float level, CardinalRitePhase phase, RiteWorldMath.Ending ending,
			float endingAge) {
		return new RiteWorldState.World(BlockPos.ZERO, null, level, level, 0.0F, 2, 4, CardinalRiteInstability.STABLE,
				10.0F, "storm", true, phase, RiteFloor.NONE, ending, endingAge);
	}

	@Test
	void noWoundBeforeHalfway() {
		assertNull(RiteSkyWound.shape(world(0.25F, CardinalRitePhase.CONSECRATION, RiteWorldMath.Ending.NONE, 0), 1));
	}

	@Test
	void itFormsAtHalfwayAndGrowsWithTheRite() {
		RiteSkyWound.Shape formed = RiteSkyWound.shape(world(0.5F, CardinalRitePhase.ORDEAL, RiteWorldMath.Ending.NONE, 0), 1);
		assertEquals(1.0F, formed.presence(), 1e-6F);
		assertEquals(8.0F, formed.radius(), 1e-6F);
		assertEquals(0.0F, formed.grow(), 1e-6F);

		RiteSkyWound.Shape grown = RiteSkyWound.shape(world(1.0F, CardinalRitePhase.ORDEAL, RiteWorldMath.Ending.NONE, 0), 1);
		assertEquals(16.0F, grown.radius(), 1e-6F);
		assertEquals(1.0F, grown.grow(), 1e-6F);
	}

	@Test
	void itOpensOnlyAtCulmination() {
		assertEquals(0.0F, RiteSkyWound.shape(world(1.0F, CardinalRitePhase.OFFERING_PROCESSION,
				RiteWorldMath.Ending.NONE, 0), 1).open());
		assertEquals(1.0F, RiteSkyWound.shape(world(1.0F, CardinalRitePhase.CULMINATION,
				RiteWorldMath.Ending.NONE, 0), 1).open(), 1e-6F);
	}

	@Test
	void completionClosesItBehindAFlash() {
		RiteSkyWound.Shape closing = RiteSkyWound.shape(world(1.0F, CardinalRitePhase.CULMINATION,
				RiteWorldMath.Ending.COMPLETED, 0), 1);
		assertEquals(1.0F, closing.flash(), 1e-6F);
		assertEquals(16.0F, closing.radius(), 1e-6F);
		assertNull(RiteSkyWound.shape(world(1.0F, CardinalRitePhase.CULMINATION, RiteWorldMath.Ending.COMPLETED, 14), 1));
	}

	@Test
	void aCollapseShuddersItShut() {
		assertNotNull(RiteSkyWound.shape(world(1.0F, CardinalRitePhase.ORDEAL, RiteWorldMath.Ending.COLLAPSED, 2), 1));
		assertNull(RiteSkyWound.shape(world(1.0F, CardinalRitePhase.ORDEAL, RiteWorldMath.Ending.COLLAPSED, 30), 1));
	}
}
```

Add to `RiteWorldSourceTest`:

```java
	@Test
	void theSkyWoundIsRegisteredAndDrawn() throws IOException {
		String shaders = read("src/main/java/com/vincenthuto/hemomancy/common/init/ShaderInit.java");
		String events = read("src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java");

		assertTrue(shaders.contains("registerShader(event, CARDINAL_RITE_SKY_WOUND.createInstance(provider));"));
		assertTrue(events.contains("RiteSkyWound.render(event.getPoseStack(), partialTick);"));
		assertTrue(events.contains("RiteSkyWound.tick(Minecraft.getInstance());"));
		assertTrue(events.contains("RiteSkyWound.clear();"));
	}
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `./gradlew.bat test --tests "com.vincenthuto.hemomancy.client.render.world.rite.escalation.RiteSkyWoundTest"`
Expected: compilation FAILS with `cannot find symbol: class RiteSkyWound`.

- [ ] **Step 3: Write the shader**

`cardinal_rite_sky_wound.json`:

```json
{
  "blend": {
    "func": "add",
    "srcrgb": "srcalpha",
    "dstrgb": "1-srcalpha"
  },
  "vertex": "hemomancy:world/cardinal_rite_sky_wound",
  "fragment": "hemomancy:world/cardinal_rite_sky_wound",
  "attributes": [
    "Position",
    "Color",
    "UV0"
  ],
  "samplers": [],
  "uniforms": [
    { "name": "ModelViewMat", "type": "matrix4x4", "count": 16, "values": [ 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0 ] },
    { "name": "ProjMat", "type": "matrix4x4", "count": 16, "values": [ 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0 ] },
    { "name": "ColorModulator", "type": "float", "count": 4, "values": [ 1.0, 1.0, 1.0, 1.0 ] },
    { "name": "HemoTime", "type": "float", "count": 1, "values": [ 0.0 ] },
    { "name": "WoundSeed", "type": "float", "count": 1, "values": [ 0.0 ] },
    { "name": "WoundShape", "type": "float", "count": 4, "values": [ 0.0, 0.0, 0.0, 0.0 ] },
    { "name": "BaseColor", "type": "float", "count": 3, "values": [ 0.16, 0.04, 0.05 ] },
    { "name": "VeinColor", "type": "float", "count": 3, "values": [ 0.70, 0.08, 0.12 ] },
    { "name": "GlowColor", "type": "float", "count": 3, "values": [ 1.0, 0.54, 0.45 ] }
  ]
}
```

`cardinal_rite_sky_wound.vsh`:

```glsl
#version 150

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

in vec3 Position;
in vec4 Color;
in vec2 UV0;

out vec4 vertexColor;
out vec2 texCoord0;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    vertexColor = Color;
    texCoord0 = UV0;
}
```

`cardinal_rite_sky_wound.fsh`:

```glsl
#version 150

// The sky wound over a deepening Cardinal Rite: a slow vortex of cloud that darkens and grows veins as the rite
// deepens, glows at its core, and at culmination opens into a red eye. texCoord0 runs -1..1 across the disc.

uniform vec4 ColorModulator;
uniform float HemoTime;
uniform float WoundSeed;
// x: how far the wound has grown, y: how far its eye has opened, z: a flash, w: strain.
uniform vec4 WoundShape;
uniform vec3 BaseColor;
uniform vec3 VeinColor;
uniform vec3 GlowColor;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

float hash(vec2 value) {
    return fract(sin(dot(value, vec2(127.1, 311.7)) + WoundSeed * 0.013) * 43758.5453);
}

float noise(vec2 value) {
    vec2 cell = floor(value);
    vec2 local = fract(value);
    vec2 u = local * local * (3.0 - 2.0 * local);
    return mix(mix(hash(cell), hash(cell + vec2(1.0, 0.0)), u.x),
            mix(hash(cell + vec2(0.0, 1.0)), hash(cell + vec2(1.0, 1.0)), u.x), u.y);
}

float fbm(vec2 value) {
    float total = 0.0;
    float amplitude = 0.5;
    mat2 turn = mat2(0.8, -0.6, 0.6, 0.8);
    for (int i = 0; i < 5; i++) {
        total += noise(value) * amplitude;
        value = turn * value * 2.02 + 1.7;
        amplitude *= 0.5;
    }
    return total;
}

// Distance to the nearest cell point, and to the edge between the two nearest.
vec2 cellular(vec2 value) {
    vec2 cell = floor(value);
    vec2 local = fract(value);
    float nearest = 8.0;
    float second = 8.0;
    for (int y = -1; y <= 1; y++) {
        for (int x = -1; x <= 1; x++) {
            vec2 offset = vec2(float(x), float(y));
            vec2 point = offset + 0.15 + 0.7 * vec2(hash(cell + offset), hash(cell + offset + vec2(17.3, 41.9)));
            float distance = length(point - local);
            if (distance < nearest) {
                second = nearest;
                nearest = distance;
            } else if (distance < second) {
                second = distance;
            }
        }
    }
    return vec2(nearest, second - nearest);
}

float line(float distance, float width) {
    return 1.0 - smoothstep(width, width + 0.04, distance);
}

void main() {
    vec2 p = texCoord0;
    float r = length(p);
    if (r > 1.0) {
        discard;
    }
    float grow = clamp(WoundShape.x, 0.0, 1.0);
    float open = clamp(WoundShape.y, 0.0, 1.0);
    float flash = clamp(WoundShape.z, 0.0, 1.0);
    float strain = clamp(WoundShape.w, 0.0, 1.0);

    // The cloud turns about the centre, faster near it.
    float angle = atan(p.y, p.x);
    float twist = HemoTime * 0.004 + (1.0 - r) * (2.5 + 2.0 * grow);
    vec2 q = vec2(cos(angle + twist), sin(angle + twist)) * r;
    float cloud = fbm(q * 3.0 + WoundSeed);
    float bands = fbm(vec2(angle * 1.5 + twist * 2.0, r * 6.0 - HemoTime * 0.01) + 9.0);
    float density = smoothstep(1.0, 0.55, r) * (0.55 + 0.45 * cloud);

    // Veins thread the cloud as the wound grows.
    vec2 warp = vec2(fbm(q * 2.0 + 3.0), fbm(q * 2.0 + 7.0)) - 0.5;
    float veins = line(cellular(q * 4.0 + warp * 1.5).y, 0.015 + 0.02 * grow) * smoothstep(0.0, 0.6, grow);

    // The core glows; at culmination it opens into an eye with a burning rim. Strain makes the glow stutter.
    float core = exp(-r * r * (18.0 - 10.0 * grow));
    float eyeRadius = 0.08 + 0.32 * open;
    float eye = smoothstep(eyeRadius + 0.04, eyeRadius - 0.04, r) * open;
    float rimDistance = (r - eyeRadius) / 0.05;
    float rim = exp(-rimDistance * rimDistance) * open;
    float stutter = 1.0 - strain * 0.6 * step(0.5, fract(HemoTime * 0.37 + hash(vec2(floor(HemoTime * 0.25), 3.0))));

    vec3 color = mix(BaseColor * 0.35, BaseColor * 0.7 + VeinColor * 0.15, bands) * (0.6 + 0.4 * cloud);
    color = mix(color, VeinColor * 0.9, veins * 0.8);
    color += GlowColor * core * (0.25 + 0.75 * grow) * stutter;
    color = mix(color, VeinColor * 0.25, eye);
    color += GlowColor * rim * (0.8 + 0.4 * sin(HemoTime * 0.2));
    color += GlowColor * flash * (0.4 + 0.6 * exp(-r * 3.0));

    float alpha = density * (0.55 + 0.35 * grow) + veins * 0.4 + core * 0.5 + rim + eye * 0.9 + flash * 0.3;
    alpha = clamp(alpha, 0.0, 1.0) * smoothstep(1.0, 0.85, r) * vertexColor.a;
    fragColor = vec4(color * ColorModulator.rgb, alpha * ColorModulator.a);
}
```

- [ ] **Step 4: Register the shader and its render type**

In `ShaderInit`, after the `CARDINAL_RITE_FOG` holder add:

```java
	public static ShaderHolder CARDINAL_RITE_SKY_WOUND = new ShaderHolder(Hemomancy.rloc("world/cardinal_rite_sky_wound"),
			DefaultVertexFormat.POSITION_TEX_COLOR, "HemoTime", "WoundSeed", "WoundShape", "BaseColor", "VeinColor",
			"GlowColor");
```

and in `register(RegisterShadersEvent event)`, after `registerShader(event, CARDINAL_RITE_FOG.createInstance(provider));` add:

```java
        registerShader(event, CARDINAL_RITE_SKY_WOUND.createInstance(provider));
```

In `HemoRenderTypes`, after `createCardinalRiteFog(...)` add:

```java
	/** One sky-wound type per seed; its uniforms are updated in place, so callers end the batch per wound. */
	private static final Map<Float, CardinalSkyWoundType> CARDINAL_SKY_WOUND_TYPES = riteTypeCache();

	private static final class CardinalSkyWoundType {
		private final float seed;
		private float gameTime;
		private float grow;
		private float open;
		private float flash;
		private float strain;
		private int baseColor;
		private int veinColor;
		private int glowColor;
		private final RenderType type;

		private CardinalSkyWoundType(float seed) {
			this.seed = seed;
			this.type = createCardinalSkyWound(this);
		}
	}

	public static RenderType cardinalRiteSkyWound(float gameTime, float seed, float grow, float open, float flash,
			float strain, int baseColor, int veinColor, int glowColor) {
		CardinalSkyWoundType wound = CARDINAL_SKY_WOUND_TYPES.get(seed);
		if (wound == null) {
			wound = new CardinalSkyWoundType(seed);
			CARDINAL_SKY_WOUND_TYPES.put(seed, wound);
		}
		wound.gameTime = gameTime;
		wound.grow = grow;
		wound.open = open;
		wound.flash = flash;
		wound.strain = strain;
		wound.baseColor = baseColor;
		wound.veinColor = veinColor;
		wound.glowColor = glowColor;
		return wound.type;
	}

	private static RenderType createCardinalSkyWound(CardinalSkyWoundType wound) {
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"cardinal_rite_sky_wound_uniforms",
				() -> {
					ShaderInstance shader = ShaderInit.CARDINAL_RITE_SKY_WOUND.getInstance().get();
					setUniform(shader, "HemoTime", wound.gameTime);
					setUniform(shader, "WoundSeed", wound.seed);
					setUniform(shader, "WoundShape", wound.grow, wound.open, wound.flash, wound.strain);
					setRgbUniform(shader, "BaseColor", wound.baseColor);
					setRgbUniform(shader, "VeinColor", wound.veinColor);
					setRgbUniform(shader, "GlowColor", wound.glowColor);
					setUniform(shader, "ColorModulator", 1.0F, 1.0F, 1.0F, 1.0F);
				},
				() -> {
					ShaderInstance shader = ShaderInit.CARDINAL_RITE_SKY_WOUND.getInstance().get();
					if (shader instanceof ExtendedShaderInstance extended) {
						extended.setUniformDefaults();
					}
				});
		return RenderType.create("cardinal_rite_sky_wound",
				DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, 256, false, false,
				RenderType.CompositeState.builder()
						.setShaderState(ShaderInit.CARDINAL_RITE_SKY_WOUND.getShard())
						.setTexturingState(uniforms)
						.setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
						.setDepthTestState(RenderType.LEQUAL_DEPTH_TEST)
						.setWriteMaskState(RenderType.COLOR_WRITE)
						.setCullState(RenderType.NO_CULL)
						.setLightmapState(RenderType.NO_LIGHTMAP)
						.createCompositeState(false));
	}
```

- [ ] **Step 5: Create `RiteSkyWound`**

```java
package com.vincenthuto.hemomancy.client.render.world.rite.escalation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.client.render.world.CardinalRiteFogLightning;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteInstability;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;

/**
 * The sky wound: a slow vortex of cloud gathering high over a rite once it is halfway through, darkening and veined
 * as the rite deepens, opening into a red eye at culmination. Storm rites throw lightning from it, and a collapse
 * brings it down in a last few strikes. It is drawn to {@link RiteWorldMath#WOUND_DRAW_DISTANCE} as a landmark.
 */
public final class RiteSkyWound {
	private static final Map<BlockPos, Long> LAST_STRIKE = new HashMap<>();
	private static final RandomSource RANDOM = RandomSource.create();

	private RiteSkyWound() {
	}

	record Shape(float presence, float radius, float grow, float open, float flash, float strain) {
	}

	static Shape shape(RiteWorldState.World world, float partialTick) {
		float level = world.level(partialTick);
		float ending = RiteWorldMath.woundEnding(world.ending(), world.endingAge());
		boolean closes = world.ending() == RiteWorldMath.Ending.COMPLETED
				|| world.ending() == RiteWorldMath.Ending.COLLAPSED;
		float presence = RiteWorldMath.woundPresence(level)
				* (closes ? Math.min(1.0F, Math.max(0.0F, ending) * 4.0F) : ending);
		if (presence <= 0.003F) return null;
		float radius = RiteWorldMath.woundRadius(world.footprint(), level) * (closes ? Math.max(0.0F, ending) : 1.0F);
		float grow = RiteWorldMath.ramp(level, RiteWorldMath.WOUND_FROM);
		float open = world.culminating() ? CardinalRiteMath.smoothstep(0.9F, 1.0F, level) : 0.0F;
		float flash = RiteWorldMath.woundFlash(world.ending(), world.endingAge(), world.surge());
		float strain = world.strain() == CardinalRiteInstability.STABLE ? 0.0F
				: world.strain() == CardinalRiteInstability.STRAINED ? 0.5F : 1.0F;
		return new Shape(presence, radius, grow, open, flash, strain);
	}

	public static void render(PoseStack poseStack, float partialTick) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) return;
		Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
		float time = mc.level.getGameTime() + partialTick;
		MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
		for (RiteWorldState.World world : RiteWorldState.worlds()) {
			Shape shape = shape(world, partialTick);
			if (shape == null) continue;
			BlockPos center = world.center();
			double x = center.getX() + 0.5D;
			double z = center.getZ() + 0.5D;
			double y = RiteWorldMath.woundAltitude(center.getY(), world.footprint(), mc.level.getMaxBuildHeight());
			double distance = Math.sqrt((x - camera.x) * (x - camera.x) + (z - camera.z) * (z - camera.z));
			if (distance > RiteWorldMath.WOUND_DRAW_DISTANCE) continue;
			float fade = (float) Math.min(1.0D, (RiteWorldMath.WOUND_DRAW_DISTANCE - distance) / 64.0D);
			var palette = world.floor().palette();
			RenderType type = HemoRenderTypes.cardinalRiteSkyWound(time, seed(center), shape.grow(), shape.open(),
					shape.flash(), shape.strain(), palette.base(), palette.vein(), palette.glow());
			VertexConsumer consumer = buffers.getBuffer(type);
			poseStack.pushPose();
			poseStack.translate(x - camera.x, y - camera.y, z - camera.z);
			disc(consumer, poseStack.last().pose(), shape.radius(), Math.round(255.0F * shape.presence() * fade));
			poseStack.popPose();
			buffers.endBatch(type);
		}
	}

	private static void disc(VertexConsumer consumer, Matrix4f matrix, float radius, int alpha) {
		consumer.addVertex(matrix, -radius, 0.0F, -radius).setUv(-1.0F, -1.0F).setColor(255, 255, 255, alpha);
		consumer.addVertex(matrix, -radius, 0.0F, radius).setUv(-1.0F, 1.0F).setColor(255, 255, 255, alpha);
		consumer.addVertex(matrix, radius, 0.0F, radius).setUv(1.0F, 1.0F).setColor(255, 255, 255, alpha);
		consumer.addVertex(matrix, radius, 0.0F, -radius).setUv(1.0F, -1.0F).setColor(255, 255, 255, alpha);
	}

	/** Ambient strikes from storm rites' wounds, and a collapse's last strikes. Called once per client tick. */
	public static void tick(Minecraft mc) {
		if (mc.level == null) {
			LAST_STRIKE.clear();
			return;
		}
		long now = mc.level.getGameTime();
		for (RiteWorldState.World world : RiteWorldState.worlds()) {
			Shape shape = shape(world, 1.0F);
			if (shape == null) continue;
			boolean strike = false;
			if (world.ending() == RiteWorldMath.Ending.COLLAPSED) {
				for (int at : RiteWorldMath.COLLAPSE_STRIKE_TICKS) {
					if (Math.round(world.endingAge()) == at) strike = true;
				}
			} else if (!world.ended() && world.lightning()) {
				Long last = LAST_STRIKE.get(world.center());
				if (last == null) {
					LAST_STRIKE.put(world.center(), now);
				} else if (now - last >= RiteWorldMath.woundStrikeInterval(world.level(1.0F))) {
					strike = true;
				}
			}
			if (!strike) continue;
			LAST_STRIKE.put(world.center(), now);
			BlockPos center = world.center();
			double y = RiteWorldMath.woundAltitude(center.getY(), world.footprint(), mc.level.getMaxBuildHeight());
			double fromAngle = RANDOM.nextDouble() * Math.PI * 2.0D;
			double from = RANDOM.nextDouble() * shape.radius() * 0.5D;
			double toAngle = RANDOM.nextDouble() * Math.PI * 2.0D;
			double to = RANDOM.nextDouble() * world.footprint() * 0.8D;
			Vec3 start = new Vec3(center.getX() + 0.5D + Math.cos(fromAngle) * from, y - 1.0D,
					center.getZ() + 0.5D + Math.sin(fromAngle) * from);
			Vec3 end = new Vec3(center.getX() + 0.5D + Math.cos(toAngle) * to, center.getY() + 1.1D,
					center.getZ() + 0.5D + Math.sin(toAngle) * to);
			CardinalRiteFogLightning.strike(start, end, RANDOM.nextLong(), 0.0F);
		}
		LAST_STRIKE.keySet().removeIf(center -> RiteWorldState.at(center) == null);
	}

	public static void clear() {
		LAST_STRIKE.clear();
	}

	private static float seed(BlockPos center) {
		return center.getX() * 0.173F + center.getY() * 0.051F + center.getZ() * 0.211F;
	}
}
```

- [ ] **Step 6: Hook it into the client**

In `ClientEvents.renderLevelLastEvent`, inside the `AFTER_TRANSLUCENT_BLOCKS` block, after the `CardinalRiteCulminationRenderer.render(...)` line add:

```java
            com.vincenthuto.hemomancy.client.render.world.rite.escalation.RiteSkyWound.render(event.getPoseStack(), partialTick);
```

In `onClientTickPost`, inside the `if` block that ticks `RiteWorldState` (Task 5), after that call add:

```java
            com.vincenthuto.hemomancy.client.render.world.rite.escalation.RiteSkyWound.tick(Minecraft.getInstance());
```

In `onClientPlayerLogout` and `onClientLevelUnload`, after `RiteWorldState.clear();` add:

```java
		com.vincenthuto.hemomancy.client.render.world.rite.escalation.RiteSkyWound.clear();
```

- [ ] **Step 7: Validate the shaders**

Run (Git Bash, from the project root):

```bash
glslangValidator -S frag src/main/resources/assets/hemomancy/shaders/core/world/cardinal_rite_sky_wound.fsh && glslangValidator -S vert src/main/resources/assets/hemomancy/shaders/core/world/cardinal_rite_sky_wound.vsh
```

Expected: no errors and exit code 0. These shaders import nothing, so no stubs are needed.

- [ ] **Step 8: Run the tests to verify they pass**

Run: `./gradlew.bat test --tests "com.vincenthuto.hemomancy.client.render.world.rite.escalation.*"`
Expected: PASS.

- [ ] **Step 9: Checkpoint (no commit)**

Run: `./gradlew.bat compileJava compileTestJava`
Expected: BUILD SUCCESSFUL.

### Task 10: Braziers lean, grow and gutter

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/render/world/BrazierFlames.java` (`render`)
- Test: covered by `RiteWorldStateTest.braziersInsideTheFootprintLeanTowardTheFocus` (Task 5); `ModelFirstTileRendererResourceTest` must keep passing.

**Interfaces:**
- Consumes: `RiteWorldState.flameAt(BlockPos, double)` and `RiteWorldState.Flame` (Task 5).
- Produces: nothing new.

- [ ] **Step 1: Bend the fire**

In `BrazierFlames.render`, replace

```java
        Vec3 up = new Vec3(0, 1, 0);
        double scale = (sanguine ? 1.2 : 1.0) * (.35 + .65 * fill);
        int shape = sanguine ? TORCH : FLAME;
        int color = sanguine ? TORCH_COLOR : WHITE;
```

with

```java
        // A deepening rite leans its braziers toward the Focus, feeds them taller and turns them sanguine from
        // halfway; strain gutters them.
        var flame = com.vincenthuto.hemomancy.client.render.world.rite.escalation.RiteWorldState.flameAt(pos, time);
        boolean hot = sanguine || flame.sanguine();
        Vec3 up = new Vec3(flame.leanX(), 1, flame.leanZ()).normalize();
        double scale = (hot ? 1.2 : 1.0) * (.35 + .65 * fill) * flame.height();
        int shape = hot ? TORCH : FLAME;
        int color = hot ? TORCH_COLOR : WHITE;
```

Then replace `if (!sanguine) ThermalGeometry.smoke(` with `if (!hot) ThermalGeometry.smoke(`, and `feed(pos, sanguine, seed);` with `feed(pos, hot, seed);`.

- [ ] **Step 2: Run the tests to verify they pass**

Run: `./gradlew.bat test --tests "com.vincenthuto.hemomancy.common.block.ModelFirstTileRendererResourceTest" --tests "com.vincenthuto.hemomancy.client.render.world.rite.escalation.RiteWorldStateTest"`
Expected: PASS. The brazier test still finds `ThermalRenderTypes.STEADY_FLAME`, `HemoRenderTypes.bloodPool(` and `ThermalParticles.lick(`, and no `ParticleTypes.`.

- [ ] **Step 3: Checkpoint (no commit)**

Run: `./gradlew.bat compileJava`
Expected: BUILD SUCCESSFUL.

### Task 11: Blood rain

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/client/render/world/rite/escalation/RiteBloodRain.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java` (`onClientTickPost`)
- Test: `src/test/java/com/vincenthuto/hemomancy/client/render/world/rite/escalation/RiteWorldSourceTest.java` (add a check)

**Interfaces:**
- Consumes: `RiteWorldState.worlds()`, `RiteWorldMath.rainLevel`, `rainEnding`, `rainPerTick` (Task 5); `HemoParticleData.bloodCell(ParticleColor)`.
- Produces: `RiteBloodRain.tick(Minecraft)`.

- [ ] **Step 1: Write the failing test**

Add to `RiteWorldSourceTest`:

```java
	@Test
	void bloodRainUsesHemomancyParticlesOnly() throws IOException {
		String rain = read("src/main/java/com/vincenthuto/hemomancy/client/render/world/rite/escalation/RiteBloodRain.java");
		String events = read("src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java");

		assertTrue(rain.contains("HemoParticleData.bloodCell("));
		assertFalse(rain.contains("ParticleTypes."), "no vanilla particles");
		assertTrue(events.contains("RiteBloodRain.tick(Minecraft.getInstance());"));
	}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew.bat test --tests "com.vincenthuto.hemomancy.client.render.world.rite.escalation.RiteWorldSourceTest"`
Expected: FAIL with `NoSuchFileException` for `RiteBloodRain.java`.

- [ ] **Step 3: Create `RiteBloodRain`**

```java
package com.vincenthuto.hemomancy.client.render.world.rite.escalation;

import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

/**
 * Late in a rite, blood rains inside its boundary from under the sky wound: blood cells falling through the air,
 * with splashes bursting on the ground beneath. Splashes are spawned on the ground directly rather than tracking each
 * drop down to it.
 */
public final class RiteBloodRain {
	private static final double SPAWN_HEIGHT = 12.0D;
	private static final double SPAWN_SPREAD = 4.0D;
	/** The blood-cell particle doubles its speed and fades over its life; this falls about fourteen blocks. */
	private static final double FALL_SPEED = -0.4D;
	private static final double CULL_DISTANCE = 64.0D;
	private static final RandomSource RANDOM = RandomSource.create();

	private RiteBloodRain() {
	}

	public static void tick(Minecraft mc) {
		if (mc.level == null || mc.isPaused()) return;
		float particleScale = switch (mc.options.particles().get()) {
			case ALL -> 1.0F;
			case DECREASED -> 0.5F;
			case MINIMAL -> 0.0F;
		};
		if (particleScale <= 0.0F) return;
		Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
		for (RiteWorldState.World world : RiteWorldState.worlds()) {
			float rain = RiteWorldMath.rainLevel(world.level(1.0F))
					* RiteWorldMath.rainEnding(world.ending(), world.endingAge());
			int count = RiteWorldMath.rainPerTick(rain, world.footprint(), particleScale, world.strain());
			if (count <= 0) continue;
			BlockPos center = world.center();
			double cx = center.getX() + 0.5D;
			double cz = center.getZ() + 0.5D;
			double reach = CULL_DISTANCE + world.footprint();
			if ((cx - camera.x) * (cx - camera.x) + (cz - camera.z) * (cz - camera.z) > reach * reach) continue;
			ParticleOptions drop = HemoParticleData.bloodCell(colour(world.floor().palette().vein()));
			for (int i = 0; i < count; i++) {
				double angle = RANDOM.nextDouble() * Math.PI * 2.0D;
				double radius = Math.sqrt(RANDOM.nextDouble()) * world.footprint();
				mc.level.addParticle(drop, cx + Math.cos(angle) * radius,
						center.getY() + 1.0D + SPAWN_HEIGHT + RANDOM.nextDouble() * SPAWN_SPREAD,
						cz + Math.sin(angle) * radius, 0.0D, FALL_SPEED, 0.0D);
			}
			ParticleOptions splash = HemoParticleData.bloodCell(colour(world.floor().palette().glow()));
			for (int i = 0; i < Math.max(1, count / 3); i++) {
				double angle = RANDOM.nextDouble() * Math.PI * 2.0D;
				double radius = Math.sqrt(RANDOM.nextDouble()) * world.footprint();
				int x = Mth.floor(cx + Math.cos(angle) * radius);
				int z = Mth.floor(cz + Math.sin(angle) * radius);
				int top = mc.level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
				if (Math.abs(top - (center.getY() + 1)) > 6) continue;
				for (int k = 0; k < 3; k++) {
					mc.level.addParticle(splash, x + RANDOM.nextDouble(), top + 0.05D, z + RANDOM.nextDouble(),
							(RANDOM.nextDouble() - 0.5D) * 0.06D, 0.04D + RANDOM.nextDouble() * 0.03D,
							(RANDOM.nextDouble() - 0.5D) * 0.06D);
				}
			}
		}
	}

	private static ParticleColor colour(int argb) {
		return new ParticleColor((argb >> 16) & 255, (argb >> 8) & 255, argb & 255);
	}
}
```

- [ ] **Step 4: Tick it**

In `ClientEvents.onClientTickPost`, inside the `if` block that ticks `RiteWorldState`, after `RiteSkyWound.tick(...)` add:

```java
            com.vincenthuto.hemomancy.client.render.world.rite.escalation.RiteBloodRain.tick(Minecraft.getInstance());
```

- [ ] **Step 5: Run the test to verify it passes**

Run: `./gradlew.bat test --tests "com.vincenthuto.hemomancy.client.render.world.rite.escalation.RiteWorldSourceTest"`
Expected: PASS.

- [ ] **Step 6: Checkpoint (no commit)**

Run: `./gradlew.bat compileJava compileTestJava`
Expected: BUILD SUCCESSFUL.

### Task 12: The sound bed

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/init/SoundInit.java` (three events after `RITE_ESCALATE`)
- Modify: `src/main/resources/assets/hemomancy/sounds.json` (three entries after `"rite.escalate"`)
- Modify: `src/main/resources/assets/hemomancy/lang/en_us.json` (three subtitles)
- Create: `src/main/java/com/vincenthuto/hemomancy/client/sound/RiteLoopSound.java`
- Create: `src/main/java/com/vincenthuto/hemomancy/client/sound/RiteSoundBed.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java` (tick, stop on logout and unload)
- Test: `src/test/java/com/vincenthuto/hemomancy/client/sound/RiteSoundBedTest.java`

**Interfaces:**
- Consumes: `RiteWorldState.worlds()`, `World`, `RiteWorldMath.viewerWeight`, `droneVolume`, `heartbeatVolume`, `stormVolume`, `strainPitch`, `soundEnding` (Task 5); `CardinalRiteFloorVisualRules.riteTempo(String)`, `TEMPO_ACTIVE`, `TEMPO_INTENSE`.
- Produces: `SoundInit.RITE_AMBIENT_DRONE`, `RITE_AMBIENT_HEARTBEAT`, `RITE_AMBIENT_STORM`; `RiteLoopSound`; `RiteSoundBed.tick(Minecraft)`, `stopAll()`; package-private `static World nearest(List<World>, Vec3)` and `static int beatPeriod(CardinalRitePhase)`.

- [ ] **Step 1: Write the failing test**

```java
package com.vincenthuto.hemomancy.client.sound;

import com.vincenthuto.hemomancy.client.render.world.rite.escalation.RiteFloor;
import com.vincenthuto.hemomancy.client.render.world.rite.escalation.RiteWorldMath;
import com.vincenthuto.hemomancy.client.render.world.rite.escalation.RiteWorldState;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteInstability;
import com.vincenthuto.hemomancy.common.rite.CardinalRitePhase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

final class RiteSoundBedTest {
	private static RiteWorldState.World world(BlockPos center, int stage) {
		float level = RiteWorldMath.level(stage, 4);
		return new RiteWorldState.World(center, null, level, level, 0.0F, stage, 4, CardinalRiteInstability.STABLE,
				8.0F, "storm", true, CardinalRitePhase.ORDEAL, RiteFloor.NONE, RiteWorldMath.Ending.NONE, 0.0F);
	}

	@Test
	void soundBedFollowsTheNearestRite() {
		BlockPos near = new BlockPos(5, 64, 0);
		BlockPos far = new BlockPos(30, 64, 0);

		RiteWorldState.World chosen = RiteSoundBed.nearest(List.of(world(far, 3), world(near, 1)), new Vec3(0, 65, 0));

		assertEquals(near, chosen.center());
	}

	@Test
	void riteBeforeItsFirstStepOrOutOfEarshotIsSilent() {
		assertNull(RiteSoundBed.nearest(List.of(world(BlockPos.ZERO, 0)), new Vec3(0, 65, 0)));
		assertNull(RiteSoundBed.nearest(List.of(world(new BlockPos(200, 64, 0), 3)), new Vec3(0, 65, 0)));
	}

	@Test
	void theHeartbeatKeepsTheFloorsTempo() {
		assertEquals(18, RiteSoundBed.beatPeriod(CardinalRitePhase.ORDEAL));
		assertEquals(18, RiteSoundBed.beatPeriod(CardinalRitePhase.CULMINATION));
		assertEquals(24, RiteSoundBed.beatPeriod(CardinalRitePhase.STILL_INTERVAL));
		assertEquals(36, RiteSoundBed.beatPeriod(null), "succession keeps the resting beat");
	}
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew.bat test --tests "com.vincenthuto.hemomancy.client.sound.RiteSoundBedTest"`
Expected: compilation FAILS with `cannot find symbol: class RiteSoundBed`.

- [ ] **Step 3: Register the sound events**

In `SoundInit`, after `RITE_ESCALATE` add:

```java
	public static final DeferredHolder<SoundEvent, SoundEvent> RITE_AMBIENT_DRONE = registerSoundEvent("rite.ambient.drone");
	public static final DeferredHolder<SoundEvent, SoundEvent> RITE_AMBIENT_HEARTBEAT = registerSoundEvent("rite.ambient.heartbeat");
	public static final DeferredHolder<SoundEvent, SoundEvent> RITE_AMBIENT_STORM = registerSoundEvent("rite.ambient.storm");
```

In `sounds.json`, add a comma after the `"rite.escalate"` entry, then:

```json
  "rite.ambient.drone": {"subtitle": "subtitle.hemomancy.rite.ambient.drone", "sounds": [{"name": "minecraft:ambient.crimson_forest.loop", "type": "event", "pitch": 0.7}]},
  "rite.ambient.heartbeat": {"subtitle": "subtitle.hemomancy.rite.ambient.heartbeat", "sounds": [{"name": "minecraft:entity.warden.heartbeat", "type": "event", "pitch": 0.8}]},
  "rite.ambient.storm": {"subtitle": "subtitle.hemomancy.rite.ambient.storm", "sounds": [{"name": "minecraft:ambient.basalt_deltas.loop", "type": "event", "pitch": 0.8}]}
```

In `en_us.json`, after `"subtitle.hemomancy.rite.escalate": "The rite deepens",` add:

```json
  "subtitle.hemomancy.rite.ambient.drone": "The rite drones",
  "subtitle.hemomancy.rite.ambient.heartbeat": "The floor's heart beats",
  "subtitle.hemomancy.rite.ambient.storm": "Blood wind howls",
```

- [ ] **Step 4: Create `RiteLoopSound`**

```java
package com.vincenthuto.hemomancy.client.sound;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

/** One looping layer of a rite's sound bed; the bed steers its volume, pitch and place each tick. */
public final class RiteLoopSound extends AbstractTickableSoundInstance {
	private float targetVolume;
	private float targetPitch = 1.0F;

	public RiteLoopSound(SoundEvent event, double x, double y, double z) {
		super(event, SoundSource.AMBIENT, RandomSource.create());
		this.looping = true;
		this.delay = 0;
		this.volume = 0.001F;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public void steer(float volume, float pitch, double x, double y, double z) {
		this.targetVolume = volume;
		this.targetPitch = pitch;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public void fadeOut() {
		targetVolume = 0.0F;
	}

	@Override
	public boolean canStartSilent() {
		return true;
	}

	@Override
	public void tick() {
		volume += (targetVolume - volume) * 0.15F;
		pitch += (targetPitch - pitch) * 0.15F;
		if (targetVolume <= 0.001F && volume <= 0.005F) stop();
	}
}
```

- [ ] **Step 5: Create `RiteSoundBed`**

```java
package com.vincenthuto.hemomancy.client.sound;

import com.vincenthuto.hemomancy.client.render.world.rite.escalation.RiteWorldMath;
import com.vincenthuto.hemomancy.client.render.world.rite.escalation.RiteWorldState;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.rite.CardinalRitePhase;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorVisualRules;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * The sound bed of the nearest deepening rite: a drone from its first step, the floor's heartbeat once the creep
 * comes, and wind late on. Strain lowers its pitch, still intervals silence it, and a collapse cuts it off.
 */
public final class RiteSoundBed {
	private static RiteLoopSound drone;
	private static RiteLoopSound storm;
	private static BlockPos following;

	private RiteSoundBed() {
	}

	public static void tick(Minecraft mc) {
		if (mc.level == null || mc.player == null) {
			stopAll();
			return;
		}
		if (mc.isPaused()) return;
		Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
		RiteWorldState.World world = nearest(RiteWorldState.worlds(), camera);
		if (world == null || !world.center().equals(following)) {
			if (drone != null) drone.fadeOut();
			if (storm != null) storm.fadeOut();
			drone = null;
			storm = null;
			following = world == null ? null : world.center();
			if (world == null) return;
		}
		BlockPos center = world.center();
		double x = center.getX() + 0.5D;
		double y = center.getY() + 1.5D;
		double z = center.getZ() + 0.5D;
		double dx = x - camera.x;
		double dz = z - camera.z;
		float weight = RiteWorldMath.viewerWeight(Math.sqrt(dx * dx + dz * dz), world.footprint());
		float ending = RiteWorldMath.soundEnding(world.ending(), world.endingAge());
		float level = world.level(1.0F);
		float pitch = RiteWorldMath.strainPitch(world.strain());
		drone = steer(mc, drone, SoundInit.RITE_AMBIENT_DRONE.get(),
				RiteWorldMath.droneVolume(level, weight, world.still()) * ending, pitch, x, y, z);
		storm = steer(mc, storm, SoundInit.RITE_AMBIENT_STORM.get(),
				RiteWorldMath.stormVolume(level, weight, world.still()) * ending, pitch, x, y + 6.0D, z);
		float heart = RiteWorldMath.heartbeatVolume(level, weight, world.still()) * ending;
		if (heart > 0.01F && mc.level.getGameTime() % beatPeriod(world.phase()) == 0) {
			mc.level.playLocalSound(x, y, z, SoundInit.RITE_AMBIENT_HEARTBEAT.get(), SoundSource.AMBIENT, heart, pitch,
					false);
		}
	}

	public static void stopAll() {
		Minecraft mc = Minecraft.getInstance();
		if (drone != null) mc.getSoundManager().stop(drone);
		if (storm != null) mc.getSoundManager().stop(storm);
		drone = null;
		storm = null;
		following = null;
	}

	/** The floor's strong beats come every 36, 24 or 18 ticks at rest, during a rite, and in its hardest phases. */
	static int beatPeriod(CardinalRitePhase phase) {
		int tempo = CardinalRiteFloorVisualRules.riteTempo(phase == null ? "" : phase.name());
		return tempo >= CardinalRiteFloorVisualRules.TEMPO_INTENSE ? 18
				: tempo == CardinalRiteFloorVisualRules.TEMPO_ACTIVE ? 24 : 36;
	}

	/** The nearest rite that has taken a step and is within earshot. */
	static RiteWorldState.World nearest(List<RiteWorldState.World> worlds, Vec3 camera) {
		RiteWorldState.World best = null;
		double bestDistance = Double.MAX_VALUE;
		for (RiteWorldState.World world : worlds) {
			if (world.stage() <= 0) continue;
			double dx = world.center().getX() + 0.5D - camera.x;
			double dz = world.center().getZ() + 0.5D - camera.z;
			double distance = Math.sqrt(dx * dx + dz * dz);
			if (RiteWorldMath.viewerWeight(distance, world.footprint()) <= 0.0F) continue;
			if (distance < bestDistance) {
				best = world;
				bestDistance = distance;
			}
		}
		return best;
	}

	private static RiteLoopSound steer(Minecraft mc, RiteLoopSound sound, SoundEvent event, float volume, float pitch,
			double x, double y, double z) {
		if (sound == null || sound.isStopped()) {
			if (volume <= 0.01F) return null;
			sound = new RiteLoopSound(event, x, y, z);
			mc.getSoundManager().play(sound);
		}
		sound.steer(volume, pitch, x, y, z);
		return sound;
	}
}
```

- [ ] **Step 6: Tick it and stop it**

In `ClientEvents.onClientTickPost`, after the `if` block that ticks `RiteWorldState` and its layers, add:

```java
        com.vincenthuto.hemomancy.client.sound.RiteSoundBed.tick(Minecraft.getInstance());
```

(outside that block, so it can stop itself when the level is gone). In `onClientPlayerLogout` and `onClientLevelUnload`, after `RiteSkyWound.clear();` add:

```java
		com.vincenthuto.hemomancy.client.sound.RiteSoundBed.stopAll();
```

- [ ] **Step 7: Run the test to verify it passes**

Run: `./gradlew.bat test --tests "com.vincenthuto.hemomancy.client.sound.RiteSoundBedTest"`
Expected: PASS.

- [ ] **Step 8: Checkpoint (no commit)**

Run: `./gradlew.bat compileJava compileTestJava`
Expected: BUILD SUCCESSFUL.

### Task 13: Blood threads over sleeping sockets

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/client/render/world/rite/escalation/RiteSocketMarkerRenderer.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java` (render at `AFTER_TRANSLUCENT_BLOCKS`)
- Test: `src/test/java/com/vincenthuto/hemomancy/client/render/world/rite/escalation/RiteWorldSourceTest.java` (add a check)

**Interfaces:**
- Consumes: `RiteEntry.getSocketMarkers()`, `ActiveRiteClientData.SocketMarker` (Task 3); `RiteSurface`, `AnimusMortemRenderTypes`, `CardinalRiteMath.heartbeat`.
- Produces: `RiteSocketMarkerRenderer.render(PoseStack, float)`.

- [ ] **Step 1: Write the failing test**

Add to `RiteWorldSourceTest`:

```java
	@Test
	void sleepingSocketsAreMarkedInTheWorld() throws IOException {
		String events = read("src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java");
		String marker = read("src/main/java/com/vincenthuto/hemomancy/client/render/world/rite/escalation/RiteSocketMarkerRenderer.java");

		assertTrue(events.contains("RiteSocketMarkerRenderer.render(event.getPoseStack(), partialTick);"));
		assertTrue(marker.contains("rite.getSocketMarkers()"));
		assertTrue(marker.contains("AnimusMortemRenderTypes.ANIMUS"), "living blood, like the anchor jets");
	}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew.bat test --tests "com.vincenthuto.hemomancy.client.render.world.rite.escalation.RiteWorldSourceTest"`
Expected: FAIL with `NoSuchFileException` for `RiteSocketMarkerRenderer.java`.

- [ ] **Step 3: Create `RiteSocketMarkerRenderer`**

```java
package com.vincenthuto.hemomancy.client.render.world.rite.escalation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.data.ActiveRiteClientData;
import com.vincenthuto.hemomancy.client.render.world.AnimusMortemRenderTypes;
import com.vincenthuto.hemomancy.client.render.world.rite.RiteSurface;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/**
 * A thin, pulsing thread of blood standing over each required support socket whose sigil still sleeps, so the
 * caster can see what the circle still lacks without a panel. It is the anchor jet's thread at a smaller scale.
 */
public final class RiteSocketMarkerRenderer {
	private static final RiteSurface.View VIEW = new RiteSurface.View();
	private static final RiteSurface.Path PATH = new RiteSurface.Path();
	private static final double MAX_DISTANCE_SQR = 64.0D * 64.0D;
	private static final double HEIGHT = 1.4D;
	private static final int STATIONS = 7;

	private RiteSocketMarkerRenderer() {
	}

	public static void render(PoseStack poseStack, float partialTick) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) return;
		Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
		double time = mc.level.getGameTime() + partialTick;
		MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
		VertexConsumer blood = null;
		for (ActiveRiteClientData.RiteEntry rite : ActiveRiteClientData.getActiveRites()) {
			if (rite.isUnstained()) continue;
			for (ActiveRiteClientData.SocketMarker marker : rite.getSocketMarkers()) {
				Vec3 at = Vec3.atBottomCenterOf(marker.pos());
				if (camera.distanceToSqr(at) > MAX_DISTANCE_SQR) continue;
				if (blood == null) {
					AnimusMortemRenderTypes.begin(time);
					blood = buffers.getBuffer(AnimusMortemRenderTypes.ANIMUS);
				}
				drawThread(poseStack, blood, mc, camera, at, time, marker.sigil().hashCode());
			}
		}
		if (blood != null) buffers.endBatch(AnimusMortemRenderTypes.ANIMUS);
	}

	private static void drawThread(PoseStack poseStack, VertexConsumer consumer, Minecraft mc, Vec3 camera, Vec3 at,
			double time, int seed) {
		VIEW.set(camera.x, camera.y, camera.z, at.x, at.y, at.z, mc.gameRenderer.getMainCamera().rotation());
		poseStack.pushPose();
		poseStack.translate(at.x - camera.x, at.y - camera.y, at.z - camera.z);
		Matrix4f matrix = poseStack.last().pose();
		double beat = CardinalRiteMath.heartbeat((float) (time + (seed & 15)));
		double height = HEIGHT * (0.75D + 0.35D * beat);
		PATH.clear();
		for (int s = 0; s < STATIONS; s++) {
			double t = s / (STATIONS - 1.0D);
			double waver = Math.sin(time * 0.5D + t * 7.0D + seed) * 0.02D * t;
			PATH.add(waver, t * height, waver * 0.6D, 0.03D * (1.0D - 0.6D * t));
		}
		RiteSurface.ribbon(matrix, consumer, VIEW, PATH, RiteSurface.RIBBON, seed, RiteSurface.TINT, 0.85F);
		RiteSurface.card(matrix, consumer, VIEW, 0.0D, height, 0.0D, 0.04D, 0.04D, RiteSurface.FOCUS, seed + 1,
				RiteSurface.CORE, 0.5F + 0.4F * (float) beat);
		poseStack.popPose();
	}
}
```

- [ ] **Step 4: Draw it**

In `ClientEvents.renderLevelLastEvent`, inside the `AFTER_TRANSLUCENT_BLOCKS` block, after the `CardinalRiteAnchorJetRenderer.render(...)` line add:

```java
            com.vincenthuto.hemomancy.client.render.world.rite.escalation.RiteSocketMarkerRenderer.render(event.getPoseStack(), partialTick);
```

- [ ] **Step 5: Run the test to verify it passes**

Run: `./gradlew.bat test --tests "com.vincenthuto.hemomancy.client.render.world.rite.escalation.RiteWorldSourceTest"`
Expected: PASS.

- [ ] **Step 6: Checkpoint (no commit)**

Run: `./gradlew.bat compileJava compileTestJava`
Expected: BUILD SUCCESSFUL.

### Task 14: Saved-rite GameTest, docs and full verification

**Files:**
- Create: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/CardinalRiteEscalationGameTests.java`
- Modify: `docs/HEMOMANCY_REFERENCE.md` (after the paragraph that begins `**Rite beats on the boundary.**`)
- Modify: `wiki/Harbinger-Path.md` (after the sentence `When the rite ends, the Staff returns to that hand.`)
- Modify: `docs/CARDINAL_RITE_AUDIT.md` (status section, after the line that begins `**Effects (§6.2):**`)

**Interfaces:**
- Consumes: everything above.
- Produces: nothing new.

- [ ] **Step 1: Write the GameTest**

Real `ItemStack` offerings need the game's registries, so this runs as a GameTest rather than a JVM test:

```java
package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteEscalation;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

/** The world stage of a rite saved with real offerings, as the server saves it. */
@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CardinalRiteEscalationGameTests {
	@GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "rite_escalation")
	public static void offeringsKeepTheirProcessionStepThroughASave(GameTestHelper helper) {
		var provider = helper.getLevel().registryAccess();
		ActiveCardinalRite rite = ActiveCardinalRite.interactive(UUID.randomUUID(), helper.absolutePos(BlockPos.ZERO),
				Hemomancy.rloc("cardinal_rite/escalation_fixture"), 1200, 5, 4, false, 0, 4);
		rite.captureOfferingItinerary(List.of(new ActiveCardinalRite.RiteOffering(
				helper.absolutePos(new BlockPos(2, 0, 0)), new ItemStack(Items.BONE), true)));
		for (int i = 0; i < 4; i++) rite.fillAnchor(i, 50);
		CardinalRiteEscalation.update(rite);

		ActiveCardinalRite copy = ActiveCardinalRite.deserialize(rite.serialize(provider), provider);

		helper.assertTrue(copy.getEscalationStage() == 1, "the counted ring survives the save");
		helper.assertTrue(copy.getEscalationTotal() == 4, "the saved offering keeps its procession step");
		copy.enterInscription();
		copy.sealAltar(false);
		helper.assertTrue(CardinalRiteEscalation.update(copy) && copy.getEscalationStage() == 3,
				"after loading, the seal and the procession step together");
		helper.succeed();
	}
}
```

- [ ] **Step 2: Run the GameTests**

Run: `./gradlew.bat runGameTestServer`
Then: `grep -E "LogTestReporter.*failed at|required tests" build/phlegethontic-gametest/logs/latest.log`
Expected: no failure line mentions `offeringskeeptheirprocessionstepthroughasave`. The known pre-existing failure `blockitemexceptionskeeptheirowners` may still appear, along with a known flaky combat test (for example `idlemorphlingsinkstofloor` or `funeralbellscalesbankednecrosisandpayment`). Rerun once if any other test fails, to tell a flake from a regression.

- [ ] **Step 3: Update the reference doc**

In `docs/HEMOMANCY_REFERENCE.md`, after the paragraph that begins `**Rite beats on the boundary.**`, add:

```markdown
**The world stage.** A Harbinger rite also changes the world around it, one step per milestone: each anchor ring filled for the first time, each station circuit, the seal (or a puppeteer trial manifesting), the offering procession and culmination (`CardinalRiteEscalation`; succession steps with its five cues instead). The stage is saved with the rite, never falls when instability drains an anchor, and is synced with its total. Each step is announced by an `ESCALATION` beat and the `rite.escalate` sound. Clients ease toward the stage (`RiteWorldState`) and draw it in layers, with every threshold in `RiteWorldMath`:

- From the first step: the fog leans toward the floor's blood colour and closes in, the brazier trench fills, braziers grow and lean toward the Focus, and a drone starts.
- From a third of the way: veined membrane creeps past the floor's edge over the ground, and the floor's heartbeat is heard.
- From halfway: a vortex of cloud gathers high over the Focus, and the braziers burn sanguine. Storm rites throw lightning from it.
- From three quarters: blood rains inside the boundary and the trench spills over.
- At culmination: the sky wound opens into a red eye.

Strain shows in the world too: the fog closes further, braziers gutter, the creep bruises and pulls back, the wound's core stutters and the sound bed drops in pitch. A completion closes the wound in a flash and lets the creep sink; a collapse tears the creep back and brings the wound down in lightning; a cancellation fades everything. Nothing in the world is edited: it is all drawn on the client and gone when the rite ends.

**The rite panel.** The top-of-screen panel is off by default (client config `world_rendering.cardinalRiteHud`: `OFF` or `FULL`). With it off, the rite's owner gets one action-bar line when a phase begins (`hemomancy.rite.guide.*`) and one for each new problem (`hemomancy.rite.problem.*`: a sleeping required sigil, missing allies, the wrong medium, a broken sore, an owed sacrifice, a held finale). The server sends problems as translatable keys. During inscription, each required support socket whose sigil still sleeps shows a thin, pulsing blood thread.
```

- [ ] **Step 4: Update the wiki**

In `wiki/Harbinger-Path.md`, after the sentence `When the rite ends, the Staff returns to that hand.`, add:

```markdown
As a Cardinal Rite deepens, the world around it changes with each filled ring, the seal, the procession and culmination: blood-coloured fog, a filling trench, membrane creeping over the ground, a vortex gathering in the sky, blood rain, and a red eye opening overhead at the end. Strain shows as closing fog, guttering braziers and a bruised, receding creep. The top-of-screen rite panel is off by default; set `cardinalRiteHud` to `FULL` in the client config to bring it back.
```

- [ ] **Step 5: Update the audit status**

In `docs/CARDINAL_RITE_AUDIT.md`, after the line that begins `**Effects (§6.2):**`, add:

```markdown
**World stage (2026-10-13):** each milestone pushes the world around a rite a step further (fog, trench, braziers, creep, sky wound, blood rain, sound bed), and the top panel is off by default with owner-only action-bar guidance. Design: `docs/superpowers/specs/2026-10-13-cardinal-rite-world-escalation-design.md`.
```

- [ ] **Step 6: Full verification**

Run: `./gradlew.bat compileJava compileTestJava compileGameTestJava`
Expected: BUILD SUCCESSFUL.

Run: `./gradlew.bat test`
Expected: every new test passes. The only failures are the 16 pre-existing ones recorded in `docs/CARDINAL_RITE_AUDIT.md` (model generators, mojibake, missing lang/textures, legacy suite count).

Run the three shader checks from Tasks 7 and 9 again.
Expected: no errors.

- [ ] **Step 7: Checkpoint (no commit)**

Leave every change in the working tree. Report the test and GameTest results, and note that the visuals still need an in-game tuning pass.
