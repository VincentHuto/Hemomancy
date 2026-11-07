package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.client.render.world.PutricielBloomRules.Burn;
import com.vincenthuto.hemomancy.client.render.world.PutricielBloomRules.Cap;
import com.vincenthuto.hemomancy.client.render.world.PutricielBloomRules.Look;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Saint Putriciel's visual pass 2 rules: a rot telegraph emerges, spreads, fruits, bursts at the commit, stands and
 * dries away; a fire telegraph chars out, runs white-hot at the commit and cools to ash; the spreading front is ragged
 * but ordered; growths unfurl past full, settle, quiver, wither and are gone; debris arcs and settles; the reach lashes
 * out and snaps back; nothing is drawn outside its life.
 */
class PutricielBloomRulesTest {
	private static final int REVEAL = 0, WINDUP = 20, ACTIVE = 6;

	@Test
	void aRotTelegraphRunsItsWholeLife() {
		assertSame(Look.NONE, PutricielBloomRules.look(-1.0F, REVEAL, WINDUP, ACTIVE), "nothing before the reveal");
		Look previous = PutricielBloomRules.look(0.0F, REVEAL, WINDUP, ACTIVE);
		for (float t = 1.0F; t < WINDUP; t += 1.0F) {
			Look look = PutricielBloomRules.look(t, REVEAL, WINDUP, ACTIVE);
			assertTrue(look.grow() >= previous.grow() && look.wet() >= previous.wet() && look.spore() >= previous.spore(),
					"the colony only spreads, pools and ripens through the wind-up at " + t);
			assertEquals(0.0F, look.heave(), "no heave before the commit");
			previous = look;
		}
		assertEquals(1.0F, PutricielBloomRules.look(WINDUP * PutricielBloomRules.SPREAD_LEAD + 0.01F, REVEAL, WINDUP, ACTIVE).grow(), 1.0E-3F,
				"the colony has taken the whole footprint by the spread lead");
		Look flash = PutricielBloomRules.look(WINDUP, REVEAL, WINDUP, ACTIVE);
		assertEquals(1.0F, flash.heave(), 1.0E-4F, "the commit heaves");
		assertEquals(1.0F, flash.spore(), 1.0E-4F, "and fruits in full");
		assertTrue(flash.width() > 1.0F, "a trench yawns at the flash");
		Look standing = PutricielBloomRules.look(WINDUP + ACTIVE - 0.5F, REVEAL, WINDUP, ACTIVE);
		assertEquals(1.0F, standing.alpha());
		assertEquals(1.0F, standing.wet());
		float end = PutricielBloomRules.lastTick(WINDUP, ACTIVE);
		assertTrue(PutricielBloomRules.look(end - 8.0F, REVEAL, WINDUP, ACTIVE).wet() < 1.0F, "it dries through the linger");
		assertTrue(PutricielBloomRules.look(end - 1.0F, REVEAL, WINDUP, ACTIVE).alpha() < 0.1F, "and fades");
		assertFalse(PutricielBloomRules.look(end + 0.01F, REVEAL, WINDUP, ACTIVE).visible(), "gone after the linger");
	}

	@Test
	void aFireTelegraphCharsHeatsAndCools() {
		assertSame(Burn.NONE, PutricielBloomRules.burn(-1.0F, REVEAL, WINDUP, ACTIVE));
		float lastHeat = 0.0F;
		for (float t = 0.0F; t < WINDUP; t += 1.0F) {
			Burn burn = PutricielBloomRules.burn(t, REVEAL, WINDUP, ACTIVE);
			assertTrue(burn.heat() >= lastHeat, "the heat only climbs through the wind-up");
			assertTrue(burn.heat() < PutricielBloomRules.WHITE_HOT, "never white-hot before the commit");
			lastHeat = burn.heat();
		}
		assertTrue(PutricielBloomRules.burn(WINDUP, REVEAL, WINDUP, ACTIVE).heat() >= PutricielBloomRules.WHITE_HOT, "white-hot at the commit");
		Burn cooling = PutricielBloomRules.burn(WINDUP + ACTIVE + PutricielBloomRules.COOL * 0.5F, REVEAL, WINDUP, ACTIVE);
		Burn standing = PutricielBloomRules.burn(WINDUP + ACTIVE - 0.5F, REVEAL, WINDUP, ACTIVE);
		assertTrue(cooling.heat() < standing.heat() && cooling.ash() > standing.ash(), "the embers cool to ash");
		assertFalse(PutricielBloomRules.burn(WINDUP + ACTIVE + PutricielBloomRules.COOL + 0.1F, REVEAL, WINDUP, ACTIVE).visible());
	}

