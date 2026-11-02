# Somatic Loom Witnessed Weave (Core) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the Somatic Loom's orb-dragging ritual with the witnessed weave at the base tier. Paying spins enzymes from vials onto the pirns. A Harbinger echo casts on loop above the warp, and hostile frayed threads tear loose and attack by tendency. The player survives or strikes them until they go slack, then hooks them home with a Living Staff. The weave ends in a climax that crystallizes the memory.

**Architecture:**
- **Server.** A pure `SomaticWeaveRules` owns every number and timing. `SomaticLoomBlockEntity` runs a tick-counted state machine (paid → weaving → climax). It persists threads and pause state and never fails a paid weave.
- **Threads.** Real server entities (`FrayedThreadEntity`, plus helper `ThreadBoltEntity` and `ThreadHazardEntity`) attack through `ThreadAttacks`.
- **Client.** Renders the threads with the existing knot/tether geometry, the vial and pirn spin-up, and a translucent echo built from the Mnemonist model, posed by the existing casting clips. The echo uses a school-gesture fallback until per-manipulation casts arrive (Plan 3).

**Tech Stack:** Java 21, NeoForge 1.21.1 (21.1.251), JUnit 5, NeoForge GameTests, Python 3 + Pillow + NumPy model generator (`tools/model_export`).

**Spec:** `docs/superpowers/specs/2026-10-11-somatic-loom-weave-design.md`

**Out of this plan** (separate plans to follow):
- Plan 2: loom tiers (station upgrade, stage models, rank gates, auto-catch).
- Plan 3: real echo casts for every manipulation.
- Plan 4: the Cortical Chamber dome.

Until Plan 2, the base loom weaves every rank, exactly as it does today.

## Global Constraints

- NeoForge imports only (`net.neoforged.*`), `DeferredHolder`, and `Hemomancy.rloc("snake_case_id")`; no `net.minecraftforge.*`.
- Networking uses `CustomPacketPayload` records with `TYPE` and `STREAM_CODEC`, registered in `PacketHandler.onRegisterPayloads`. No `SimpleChannel`.
- Visuals use Hemomancy/HutosLib effects (`HemoParticleData.glow/ember/darkGlow/bloodCell`, `ManipulationVisuals` cues), never vanilla `ParticleTypes.*`. Read `docs/MANIPULATION_VISUALS.md` before Task 6 and Task 8.
- Blood-side models carry no copper, brass, bronze or silver tones (hematic iron, bone, blood-wood, nerve, glass).
- **A paid weave can never dead-end.** Enzymes are taken into the pirns at payment. Death, logout, leaving the area and blackout only pause. Loose threads retreat into the echo, and woven threads and paid blood are kept.
- **Lethality:** HUMILIS and MEDIOCRITAS weaves floor the weaver at 4 health and black out; SUMMA, MAGISTER and PERFECTUS can kill.
- At most 2 threads are loose below SUMMA, and 3 at SUMMA and above.
- Match the file you edit:
  - `SomaticLoomBlockEntity.java` is tabs with `\r\r\n` line endings committed that way. Preserve them byte-for-byte; patch it with a script that maps `\n` to the file's separator, as in the scratch patch scripts used before.
  - Other loom classes use tabs and CRLF.
  - Generators are 4-space Python.
- Do not commit. The user commits; leave work in the working tree.
- Parallel sessions share `build/`. A mid-run `NoClassDefFoundError` or "bad class file" is usually a concurrent build: rerun before debugging.

## Review Focus

1. **A weave interrupted at every phase** (logout mid-fray, death with threads loose, loom broken mid-weave, server restart during climax) must resume or clean up, never orphan threads or eat the memory. Task 3 tests save/load mid-weave and orphan thread self-discard.
2. **Non-weaver damage sources near a low-rank weave** (a zombie, lava) must not be floored. Only thread-originated damage, and fire, freeze or poison applied by threads within the window, is clamped. Task 5 tests that a non-thread attacker is not floored.
3. **Two players near one loom:** only the weaver is hunted, the helper's staff hits still add strain, and the helper can hook a slack thread. Task 5 tests hook by a non-weaver.
4. **Saves from before this change** with `ritualOrbs` mid-weave migrate to threads instead of losing the weave. Task 3 has a migration GameTest.
5. **Facing:** the echo anchor, fray origin and tethers must match the model on all four facings. Task 3 unit-tests `SomaticLoomFrame` rotation against the blockstate convention.

---

## File Structure

| File | Responsibility |
|---|---|
| `common/tile/harbinger/crafting/SomaticWeaveRules.java` (create) | Pure numbers and timing: loop, fray, strain, slack, caps, lethality, floor, power, intervals |
| `common/block/harbinger/crafting/SomaticLoomFrame.java` (create) | Model-pixel to world conversion for a facing (anchors for server and client) |
| `common/block/harbinger/crafting/SomaticLoomGeometry.java` (move from `client/render/tile/harbinger/crafting/`) | Generated anchors; now in common so the server can read the echo anchor |
| `common/entity/weave/FrayedThreadEntity.java` (create) | Hostile thread: steering, windup and attack cadence, strain and slack, staff hits, hook |
| `common/entity/weave/ThreadBoltEntity.java` (create) | Tendency bolt projectile fired by threads; applies the on-hit effect |
| `common/entity/weave/ThreadHazardEntity.java` (create) | Lingering ground patch (Flammeus burn, Mortem miasma) |
| `common/entity/weave/ThreadAttacks.java` (create) | Per-tendency movement profile and attack execution |
| `common/entity/weave/WeaveDamageEvents.java` (create) | Non-lethal floor and blackout via `LivingIncomingDamageEvent` |
| `common/tile/harbinger/crafting/SomaticLoomBlockEntity.java` (modify) | Weave state machine replacing ritual orbs; spun enzymes; migration |
| `common/init/EntityInit.java` (modify) | Register the three entities and the thread's attributes |
| `client/render/entity/weave/*` (create) | Thread, bolt and hazard renderers |
| `client/render/world/ThreadKnotGeometry.java` (create) | Shared knot, frayed-tail and tether drawing, moved out of `SomaticLoomRenderer` |
| `client/render/tile/harbinger/crafting/SomaticLoomEcho.java` (create) | Echo caster: model, pose, fraying limbs, climax |
| `client/render/tile/harbinger/crafting/echo/EchoCast.java`, `EchoCasts.java`, `SchoolGestureEchoCast.java` (create) | Echo cast registry and fallback |
| `client/render/tile/harbinger/crafting/SomaticLoomRenderer.java` (modify) | Drop orb drawing; vials and spin-up; echo; weave-state warp and bands |
| `item/harbinger/tool/living/LivingStaffItem.java` (modify) | Remove the per-tick loom scan |
| `tools/model_export/somatic_loom.py` (modify) | Vials, filaments, liquid parts, echo anchor; geometry to common |
| gameTest: `SomaticLoomWeavingGameTests.java`, `LoomWeaveTestSupport.java` (create/modify), journey automation, clinical tests, client review | New flow and support helper |

Paths are under `src/main/java/com/vincenthuto/hemomancy/` unless noted.

---

### Task 1: SomaticWeaveRules

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/crafting/SomaticWeaveRules.java`
- Test: `src/test/java/com/vincenthuto/hemomancy/common/tile/harbinger/crafting/SomaticWeaveRulesTest.java`

**Interfaces:**
- Produces:
  - `SomaticWeaveRules.LOOP_TICKS` (80), `RELEASE_TICK` (40), `FRAY_TICK` (46), `CLIMAX_TICKS` (60), `BLACKOUT_TICKS` (100), `WINDUP_TICKS` (20), `RITUAL_RADIUS` (12.0), `NON_LETHAL_FLOOR` (4.0f), `NON_LETHAL_WINDOW_TICKS` (200).
  - `boolean frayDue(long weaveTicks)`
  - `int maxLoose(EnumManipulationRank)`, `boolean lethal(EnumManipulationRank)`
  - `float strainPerAttack(EnumManipulationRank)`, `float strainPerHit(EnumManipulationRank)`
  - `float attackPower(EnumManipulationRank)`, `int attackInterval(EnumManipulationRank)`
  - `int threadsFor(int requirement)`
  - `float flooredDamage(float health, float amount)`, `boolean blacksOut(float health, float amount)`
  - `boolean weaverPresent(boolean alive, boolean sameDimension, double distanceSqr)`

- [ ] **Step 1: Write the failing test**

```java
package com.vincenthuto.hemomancy.common.tile.harbinger.crafting;

import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import org.junit.jupiter.api.Test;

import static com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank.*;
import static org.junit.jupiter.api.Assertions.*;

class SomaticWeaveRulesTest {
	@Test
	void threadsTearLooseOncePerEchoLoopAfterTheRelease() {
		assertFalse(SomaticWeaveRules.frayDue(0));
		assertTrue(SomaticWeaveRules.frayDue(SomaticWeaveRules.FRAY_TICK));
		assertTrue(SomaticWeaveRules.frayDue(SomaticWeaveRules.FRAY_TICK + SomaticWeaveRules.LOOP_TICKS));
		assertFalse(SomaticWeaveRules.frayDue(SomaticWeaveRules.FRAY_TICK + 1));
		assertTrue(SomaticWeaveRules.FRAY_TICK > SomaticWeaveRules.RELEASE_TICK);
	}

	@Test
	void lowRanksHoldTwoThreadsAndCannotKill() {
		for (EnumManipulationRank r : new EnumManipulationRank[]{HUMILIS, MEDIOCRITAS}) {
			assertEquals(2, SomaticWeaveRules.maxLoose(r));
			assertFalse(SomaticWeaveRules.lethal(r));
		}
		for (EnumManipulationRank r : new EnumManipulationRank[]{SUMMA, MAGISTER, PERFECTUS}) {
			assertEquals(3, SomaticWeaveRules.maxLoose(r));
			assertTrue(SomaticWeaveRules.lethal(r));
		}
	}

	@Test
	void strikingStrainsFasterThanWaitingAndHigherRanksEndureMore() {
		for (EnumManipulationRank r : EnumManipulationRank.values()) {
			float perAttack = SomaticWeaveRules.strainPerAttack(r);
			float perHit = SomaticWeaveRules.strainPerHit(r);
			assertTrue(perHit > 0 && perHit < perAttack, r.name());
			assertTrue(perAttack <= 0.5f, "every thread attacks at least twice: " + r);
		}
		assertTrue(SomaticWeaveRules.strainPerAttack(PERFECTUS) < SomaticWeaveRules.strainPerAttack(HUMILIS));
		assertTrue(SomaticWeaveRules.attackPower(PERFECTUS) > SomaticWeaveRules.attackPower(HUMILIS));
		assertTrue(SomaticWeaveRules.attackInterval(PERFECTUS) < SomaticWeaveRules.attackInterval(HUMILIS));
		assertTrue(SomaticWeaveRules.attackInterval(PERFECTUS) >= 40);
	}

	@Test
	void eachEnzymeUnitIsOneThreadCappedPerTendency() {
		assertEquals(0, SomaticWeaveRules.threadsFor(0));
		assertEquals(4, SomaticWeaveRules.threadsFor(4));
		assertEquals(8, SomaticWeaveRules.threadsFor(20));
	}

	@Test
	void theFloorClampsDamageAndBlacksOut() {
		assertEquals(3f, SomaticWeaveRules.flooredDamage(10f, 3f), 1e-6);
		assertFalse(SomaticWeaveRules.blacksOut(10f, 3f));
		assertEquals(6f, SomaticWeaveRules.flooredDamage(10f, 9f), 1e-6);
		assertTrue(SomaticWeaveRules.blacksOut(10f, 9f));
		assertEquals(0f, SomaticWeaveRules.flooredDamage(3f, 5f), 1e-6);
		assertTrue(SomaticWeaveRules.blacksOut(3f, 5f));
	}

	@Test
	void theWeaverMustBeAliveHereAndClose() {
		double r2 = SomaticWeaveRules.RITUAL_RADIUS * SomaticWeaveRules.RITUAL_RADIUS;
		assertTrue(SomaticWeaveRules.weaverPresent(true, true, r2 - 1));
		assertFalse(SomaticWeaveRules.weaverPresent(true, true, r2 + 1));
		assertFalse(SomaticWeaveRules.weaverPresent(false, true, 1));
		assertFalse(SomaticWeaveRules.weaverPresent(true, false, 1));
	}
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew.bat test --tests "*SomaticWeaveRulesTest" --console=plain`
Expected: compilation failure, `SomaticWeaveRules` not found.

- [ ] **Step 3: Write the implementation**

```java
package com.vincenthuto.hemomancy.common.tile.harbinger.crafting;

import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;

/**
 * Numbers and timing for the Somatic Loom's witnessed weave. Counted in weave ticks, which stop while the
 * weave is paused, so a pause never shifts a thread's place in the echo's loop.
 */
public final class SomaticWeaveRules {
	/** One echo cast: opening, hold, release, recovery, then a breath before the next. */
	public static final int LOOP_TICKS = 80;
	public static final int RELEASE_TICK = 40;
	/** A thread tears loose just after the release, when the echo is most spent. */
	public static final int FRAY_TICK = 46;
	public static final int CLIMAX_TICKS = 60;
	public static final int BLACKOUT_TICKS = 100;
	public static final int WINDUP_TICKS = 20;
	public static final double RITUAL_RADIUS = 12.0D;
	public static final float NON_LETHAL_FLOOR = 4.0F;
	/** How long after a thread's attack its lingering fire, frost or poison still counts as the weave's. */
	public static final int NON_LETHAL_WINDOW_TICKS = 200;
	private static final int MAX_THREADS_PER_TENDENCY = 8;

	private SomaticWeaveRules() {
	}

	public static boolean frayDue(long weaveTicks) {
		return weaveTicks % LOOP_TICKS == FRAY_TICK;
	}

	public static int maxLoose(EnumManipulationRank rank) {
		return lethal(rank) ? 3 : 2;
	}

	public static boolean lethal(EnumManipulationRank rank) {
		return rank.ordinal() >= EnumManipulationRank.SUMMA.ordinal();
	}

	/** Attacks a thread makes before it goes slack on its own. */
	private static int attacksToSlack(EnumManipulationRank rank) {
		return switch (rank) {
			case HUMILIS -> 2;
			case MEDIOCRITAS, SUMMA -> 3;
			case MAGISTER -> 4;
			case PERFECTUS -> 5;
		};
	}

	public static float strainPerAttack(EnumManipulationRank rank) {
		return 1.0F / attacksToSlack(rank);
	}

	/** A staff hit strains a thread half as much as its own attack, but hits come far faster than attacks. */
	public static float strainPerHit(EnumManipulationRank rank) {
		return 0.5F / attacksToSlack(rank);
	}

	public static float attackPower(EnumManipulationRank rank) {
		return switch (rank) {
			case HUMILIS -> 1.0F;
			case MEDIOCRITAS -> 1.25F;
			case SUMMA -> 1.6F;
			case MAGISTER -> 2.0F;
			case PERFECTUS -> 2.5F;
		};
	}

	public static int attackInterval(EnumManipulationRank rank) {
		return Math.max(40, 70 - 6 * rank.ordinal());
	}

	public static int threadsFor(int requirement) {
		return Math.max(0, Math.min(MAX_THREADS_PER_TENDENCY, requirement));
	}

	/** Damage a non-lethal weave lets through: never past the floor. */
	public static float flooredDamage(float health, float amount) {
		return Math.max(0.0F, Math.min(amount, health - NON_LETHAL_FLOOR));
	}

	public static boolean blacksOut(float health, float amount) {
		return health - amount <= NON_LETHAL_FLOOR;
	}

