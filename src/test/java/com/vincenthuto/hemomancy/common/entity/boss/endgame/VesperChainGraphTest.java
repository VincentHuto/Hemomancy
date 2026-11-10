package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

final class VesperChainGraphTest {
	private static VesperChainGraph.Context ctx(double distance, boolean landed, boolean shielding, int length,
			long seed) {
		return new VesperChainGraph.Context(distance, landed, shielding, length, RandomSource.create(seed));
	}

	@Test
	void everyWeaponHasOpenerFollowUpAndFinisherAndThreeToFourMoves() {
		for (EnumBloodTemper temper : EnumBloodTemper.values()) {
			List<VesperWeaponAction> moves = VesperChainGraph.moveset(temper);
			assertTrue(moves.size() >= 3 && moves.size() <= 4, temper + " has " + moves.size());
			Set<VesperChainGraph.Role> roles = EnumSet.noneOf(VesperChainGraph.Role.class);
			for (VesperWeaponAction move : moves) {
				assertEquals(temper, move.temper());
				roles.add(move.role());
			}
			assertEquals(EnumSet.allOf(VesperChainGraph.Role.class), roles, temper.name());
		}
	}

	@Test
	void nextStartsFromThePreviousExitPose() {
		for (EnumBloodTemper temper : EnumBloodTemper.values()) {
			for (VesperWeaponAction previous : VesperChainGraph.moveset(temper)) {
				if (previous.role() == VesperChainGraph.Role.FINISHER) continue;
				for (long seed = 0; seed < 20; seed++) {
					VesperWeaponAction next = VesperChainGraph.next(previous, temper, VesperEveningStarAct.LOCKED,
							ctx(4.0D, true, false, 1, seed));
					assertNotEquals(VesperWeaponAction.NONE, next);
					if (next.role() != VesperChainGraph.Role.OPENER) assertEquals(previous.exit(), next.entry(),
							previous + " -> " + next);
				}
			}
		}
	}

	@Test
	void lockedActStaysInTheStanceWeapon() {
		for (EnumBloodTemper temper : EnumBloodTemper.values()) {
			VesperWeaponAction previous = VesperWeaponAction.NONE;
			for (int step = 0; step < 12; step++) {
				VesperWeaponAction next = VesperChainGraph.next(previous, temper, VesperEveningStarAct.LOCKED,
						ctx(5.0D, step % 2 == 0, false, step % 5, step));
				assertEquals(temper, next.temper());
				previous = next;
			}
		}
	}

	@Test
	void openActCrossesSchoolsAtBlendPoints() {
		boolean crossed = false;
		for (long seed = 0; seed < 200 && !crossed; seed++) {
			VesperWeaponAction previous = VesperChainGraph.moveset(EnumBloodTemper.ANIMUS).getFirst();
			VesperWeaponAction next = VesperChainGraph.next(previous, EnumBloodTemper.ANIMUS,
					VesperEveningStarAct.OPEN, ctx(4.0D, true, false, 1, seed));
			if (next.temper() != EnumBloodTemper.ANIMUS) {
				crossed = true;
				assertEquals(previous.exit(), next.entry(), "a cross-school swap still blends pose to pose");
			}
		}
		assertTrue(crossed, "the Open act must sometimes hand the chain to another school");
	}

	@Test
	void finisherOrChainCapEndsTheString() {
		EnumBloodTemper temper = EnumBloodTemper.MORTEM;
		VesperWeaponAction finisher = VesperChainGraph.moveset(temper).stream()
				.filter(move -> move.role() == VesperChainGraph.Role.FINISHER).findFirst().orElseThrow();
		assertEquals(VesperChainGraph.Role.OPENER, VesperChainGraph.next(finisher, temper,
				VesperEveningStarAct.LOCKED, ctx(4.0D, true, false, 2, 1L)).role());
		VesperWeaponAction followUp = VesperChainGraph.moveset(temper).stream()
				.filter(move -> move.role() == VesperChainGraph.Role.FOLLOW_UP).findFirst().orElseThrow();
		assertEquals(VesperChainGraph.Role.OPENER, VesperChainGraph.next(followUp, temper,
				VesperEveningStarAct.LOCKED, ctx(4.0D, true, false, VesperChainGraph.MAX_CHAIN, 1L)).role());
	}

	@Test
	void noImmediateRepeat() {
		for (EnumBloodTemper temper : EnumBloodTemper.values()) {
			VesperWeaponAction previous = VesperWeaponAction.NONE;
			for (int step = 0; step < 30; step++) {
				VesperWeaponAction next = VesperChainGraph.next(previous, temper, VesperEveningStarAct.LOCKED,
						ctx(step % 3 == 0 ? 12.0D : 3.5D, step % 2 == 0, step % 4 == 0, step % 6, step));
				assertNotEquals(previous, next, temper + " repeated " + next);
				previous = next;
			}
		}
	}

	@Test
	void farTargetsGetAGapCloserOpener() {
		for (EnumBloodTemper temper : EnumBloodTemper.values()) {
			VesperWeaponAction opener = VesperChainGraph.next(VesperWeaponAction.NONE, temper,
					VesperEveningStarAct.LOCKED, ctx(14.0D, false, false, 0, 3L));
			assertEquals(VesperChainGraph.Role.OPENER, opener.role());
		}
	}
}
