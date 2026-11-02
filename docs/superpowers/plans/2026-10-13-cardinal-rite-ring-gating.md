# Cardinal Rite Ring Gating Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Multi-ring Cardinal Rites open ring 0 in Consecration and wake each later ring at a later phase. The phase a ring wakes in cannot end until that ring is fed. Each ring costs 25% more blood per anchor and opens with a bigger moment.

**Architecture:**
- **Server:** `ActiveCardinalRite` gains per-anchor ring ranks, an open-ring high-water mark and an opener counter. All set through `configureRings(...)` at activation, so rites created any other way (tests, old saves) keep today's behaviour.
- **Shared rules:** pure classes `CardinalRiteRingRules` (ranks, cost, schedule) and `RingOpeningMath` (drama scaling).
- **Phase holds:** the phase gates live in `ActiveCardinalRite`; the server tick announces each woken ring with a `RING_OPENED` beat, a sting and blood-cell bursts.
- **Client:** reads synced per-anchor requirements, where 0 means a sleeping anchor. Sleeping sores draw as small closed scars that tear open, jets flare, the floor shock and the world-stage surge scale with the ring.

**Tech Stack:** NeoForge 1.21.1 (21.1.251), Java 21, JUnit 5, NeoForge GameTest.

**Spec:** `docs/superpowers/specs/2026-10-13-cardinal-rite-ring-gating-design.md` (builds on `docs/superpowers/specs/2026-10-13-cardinal-rite-world-escalation-design.md`).

## Global Constraints

- Ring 0 is open from Consecration; one-ring rites play exactly as today.
- Openers, in order: Station projection, each Ordeal wave, each Still interval, the Offering procession. Ring *k* opens on entering opener *k − 1*; leftover rings all open at the last opener; a rite with no openers opens its later rings on entering Inscription.
- Requirement for rank *r*: `round(50 * (1 + 0.25 * r))` mL → 50, 63, 75, 88, 100, 113, 125, 138.
- Older saves (no ring keys) load with every ring open at a flat 50 mL.
- Puppeteer trials, succession and Unstained rites are not gated.
- Hemomancy/HutosLib particles (`HemoParticleData`) only; no vanilla `ParticleTypes`.
- No copper, brass, bronze or silver accents on blood visuals.
- NeoForge payload networking only (`CustomPacketPayload`, `TYPE`, `STREAM_CODEC`).
- Leave all work uncommitted in the working tree (user preference). Each task's "Commit" step is replaced by a ledger line.
- `sounds.json` and `en_us.json` use CRLF line endings; preserve them when editing.

## Review Focus

1. **Multi-ring rites started through real activation in GameTest fixtures.** Journey and station fixtures that fill every anchor up front now meet dormant rings. Fixtures must feed the open rings at each phase, not bypass gating. *(Task 7.)*
2. **Two or more rings waking on the same tick** (the last opener). Every woken ring must be announced once, in order, and the guidance line should name the deepest. *(Tasks 4 and 5.)*
3. **A save made mid-hold** (ring woken but unfed). After loading, the same ring is still open, still unfed and still holding the phase, and it is not announced again. *(Task 2 serialisation test and the Task 7 GameTest.)*
4. **Projection aimed off-circuit during Station projection.** While a woken ring hungers, blood goes to anchors. Otherwise it is swallowed, as today, rather than firing a stray projection. *(Task 4 source test.)*
5. **Instability during a hold.** Sleeping anchors never break, drain or add deficit pressure. A woken ring's empty anchors add the normal deficit pressure, which is capped at 10 outside still intervals. *(Task 2 tests.)*

---

## File Structure

| File | Responsibility |
|---|---|
| `common/rite/CardinalRiteRingRules.java` (new) | Ranks from authored rings, per-rank cost, opener count, rings to open per opener |
| `common/rite/RingOpeningMath.java` (new) | How strongly each woken ring is staged (depth 0..1 → burst, tear, shock, sting, surge) |
| `common/rite/ActiveCardinalRite.java` | Ring state, open-only fill/drain/instability, schedule hook in phase entry, phase gates, NBT |
| `common/rite/CardinalRiteCeremonyRules.java` | Deficit pressure with per-anchor requirements |
| `common/rite/CardinalRiteBoundaryProgress.java` | Arcs only between fed, open anchors |
| `common/rite/CardinalRiteCeremonyDefinition.java` | `anchorBloodCostMl` sums ranked costs |
| `common/rite/CardinalRiteEventKind.java` | `RING_OPENED` |
| `common/rite/CardinalRiteProblems.java` | `RING_HUNGERS` problem |
| `common/init/SoundInit.java`, `sounds.json`, `en_us.json` | `rite.ring_open` sting, subtitle, guide and problem lines |
| `common/network/capa/harbinger/BloodCraftingKeyPressPacket.java` | `configureRings` at activation |
| `common/rite/harbinger/CardinalRiteOrdealEngine.java` | Station hold tick, procession guard, rite deficit pressure |
| `common/rite/harbinger/CardinalRiteInteractionHandler.java` | Station fall-through to anchors, open-only nearest anchor, seal refusal |
| `common/rite/harbinger/HarbingerCardinalRiteEvents.java` | Requirement readers, ring announcements, sync of rings, problem facts |
| `client/data/ActiveRiteClientData.java`, `PacketSyncActiveRites.java` | Open rings and per-anchor requirements on the client |
| `client/render/world/rite/escalation/RingTear.java` (new) | How far a woken anchor has torn open, from the beats |
| `client/render/world/floor/AnchorSores.java` | Scars for sleeping anchors, tear growth, per-anchor fill |
| `client/render/world/rite/CardinalRiteAnchorJetRenderer.java` | Requirement-based full check, flare on waking |
| `client/render/world/floor/FloorRiteResponse.java` | Ring opening shock, scaled |
| `client/render/world/rite/escalation/RiteWorldState.java`, `client/data/CardinalRiteEventClientState.java` | World-stage surge on waking |
| `client/render/world/rite/escalation/RiteGuidance.java` | "The Nth ring wakes" line |
| `client/screen/overlay/CardinalRiteOverlay.java` | Sleeping rings as hollow pips |
| `src/gameTest/.../CardinalRiteRingGatingGameTests.java` (new) | Two-ring rite through an ordeal and a save |

Java paths are under `src/main/java/com/vincenthuto/hemomancy/`, tests under `src/test/java/com/vincenthuto/hemomancy/`, resources under `src/main/resources/assets/hemomancy/`.

---

### Task 1: Ring rules and ring-opening maths

**Files:**
- Create: `common/rite/CardinalRiteRingRules.java`
- Create: `common/rite/RingOpeningMath.java`
- Test: `common/rite/CardinalRiteRingRulesTest.java`, `common/rite/RingOpeningMathTest.java`

**Interfaces:**
- Produces:
  - `CardinalRiteRingRules`:
    - `int[] ranks(List<Integer> authoredRings)` and `int[] ranksOf(List<CardinalRiteCeremonyDefinition.Anchor>)`
    - `int ringCount(int[] ranks)`
    - `int bloodForRank(int rank)`
    - `boolean isOpener(CardinalRitePhase)`
    - `int openerCount(boolean station, int waves, boolean stillIntervals, boolean offerings)`
    - `int ringsToOpen(int index, int openers, int sleeping)`
  - `RingOpeningMath`:
    - `float SCAR_SCALE`
    - `float depth(int rank, int rings)`
    - `int burstCells(float)`, `float tearTicks(float)`, `float tear(float age, float depth)`
    - `float shockScale(float)`, `float shockReach(float)`
    - `float stingPitch(float)`, `float stingVolume(float)`
    - `float fogSurge(float)`

- [ ] **Step 1: Write the failing tests**

`src/test/java/com/vincenthuto/hemomancy/common/rite/CardinalRiteRingRulesTest.java`:

```java
package com.vincenthuto.hemomancy.common.rite;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

final class CardinalRiteRingRulesTest {
	@Test
	void ranksRenumberAuthoredRingsDenselyInnermostFirst() {
		assertArrayEquals(new int[] {0, 0, 1, 1}, CardinalRiteRingRules.ranks(List.of(1, 1, 2, 2)),
				"layout rings start at 1");
		assertArrayEquals(new int[] {1, 0, 1, 0}, CardinalRiteRingRules.ranks(List.of(3, 0, 3, 0)),
				"gaps close and order follows the authored value, not the index");
		assertEquals(2, CardinalRiteRingRules.ringCount(new int[] {1, 0, 1, 0}));
		assertEquals(0, CardinalRiteRingRules.ringCount(new int[0]));
	}

	@Test
	void eachRingAsksAQuarterMoreBloodPerAnchor() {
		int[] expected = {50, 63, 75, 88, 100, 113, 125, 138};
		for (int rank = 0; rank < expected.length; rank++) {
			assertEquals(expected[rank], CardinalRiteRingRules.bloodForRank(rank), "rank " + rank);
		}
		assertEquals(50, CardinalRiteRingRules.bloodForRank(-1));
	}

	@Test
	void openersAreThePhasesAfterTheSealBeforeCulmination() {
		assertTrue(CardinalRiteRingRules.isOpener(CardinalRitePhase.STATION_PROJECTION));
		assertTrue(CardinalRiteRingRules.isOpener(CardinalRitePhase.ORDEAL));
		assertTrue(CardinalRiteRingRules.isOpener(CardinalRitePhase.STILL_INTERVAL));
		assertTrue(CardinalRiteRingRules.isOpener(CardinalRitePhase.OFFERING_PROCESSION));
		assertFalse(CardinalRiteRingRules.isOpener(CardinalRitePhase.INSCRIPTION));
		assertFalse(CardinalRiteRingRules.isOpener(CardinalRitePhase.CULMINATION));

		assertEquals(1, CardinalRiteRingRules.openerCount(true, 0, true, false), "a station rite projects once");
		assertEquals(2, CardinalRiteRingRules.openerCount(true, 0, false, true));
		assertEquals(3, CardinalRiteRingRules.openerCount(false, 3, false, false), "one per wave");
		assertEquals(6, CardinalRiteRingRules.openerCount(false, 3, true, true), "waves, intervals between them, procession");
		assertEquals(1, CardinalRiteRingRules.openerCount(false, 0, true, false), "a lone still interval");
		assertEquals(0, CardinalRiteRingRules.openerCount(false, 0, false, false));
	}

	@Test
	void oneRingWakesPerOpenerAndTheLastOpenerWakesTheRest() {
		assertEquals(1, CardinalRiteRingRules.ringsToOpen(0, 3, 2));
		assertEquals(1, CardinalRiteRingRules.ringsToOpen(1, 3, 1));
		assertEquals(5, CardinalRiteRingRules.ringsToOpen(0, 1, 5), "more rings than openers");
		assertEquals(3, CardinalRiteRingRules.ringsToOpen(4, 2, 3), "an opener entered again past the plan");
		assertEquals(0, CardinalRiteRingRules.ringsToOpen(0, 3, 0), "nothing left asleep");
	}
}
```

`src/test/java/com/vincenthuto/hemomancy/common/rite/RingOpeningMathTest.java`:

```java
package com.vincenthuto.hemomancy.common.rite;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class RingOpeningMathTest {
	@Test
	void depthRunsFromTheFirstLaterRingToTheLast() {
		assertEquals(1.0F, RingOpeningMath.depth(1, 2), 1e-6F, "a two-ring rite's only later ring is also its last");
		assertEquals(0.0F, RingOpeningMath.depth(1, 3), 1e-6F);
		assertEquals(1.0F, RingOpeningMath.depth(2, 3), 1e-6F);
		assertEquals(0.0F, RingOpeningMath.depth(1, 8), 1e-6F);
		assertEquals(1.0F, RingOpeningMath.depth(7, 8), 1e-6F);
		assertTrue(RingOpeningMath.depth(4, 8) > RingOpeningMath.depth(3, 8));
	}

	@Test
	void everyFactorGrowsWithDepth() {
		assertTrue(RingOpeningMath.burstCells(1.0F) > RingOpeningMath.burstCells(0.0F));
		assertTrue(RingOpeningMath.tearTicks(1.0F) > RingOpeningMath.tearTicks(0.0F));
		assertTrue(RingOpeningMath.shockScale(1.0F) > RingOpeningMath.shockScale(0.0F));
		assertTrue(RingOpeningMath.shockReach(1.0F) > RingOpeningMath.shockReach(0.0F));
		assertTrue(RingOpeningMath.stingVolume(1.0F) > RingOpeningMath.stingVolume(0.0F));
		assertTrue(RingOpeningMath.stingPitch(1.0F) < RingOpeningMath.stingPitch(0.0F), "deeper rings sound lower");
		assertTrue(RingOpeningMath.fogSurge(1.0F) > RingOpeningMath.fogSurge(0.0F));
		assertEquals(1.0F, RingOpeningMath.fogSurge(1.0F), 1e-6F, "the last ring surges the world fully");
	}

	@Test
	void aSoreTearsFromItsScarToFullSize() {
		assertEquals(0.0F, RingOpeningMath.tear(0.0F, 0.5F), 1e-6F);
		assertEquals(1.0F, RingOpeningMath.tear(RingOpeningMath.tearTicks(0.5F), 0.5F), 1e-6F);
		assertEquals(1.0F, RingOpeningMath.tear(-1.0F, 0.5F), 1e-6F, "no beat remembered: fully open");
		assertTrue(RingOpeningMath.SCAR_SCALE > 0.0F && RingOpeningMath.SCAR_SCALE < 0.5F);
	}
}
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `./gradlew.bat test --tests "com.vincenthuto.hemomancy.common.rite.CardinalRiteRingRulesTest" --tests "com.vincenthuto.hemomancy.common.rite.RingOpeningMathTest"`
Expected: FAIL (compilation: `CardinalRiteRingRules` and `RingOpeningMath` do not exist).

- [ ] **Step 3: Write the implementation**

`src/main/java/com/vincenthuto/hemomancy/common/rite/CardinalRiteRingRules.java`:

```java
package com.vincenthuto.hemomancy.common.rite;

