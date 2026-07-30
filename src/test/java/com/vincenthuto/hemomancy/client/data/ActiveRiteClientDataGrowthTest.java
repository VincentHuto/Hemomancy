package com.vincenthuto.hemomancy.client.data;

import com.vincenthuto.hemomancy.common.rite.CardinalRiteBoundaryProgress;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public final class ActiveRiteClientDataGrowthTest {
	@AfterEach
	void clearClientState() {
		ActiveRiteClientData.clear();
	}

	@Test
	void newlyVisibleNodesGrowTowardTheirSyncedRadiusInsteadOfSnappingIntoPlace() {
		ActiveRiteClientData.set(List.of(riteWithBlob(0.16F)));
		ActiveRiteClientData.SanguineBlob blob =
				ActiveRiteClientData.getActiveRites().get(0).getSanguineBlobs().get(0);

		assertFloatEquals(0.0F, blob.renderRadius(0.0F), "initial radius");
		ActiveRiteClientData.tick();
		float firstTick = blob.renderRadius(1.0F);
		assertTrue(firstTick > 0.0F, "growth starts on the first client tick");
		assertTrue(firstTick < 0.16F, "growth does not snap to the target");

		for (int tick = 0; tick < 30; tick++) ActiveRiteClientData.tick();
		assertTrue(blob.renderRadius(1.0F) > 0.155F, "growth converges on the authored radius");
	}

	@Test
	void refreshedNodeTargetsContinueFromTheVisibleSize() {
		ActiveRiteClientData.set(List.of(riteWithBlob(0.08F)));
		for (int tick = 0; tick < 30; tick++) ActiveRiteClientData.tick();
		float beforeRefresh = currentBlob().renderRadius(1.0F);

		ActiveRiteClientData.set(List.of(riteWithBlob(0.16F)));
		ActiveRiteClientData.SanguineBlob refreshed = currentBlob();

		assertFloatEquals(beforeRefresh, refreshed.renderRadius(0.0F), "radius preserved across sync");
		ActiveRiteClientData.tick();
		assertTrue(refreshed.renderRadius(1.0F) > beforeRefresh, "refreshed target resumes growing");
		assertTrue(refreshed.renderRadius(1.0F) < 0.16F, "refreshed target remains interpolated");
	}

	@Test
	void newlyVisibleBoundarySegmentsGrowAcrossTwentyClientTicks() {
		CardinalRiteBoundaryProgress.Segment segment =
				new CardinalRiteBoundaryProgress.Segment(0, 0.0D, Math.PI / 2.0D, 0, 1.0F);
		ActiveRiteClientData.set(List.of(riteWithSegments(List.of(segment))));
		ActiveRiteClientData.RiteEntry rite = currentRite();

		assertFloatEquals(0.0F, rite.boundaryGrowth(segment, 0.0F), "initial segment growth");
		ActiveRiteClientData.tick();
		assertFloatEquals(0.025F, rite.boundaryGrowth(segment, 0.5F), "partial-tick segment growth");

		for (int tick = 1; tick < 20; tick++) ActiveRiteClientData.tick();
		assertFloatEquals(1.0F, rite.boundaryGrowth(segment, 1.0F), "completed segment growth");
	}

	@Test
	void newlyVisibleRiteStainFadesInAndSurvivesSyncRefreshes() {
		ActiveRiteClientData.set(List.of(riteWithSegments(List.of())));
		ActiveRiteClientData.RiteEntry rite = currentRite();

		assertFloatEquals(0.0F, rite.stainFadeProgress(0.0F), "initial stain fade");
		for (int tick = 0; tick < 10; tick++) ActiveRiteClientData.tick();
		float beforeRefresh = rite.stainFadeProgress(1.0F);
		assertTrue(beforeRefresh > 0.0F, "stain begins fading in");
		assertTrue(beforeRefresh < 1.0F, "stain does not snap to full opacity");

		ActiveRiteClientData.set(List.of(riteWithSegments(List.of())));
		assertFloatEquals(beforeRefresh, currentRite().stainFadeProgress(1.0F),
				"stain fade retained across sync");

		for (int tick = 10; tick < 30; tick++) ActiveRiteClientData.tick();
		assertFloatEquals(1.0F, currentRite().stainFadeProgress(1.0F),
				"stain reaches full opacity");
	}

	@Test
	void refreshedBoundarySegmentsPreserveTheirVisibleProgress() {
		CardinalRiteBoundaryProgress.Segment segment =
				new CardinalRiteBoundaryProgress.Segment(0, 0.0D, Math.PI / 2.0D, 0, 1.0F);
		ActiveRiteClientData.set(List.of(riteWithSegments(List.of(segment))));
		for (int tick = 0; tick < 6; tick++) ActiveRiteClientData.tick();
		float beforeRefresh = currentRite().boundaryGrowth(segment, 1.0F);

		ActiveRiteClientData.set(List.of(riteWithSegments(List.of(segment))));

		assertFloatEquals(beforeRefresh, currentRite().boundaryGrowth(segment, 1.0F),
				"segment progress retained across sync");
	}

	@Test
	void restoredBoundarySegmentsReplayTheirGrowth() {
		CardinalRiteBoundaryProgress.Segment segment =
				new CardinalRiteBoundaryProgress.Segment(0, 0.0D, Math.PI / 2.0D, 0, 1.0F);
		ActiveRiteClientData.set(List.of(riteWithSegments(List.of(segment))));
		for (int tick = 0; tick < 20; tick++) ActiveRiteClientData.tick();

		ActiveRiteClientData.set(List.of(riteWithSegments(List.of())));
		ActiveRiteClientData.set(List.of(riteWithSegments(List.of(segment))));

		assertFloatEquals(0.0F, currentRite().boundaryGrowth(segment, 1.0F),
				"restored segment growth restarts");
	}

	@Test
	void completedBoundarySegmentsEmitOneSealEventAndTrackEffectAge() {
		CardinalRiteBoundaryProgress.Segment segment =
				new CardinalRiteBoundaryProgress.Segment(0, 0.0D, Math.PI / 2.0D, 0, 1.0F);
		ActiveRiteClientData.set(List.of(riteWithSegments(List.of(segment))));
		ActiveRiteClientData.RiteEntry rite = currentRite();

		assertFloatEquals(-1.0F, rite.boundaryEffectAge(segment, 1.0F), "pre-completion effect age");
		for (int tick = 0; tick < 20; tick++) ActiveRiteClientData.tick();

		assertFloatEquals(0.0F, rite.boundaryEffectAge(segment, 1.0F), "completion effect age");
		assertTrue(rite.consumeBoundaryCompletion(segment), "completion event emitted");
		assertTrue(!rite.consumeBoundaryCompletion(segment), "completion event consumed once");

		ActiveRiteClientData.tick();
		assertFloatEquals(0.5F, rite.boundaryEffectAge(segment, 0.5F),
				"completion effect partial-tick age");
	}

	private static ActiveRiteClientData.SanguineBlob currentBlob() {
		return currentRite().getSanguineBlobs().get(0);
	}

	private static ActiveRiteClientData.RiteEntry currentRite() {
		return ActiveRiteClientData.getActiveRites().get(0);
	}

	private static ActiveRiteClientData.RiteEntry riteWithBlob(float radius) {
		return new ActiveRiteClientData.RiteEntry(
				BlockPos.ZERO, 3, 0.0D,
				ResourceLocation.fromNamespaceAndPath("hemomancy", "growth_test"),
				false, "CONSECRATION", 0, 0, 1, 0, 1,
				0, 200, 0, 0, 0, "",
				0.0F, List.of(), List.<CardinalRiteBoundaryProgress.Segment>of(), List.of(),
				List.of(new ActiveRiteClientData.SanguineBlob(
						0.5D, 0.2D, 0.5D, radius, 0xFF3746, 42L)));
	}

	private static ActiveRiteClientData.RiteEntry riteWithSegments(
			List<CardinalRiteBoundaryProgress.Segment> segments) {
		return new ActiveRiteClientData.RiteEntry(
				BlockPos.ZERO, 3, 0.0D,
				ResourceLocation.fromNamespaceAndPath("hemomancy", "growth_test"),
				false, "CONSECRATION", 0, 0, 1, 0, 1,
				0, 200, 0, 0, 0, "",
				0.0F, List.of(), segments, List.of(), List.of());
	}

	private static void assertFloatEquals(float expected, float actual, String label) {
		if (Math.abs(expected - actual) > 0.0001F) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}

	private static void assertTrue(boolean actual, String label) {
		if (!actual) throw new AssertionError(label);
	}
}