	@Test
	void theFrontIsTakenBehindAndClearAhead() {
		assertEquals(1.0F, PutricielBloomRules.front(0.0D, 5.0D, 1.0D), 1.0E-6F);
		assertEquals(0.5F, PutricielBloomRules.front(5.0D, 5.0D, 1.0D), 1.0E-6F);
		assertEquals(0.0F, PutricielBloomRules.front(6.5D, 5.0D, 1.0D), 1.0E-6F);
		assertEquals(0.0F, PutricielBloomRules.front(0.0D, 0.0D, 1.0D), "nothing is taken before the front starts");
		for (double d = 0.0D; d < 8.0D; d += 0.25D) {
			assertTrue(PutricielBloomRules.front(d, 4.0D, 1.0D) >= PutricielBloomRules.front(d + 0.25D, 4.0D, 1.0D), "falling outward");
		}
	}

	@Test
	void rosettesAreTieredVariedAndStable() {
		Set<Long> shapes = new HashSet<>();
		for (int seed = 0; seed < 40; seed++) {
			List<Cap> caps = PutricielBloomRules.rosette(seed, 1.0D, 6);
			assertEquals(6, caps.size());
			assertEquals(caps, PutricielBloomRules.rosette(seed, 1.0D, 6), "the same seed grows the same rosette");
			int lastTier = 0;
			for (Cap cap : caps) {
				assertTrue(cap.radius() > 0.2D && cap.radius() < 0.7D, "a cap about the rosette's scale: " + cap.radius());
				assertTrue(cap.tilt() > 0.0D, "caps tilt up off the floor");
				assertTrue(cap.span() > 1.5D && cap.span() < Math.PI, "a fan, not a disc");
				assertTrue(cap.tier() >= lastTier, "tiers run upward");
				assertTrue(cap.lift() >= 0.0D && cap.lift() < 0.6D);
				lastTier = cap.tier();
			}
			assertEquals(2, caps.get(caps.size() - 1).tier(), "six caps make three tiers");
			assertTrue(caps.get(caps.size() - 1).delay() > caps.get(0).delay(), "the upper tiers unfurl a beat later");
			shapes.add(Double.doubleToLongBits(caps.get(0).radius()));
		}
		assertTrue(shapes.size() > 30, "no two rosettes alike");
	}

	@Test
	void growthsUnfurlSettleQuiverAndWither() {
		assertEquals(0.0F, PutricielBloomRules.unfurl(0.0F));
		float peak = 0.0F;
		for (float age = 0.0F; age <= PutricielBloomRules.UNFURL + PutricielBloomRules.SETTLE + 2.0F; age += 0.25F) {
			peak = Math.max(peak, PutricielBloomRules.unfurl(age));
		}
		assertTrue(peak > 1.05F && peak <= PutricielBloomRules.OVERSHOOT + 1.0E-4F, "it swells past full size: " + peak);
		assertEquals(1.0F, PutricielBloomRules.unfurl(PutricielBloomRules.UNFURL + PutricielBloomRules.SETTLE + 1.0F), 1.0E-4F, "and settles");
		assertEquals(0.0D, PutricielBloomRules.quiver(1.0F, 30.0D, 3), "no quiver while it unfurls");
		assertTrue(Math.abs(PutricielBloomRules.quiver(60.0F, 30.0D, 3)) < 0.002D, "the quiver dies away");
		float witherAt = 20.0F;
		assertEquals(1.0F, PutricielBloomRules.formation(10.0F, witherAt), 1.0E-4F, "whole while it stands");
		assertEquals(0.0F, PutricielBloomRules.formation(0.0F, witherAt), "nothing before it breaks out");
		assertEquals(0.0F, PutricielBloomRules.formation(witherAt + PutricielBloomRules.WITHER, witherAt), 1.0E-4F, "crumbled away");
		assertEquals(0.5F, PutricielBloomRules.size(witherAt + PutricielBloomRules.WITHER + 1.0F, witherAt), 1.0E-4F, "shrunk by half as it went");
	}