import java.util.List;
import java.util.TreeSet;

/**
 * How a Cardinal Rite's anchor rings are numbered, priced and woken. Ring 0 is fed in Consecration; each later ring
 * wakes when the sealed rite enters one of its openers, and every ring still asleep wakes at the last.
 */
public final class CardinalRiteRingRules {
	private CardinalRiteRingRules() {
	}

	/** Each anchor's rank: its authored ring value renumbered densely from 0, innermost first. */
	public static int[] ranks(List<Integer> authoredRings) {
		if (authoredRings == null || authoredRings.isEmpty()) return new int[0];
		List<Integer> distinct = List.copyOf(new TreeSet<>(authoredRings));
		int[] ranks = new int[authoredRings.size()];
		for (int i = 0; i < ranks.length; i++) ranks[i] = distinct.indexOf(authoredRings.get(i));
		return ranks;
	}

	public static int[] ranksOf(List<CardinalRiteCeremonyDefinition.Anchor> anchors) {
		return ranks(anchors == null ? List.of()
				: anchors.stream().map(CardinalRiteCeremonyDefinition.Anchor::ring).toList());
	}

	public static int ringCount(int[] ranks) {
		int highest = -1;
		for (int rank : ranks) highest = Math.max(highest, rank);
		return highest + 1;
	}

	/** Blood one anchor of this rank asks for: a quarter more than the ring inside it, rounded half up. */
	public static int bloodForRank(int rank) {
		return (CardinalRiteCeremonyRules.BLOOD_PER_ANCHOR_ML * (4 + Math.max(0, rank)) + 2) / 4;
	}

	/** The phases a later ring can wake in. */
	public static boolean isOpener(CardinalRitePhase phase) {
		return phase == CardinalRitePhase.STATION_PROJECTION || phase == CardinalRitePhase.ORDEAL
				|| phase == CardinalRitePhase.STILL_INTERVAL || phase == CardinalRitePhase.OFFERING_PROCESSION;
	}

	/** How many openers a sealed rite of this shape enters before Culmination. */
	public static int openerCount(boolean station, int waves, boolean stillIntervals, boolean offerings) {
		int procession = offerings ? 1 : 0;
		if (station) return 1 + procession;
		if (waves > 0) return waves + (stillIntervals ? waves - 1 : 0) + procession;
		return (stillIntervals ? 1 : 0) + procession;
	}

	/** Rings to wake on entering opener {@code index}: one each, and every ring still asleep at the last. */
	public static int ringsToOpen(int index, int openers, int sleeping) {
		if (sleeping <= 0 || index < 0) return 0;
		return index >= openers - 1 ? sleeping : 1;
	}
}
```

`src/main/java/com/vincenthuto/hemomancy/common/rite/RingOpeningMath.java`:

```java
package com.vincenthuto.hemomancy.common.rite;

/**
 * How hard each woken ring lands. Every factor rises with the ring's depth: 0 for the first ring that wakes after
 * the Focus's own, 1 for the rite's last. The server plays the sting and bursts from it; the client the tear, the
 * shock and the surge. All ring-opening tuning lives here.
 */
public final class RingOpeningMath {
	/** A sleeping anchor's sore, as a share of its full size: a small closed scar. */
	public static final float SCAR_SCALE = 0.32F;

	private RingOpeningMath() {
	}

	public static float depth(int rank, int rings) {
		if (rings <= 2) return 1.0F;
		return clamp((rank - 1) / (float) (rings - 2));
	}

	public static int burstCells(float depth) {
		return Math.round(lerp(depth, 12.0F, 40.0F));
	}

	public static float tearTicks(float depth) {
		return lerp(depth, 30.0F, 50.0F);
	}

	/** How far a waking sore has torn open, from its scar (0) to full (1); fully open with no beat remembered. */
	public static float tear(float age, float depth) {
		if (age < 0.0F) return 1.0F;
		float t = clamp(age / tearTicks(depth));
		return t * t * (3.0F - 2.0F * t);
	}

	public static float shockScale(float depth) {
		return lerp(depth, 0.7F, 1.4F);
	}

	public static float shockReach(float depth) {
		return lerp(depth, 0.8F, 1.5F);
	}

	public static float stingPitch(float depth) {
		return lerp(depth, 0.9F, 0.55F);
	}

	public static float stingVolume(float depth) {
		return lerp(depth, 1.0F, 1.6F);
	}

	public static float fogSurge(float depth) {
		return lerp(depth, 0.35F, 1.0F);
	}

	private static float lerp(float t, float from, float to) {
		return from + (to - from) * clamp(t);
	}

	private static float clamp(float value) {
		return Math.max(0.0F, Math.min(1.0F, value));
	}
}
```

- [ ] **Step 4: Run the tests to verify they pass**

Run: `./gradlew.bat test --tests "com.vincenthuto.hemomancy.common.rite.CardinalRiteRingRulesTest" --tests "com.vincenthuto.hemomancy.common.rite.RingOpeningMathTest"`
Expected: PASS (7 tests).

- [ ] **Step 5: Ledger** (no commit; user preference)

---

### Task 2: Ring state on the rite

**Files:**
- Modify: `common/rite/ActiveCardinalRite.java`
- Modify: `common/rite/CardinalRiteCeremonyRules.java:89-97`
- Test: `common/rite/CardinalRiteRingStateTest.java` (new)

**Interfaces:**
- Consumes: `CardinalRiteRingRules.ringCount`, `bloodForRank` (Task 1).
- Produces on `ActiveCardinalRite`:
  - Setup and identity: `void configureRings(int[] ranks, boolean stillIntervals)`, `boolean ringsGated()`, `int anchorRank(int)`
  - Ring counts: `int totalAnchorRings()` (rank-based), `int getOpenRings()`, `boolean isAnchorOpen(int)`
  - Requirements: `int anchorRequirementMl(int)`, `int[] anchorRequirements()` (0 while asleep), `int openAnchorCount()`, `int openRequirementMl()`, `int fullRequirementMl()`, `int committedAnchorBloodMl()`
  - Pressure: `int anchorDeficitInstability()`, `int hungryLaterRingAnchors()`
  - Announcements: `int nextRingToAnnounce()`
  - NBT keys `AnchorRanks`, `OpenRings`, `RingOpenersEntered`, `AnnouncedRings`, `StillIntervalsPlanned`.
- Produces `CardinalRiteCeremonyRules.anchorDeficitInstability(int[] bloodMl, int[] requiredMl)`.

- [ ] **Step 1: Write the failing tests**

`src/test/java/com/vincenthuto/hemomancy/common/rite/CardinalRiteRingStateTest.java`:

```java
package com.vincenthuto.hemomancy.common.rite;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

final class CardinalRiteRingStateTest {
	static final ResourceLocation PLAIN = ResourceLocation.parse("hemomancy:cardinal_rite/ring_test");
	static final int[] TWO_RINGS = {0, 0, 0, 0, 1, 1, 1, 1};

	static ActiveCardinalRite rite(int anchors, int waves) {
		return ActiveCardinalRite.interactive(UUID.randomUUID(), BlockPos.ZERO, PLAIN, 1200, 5, 5, false, waves, anchors);
	}

	static ActiveCardinalRite twoRings(int waves) {
		ActiveCardinalRite rite = rite(8, waves);
		rite.configureRings(TWO_RINGS, false);
		return rite;
	}

	@Test
	void anUnconfiguredRiteKeepsEveryRingOpenAtTheFlatCost() {
		ActiveCardinalRite rite = rite(8, 1);

		assertFalse(rite.ringsGated());
		assertEquals(2, rite.getOpenRings());
		assertTrue(rite.isAnchorOpen(5));
		assertEquals(50, rite.anchorRequirementMl(5));
		assertTrue(rite.fillAnchor(5, 50));
	}

	@Test
	void laterRingsSleepUntilWoken() {
		ActiveCardinalRite rite = twoRings(1);

		assertTrue(rite.ringsGated());
		assertEquals(1, rite.getOpenRings());
		assertFalse(rite.isAnchorOpen(4));
		assertFalse(rite.fillAnchor(4, 63), "a sleeping anchor takes no blood");
		assertEquals(0, rite.bloodNeededForAnchor(4));
		assertEquals(0, rite.drainAnchor(4, 5));
		for (int i = 0; i < 4; i++) rite.fillAnchor(i, 50);
		assertTrue(rite.areAnchorsConsecrated(), "only open anchors count");
		assertEquals(1, rite.completedRings());
		assertArrayEquals(new int[] {50, 50, 50, 50, 0, 0, 0, 0}, rite.anchorRequirements());
		assertEquals(63, rite.anchorRequirementMl(4), "the cost is the rank's, asleep or not");
		assertEquals(200, rite.openRequirementMl());
		assertEquals(452, rite.fullRequirementMl());
		assertEquals(200, rite.committedAnchorBloodMl());
	}

	@Test
	void ringsFollowTheAuthoredValueNotTheIndex() {
		ActiveCardinalRite rite = rite(4, 0);
		rite.configureRings(new int[] {1, 0, 1, 0}, false);

		assertTrue(rite.isAnchorOpen(1));
		assertFalse(rite.isAnchorOpen(0));
		assertEquals(2, rite.totalAnchorRings());
		rite.fillAnchor(1, 50);
		rite.fillAnchor(3, 50);
		assertTrue(rite.areAnchorsConsecrated());
	}

	@Test
	void sleepingAnchorsAddNoDeficitAndNeverBreak() {
		ActiveCardinalRite rite = twoRings(1);
		for (int i = 0; i < 4; i++) rite.fillAnchor(i, 50);
		assertEquals(0, rite.anchorDeficitInstability());

		rite.setInstabilityDamagePriority(new int[] {7, 6, 5, 4, 3, 2, 1, 0});
		rite.addInstability(90);
		for (int anchor = 4; anchor < 8; anchor++) {
			assertFalse(rite.isInstabilityDamagedAnchor(anchor), "sleeping anchor " + anchor + " broke");
		}
		assertTrue(rite.getBrokenInstabilityAnchors().stream().allMatch(index -> index < 4));
	}

	@Test
	void deficitPressureReadsEachAnchorsOwnRequirement() {
		assertEquals(0, CardinalRiteCeremonyRules.anchorDeficitInstability(new int[] {63, 0}, new int[] {63, 0}));
		assertEquals(2, CardinalRiteCeremonyRules.anchorDeficitInstability(new int[] {50}, new int[] {63}));
		assertEquals(CardinalRiteCeremonyRules.anchorDeficitInstability(new int[] {20}),
				CardinalRiteCeremonyRules.anchorDeficitInstability(new int[] {20}, null), "flat 50 without requirements");
	}

	@Test
	void ringStateSurvivesASave() {
		ActiveCardinalRite rite = twoRings(1);
		for (int i = 0; i < 3; i++) rite.fillAnchor(i, 50);

		ActiveCardinalRite copy = ActiveCardinalRite.deserialize(rite.serialize());

		assertTrue(copy.ringsGated());
		assertEquals(1, copy.getOpenRings());
		assertFalse(copy.isAnchorOpen(4));
		assertEquals(63, copy.anchorRequirementMl(4));
		assertEquals(-1, copy.nextRingToAnnounce(), "ring 0 is never announced");
	}

	@Test
	void anOlderSaveOpensEveryRingAtTheFlatCost() {
		ActiveCardinalRite copy = ActiveCardinalRite.deserialize(rite(8, 1).serialize());

		assertFalse(copy.ringsGated());
		assertEquals(2, copy.getOpenRings());
		assertEquals(50, copy.anchorRequirementMl(7));
	}

	@Test
	void balancingNeverPoursBloodIntoASleepingRing() {
		ActiveCardinalRite rite = twoRings(1);
		rite.fillAnchor(0, 50);
		rite.fillAnchor(1, 10);
		rite.balanceAnchors();

		int[] blood = rite.getAnchorBloodMl();
		assertEquals(60, blood[0] + blood[1] + blood[2] + blood[3]);
		for (int anchor = 4; anchor < 8; anchor++) assertEquals(0, blood[anchor]);
	}
}
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `./gradlew.bat test --tests "com.vincenthuto.hemomancy.common.rite.CardinalRiteRingStateTest"`
Expected: FAIL (compilation: `configureRings`, `ringsGated`, `isAnchorOpen` … do not exist).