	public static boolean weaverPresent(boolean alive, boolean sameDimension, double distanceSqr) {
		return alive && sameDimension && distanceSqr <= RITUAL_RADIUS * RITUAL_RADIUS;
	}
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew.bat test --tests "*SomaticWeaveRulesTest" --console=plain`
Then: `grep -o 'tests="[0-9]*" skipped="[0-9]*" failures="[0-9]*"' build/test-results/test/TEST-com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticWeaveRulesTest.xml`
Expected: `tests="6" skipped="0" failures="0"`.

---

### Task 2: Model vials, echo anchor and shared geometry

**Files:**
- Modify: `tools/model_export/somatic_loom.py`
- Move: `src/main/java/com/vincenthuto/hemomancy/client/render/tile/harbinger/crafting/SomaticLoomGeometry.java` → `src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/crafting/SomaticLoomGeometry.java` (regenerated)
- Create: `src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/crafting/SomaticLoomFrame.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/render/tile/harbinger/crafting/SomaticLoomRenderer.java` (static import path only)
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/crafting/SomaticLoomBlock.java` (pirn-shelf outline box y0 4.6 → 4.2)
- Generated: `models/block/somatic_loom.json`, `somatic_loom_glass.json` (new), `somatic_loom_vial_<tendency>.json` ×8 (new), item model, blockstate (now two-layer), bbmodel
- Test: `src/test/java/com/vincenthuto/hemomancy/common/block/harbinger/crafting/SomaticLoomFrameTest.java`; modify `ModelFirstTileRendererResourceTest.java`

**Interfaces:**
- Produces:
  - `SomaticLoomGeometry`, in package `common.block.harbinger.crafting`, with every existing constant plus `VIAL_FLOOR`, `VIAL_DEPTH`, `ECHO_X`, `ECHO_Y`, `ECHO_Z`. `PIRNS` doubles as the vial (x, z).
  - `SomaticLoomFrame.toWorld(BlockPos pos, Direction facing, double x, double y, double z) -> Vec3` (block units, world space).
  - Part models `hemomancy:block/somatic_loom_vial_<animus|flammeus|ductilis|lux|mortem|congeatio|ferric|tenebris>`: one pixel of liquid on `VIAL_FLOOR`.

- [ ] **Step 1: Write the failing frame test**

```java
package com.vincenthuto.hemomancy.common.block.harbinger.crafting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SomaticLoomFrameTest {
	private static void assertVec(double x, double y, double z, Vec3 v) {
		assertEquals(x, v.x, 1e-9);
		assertEquals(y, v.y, 1e-9);
		assertEquals(z, v.z, 1e-9);
	}

	@Test
	void northFacingIsTheModelFrame() {
		assertVec(10.5, 21.0, 30.25, SomaticLoomFrame.toWorld(new BlockPos(10, 20, 30), Direction.NORTH, 8, 16, 4));
	}

	/** Matches the blockstate's y rotation (east y90) and SarcophagusGeometry.point. */
	@Test
	void eastFacingTurnsTheFrontToFaceEast() {
		// The front of the model (z = 0) faces the player; facing east, the front is at x = 16.
		assertVec(1.0, 0.0, 0.5, SomaticLoomFrame.toWorld(BlockPos.ZERO, Direction.EAST, 8, 0, 0));
		assertVec(0.5, 0.0, 0.5, SomaticLoomFrame.toWorld(BlockPos.ZERO, Direction.SOUTH, 8, 0, 8));
		assertVec(0.0, 0.0, 0.5, SomaticLoomFrame.toWorld(BlockPos.ZERO, Direction.WEST, 8, 0, 16));
	}
}
```

Hand-check the EAST case with `SarcophagusGeometry.point`: one turn maps (x, z) = (8, 0) to (16 − 0, 8) = (16, 8), which is (1.0, 0.5) in blocks. The SOUTH case is two turns: (8, 8) to (8, 8) to (8, 8). The WEST case is three turns: (8, 16) to (0, 8) to (8, 0) to (16, 8). That gives (1.0, 0.5), not (0.0, 0.5). **Correct the WEST expectation to `assertVec(1.0, 0.0, 0.5, ...)` for input (8, 0, 16)** before running. The point of the test is that the output equals `SarcophagusGeometry.point` for every facing. Write it as:

```java
	@Test
	void everyFacingMatchesTheSarcophagusConvention() {
		for (Direction facing : Direction.Plane.HORIZONTAL) {
			double[] expected = com.vincenthuto.hemomancy.common.block.harbinger.puzzle.SarcophagusGeometry
					.point(facing, 3, 5, 11);
			assertVec(expected[0], expected[1], expected[2], SomaticLoomFrame.toWorld(BlockPos.ZERO, facing, 3, 5, 11));
		}
	}
```

Keep `northFacingIsTheModelFrame` and `everyFacingMatchesTheSarcophagusConvention`, and **drop** `eastFacingTurnsTheFrontToFaceEast`. The convention test is the authority.

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew.bat test --tests "*SomaticLoomFrameTest" --console=plain`
Expected: compile failure, `SomaticLoomFrame` not found.

- [ ] **Step 3: Write `SomaticLoomFrame`**

```java
package com.vincenthuto.hemomancy.common.block.harbinger.crafting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

/** Model pixels of the north-facing loom to world positions, turned the way the blockstate turns the model. */
public final class SomaticLoomFrame {
	private SomaticLoomFrame() {
	}

	public static Vec3 toWorld(BlockPos pos, Direction facing, double x, double y, double z) {
		for (int turn = 0; turn < turns(facing); turn++) {
			double nextX = 16 - z;
			z = x;
			x = nextX;
		}
		return new Vec3(pos.getX() + x / 16, pos.getY() + y / 16, pos.getZ() + z / 16);
	}

	private static int turns(Direction facing) {
		return switch (facing) {
			case EAST -> 1;
			case SOUTH -> 2;
			case WEST -> 3;
			default -> 0;
		};
	}
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew.bat test --tests "*SomaticLoomFrameTest" --console=plain`
Expected: 2 tests, 0 failures.

- [ ] **Step 5: Add vials, liquid parts and the echo anchor to the generator**

In `tools/model_export/somatic_loom.py`:

(a) Add textures (vanilla stained glass as tinted liquid) and the vial constants:

```python
# in TEXTURES:
    'vialglass': 'hemomancy:block/vial_glass',
    'liq_animus': 'minecraft:block/red_stained_glass',
    'liq_flammeus': 'minecraft:block/orange_stained_glass',
    'liq_ductilis': 'minecraft:block/yellow_stained_glass',
    'liq_lux': 'minecraft:block/white_stained_glass',
    'liq_mortem': 'minecraft:block/green_stained_glass',
    'liq_congeatio': 'minecraft:block/blue_stained_glass',
    'liq_ferric': 'minecraft:block/gray_stained_glass',
    'liq_tenebris': 'minecraft:block/purple_stained_glass',
```

```python
TRANSLUCENT = {'vialglass'} | {'liq_' + t for t in TENDENCIES}
# Enzyme vials hang under the pirn shelf; the stock stands as liquid, a filament rises to the pirn.
VIAL_FLOOR, VIAL_DEPTH, VIAL_RADIUS = 4.6, 2.0, .55
VIAL_TOP = 6.8
# The echo stands on the warp between the reed and the heddles.
ECHO = (8.0, 15.0, 12.5)
```

`TENDENCIES` must be defined above `TRANSLUCENT`; move the `TENDENCIES` tuple up if needed. Construct `Model(TRANSLUCENT, CAPS)` everywhere `Model(caps=CAPS)` is used.

(b) In `pirns(m)`, after each pirn's spindle, add the vial:

```python
            m.shell('vial_glass', (x, z), VIAL_RADIUS + .12, VIAL_FLOOR - .15, VIAL_TOP, .1, 'vialglass', ends=True)
            m.octo('vial_cradle', 'y', (x, 0, z), VIAL_RADIUS + .32, VIAL_FLOOR + .9, VIAL_FLOOR + 1.15, 'iron',
                   faces=('north', 'south', 'east', 'west', 'up', 'down'))
            m.box('vial_hanger', (x - .1, VIAL_TOP, z - .1), (x + .1, 7.0, z + .1), 'iron')
            m.box('filament', (x - .05, VIAL_FLOOR + .2, z + .2), (x + .05, PIRN_FLOOR, z + .3), tendency)
```

Set `tendency = TENDENCIES[n * 4 + k]` in the loop. The filament runs up the inside of the glass beside the hanger into the pirn flange.

(c) Add the liquid part:

```python
def vial_liquid(tendency):
    """Enzyme liquid in one vial, one pixel deep; the renderer stretches it by stored stock."""
    m = Model(TRANSLUCENT, CAPS)
    x, z = PIRNS[TENDENCIES.index(tendency)]
    m.octo('vial_' + tendency, 'y', (x, 0, z), VIAL_RADIUS, VIAL_FLOOR, VIAL_FLOOR + 1, 'liq_' + tendency)
    return m
```

(d) In `export()`:
- split the body into cutout and glass (`solid, glass = kit.split_glass(body)`);
- write `somatic_loom.json` from `solid` and `somatic_loom_glass.json` from `glass`;
- write each `somatic_loom_vial_<t>.json`;
- write the blockstate with `kit.two_layer_blockstate(NAME)`;
- make the item model `body` + full cops + full liquids (`vial_liquid` with height `VIAL_DEPTH`; add an optional `height=1.0` parameter mirroring `cop`);
- add `('vial_' + t, vial_liquid(t))` groups to the bbmodel.

Keep `kit.separate([body], TEXTURES, TRANSLUCENT, 'Somatic Loom')`.

(e) In `geometry_java()`:
- change `GEOMETRY` to `kit.ROOT / 'src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/crafting/SomaticLoomGeometry.java'`;
- set the package line to `package com.vincenthuto.hemomancy.common.block.harbinger.crafting;`;
- append:

```python
    /** Enzyme vials share the pirn (x, z); liquid stands from VIAL_FLOOR up to VIAL_DEPTH pixels at full stock. */
    public static final float VIAL_FLOOR = {fmt(VIAL_FLOOR)}, VIAL_DEPTH = {fmt(VIAL_DEPTH)};
    /** Where the echo's feet stand on the warp. */
    public static final float ECHO_X = {fmt(ECHO[0])}, ECHO_Y = {fmt(ECHO[1])}, ECHO_Z = {fmt(ECHO[2])};
```

Delete the old client-package file after regenerating:

```bash
rm src/main/java/com/vincenthuto/hemomancy/client/render/tile/harbinger/crafting/SomaticLoomGeometry.java
```

In `SomaticLoomRenderer.java`, change `import static com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.SomaticLoomGeometry.*;` to `import static com.vincenthuto.hemomancy.common.block.harbinger.crafting.SomaticLoomGeometry.*;`.

(f) Update the doc string to mention the vials and the glass layer.

- [ ] **Step 6: Export, check fights and determinism, preview**

```bash
cd tools/model_export && python somatic_loom.py --preview && python coplanar.py --together ../../src/main/resources/assets/hemomancy/models/block/somatic_loom.json ../../src/main/resources/assets/hemomancy/models/block/somatic_loom_glass.json ../../src/main/resources/assets/hemomancy/models/block/somatic_loom_cop_*.json ../../src/main/resources/assets/hemomancy/models/block/somatic_loom_vial_*.json && sha1sum ../../src/main/resources/assets/hemomancy/models/block/somatic_loom*.json > /tmp/a && python somatic_loom.py && sha1sum ../../src/main/resources/assets/hemomancy/models/block/somatic_loom*.json | diff - /tmp/a && echo deterministic
```

Expected: `combined: 0 fights` and `deterministic`. Open `build/somatic_loom_preview.png`: the vials should hang under each pirn shelf with the filament visible. If fights appear between the filament and the glass, nudge the filament z by 0.03.

- [ ] **Step 7: Update the resource contract**

In `ModelFirstTileRendererResourceTest.main`, in the loom block, add:

```java
		assertContains("somatic loom vials bake into a translucent glass layer",
				read(RESOURCE_ROOT.resolve("assets/hemomancy/models/block/somatic_loom_glass.json")),
				"\"render_type\": \"translucent\"");
		assertContains("somatic loom blockstate applies its glass layer",
				read(RESOURCE_ROOT.resolve("assets/hemomancy/blockstates/somatic_loom.json")),
				"\"hemomancy:block/somatic_loom_glass\"");
		for (String tendency : new String[]{"animus", "flammeus", "ductilis", "lux", "mortem", "congeatio", "ferric",
				"tenebris"})
			assertContains("somatic loom vial liquid part for " + tendency,
					read(RESOURCE_ROOT.resolve("assets/hemomancy/models/block/somatic_loom_vial_" + tendency + ".json")),
					"\"name\": \"vial_" + tendency + "\"");
		assertContains("somatic loom anchors live where the server can read them",
				read(SOURCE_ROOT.resolve("com/vincenthuto/hemomancy/common/block/harbinger/crafting/SomaticLoomGeometry.java")),
				"ECHO_Z");
```

Change the existing `SomaticLoomGeometry.java` path assertion to the common package. In `SomaticLoomBlock`, change the pirn-shelf outline boxes' y0 from `4.6` to `4.2` so they cover the vials.

- [ ] **Step 8: Compile and run the contracts**

```powershell
./gradlew.bat compileJava compileTestJava --console=plain -q
$gson = "$env:USERPROFILE\.gradle\caches\modules-2\files-2.1\com.google.code.gson\gson\2.10\dd9b193aef96e973d5a11ab13cd17430c2e4306b\gson-2.10.jar"
java -cp "build/classes/java/test;$gson" com.vincenthuto.hemomancy.common.block.ModelFirstTileRendererResourceTest
java -cp "build/classes/java/test;$gson" com.vincenthuto.hemomancy.common.block.WaterloggableDecorativeBlocksTest
```

Expected: both exit 0.

---

### Task 3: Weave state machine, thread entity skeleton and orb removal

This is the breaking swap. The orbs, the fixed-depth drag and the staff scan are replaced. Threads exist, hover, take strain, go slack and can be hooked, but do not attack yet (Task 4). All callers and tests move to the new API in this task.

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/entity/weave/FrayedThreadEntity.java`
- Modify: `common/init/EntityInit.java` (type and attributes), `client/event/ClientEvents.java` (temporary renderer: `NoopRenderer` until Task 6)
- Modify: `common/tile/harbinger/crafting/SomaticLoomBlockEntity.java` (state machine; remove `RitualOrb` and every orb method listed below)
- Modify: `common/block/harbinger/crafting/SomaticLoomBlock.java` (`isWeavingOrbs` → `isWeaving`)
- Modify: `item/harbinger/tool/living/LivingStaffItem.java` (delete `dragNearestLoomOrb` and its call at :166-168)
- Modify: `client/render/tile/harbinger/crafting/SomaticLoomRenderer.java`:
  - delete `drawRitualOrbs`, `drawTether`, `drawFrayedTails`, `drawKnot`, `disc`, `arc`, `onPlane`, `ribbonSegment`, `quad` and `orbSeed` (they move to `ThreadKnotGeometry` in Task 6);
  - read thread states for the warp and the bands;
  - replace `isWeavingOrbs` with `isWeaving`.
- Create: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/LoomWeaveTestSupport.java`
- Modify: `src/gameTest/.../journey/HarbingerJourneyAutomation.java` (`weaveMemory`, 507-532), `ClinicalBloodProgressionGameTests.java` (3655-3680 drag loop; 3716 feedback text), `HarbingerRepairGameTests.java` (1109-1123 `isWeavingOrbs`), `LoomClientReview.java` (`completeOrbs`), `SomaticLoomWeavingGameTests.java` (rewrite)
- Modify tests: `MemoryWeavingRedesignSourceTest.java`, `ModelFirstTileRendererResourceTest.java`

**Interfaces:**
- Consumes: `SomaticWeaveRules` (Task 1), `SomaticLoomFrame` and `SomaticLoomGeometry.ECHO_*` (Task 2).
- Produces:
  - `SomaticLoomBlockEntity.PHASE_WEAVING` (2, renamed from `PHASE_WEAVING_ORBS`, same value) and `PHASE_CLIMAX` (3).
  - `boolean isWeaving()` (phase 2 or 3), `boolean isInClimax()`.
  - `List<WeaveThread> getWeaveThreads()`, where `WeaveThread` has `tendency()`, `state()` and `entity()`, and `WeaveThread.State` is `{HELD, LOOSE, WOVEN}`.
  - `@Nullable UUID getWeaver()`, `long getWeaveTicks()`, `int getClimaxTicks()`, `boolean isPaused()`.
  - `int getSpun(EnumBloodTendency)`, `EnumManipulationRank getWeaveRank()`, `@Nullable BloodManipulation getWeaveManipulation()`.
  - `Vec3 echoAnchor()` (world).
  - `boolean ownsLooseThread(UUID)`, `boolean weaveHome(FrayedThreadEntity thread, Player by)`, `void blackout()`, `@Nullable ServerPlayer weaverPlayer(ServerLevel)`, `List<FrayedThreadEntity> looseThreadEntities(ServerLevel)`.
  - `FrayedThreadEntity`:
    - constructor `(EntityType<? extends FrayedThreadEntity>, Level)`;
    - `static FrayedThreadEntity spawn(ServerLevel, SomaticLoomBlockEntity loom, EnumBloodTendency, EnumManipulationRank)`;
    - `EnumBloodTendency tendency()`, `EnumManipulationRank rank()`, `BlockPos loomPos()`;
    - `float strain()`, `boolean isSlack()`, `void addStrain(float)`, `boolean tryHook(Player)`;
    - `int windup()`, `boolean isFaded()`.
  - `EntityInit.frayed_thread`.
  - `LoomWeaveTestSupport.weaveAll(ServerLevel, BlockPos, SomaticLoomBlockEntity, Player) -> boolean` (ticks the loom, strains and hooks every thread, runs the climax).

- [ ] **Step 1: Rewrite the weaving GameTests as the failing spec**

Replace `src/gameTest/java/com/vincenthuto/hemomancy/gametest/SomaticLoomWeavingGameTests.java` with the following. It keeps the no-dead-end test, adapted to the new flow, and adds lifecycle, pause, persistence, migration and orphan tests.

```java
package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.crafting.SomaticLoomBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.entity.weave.FrayedThreadEntity;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticLoomBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticLoomBlockEntity.WeaveThread;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticWeaveRules;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder("somatic_loom_validation")
@PrefixGameTestTemplate(false)
public final class SomaticLoomWeavingGameTests {
    private static final String AXONAL = "memory_weaving/memory_axonal_transduction";

    /** A loom stocked for Axonal Transduction (4 Ductilis, 600 mL, ganglion catalyst) and a weaver beside it. */
    private record Setup(BlockPos pos, SomaticLoomBlockEntity loom, FakePlayer weaver) {}

