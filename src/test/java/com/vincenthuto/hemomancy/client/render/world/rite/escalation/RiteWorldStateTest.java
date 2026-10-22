package com.vincenthuto.hemomancy.client.render.world.rite.escalation;

import com.vincenthuto.hemomancy.common.rite.harbinger.ActiveRiteData;
import com.vincenthuto.hemomancy.client.rite.CardinalRiteEventClientState;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteEventKind;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteInstability;
import com.vincenthuto.hemomancy.common.rite.CardinalRitePhase;
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

	static ActiveRiteData.RiteEntry entry(BlockPos center, String phase, int stage, int total,
			boolean unstained, int instability) {
		return new ActiveRiteData.RiteEntry(center, 5, 0.0D,
				ResourceLocation.parse("hemomancy:cardinal_rite/world_test"), unstained, phase, instability, 0, 0, 1, 1,
				0, 0, 0, 0, 0, "", 8.0F, List.of(), List.of(), List.of(), List.of(), true, UUID.randomUUID(), 0, -1,
				"storm", true, true, 0).withEscalation(stage, total);
	}

	static ActiveRiteData.RiteEntry entry(BlockPos center, String phase, int stage, int total) {
		return entry(center, phase, stage, total, false, 0);
	}

	private static void tick(List<ActiveRiteData.RiteEntry> active,
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
		ActiveRiteData.RiteEntry live = entry(FOCUS, "CULMINATION", 4, 4);
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
		ActiveRiteData.RiteEntry live = entry(FOCUS, "ORDEAL", 2, 5);
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

	@Test
	void aCollapseTearsTheCreepAwayInsteadOfLeavingAFringe() {
		RiteWorldState.World collapsing = new RiteWorldState.World(FOCUS, null, 1.0F, 1.0F, 0.0F, 4, 4,
				CardinalRiteInstability.STABLE, 8.0F, "storm", true, CardinalRitePhase.ORDEAL, WORKING,
				RiteWorldMath.Ending.COLLAPSED, 20.0F);

		float[] blood = RiteWorldState.bloodUniform(collapsing, 1.0F);

		assertEquals(WORKING.edge(), blood[2], 1e-5F, "torn back to the floor's edge");
		assertEquals(0.0F, blood[3], 1e-6F, "with nothing left beyond it");
	}

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
}