- [ ] **Step 3: Deficit pressure with requirements**

In `common/rite/CardinalRiteCeremonyRules.java`, replace `anchorDeficitInstability(int[] anchorBloodMl)` with:

```java
	public static int anchorDeficitInstability(int[] anchorBloodMl) {
		return anchorDeficitInstability(anchorBloodMl, null);
	}

	/** Pressure from anchors below their own requirement; a requirement of 0 (a sleeping anchor) presses nothing. */
	public static int anchorDeficitInstability(int[] anchorBloodMl, int[] requiredMl) {
		if (anchorBloodMl == null) return 0;
		int pressure = 0;
		for (int i = 0; i < anchorBloodMl.length; i++) {
			int required = requiredMl != null && i < requiredMl.length ? requiredMl[i] : BLOOD_PER_ANCHOR_ML;
			int missing = Math.max(0, required - anchorBloodMl[i]);
			pressure += (missing + 9) / 10;
		}
		return Math.min(10, pressure);
	}
```

- [ ] **Step 4: Ring state in `ActiveCardinalRite`**

Add fields after `private int escalationStage;`:

```java
	/** Each anchor's ring rank, or null for a rite started before ring gating: every ring open at the flat cost. */
	private int[] anchorRanks;
	/** Rings woken so far; only rises. */
	private int openRings;
	/** Opener phases the sealed rite has entered while rings still slept. */
	private int ringOpenersEntered;
	/** Woken rings already announced to the world; ring 0 never is. */
	private int announcedRings;
	/** Whether the recipe holds still intervals, known before the seal records it. */
	private boolean stillIntervalsPlanned;
```

Add methods after `getEscalationTotal()`:

```java
	/** Wakes ring 0 only; later rings wake as the sealed rite enters its openers. Ignored unless one rank per anchor. */
	public void configureRings(int[] ranks, boolean stillIntervals) {
		if (ranks == null || ranks.length == 0 || ranks.length != anchorBloodMl.length) return;
		anchorRanks = ranks.clone();
		openRings = 1;
		announcedRings = 1;
		ringOpenersEntered = 0;
		stillIntervalsPlanned = stillIntervals;
		reconcileInstabilityDamage();
	}

	public boolean ringsGated() {
		return anchorRanks != null;
	}

	public int anchorRank(int anchorIndex) {
		return anchorRanks != null && anchorIndex >= 0 && anchorIndex < anchorRanks.length
				? anchorRanks[anchorIndex] : anchorIndex / CardinalRiteCeremonyRules.ANCHORS_PER_DEGREE;
	}

	public int getOpenRings() {
		return ringsGated() ? openRings : totalAnchorRings();
	}

	public boolean isAnchorOpen(int anchorIndex) {
		return anchorIndex >= 0 && anchorIndex < anchorBloodMl.length && anchorRank(anchorIndex) < getOpenRings();
	}

	/** The blood this anchor asks for, by its rank; the flat cost for a rite started before ring gating. */
	public int anchorRequirementMl(int anchorIndex) {
		return ringsGated() ? CardinalRiteRingRules.bloodForRank(anchorRank(anchorIndex))
				: CardinalRiteCeremonyRules.BLOOD_PER_ANCHOR_ML;
	}

	/** Each anchor's requirement, or 0 while its ring sleeps. */
	public int[] anchorRequirements() {
		int[] required = new int[anchorBloodMl.length];
		for (int i = 0; i < required.length; i++) required[i] = isAnchorOpen(i) ? anchorRequirementMl(i) : 0;
		return required;
	}

	public int openAnchorCount() {
		int open = 0;
		for (int i = 0; i < anchorBloodMl.length; i++) if (isAnchorOpen(i)) open++;
		return open;
	}

	public int openRequirementMl() {
		int total = 0;
		for (int i = 0; i < anchorBloodMl.length; i++) if (isAnchorOpen(i)) total += anchorRequirementMl(i);
		return total;
	}

	public int fullRequirementMl() {
		int total = 0;
		for (int i = 0; i < anchorBloodMl.length; i++) total += anchorRequirementMl(i);
		return total;
	}

	/** Blood standing in the open anchors, each counted up to its requirement. */
	public int committedAnchorBloodMl() {
		int total = 0;
		for (int i = 0; i < anchorBloodMl.length; i++) {
			if (isAnchorOpen(i)) total += Math.clamp(anchorBloodMl[i], 0, anchorRequirementMl(i));
		}
		return total;
	}

	public int anchorDeficitInstability() {
		return CardinalRiteCeremonyRules.anchorDeficitInstability(anchorBloodMl, anchorRequirements());
	}

	/** Open anchors beyond ring 0 still below their requirement. */
	public int hungryLaterRingAnchors() {
		if (!ringsGated()) return 0;
		int hungry = 0;
		for (int i = 0; i < anchorBloodMl.length; i++) {
			if (anchorRank(i) > 0 && bloodNeededForAnchor(i) > 0) hungry++;
		}
		return hungry;
	}

	/** The next woken ring to announce, marking it announced, or -1 when every woken ring has been. */
	public int nextRingToAnnounce() {
		if (!ringsGated() || announcedRings >= openRings) return -1;
		return announcedRings++;
	}
```

Replace these existing methods:

```java
	public boolean fillAnchor(int anchorIndex, int availableBloodMl) {
		if (phase != CardinalRitePhase.CONSECRATION && phase != CardinalRitePhase.ORDEAL
				&& phase != CardinalRitePhase.STILL_INTERVAL) return false;
		if (!isAnchorOpen(anchorIndex) || availableBloodMl <= 0) return false;
		int missing = anchorRequirementMl(anchorIndex) - anchorBloodMl[anchorIndex];
		if (missing <= 0) return false;
		int accepted = Math.min(missing, availableBloodMl);
		anchorBloodMl[anchorIndex] += accepted;
		committedBloodMl += accepted;
		idleTicks = 0;
		return accepted > 0;
	}

	public int bloodNeededForAnchor(int anchorIndex) {
		if (!isAnchorOpen(anchorIndex)) return 0;
		return Math.max(0, anchorRequirementMl(anchorIndex) - anchorBloodMl[anchorIndex]);
	}

	public boolean areAnchorsConsecrated() {
		if (anchorBloodMl.length == 0) return false;
		for (int i = 0; i < anchorBloodMl.length; i++) {
			if (isAnchorOpen(i) && anchorBloodMl[i] < anchorRequirementMl(i)) return false;
		}
		return true;
	}

	public int totalAnchorRings() {
		return anchorRanks != null ? CardinalRiteRingRules.ringCount(anchorRanks)
				: Math.ceilDiv(anchorBloodMl.length, CardinalRiteCeremonyRules.ANCHORS_PER_DEGREE);
	}

	public int completedRings() {
		int completed = 0;
		for (int ring = 0; ring < totalAnchorRings(); ring++) {
			boolean full = ring < getOpenRings();
			for (int i = 0; full && i < anchorBloodMl.length; i++) {
				if (anchorRank(i) == ring && anchorBloodMl[i] < anchorRequirementMl(i)) full = false;
			}
			if (!full) break;
			completed++;
		}
		return completed;
	}
```

The `fillAnchor` phase list is widened in Task 3.

```java
	public int drainAnchor(int anchorIndex, int requestedMl) {
		if (!isAnchorOpen(anchorIndex) || requestedMl <= 0) return 0;
		int drained = Math.min(requestedMl, anchorBloodMl[anchorIndex]);
		anchorBloodMl[anchorIndex] -= drained;
		return drained;
	}

	public void applyAnchorDeficitPressure() {
		int pressure = anchorDeficitInstability();
		if (pressure > instability) addInstability(pressure - instability);
	}

	public void balanceAnchors() {
		for (int ring = 0; ring < getOpenRings(); ring++) {
			int total = 0;
			int count = 0;
			for (int i = 0; i < anchorBloodMl.length; i++) {
				if (anchorRank(i) != ring) continue;
				total += anchorBloodMl[i];
				count++;
			}
			if (count == 0) continue;
			int average = total / count;
			int remainder = total % count;
			int seen = 0;
			for (int i = 0; i < anchorBloodMl.length; i++) {
				if (anchorRank(i) == ring) anchorBloodMl[i] = average + (seen++ < remainder ? 1 : 0);
			}
		}
	}

	private int[] effectiveInstabilityDamagePriority() {
		int[] base;
		if (instabilityDamagePriority.length == anchorBloodMl.length) {
			base = instabilityDamagePriority;
		} else {
			base = new int[anchorBloodMl.length];
			for (int index = 0; index < base.length; index++) base[index] = index;
		}
		return ringsGated() ? java.util.Arrays.stream(base).filter(this::isAnchorOpen).toArray() : base;
	}
```

In `offerInstabilityRepair`, `instabilityAnchorIntegrity`, `threatenedInstabilityAnchor` and `reconcileInstabilityDamage`, replace each `anchorBloodMl.length` passed to `CardinalRiteInstabilityBoundaryRules` (`repairInstabilityAmount`, `flickerProgress`, `brokenAnchorCount`) with `openAnchorCount()`. In `reconcileInstabilityDamage`, extend the first `removeIf` to `index -> index < 0 || index >= anchorBloodMl.length || !isAnchorOpen(index)`.

In `getProgress`, the `CONSECRATION` case becomes:

```java
			case CONSECRATION -> anchorBloodMl.length == 0 ? 0.0D
					: 0.25D * committedBloodMl / (double) Math.max(1, openRequirementMl());
```

In `serialize`, after `tag.putInt("EscalationStage", escalationStage);`:

```java
		if (anchorRanks != null) {
			tag.putIntArray("AnchorRanks", anchorRanks);
			tag.putInt("OpenRings", openRings);
			tag.putInt("RingOpenersEntered", ringOpenersEntered);
			tag.putInt("AnnouncedRings", announcedRings);
			tag.putBoolean("StillIntervalsPlanned", stillIntervalsPlanned);
		}
```

In `deserialize`, immediately after the `BrokenInstabilityAnchors` loop and **before** `rite.reconcileInstabilityDamage();`:

```java
		int[] ranks = tag.getIntArray("AnchorRanks");
		if (tag.contains("AnchorRanks") && ranks.length == rite.anchorBloodMl.length && ranks.length > 0) {
			rite.anchorRanks = ranks;
			int rings = CardinalRiteRingRules.ringCount(ranks);
			rite.openRings = Math.clamp(tag.getInt("OpenRings"), 1, rings);
			rite.ringOpenersEntered = Math.max(0, tag.getInt("RingOpenersEntered"));
			rite.announcedRings = Math.clamp(tag.getInt("AnnouncedRings"), 1, rite.openRings);
			rite.stillIntervalsPlanned = tag.getBoolean("StillIntervalsPlanned");
		}
```

- [ ] **Step 5: Run the tests to verify they pass**

Run: `./gradlew.bat test --tests "com.vincenthuto.hemomancy.common.rite.*"`
Expected: PASS. This includes `CardinalRiteRingStateTest` (8) and the existing `ActiveCardinalRiteCeremonyTest`, `CardinalRiteEscalationTest` and `CardinalRiteCompletionAuditTest`, which are unconfigured rites, so their behaviour is unchanged.

- [ ] **Step 6: Ledger**

---

### Task 3: The opening schedule and the phase holds

**Files:**
- Modify: `common/rite/ActiveCardinalRite.java` (`setPhase`, `holdBeforeFinale`, `fillAnchor`, `upgradeBloodNeeded`, `fillUpgradeCircuit`, `finishOfferingProcession`; new `tryFinishStationProjection`, `ringOpenerCount`)
- Test: `common/rite/CardinalRiteRingScheduleTest.java` (new)

**Interfaces:**
- Consumes: Task 1 `isOpener`, `openerCount`, `ringsToOpen`; Task 2 ring state.
- Produces: `boolean tryFinishStationProjection()`, `int ringOpenerCount()`. Rings wake on phase entry, and `fillAnchor` accepts Inscription, Station projection and Offering procession.

- [ ] **Step 1: Write the failing tests**

`src/test/java/com/vincenthuto/hemomancy/common/rite/CardinalRiteRingScheduleTest.java`:

```java
package com.vincenthuto.hemomancy.common.rite;

import com.vincenthuto.hemomancy.common.rite.harbinger.StationUpgradeRites;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

final class CardinalRiteRingScheduleTest {
	private static final ResourceLocation PLAIN = ResourceLocation.parse("hemomancy:cardinal_rite/ring_test");
	private static final ResourceLocation STATION = ResourceLocation.parse("hemomancy:cardinal_rite/first_condensation");

	private static ActiveCardinalRite rite(ResourceLocation id, int rings, int waves, boolean stillIntervals) {
		ActiveCardinalRite rite = ActiveCardinalRite.interactive(UUID.randomUUID(), BlockPos.ZERO, id, 1200, 5, 6,
				false, waves, rings * 4);
		int[] ranks = new int[rings * 4];
		for (int i = 0; i < ranks.length; i++) ranks[i] = i / 4;
		rite.configureRings(ranks, stillIntervals);
		return rite;
	}

	private static void feedOpen(ActiveCardinalRite rite) {
		for (int i = 0; i < rite.getAnchorBloodMl().length; i++) rite.fillAnchor(i, rite.bloodNeededForAnchor(i));
	}

	private static void consecrateAndSeal(ActiveCardinalRite rite, boolean stillIntervals) {
		feedOpen(rite);
		assertTrue(rite.enterInscription());
		assertTrue(rite.sealAltar(stillIntervals));
	}

	@Test
	void theSecondRingWakesOnTheFirstWaveAndHoldsIt() {
		ActiveCardinalRite rite = rite(PLAIN, 2, 2, false);
		feedOpen(rite);
		rite.enterInscription();
		assertEquals(1, rite.getOpenRings(), "a rite with openers keeps its later rings asleep at Inscription");
		rite.sealAltar(false);

		assertEquals(CardinalRitePhase.ORDEAL, rite.getPhase());
		assertEquals(2, rite.getOpenRings());
		assertFalse(rite.areAnchorsConsecrated(), "the ordeal's wave cannot clear while the new ring hungers");
		assertEquals(4, rite.hungryLaterRingAnchors());
		assertTrue(rite.fillAnchor(4, 63));
		assertEquals(63, rite.getAnchorBloodMl()[4], "ring 1 asks 63 mL");
		feedOpen(rite);
		assertTrue(rite.areAnchorsConsecrated());
		assertEquals(1, rite.nextRingToAnnounce());
		assertEquals(-1, rite.nextRingToAnnounce(), "each ring is announced once");
	}

	@Test
	void ringsSpreadAcrossWavesAndTheIntervalsBetweenThem() {
		ActiveCardinalRite rite = rite(PLAIN, 3, 2, true);
		consecrateAndSeal(rite, true);
		assertEquals(2, rite.getOpenRings(), "wave one wakes ring 1");

		rite.completeWave();
		assertEquals(CardinalRitePhase.STILL_INTERVAL, rite.getPhase());
		assertEquals(3, rite.getOpenRings(), "the interval wakes ring 2");
		rite.finishStillInterval();
		assertEquals(3, rite.getOpenRings(), "nothing left to wake on wave two");
	}

	@Test
	void ringsBeyondTheLastOpenerAllWakeThere() {
		ActiveCardinalRite rite = rite(PLAIN, 4, 1, false);
		consecrateAndSeal(rite, false);

		assertEquals(4, rite.getOpenRings());
		assertEquals(1, rite.nextRingToAnnounce());
		assertEquals(2, rite.nextRingToAnnounce());
		assertEquals(3, rite.nextRingToAnnounce());
		assertEquals(-1, rite.nextRingToAnnounce());
	}

	@Test
	void aRiteWithNoOpenersWakesItsLaterRingsAtInscription() {
		ActiveCardinalRite rite = rite(PLAIN, 2, 0, false);
		feedOpen(rite);
		assertTrue(rite.enterInscription());

		assertEquals(2, rite.getOpenRings());
		assertFalse(rite.areAnchorsConsecrated(), "the seal waits for the woken ring");
		feedOpen(rite);
		assertTrue(rite.areAnchorsConsecrated(), "anchors are fed during Inscription");
	}

	@Test
	void theProcessionCannotEndWhileTheRingHungers() {
		ActiveCardinalRite rite = rite(PLAIN, 2, 0, false);
		rite.captureOfferingItinerary(List.of(new ActiveCardinalRite.RiteOffering(new BlockPos(2, 0, 0), null, true)));
		consecrateAndSeal(rite, false);
		assertEquals(CardinalRitePhase.OFFERING_PROCESSION, rite.getPhase());
		assertEquals(2, rite.getOpenRings());

		rite.markOfferingDrawn();
		assertFalse(rite.finishOfferingProcession());
		feedOpen(rite);
		assertTrue(rite.finishOfferingProcession());
		assertEquals(CardinalRitePhase.CULMINATION, rite.getPhase());
	}

	@Test
	void theLastCircuitWaitsForTheWokenRing() {
		ActiveCardinalRite rite = rite(STATION, 2, 0, false);
		consecrateAndSeal(rite, false);
		assertEquals(CardinalRitePhase.STATION_PROJECTION, rite.getPhase());
		assertEquals(2, rite.getOpenRings());

		int circuits = StationUpgradeRites.circuits(STATION);
		for (int circuit = 0; circuit < circuits; circuit++) {
			assertTrue(rite.fillUpgradeCircuit(circuit, rite.upgradeBloodNeeded()));
		}
		assertEquals(CardinalRitePhase.STATION_PROJECTION, rite.getPhase(), "held for the ring");
		assertEquals(0, rite.upgradeBloodNeeded(), "no circuit is left to feed");
		assertFalse(rite.tryFinishStationProjection());
		feedOpen(rite);
		assertTrue(rite.tryFinishStationProjection());
		assertEquals(CardinalRitePhase.CULMINATION, rite.getPhase());
	}

	@Test
	void anUnconfiguredStationRiteStillHandsOffAtItsLastCircuit() {
		ActiveCardinalRite rite = ActiveCardinalRite.interactive(UUID.randomUUID(), BlockPos.ZERO, STATION, 1200, 5,
				6, false, 0, 8);
		feedOpen(rite);
		rite.enterInscription();
		rite.sealAltar(false);
		int circuits = StationUpgradeRites.circuits(STATION);
		for (int circuit = 0; circuit < circuits; circuit++) rite.fillUpgradeCircuit(circuit, rite.upgradeBloodNeeded());

		assertEquals(CardinalRitePhase.CULMINATION, rite.getPhase());
	}

	@Test
	void ringStateAndHoldSurviveASaveMidHold() {
		ActiveCardinalRite rite = rite(PLAIN, 2, 2, false);
		consecrateAndSeal(rite, false);
		assertEquals(1, rite.nextRingToAnnounce());

		ActiveCardinalRite copy = ActiveCardinalRite.deserialize(rite.serialize());

		assertEquals(2, copy.getOpenRings());
		assertFalse(copy.areAnchorsConsecrated());
		assertEquals(-1, copy.nextRingToAnnounce(), "the woken ring is not announced again after loading");
	}
}
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `./gradlew.bat test --tests "com.vincenthuto.hemomancy.common.rite.CardinalRiteRingScheduleTest"`
Expected: FAIL. `tryFinishStationProjection` does not compile yet. Once it is stubbed, the open-ring assertions fail because nothing wakes rings.

- [ ] **Step 3: Implement the schedule and holds**

In `ActiveCardinalRite`, replace `setPhase`:

```java
	private void setPhase(CardinalRitePhase phase) {
		enterPhase(phase, true);
	}

	private void enterPhase(CardinalRitePhase phase, boolean wakesRings) {
		this.phase = phase;
		this.phaseTicks = 0;
		this.idleTicks = 0;
		if (wakesRings) wakeRings(phase);
	}

	/** Wakes the rings this phase entry owes: one per opener, every ring left at the last, or all at Inscription. */
	private void wakeRings(CardinalRitePhase entered) {
		if (!ringsGated() || openRings >= totalAnchorRings()) return;
		int sleeping = totalAnchorRings() - openRings;
		int opening;
		if (entered == CardinalRitePhase.INSCRIPTION) {
			opening = ringOpenerCount() == 0 ? sleeping : 0;
		} else if (altarSealed && CardinalRiteRingRules.isOpener(entered)) {
			opening = CardinalRiteRingRules.ringsToOpen(ringOpenersEntered++, ringOpenerCount(), sleeping);
		} else {
			return;
		}
		if (opening <= 0) return;
		openRings += opening;
		reconcileInstabilityDamage();
	}

	/** Openers the sealed rite enters before Culmination, from its shape. */
	public int ringOpenerCount() {
		return CardinalRiteRingRules.openerCount(
				com.vincenthuto.hemomancy.common.rite.harbinger.StationUpgradeRites.isRite(recipeId), totalWaves,
				altarSealed ? betweenWaveStillIntervals : stillIntervalsPlanned, !offeringItinerary.isEmpty());
	}
```

In `holdBeforeFinale`, replace `setPhase(CardinalRitePhase.STILL_INTERVAL);` with `enterPhase(CardinalRitePhase.STILL_INTERVAL, false);`, so a finale hold is not an opener.

In `fillAnchor`, replace the phase check with:

```java
		if (phase != CardinalRitePhase.CONSECRATION && phase != CardinalRitePhase.INSCRIPTION
				&& phase != CardinalRitePhase.STATION_PROJECTION && phase != CardinalRitePhase.ORDEAL
				&& phase != CardinalRitePhase.STILL_INTERVAL && phase != CardinalRitePhase.OFFERING_PROCESSION) return false;
```

Replace `upgradeBloodNeeded`:

```java
	public int upgradeBloodNeeded() {
		if (phase != CardinalRitePhase.STATION_PROJECTION || upgrade.getInt("Stage")
				>= com.vincenthuto.hemomancy.common.rite.harbinger.StationUpgradeRites.circuits(recipeId)) return 0;
		return com.vincenthuto.hemomancy.common.rite.harbinger.StationUpgradeRites.bloodPerCircuit(recipeId)
				- upgrade.getInt("BloodAtStage");
	}
```

In `fillUpgradeCircuit`, replace

```java
				if (circuit + 1 == com.vincenthuto.hemomancy.common.rite.harbinger.StationUpgradeRites.circuits(recipeId))
					setPhase(finalePhase());
```

with

```java
				if (circuit + 1 == com.vincenthuto.hemomancy.common.rite.harbinger.StationUpgradeRites.circuits(recipeId))
					tryFinishStationProjection();
```

and add:

```java
	/** Hands a station rite whose circuits are all fed to its finale, once every woken ring is fed too. */
	public boolean tryFinishStationProjection() {
		if (phase != CardinalRitePhase.STATION_PROJECTION || upgrade.getInt("Stage")
				< com.vincenthuto.hemomancy.common.rite.harbinger.StationUpgradeRites.circuits(recipeId)) return false;
		if (ringsGated() && !areAnchorsConsecrated()) return false;
		setPhase(finalePhase());
		return true;
	}
```

In `finishOfferingProcession`, after the existing guard, add:

```java
		if (ringsGated() && !areAnchorsConsecrated()) return false;
```

- [ ] **Step 4: Run the tests to verify they pass**

Run: `./gradlew.bat test --tests "com.vincenthuto.hemomancy.common.rite.*"`
Expected: PASS (`CardinalRiteRingScheduleTest` 8, `CardinalRiteRingStateTest` 8, all existing `common.rite` tests).

- [ ] **Step 5: Ledger**

---

### Task 4: Server wiring: activation, engine, projection, announcements

**Files:**
- Modify: `common/network/capa/harbinger/BloodCraftingKeyPressPacket.java` (after the `setInstabilityDamagePriority` block, around line 533)
- Modify: `common/rite/harbinger/CardinalRiteOrdealEngine.java` (`tick` switch, `tickOfferingProcession` end, `tickStillInterval`)
- Modify: `common/rite/harbinger/CardinalRiteInteractionHandler.java` (`tryProject` station branch and target fallbacks, `tryProjectSeal`, `anchorAt`)
- Modify: `common/rite/harbinger/HarbingerCardinalRiteEvents.java` (`emitAnchorBeats`, tick loop, `toClientEntry`, `anchorStates`, `nextAnchorRepair`, `buildChecklist`, `visibleSanguineBlobs`, `refundCleanRite`; new `announceWokenRings`)
- Modify: `common/rite/CardinalRiteBoundaryProgress.java`, `common/rite/CardinalRiteCeremonyDefinition.java:62-64`, `common/rite/CardinalRiteEventKind.java`, `common/init/SoundInit.java`, `sounds.json`, `lang/en_us.json`
- Test: `common/rite/CardinalRiteBoundaryProgressTest.java` (add one test), `common/rite/harbinger/RingGatingServerSourceTest.java` (new)

**Interfaces:**
- Consumes: Tasks 1 to 3.
- Produces:
  - `CardinalRiteEventKind.RING_OPENED`. Its beat carries `index` = ring rank, `style` = total ring count as a decimal string, and `points` = the ring's anchor aim points.
  - `SoundInit.RITE_RING_OPEN`.
  - `CardinalRiteBoundaryProgress.completedSegments(anchors, blood, int[] requiredMl)`.

- [ ] **Step 1: Write the failing tests**

Append to `src/test/java/com/vincenthuto/hemomancy/common/rite/CardinalRiteBoundaryProgressTest.java`. Read the file first and reuse its anchor fixture style. The test below builds its own anchors.

```java
	@Test
	void arcsJoinOnlyFedOpenAnchorsAtTheirOwnRequirement() {
		var anchors = java.util.List.of(
				new CardinalRiteCeremonyDefinition.Anchor(3, 0, 0, 1, 0), new CardinalRiteCeremonyDefinition.Anchor(0, 0, 3, 1, 1),
				new CardinalRiteCeremonyDefinition.Anchor(-3, 0, 0, 1, 2), new CardinalRiteCeremonyDefinition.Anchor(0, 0, -3, 1, 3));
		int[] blood = {63, 63, 50, 63};

		assertEquals(2, CardinalRiteBoundaryProgress.completedSegments(anchors, blood, new int[] {63, 63, 63, 63}).size(),
				"only the arcs between anchors at 63 mL");
		assertTrue(CardinalRiteBoundaryProgress.completedSegments(anchors, new int[4], new int[4]).isEmpty(),
				"a sleeping ring (requirement 0) is never complete");
		assertEquals(4, CardinalRiteBoundaryProgress.completedSegments(anchors, new int[] {50, 50, 50, 50}).size(),
				"the flat overload keeps the flat cost");
	}