    private static Setup setup(GameTestHelper helper, int ductilis) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        var loom = (SomaticLoomBlock) BlockInit.somatic_loom.get();
        level.setBlock(pos, loom.defaultBlockState(), 3);
        var te = (SomaticLoomBlockEntity) level.getBlockEntity(pos);
        te.addItem(null, new ItemStack(ItemInit.hematic_memory.get()), null);
        te.addItem(null, new ItemStack(ItemInit.naeglerophaeon_ganglion.get()), null);
        CompoundTag tag = te.saveWithoutMetadata(level.registryAccess());
        CompoundTag stock = new CompoundTag();
        stock.putInt(EnumBloodTendency.DUCTILIS.name(), ductilis);
        tag.put("storedEnzymes", stock);
        te.loadWithComponents(tag, level.registryAccess());
        helper.assertTrue(te.selectRecipe(null, Hemomancy.rloc(AXONAL)), "Axonal Transduction should be selectable");
        var weaver = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "loom_weaver"));
        weaver.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() - 2.5);
        HemoCapabilityAccess.getBloodVolume(weaver).ifPresent(volume -> {
            volume.setActive(true);
            volume.setBloodVolume(volume.getMaxBloodVolume());
        });
        return new Setup(pos, te, weaver);
    }

    private static void pay(GameTestHelper helper, Setup s) {
        helper.assertTrue(s.loom.tryChargeRitualBlood(s.weaver, 600, false) && s.loom.isWeaving(),
                "Paying the full blood cost should begin the weave");
    }

    private static void tick(GameTestHelper helper, Setup s, int ticks) {
        for (int i = 0; i < ticks; i++)
            SomaticLoomBlockEntity.serverTick(helper.getLevel(), s.pos, helper.getLevel().getBlockState(s.pos), s.loom);
    }

    private static long count(Setup s, WeaveThread.State state) {
        return s.loom.getWeaveThreads().stream().filter(t -> t.state() == state).count();
    }

    @GameTest(template = "empty")
    public static void payingSpinsTheEnzymesIntoThePirns(GameTestHelper helper) {
        Setup s = setup(helper, 6);
        pay(helper, s);
        helper.assertTrue(s.loom.getStoredEnzyme(EnumBloodTendency.DUCTILIS) == 2, "Payment draws exactly the recipe's 4");
        helper.assertTrue(s.loom.getSpun(EnumBloodTendency.DUCTILIS) == 4, "The drawn enzyme is wound onto the pirn");
        helper.assertTrue(s.loom.getWeaveThreads().size() == 4 && count(s, WeaveThread.State.HELD) == 4,
                "One held thread per enzyme");
        s.weaver.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void threadsTearLooseOnTheEchoLoopUpToTheCap(GameTestHelper helper) {
        Setup s = setup(helper, 4);
        pay(helper, s);
        tick(helper, s, SomaticWeaveRules.FRAY_TICK + 1);
        helper.assertTrue(count(s, WeaveThread.State.LOOSE) == 1, "One thread frays per loop");
        tick(helper, s, SomaticWeaveRules.LOOP_TICKS * 5);
        // Axonal Transduction is PERFECTUS: three loose at most.
        helper.assertTrue(count(s, WeaveThread.State.LOOSE) == 3, "Loose threads stop at the rank's cap");
        helper.assertTrue(s.loom.looseThreadEntities(helper.getLevel()).size() == 3, "Every loose thread is a live entity");
        s.weaver.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void slackThreadsHookHomeAndTheClimaxCrystallizes(GameTestHelper helper) {
        Setup s = setup(helper, 4);
        pay(helper, s);
        helper.assertTrue(LoomWeaveTestSupport.weaveAll(helper.getLevel(), s.pos, s.loom, s.weaver),
                "The weave should finish");
        helper.assertFalse(s.loom.isWeaving(), "The loom returns to idle after the climax");
        helper.assertTrue(s.loom.getSpun(EnumBloodTendency.DUCTILIS) == 0, "Every spun enzyme is woven in");
        boolean delivered = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(s.pos).inflate(3)).stream()
                .anyMatch(item -> item.getItem().is(ItemInit.memory_axonal_transduction.get()));
        helper.assertTrue(delivered, "The finished memory should crystallize");
        s.weaver.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void tautThreadsRefuseTheHook(GameTestHelper helper) {
        Setup s = setup(helper, 4);
        pay(helper, s);
        tick(helper, s, SomaticWeaveRules.FRAY_TICK + 1);
        FrayedThreadEntity thread = s.loom.looseThreadEntities(helper.getLevel()).getFirst();
        helper.assertFalse(thread.tryHook(s.weaver), "A taut thread cannot be woven");
        thread.addStrain(1f);
        helper.assertTrue(thread.isSlack() && thread.tryHook(s.weaver), "A slack thread hooks home");
        helper.assertTrue(count(s, WeaveThread.State.WOVEN) == 1, "The hooked thread is woven");
        s.weaver.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void leavingTheAreaPausesAndRecallsLooseThreads(GameTestHelper helper) {
        Setup s = setup(helper, 4);
        pay(helper, s);
        tick(helper, s, SomaticWeaveRules.FRAY_TICK + 1);
        long before = s.loom.getWeaveTicks();
        s.weaver.moveTo(s.pos.getX() + 40, s.pos.getY(), s.pos.getZ());
        tick(helper, s, 5);
        helper.assertTrue(s.loom.isPaused() && count(s, WeaveThread.State.LOOSE) == 0
                && s.loom.looseThreadEntities(helper.getLevel()).isEmpty(), "Loose threads retreat into the echo");
        helper.assertTrue(s.loom.getWeaveTicks() == before, "A paused weave does not advance its loop");
        s.weaver.moveTo(s.pos.getX() + 0.5, s.pos.getY(), s.pos.getZ() - 2.5);
        tick(helper, s, SomaticWeaveRules.LOOP_TICKS);
        helper.assertTrue(!s.loom.isPaused() && count(s, WeaveThread.State.LOOSE) >= 1, "Returning resumes the weave");
        s.weaver.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void aMidWeaveSaveRestoresWovenAndHeldThreads(GameTestHelper helper) {
        Setup s = setup(helper, 4);
        pay(helper, s);
        tick(helper, s, SomaticWeaveRules.FRAY_TICK + 1);
        FrayedThreadEntity thread = s.loom.looseThreadEntities(helper.getLevel()).getFirst();
        thread.addStrain(1f);
        thread.tryHook(s.weaver);
        CompoundTag saved = s.loom.saveWithoutMetadata(helper.getLevel().registryAccess());
        s.loom.loadWithComponents(saved, helper.getLevel().registryAccess());
        helper.assertTrue(s.loom.isWeaving() && count(s, WeaveThread.State.WOVEN) == 1
                && s.loom.getWeaver().equals(s.weaver.getUUID()), "Woven threads and the weaver survive a reload");
        s.weaver.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void preWitnessedSavesMigrateTheirOrbsToThreads(GameTestHelper helper) {
        Setup s = setup(helper, 4);
        CompoundTag tag = s.loom.saveWithoutMetadata(helper.getLevel().registryAccess());
        ListTag orbs = new ListTag();
        for (int i = 0; i < 4; i++) {
            CompoundTag orb = new CompoundTag();
            orb.putString("tendency", EnumBloodTendency.DUCTILIS.name());
            orb.putInt("enzymeCost", 1);
            orb.putBoolean("completed", i == 0);
            orbs.add(orb);
        }
        tag.put("ritualOrbs", orbs);
        tag.putInt("craftPhase", SomaticLoomBlockEntity.PHASE_WEAVING);
        tag.putDouble("ritualBloodCharged", 600);
        tag.remove("weaveThreads");
        s.loom.loadWithComponents(tag, helper.getLevel().registryAccess());
        helper.assertTrue(s.loom.isWeaving() && count(s, WeaveThread.State.WOVEN) == 1
                && count(s, WeaveThread.State.HELD) == 3, "Completed orbs become woven threads, the rest held");
        helper.assertTrue(s.loom.getSpun(EnumBloodTendency.DUCTILIS) == 3, "Unwoven orbs keep their enzyme on the pirn");
        tick(helper, s, SomaticWeaveRules.FRAY_TICK + 1);
        helper.assertTrue(s.weaver.getUUID().equals(s.loom.getWeaver()), "The nearest player takes up an orphaned weave");
        s.weaver.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void orphanedThreadsUnravelWhenTheLoomIsGone(GameTestHelper helper) {
        Setup s = setup(helper, 4);
        pay(helper, s);
        tick(helper, s, SomaticWeaveRules.FRAY_TICK + 1);
        FrayedThreadEntity thread = s.loom.looseThreadEntities(helper.getLevel()).getFirst();
        helper.getLevel().removeBlock(s.pos, false);
        thread.tick();
        helper.assertTrue(thread.isRemoved(), "A thread without its loom unravels");
        s.weaver.discard();
        helper.succeed();
    }
}
```

- [ ] **Step 2: Run the suite to verify it fails**

Run: `./gradlew.bat runSomaticLoomGameTestServer --console=plain`
Expected: compile failure (`FrayedThreadEntity`, `WeaveThread`, `isWeaving`, `LoomWeaveTestSupport` missing).

- [ ] **Step 3: Register the entity**

In `EntityInit.java`, next to the other summons:

```java
	public static final DeferredHolder<EntityType<?>, EntityType<FrayedThreadEntity>> frayed_thread = ENTITY_TYPES.register(
			"frayed_thread",
			() -> EntityType.Builder.of(FrayedThreadEntity::new, MobCategory.MISC)
					.sized(0.4F, 0.4F).clientTrackingRange(10).updateInterval(1)
					.build(Hemomancy.rloc("frayed_thread").toString()));
```

In `onAttributeCreate`, add `event.put(EntityInit.frayed_thread.get(), FrayedThreadEntity.setAttributes().build());`. In `ClientEvents.renderEntities`, add `event.registerEntityRenderer(EntityInit.frayed_thread.get(), net.minecraft.client.renderer.entity.NoopRenderer::new);` (replaced in Task 6). Add `"entity.hemomancy.frayed_thread": "Frayed Thread"` to `src/main/resources/assets/hemomancy/lang/en_us.json`.

- [ ] **Step 4: Write `FrayedThreadEntity`** (no attacks yet; Task 4 fills `ThreadAttacks`)

```java
package com.vincenthuto.hemomancy.common.entity.weave;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffItem;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticLoomBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticWeaveRules;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * A thread torn loose from a Somatic Loom's echo. It hunts the weaver and attacks with its tendency's kit; each
 * attack and each staff hit strains it until it goes slack, when a Living Staff can hook it home. It never dies:
 * damage only strains it. Without its loom, or once its loom no longer counts it loose, it unravels.
 */
public class FrayedThreadEntity extends PathfinderMob {
	private static final EntityDataAccessor<Byte> TENDENCY = SynchedEntityData.defineId(FrayedThreadEntity.class, EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Byte> RANK = SynchedEntityData.defineId(FrayedThreadEntity.class, EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Float> STRAIN = SynchedEntityData.defineId(FrayedThreadEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Integer> WINDUP = SynchedEntityData.defineId(FrayedThreadEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> FADED = SynchedEntityData.defineId(FrayedThreadEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<BlockPos> LOOM = SynchedEntityData.defineId(FrayedThreadEntity.class, EntityDataSerializers.BLOCK_POS);
	private int cooldown;

	public FrayedThreadEntity(EntityType<? extends FrayedThreadEntity> type, Level level) {
		super(type, level);
		setNoGravity(true);
		noPhysics = true;
	}

	public static AttributeSupplier.Builder setAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 20.0D).add(Attributes.FOLLOW_RANGE, 24.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.3D);
	}

	public static FrayedThreadEntity spawn(ServerLevel level, SomaticLoomBlockEntity loom, EnumBloodTendency tendency,
			EnumManipulationRank rank) {
		FrayedThreadEntity thread = new FrayedThreadEntity(EntityInit.frayed_thread.get(), level);
		thread.entityData.set(TENDENCY, (byte) tendency.ordinal());
		thread.entityData.set(RANK, (byte) rank.ordinal());
		thread.entityData.set(LOOM, loom.getBlockPos());
		Vec3 at = loom.echoAnchor();
		thread.moveTo(at.x, at.y + 0.5D, at.z, level.random.nextFloat() * 360F, 0F);
		thread.cooldown = SomaticWeaveRules.attackInterval(rank) / 2;
		level.addFreshEntity(thread);
		level.sendParticles(HemoParticleData.glow(tendency.getColor()), at.x, at.y + 0.5D, at.z, 14, 0.15D, 0.15D, 0.15D, 0.04D);
		level.playSound(null, at.x, at.y, at.z, SoundEvents.WOOL_BREAK, SoundSource.BLOCKS, 0.9F, 0.6F);
		return thread;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(TENDENCY, (byte) 0);
		builder.define(RANK, (byte) 0);
		builder.define(STRAIN, 0.0F);
		builder.define(WINDUP, 0);
		builder.define(FADED, 0);
		builder.define(LOOM, BlockPos.ZERO);
	}

	public EnumBloodTendency tendency() { return EnumBloodTendency.values()[entityData.get(TENDENCY)]; }
	public EnumManipulationRank rank() { return EnumManipulationRank.values()[entityData.get(RANK)]; }
	public BlockPos loomPos() { return entityData.get(LOOM); }
	public float strain() { return entityData.get(STRAIN); }
	public boolean isSlack() { return strain() >= 1.0F; }
	public int windup() { return entityData.get(WINDUP); }
	public boolean isFaded() { return entityData.get(FADED) > 0; }
	protected void setWindup(int ticks) { entityData.set(WINDUP, ticks); }
	public void fade(int ticks) { entityData.set(FADED, ticks); }

	public void addStrain(float amount) {
		if (isSlack()) return;
		entityData.set(STRAIN, Math.min(1.0F, strain() + amount));
		if (isSlack()) {
			setWindup(0);
			if (level() instanceof ServerLevel server) {
				server.sendParticles(HemoParticleData.darkGlow(tendency().getColor()), getX(), getY(), getZ(), 10, 0.2D, 0.2D, 0.2D, 0.02D);
				server.playSound(null, getX(), getY(), getZ(), SoundEvents.WOOL_PLACE, SoundSource.HOSTILE, 0.8F, 0.5F);
			}
		}
	}

	/** Weaves a slack thread home through the loom; a taut one refuses. */
	public boolean tryHook(Player by) {
		if (!isSlack() || !(level() instanceof ServerLevel server)) return false;
		return server.getBlockEntity(loomPos()) instanceof SomaticLoomBlockEntity loom && loom.weaveHome(this, by);
	}

	@Nullable
	private SomaticLoomBlockEntity loom(ServerLevel server) {
		return server.isLoaded(loomPos()) && server.getBlockEntity(loomPos()) instanceof SomaticLoomBlockEntity loom
				&& loom.ownsLooseThread(getUUID()) ? loom : null;
	}

	@Override
	public void tick() {
		noPhysics = true;
		super.tick();
		if (!(level() instanceof ServerLevel server)) return;
		SomaticLoomBlockEntity loom = loom(server);
		if (loom == null) {
			discard();
			return;
		}
		if (entityData.get(FADED) > 0) entityData.set(FADED, entityData.get(FADED) - 1);
		Vec3 home = loom.echoAnchor();
		if (isSlack()) {
			// Spent: it sinks toward the loom and waits to be hooked.
			steer(home.add(0, 0.6D, 0), 0.04D);
			return;
		}
		ServerPlayer target = loom.weaverPlayer(server);
		if (target == null) {
			steer(home.add(0, 1.0D, 0), 0.06D);
			return;
		}
		ThreadAttacks.Profile profile = ThreadAttacks.profile(tendency());
		double angle = tickCount * profile.orbit() + getId();
		Vec3 desired = target.position().add(Math.cos(angle) * profile.distance(), profile.height(),
				Math.sin(angle) * profile.distance());
		Vec3 fromLoom = desired.subtract(home);
		if (fromLoom.lengthSqr() > SomaticWeaveRules.RITUAL_RADIUS * SomaticWeaveRules.RITUAL_RADIUS)
			desired = home.add(fromLoom.normalize().scale(SomaticWeaveRules.RITUAL_RADIUS));
		steer(desired, profile.speed());
		getLookControl().setLookAt(target);
		tickAttack(target);
	}

	private void steer(Vec3 to, double speed) {
		Vec3 delta = to.subtract(position());
		Vec3 push = delta.lengthSqr() < 1.0E-4D ? Vec3.ZERO : delta.normalize().scale(Math.min(delta.length(), speed));
		setDeltaMovement(getDeltaMovement().scale(0.8D).add(push));
	}

	private void tickAttack(ServerPlayer target) {
		int windup = windup();
		if (windup > 0) {
			setWindup(windup - 1);
			if (windup - 1 == 0) {
				ThreadAttacks.perform(this, target);
				addStrain(SomaticWeaveRules.strainPerAttack(rank()));
				cooldown = SomaticWeaveRules.attackInterval(rank());
			}
			return;
		}
		if (--cooldown <= 0 && hasLineOfSight(target) && distanceToSqr(target) < 14 * 14) {
			setWindup(SomaticWeaveRules.WINDUP_TICKS);
			level().playSound(null, getX(), getY(), getZ(), SoundEvents.WOOL_HIT, SoundSource.HOSTILE, 1.0F, 1.6F);
		}
	}

	/** Threads take no harm: a Living Staff strike strains them, anything else passes through. */
	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (level().isClientSide || isFaded() || isSlack()) return false;
		if (source.getEntity() instanceof Player player && player.getMainHandItem().getItem() instanceof LivingStaffItem) {
			addStrain(SomaticWeaveRules.strainPerHit(rank()));
			if (level() instanceof ServerLevel server)
				server.sendParticles(HemoParticleData.ember(tendency().getColor(), 0.08F, 0.8F, 14), getX(), getY(), getZ(),
						8, 0.15D, 0.15D, 0.15D, 0.05D);
			setDeltaMovement(getDeltaMovement().add(position().subtract(player.position()).normalize().scale(0.4D)));
		}
		return false;
	}

	@Override
	protected InteractionResult mobInteract(Player player, InteractionHand hand) {
		if (player.getItemInHand(hand).getItem() instanceof LivingStaffItem && isSlack()) {
			return tryHook(player) ? InteractionResult.SUCCESS : InteractionResult.PASS;
		}
		return InteractionResult.PASS;
	}

	@Override public boolean isPushable() { return false; }
	@Override public boolean removeWhenFarAway(double distance) { return false; }
	@Override protected void registerGoals() {}
	@Override public boolean causeFallDamage(float distance, float multiplier, DamageSource source) { return false; }

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putByte("Tendency", entityData.get(TENDENCY));
		tag.putByte("Rank", entityData.get(RANK));
		tag.putFloat("Strain", strain());
		tag.put("Loom", NbtUtils.writeBlockPos(loomPos()));
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		entityData.set(TENDENCY, tag.getByte("Tendency"));
		entityData.set(RANK, tag.getByte("Rank"));
		entityData.set(STRAIN, tag.getFloat("Strain"));
		NbtUtils.readBlockPos(tag, "Loom").ifPresent(pos -> entityData.set(LOOM, pos));
	}
}
```

Create a stub `ThreadAttacks` now so this compiles. Task 4 replaces `perform`.

```java
package com.vincenthuto.hemomancy.common.entity.weave;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import net.minecraft.server.level.ServerPlayer;

/** Each tendency's way of moving and striking. */
public final class ThreadAttacks {
	/** Preferred distance and height from the weaver, orbit speed (radians/tick) and steering speed. */
	public record Profile(double distance, double height, double orbit, double speed) {}

	private ThreadAttacks() {
	}

	public static Profile profile(EnumBloodTendency tendency) {
		return switch (tendency) {
			case ANIMUS -> new Profile(4.5, 1.6, 0.03, 0.09);
			case FLAMMEUS -> new Profile(3.0, 1.2, 0.08, 0.16);
			case CONGEATIO -> new Profile(5.0, 1.4, 0.015, 0.05);
			case MORTEM -> new Profile(3.5, 0.5, 0.02, 0.06);
			case TENEBRIS -> new Profile(4.0, 1.8, 0.04, 0.1);
			case FERRIC -> new Profile(3.5, 0.3, 0.025, 0.07);
			case LUX -> new Profile(5.5, 3.2, 0.02, 0.08);
			case DUCTILIS -> new Profile(4.5, 1.4, 0.06, 0.14);
		};
	}

	public static void perform(FrayedThreadEntity thread, ServerPlayer target) {
	}
}
```

- [ ] **Step 5: Replace the orb state in `SomaticLoomBlockEntity`**

Patch the file with a Python script that preserves the `\r\r\n` separators, as in the scratchpad's `patch_enzymes.py`: split on `\n`, keep each line's trailing `\r` run, and write back.

**Delete:**
- `TAG_ACTIVE_ORB`, `TAG_LAST_ORB_DRAG_GAME_TIME`;
- every `ORB_*` constant except none (all go);
- the fields `ritualOrbs`, `activeOrb`, `lastOrbDragGameTime`;
- the methods `beginOrbWeaving`, `spawnRitualOrbs`, `splitOrbCosts`, `tickOrbDrift`, `tickWanderingOrb`, `tickClientOrbMotion`, `dragSelectedOrb`, `isOrbWithinLoomAbsorptionBounds`, `getActiveOrTargetedOrb`, `findOrbDragTarget`, `clampOrbOffsetToRitualBounds`, `findTargetedOrb`, `completeOrb`, `spawnOrbDragTrail`, `spendOrbEnzyme`, `allOrbsCompleted`, `clearActiveOrbDrag`, `mergeSyncedRitualOrbs`;
- the `RitualOrb` class;
- `getRitualOrbs()`.

Keep `playOrbAbsorptionFeedback` logic, renamed `playWeaveFeedback(EnumBloodTendency)`.

Rename `PHASE_WEAVING_ORBS` to `PHASE_WEAVING` (value 2) and add `public static final int PHASE_CLIMAX = 3;`.

**Add these tags and fields:**

```java
	private static final String TAG_WEAVE_THREADS = "weaveThreads";
	private static final String TAG_WEAVER = "weaver";
	private static final String TAG_WEAVE_TICKS = "weaveTicks";
	private static final String TAG_CLIMAX_TICKS = "climaxTicks";
	private static final String TAG_PAUSED_TICKS = "pausedTicks";
	private static final String TAG_SPUN = "spun";
	private static final String TAG_LEGACY_RITUAL_ORBS = "ritualOrbs";

	private final List<WeaveThread> weaveThreads = new ArrayList<>();
	private final EnumMap<EnumBloodTendency, Integer> spun = MemoryWeavingRecipe.blankEnzymeRequirements();
	@Nullable private UUID weaver;
	/** Counted only while the weave runs, so pausing freezes the echo's loop in place. */
	private long weaveTicks;
	private int climaxTicks;
	private int pausedTicks;
	private boolean paused;
```

**Add the thread record** (replacing `RitualOrb`):

```java
	/** One enzyme's thread: still held in the echo, loose as a hostile entity, or woven into the cloth. */
	public static final class WeaveThread {
		public enum State { HELD, LOOSE, WOVEN }

		private final EnumBloodTendency tendency;
		private State state = State.HELD;
		@Nullable private UUID entity;

		private WeaveThread(EnumBloodTendency tendency) {
			this.tendency = tendency;
		}

		public EnumBloodTendency tendency() { return tendency; }
		public State state() { return state; }
		@Nullable public UUID entity() { return entity; }

		private CompoundTag save() {
			CompoundTag tag = new CompoundTag();
			tag.putString("tendency", tendency.name());
			tag.putString("state", state.name());
			if (entity != null) tag.putUUID("entity", entity);
			return tag;
		}

		private static WeaveThread load(CompoundTag tag) {
			WeaveThread thread = new WeaveThread(EnumBloodTendency.valueOf(tag.getString("tendency")));
			thread.state = State.valueOf(tag.getString("state"));
			if (tag.hasUUID("entity")) thread.entity = tag.getUUID("entity");
			return thread;
		}
	}
```

**Replace `beginOrbWeaving`.** It is called from `tryChargeRitualBlood` at both former call sites.

```java
	private void beginWeave(Player player) {
		if (curRecipe == null || level == null) return;
		craftingPhase = PHASE_WEAVING;
		weaver = player.getUUID();
		weaveTicks = 0;
		climaxTicks = 0;
		pausedTicks = 0;
		paused = false;
		weaveThreads.clear();
		// Payment draws each required enzyme out of its vial and spins it onto the pirn as thread.
		for (EnumBloodTendency tend : EnumBloodTendency.values()) {
			int required = curRecipe.getEnzymeRequirement(tend);
			if (required <= 0) continue;
			consumeStoredEnzyme(tend, required);
			spun.put(tend, required);
			for (int i = 0; i < SomaticWeaveRules.threadsFor(required); i++) weaveThreads.add(new WeaveThread(tend));
		}
		msg(player, "The echo of the memory takes shape over the warp. Its threads will fight being bound.",
				ChatFormatting.DARK_PURPLE, true);
		playSound(SoundEvents.END_PORTAL_FRAME_FILL, 0.8f, 0.55f);
		if (level instanceof ServerLevel serverLevel)
			PacketHandler.sendMonolithShatterBurst(Vec3.atCenterOf(worldPosition), 48.0D, serverLevel);
		markDirtyAndSync();
	}
```

**Server tick.** Replace the body of `serverTick`:

```java
	public static void serverTick(Level level, BlockPos worldPosition, BlockState state, SomaticLoomBlockEntity self) {
		if (self.curRecipe == null || level.getGameTime() % 20L == 0L) self.refreshRecipe();
		if (level instanceof ServerLevel server && self.isWeaving() && !self.recoveryRequired) self.tickWeave(server);
	}
```

**Client tick.** Replace the body of `clientTick`:

```java
	public static void clientTick(Level level, BlockPos worldPosition, BlockState state, SomaticLoomBlockEntity self) {
		if (self.craftingPhase == PHASE_WEAVING && !self.paused) self.weaveTicks++;
		else if (self.craftingPhase == PHASE_CLIMAX) self.climaxTicks++;
	}
```

**The state machine:**

```java
	private void tickWeave(ServerLevel server) {
		if (craftingPhase == PHASE_CLIMAX) {
			if (++climaxTicks >= SomaticWeaveRules.CLIMAX_TICKS) tickCompletion(weaverPlayer(server));
			return;
		}
		if (weaver == null) adoptNearestWeaver(server);
		reconcileLooseThreads(server);
		ServerPlayer player = weaverPlayer(server);
		boolean present = player != null && SomaticWeaveRules.weaverPresent(player.isAlive(),
				player.level() == server, player.distanceToSqr(Vec3.atCenterOf(worldPosition)));
		if (pausedTicks > 0) pausedTicks--;
		boolean shouldPause = !present || pausedTicks > 0;
		if (shouldPause != paused) {
			paused = shouldPause;
			if (paused) recallLooseThreads(server);
			markDirtyAndSync();
		}
		if (paused) return;
		weaveTicks++;
		if (SomaticWeaveRules.frayDue(weaveTicks) && looseCount() < SomaticWeaveRules.maxLoose(getWeaveRank())) {
			weaveThreads.stream().filter(t -> t.state == WeaveThread.State.HELD).findFirst().ifPresent(thread -> {
				thread.state = WeaveThread.State.LOOSE;
				thread.entity = FrayedThreadEntity.spawn(server, this, thread.tendency, getWeaveRank()).getUUID();
				markDirtyAndSync();
			});
		}
	}

	private long looseCount() {
		return weaveThreads.stream().filter(t -> t.state == WeaveThread.State.LOOSE).count();
	}

	/** Loose threads whose entity has gone (unloaded, killed by commands) return to the echo. */
	private void reconcileLooseThreads(ServerLevel server) {
		boolean changed = false;
		for (WeaveThread thread : weaveThreads) {
			if (thread.state == WeaveThread.State.LOOSE
					&& (thread.entity == null || !(server.getEntity(thread.entity) instanceof FrayedThreadEntity))) {
				thread.state = WeaveThread.State.HELD;
				thread.entity = null;
				changed = true;
			}
		}
		if (changed) markDirtyAndSync();
	}

	private void recallLooseThreads(ServerLevel server) {
		for (WeaveThread thread : weaveThreads) {
			if (thread.state != WeaveThread.State.LOOSE) continue;
			if (thread.entity != null && server.getEntity(thread.entity) instanceof FrayedThreadEntity entity)
				entity.discard();
			thread.state = WeaveThread.State.HELD;
			thread.entity = null;
		}
	}

	/** A weave carried over from an older save, or whose weaver is unknown, is taken up by the nearest player. */
	private void adoptNearestWeaver(ServerLevel server) {
		Vec3 centre = Vec3.atCenterOf(worldPosition);
		Player nearest = server.getNearestPlayer(centre.x, centre.y, centre.z, SomaticWeaveRules.RITUAL_RADIUS,
				p -> p.isAlive() && !p.isSpectator());
		if (nearest != null) {
			weaver = nearest.getUUID();
			markDirtyAndSync();
		}
	}

	public boolean weaveHome(FrayedThreadEntity entity, Player by) {
		for (WeaveThread thread : weaveThreads) {
			if (thread.state != WeaveThread.State.LOOSE || !entity.getUUID().equals(thread.entity)) continue;
			thread.state = WeaveThread.State.WOVEN;
			thread.entity = null;
			spun.merge(thread.tendency, -1, (a, b) -> Math.max(0, a + b));
			entity.discard();
			playWeaveFeedback(thread.tendency);
			if (weaveThreads.stream().allMatch(t -> t.state == WeaveThread.State.WOVEN)) {
				craftingPhase = PHASE_CLIMAX;
				climaxTicks = 0;
			}
			markDirtyAndSync();
			return true;
		}
		return false;
	}

	public void blackout() {
		pausedTicks = SomaticWeaveRules.BLACKOUT_TICKS;
	}

	public boolean ownsLooseThread(UUID entity) {
		return isWeaving() && weaveThreads.stream()
				.anyMatch(t -> t.state == WeaveThread.State.LOOSE && entity.equals(t.entity));
	}

	@Nullable
	public ServerPlayer weaverPlayer(ServerLevel server) {
		return weaver == null ? null : server.getServer().getPlayerList().getPlayer(weaver);
	}

	public List<FrayedThreadEntity> looseThreadEntities(ServerLevel server) {
		List<FrayedThreadEntity> out = new ArrayList<>();
		for (WeaveThread thread : weaveThreads)
			if (thread.state == WeaveThread.State.LOOSE && thread.entity != null
					&& server.getEntity(thread.entity) instanceof FrayedThreadEntity entity)
				out.add(entity);
		return out;
	}

	public Vec3 echoAnchor() {
		Direction facing = getBlockState().hasProperty(SomaticLoomBlock.FACING)
				? getBlockState().getValue(SomaticLoomBlock.FACING) : Direction.NORTH;
		return SomaticLoomFrame.toWorld(worldPosition, facing, SomaticLoomGeometry.ECHO_X, SomaticLoomGeometry.ECHO_Y,
				SomaticLoomGeometry.ECHO_Z);
	}

	@Nullable
	public BloodManipulation getWeaveManipulation() {
		if (curRecipe == null || level == null) return null;
		return curRecipe.getResultItem(level.registryAccess()).getItem() instanceof BloodMemoryItem memory
				? memory.getManip() : null;
	}

	public EnumManipulationRank getWeaveRank() {
		BloodManipulation manip = getWeaveManipulation();
		return manip == null ? EnumManipulationRank.HUMILIS : manip.getRank();
	}

	public boolean isWeaving() { return craftingPhase == PHASE_WEAVING || craftingPhase == PHASE_CLIMAX; }
	public boolean isInClimax() { return craftingPhase == PHASE_CLIMAX; }
	public boolean isPaused() { return paused; }
	public List<WeaveThread> getWeaveThreads() { return List.copyOf(weaveThreads); }
	@Nullable public UUID getWeaver() { return weaver; }
	public long getWeaveTicks() { return weaveTicks; }
	public int getClimaxTicks() { return climaxTicks; }
	public int getSpun(EnumBloodTendency tendency) { return spun.getOrDefault(tendency, 0); }
```

`tickCompletion` already takes `@Nullable Player` through `msg` and `instanceof ServerPlayer`. Mark its parameter `@Nullable`. In `clearActiveRitual`, add:

```java
		if (level instanceof ServerLevel server) recallLooseThreads(server);
		weaveThreads.clear();
		for (EnumBloodTendency tend : EnumBloodTendency.values()) spun.put(tend, 0);
		weaver = null;
		weaveTicks = 0;
		climaxTicks = 0;
		pausedTicks = 0;
		paused = false;
```

`isWeavingOrbs()` becomes `isWeaving()`. Update `isCrafting`-adjacent checks that compared `craftingPhase == PHASE_WEAVING_ORBS` to use `isWeaving()`, including `addItem`'s `ritualBloodCharged > 0 || isWeavingOrbs()` and `refreshRecipe`'s `craftingPhase == PHASE_WEAVING_ORBS`.

**Update `provideTendencyFeedback`'s weaving message (former 854-856):**

```java
		if (isWeaving()) {
			msg(player, "Survive the frayed threads or strike them with a Living Staff until they go slack, then use the staff on a slack thread to weave it home. Crouch-use cancels and loses spent blood and enzymes.", ChatFormatting.DARK_PURPLE, false);
			return;
		}
```

**Save** (in `saveShared`, replacing the orb list and drag tags):

```java
		ListTag threads = new ListTag();
		for (WeaveThread thread : weaveThreads) threads.add(thread.save());
		tag.put(TAG_WEAVE_THREADS, threads);
		if (weaver != null) tag.putUUID(TAG_WEAVER, weaver);
		tag.putLong(TAG_WEAVE_TICKS, weaveTicks);
		tag.putInt(TAG_CLIMAX_TICKS, climaxTicks);
		tag.putInt(TAG_PAUSED_TICKS, pausedTicks);
		tag.putBoolean("paused", paused);
		CompoundTag spunTag = new CompoundTag();
		for (EnumBloodTendency tend : EnumBloodTendency.values()) spunTag.putInt(tend.name(), spun.getOrDefault(tend, 0));
		tag.put(TAG_SPUN, spunTag);
```

**Load** (in `loadShared`, replacing the orb block), including the legacy migration:

```java
		weaveThreads.clear();
		if (tag.contains(TAG_WEAVE_THREADS)) {
			ListTag threads = tag.getList(TAG_WEAVE_THREADS, Tag.TAG_COMPOUND);
			for (int i = 0; i < threads.size(); i++) weaveThreads.add(WeaveThread.load(threads.getCompound(i)));
			CompoundTag spunTag = tag.getCompound(TAG_SPUN);
			for (EnumBloodTendency tend : EnumBloodTendency.values()) spun.put(tend, spunTag.getInt(tend.name()));
		} else if (tag.contains(TAG_LEGACY_RITUAL_ORBS)) {
			// Pre-witnessed saves: completed orbs are woven, the rest are held with their enzyme still on the pirn.
			for (EnumBloodTendency tend : EnumBloodTendency.values()) spun.put(tend, 0);
			ListTag orbs = tag.getList(TAG_LEGACY_RITUAL_ORBS, Tag.TAG_COMPOUND);
			for (int i = 0; i < orbs.size(); i++) {
				CompoundTag orb = orbs.getCompound(i);
				WeaveThread thread = new WeaveThread(EnumBloodTendency.valueOf(orb.getString("tendency")));
				if (orb.getBoolean("completed")) thread.state = WeaveThread.State.WOVEN;
				else spun.merge(thread.tendency, 1, Integer::sum);
				weaveThreads.add(thread);
			}
		}
		weaver = tag.hasUUID(TAG_WEAVER) ? tag.getUUID(TAG_WEAVER) : null;
		weaveTicks = tag.getLong(TAG_WEAVE_TICKS);
		climaxTicks = tag.getInt(TAG_CLIMAX_TICKS);
		pausedTicks = tag.getInt(TAG_PAUSED_TICKS);
		paused = tag.getBoolean("paused");
```

Imports to add: `FrayedThreadEntity`, `SomaticLoomBlock`, `SomaticLoomFrame`, `SomaticLoomGeometry`, `BloodManipulation`, `EnumManipulationRank`, `net.minecraft.core.Direction`, `java.util.UUID`. `BloodMemoryItem` is already imported. Remove `AABB` if it is now unused.

- [ ] **Step 6: Remove the staff scan and fix callers**

- `LivingStaffItem.java`: delete `dragNearestLoomOrb` (324-339) and its call block (166-168). Delete the `SomaticLoomBlockEntity` import if unused.
- `SomaticLoomBlock.java`: replace `isWeavingOrbs()` with `isWeaving()` at 266, 276 and 291.
- `SomaticLoomRenderer.java`:
  - replace `isWeavingOrbs()` with `isWeaving()`;
  - delete the `if (te.isWeavingOrbs()) { ... drawRitualOrbs(...) }` block in `render`, plus `fellAnchor` and every orb drawing helper (moved in Task 6);
  - in `drawWarp`, replace the orb loop with the following, keeping `owed`, which is now always satisfied:

```java
		if (weaving) {
			for (SomaticLoomBlockEntity.WeaveThread thread : te.getWeaveThreads()) {
				int t = thread.tendency().ordinal();
				orbs[t]++;
				if (thread.state() == SomaticLoomBlockEntity.WeaveThread.State.WOVEN) done[t]++;
			}
		}
```

  and set `boolean shortStock = !weaving && stored < required[t];` (spun enzyme can't run short).
  - In `drawWovenBands`, iterate `te.getWeaveThreads()` filtered to `WOVEN`, mapping to `tendency()`, and use `te.getWeaveThreads().size()` for the step.
- `SomaticLoomBlockEntity.getRenderBoundingBox` stays as is.

- [ ] **Step 7: Write `LoomWeaveTestSupport` and migrate the automation and tests**

```java
package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.entity.weave.FrayedThreadEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticLoomBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticWeaveRules;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

/** Drives a paid Somatic Loom weave to completion deterministically: ticks the loom, spends and hooks each thread. */
public final class LoomWeaveTestSupport {
    private LoomWeaveTestSupport() {}

    public static boolean weaveAll(ServerLevel level, BlockPos pos, SomaticLoomBlockEntity loom, Player weaver) {
        int budget = SomaticWeaveRules.LOOP_TICKS * (loom.getWeaveThreads().size() + 4) + SomaticWeaveRules.CLIMAX_TICKS + 20;
        for (int tick = 0; tick < budget && loom.isWeaving(); tick++) {
            SomaticLoomBlockEntity.serverTick(level, pos, level.getBlockState(pos), loom);
            for (FrayedThreadEntity thread : loom.looseThreadEntities(level)) {
                thread.addStrain(1f);
                if (!thread.tryHook(weaver)) return false;
            }
        }
        return !loom.isWeaving();
    }
}
```

- `HarbingerJourneyAutomation.weaveMemory` (507-532): keep the pay block (with `isWeavingOrbs` → `isWeaving`). Replace the orb while-loop with:

```java
		if (!LoomWeaveTestSupport.weaveAll(level, pos, loom, player))
			throw new IllegalStateException("Somatic Loom weave could not be completed");
```

  Use the method's existing `level`, loom position and player variables (named as they are at 507-532). Add the import `com.vincenthuto.hemomancy.gametest.LoomWeaveTestSupport`.
- `ClinicalBloodProgressionGameTests.java`:
  - replace the drag loop at 3663-3675 with `h.assertTrue(LoomWeaveTestSupport.weaveAll(level, pos, loom, p), "The first weave should finish");`, matching that test's variable names;
  - change `isWeavingOrbs()` at 3661 to `isWeaving()`;
  - change the expected feedback text at 3716 from "Hold a Living Staff on a strand" to "Survive the frayed threads or strike them with a Living Staff".
- `HarbingerRepairGameTests.java` 1109, 1117, 1123: `isWeavingOrbs()` → `isWeaving()`.
- `LoomClientReview.java`:
  - replace `completeOrbs(level, weaving, 3)` with a loop that ticks the loom until 3 threads are loose, then strains and hooks them through `LoomWeaveTestSupport`-style calls;
  - change its log line `weaving.getRitualOrbs().size()` to `weaving.getWeaveThreads().size()`;
  - delete the `completeOrbs` method.

- [ ] **Step 8: Update the source contracts**

In `MemoryWeavingRedesignSourceTest.java`:
- **delete** the assertions at lines 48, 60-66, 68-104 (every one naming orb drag, wander, drift, absorption bounds, `costs.add(1)`, `ORB_*`, `previousOffset`, `renderOffset`, `spawnOrbDragTrail` or `HemoParticleData.glow(orb...)`), 112-121 (renderer orb hooks), and 184 (staff `dragSelectedOrb`);
- change line 46 to `assertContains("loom has a weaving phase", loom, "PHASE_WEAVING");` and line 172 to expect `te.isWeaving()`;
- add:

```java
		assertContains("loom spins required enzymes onto the pirns at payment", loom, "consumeStoredEnzyme(tend, required)");
		assertContains("loom threads tear loose on the echo loop", loom, "SomaticWeaveRules.frayDue(weaveTicks)");
		assertContains("loom recalls loose threads when the weave pauses", loom, "recallLooseThreads(server)");
		assertContains("loom migrates pre-witnessed orb saves", loom, "TAG_LEGACY_RITUAL_ORBS");
		assertDoesNotContain("loom no longer drags orbs at a fixed depth", loom, "dragSelectedOrb");
		assertDoesNotContain("the living staff no longer scans for looms every tick", staff, "dragNearestLoomOrb");
```

In `ModelFirstTileRendererResourceTest.java`, replace `"drawRitualOrbs(buffer"` in the loom hook list with `"drawWarp(vc, px, te"`. That hook already exists; the thread rendering is checked in Task 6.

- [ ] **Step 9: Compile, run the contracts and GameTests**

```powershell
./gradlew.bat compileJava compileTestJava compileGameTestJava --console=plain -q
$gson = "$env:USERPROFILE\.gradle\caches\modules-2\files-2.1\com.google.code.gson\gson\2.10\dd9b193aef96e973d5a11ab13cd17430c2e4306b\gson-2.10.jar"
java -cp "build/classes/java/test;$gson" com.vincenthuto.hemomancy.common.recipe.MemoryWeavingRedesignSourceTest
java -cp "build/classes/java/test;$gson" com.vincenthuto.hemomancy.common.block.ModelFirstTileRendererResourceTest
./gradlew.bat runSomaticLoomGameTestServer --console=plain
```

Expected: both mains exit 0, and `All 8 required tests passed`. Then run the journey and clinical suites that weave:
- `./gradlew.bat runClinicalGameTestServer --console=plain`
- the default `gameTestServer` for `JourneyAutomationGameTests` and `HarbingerRepairGameTests`

All loom-related tests must pass. Unrelated failures from other sessions' work must be named in the report, not fixed here.

---

### Task 4: Tendency attacks

**Files:**
- Create: `common/entity/weave/ThreadBoltEntity.java`, `common/entity/weave/ThreadHazardEntity.java`
- Modify: `common/entity/weave/ThreadAttacks.java` (`perform`, `onBoltHit`, `spikes`), `common/init/EntityInit.java` (two types), `client/event/ClientEvents.java` (`NoopRenderer` for both until Task 6)
- Test: add to `SomaticLoomWeavingGameTests.java`

**Interfaces:**
- Consumes: `FrayedThreadEntity` (`tendency()`, `rank()`, `fade(int)`), `SomaticWeaveRules.attackPower`.
- Produces:
  - `ThreadBoltEntity(Level, FrayedThreadEntity owner, EnumBloodTendency, float power)`;
  - `ThreadHazardEntity.spawn(ServerLevel, FrayedThreadEntity owner, Vec3 at, EnumBloodTendency, float radius, int ticks, float power)`;
  - `ThreadAttacks.onBoltHit(EnumBloodTendency, FrayedThreadEntity thread, LivingEntity target, float power)` (public for tests);
  - `ThreadAttacks.markWeaveHarm(ServerPlayer target, FrayedThreadEntity thread)`, which records the non-lethal window used in Task 5.

- [ ] **Step 1: Write the failing attack GameTests**

Append to `SomaticLoomWeavingGameTests`. Mock players take damage; `FakePlayer` is invulnerable, so it is used only for blood volume.

```java
    private static FrayedThreadEntity looseThread(GameTestHelper helper, Setup s) {
        pay(helper, s);
        tick(helper, s, SomaticWeaveRules.FRAY_TICK + 1);
        return s.loom.looseThreadEntities(helper.getLevel()).getFirst();
    }

    @GameTest(template = "empty")
    public static void tendencyHitsApplyTheirSchoolsHarm(GameTestHelper helper) {
        Setup s = setup(helper, 4);
        FrayedThreadEntity thread = looseThread(helper, s);
        var victim = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        victim.moveTo(thread.getX() + 1, thread.getY(), thread.getZ());

        ThreadAttacks.onBoltHit(EnumBloodTendency.CONGEATIO, thread, victim, 1f);
        helper.assertTrue(victim.hasEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN)
                && victim.getTicksFrozen() > 0, "Congeatio slows and freezes");

        float before = victim.getHealth();
        ThreadAttacks.onBoltHit(EnumBloodTendency.LUX, thread, victim, 1f);
        helper.assertTrue(victim.getHealth() < before && victim.hasEffect(net.minecraft.world.effect.MobEffects.GLOWING),
                "Lux lances burn and mark");

        ThreadAttacks.onBoltHit(EnumBloodTendency.FLAMMEUS, thread, victim, 1f);
        helper.assertTrue(victim.getRemainingFireTicks() > 0, "Flammeus ignites");
        s.weaver.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void animusBoltsDrainTheWeaversBlood(GameTestHelper helper) {
        Setup s = setup(helper, 4);
        FrayedThreadEntity thread = looseThread(helper, s);
        double before = HemoCapabilityAccess.getBloodVolume(s.weaver).orElseThrow().getBloodVolume();
        ThreadAttacks.onBoltHit(EnumBloodTendency.ANIMUS, thread, s.weaver, 1f);
        double after = HemoCapabilityAccess.getBloodVolume(s.weaver).orElseThrow().getBloodVolume();
        helper.assertTrue(after < before, "Animus drains blood volume");
        s.weaver.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void areaAttacksLandOnTheWeaver(GameTestHelper helper) {
        Setup s = setup(helper, 4);
        FrayedThreadEntity thread = looseThread(helper, s);
        var victim = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        victim.moveTo(thread.getX() + 2, thread.getY() - 1, thread.getZ());
        var level = helper.getLevel();

        ThreadHazardEntity miasma = ThreadHazardEntity.spawn(level, thread, victim.position(), EnumBloodTendency.MORTEM, 2.5f, 80, 1f);
        for (int i = 0; i < 21; i++) miasma.tick();
        helper.assertTrue(victim.hasEffect(net.minecraft.world.effect.MobEffects.CONFUSION)
                && victim.hasEffect(net.minecraft.world.effect.MobEffects.POISON), "Mortem miasma sickens");

        int spikesBefore = level.getEntitiesOfClass(com.vincenthuto.hemomancy.common.entity.summon.EntityIronSpike.class,
                new AABB(victim.blockPosition()).inflate(5)).size();
        ThreadAttacks.spikes(thread, victim, 1f);
        int spikesAfter = level.getEntitiesOfClass(com.vincenthuto.hemomancy.common.entity.summon.EntityIronSpike.class,
                new AABB(victim.blockPosition()).inflate(5)).size();
        helper.assertTrue(spikesAfter > spikesBefore && victim.getDeltaMovement().y > 0, "Ferric spikes erupt and launch");
        s.weaver.discard();
        helper.succeed();
    }
```

Add the imports `com.vincenthuto.hemomancy.common.entity.weave.ThreadAttacks` and `ThreadHazardEntity`.

- [ ] **Step 2: Run to verify it fails**

Run: `./gradlew.bat runSomaticLoomGameTestServer --console=plain`
Expected: compile failure (`ThreadHazardEntity`, `onBoltHit`, `spikes` missing).

- [ ] **Step 3: Register the bolt and hazard types**

In `EntityInit`:

```java
	public static final DeferredHolder<EntityType<?>, EntityType<ThreadBoltEntity>> thread_bolt = ENTITY_TYPES.register(
			"thread_bolt",
			() -> EntityType.Builder.<ThreadBoltEntity>of(ThreadBoltEntity::new, MobCategory.MISC)
					.sized(0.25F, 0.25F).clientTrackingRange(8).updateInterval(1)
					.build(Hemomancy.rloc("thread_bolt").toString()));
	public static final DeferredHolder<EntityType<?>, EntityType<ThreadHazardEntity>> thread_hazard = ENTITY_TYPES.register(
			"thread_hazard",
			() -> EntityType.Builder.<ThreadHazardEntity>of(ThreadHazardEntity::new, MobCategory.MISC)
					.sized(1.0F, 0.25F).clientTrackingRange(8)
					.build(Hemomancy.rloc("thread_hazard").toString()));
```

Register `NoopRenderer` for both in `ClientEvents.renderEntities`, and add lang names "Thread Bolt" and "Thread Hazard".

- [ ] **Step 4: Write `ThreadBoltEntity`**

```java
package com.vincenthuto.hemomancy.common.entity.weave;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

/** A knot of a thread's tendency flung at the weaver; its hit applies that tendency's harm. */
public class ThreadBoltEntity extends ThrowableProjectile {
	private static final EntityDataAccessor<Byte> TENDENCY = SynchedEntityData.defineId(ThreadBoltEntity.class, EntityDataSerializers.BYTE);
	private float power = 1.0F;

	public ThreadBoltEntity(EntityType<? extends ThreadBoltEntity> type, Level level) {
		super(type, level);
	}

	public ThreadBoltEntity(Level level, FrayedThreadEntity owner, EnumBloodTendency tendency, float power) {
		super(EntityInit.thread_bolt.get(), owner, level);
		this.entityData.set(TENDENCY, (byte) tendency.ordinal());
		this.power = power;
	}

	public EnumBloodTendency tendency() {
		return EnumBloodTendency.values()[entityData.get(TENDENCY)];
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(TENDENCY, (byte) 0);
	}

	@Override
	protected double getDefaultGravity() {
		return 0.0D;
	}

	@Override
	public void tick() {
		super.tick();
		if (level().isClientSide)
			level().addParticle(HemoParticleData.glow(tendency().getColor()), getX(), getY(), getZ(), 0, 0, 0);
		else if (tickCount > 60) discard();
	}

	@Override
	protected void onHitEntity(EntityHitResult hit) {
		if (!level().isClientSide && hit.getEntity() instanceof LivingEntity target && getOwner() instanceof FrayedThreadEntity thread
				&& !(target instanceof FrayedThreadEntity))
			ThreadAttacks.onBoltHit(tendency(), thread, target, power);
		discard();
	}

	@Override
	protected void onHitBlock(BlockHitResult hit) {
		super.onHitBlock(hit);
		if (!level().isClientSide && tendency() == EnumBloodTendency.FLAMMEUS && getOwner() instanceof FrayedThreadEntity thread
				&& level() instanceof net.minecraft.server.level.ServerLevel server)
			ThreadHazardEntity.spawn(server, thread, hit.getLocation(), EnumBloodTendency.FLAMMEUS, 1.5F, 60, power);
		discard();
	}
}
```

- [ ] **Step 5: Write `ThreadHazardEntity`**

```java
package com.vincenthuto.hemomancy.common.entity.weave;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.UUID;

/** A lingering patch a thread leaves on the ground: Flammeus embers burn, Mortem miasma sickens. */
public class ThreadHazardEntity extends Entity {
	private static final EntityDataAccessor<Byte> TENDENCY = SynchedEntityData.defineId(ThreadHazardEntity.class, EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Float> RADIUS = SynchedEntityData.defineId(ThreadHazardEntity.class, EntityDataSerializers.FLOAT);
	private int life;
	private float power = 1.0F;
	@Nullable private UUID owner;

	public ThreadHazardEntity(EntityType<? extends ThreadHazardEntity> type, Level level) {
		super(type, level);
		noPhysics = true;
	}

	public static ThreadHazardEntity spawn(ServerLevel level, FrayedThreadEntity owner, Vec3 at, EnumBloodTendency tendency,
			float radius, int ticks, float power) {
		ThreadHazardEntity hazard = new ThreadHazardEntity(EntityInit.thread_hazard.get(), level);
		hazard.entityData.set(TENDENCY, (byte) tendency.ordinal());
		hazard.entityData.set(RADIUS, radius);
		hazard.life = ticks;
		hazard.power = power;
		hazard.owner = owner.getUUID();
		hazard.moveTo(at.x, at.y, at.z);
		level.addFreshEntity(hazard);
		return hazard;
	}

	public EnumBloodTendency tendency() { return EnumBloodTendency.values()[entityData.get(TENDENCY)]; }
	public float radius() { return entityData.get(RADIUS); }

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(TENDENCY, (byte) 0);
		builder.define(RADIUS, 1.5F);
	}

	@Override
	public void tick() {
		super.tick();
		float r = radius();
		if (level().isClientSide) {
			for (int i = 0; i < 2; i++) {
				double a = random.nextDouble() * Math.PI * 2, d = random.nextDouble() * r;
				level().addParticle(tendency() == EnumBloodTendency.MORTEM ? HemoParticleData.darkGlow(tendency().getColor())
								: HemoParticleData.ember(tendency().getColor(), 0.08F, 0.8F, 18),
						getX() + Math.cos(a) * d, getY() + 0.1, getZ() + Math.sin(a) * d, 0, 0.03, 0);
			}
			return;
		}
		if (--life <= 0) {
			discard();
			return;
		}
		if (tickCount % 20 != 0 || !(level() instanceof ServerLevel server)) return;
		FrayedThreadEntity thread = owner != null && server.getEntity(owner) instanceof FrayedThreadEntity t ? t : null;
		for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class, new AABB(blockPosition()).inflate(r, 1.0, r),
				e -> e.isAlive() && !(e instanceof FrayedThreadEntity) && e.distanceToSqr(this) <= r * r + 1)) {
			if (tendency() == EnumBloodTendency.MORTEM) {
				victim.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0));
				victim.addEffect(new MobEffectInstance(MobEffects.POISON, 60, power >= 2.0F ? 1 : 0));
			} else {
				victim.hurt(thread != null ? damageSources().mobAttack(thread) : damageSources().inFire(), power);
				victim.igniteForSeconds(1.0F);
			}
			if (thread != null && victim instanceof ServerPlayer player) ThreadAttacks.markWeaveHarm(player, thread);
		}
	}

	@Override protected void readAdditionalSaveData(CompoundTag tag) { discard(); }
	@Override protected void addAdditionalSaveData(CompoundTag tag) {}
}
```

Hazards are transient: a reload discards them.

- [ ] **Step 6: Fill in `ThreadAttacks`**

Replace the stub `perform` and add the helpers:

```java
	public static void perform(FrayedThreadEntity thread, ServerPlayer target) {
		if (!(thread.level() instanceof ServerLevel server)) return;
		float power = SomaticWeaveRules.attackPower(thread.rank());
		EnumBloodTendency tendency = thread.tendency();
		switch (tendency) {
			case ANIMUS -> fire(server, thread, target, tendency, power, 1, 0F, 1.2F);
			case FLAMMEUS -> {
				thread.setDeltaMovement(target.getEyePosition().subtract(thread.position()).normalize().scale(0.6D));
				fire(server, thread, target, tendency, power, 1, 0F, 1.1F);
			}
			case CONGEATIO -> fire(server, thread, target, tendency, power, 3, 12F, 0.8F);
			case LUX -> {
				target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20, 0));
				fire(server, thread, target, tendency, power, 2, 5F, 1.6F);
			}
			case MORTEM -> ThreadHazardEntity.spawn(server, thread, target.position(), tendency, 2.5F, 80, power);
			case TENEBRIS -> {
				if (thread.distanceToSqr(target) < 100) {
					target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
					target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 100, 0));
				}
				thread.fade(40);
				server.sendParticles(HemoParticleData.darkGlow(tendency.getColor()), thread.getX(), thread.getY(), thread.getZ(),
						24, 0.6D, 0.6D, 0.6D, 0.04D);
			}
			case FERRIC -> spikes(thread, target, power);
			case DUCTILIS -> jolt(server, thread, target, power);
		}
		markWeaveHarm(target, thread);
	}

	private static void fire(ServerLevel server, FrayedThreadEntity thread, LivingEntity target, EnumBloodTendency tendency,
			float power, int count, float spread, float speed) {
		Vec3 aim = target.getEyePosition().subtract(thread.position());
		for (int i = 0; i < count; i++) {
			ThreadBoltEntity bolt = new ThreadBoltEntity(server, thread, tendency, power);
			bolt.setPos(thread.getX(), thread.getY(), thread.getZ());
			float yaw = count == 1 ? 0F : (i - (count - 1) / 2F) * spread;
			Vec3 dir = aim.yRot((float) Math.toRadians(yaw));
			bolt.shoot(dir.x, dir.y, dir.z, speed, 2.0F);
			server.addFreshEntity(bolt);
		}
	}

	public static void onBoltHit(EnumBloodTendency tendency, FrayedThreadEntity thread, LivingEntity target, float power) {
		target.hurt(thread.damageSources().mobAttack(thread), 2.0F * power);
		switch (tendency) {
			case ANIMUS -> {
				if (target instanceof ServerPlayer player)
					HemoCapabilityAccess.getBloodVolume(player).ifPresent(volume -> {
						if (volume.isActive() && volume.drain(Math.min(volume.getBloodVolume(), 10.0D * power)))
							PacketHandler.sendToPlayer(player, new BloodVolumeServerPacket(volume));
					});
			}
			case FLAMMEUS -> target.igniteForSeconds(2.0F);
			case CONGEATIO -> {
				target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
				target.setTicksFrozen(Math.min(target.getTicksFrozen() + 60, target.getTicksRequiredToFreeze() + 40));
			}
			case LUX -> target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0));
			default -> {
			}
		}
		if (target instanceof ServerPlayer player) markWeaveHarm(player, thread);
	}

	/** Copied from EndgameBossActions.summonGripSpikes (private there): a short ring of spikes, then a launch. */
	public static void spikes(FrayedThreadEntity thread, LivingEntity target, float power) {
		if (!(thread.level() instanceof ServerLevel server)) return;
		int count = power >= 2.0F ? 4 : 3;
		for (int i = 0; i < count; i++) {
			double a = i * Math.PI * 2 / count + server.random.nextDouble() * 0.5;
			BlockPos base = BlockPos.containing(target.getX() + Math.cos(a) * 1.5, target.getY(), target.getZ() + Math.sin(a) * 1.5);
			BlockPos ground = findGround(server, base);
			if (ground == null) continue;
			EntityIronSpike spike = new EntityIronSpike(EntityInit.iron_spike.get(), server, thread);
			spike.moveTo(ground.getX() + 0.5D, ground.getY(), ground.getZ() + 0.5D, server.random.nextFloat() * 360F, 0F);
			server.addFreshEntity(spike);
		}
		Vec3 away = target.position().subtract(thread.position()).multiply(1, 0, 1);
		Vec3 push = away.lengthSqr() < 1.0E-4D ? Vec3.ZERO : away.normalize().scale(0.4D);
		target.setDeltaMovement(target.getDeltaMovement().add(push).add(0.0D, 0.6D, 0.0D));
		target.hurtMarked = true;
	}

	private static void jolt(ServerLevel server, FrayedThreadEntity thread, ServerPlayer target, float power) {
		if (thread.distanceToSqr(target) > 100 || !thread.hasLineOfSight(target)) return;
		target.hurt(thread.damageSources().mobAttack(thread), 1.5F * power);
		target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 6));
		Vec3 pull = thread.position().subtract(target.position()).normalize().scale(0.5D);
		target.setDeltaMovement(target.getDeltaMovement().add(pull));
		target.hurtMarked = true;
		Vec3 from = thread.position(), to = target.getEyePosition();
		for (int i = 0; i <= 10; i++) {
			Vec3 p = from.lerp(to, i / 10.0D);
			server.sendParticles(HemoParticleData.glow(thread.tendency().getColor()), p.x, p.y, p.z, 1, 0.05D, 0.05D, 0.05D, 0D);
		}
	}

	/** Scans for air over solid ground near a position, as EndgameBossActions.findGround does. */
	private static BlockPos findGround(ServerLevel server, BlockPos base) {
		for (int dy = 4; dy >= -5; dy--) {
			BlockPos p = base.above(dy);
			if (server.getBlockState(p).isAir() && server.getBlockState(p.below()).isSolid()) return p;
		}
		return null;
	}

	/** Records that a thread just harmed the weaver, so its lingering harm counts toward the non-lethal floor. */
	public static void markWeaveHarm(ServerPlayer target, FrayedThreadEntity thread) {
		CompoundTag data = target.getPersistentData();
		data.putLong("hemomancy:weave_harm_until", target.level().getGameTime() + SomaticWeaveRules.NON_LETHAL_WINDOW_TICKS);
		data.putLong("hemomancy:weave_loom", thread.loomPos().asLong());
		data.putBoolean("hemomancy:weave_lethal", SomaticWeaveRules.lethal(thread.rank()));
	}