	@Test
	void sacsSwellAndTearOpen() {
		assertEquals(0.0F, PutricielBloomRules.swell(0.0F, 0.0D, 1));
		assertTrue(PutricielBloomRules.swell(PutricielBloomRules.SWELL, 0.0D, 1) > 0.9F);
		assertEquals(1.0F, PutricielBloomRules.tear(-2.0F), "whole until it bursts");
		assertTrue(PutricielBloomRules.tear(PutricielBloomRules.BURST * 0.5F) < 1.0F, "torn from the top");
		assertEquals(0.0F, PutricielBloomRules.tear(PutricielBloomRules.BURST + PutricielBloomRules.SHRIVEL + 0.1F), 1.0E-4F, "shrivelled");
		assertEquals(1.0F, PutricielBloomRules.breach(PutricielBloomRules.BREACH + 1.0F), 1.0E-4F);
		assertEquals(0.0F, PutricielBloomRules.breach(-1.0F));
	}

	@Test
	void debrisArcsSettlesAndGoes() {
		for (int i = 0; i < 8; i++) {
			assertNull(PutricielBloomRules.clod(3, i, -0.5F, 1.5D));
			assertNull(PutricielBloomRules.clod(3, i, PutricielBloomRules.CLOD_LIFE, 1.5D));
			double top = 0.0D;
			for (float age = 0.0F; age < PutricielBloomRules.CLOD_LIFE; age += 0.5F) {
				double[] clod = PutricielBloomRules.clod(3, i, age, 1.5D);
				assertNotNull(clod);
				assertTrue(clod[1] >= 0.0D, "never under the floor");
				top = Math.max(top, clod[1]);
			}
			assertTrue(top > 0.1D, "it flies");
			assertEquals(0.0D, PutricielBloomRules.clod(3, i, PutricielBloomRules.CLOD_LIFE - 0.5F, 1.5D)[1], 1.0E-9D, "and has landed");
			double[] early = PutricielBloomRules.puff(3, i, 1.0F, 1.0D), late = PutricielBloomRules.puff(3, i, PutricielBloomRules.PUFF_LIFE - 1.0F, 1.0D);
			assertTrue(late[3] > early[3] && late[4] > early[4], "a puff spreads and thins");
			assertNull(PutricielBloomRules.puff(3, i, PutricielBloomRules.PUFF_LIFE, 1.0D));
		}
	}

	@Test
	void theReachLashesOutHoldsAndSnapsBack() {
		assertEquals(-1.0F, PutricielBloomRules.reachHead(-0.1F, 5));
		assertTrue(PutricielBloomRules.reachHead(1.0F, 5) > 0.5F, "it lashes out fast");
		assertEquals(1.0F, PutricielBloomRules.reachHead(4.0F, 5));
		assertTrue(PutricielBloomRules.reachHead(5.0F + PutricielBloomRules.REACH_BACK * 0.5F, 5) < 1.0F, "and snaps back");
		assertEquals(-1.0F, PutricielBloomRules.reachHead(5.0F + PutricielBloomRules.REACH_BACK, 5));
		assertEquals(0.0F, PutricielBloomRules.wake(-PutricielBloomRules.WAKE_LEAD - 0.1F, 6));
		assertEquals(1.0F, PutricielBloomRules.wake(3.0F, 6));
		assertEquals(0.0F, PutricielBloomRules.wake(6.0F + PutricielBloomRules.WAKE_LINGER, 6), 1.0E-6F);
		assertEquals(0.0F, PutricielBloomRules.fray(3.0F, 6));
		assertEquals(1.0F, PutricielBloomRules.fray(6.0F + PutricielBloomRules.WAKE_LINGER, 6), 1.0E-6F);
		assertEquals(2.0D, PutricielBloomRules.arc(0.5D, 2.0D), 1.0E-9D, "a throw peaks half way");
		assertEquals(0.0D, PutricielBloomRules.arc(1.0D, 2.0D), 1.0E-9D);
	}