```

(Add `import static org.junit.jupiter.api.Assertions.*;` if the file imports assertions individually.)

`src/test/java/com/vincenthuto/hemomancy/common/rite/harbinger/RingGatingServerSourceTest.java`:

```java
package com.vincenthuto.hemomancy.common.rite.harbinger;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

final class RingGatingServerSourceTest {
	private static final String ROOT = "src/main/java/com/vincenthuto/hemomancy/";

	static String read(String path) throws IOException {
		return Files.readString(Path.of(path)).replace("\r\n", "\n");
	}

	@Test
	void activationConfiguresRingsForHarbingerCeremoniesOnly() throws IOException {
		String packet = read(ROOT + "common/network/capa/harbinger/BloodCraftingKeyPressPacket.java");
		assertTrue(packet.contains("rite.configureRings(com.vincenthuto.hemomancy.common.rite.CardinalRiteRingRules.ranksOf("));
		assertTrue(packet.contains("if (!recipe.isPuppeteerTrial() && !recipe.isUnstained()) {"));
	}

	@Test
	void theEngineHoldsTheStationAndTheProcessionForTheRing() throws IOException {
		String engine = read(ROOT + "common/rite/harbinger/CardinalRiteOrdealEngine.java");
		assertTrue(engine.contains("case STATION_PROJECTION -> rite.tryFinishStationProjection();"));
		assertTrue(engine.contains("&& rite.finishOfferingProcession()) {"), "the swell message only on a real hand-off");
		assertTrue(engine.contains("rite.addInstability(rite.anchorDeficitInstability());"));
	}

	@Test
	void projectionFeedsTheWokenRingAndTheSealWaitsForIt() throws IOException {
		String handler = read(ROOT + "common/rite/harbinger/CardinalRiteInteractionHandler.java");
		assertTrue(handler.contains("if (rite.hungryLaterRingAnchors() <= 0) return CardinalRiteProjectionResult.handled(0.0D);"));
		assertTrue(handler.contains("The woken ring hungers. Feed its sores before the altar will seal."));
		assertTrue(handler.contains("filter(rite::isAnchorOpen)"), "the nearest-anchor fallback skips sleeping anchors");
	}

	@Test
	void wokenRingsAreAnnouncedWithHemomancyParticles() throws IOException {
		String events = read(ROOT + "common/rite/harbinger/HarbingerCardinalRiteEvents.java");
		int start = events.indexOf("private static void announceWokenRings(");
		int end = events.indexOf("\n\t}\n", start);
		assertTrue(start >= 0 && end > start);
		String announce = events.substring(start, end);
		assertTrue(announce.contains("CardinalRiteEventKind.RING_OPENED"));
		assertTrue(announce.contains("SoundInit.RITE_RING_OPEN"));
		assertTrue(announce.contains("HemoParticleData.bloodCell("));
		assertFalse(announce.contains("ParticleTypes."), "no vanilla particles");
		assertTrue(events.contains("announceWokenRings(sLevel, rite, recipe);"));
		assertFalse(events.contains("BLOOD_PER_ANCHOR_ML"), "every reader uses the anchor's own requirement");
	}

	@Test
	void theStingIsRegisteredWithASubtitle() throws IOException {
		String sounds = read("src/main/resources/assets/hemomancy/sounds.json");
		String lang = read("src/main/resources/assets/hemomancy/lang/en_us.json");
		String init = read(ROOT + "common/init/SoundInit.java");
		assertTrue(init.contains("RITE_RING_OPEN = registerSoundEvent(\"rite.ring_open\")"));
		assertTrue(sounds.contains("\"rite.ring_open\": {\"subtitle\": \"subtitle.hemomancy.rite.ring_open\""));
		assertTrue(lang.contains("\"subtitle.hemomancy.rite.ring_open\":"));
	}
}
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `./gradlew.bat test --tests "com.vincenthuto.hemomancy.common.rite.CardinalRiteBoundaryProgressTest" --tests "com.vincenthuto.hemomancy.common.rite.harbinger.RingGatingServerSourceTest"`
Expected: FAIL (the 3-argument `completedSegments` does not compile; the source assertions fail).

- [ ] **Step 3: Boundary progress, ceremony cost, event kind, sound**

`CardinalRiteBoundaryProgress.java`: rename the existing `completedSegments(anchors, anchorBloodMl)` body into a new overload with `int[] requiredMl`, and keep the old signature delegating:

```java
	public static List<Segment> completedSegments(
			List<CardinalRiteCeremonyDefinition.Anchor> anchors, int[] anchorBloodMl) {
		return completedSegments(anchors, anchorBloodMl, null);
	}

	/** Arcs between neighbouring anchors that are both fed to their own requirement; 0 marks a sleeping anchor. */
	public static List<Segment> completedSegments(
			List<CardinalRiteCeremonyDefinition.Anchor> anchors, int[] anchorBloodMl, int[] requiredMl) {
		// ... existing body, with both complete(...) calls passing requiredMl
	}

	private static boolean complete(int[] blood, int[] requiredMl, int index) {
		if (index < 0 || index >= blood.length) return false;
		int required = requiredMl != null && index < requiredMl.length
				? requiredMl[index] : CardinalRiteCeremonyRules.BLOOD_PER_ANCHOR_ML;
		return required > 0 && blood[index] >= required;
	}
```

In the moved body, the line `if (!complete(anchorBloodMl, start.index()) || !complete(anchorBloodMl, end.index())) continue;` becomes `if (!complete(anchorBloodMl, requiredMl, start.index()) || !complete(anchorBloodMl, requiredMl, end.index())) continue;`.

`CardinalRiteCeremonyDefinition.anchorBloodCostMl()`:

```java
	public int anchorBloodCostMl() {
		int total = 0;
		for (int rank : CardinalRiteRingRules.ranksOf(anchors)) total += CardinalRiteRingRules.bloodForRank(rank);
		return total;
	}
```

`CardinalRiteEventKind.java`: change `ESCALATION;` to `ESCALATION,` and append:

```java
	/** A later anchor ring wakes: index is its rank, style the rite's ring count, points its anchors' aim points. */
	RING_OPENED;
```

`SoundInit.java`, after `RITE_ESCALATE`:

```java
	public static final DeferredHolder<SoundEvent, SoundEvent> RITE_RING_OPEN = registerSoundEvent("rite.ring_open");
```

`sounds.json`: before the closing `}` add a comma after the `rite.ambient.storm` line, then this line. Edit with Python `newline='\r\n'`, or the Edit tool on the exact line, to keep CRLF:

```json
  "rite.ring_open": {"subtitle": "subtitle.hemomancy.rite.ring_open", "sounds": [{"name": "minecraft:entity.warden.roar", "type": "event", "pitch": 0.7, "volume": 0.9}]}
```

`en_us.json`: after `"subtitle.hemomancy.rite.ambient.storm": "Blood wind howls",` add:

```json
  "subtitle.hemomancy.rite.ring_open": "A ring of the rite tears open",
```

- [ ] **Step 4: Activation**

In `BloodCraftingKeyPressPacket.java`, replace

```java
				if (recipe.hasInteractiveCeremony()) {
					rite.setInstabilityDamagePriority(
							com.vincenthuto.hemomancy.common.rite.CardinalRiteInstabilityBoundaryRules
												.damagePriority(recipe.getCeremony().anchors()).stream()
									.mapToInt(Integer::intValue).toArray());
				}
```

with

```java
				if (recipe.hasInteractiveCeremony()) {
					rite.setInstabilityDamagePriority(
							com.vincenthuto.hemomancy.common.rite.CardinalRiteInstabilityBoundaryRules
												.damagePriority(recipe.getCeremony().anchors()).stream()
									.mapToInt(Integer::intValue).toArray());
					if (!recipe.isPuppeteerTrial() && !recipe.isUnstained()) {
						// Ring 0 opens now; each later ring wakes at a later phase of the rite.
						rite.configureRings(com.vincenthuto.hemomancy.common.rite.CardinalRiteRingRules.ranksOf(
								recipe.getCeremony().anchors()), recipe.getCeremony().stillIntervalTicks() > 0);
					}
				}
```