```

Imports: `HemoCapabilityAccess`, `PacketHandler`, `BloodVolumeServerPacket` (`common.network.capa.harbinger`), `EntityInit`, `EntityIronSpike` (`common.entity.summon`), `HemoParticleData`, `SomaticWeaveRules`, `BlockPos`, `CompoundTag`, `ServerLevel`, `MobEffectInstance`, `MobEffects`, `LivingEntity`, `Vec3`. `markWeaveHarm` takes a `ServerPlayer`; callers holding a `LivingEntity` guard with `instanceof ServerPlayer`, as `onBoltHit` and the hazard do. `perform` already has a `ServerPlayer`.

- [ ] **Step 7: Run to verify it passes**

Run: `./gradlew.bat runSomaticLoomGameTestServer --console=plain`
Expected: `All 11 required tests passed`.

---

### Task 5: Non-lethal floor, blackout and helpers

**Files:**
- Create: `common/entity/weave/WeaveDamageEvents.java`
- Test: add to `SomaticLoomWeavingGameTests.java`

**Interfaces:**
- Consumes: the persistent-data keys written by `ThreadAttacks.markWeaveHarm`, `SomaticLoomBlockEntity.blackout()`, and `SomaticWeaveRules.flooredDamage` / `blacksOut`.
- Produces: nothing new for later tasks.

- [ ] **Step 1: Write the failing tests**

```java
    @GameTest(template = "empty")
    public static void lowRankThreadsBlackOutInsteadOfKilling(GameTestHelper helper) {
        Setup s = setupBloodShot(helper);   // HUMILIS recipe, see helper below
        pay(helper, s);
        tick(helper, s, SomaticWeaveRules.FRAY_TICK + 1);
        FrayedThreadEntity thread = s.loom.looseThreadEntities(helper.getLevel()).getFirst();
        var victim = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        // The mock is the weaver for this test.
        s.loom.loadWithComponents(withWeaver(helper, s, victim.getUUID()), helper.getLevel().registryAccess());
        victim.setHealth(6.0F);
        victim.getPersistentData().putLong("hemomancy:weave_harm_until", helper.getLevel().getGameTime() + 100);
        victim.getPersistentData().putLong("hemomancy:weave_loom", s.pos.asLong());
        victim.getPersistentData().putBoolean("hemomancy:weave_lethal", false);
        victim.hurt(thread.damageSources().mobAttack(thread), 20.0F);
        helper.assertTrue(victim.isAlive() && victim.getHealth() >= SomaticWeaveRules.NON_LETHAL_FLOOR - 0.01F,
                "A HUMILIS weave floors the weaver");
        helper.assertTrue(s.loom.isPaused() || pausedSoon(helper, s), "Blacking out pauses the weave");
        s.weaver.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void otherAttackersAreNotFloored(GameTestHelper helper) {
        var victim = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        victim.setHealth(6.0F);
        victim.getPersistentData().putLong("hemomancy:weave_harm_until", helper.getLevel().getGameTime() + 100);
        victim.getPersistentData().putBoolean("hemomancy:weave_lethal", false);
        var zombie = new net.minecraft.world.entity.monster.Zombie(helper.getLevel());
        victim.hurt(helper.getLevel().damageSources().mobAttack(zombie), 20.0F);
        helper.assertTrue(victim.isDeadOrDying(), "A zombie near a low-rank weave still kills");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void aHelperStrainsAndHooksTheWeaversThreads(GameTestHelper helper) {
        Setup s = setup(helper, 4);
        FrayedThreadEntity thread = looseThread(helper, s);
        var ally = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        ally.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,
                new ItemStack(com.vincenthuto.hemomancy.common.init.ItemInit.living_staff.get()));
        float before = thread.strain();
        thread.hurt(helper.getLevel().damageSources().playerAttack(ally), 4.0F);
        helper.assertTrue(thread.strain() > before, "A helper's staff hit strains the thread");
        thread.addStrain(1f);
        helper.assertTrue(thread.tryHook(ally), "A helper may hook a slack thread home");
        s.weaver.discard();
        helper.succeed();
    }
```

Add these helpers to the class:
- `setupBloodShot(helper)`: mirrors `setup`, but with a hematic memory, the Blood Shot recipe's catalyst and its enzyme. Read `memory_weaving/memory_blood_shot.json` for the catalyst and enzyme, and pass the recipe id `memory_weaving/memory_blood_shot`.
- `withWeaver(helper, s, uuid)`: returns `s.loom.saveWithoutMetadata(...)` with `tag.putUUID("weaver", uuid)`.
- `pausedSoon(helper, s)`: ticks the loom once with `tick(helper, s, 1)` and returns `s.loom.isPaused()`.

If `ItemInit.living_staff` is named differently, find it with `grep -n "LivingStaffItem::new\|new LivingStaffItem" src/main/java/com/vincenthuto/hemomancy/common/init/ItemInit.java`.

- [ ] **Step 2: Run to verify it fails**

Run: `./gradlew.bat runSomaticLoomGameTestServer --console=plain`
Expected: `lowRankThreadsBlackOutInsteadOfKilling` fails (the victim dies). The other two pass, since the helper behaviour is implemented in Task 3.

- [ ] **Step 3: Write `WeaveDamageEvents`**

```java
package com.vincenthuto.hemomancy.common.entity.weave;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticLoomBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticWeaveRules;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * Low-rank weaves cannot kill: harm from a thread, or the fire, frost and poison it just left, stops at the
 * floor and blacks the weaver out instead, pausing the weave. Other attackers are untouched.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class WeaveDamageEvents {
	private WeaveDamageEvents() {
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onIncomingDamage(LivingIncomingDamageEvent event) {
		if (!(event.getEntity() instanceof Player player) || player.level().isClientSide) return;
		CompoundTag data = player.getPersistentData();
		if (data.getLong("hemomancy:weave_harm_until") < player.level().getGameTime()
				|| data.getBoolean("hemomancy:weave_lethal") || !fromTheWeave(event.getSource())) return;
		float health = player.getHealth();
		if (!SomaticWeaveRules.blacksOut(health, event.getAmount())) return;
		event.setAmount(SomaticWeaveRules.flooredDamage(health, event.getAmount()));
		player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 0));
		player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 30, 0));
		BlockPos loomPos = BlockPos.of(data.getLong("hemomancy:weave_loom"));
		if (player.level().getBlockEntity(loomPos) instanceof SomaticLoomBlockEntity loom) {
			loom.blackout();
			Vec3 away = player.position().subtract(Vec3.atCenterOf(loomPos)).multiply(1, 0, 1);
			if (away.lengthSqr() > 1.0E-4D) {
				player.setDeltaMovement(away.normalize().scale(0.6D).add(0, 0.3D, 0));
				player.hurtMarked = true;
			}
		}
		data.remove("hemomancy:weave_harm_until");
	}

	private static boolean fromTheWeave(DamageSource source) {
		Entity attacker = source.getEntity();
		if (attacker instanceof FrayedThreadEntity) return true;
		if (attacker != null) return false;
		return source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypeTags.IS_FREEZING)
				|| source.is(DamageTypes.MAGIC) || source.is(DamageTypes.INDIRECT_MAGIC);
	}
}
```

The blackout happens through `loom.blackout()`, which sets `pausedTicks`. The next server tick pauses the weave and recalls the threads. `pausedSoon` covers that one-tick lag.

- [ ] **Step 4: Run to verify it passes**

Run: `./gradlew.bat runSomaticLoomGameTestServer --console=plain`
Expected: `All 14 required tests passed`.

---

### Task 6: Thread rendering

**Files:**
- Create: `client/render/world/ThreadKnotGeometry.java`
- Create: `client/render/entity/weave/FrayedThreadRenderer.java`, `ThreadBoltRenderer.java`, `ThreadHazardRenderer.java`
- Modify: `client/event/ClientEvents.java` (swap the three `NoopRenderer`s)
- Modify: `test/.../MemoryWeavingRedesignSourceTest.java`

**Interfaces:**
- Consumes: the entity getters from Tasks 3 and 4, and `SomaticLoomFrame` / `SomaticLoomGeometry.ECHO_*`.
- Produces:
  - `ThreadKnotGeometry.knot(VertexConsumer, Matrix4f, Vector3f right, Vector3f up, Vector3f at, float[] colour, float time, float seed, float scale, float alpha)`
  - `ThreadKnotGeometry.tails(VertexConsumer, Matrix4f, Vec3 eye, Vector3f knot, Vec3 motion, float[] colour, float time, float seed, float alpha)`
  - `ThreadKnotGeometry.tether(VertexConsumer, Matrix4f, Vec3 eye, Vector3f from, Vector3f to, float[] colour, float time, float seed, float alpha)`
  - `ThreadKnotGeometry.vivid(ParticleColor) -> float[]`

All are moved from the deleted `SomaticLoomRenderer` orb helpers. The `alpha` and `scale` parameters are new multipliers.

- [ ] **Step 1: Write the failing source contract**

In `MemoryWeavingRedesignSourceTest.main`, after the renderer reads:

```java
		String threadRenderer = read("src/main/java/com/vincenthuto/hemomancy/client/render/entity/weave/FrayedThreadRenderer.java");
		assertContains("frayed threads draw as knotted thread-ends", threadRenderer, "ThreadKnotGeometry.knot(");
		assertContains("frayed threads stay tethered to the echo", threadRenderer, "ThreadKnotGeometry.tether(");
		assertContains("frayed threads telegraph their attacks", threadRenderer, "thread.windup()");
		assertContains("slack threads dim and sink", threadRenderer, "thread.isSlack()");
		assertDoesNotContain("thread visuals stay custom", threadRenderer, "ParticleTypes.");