	@Test
	void fireLeapsAndShocksRunOut() {
		assertEquals(0.0F, PutricielBloomRules.flare(0.0F));
		assertTrue(PutricielBloomRules.flare(PutricielBloomRules.FLARE) > 1.2F, "a tongue leaps past its height");
		assertEquals(1.0F, PutricielBloomRules.flare(PutricielBloomRules.FLARE * 2.0F + 1.0F), 1.0E-4F, "and burns steady");
		assertTrue(PutricielBloomRules.shockRadius(-1.0F, 3.0D) < 0.0D);
		assertEquals(3.0D, PutricielBloomRules.shockRadius(PutricielBloomRules.SHOCK, 3.0D), 1.0E-6D);
		assertTrue(PutricielBloomRules.shockRadius(PutricielBloomRules.SHOCK + 0.1F, 3.0D) < 0.0D);
		for (int i = 0; i < 1000; i++) {
			double u = PutricielBloomRules.unit(7, i);
			assertTrue(u >= 0.0D && u < 1.0D);
			assertEquals(u, PutricielBloomRules.unit(7, i));
		}
	}

	// ── rot sludge and the burning body's shed (revision 2026-11-06) ──

	@Test
	void aPoolHasBodyARaisedLipAndALobedRim() {
		assertEquals(PutricielBloomRules.SLUDGE_DEPTH, PutricielBloomRules.sludgeProfile(0.0D), 1.0E-9D, "0.18 thick at the centre");
		assertEquals(0.0D, PutricielBloomRules.sludgeProfile(1.0D), "down to the floor at the rim");
		double max = 0.0D;
		for (double rho = 0.0D; rho <= 1.0D; rho += 0.005D) {
			double h = PutricielBloomRules.sludgeProfile(rho);
			assertTrue(h >= 0.0D);
			max = Math.max(max, h);
		}
		assertTrue(max <= PutricielBloomRules.SLUDGE_DEPTH + 1.0E-9D);
		assertEquals(PutricielBloomRules.SLUDGE_LIP, PutricielBloomRules.sludgeProfile(0.88D), 2.0E-3D, "the meniscus lip");
		assertTrue(PutricielBloomRules.sludgeProfile(0.88D) > PutricielBloomRules.sludgeProfile(0.84D) + 0.015D, "raised over the shallows");
		assertTrue(PutricielBloomRules.sludgeProfile(0.96D) < PutricielBloomRules.sludgeProfile(0.9D), "then rounds down to the floor");
		for (int seed = 0; seed < 40; seed++) {
			int peaks = 0;
			double previous = PutricielBloomRules.lobe(seed, -Math.PI * 2.0D / 360.0D), here = PutricielBloomRules.lobe(seed, 0.0D);
			for (int i = 1; i <= 360; i++) {
				double next = PutricielBloomRules.lobe(seed, i * Math.PI * 2.0D / 360.0D);
				assertTrue(next >= PutricielBloomRules.LOBE_MIN - 1.0E-9D && next <= PutricielBloomRules.LOBE_MAX + 1.0E-9D);
				if (here > previous && here >= next) peaks++;
				previous = here;
				here = next;
			}
			assertTrue(peaks >= 5 && peaks <= 9, "five to seven lobes, give or take a ripple: " + peaks);
		}
	}