(Match the file's exact indentation when editing.)

- [ ] **Step 5: Engine**

In `CardinalRiteOrdealEngine.tick`, add to the switch after `case INSCRIPTION -> {...}`:

```java
			case STATION_PROJECTION -> rite.tryFinishStationProjection();
```

In `tickOfferingProcession`, replace

```java
		if (rite.isReturningFromOfferings()
				&& CardinalRiteFinaleTiming.processionFinished(rite.getPhaseTicks(), total)) {
			rite.finishOfferingProcession();
```

with

```java
		if (rite.isReturningFromOfferings()
				&& CardinalRiteFinaleTiming.processionFinished(rite.getPhaseTicks(), total)
				&& rite.finishOfferingProcession()) {
```

In `tickStillInterval`, replace

```java
					rite.addInstability(CardinalRiteCeremonyRules.anchorDeficitInstability(
							rite.getAnchorBloodMl()));
```

with

```java
					rite.addInstability(rite.anchorDeficitInstability());
```

- [ ] **Step 6: Projection**

In `CardinalRiteInteractionHandler.tryProject`, replace the station branch's

```java
				if (aimed != stage) return CardinalRiteProjectionResult.handled(0.0D);
				int paid = spendBlood(player, rite,
						Math.min(rite.upgradeBloodNeeded(), Math.max(1, (int) Math.floor(projectionRate))));
				if (paid > 0 && rite.fillUpgradeCircuit(stage, paid)) {
					data.setDirty();
					serverLevel.playSound(null, StationUpgradeRites.target(rite, stage),
							SoundInit.RITE_ANCHOR_FILL.get(), SoundSource.BLOCKS, 0.6F, 0.85F + stage * 0.1F);
				}
				return CardinalRiteProjectionResult.handled(paid);
			}
```

with

```java
				if (aimed == stage) {
					int paid = spendBlood(player, rite,
							Math.min(rite.upgradeBloodNeeded(), Math.max(1, (int) Math.floor(projectionRate))));
					if (paid > 0 && rite.fillUpgradeCircuit(stage, paid)) {
						data.setDirty();
						serverLevel.playSound(null, StationUpgradeRites.target(rite, stage),
								SoundInit.RITE_ANCHOR_FILL.get(), SoundSource.BLOCKS, 0.6F, 0.85F + stage * 0.1F);
					}
					return CardinalRiteProjectionResult.handled(paid);
				}
				// A woken ring is fed through its sores while the circuits wait.
				if (rite.hungryLaterRingAnchors() <= 0) return CardinalRiteProjectionResult.handled(0.0D);
			}
```

In the same method, replace `if (target == null) continue;` with

```java
			if (target == null) {
				if (rite.getPhase() == CardinalRitePhase.STATION_PROJECTION) return CardinalRiteProjectionResult.handled(0.0D);
				continue;
			}
```

and replace `if (anchor < 0) continue;` with

```java
			if (anchor < 0) {
				if (rite.getPhase() == CardinalRitePhase.STATION_PROJECTION) return CardinalRiteProjectionResult.handled(0.0D);
				continue;
			}
```

In `tryProjectSeal`, immediately before `prepareWaveDeck(rite, recipe);` add:

```java
		if (!rite.areAnchorsConsecrated()) {
			player.displayClientMessage(Component.literal("The woken ring hungers. Feed its sores before the altar will seal.")
					.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), true);
			return true;
		}
```

Replace `anchorAt`:

```java
	private static int anchorAt(ServerLevel level, ActiveCardinalRite rite, BlockPos target) {
		CardinalRiteRecipe recipe = CardinalRiteRecipe.getRiteByLocation(level, rite.getRecipeId());
		if (recipe == null || recipe.getCeremony() == null) return -1;
		var anchors = recipe.getCeremony().anchors();
		List<Integer> open = java.util.stream.IntStream.range(0, anchors.size())
				.filter(rite::isAnchorOpen).boxed().toList();
		int nearest = CardinalRiteTargetGeometry.nearestAnchorIndex(
				CardinalRiteAnchorVisualRules.riteSurface(rite.getCenterPos()),
				open.stream().map(index -> anchors.get(index).offset()).toList(), target, 1.6D);
		return nearest < 0 ? -1 : open.get(nearest);
	}
```

- [ ] **Step 7: Events: requirement readers and announcements**

In `HarbingerCardinalRiteEvents.java`, make these replacements:

1. `emitAnchorBeats`: `if (blood[i] >= CardinalRiteCeremonyRules.BLOOD_PER_ANCHOR_ML) full.set(i);` → `if (rite.isAnchorOpen(i) && blood[i] >= rite.anchorRequirementMl(i)) full.set(i);`
2. Tick loop: after `if (recipe != null && !unstained) emitAnchorBeats(sLevel, rite, recipe);` add `if (recipe != null && !unstained) announceWokenRings(sLevel, rite, recipe);`
3. `toClientEntry`:
   - `int upfront = rite.getAnchorBloodMl().length * CardinalRiteCeremonyRules.BLOOD_PER_ANCHOR_ML;` → `int upfront = rite.openRequirementMl();`
   - The committed sum `java.util.Arrays.stream(rite.getAnchorBloodMl()).map(blood -> Math.clamp(blood, 0, CardinalRiteCeremonyRules.BLOOD_PER_ANCHOR_ML)).sum()` → `rite.committedAnchorBloodMl()`
   - `CardinalRiteBoundaryProgress.completedSegments(recipe.getCeremony().anchors(), rite.getAnchorBloodMl())` → `CardinalRiteBoundaryProgress.completedSegments(recipe.getCeremony().anchors(), rite.getAnchorBloodMl(), rite.anchorRequirements())`
4. `anchorStates`: `Math.clamp(blood[i], 0, CardinalRiteCeremonyRules.BLOOD_PER_ANCHOR_ML)` → `Math.clamp(blood[i], 0, rite.anchorRequirements()[i])`. Hoist `int[] required = rite.anchorRequirements();` above the loop and use `required[i]`.
5. `nextAnchorRepair`: the two `CardinalRiteCeremonyRules.BLOOD_PER_ANCHOR_ML` become `rite.anchorRequirementMl(index)`.
6. `buildChecklist` CONSECRATION: replace
   ```java
   			for (int blood : rite.getAnchorBloodMl()) {
   				if (blood < CardinalRiteCeremonyRules.BLOOD_PER_ANCHOR_ML) missing++;
   			}
   ```
   with
   ```java
   			for (int i = 0; i < rite.getAnchorBloodMl().length; i++) {
   				if (rite.bloodNeededForAnchor(i) > 0) missing++;
   			}
   ```
7. `buildChecklist` ORDEAL: replace the `dry` stream with `long dry = java.util.stream.IntStream.range(0, rite.getAnchorBloodMl().length).filter(i -> rite.bloodNeededForAnchor(i) > 0).count();`
8. `visibleSanguineBlobs`:
   - At the top of the loop body add `if (!rite.isAnchorOpen(i)) continue;`
   - `blood[i] < CardinalRiteCeremonyRules.BLOOD_PER_ANCHOR_ML` → `blood[i] < rite.anchorRequirementMl(i)`
   - `formingBoundaryRadius(blood[i], CardinalRiteCeremonyRules.BLOOD_PER_ANCHOR_ML)` → `formingBoundaryRadius(blood[i], rite.anchorRequirementMl(i))`
9. `refundCleanRite`: `int refund = Math.round(anchors * CardinalRiteCeremonyRules.BLOOD_PER_ANCHOR_ML * CardinalRiteCeremonyRules.CLEAN_RITE_REFUND);` → `int refund = Math.round(rite.fullRequirementMl() * CardinalRiteCeremonyRules.CLEAN_RITE_REFUND);`
10. Run `grep -n "BLOOD_PER_ANCHOR_ML" HarbingerCardinalRiteEvents.java`. Any remaining hit is a reader of "this anchor's requirement": convert it the same way, then drop the import of `CardinalRiteCeremonyRules` only if it is now unused.

Add the method after `emitAnchorBeats`:

```java
	/**
	 * Each ring that woke since the last tick tears open: its beat for the floor, sores and world, a sting heard
	 * rite-wide, and living blood bursting from each of its sores. Deeper rings land harder.
	 */
	private static void announceWokenRings(ServerLevel level, ActiveCardinalRite rite, CardinalRiteRecipe recipe) {
		for (int rank = rite.nextRingToAnnounce(); rank >= 0; rank = rite.nextRingToAnnounce()) {
			int rings = rite.totalAnchorRings();
			float depth = com.vincenthuto.hemomancy.common.rite.RingOpeningMath.depth(rank, rings);
			java.util.List<net.minecraft.world.phys.Vec3> points = new java.util.ArrayList<>();
			if (recipe.getCeremony() != null) {
				var anchors = recipe.getCeremony().anchors();
				for (int i = 0; i < anchors.size(); i++) {
					if (rite.anchorRank(i) == rank) {
						points.add(CardinalRiteTargetGeometry.anchorAimPoint(rite.getCenterPos(), anchors.get(i).offset()));
					}
				}
			}
			CardinalRiteBeats.send(level, rite, CardinalRiteEventKind.RING_OPENED, rank, Integer.toString(rings),
					points, ItemStack.EMPTY);
			CardinalRiteBeats.sound(level, rite.getCenterPos(), SoundInit.RITE_RING_OPEN,
					com.vincenthuto.hemomancy.common.rite.RingOpeningMath.stingVolume(depth),
					com.vincenthuto.hemomancy.common.rite.RingOpeningMath.stingPitch(depth));
			var cell = com.vincenthuto.hemomancy.common.particle.HemoParticleData.bloodCell(new ParticleColor(150, 6, 18));
			int count = com.vincenthuto.hemomancy.common.rite.RingOpeningMath.burstCells(depth);
			for (net.minecraft.world.phys.Vec3 point : points) {
				level.sendParticles(cell, point.x, point.y, point.z, count, 0.35D, 0.6D, 0.35D, 0.08D);
			}
		}
	}
```

`ParticleColor` is already imported in this file because the sacrifice draw uses it. If `ItemStack` is not imported, use `net.minecraft.world.item.ItemStack.EMPTY`.

- [ ] **Step 8: Run the tests to verify they pass, then compile everything**

Run: `./gradlew.bat test --tests "com.vincenthuto.hemomancy.common.rite.*" --tests "com.vincenthuto.hemomancy.common.rite.harbinger.*"`
Expected: PASS.

Run: `./gradlew.bat compileJava compileTestJava compileGameTestJava`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 9: Ledger**

---

### Task 5: Sync, the hungry-ring problem and the waking line

**Files:**
- Modify: `client/data/ActiveRiteClientData.java` (`RiteEntry`)
- Modify: `common/network/capa/harbinger/PacketSyncActiveRites.java` (encode after socket markers; decode chain)
- Modify: `common/rite/harbinger/HarbingerCardinalRiteEvents.java` (`toClientEntry` chain, `problemFacts`)
- Modify: `common/rite/CardinalRiteProblems.java`
- Modify: `client/render/world/rite/escalation/RiteGuidance.java`
- Modify: `lang/en_us.json`
- Test: `common/network/capa/harbinger/PacketSyncActiveRitesEscalationTest.java` (add), `common/rite/CardinalRiteProblemsTest.java` (add), `client/render/world/rite/escalation/RiteGuidanceTest.java` (add)

**Interfaces:**
- Consumes: `rite.getOpenRings()`, `rite.anchorRequirements()`, `rite.hungryLaterRingAnchors()`.
- Produces:
  - `RiteEntry.withRings(int openRings, int[] requirements)`
  - `int getOpenRings()` (falls back to `getTotalRings()` when unsynced) and `int[] getAnchorRequirements()`
  - `int anchorRequirement(int i)` (falls back to 50 when unsynced) and `boolean anchorDormant(int i)`
  - `CardinalRiteProblems.RING_HUNGERS`, with `Facts.hungryRingAnchors` (the old 8-argument constructor is kept)
  - `RiteGuidance.decide(Seen, String, String, boolean, List<RiteProblem>, long, int openRings)` and `RiteGuidance.ringKey(int)`

- [ ] **Step 1: Write the failing tests**

Append to `PacketSyncActiveRitesEscalationTest`:

```java
	@Test
	void ringsRoundTripAndUnsyncedEntriesStayFlat() {
		ActiveRiteClientData.RiteEntry copy = roundTrip(entry().withRings(2, new int[] {50, 50, 50, 50, 63, 63, 0, 0}));

		assertEquals(2, copy.getOpenRings());
		assertEquals(63, copy.anchorRequirement(4));
		org.junit.jupiter.api.Assertions.assertTrue(copy.anchorDormant(6));
		org.junit.jupiter.api.Assertions.assertFalse(copy.anchorDormant(0));

		ActiveRiteClientData.RiteEntry unsynced = entry();
		assertEquals(unsynced.getTotalRings(), unsynced.getOpenRings());
		assertEquals(50, unsynced.anchorRequirement(3));
		org.junit.jupiter.api.Assertions.assertFalse(unsynced.anchorDormant(3));
	}
```

Append to `CardinalRiteProblemsTest` (read its existing helpers first; this test builds `Facts` directly):

```java
	@Test
	void aHungryWokenRingIsNamedOutsideConsecration() {
		var ordeal = new CardinalRiteProblems.Facts(CardinalRitePhase.ORDEAL, List.of(), 0, 0, true, 0, 0, false, 3);
		assertEquals(List.of(new CardinalRiteProblems.RiteProblem(CardinalRiteProblems.RING_HUNGERS, List.of("3"))),
				CardinalRiteProblems.of(ordeal));

		var consecration = new CardinalRiteProblems.Facts(CardinalRitePhase.CONSECRATION, List.of(), 0, 0, true, 0, 0,
				false, 3);
		assertTrue(CardinalRiteProblems.of(consecration).isEmpty(), "ring 0 is the phase's own work, not a problem");
		assertEquals(0, new CardinalRiteProblems.Facts(CardinalRitePhase.ORDEAL, List.of(), 0, 0, true, 0, 0, false)
				.hungryRingAnchors(), "the old shape still builds");
	}
```

Append to `RiteGuidanceTest` (read its helpers first):

```java
	@Test
	void aWakingRingIsAnnouncedAfterThePhaseLine() {
		RiteGuidance.Decision first = RiteGuidance.decide(RiteGuidance.Seen.NONE, "CONSECRATION", "", false, List.of(), 0, 1);
		RiteGuidance.Decision ring = RiteGuidance.decide(first.seen(), "ORDEAL", "", false, List.of(),
				RiteGuidance.QUIET_TICKS + 1, 2);
		assertEquals("hemomancy.rite.guide.ring_woke.2", ring.line().key(),
				"the ordeal has no guide line of its own, so the ring speaks at once");
		assertNull(RiteGuidance.decide(ring.seen(), "ORDEAL", "", false, List.of(), RiteGuidance.QUIET_TICKS * 3, 2)
				.line(), "once only");
	}

	@Test
	void ringsAlreadyAwakeWhenFirstSeenAreNotAnnounced() {
		RiteGuidance.Decision first = RiteGuidance.decide(RiteGuidance.Seen.NONE, "ORDEAL", "", false, List.of(), 0, 3);
		assertNull(RiteGuidance.decide(first.seen(), "ORDEAL", "", false, List.of(), RiteGuidance.QUIET_TICKS + 1, 3)
				.line());
		assertEquals("hemomancy.rite.guide.ring_woke.8", RiteGuidance.ringKey(11), "ordinals stop at the eighth");
	}

	@Test
	void theStillIntervalLineComesFirstThenTheRing() {
		RiteGuidance.Decision first = RiteGuidance.decide(RiteGuidance.Seen.NONE, "ORDEAL", "", false, List.of(), 0, 2);
		RiteGuidance.Decision phase = RiteGuidance.decide(first.seen(), "STILL_INTERVAL", "", false, List.of(),
				RiteGuidance.QUIET_TICKS + 1, 3);
		assertEquals("hemomancy.rite.guide.still_interval", phase.line().key());
		RiteGuidance.Decision ring = RiteGuidance.decide(phase.seen(), "STILL_INTERVAL", "", false, List.of(),
				RiteGuidance.QUIET_TICKS * 2 + 2, 3);
		assertEquals("hemomancy.rite.guide.ring_woke.3", ring.line().key());
	}
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `./gradlew.bat test --tests "com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncActiveRitesEscalationTest" --tests "com.vincenthuto.hemomancy.common.rite.CardinalRiteProblemsTest" --tests "com.vincenthuto.hemomancy.client.render.world.rite.escalation.RiteGuidanceTest"`
Expected: FAIL (compilation: `withRings`, the 9-argument `Facts`, the 7-argument `decide` and `ringKey` do not exist).

- [ ] **Step 3: Client entry and packet**

In `ActiveRiteClientData.RiteEntry`, add fields beside `escalation`:

```java
		private int openRings = -1;
		private int[] anchorRequirements = new int[0];
```

and methods beside `withEscalation`:

```java
		/** Rings woken so far; every ring when the server sent none. */
		public int getOpenRings() { return openRings < 0 ? totalRings : openRings; }
		public int[] getAnchorRequirements() { return anchorRequirements; }
		/** An anchor's requirement in mL; the flat cost when the server sent none. */
		public int anchorRequirement(int anchorIndex) {
			return anchorIndex >= 0 && anchorIndex < anchorRequirements.length ? anchorRequirements[anchorIndex]
					: com.vincenthuto.hemomancy.common.rite.CardinalRiteCeremonyRules.BLOOD_PER_ANCHOR_ML;
		}
		/** Whether this anchor's ring still sleeps. */
		public boolean anchorDormant(int anchorIndex) {
			return anchorIndex >= 0 && anchorIndex < anchorRequirements.length && anchorRequirements[anchorIndex] == 0;
		}
		public RiteEntry withRings(int open, int[] requirements) {
			openRings = Math.max(0, open);
			anchorRequirements = requirements == null ? new int[0] : requirements.clone();
			return this;
		}
```

In `PacketSyncActiveRites.encode`, after the socket-marker loop:

```java
			buf.writeVarInt(entry.getOpenRings());
			buf.writeVarIntArray(entry.getAnchorRequirements());
```

In `decode`, extend the chain: `.withGuidance(readProblems(buf), readSocketMarkers(buf))` → `.withGuidance(readProblems(buf), readSocketMarkers(buf)).withRings(buf.readVarInt(), buf.readVarIntArray())`.

In `HarbingerCardinalRiteEvents.toClientEntry`, append to the returned chain after `.withGuidance(...)`:

```java
				.withRings(rite.getOpenRings(), unstained ? new int[0] : rite.anchorRequirements())
```

- [ ] **Step 4: The problem**

In `CardinalRiteProblems`:

```java
	public static final String RING_HUNGERS = "hemomancy.rite.problem.ring_hungers";
```

Replace the `Facts` record with:

```java
	public record Facts(CardinalRitePhase phase, List<String> sleepingRequiredSigils, int requiredAllies,
			int availableAllies, boolean mediumReady, int damagedAnchors, int sacrificeOwedMl,
			boolean heldBeforeFinale, int hungryRingAnchors) {
		public Facts {
			sleepingRequiredSigils = sleepingRequiredSigils == null ? List.of() : List.copyOf(sleepingRequiredSigils);
		}

		public Facts(CardinalRitePhase phase, List<String> sleepingRequiredSigils, int requiredAllies,
				int availableAllies, boolean mediumReady, int damagedAnchors, int sacrificeOwedMl,
				boolean heldBeforeFinale) {
			this(phase, sleepingRequiredSigils, requiredAllies, availableAllies, mediumReady, damagedAnchors,
					sacrificeOwedMl, heldBeforeFinale, 0);
		}
	}
```

In `of(Facts)`, before the `feeding` line:

```java
		boolean afterConsecration = phase == CardinalRitePhase.INSCRIPTION || phase == CardinalRitePhase.STATION_PROJECTION
				|| phase == CardinalRitePhase.ORDEAL || phase == CardinalRitePhase.STILL_INTERVAL
				|| phase == CardinalRitePhase.OFFERING_PROCESSION;
		if (afterConsecration && facts.hungryRingAnchors() > 0) {
			problems.add(new RiteProblem(RING_HUNGERS, List.of(Integer.toString(facts.hungryRingAnchors()))));
		}
```

In `HarbingerCardinalRiteEvents.problemFacts`, pass the new argument: `rite.getPhase() == CardinalRitePhase.STILL_INTERVAL && rite.isHeldBeforeFinale(), rite.hungryLaterRingAnchors());`.

- [ ] **Step 5: The waking line**

In `RiteGuidance`:

```java
	/** What the owner has already been told about one rite. Problems are remembered by identity; rings -1 is unseen. */
	record Seen(String phase, String cue, List<String> announced, long quietUntil, int rings) {
		static final Seen NONE = new Seen("", "", List.of(), Long.MIN_VALUE, -1);

		Seen(String phase, String cue, List<String> announced, long quietUntil) {
			this(phase, cue, announced, quietUntil, -1);
		}
	}

	/** "The Nth ring wakes", named for the deepest ring awake; ordinals stop at the eighth. */
	static String ringKey(int openRings) {
		return "hemomancy.rite.guide.ring_woke." + Math.clamp(openRings, 2, 8);
	}

	static Decision decide(Seen seen, String phase, String cue, boolean succession, List<RiteProblem> problems,
			long now) {
		return decide(seen, phase, cue, succession, problems, now, -1);
	}