```

- [ ] **Step 2: Run to verify it fails**

```powershell
./gradlew.bat compileTestJava --console=plain -q
java -cp "build/classes/java/test;$gson" com.vincenthuto.hemomancy.common.recipe.MemoryWeavingRedesignSourceTest
```

Expected: `NoSuchFileException` for `FrayedThreadRenderer.java`.

- [ ] **Step 3: Create `ThreadKnotGeometry`**

Move these methods verbatim from the 2026-10-11 `SomaticLoomRenderer` (the "loose thread-ends" section that Task 3 deleted; recover it with `git diff` or the scratchpad's `orbs_tail.java`): `drawTether` → `tether`, `drawFrayedTails` → `tails`, `drawKnot` → `knot`, and `disc`, `arc`, `onPlane`, `ribbonSegment`, `quad`, `emitQuad`, plus `vivid`, `mix` and `HOT` from the loom renderer. Make the class `public final` with public static entry points.

Add an `alpha` multiplier applied to every emitted alpha, and a `scale` multiplier on the knot radii (0.06, 0.2, 0.088 and so on). Change `SomaticLoomRenderer` to call `ThreadKnotGeometry.vivid` / `mix` instead of its private copies, and delete those copies.

- [ ] **Step 4: Create `FrayedThreadRenderer`**

```java
package com.vincenthuto.hemomancy.client.render.entity.weave;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.render.world.ThreadKnotGeometry;
import com.vincenthuto.hemomancy.common.block.harbinger.crafting.SomaticLoomBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.crafting.SomaticLoomFrame;
import com.vincenthuto.hemomancy.common.block.harbinger.crafting.SomaticLoomGeometry;
import com.vincenthuto.hemomancy.common.entity.weave.FrayedThreadEntity;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/** A frayed thread: a hostile knot of its tendency trailing fibres, tethered back to the loom's echo. */
public class FrayedThreadRenderer extends EntityRenderer<FrayedThreadEntity> {
	public FrayedThreadRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public void render(FrayedThreadEntity thread, float yaw, float partialTicks, PoseStack stack, MultiBufferSource buffer, int light) {
		var camera = Minecraft.getInstance().gameRenderer.getMainCamera();
		Vec3 origin = thread.getPosition(partialTicks);
		Vec3 eye = camera.getPosition().subtract(origin);
		Vector3f right = new Vector3f(camera.getLeftVector()).negate();
		Vector3f up = new Vector3f(camera.getUpVector());
		float time = thread.tickCount + partialTicks;
		float seed = (thread.getId() * 0.618f) % 1f;
		float[] colour = ThreadKnotGeometry.vivid(thread.tendency().getColor());
		float alpha = thread.isFaded() ? 0.15f : thread.isSlack() ? 0.4f : 1f;
		// A windup swells the knot and flashes it hot before the strike.
		float tell = thread.windup() > 0 ? 1f + 0.6f * Mth.sin((20 - thread.windup()) * 0.6f) * 0.5f + 0.3f : 1f;
		VertexConsumer vc = buffer.getBuffer(RenderTypeInit.LOOM_EFFECT);
		Matrix4f mat = stack.last().pose();
		Vector3f knot = new Vector3f(0, thread.getBbHeight() / 2, 0);
		Vec3 echo = echoAnchor(thread).subtract(origin);
		ThreadKnotGeometry.tether(vc, mat, eye, knot, new Vector3f((float) echo.x, (float) echo.y, (float) echo.z),
				colour, time, seed, alpha * 0.7f);
		ThreadKnotGeometry.tails(vc, mat, eye, knot, thread.getDeltaMovement(), colour, time, seed, alpha);
		ThreadKnotGeometry.knot(vc, mat, right, up, knot, thread.windup() > 0 ? ThreadKnotGeometry.hot(colour) : colour,
				time, seed, (thread.isSlack() ? 0.7f : 1.4f) * tell, alpha);
	}

