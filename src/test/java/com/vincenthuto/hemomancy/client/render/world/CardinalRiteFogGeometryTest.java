package com.vincenthuto.hemomancy.client.render.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CardinalRiteFogGeometryTest {
	@Test
	void fullFootprintDefinesTheFogPerimeterBeforeRingsAreCompleted() {
		assertEquals(11.75F,
				CardinalRiteFogGeometry.perimeterRadius(11.75F, 9, 0, false));
	}

	@Test
	void completedBoundaryAndRiteSizeProvideOrderedFallbacks() {
		assertEquals(5.0F,
				CardinalRiteFogGeometry.perimeterRadius(0.0F, 9, 3, false));
		assertEquals(2.5F,
				CardinalRiteFogGeometry.perimeterRadius(0.0F, 3, 0, false));
	}

	@Test
	void renderDistanceIncludesTheFogRadiusBeyondTheChunkLimit() {
		BlockPos center = BlockPos.ZERO;
		assertTrue(CardinalRiteFogGeometry.isWithinRenderDistance(
				center, new Vec3(40.0D, 0.0D, 0.0D), 10.0F, 2));
		assertFalse(CardinalRiteFogGeometry.isWithinRenderDistance(
				center, new Vec3(48.0D, 0.0D, 0.0D), 10.0F, 2));
	}

	@Test
	void puffPopulationScalesWithRadiusWithoutBecomingExcessive() {
		int smallPopulation = CardinalRiteFogGeometry.puffs(2.7F, 3.0F).size();
		int largePopulation = CardinalRiteFogGeometry.puffs(2.7F, 30.0F).size();

		assertTrue(smallPopulation >= 165 && smallPopulation <= 220,
				"small rites retain enough overlapping puffs to stay continuous");
		assertTrue(largePopulation > smallPopulation && largePopulation <= 430,
				"very large rites cap their billboard cost");
		assertTrue(CardinalRiteFogGeometry.puffs(2.7F, 12.0F).size() > 70,
				"normal large rites receive a denser cloud population");
	}

	@Test
	void sameRiteProducesStablePuffsWithVariedCloudProperties() {
		var first = CardinalRiteFogGeometry.puffs(2.7F, 11.75F);
		var repeated = CardinalRiteFogGeometry.puffs(2.7F, 11.75F);

		assertEquals(first, repeated, "the smoke arrangement remains stable between frames");
		assertTrue(first.stream().map(CardinalRiteFogGeometry.FogPuff::halfWidth).distinct().count() > 20);
		assertTrue(first.stream().map(CardinalRiteFogGeometry.FogPuff::halfHeight).distinct().count() > 20);
		assertTrue(first.stream().map(CardinalRiteFogGeometry.FogPuff::crimsonWeight).distinct().count() > 20);
		assertTrue(first.stream().anyMatch(puff -> puff.crimsonWeight() > 0.55F),
				"some puffs carry strong crimson currents");
		assertTrue(first.stream().filter(puff -> puff.crimsonWeight() < 0.35F).count() > first.size() / 2,
				"the overall ring remains black-dominant");
	}

	@Test
	void puffsFillAnIrregularBroadTorusInsteadOfOnePerfectCircle() {
		var puffs = CardinalRiteFogGeometry.puffs(2.7F, 11.75F);
		float minimumRadiusOffset = puffs.stream()
				.map(CardinalRiteFogGeometry.FogPuff::radialOffset)
				.min(Float::compareTo)
				.orElseThrow();
		float maximumRadiusOffset = puffs.stream()
				.map(CardinalRiteFogGeometry.FogPuff::radialOffset)
				.max(Float::compareTo)
				.orElseThrow();
		float minimumHeight = puffs.stream()
				.map(CardinalRiteFogGeometry.FogPuff::heightOffset)
				.min(Float::compareTo)
				.orElseThrow();
		float maximumHeight = puffs.stream()
				.map(CardinalRiteFogGeometry.FogPuff::heightOffset)
				.max(Float::compareTo)
				.orElseThrow();

		assertTrue(minimumRadiusOffset < -1.50F);
		assertTrue(maximumRadiusOffset > 2.50F);
		assertTrue(maximumRadiusOffset - minimumRadiusOffset > 4.00F,
				"puff centers occupy a broad radial smoke band");
		assertTrue(minimumHeight >= 0.08F && maximumHeight <= 2.25F);
		assertTrue(maximumHeight - minimumHeight > 0.70F,
				"the smoke occupies a low volume instead of one flat plane");
	}

	@Test
	void angularPlacementFormsDenseClustersSeparatedByThinGaps() {
		var puffs = new ArrayList<>(CardinalRiteFogGeometry.puffs(2.7F, 11.75F).stream()
				.filter(puff -> !puff.scattered())
				.toList());
		puffs.sort(Comparator.comparingDouble(CardinalRiteFogGeometry.FogPuff::angle));
		double nominalGap = Math.PI * 2.0D / puffs.size();
		double smallestGap = Double.MAX_VALUE;
		double largestGap = 0.0D;
		for (int index = 0; index < puffs.size(); index++) {
			double current = puffs.get(index).angle();
			double next = index + 1 < puffs.size()
					? puffs.get(index + 1).angle()
					: puffs.getFirst().angle() + Math.PI * 2.0D;
			double gap = next - current;
			smallestGap = Math.min(smallestGap, gap);
			largestGap = Math.max(largestGap, gap);
		}

		assertTrue(smallestGap < nominalGap * 0.25D,
				"some neighboring puffs pack tightly into a dense cloud mass");
		assertTrue(largestGap > nominalGap * 3.0D,
				"cluster boundaries create visibly thinner stretches of the ring");
	}

	@Test
	void primaryFogUsesUnevenCloudMassesInsteadOfOneClusterPerSector() {
		var bodyPuffs = CardinalRiteFogGeometry.puffs(2.7F, 11.75F).stream()
				.filter(puff -> !puff.scattered())
				.toList();
		int[] angularBins = new int[12];
		for (var puff : bodyPuffs) {
			int bin = Math.min(angularBins.length - 1,
					(int) (puff.angle() / (Math.PI * 2.0D) * angularBins.length));
			angularBins[bin]++;
		}
		int sparsest = java.util.Arrays.stream(angularBins).min().orElseThrow();
		int densest = java.util.Arrays.stream(angularBins).max().orElseThrow();

		assertTrue(sparsest <= 1, "some broad sectors are left to the scattered background fog");
		assertTrue(densest >= bodyPuffs.size() / 6,
				"nearby cluster centers can merge into a visibly dominant cloud mass");
	}

	@Test
	void lowOpacityScatterFogSpreadsInsideAndOutsideThePrimaryRing() {
		var puffs = CardinalRiteFogGeometry.puffs(2.7F, 11.75F);
		var scattered = puffs.stream()
				.filter(CardinalRiteFogGeometry.FogPuff::scattered)
				.toList();

		assertTrue(scattered.size() >= puffs.size() / 2
						&& scattered.size() <= puffs.size() * 2 / 3,
				"scatter fog remains a supporting layer rather than replacing the clumps");
		assertTrue(scattered.stream().anyMatch(puff -> puff.radialOffset() < -1.50F));
		assertTrue(scattered.stream().anyMatch(puff -> puff.radialOffset() > 2.50F));
		assertTrue(scattered.stream().allMatch(puff -> puff.opacity() <= 0.20F),
				"strewn fog stays softer than the primary cloud masses");
	}

	@Test
	void individualPuffsOrbitAndBobLikeABrewingStorm() {
		var puff = CardinalRiteFogGeometry.puffs(2.7F, 11.75F).get(17);
		var start = CardinalRiteFogGeometry.position(puff, 11.75F, 0.0F);
		var later = CardinalRiteFogGeometry.position(puff, 11.75F, 80.0F);
		double horizontalMotion = Math.hypot(later.x() - start.x(), later.z() - start.z());
		double startAngle = Math.atan2(start.z(), start.x());
		double laterAngle = Math.atan2(later.z(), later.x());
		double angularMotion = Math.abs(Math.atan2(Math.sin(laterAngle - startAngle),
				Math.cos(laterAngle - startAngle)));

		assertNotEquals(start, later, "each puff has slow independent motion");
		assertTrue(horizontalMotion > 0.01D && horizontalMotion < 8.0D);
		assertTrue(angularMotion > 0.10D && angularMotion < 0.75D,
				"puffs circulate around the rite instead of staying fixed in place");
		assertTrue(Math.abs(later.y() - start.y()) < 0.65D,
				"the layered smoke bobs with the brewing storm");
		assertTrue(Math.abs(Math.hypot(later.x(), later.z())
				- Math.hypot(start.x(), start.z())) < 0.35D,
				"motion stays locally bounded instead of orbiting the rite");
	}

	@Test
	void puffsFadeIndividuallyAtDifferentReformationPaces() {
		var puffs = CardinalRiteFogGeometry.puffs(2.7F, 11.75F);
		assertTrue(puffs.stream().map(CardinalRiteFogGeometry.FogPuff::fadeSpeed).distinct().count() > 20);

		var puff = puffs.get(17);
		float minimum = Float.MAX_VALUE;
		float maximum = Float.MIN_VALUE;
		for (int tick = 0; tick <= 600; tick += 20) {
			float opacity = CardinalRiteFogGeometry.opacityMultiplier(puff, tick);
			minimum = Math.min(minimum, opacity);
			maximum = Math.max(maximum, opacity);
		}
		assertTrue(minimum < 0.35F, "clouds can fade nearly away before reforming");
		assertTrue(maximum > 0.90F, "clouds return to full density during their cycle");
	}
}