	@Test
	void aPoolFormsWithAnOvershootBubblesAndSettles() {
		assertFalse(PutricielBloomRules.sludge(-1.0F).visible());
		assertFalse(PutricielBloomRules.sludge(0.0F).visible(), "it spreads from the splash");
		assertEquals(1.0F, PutricielBloomRules.sludge(PutricielBloomRules.SLUDGE_FORM).spread(), 1.0E-4F, "out to its rim by 8");
		float peak = 0.0F;
		for (float t = 0.0F; t < 20.0F; t += 0.25F) peak = Math.max(peak, PutricielBloomRules.sludge(t).thick());
		assertEquals(PutricielBloomRules.SLUDGE_OVERSHOOT, peak, 0.03F, "its body overshoots");
		assertEquals(1.0F, PutricielBloomRules.sludge(40.0F).thick(), 1.0E-4F, "and settles");
		assertEquals(1.0F, PutricielBloomRules.sludge(40.0F).gloss(), "wet and glossy in the rot");
		assertEquals(0.0F, PutricielBloomRules.sludge(400.0F).charred(), "no char, no fire, in the rot");
		var start = PutricielBloomRules.settling(0.0F, PutricielBloomRules.SLUDGE_SETTLE);
		var late = PutricielBloomRules.settling(PutricielBloomRules.SLUDGE_SETTLE - 0.1F, PutricielBloomRules.SLUDGE_SETTLE);
		assertEquals(1.0F, start.gloss(), 1.0E-4F);
		assertTrue(late.gloss() < 0.05F, "the gloss dies as it settles");
		assertEquals(PutricielBloomRules.SETTLED_THIN, late.thick(), 0.01F, "it thins to a fifth");
		assertFalse(PutricielBloomRules.settling(PutricielBloomRules.SLUDGE_SETTLE, PutricielBloomRules.SLUDGE_SETTLE).visible());
		int bursts = 0;
		boolean swelling = false;
		for (float t = 0.0F; t < 400.0F; t += 0.5F) {
			double[] bubble = PutricielBloomRules.bubble(9, 0, t);
			if (bubble == null) continue;
			assertTrue(bubble[2] >= 0.0D && bubble[2] <= 1.0D && Math.hypot(bubble[0], bubble[1]) <= 0.56D);
			if (bubble[3] < 0.0D) swelling = true;
			else if (swelling) {
				bursts++;
				swelling = false;
			}
		}
		assertTrue(bursts >= 9 && bursts <= 17, "a bubble every 25 to 40 ticks: " + bursts);
	}

	@Test
	void theFireTakesAPoolFromItsCatchPointToAsh() {
		assertEquals(0.0F, PutricielBloomRules.sludgeChar(-0.5F, 5.0F), "untouched ahead of the front");
		assertEquals(PutricielBloomRules.BOIL_CHAR, PutricielBloomRules.sludgeChar(24.0F, 29.0F), 1.0E-3F, "charred behind it through the boil");
		assertEquals(1.0F, PutricielBloomRules.sludgeChar(0.0F, PutricielBloomRules.SLUDGE_ASH_AT), 1.0E-4F, "a cold crust by the ash");
		assertEquals(1.5D, PutricielBloomRules.catchFront(PutricielBloomRules.SLUDGE_CATCH, 1.5D), 1.0E-9D, "a sixth of the radius a tick");
		assertEquals(0.0F, PutricielBloomRules.sludgeBurn(PutricielBloomRules.SLUDGE_CATCH * 2.0F).gloss(), 1.0E-4F, "the gloss boils off");
		assertEquals(0.08F / 0.18F, PutricielBloomRules.sludgeBurn(PutricielBloomRules.SLUDGE_BOIL_END).thick(), 0.01F, "thinned to 0.08");
		assertTrue(PutricielBloomRules.sludgeBurn(PutricielBloomRules.SLUDGE_BOIL_END).curl() > 0.95F, "its lobes curled up");
		assertTrue(PutricielBloomRules.sludgeBurn(PutricielBloomRules.SLUDGE_BURN - 1.0F).thick() < 0.05F, "crumbled and sunk");
		assertFalse(PutricielBloomRules.sludgeBurn(PutricielBloomRules.SLUDGE_BURN).visible(), "gone by 50");
		// the fire over it: low along the front, tall through the boil, embers through the char, out at the crumble
		assertTrue(PutricielBloomRules.sludgeFire(2.0F) * 1.1F <= 0.5F + 1.0E-4F);
		assertEquals(1.0F, PutricielBloomRules.sludgeFire(15.0F), 1.0E-4F);
		assertEquals(0.2F / 1.1F, PutricielBloomRules.sludgeFire(PutricielBloomRules.SLUDGE_ASH_AT), 1.0E-3F);
		assertEquals(0.0F, PutricielBloomRules.sludgeFire(PutricielBloomRules.SLUDGE_ASH_AT + 4.0F));
		// up to two bursts a tick through the boil
		for (float b = PutricielBloomRules.SLUDGE_CATCH; b < PutricielBloomRules.SLUDGE_BOIL_END; b += 1.0F) {
			int bursting = 0;
			for (int slot = 0; slot < PutricielBloomRules.BOIL_SLOTS; slot++) {
				double[] bubble = PutricielBloomRules.boilBubble(3, slot, b + 0.25F);
				if (bubble != null && bubble[3] >= 0.0D && bubble[3] < 1.0D) bursting++;
			}
			assertTrue(bursting <= 2, "at most two bursts a tick: " + bursting + " at " + b);
		}
		assertNull(PutricielBloomRules.boilBubble(3, 0, 2.0F), "no boil before the catch is done");
		// the scorch on the ash cools and fades by 110
		assertEquals(1.0F, PutricielBloomRules.coolingAlpha(PutricielBloomRules.SLUDGE_BURN + 10.0F), 1.0E-4F);
		assertEquals(0.0F, PutricielBloomRules.coolingAlpha(PutricielBloomRules.SLUDGE_BURN + PutricielBloomRules.SLUDGE_COOL), 1.0E-4F);
		assertEquals(0.0F, PutricielBloomRules.coolingHeat(PutricielBloomRules.SLUDGE_BURN + PutricielBloomRules.SLUDGE_COOL), 1.0E-4F);
	}