	private static Vec3 echoAnchor(FrayedThreadEntity thread) {
		BlockState state = thread.level().getBlockState(thread.loomPos());
		Direction facing = state.hasProperty(SomaticLoomBlock.FACING) ? state.getValue(SomaticLoomBlock.FACING) : Direction.NORTH;
		return SomaticLoomFrame.toWorld(thread.loomPos(), facing, SomaticLoomGeometry.ECHO_X, SomaticLoomGeometry.ECHO_Y,
				SomaticLoomGeometry.ECHO_Z);
	}

	@Override
	public ResourceLocation getTextureLocation(FrayedThreadEntity entity) {
		return TextureAtlas.LOCATION_BLOCKS;
	}
}
```

Add `public static float[] hot(float[] colour) { return mix(colour, HOT, 0.6f); }` to `ThreadKnotGeometry`.

- [ ] **Step 5: Create the bolt and hazard renderers**

`ThreadBoltRenderer` draws a small knot (`ThreadKnotGeometry.knot(..., scale 0.6f, alpha 1f)`) plus a short `tails` streak against its motion, in the bolt's tendency colour, using the same camera setup as above. `ThreadHazardRenderer` draws a faint camera-independent ground ring: a flat octagonal ring of `LOOM_EFFECT` quads at `y + 0.02` with radius `radius()`, colour `vivid(tendency)` at alpha 0.25 pulsing with `0.15 * sin(time * 0.2)`. The hazard's client tick already emits its particles.

Register all three in `ClientEvents.renderEntities`, replacing the `NoopRenderer` lines:

```java
		event.registerEntityRenderer(EntityInit.frayed_thread.get(), FrayedThreadRenderer::new);
		event.registerEntityRenderer(EntityInit.thread_bolt.get(), ThreadBoltRenderer::new);
		event.registerEntityRenderer(EntityInit.thread_hazard.get(), ThreadHazardRenderer::new);