```

Rename the existing `decide(...)` body into `decide(..., long now, int openRings)`. Update every `new Seen(a, b, c, d)` inside it to carry the rings: use `kept.rings()` or `seen.rings()`, whichever object it copies from. Then make these changes:

- `Seen kept = new Seen(seen.phase(), seen.cue(), announced, seen.quietUntil(), seen.rings());`
- Directly after it, so first sight and unknown counts are learned silently:
  ```java
  		if (openRings >= 0 && (kept.rings() < 0 || openRings < kept.rings())) {
  			kept = new Seen(kept.phase(), kept.cue(), announced, kept.quietUntil(), openRings);
  		}
  ```
- After the phase-change block and before the succession cue block:
  ```java
  		if (openRings > kept.rings() && kept.rings() >= 0) {
  			return new Decision(new Seen(kept.phase(), kept.cue(), announced, now + QUIET_TICKS, openRings),
  					new Line(ringKey(openRings), List.of()));
  		}
  ```
- In `tick`, call `decide(..., ticks, entry.getOpenRings())`.

**Note on the tests:** a phase change to `ORDEAL` (no guide key) sets no quiet window, so the ring line comes in the same decision. A phase with a guide line (`STILL_INTERVAL`) speaks first and the ring follows after `QUIET_TICKS`.

- [ ] **Step 6: Lang**

Add after `"hemomancy.rite.problem.finale_held": ...` (CRLF preserved):

```json
  "hemomancy.rite.problem.ring_hungers": "The woken ring hungers: %s sores still empty.",
```

Add after `"hemomancy.rite.guide.culmination": ...`:

```json
  "hemomancy.rite.guide.ring_woke.2": "The second ring wakes. Feed its sores.",
  "hemomancy.rite.guide.ring_woke.3": "The third ring wakes. Feed its sores.",
  "hemomancy.rite.guide.ring_woke.4": "The fourth ring wakes. Feed its sores.",
  "hemomancy.rite.guide.ring_woke.5": "The fifth ring wakes. Feed its sores.",
  "hemomancy.rite.guide.ring_woke.6": "The sixth ring wakes. Feed its sores.",
  "hemomancy.rite.guide.ring_woke.7": "The seventh ring wakes. Feed its sores.",
  "hemomancy.rite.guide.ring_woke.8": "The eighth ring wakes. Feed its sores.",
```

- [ ] **Step 7: Run the tests to verify they pass**

Run: `./gradlew.bat test --tests "com.vincenthuto.hemomancy.common.network.capa.harbinger.*" --tests "com.vincenthuto.hemomancy.common.rite.*" --tests "com.vincenthuto.hemomancy.client.render.world.rite.escalation.*"`
Expected: PASS.

- [ ] **Step 8: Ledger**

---

### Task 6: Scars, the tear, the flare, the shock, the surge and the pips

**Files:**
- Create: `client/render/world/rite/escalation/RingTear.java`
- Modify: `client/render/world/floor/AnchorSores.java`
- Modify: `client/render/world/rite/CardinalRiteAnchorJetRenderer.java:58-75`
- Modify: `client/render/world/floor/FloorRiteResponse.java:175-182`
- Modify: `client/render/world/rite/escalation/RiteWorldState.java`, `client/data/CardinalRiteEventClientState.java` (`accept`)
- Modify: `client/screen/overlay/CardinalRiteOverlay.java:72,107-111`
- Test: `client/render/world/rite/escalation/RiteWorldStateTest.java` (add), `client/screen/overlay/CardinalRiteRingPipsTest.java` (new), `client/render/world/rite/escalation/RingGatingClientSourceTest.java` (new)

**Interfaces:**
- Consumes:
  - `RiteEntry.anchorRequirement(i)` and `anchorDormant(i)`
  - The `RING_OPENED` beat: `index` = rank, `style` = ring count
  - `RingOpeningMath` and `CardinalRiteRingRules.ranksOf`
- Produces:
  - `RingTear`: `static CardinalRiteEventClientState.RiteEvent waking(BlockPos, int rank, float partialTick)`, `static int ringsOf(String style)`, `static float size(boolean dormant, BlockPos, int rank, float partialTick)`
  - `RiteWorldState.ringOpened(BlockPos, float surge)`
  - `CardinalRiteOverlay.pips(int complete, int open, int total)`

- [ ] **Step 1: Write the failing tests**

Append to `RiteWorldStateTest`:

```java
	@Test
	void aWakingRingSurgesTheWorldWithoutStepping() {
		tick(List.of(entry(FOCUS, "ORDEAL", 2, 6)), List.of());
		RiteWorldState.ringOpened(FOCUS, 0.6F);
		tick(List.of(entry(FOCUS, "ORDEAL", 2, 6)), List.of());

		RiteWorldState.World world = RiteWorldState.at(FOCUS);
		assertTrue(world.surge() > 0.45F && world.surge() < 0.6F, "the surge lands and starts to decay");
		assertEquals(2, world.stage(), "a ring opening is not a world step");
		RiteWorldState.ringOpened(new BlockPos(0, 0, 0), 1.0F);
	}
```

(The last line documents that an unknown Focus is a no-op; it must not throw.)

`src/test/java/com/vincenthuto/hemomancy/client/screen/overlay/CardinalRiteRingPipsTest.java`:

```java
package com.vincenthuto.hemomancy.client.screen.overlay;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class CardinalRiteRingPipsTest {
	@Test
	void sleepingRingsDrawAsHollowCircles() {
		assertEquals("◆◇○", CardinalRiteOverlay.pips(1, 2, 3));
		assertEquals("◆◆", CardinalRiteOverlay.pips(2, 2, 2));
		assertEquals("◇○○○", CardinalRiteOverlay.pips(0, 1, 4));
	}
}
```

`src/test/java/com/vincenthuto/hemomancy/client/render/world/rite/escalation/RingGatingClientSourceTest.java`:

```java
package com.vincenthuto.hemomancy.client.render.world.rite.escalation;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

final class RingGatingClientSourceTest {
	private static final String ROOT = "src/main/java/com/vincenthuto/hemomancy/";

	@Test
	void soresScarWhileAsleepAndTearOpen() throws IOException {
		String sores = RiteWorldSourceTest.read(ROOT + "client/render/world/floor/AnchorSores.java");
		assertTrue(sores.contains("RingTear.size(rite.anchorDormant(i), origin, ranks[i], 0.0F)"));
		assertTrue(sores.contains("/ (float) Math.max(1, rite.anchorRequirement(i))"));
		assertFalse(sores.contains("BLOOD_PER_ANCHOR_ML"));
	}

	@Test
	void jetsReadTheAnchorsOwnRequirementAndFlareOnWaking() throws IOException {
		String jets = RiteWorldSourceTest.read(ROOT + "client/render/world/rite/CardinalRiteAnchorJetRenderer.java");
		assertTrue(jets.contains("rite.anchorRequirement(i)"));
		assertTrue(jets.contains("RingTear.waking(center, ranks[i], partialTick)"));
		assertFalse(jets.contains("BLOOD_PER_ANCHOR_ML"));
	}

	@Test
	void theFloorShakesAndTheWorldSurgesWhenARingWakes() throws IOException {
		String floor = RiteWorldSourceTest.read(ROOT + "client/render/world/floor/FloorRiteResponse.java");
		String events = RiteWorldSourceTest.read(ROOT + "client/data/CardinalRiteEventClientState.java");
		assertTrue(floor.contains("CardinalRiteEventKind.RING_OPENED"));
		assertTrue(floor.contains("RingOpeningMath.shockScale("));
		assertTrue(events.contains("RiteWorldState.ringOpened(packet.center(),"));
	}

	@Test
	void thePanelShowsSleepingRings() throws IOException {
		String overlay = RiteWorldSourceTest.read(ROOT + "client/screen/overlay/CardinalRiteOverlay.java");
		assertTrue(overlay.contains("pips(rite.getCompletedRings(), rite.getOpenRings(), rite.getTotalRings())"));
	}
}
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `./gradlew.bat test --tests "com.vincenthuto.hemomancy.client.render.world.rite.escalation.*" --tests "com.vincenthuto.hemomancy.client.screen.overlay.CardinalRiteRingPipsTest"`
Expected: FAIL (compilation: `ringOpened` and the 3-argument `pips` do not exist).

- [ ] **Step 3: `RingTear`**

`src/main/java/com/vincenthuto/hemomancy/client/render/world/rite/escalation/RingTear.java`:

```java
package com.vincenthuto.hemomancy.client.render.world.rite.escalation;

import com.vincenthuto.hemomancy.client.data.CardinalRiteEventClientState;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteEventKind;
import com.vincenthuto.hemomancy.common.rite.RingOpeningMath;
import net.minecraft.core.BlockPos;

/** How far the sores of a waking ring have torn open, read from its {@code RING_OPENED} beat. */
public final class RingTear {
	private RingTear() {
	}

	/** The beat that woke this ring, while its tear is still running; null otherwise. */
	public static CardinalRiteEventClientState.RiteEvent waking(BlockPos center, int rank, float partialTick) {
		for (CardinalRiteEventClientState.RiteEvent event : CardinalRiteEventClientState.recent(center)) {
			if (event.kind() != CardinalRiteEventKind.RING_OPENED || event.index() != rank) continue;
			float depth = RingOpeningMath.depth(rank, ringsOf(event.style()));
			if (event.age(partialTick) <= RingOpeningMath.tearTicks(depth)) return event;
		}
		return null;
	}

	/** The ring count a beat carries in its style. */
	public static int ringsOf(String style) {
		try {
			return Integer.parseInt(style);
		} catch (NumberFormatException ignored) {
			return 2;
		}
	}

	/** A sore's size as a share of full: the scar while asleep, growing through the tear, then full. */
	public static float size(boolean dormant, BlockPos center, int rank, float partialTick) {
		if (dormant) return RingOpeningMath.SCAR_SCALE;
		CardinalRiteEventClientState.RiteEvent event = waking(center, rank, partialTick);
		if (event == null) return 1.0F;
		float tear = RingOpeningMath.tear(event.age(partialTick), RingOpeningMath.depth(rank, ringsOf(event.style())));
		return RingOpeningMath.SCAR_SCALE + (1.0F - RingOpeningMath.SCAR_SCALE) * tear;
	}
}
```