	@Test
	void aRunnerCrawlsToItsPoolAndGoesOut() {
		assertEquals(0.0D, PutricielBloomRules.runnerHead(0.0F, 5.0D));
		assertEquals(PutricielBloomRules.RUNNER_SPEED * 4.0D, PutricielBloomRules.runnerHead(4.0F, 5.0D), 1.0E-9D);
		assertEquals(5.0D, PutricielBloomRules.runnerHead(100.0F, 5.0D), 1.0E-9D);
		float arrive = (float) (5.0D / PutricielBloomRules.RUNNER_SPEED);
		assertTrue(PutricielBloomRules.runnerFire(arrive * 0.5F, 5.0D) > 0.9F);
		assertEquals(0.0F, PutricielBloomRules.runnerFire(arrive + 10.5F, 5.0D), 1.0E-4F);
	}

	@Test
	void fragmentsFallBounceOnceAndSmoulderOut() {
		for (int i = 0; i < 5; i++) {
			double drop = 2.6D;
			int floorings = 0;
			boolean onFloor = false;
			double[] last = null;
			for (float age = 0.0F; age < 120.0F; age += 0.25F) {
				double[] piece = PutricielBloomRules.fragment(11, i, age, drop);
				if (piece == null) break;
				assertTrue(piece[1] >= -drop - 1.0E-9D, "never through the floor");
				boolean down = piece[1] <= -drop + 1.0E-3D;
				if (down && !onFloor) floorings++;
				onFloor = down;
				last = piece;
			}
			assertNotNull(last);
			assertTrue(floorings >= 1 && floorings <= 2, "it falls, bounces once and lies: " + floorings);
			assertTrue(last[4] >= 0.0D && last[4] <= PutricielBloomRules.SMOULDER, "it smoulders, then it is gone");
		}
		assertNull(PutricielBloomRules.fragment(11, 0, -1.0F, 2.0D));
	}

	@Test
	void ashStreamsAtItsRate() {
		int most = 0;
		for (float age = 0.0F; age < 200.0F; age += 0.7F) {
			int alive = 0;
			for (int i = 0; i < 12; i++) if (PutricielBloomRules.flake(5, i, age, 6.0F) != null) alive++;
			most = Math.max(most, alive);
		}
		assertTrue(most >= 8 && most <= 10, "six a second for 30 ticks: " + most);
		assertNull(PutricielBloomRules.flake(5, 0, 50.0F, 0.0F));
	}
}