```

- [ ] **Step 6: Run the contracts**

```powershell
./gradlew.bat compileJava compileTestJava --console=plain -q
java -cp "build/classes/java/test;$gson" com.vincenthuto.hemomancy.common.recipe.MemoryWeavingRedesignSourceTest
java -cp "build/classes/java/test;$gson" com.vincenthuto.hemomancy.common.block.ModelFirstTileRendererResourceTest
```

Expected: both exit 0.

---

### Task 7: Vial stock and spin-up rendering

**Files:**
- Modify: `common/tile/harbinger/crafting/SomaticLoomBlockEntity.java` (client display lerp fields)
- Modify: `client/render/tile/harbinger/crafting/SomaticLoomRenderer.java` (`renderCops` reads spun; new `renderVials`)
- Modify: `client/render/tile/harbinger/crafting/SomaticLoomEffectRules.java` and its test (`vialFill`, `spunFill`)
- Modify: `client/event/ClientEvents.java` (register vial parts)

**Interfaces:**
- Consumes: `getSpun`, `getStoredEnzyme`, `SomaticLoomGeometry.VIAL_FLOOR` / `VIAL_DEPTH`, and the part models from Task 2.
- Produces:
  - `SomaticLoomBlockEntity.displayedSpun(EnumBloodTendency) -> float` and `displayedStock(EnumBloodTendency) -> float` (client-smoothed);
  - `SomaticLoomEffectRules.vialFill(float stock, int capacity) -> float` and `spunFill(float spun) -> float`.

- [ ] **Step 1: Write the failing rules tests**

Add to `SomaticLoomEffectRulesTest`:

```java
	@Test
	void vialsShowStockAndPirnsShowWhatIsSpunForTheWeave() {
		assertEquals(0f, SomaticLoomEffectRules.vialFill(0f, 64), 1e-6);
		assertEquals(0.5f, SomaticLoomEffectRules.vialFill(16f, 64), 1e-6);
		assertEquals(1f, SomaticLoomEffectRules.vialFill(64f, 64), 1e-6);
		assertEquals(0f, SomaticLoomEffectRules.spunFill(0f), 1e-6);
		assertEquals(0.5f, SomaticLoomEffectRules.spunFill(4f), 1e-6);
		assertEquals(1f, SomaticLoomEffectRules.spunFill(12f), 1e-6);
	}
```

- [ ] **Step 2: Run to verify it fails**

Run: `./gradlew.bat test --tests "*SomaticLoomEffectRulesTest" --console=plain`
Expected: compile failure (`vialFill` missing).

- [ ] **Step 3: Implement the rules**

```java
    /** Vial liquid height for stored stock: square-root, so a weave's few enzymes still show against 64. */
    public static float vialFill(float stock, int capacity) {
        return (float) Math.sqrt(fill(stock, capacity));
    }

    /** Pirn thread height for enzyme spun for the current weave; a tendency never needs more than 8. */
    public static float spunFill(float spun) {
        return (float) fill(spun, 8);
    }
```

`fill` takes doubles; floats widen. Remove `copFill` and its test only if nothing else uses it. After this task the renderer uses `spunFill` for cops, so delete `copFill` and its test `pirnsShowSmallStocksAndFillAtCapacity`.

- [ ] **Step 4: Add client smoothing to the block entity**

Fields (client-only use, not saved):

```java
	private final float[] shownSpun = new float[EnumBloodTendency.values().length];
	private final float[] shownStock = new float[EnumBloodTendency.values().length];
```

In `clientTick`, before the weave counters:

```java
		for (EnumBloodTendency tend : EnumBloodTendency.values()) {
			int i = tend.ordinal();
			// Payment visibly draws liquid up into thread: both ease toward their synced values.
			self.shownSpun[i] += (self.getSpun(tend) - self.shownSpun[i]) * 0.08F;
			self.shownStock[i] += (self.getStoredEnzyme(tend) - self.shownStock[i]) * 0.08F;
		}
```

Getters: `public float displayedSpun(EnumBloodTendency t) { return shownSpun[t.ordinal()]; }` and the same for `displayedStock`.

- [ ] **Step 5: Render the vials and the spun cops**

In `SomaticLoomRenderer`:
- register vial parts: `PART_MODELS` gains `"vial_" + name` for each tendency (build both lists in `cops()`, renamed `parts()`);
- change `renderCops` to use `SomaticLoomEffectRules.spunFill(te.displayedSpun(tendency))`;
- add `renderVials`, called right after `renderCops`, which draws each `vial_<t>` part translucent, scaled from `VIAL_FLOOR` by `VIAL_DEPTH * vialFill(te.displayedStock(t), SomaticLoomBlockEntity.ENZYME_CAPACITY)`, through `buffer.getBuffer(RenderType.entityTranslucentCull(TextureAtlas.LOCATION_BLOCKS))`.

`ClientEvents` already registers every `SomaticLoomRenderer.PART_MODELS` entry, so the vials are picked up automatically.

- [ ] **Step 6: Run the tests and contracts**

```powershell
./gradlew.bat test --tests "*SomaticLoomEffectRulesTest" --console=plain
./gradlew.bat compileJava compileTestJava --console=plain -q
java -cp "build/classes/java/test;$gson" com.vincenthuto.hemomancy.common.block.ModelFirstTileRendererResourceTest
```

Expected: all pass. In `ModelFirstTileRendererResourceTest`, add `"renderVials(te, stack"` to the loom renderer hook list before running.

---

### Task 8: The echo, its cast registry and the climax

**Files:**
- Create: `client/render/tile/harbinger/crafting/echo/EchoCast.java`, `EchoCasts.java`, `SchoolGestureEchoCast.java`
- Create: `client/render/tile/harbinger/crafting/SomaticLoomEcho.java`
- Modify: `client/render/tile/harbinger/crafting/SomaticLoomRenderer.java` (construct and draw the echo; the memory item rides into the climax)
- Create: `src/test/resources/echo_cast_pending.txt`
- Test: `src/test/java/com/vincenthuto/hemomancy/client/render/tile/harbinger/crafting/echo/EchoCastCoverageTest.java`

**Interfaces:**
- Consumes: `SomaticLoomBlockEntity` getters (Task 3), `SomaticWeaveRules.LOOP_TICKS` / `RELEASE_TICK` / `CLIMAX_TICKS`, `CastingClips.get(EnumBloodTendency, EnumManipulationRank).sample(CastPhase, float)`, `CastingPoseModifiers.apply(...)`, `CastPresentation.forId(String)`, `HarbingerMnemonistModel.LAYER_LOCATION`.
- Produces:
  - `EchoCast` (`void release(EchoContext)`);
  - `EchoContext(ClientLevel level, Vec3 hands, Vec3 forward, float scale, BloodManipulation manipulation)`;
  - `EchoCasts.forManipulation(String name) -> EchoCast`, `EchoCasts.register(String, EchoCast)`, `EchoCasts.registered() -> Set<String>`.

  Plan 3 registers real casts through these.

- [ ] **Step 1: Write the failing coverage test and pending list**

Generate the pending list from `ManipulationInit`:

```bash
python -c "import re;s=open('src/main/java/com/vincenthuto/hemomancy/common/init/ManipulationInit.java',encoding='utf-8').read();print('\n'.join(sorted(set(re.findall(r'MANIPS\.register\(\"([a-z0-9_]+)\"',s)))))" > src/test/resources/echo_cast_pending.txt
wc -l src/test/resources/echo_cast_pending.txt
```

Expected: about 120 lines.

```java
package com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.echo;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Every registered manipulation has a real echo cast or is listed as pending; nothing is in both or stale. */
class EchoCastCoverageTest {
	private static Set<String> names(String source, String regex) {
		Set<String> out = new TreeSet<>();
		Matcher m = Pattern.compile(regex).matcher(source);
		while (m.find()) out.add(m.group(1));
		return out;
	}

	@Test
	void everyManipulationHasAnEchoCastOrIsPending() throws IOException {
		Set<String> manipulations = names(Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/init/ManipulationInit.java")), "MANIPS\\.register\\(\"([a-z0-9_]+)\"");
		Set<String> cast = names(Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/client/render/tile/harbinger/crafting/echo/EchoCasts.java")),
				"register\\(\"([a-z0-9_]+)\"");
		Set<String> pending = new TreeSet<>(Files.readAllLines(Path.of("src/test/resources/echo_cast_pending.txt")).stream()
				.map(String::strip).filter(s -> !s.isEmpty()).toList());
		Set<String> missing = new TreeSet<>(manipulations);
		missing.removeAll(cast);
		missing.removeAll(pending);
		assertTrue(missing.isEmpty(), "Manipulations with neither an echo cast nor a pending entry: " + missing);
		Set<String> both = new HashSet<>(cast);
		both.retainAll(pending);
		assertTrue(both.isEmpty(), "Remove from echo_cast_pending.txt now that they have casts: " + both);
		Set<String> stale = new TreeSet<>(pending);
		stale.removeAll(manipulations);
		assertTrue(stale.isEmpty(), "Pending entries that are not manipulations: " + stale);
	}
}
```

- [ ] **Step 2: Run to verify it fails**

Run: `./gradlew.bat test --tests "*EchoCastCoverageTest" --console=plain`
Expected: `NoSuchFileException` for `EchoCasts.java`.

- [ ] **Step 3: Write the cast registry and fallback**

```java
package com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.echo;

import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;

/** One memory's cast, replayed by the loom's echo at the release of each loop. Visual only. */
public interface EchoCast {
	record EchoContext(ClientLevel level, Vec3 hands, Vec3 forward, float scale, BloodManipulation manipulation) {}

	void release(EchoContext context);
}
```

```java
package com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.echo;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Echo casts by manipulation name. Memories without a real cast fall back to their school's gesture; the
 * pending list in src/test/resources/echo_cast_pending.txt tracks which those are.
 */
public final class EchoCasts {
	private static final Map<String, EchoCast> CASTS = new HashMap<>();
	private static final EchoCast FALLBACK = new SchoolGestureEchoCast();

