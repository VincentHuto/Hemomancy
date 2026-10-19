package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class VesperBurrowRulesTest {
	@Test
	void groundHuntIsTenToFifteenSeconds() {
		for (long seed = 0; seed < 100; seed++) {
			int hunt = VesperBurrowRules.Plan.ground(RandomSource.create(seed)).huntTicks();
			assertTrue(hunt >= 200 && hunt <= 300, "hunt " + hunt);
		}
	}

	@Test
	void diveHuntIsFiveToSevenSeconds() {
		for (long seed = 0; seed < 100; seed++) {
			VesperBurrowRules.Plan plan = VesperBurrowRules.Plan.dive(RandomSource.create(seed));
			assertTrue(plan.fromDive());
			assertTrue(plan.huntTicks() >= 100 && plan.huntTicks() <= 140, "hunt " + plan.huntTicks());
		}
	}

	@Test
	void strikesAlternateStartingWithClaw() {
		assertEquals(VesperBurrowRules.Strike.CLAW, VesperBurrowRules.strikeAt(0));
		assertEquals(VesperBurrowRules.Strike.TAIL, VesperBurrowRules.strikeAt(1));
		assertEquals(VesperBurrowRules.Strike.CLAW, VesperBurrowRules.strikeAt(2));
	}

	@Test
	void stagesRunDiveHuntRumbleEruptionRecoveryInOrder() {
		VesperBurrowRules.Plan plan = new VesperBurrowRules.Plan(200, false);
		assertEquals(VesperBurrowRules.Stage.DIVE, VesperBurrowRules.stageAt(plan, 0));
		assertEquals(VesperBurrowRules.Stage.HUNT, VesperBurrowRules.stageAt(plan, VesperBurrowRules.DIVE_TICKS));
		int rumble = VesperBurrowRules.rumbleStart(plan);
		assertEquals(VesperBurrowRules.DIVE_TICKS + 200, rumble);
		assertEquals(VesperBurrowRules.Stage.RUMBLE, VesperBurrowRules.stageAt(plan, rumble));
		assertEquals(VesperBurrowRules.Stage.ERUPTION,
				VesperBurrowRules.stageAt(plan, rumble + VesperBurrowRules.RUMBLE_TICKS));
		assertEquals(VesperBurrowRules.Stage.RECOVERY, VesperBurrowRules.stageAt(plan,
				rumble + VesperBurrowRules.RUMBLE_TICKS + VesperBurrowRules.ERUPTION_TICKS));
		assertEquals(VesperBurrowRules.Stage.COMPLETE, VesperBurrowRules.stageAt(plan, VesperBurrowRules.total(plan)));
		assertTrue(VesperBurrowRules.RECOVERY_TICKS >= 60 && VesperBurrowRules.RECOVERY_TICKS <= 80,
				"three to four seconds to gather himself");
	}

	@Test
	void everyStrikeIsPrecededByItsTell() {
		VesperBurrowRules.Plan plan = new VesperBurrowRules.Plan(260, false);
		int strikes = 0;
		for (int tick = 1; tick < VesperBurrowRules.total(plan); tick++) {
			VesperBurrowRules.Stage stage = VesperBurrowRules.stageAt(plan, tick);
			VesperBurrowRules.Stage previous = VesperBurrowRules.stageAt(plan, tick - 1);
			if (stage == VesperBurrowRules.Stage.STRIKE && previous != VesperBurrowRules.Stage.STRIKE) {
				strikes++;
				for (int t = 1; t <= VesperBurrowRules.STRIKE_TELL_TICKS; t++) {
					assertEquals(VesperBurrowRules.Stage.STRIKE_TELL, VesperBurrowRules.stageAt(plan, tick - t));
				}
			}
		}
		assertTrue(strikes >= 4, "a ten-second hunt surfaces several times");
	}

	@Test
	void diveBurrowSurfacesOnceOrTwice() {
		for (int hunt : new int[] { 100, 140 }) {
			VesperBurrowRules.Plan plan = new VesperBurrowRules.Plan(hunt, true);
			int strikes = 0;
			for (int tick = 1; tick < VesperBurrowRules.total(plan); tick++) {
				if (VesperBurrowRules.stageAt(plan, tick) == VesperBurrowRules.Stage.STRIKE
						&& VesperBurrowRules.stageAt(plan, tick - 1) != VesperBurrowRules.Stage.STRIKE) strikes++;
			}
			assertTrue(strikes >= 1 && strikes <= 2, "strikes " + strikes);
		}
	}

	@Test
	void rumbleLockHappensBeforeEruptionWithReactionTime() {
		assertTrue(VesperBurrowRules.RUMBLE_TICKS - VesperBurrowRules.RUMBLE_LOCK_TICK >= 12);
		VesperBurrowRules.Plan plan = new VesperBurrowRules.Plan(200, false);
		assertEquals(VesperBurrowRules.rumbleStart(plan) + VesperBurrowRules.RUMBLE_LOCK_TICK,
				VesperBurrowRules.lockTick(plan));
	}

	@Test
	void trailClampedToArena() {
		Vec3 clamped = VesperBurrowRules.clampTrail(new Vec3(100.0D, 70.0D, -100.0D), 0.0D, 64.0D, 0.0D);
		assertEquals(VesperWingedFlightRules.SAFE_ARENA_HALF_EXTENT, clamped.x, 1.0E-6D);
		assertEquals(-VesperWingedFlightRules.SAFE_ARENA_HALF_EXTENT, clamped.z, 1.0E-6D);
		assertEquals(65.0D, clamped.y, 1.0E-6D);
	}

	@Test
	void stateRoundTripsThroughNbt() {
		VesperBurrowRules.State state = new VesperBurrowRules.State(57, new VesperBurrowRules.Plan(222, true),
				3.5D, -7.25D, 1.0D, 2.0D);
		CompoundTag tag = new CompoundTag();
		state.save(tag);
		assertEquals(state, VesperBurrowRules.State.load(tag));
		assertNull(VesperBurrowRules.State.load(new CompoundTag()));
	}

	@Test
	void reloadMidBurrowSurfacesIntoRecovery() {
		VesperBurrowRules.Plan plan = new VesperBurrowRules.Plan(222, false);
		VesperBurrowRules.State hunting = new VesperBurrowRules.State(80, plan, 3.5D, -7.25D, 0.0D, 0.0D);
		VesperBurrowRules.State resumed = VesperBurrowRules.afterReload(hunting);
		assertEquals(VesperBurrowRules.Stage.RECOVERY, VesperBurrowRules.stageAt(plan, resumed.tick()));
		assertEquals(3.5D, resumed.x(), 1.0E-6D);
	}

	@Test
	void clawsPierceArmorWhileTheTailPoisonsAndBleeds() {
		VesperBurrowRules.StrikeEffect claw = VesperBurrowRules.effects(VesperBurrowRules.Strike.CLAW);
		VesperBurrowRules.StrikeEffect tail = VesperBurrowRules.effects(VesperBurrowRules.Strike.TAIL);
		assertTrue(claw.armorPiercing());
		assertEquals(9.0F, claw.damage(), 1.0E-5F);
		assertEquals(0, claw.poisonTicks());
		assertFalse(tail.armorPiercing());
		assertEquals(6.0F, tail.damage(), 1.0E-5F);
		assertEquals(100, tail.poisonTicks());
		assertEquals(160, tail.bloodLossTicks());
		assertEquals(22.0F, VesperBurrowRules.ERUPTION_DAMAGE, 1.0E-5F);
		assertEquals(1.6D, VesperBurrowRules.ERUPTION_LAUNCH, 1.0E-6D);
	}

	@Test
	void targetLostDuringHuntGoesToRumbleAtCurrentPosition() {
		VesperBurrowRules.Plan plan = new VesperBurrowRules.Plan(250, false);
		VesperBurrowRules.State hunting = new VesperBurrowRules.State(70, plan, 1.0D, 2.0D, 0.0D, 0.0D);
		VesperBurrowRules.State lost = VesperBurrowRules.onTargetLost(hunting);
		assertEquals(VesperBurrowRules.Stage.RUMBLE, VesperBurrowRules.stageAt(plan, lost.tick()));
		assertEquals(1.0D, lost.lockX(), 1.0E-6D);
		assertEquals(2.0D, lost.lockZ(), 1.0E-6D);
	}
}