- [ ] **Step 4: Sores**

In `AnchorSores.emit`, after `List<CardinalRiteCeremonyDefinition.Anchor> anchors = recipe.getCeremony().anchors();` add:

```java
		int[] ranks = com.vincenthuto.hemomancy.common.rite.CardinalRiteRingRules.ranksOf(anchors);
```

Replace

```java
			float target = ActiveRiteClientData.RiteEntry.anchorBlood(state)
					/ (float) CardinalRiteCeremonyRules.BLOOD_PER_ANCHOR_ML;
```

with

```java
			// A sleeping anchor is a closed scar; a woken one tears open and fills toward its own requirement.
			float target = rite.anchorDormant(i) ? 0.0F : ActiveRiteClientData.RiteEntry.anchorBlood(state)
					/ (float) Math.max(1, rite.anchorRequirement(i));
			float size = com.vincenthuto.hemomancy.client.render.world.rite.escalation.RingTear.size(
					rite.anchorDormant(i), origin, ranks[i], 0.0F);
```

Pass `size` to `emitSore`: change its signature to `emitSore(VertexConsumer consumer, Matrix4f matrix, int dx, float y, int dz, float size, int alpha)`. Inside it, use `float half = HALF * size;` in place of `HALF` for the four corners. Update the call to `emitSore(consumer, matrix, anchor.x(), ground + FloorMesh.SURFACE_Y + LIFT, anchor.z(), size, encode(shown, ActiveRiteClientData.RiteEntry.anchorDamaged(state)));`. Remove the now-unused `CardinalRiteCeremonyRules` import.

- [ ] **Step 5: Jets**

In `CardinalRiteAnchorJetRenderer.render`, after `List<CardinalRiteCeremonyDefinition.Anchor> anchors = recipe.getCeremony().anchors();` add `int[] ranks = com.vincenthuto.hemomancy.common.rite.CardinalRiteRingRules.ranksOf(anchors);`. Replace the per-anchor skip and draw with:

```java
				int state = states[i];
				boolean full = !rite.anchorDormant(i) && !ActiveRiteClientData.RiteEntry.anchorDamaged(state)
						&& ActiveRiteClientData.RiteEntry.anchorBlood(state) >= rite.anchorRequirement(i);
				// A waking ring's sores flare once as they tear open, before any blood reaches them.
				CardinalRiteEventClientState.RiteEvent waking = full || rite.anchorDormant(i) ? null
						: com.vincenthuto.hemomancy.client.render.world.rite.escalation.RingTear.waking(center, ranks[i], partialTick);
				if (!full && waking == null) continue;
				if (blood == null) {
					AnimusMortemRenderTypes.begin(time);
					blood = buffers.getBuffer(AnimusMortemRenderTypes.ANIMUS);
				}
				CardinalRiteCeremonyDefinition.Anchor anchor = anchors.get(i);
				drawJet(poseStack, blood, mc, camera, center.getX() + anchor.x() + 0.5D, center.getY() + BASE_Y,
						center.getZ() + anchor.z() + 0.5D, time, i,
						full ? burstAge(center, i, partialTick) : waking.age(partialTick));
```

Remove the now-unused `CardinalRiteCeremonyRules` import.

- [ ] **Step 6: Shock and surge**

In `FloorRiteResponse`, replace the impact block (lines 175-182) with:

```java
		// The staff strikes, the rite pushes the world a step further, or a ring tears open: one hard shock.
		CardinalRiteEventClientState.RiteEvent woke = CardinalRiteEventClientState.latest(origin,
				CardinalRiteEventKind.RING_OPENED);
		float wokeAge = woke == null ? -1.0F : woke.age(partialTick);
		float impact = youngest(youngest(
				CardinalRiteEventClientState.latestAge(origin, CardinalRiteEventKind.STAFF_IMPACT, partialTick),
				CardinalRiteEventClientState.latestAge(origin, CardinalRiteEventKind.ESCALATION, partialTick)), wokeAge);
		if (impact >= 0.0F) {
			boolean ring = wokeAge >= 0.0F && impact == wokeAge;
			float depth = ring ? com.vincenthuto.hemomancy.common.rite.RingOpeningMath.depth(woke.index(),
					com.vincenthuto.hemomancy.client.render.world.rite.escalation.RingTear.ringsOf(woke.style())) : 0.0F;
			rings[0] = CardinalRiteFloorVisualRules.shockRadius(impact,
					ring ? reach * com.vincenthuto.hemomancy.common.rite.RingOpeningMath.shockReach(depth) : reach);
			rings[1] = CardinalRiteFloorVisualRules.shockStrength(impact)
					* (ring ? com.vincenthuto.hemomancy.common.rite.RingOpeningMath.shockScale(depth) : 1.0F);
		}
```

If `RiteEvent` is already imported as `RiteEvent` in this file, the code may use the short name. Keep the reference to `RingOpeningMath.shockScale(` so the source test matches.

In `RiteWorldState`, add:

```java
	/** A ring tore open at this Focus: the world surges without stepping. Unknown Foci are ignored. */
	public static void ringOpened(BlockPos center, float surge) {
		Tracked tracked = TRACKED.get(center);
		if (tracked != null) tracked.surge = Math.max(tracked.surge, surge);
	}
```

In `CardinalRiteEventClientState.accept`, after `EVENTS.add(event);`:

```java
		if (packet.kind() == CardinalRiteEventKind.RING_OPENED) {
			com.vincenthuto.hemomancy.client.render.world.rite.escalation.RiteWorldState.ringOpened(packet.center(),
					com.vincenthuto.hemomancy.common.rite.RingOpeningMath.fogSurge(
							com.vincenthuto.hemomancy.common.rite.RingOpeningMath.depth(packet.index(),
									com.vincenthuto.hemomancy.client.render.world.rite.escalation.RingTear.ringsOf(packet.style()))));
		}
```

The sky wound's flash already follows `World.surge()` through `RiteWorldMath.woundFlash`. So the last ring (surge 1.0) flickers a formed wound with no further change.

- [ ] **Step 7: Pips**

In `CardinalRiteOverlay`, replace `pips(rite.getCompletedRings(), rite.getTotalRings())` with `pips(rite.getCompletedRings(), rite.getOpenRings(), rite.getTotalRings())` and replace the helper:

```java
	/** Fed rings solid, woken rings hollow diamonds, sleeping rings hollow circles. */
	static String pips(int complete, int open, int total) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < total; i++) result.append(i < complete ? "◆" : i < open ? "◇" : "○");
		return result.toString();
	}
```

- [ ] **Step 8: Run the tests to verify they pass, then compile**

Run: `./gradlew.bat test --tests "com.vincenthuto.hemomancy.client.render.world.rite.escalation.*" --tests "com.vincenthuto.hemomancy.client.screen.overlay.*" --tests "com.vincenthuto.hemomancy.client.render.world.floor.*"`
Expected: PASS.

Run: `./gradlew.bat compileJava compileTestJava compileGameTestJava`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 9: Ledger**

---

### Task 7: GameTest, fixtures, docs and full verification

**Files:**
- Create: `src/gameTest/java/com/vincenthuto/hemomancy/gametest/CardinalRiteRingGatingGameTests.java`
- Modify (only if they fail): GameTest fixtures that start multi-ring rites through real activation and fill every anchor at once
- Modify: `docs/HEMOMANCY_REFERENCE.md` (after "The world stage" paragraph), `wiki/Harbinger-Path.md`, `docs/CARDINAL_RITE_AUDIT.md`

**Interfaces:**
- Consumes: everything above.

- [ ] **Step 1: Write the GameTest**

```java
package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite;
import com.vincenthuto.hemomancy.common.rite.CardinalRitePhase;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

/** A two-ring rite through its ordeal, as the server saves it. */
@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CardinalRiteRingGatingGameTests {
	@GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "rite_ring_gating")
	public static void theSecondRingSleepsThenHoldsTheFirstWaveThroughASave(GameTestHelper helper) {
		var provider = helper.getLevel().registryAccess();
		ActiveCardinalRite rite = ActiveCardinalRite.interactive(UUID.randomUUID(), helper.absolutePos(BlockPos.ZERO),
				Hemomancy.rloc("cardinal_rite/ring_gating_fixture"), 1200, 5, 5, false, 2, 8);
		rite.configureRings(new int[] {0, 0, 0, 0, 1, 1, 1, 1}, false);

		helper.assertFalse(rite.fillAnchor(4, 63), "ring 1 sleeps through Consecration");
		for (int i = 0; i < 4; i++) rite.fillAnchor(i, 50);
		helper.assertTrue(rite.enterInscription(), "ring 0 alone consecrates");
		helper.assertTrue(rite.sealAltar(false) && rite.getPhase() == CardinalRitePhase.ORDEAL, "sealed into the ordeal");
		helper.assertTrue(rite.getOpenRings() == 2, "the first wave wakes ring 1");
		helper.assertFalse(rite.areAnchorsConsecrated(), "the wave cannot clear while ring 1 hungers");
		rite.fillAnchor(4, 63);

		ActiveCardinalRite copy = ActiveCardinalRite.deserialize(rite.serialize(provider), provider);

		helper.assertTrue(copy.getOpenRings() == 2 && copy.getAnchorBloodMl()[4] == 63, "the hold survives the save");
		helper.assertTrue(copy.nextRingToAnnounce() == 1, "the waking had not been announced before the save");
		helper.assertFalse(copy.areAnchorsConsecrated(), "still held after loading");
		for (int i = 5; i < 8; i++) copy.fillAnchor(i, 63);
		helper.assertTrue(copy.areAnchorsConsecrated(), "fed, the wave may clear");
		helper.succeed();
	}
}
```

- [ ] **Step 2: Run the GameTests**

Run: `./gradlew.bat runGameTestServer` (log: `build/phlegethontic-gametest/logs/latest.log`).
Expected: `theSecondRingSleepsThenHoldsTheFirstWaveThroughASave` passes. The known pre-existing failures are `blockitemexceptionskeeptheirowners` and `idlemorphlingsinkstofloor`; `mandatoryjigsawwingsassemble` is flaky.

Any other new failure comes from a fixture that starts a multi-ring rite through real activation (`BloodCraftingKeyPressPacket`), then fills all anchors in Consecration or skips ahead. Fix such fixtures to feed open rings at each phase, as a player would:

```java
for (int i = 0; i < rite.getAnchorBloodMl().length; i++) rite.fillAnchor(i, rite.bloodNeededForAnchor(i));
```

Repeat that loop after each phase advance the fixture forces. Do **not** call `configureRings` with a single rank and do not otherwise bypass gating. Ledger each fixture changed as a ruling.

- [ ] **Step 3: Docs**

In `docs/HEMOMANCY_REFERENCE.md`, after "The world stage" paragraph, add a paragraph headed **Rings wake one phase at a time**. It should state:
- Ring 0 opens in Consecration.
- Each later ring wakes as the sealed rite enters an opener (station projection, each ordeal wave, each still interval, the procession); leftovers wake at the last opener; with no openers, later rings wake at Inscription.
- The phase a ring wakes in cannot end until that ring is fed.
- Costs: 50/63/75/88/100/113/125/138 mL by rank.
- Sleeping anchors take no blood, never break and add no pressure.
- Older saves keep every ring open at 50 mL.

In `wiki/Harbinger-Path.md`, add a player-facing paragraph with the same rules, in the voice of the existing rite paragraphs. In `docs/CARDINAL_RITE_AUDIT.md`, add a status line `Ring gating (2026-10-13): later rings wake per phase; see HEMOMANCY_REFERENCE "Rings wake one phase at a time".`

- [ ] **Step 4: Full verification**

Run: `./gradlew.bat test`
Expected: the same 16 pre-existing failures as before this plan, and nothing new:
- HematicArmatureModelSourceTest ×2, VialCentrifugeModelSourceTest ×2, MnemonicReliquaryModelSourceTest
- PhlegethonticBombardierResourceTest, BloodProfileMigrationTest, ClientModelTextureCoverageTest
- EnglishLocalizationEncodingTest, GameTestHarnessSourceContractTest ×2
- LegacyMainTestAdapterTest ×4, LegacyMainTestSuiteContractTest

No shader changes in this plan, so no native GPU check is required. If an implementer changes a shader anyway, launch `./gradlew.bat runSuccessionReviewClient` and grep its log for `Couldn't compile`.

- [ ] **Step 5: Ledger and finish** (no commit; user preference)