	static {
		// Real casts are registered here, one line each: register("blood_shot", new BloodShotEcho());
	}

	private EchoCasts() {
	}

	static void register(String name, EchoCast cast) {
		CASTS.put(name, cast);
	}

	public static EchoCast forManipulation(String name) {
		return CASTS.getOrDefault(name, FALLBACK);
	}

	public static Set<String> registered() {
		return Set.copyOf(CASTS.keySet());
	}
}
```

```java
package com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.echo;

import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import net.minecraft.world.phys.Vec3;

/** Until a memory has its real cast: a burst of its school's colour from the echo's hands along its gaze. */
final class SchoolGestureEchoCast implements EchoCast {
	@Override
	public void release(EchoContext context) {
		var color = context.manipulation().getTend().getColor();
		Vec3 hands = context.hands();
		for (int i = 0; i < 12; i++) {
			Vec3 v = context.forward().scale(0.08 * context.scale()).add(
					(context.level().random.nextDouble() - 0.5) * 0.04, (context.level().random.nextDouble() - 0.3) * 0.04,
					(context.level().random.nextDouble() - 0.5) * 0.04);
			context.level().addParticle(HemoParticleData.glow(color), hands.x, hands.y, hands.z, v.x, v.y, v.z);
		}
		for (int i = 0; i < 6; i++)
			context.level().addParticle(HemoParticleData.ember(color, 0.06F * context.scale(), 0.9F, 18),
					hands.x, hands.y, hands.z, context.forward().x * 0.05, 0.02, context.forward().z * 0.05);
	}
}
```

- [ ] **Step 4: Run to verify the coverage test passes**

Run: `./gradlew.bat test --tests "*EchoCastCoverageTest" --console=plain`
Expected: PASS. Every manipulation is pending.

- [ ] **Step 5: Write `SomaticLoomEcho`**

It needs to:
- render the Mnemonist model's part tree as a translucent tinted figure on the warp;
- pose it with the casting clips on the weave loop;
- flicker one limb per loose thread;
- solidify as threads are woven;
- fire the echo cast once per loop at the release;
- grow, turn to the weaver, cast and sink into the fell during the climax.

```java
package com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.npc.HarbingerMnemonistModel;
import com.vincenthuto.hemomancy.client.player.CastingClips;
import com.vincenthuto.hemomancy.client.player.CastingPose;
import com.vincenthuto.hemomancy.client.player.CastingPoseModifiers;
import com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.echo.EchoCast;
import com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.echo.EchoCasts;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.animation.CastPhase;
import com.vincenthuto.hemomancy.common.manipulation.animation.CastPresentation;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticLoomBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticWeaveRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import static com.vincenthuto.hemomancy.common.block.harbinger.crafting.SomaticLoomGeometry.*;

/**
 * The memory's echo: a past Harbinger, translucent and colour-shifted, standing on the warp and casting the
 * memory on loop. Each loose thread is a limb it has lost; each woven thread makes it more solid. At the climax
 * it grows whole, turns to the weaver, casts once more and sinks into the fell.
 */
final class SomaticLoomEcho {
	private static final ResourceLocation TEXTURE = Hemomancy.rloc(
			"textures/entity/npc/harbinger/harbinger_mnemonist/harbinger_mnemonist.png");
	private static final float BASE_SCALE = 0.35f;
	private final ModelPart whole;
	private final ModelPart head, body, rightArm, leftArm, rightLeg, leftLeg;
	private final ModelPart[] frayOrder;

	SomaticLoomEcho(BlockEntityRendererProvider.Context context) {
		ModelPart root = context.bakeLayer(HarbingerMnemonistModel.LAYER_LOCATION);
		whole = root.getChild("whole");
		head = whole.getChild("head");
		body = whole.getChild("body");
		rightArm = whole.getChild("rightArm");
		leftArm = whole.getChild("leftArm");
		rightLeg = whole.getChild("rightLeg");
		leftLeg = whole.getChild("leftLeg");
		frayOrder = new ModelPart[]{rightArm, leftArm, rightLeg, leftLeg};
	}

	/** Draws in the loom's model frame (already turned to its facing); `stack` is in block units. */
	void render(SomaticLoomBlockEntity te, float partialTicks, PoseStack stack, MultiBufferSource buffer) {
		BloodManipulation manip = te.getWeaveManipulation();
		if (manip == null || !te.isWeaving()) return;
		boolean climax = te.isInClimax();
		float loop = climax ? 0 : (te.getWeaveTicks() + partialTicks) % SomaticWeaveRules.LOOP_TICKS;
		float c = climax ? Mth.clamp((te.getClimaxTicks() + partialTicks) / SomaticWeaveRules.CLIMAX_TICKS, 0, 1) : 0;
		long total = te.getWeaveThreads().size();
		long woven = te.getWeaveThreads().stream().filter(t -> t.state() == SomaticLoomBlockEntity.WeaveThread.State.WOVEN).count();
		long loose = te.getWeaveThreads().stream().filter(t -> t.state() == SomaticLoomBlockEntity.WeaveThread.State.LOOSE).count();

		pose(manip, climax ? climaxPhase(c) : loopPhase(loop), climax ? climaxTime(c) : loopTime(loop));
		float scale = BASE_SCALE + (climax ? Mth.clamp(c / 0.3f, 0, 1) * (1f - BASE_SCALE) : 0);
		float sink = climax && c > 0.7f ? (c - 0.7f) / 0.3f : 0;
		float alpha = climax ? 0.95f * (1 - sink) : 0.35f + 0.45f * (total == 0 ? 0 : woven / (float) total);
		if (te.isPaused()) alpha *= 0.5f;
		var colour = manip.getTend().getColor();
		int rgb = climax ? (colour.getRed() << 16 | colour.getGreen() << 8 | colour.getBlue()) : 0xB8A8A8;

		stack.pushPose();
		stack.translate(ECHO_X / 16f, ECHO_Y / 16f - sink * 0.6f, ECHO_Z / 16f);
		if (climax) stack.mulPose(Axis.YP.rotationDegrees(turnTowardWeaver(te, c)));
		stack.scale(scale * (1 - sink * 0.8f), -scale * (1 - sink * 0.8f), -scale * (1 - sink * 0.8f));
		stack.translate(0, -1.5f, 0);
		var vc = buffer.getBuffer(RenderType.entityTranslucentEmissive(TEXTURE));
		float time = Minecraft.getInstance().level == null ? 0 : Minecraft.getInstance().level.getGameTime() + partialTicks;
		for (int i = 0; i < frayOrder.length; i++) frayOrder[i].visible = i >= loose;
		whole.render(stack, vc, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, argb(alpha, rgb));
		for (int i = 0; i < Math.min(loose, frayOrder.length); i++) {
			// A limb torn away as a thread flickers where it was.
			ModelPart limb = frayOrder[i];
			limb.visible = true;
			float flicker = 0.12f + 0.18f * Math.max(0, Mth.sin(time * 0.9f + i * 1.7f));
			whole.translateAndRotate(stack);
			limb.render(stack, vc, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, argb(alpha * flicker, rgb));
		}
		stack.popPose();

		fireRelease(te, manip, climax, loop, c, scale);
	}

	private void pose(BloodManipulation manip, CastPhase phase, float t) {
		for (ModelPart part : new ModelPart[]{head, body, rightArm, leftArm, rightLeg, leftLeg}) part.resetPose();
		CastPresentation presentation = CastPresentation.forId(manip.getName());
		CastingPose pose = CastingPoseModifiers.apply(CastingClips.get(manip.getTend(), manip.getRank()).sample(phase, t),
				presentation.purpose(), presentation.style(), phase, t, false);
		apply(head, pose.head());
		apply(body, pose.body());
		apply(rightArm, pose.rightArm());
		apply(leftArm, pose.leftArm());
		apply(rightLeg, pose.rightLeg());
		apply(leftLeg, pose.leftLeg());
	}

	private static void apply(ModelPart part, CastingPose.Rotation r) {
		part.xRot += r.x();
		part.yRot += r.y();
		part.zRot += r.z();
	}

	/** Loop: opening 0-6, hold to the release at 40, release 40-46, recovery 46-60, rest. */
	private static CastPhase loopPhase(float loop) {
		if (loop < 6) return CastPhase.OPENING;
		if (loop < SomaticWeaveRules.RELEASE_TICK) return CastPhase.HOLD;
		if (loop < SomaticWeaveRules.RELEASE_TICK + 6) return CastPhase.RELEASE;
		return CastPhase.RECOVERY;
	}

	private static float loopTime(float loop) {
		if (loop < 6) return loop;
		if (loop < SomaticWeaveRules.RELEASE_TICK) return loop - 6;
		if (loop < SomaticWeaveRules.RELEASE_TICK + 6) return loop - SomaticWeaveRules.RELEASE_TICK;
		return Math.min(14, loop - SomaticWeaveRules.RELEASE_TICK - 6);
	}

	private static CastPhase climaxPhase(float c) {
		return c < 0.35f ? CastPhase.OPENING : c < 0.45f ? CastPhase.RELEASE : CastPhase.RECOVERY;
	}

	private static float climaxTime(float c) {
		return c < 0.35f ? Math.min(6, c / 0.35f * 6) : c < 0.45f ? (c - 0.35f) / 0.1f * 6 : Math.min(14, (c - 0.45f) * 30);
	}

	private static float turnTowardWeaver(SomaticLoomBlockEntity te, float c) {
		var level = Minecraft.getInstance().level;
		var weaver = level == null || te.getWeaver() == null ? null : level.getPlayerByUUID(te.getWeaver());
		if (weaver == null) return 0;
		Vec3 echo = te.echoAnchor();
		float target = (float) Math.toDegrees(Math.atan2(weaver.getX() - echo.x, weaver.getZ() - echo.z));
		return Mth.clamp(c / 0.3f, 0, 1) * target;
	}

	private void fireRelease(SomaticLoomBlockEntity te, BloodManipulation manip, boolean climax, float loop, float c, float scale) {
		var level = Minecraft.getInstance().level;
		if (level == null) return;
		long beat = climax ? -1 : (long) ((te.getWeaveTicks()) / SomaticWeaveRules.LOOP_TICKS);
		boolean due = climax ? c >= 0.35f && !te.echoClimaxFired() : loop >= SomaticWeaveRules.RELEASE_TICK && te.echoLastLoopFired() != beat;
		if (!due) return;
		if (climax) te.markEchoClimaxFired();
		else te.markEchoLoopFired(beat);
		Vec3 anchor = te.echoAnchor();
		Vec3 hands = anchor.add(0, 1.2 * scale * 2, 0);
		EchoCasts.forManipulation(manip.getName()).release(new EchoCast.EchoContext(level, hands,
				new Vec3(0, 0, -1).yRot((float) Math.toRadians(-facingDegrees(te))), scale / BASE_SCALE, manip));
	}

	private static float facingDegrees(SomaticLoomBlockEntity te) {
		return switch (te.getBlockState().getValue(com.vincenthuto.hemomancy.common.block.harbinger.crafting.SomaticLoomBlock.FACING)) {
			case EAST -> -90; case SOUTH -> 180; case WEST -> 90; default -> 0;
		};
	}

	private static int argb(float alpha, int rgb) {
		return Mth.clamp((int) (alpha * 255), 0, 255) << 24 | rgb;
	}
}
```

Add client-only bookkeeping to `SomaticLoomBlockEntity`. These fields are not saved:

```java
	private long echoLastLoop = -1;
	private boolean echoClimaxFired;
	public long echoLastLoopFired() { return echoLastLoop; }
	public void markEchoLoopFired(long loop) { echoLastLoop = loop; }
	public boolean echoClimaxFired() { return echoClimaxFired; }
	public void markEchoClimaxFired() { echoClimaxFired = true; }
```

Reset `echoClimaxFired = false` in `clearActiveRitual` and in `beginWeave`.

**Verify before compiling:**
- `ModelPart.resetPose()` exists in 1.21.1 (it does: `ModelPart.resetPose`).
- `getName()` on `BloodManipulation` returns the registry id string used by `CastPresentation.forId`. If `forId` expects a different id, follow `CastingAnimationManager.java:87-89` and use the same value it passes.
- If the Mnemonist part tree renders upside down or offset, adjust the `scale(…, -s, -s)` / `translate(0, -1.5f, 0)` pair the way `LivingEntityRenderer` does (it scales by (-1, -1, 1) and translates by -1.501).

- [ ] **Step 6: Draw the echo from the loom renderer**

In `SomaticLoomRenderer`, add the field `private final SomaticLoomEcho echo;`, initialised in the constructor as `echo = new SomaticLoomEcho(ctx);`. In `render`, inside the loom-frame push, after `renderItems` and before the flat-colour batch:

```java
		echo.render(te, partialTicks, stack, buffer);
```

In `renderItems`, while `te.isInClimax()`, lerp the memory item from `MEMORY_Y` toward the echo's hands for the first 40% of the climax, then hold it in place. This sells "drawn down into the vessel". Hide the result-preview item above the ganglion while weaving, because the echo is now the preview.

Add `"echo.render(te, partialTicks, stack, buffer)"` to `ModelFirstTileRendererResourceTest`'s loom hook list.

- [ ] **Step 7: Compile and run**

```powershell
./gradlew.bat compileJava compileTestJava --console=plain -q
./gradlew.bat test --tests "*EchoCastCoverageTest" --tests "*SomaticLoomEffectRulesTest" --tests "*SomaticWeaveRulesTest" --tests "*SomaticLoomFrameTest" --console=plain
java -cp "build/classes/java/test;$gson" com.vincenthuto.hemomancy.common.block.ModelFirstTileRendererResourceTest
```

Expected: all pass.

---

### Task 9: Teaching, docs, client review and full verification

**Files:**
- Modify: `src/main/resources/assets/hemomancy/lang/en_us.json`:
  - change the Mnemonist line `hemomancy.dialogue.event.mnemonist_woven_vessel_complete` to "…Prepare a Living Staff before charging. The memory's echo will fray into threads that fight back: survive or strike them until they go slack, then use the staff on a slack thread to weave it home.";
  - grep `src/main/resources/data/hemomancy/dialogue_inquiry/mnemonist/hemomancy/somatic_loom.json` and `alchemist/.../somatic_loom.json` for "strand" or "drag", and update them the same way.
- Modify: `docs/HEMOMANCY_REFERENCE.md`:
  - the Somatic Loom row (ritual, vials, echo, threads, the non-lethal floor below SUMMA);
  - §25.7 "Ritual flow" steps 4-7 rewritten to the witnessed weave;
  - the "Client rendering notes" paragraph.
- Modify: `wiki/Harbinger-Path.md`, the strand and weave paragraph (around line 242).
- Modify: `docs/validation/SOMATIC_LOOM_REDESIGN_2026_10_10.md` (append a "Witnessed weave (core)" section with results).
- Modify: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/LoomClientReview.java` (stages: idle, paid with one and with three loose threads, a slack thread, the climax at 30%).

- [ ] **Step 1: Update teaching text and docs** as listed. Keep the tone: the echo is a past Harbinger's memory, not a spirit or god.

- [ ] **Step 2: Extend the client review**

In `LoomClientReview.stage`:
- the weaving loom is ticked until 3 threads are loose;
- one thread is strained to slack;
- a second loom is woven to the climax and left at `climaxTicks = 18` (tick it 18 times after the last hook).

Shots:
- a close three-quarter of the weaving loom with the echo and threads;
- a close-up of a thread mid-windup (tick that thread's windup to 8 by calling `tick()` on it after setting strain to 0);
- the climax loom;
- the vials close-up;
- night versions of the first and third.

- [ ] **Step 3: Run the full verification**

```powershell
./gradlew.bat compileJava compileTestJava compileGameTestJava --console=plain -q
./gradlew.bat test --tests "*SomaticWeaveRulesTest" --tests "*SomaticLoomFrameTest" --tests "*SomaticLoomEffectRulesTest" --tests "*EchoCastCoverageTest" --tests "*SomaticLoomMissingEnzymeFeedbackTest" --console=plain
foreach ($t in 'com.vincenthuto.hemomancy.common.block.ModelFirstTileRendererResourceTest','com.vincenthuto.hemomancy.common.recipe.MemoryWeavingRedesignSourceTest','com.vincenthuto.hemomancy.common.block.WaterloggableDecorativeBlocksTest','com.vincenthuto.hemomancy.client.render.world.MnemonicLowtideParchmentSourceTest') { & java -cp "build/classes/java/test;$gson" $t; "$t exit=$LASTEXITCODE" }
./gradlew.bat runSomaticLoomGameTestServer --console=plain
./gradlew.bat runClinicalGameTestServer --console=plain
./gradlew.bat -I build/loom-world.init.gradle runLoomReviewClient --console=plain
```

Expected:
- every JVM test passes;
- every contract exits 0;
- `All 14 required tests passed` for `somatic_loom_validation`;
- the clinical suite's loom tests pass;
- the captures show the echo on the warp with limbs flickering for loose threads, threads tethered to it, the windup flash, slack threads dimmed, the vials and spun pirns, and the climax echo grown and turned.

Then run `./gradlew.bat alphaCheck`. Report any failures and whether they're in loom code (fix them) or in other sessions' in-progress work (name them; don't fix).

- [ ] **Step 4: Report.** Summarize to the user with the captures. Do not commit.
