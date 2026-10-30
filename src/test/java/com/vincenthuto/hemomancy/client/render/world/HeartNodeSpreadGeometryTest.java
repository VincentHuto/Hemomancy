package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.common.world.HeartNodeFootprint;
import com.vincenthuto.hemomancy.common.world.HeartNodeRelation;
import com.vincenthuto.hemomancy.common.world.HeartNodeRules;
import com.vincenthuto.hemomancy.common.world.HeartNodeSettings;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HeartNodeSpreadGeometryTest {
	private static final BlockPos HEART = new BlockPos(40, 60, -30);
	private static final double CX = HEART.getX() + 0.5D;
	private static final double CZ = HEART.getZ() + 0.5D;
	private final UUID owner = UUID.randomUUID();

	private HeartNodeFootprint node(BlockPos... synapses) {
		return new HeartNodeFootprint(owner, HEART, List.of(synapses));
	}

	@Test
	void strangersSeeNothingUnlessTheOwnerShowsThem() {
		HeartNodeSettings settings = HeartNodeSettings.DEFAULT;
		assertTrue(HeartNodeSpreadGeometry.drawsFor(settings, HeartNodeRelation.OWNER));
		assertFalse(HeartNodeSpreadGeometry.drawsFor(settings, HeartNodeRelation.STRANGER));
		HeartNodeSettings shown = settings.with(HeartNodeSettings.SHOW_STRANGERS, true);
		assertTrue(HeartNodeSpreadGeometry.drawsFor(shown, HeartNodeRelation.STRANGER));
		HeartNodeSettings off = shown.with(HeartNodeSettings.SPREAD, false);
		assertFalse(HeartNodeSpreadGeometry.drawsFor(off, HeartNodeRelation.OWNER), "spread off hides it from the owner too");
		assertFalse(HeartNodeSpreadGeometry.drawsFor(off, HeartNodeRelation.STRANGER));
	}

	@Test
	void bodyStrengthRunsFromTheHeartToTheWidestReach() {
		HeartNodeFootprint node = node(HEART.offset(16, 0, 0));
		assertEquals(1.0D, HeartNodeSpreadGeometry.bodyStrength(node, CX, CZ), 1e-9);
		assertEquals(0.5D, HeartNodeSpreadGeometry.bodyStrength(node, CX + 12.0D, CZ), 1e-9);
		assertEquals(0.0D, HeartNodeSpreadGeometry.bodyStrength(node, CX + 24.0D, CZ), 1e-9);
		assertEquals(0.0D, HeartNodeSpreadGeometry.bodyStrength(node, CX + 40.0D, CZ), 1e-9, "clamped past the reach");
	}

	@Test
	void edgeDepthIsTheDeepestLobeAndRespectsTheBand() {
		HeartNodeFootprint node = node(HEART.offset(16, 0, 0));
		assertEquals(12.0D, HeartNodeSpreadGeometry.edgeDepth(node, CX, CZ, HEART.getY()), 1e-9);
		assertEquals(1.0D, HeartNodeSpreadGeometry.edgeDepth(node, CX, CZ + 11.0D, HEART.getY()), 1e-9);
		// Between the heart and the synapse the synapse lobe is the deeper one.
		assertEquals(6.0D, HeartNodeSpreadGeometry.edgeDepth(node, CX + 14.0D, CZ, HEART.getY()), 1e-9);
		assertTrue(HeartNodeSpreadGeometry.edgeDepth(node, CX + 30.0D, CZ, HEART.getY()) < 0.0D, "outside every lobe");
		assertTrue(HeartNodeSpreadGeometry.edgeDepth(node, CX, CZ, HEART.getY() + HeartNodeRules.VERTICAL_BAND + 1) < 0.0D,
				"the cavern above is outside");
	}

	@Test
	void threadsChainFromTheLobeEachSynapseTouches() {
		BlockPos first = HEART.offset(16, 0, 0);
		BlockPos second = HEART.offset(30, 0, 0);
		HeartNodeFootprint node = node(first, second);
		List<HeartNodeSpreadGeometry.Thread> threads = HeartNodeSpreadGeometry.threads(node);
		assertEquals(2, threads.size());
		assertEquals(CX, threads.get(0).fromX(), 1e-9, "the first leg runs from the heart");
		assertEquals(0.0D, threads.get(0).startLength(), 1e-9);
		assertEquals(first.getX() + 0.5D, threads.get(1).fromX(), 1e-9, "the second from the first synapse");
		assertEquals(16.0D, threads.get(1).startLength(), 1e-9);
		Map<BlockPos, Double> arrivals = HeartNodeSpreadGeometry.arrivals(node);
		assertEquals(30.0D, arrivals.get(second), 1e-9);
		assertEquals(0.0D, arrivals.get(HEART), 1e-9);
	}

	@Test
	void aCutOffSynapseHasNoThreadAndNoArrival() {
		BlockPos stranded = HEART.offset(40, 0, 0);
		HeartNodeFootprint node = node(stranded);
		assertTrue(HeartNodeSpreadGeometry.threads(node).isEmpty());
		assertFalse(HeartNodeSpreadGeometry.arrivals(node).containsKey(stranded));
	}

	@Test
	void theSignedDistanceRunsLinearlyAcrossAFaceAndChangesSideOnTheThread() {
		HeartNodeSpreadGeometry.Thread thread = new HeartNodeSpreadGeometry.Thread(0, 0, 10, 0, 4.0D);
		double left = thread.signedDistance(5, 0.5D);
		double right = thread.signedDistance(5, -0.5D);
		assertEquals(0.5D, Math.abs(left), 1e-9);
		assertEquals(-left, right, 1e-9, "the two sides carry opposite signs");
		assertEquals((left + right) / 2.0D, thread.signedDistance(5, 0.0D), 1e-9, "linear across the thread");
		assertEquals(5.0D, thread.distance(-3, 4), 1e-9, "past the start it is the distance to the start");
		assertEquals(4.0D + 5.0D, thread.pathLength(5, 3), 1e-9);
		assertEquals(4.0D + 10.0D, thread.pathLength(20, 0), 1e-9, "the run stops at the synapse");
	}

	@Test
	void theNearestThreadIsWithinTheSpanOrNone() {
		HeartNodeSpreadGeometry.Thread a = new HeartNodeSpreadGeometry.Thread(0, 0, 10, 0, 0);
		HeartNodeSpreadGeometry.Thread b = new HeartNodeSpreadGeometry.Thread(0, 3, 10, 3, 0);
		assertEquals(b, HeartNodeSpreadGeometry.nearestThread(List.of(a, b), 5, 2.0D));
		assertNotNull(HeartNodeSpreadGeometry.nearestThread(List.of(a), 5, 1.9D));
		assertNull(HeartNodeSpreadGeometry.nearestThread(List.of(a), 5, 2.1D), "past the span no thread is picked");
	}

	@Test
	void wallsCarryTheLayerOnlyNearTheFloorAndCeilingsOnlyTips() {
		assertEquals(1.0D, HeartNodeSpreadGeometry.wallWeight(0.0D), 1e-9);
		assertEquals(1.0D / 3.0D, HeartNodeSpreadGeometry.wallWeight(1.0D), 1e-9);
		assertEquals(0.0D, HeartNodeSpreadGeometry.wallWeight(1.5D), 1e-9);
		assertEquals(0.0D, HeartNodeSpreadGeometry.wallWeight(2.0D), 1e-9);
		assertEquals(HeartNodeSpreadGeometry.CEILING_TIP, HeartNodeSpreadGeometry.ceilingWeight(true, 2), 1e-9);
		assertEquals(0.0D, HeartNodeSpreadGeometry.ceilingWeight(false, 2), 1e-9, "open ceiling, no tips");
		assertEquals(0.0D, HeartNodeSpreadGeometry.ceilingWeight(true, HeartNodeSpreadGeometry.CEILING_REACH + 1), 1e-9,
				"a high ceiling carries nothing");
	}

	@Test
	void theHeartGrowsOutToItsLobeThenStopsDrawingARim() {
		long planted = 1000L;
		assertEquals(HeartNodeSpreadGeometry.FRONT_START, HeartNodeSpreadGeometry.heartFront(planted, planted), 1e-9);
		double half = HeartNodeSpreadGeometry.heartFront(planted, planted + HeartNodeRules.MATURE_TICKS / 2.0D);
		assertTrue(half > HeartNodeSpreadGeometry.FRONT_START && half < HeartNodeRules.HEART_RADIUS + 1.0D);
		assertEquals(HeartNodeSpreadGeometry.GROWN, HeartNodeSpreadGeometry.heartFront(planted,
				planted + HeartNodeRules.MATURE_TICKS), 1e-9);
		assertEquals(HeartNodeSpreadGeometry.GROWN, HeartNodeSpreadGeometry.heartFront(HeartNodeRules.LONG_GROWN, 0),
				1e-9, "an old save reads as grown");
	}

	@Test
	void aSynapseSpreadsInTenSecondsAndRecedesInThree() {
		assertEquals(0.0D, HeartNodeSpreadGeometry.synapseFront(100L, 100.0D), 1e-9);
		assertEquals(HeartNodeSpreadGeometry.GROWN, HeartNodeSpreadGeometry.synapseFront(100L, 300.0D), 1e-9);
		assertEquals(HeartNodeSpreadGeometry.GROWN,
				HeartNodeSpreadGeometry.synapseFront(HeartNodeSpreadGeometry.LONG_AGO, 0.0D), 1e-9);
		assertEquals(1.0D, HeartNodeSpreadGeometry.witherLife(100L, 100.0D), 1e-9);
		assertEquals(0.5D, HeartNodeSpreadGeometry.witherLife(100L, 130.0D), 1e-9);
		assertFalse(HeartNodeSpreadGeometry.withered(100L, 159L));
		assertTrue(HeartNodeSpreadGeometry.withered(100L, 160L));
	}

	@Test
	void aSynapseFlaresWhenThePulseReachesItEveryHeartbeat() {
		double arrive = 12.0D;
		double reached = arrive / HeartNodeSpreadGeometry.PULSE_SPEED;
		double peak = reached + HeartNodeSpreadGeometry.FLARE_PEAK;
		assertEquals(1.0D, HeartNodeSpreadGeometry.flare(peak, arrive), 1e-9, "full at the shader's flare centre");
		assertEquals(1.0D, HeartNodeSpreadGeometry.flare(peak + HeartNodeRules.HEARTBEAT_TICKS * 3, arrive), 1e-9,
				"and again each heartbeat");
		assertTrue(HeartNodeSpreadGeometry.flare(peak + HeartNodeRules.HEARTBEAT_TICKS / 2.0D, arrive) < 0.01D,
				"dark between beats");
		assertTrue(HeartNodeSpreadGeometry.flare(peak, arrive + 6.0D) < HeartNodeSpreadGeometry.flare(peak, arrive),
				"a synapse further along the threads flares later");
	}

	@Test
	void packingPutsEachValueInItsChannel() {
		int packed = HeartNodeSpreadGeometry.pack(1.0D, HeartNodeSpreadGeometry.EDGE_SPAN, 0.0D, 0.5D);
		assertEquals(128, packed >>> 24, "alpha is the weight");
		assertEquals(255, packed >> 16 & 0xFF, "red is the strength");
		assertEquals(255, packed >> 8 & 0xFF, "green is the depth over its span");
		assertEquals(128, packed & 0xFF, "blue is 0.5 on the thread");
		int far = HeartNodeSpreadGeometry.pack(-1.0D, -2.0D, -9.0D, 3.0D);
		assertEquals(255, far >>> 24);
		assertEquals(0, far >> 16 & 0xFF);
		assertEquals(0, far >> 8 & 0xFF);
		assertEquals(0, far & 0xFF, "clamped to the span");
	}
}
