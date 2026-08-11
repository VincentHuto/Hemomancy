package com.vincenthuto.hemomancy.client.render.layer.mob.endgame;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class VesperTransitionCocoonGeometryTest {
	@Test
	void sealedCocoonSurroundsVesperFromGroundToAboveHisHead() {
		List<VesperTransitionCocoonGeometry.Strand> strands =
				VesperTransitionCocoonGeometry.strands(Vec3.ZERO, 120.0F, 1.0F, 0.0F);

		assertEquals(28, strands.size());
		double minimumY = strands.stream().flatMap(strand -> strand.joints().stream())
				.mapToDouble(joint -> joint.center().y).min().orElseThrow();
		double maximumY = strands.stream().flatMap(strand -> strand.joints().stream())
				.mapToDouble(joint -> joint.center().y).max().orElseThrow();
		assertTrue(minimumY <= -0.95D, "one-block lower extension must remain part of the cocoon");
		assertTrue(maximumY >= 4.45D, "closed strands must meet one block above the crown");
		double maximumRadius = strands.stream().flatMap(strand -> strand.joints().stream())
				.mapToDouble(joint -> horizontalRadius(joint.center())).max().orElseThrow();
		assertTrue(maximumRadius >= 2.4D, "one-block radial extension must cover the rear hood");
		assertTrue(strands.stream().allMatch(strand -> strand.joints().size() == 25));
	}

	@Test
	void rootsStayPlantedWhileNeighboringStrandsWindInTheSameDirection() {
		List<VesperTransitionCocoonGeometry.Strand> early =
				VesperTransitionCocoonGeometry.strands(Vec3.ZERO, 90.0F, 1.0F, 0.0F);
		List<VesperTransitionCocoonGeometry.Strand> later =
				VesperTransitionCocoonGeometry.strands(Vec3.ZERO, 130.0F, 1.0F, 0.0F);

		assertEquals(early.get(0).joints().get(0).center(), later.get(0).joints().get(0).center());
		double evenTurn = unwrappedTurn(early.get(0));
		double oddTurn = unwrappedTurn(early.get(1));
		assertTrue(evenTurn * oddTurn > 0.0D, "neighboring strands must share one rotational flow");
	}

	@Test
	void everyTendrilRootsOnTheSameGroundPlaneAndReachesThePinchedApex() {
		List<VesperTransitionCocoonGeometry.Strand> sealed =
				VesperTransitionCocoonGeometry.strands(Vec3.ZERO, 120.0F, 1.0F, 0.0F);
		double groundY = sealed.get(0).joints().get(0).center().y;

		assertTrue(sealed.stream().allMatch(strand ->
				Math.abs(strand.joints().get(0).center().y - groundY) < 1.0E-6D));
		assertTrue(sealed.stream().allMatch(strand ->
				strand.joints().get(strand.joints().size() - 1).center().y > groundY + 4.0D));
	}

	@Test
	void apexPinchesClosedAboveTheCrown() {
		List<VesperTransitionCocoonGeometry.Strand> sealed =
				VesperTransitionCocoonGeometry.strands(Vec3.ZERO, 120.0F, 1.0F, 0.0F);
		double apexRadius = sealed.stream().map(strand -> strand.joints().get(strand.joints().size() - 1))
				.mapToDouble(joint -> horizontalRadius(joint.center())).max().orElseThrow();
		int middleIndex = sealed.get(0).joints().size() / 2;
		double middleRadius = sealed.stream().map(strand -> strand.joints().get(middleIndex))
				.mapToDouble(joint -> horizontalRadius(joint.center())).max().orElseThrow();

		assertTrue(apexRadius <= 0.42D, "the top must tighten into a closed pinch");
		assertTrue(middleRadius > apexRadius + 1.0D, "the body must remain broader than the apex");
	}

	@Test
	void tendrilsMakeMultipleTightTurnsBeforeTheApex() {
		VesperTransitionCocoonGeometry.Strand strand =
				VesperTransitionCocoonGeometry.strands(Vec3.ZERO, 120.0F, 1.0F, 0.0F).get(0);

		double totalTurn = 0.0D;
		for (int index = 1; index < strand.joints().size(); index++) {
			double previous = Math.atan2(strand.joints().get(index - 1).center().z,
					strand.joints().get(index - 1).center().x);
			double current = Math.atan2(strand.joints().get(index).center().z,
					strand.joints().get(index).center().x);
			double delta = current - previous;
			while (delta > Math.PI) delta -= Math.PI * 2.0D;
			while (delta < -Math.PI) delta += Math.PI * 2.0D;
			totalTurn += delta;
		}

		assertTrue(Math.abs(totalTurn) > Math.PI * 3.5D,
				"the cocoon should make more than one-and-three-quarter tight turns");
	}

	@Test
	void formationClimbsUpwardAndBurstThrowsTheClosedKnotOutward() {
		List<VesperTransitionCocoonGeometry.Strand> halfFormed =
				VesperTransitionCocoonGeometry.strands(Vec3.ZERO, 40.0F, 0.5F, 0.0F);
		List<VesperTransitionCocoonGeometry.Strand> sealed =
				VesperTransitionCocoonGeometry.strands(Vec3.ZERO, 120.0F, 1.0F, 0.0F);
		List<VesperTransitionCocoonGeometry.Strand> bursting =
				VesperTransitionCocoonGeometry.strands(Vec3.ZERO, 155.0F, 1.0F, 0.5F);

		assertTrue(halfFormed.get(0).joints().size() < sealed.get(0).joints().size());
		int middle = sealed.get(0).joints().size() / 2;
		double sealedRadius = horizontalRadius(sealed.get(0).joints().get(middle).center());
		double burstRadius = horizontalRadius(bursting.get(0).joints().get(middle).center());
		assertTrue(burstRadius > sealedRadius + 0.75D);
		assertTrue(bursting.get(0).joints().get(middle).opacity()
				< sealed.get(0).joints().get(middle).opacity());
	}

	private static double unwrappedTurn(VesperTransitionCocoonGeometry.Strand strand) {
		Vec3 root = strand.joints().get(0).center();
		Vec3 middle = strand.joints().get(strand.joints().size() / 2).center();
		double start = Math.atan2(root.z, root.x);
		double midpoint = Math.atan2(middle.z, middle.x);
		double delta = midpoint - start;
		while (delta > Math.PI) delta -= Math.PI * 2.0D;
		while (delta < -Math.PI) delta += Math.PI * 2.0D;
		return delta;
	}

	private static double horizontalRadius(Vec3 point) {
		return Math.sqrt(point.x * point.x + point.z * point.z);
	}
}
