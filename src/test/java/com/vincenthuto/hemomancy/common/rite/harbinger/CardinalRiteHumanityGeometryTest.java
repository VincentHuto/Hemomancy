package com.vincenthuto.hemomancy.common.rite.harbinger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class CardinalRiteHumanityGeometryTest {
	@Test
	void humanityCloudHasVoidBodyAuraEyesAndBloodWisps() {
		List<CardinalRiteHumanityGeometry.Point> points =
				CardinalRiteHumanityGeometry.cloud(4.0D, 0.0D, 0.0D, 1.0D);

		assertEquals(32, count(points, CardinalRiteHumanityGeometry.Layer.PALE_AURA));
		assertTrue(count(points, CardinalRiteHumanityGeometry.Layer.VOID_CORE) >= 20);
		assertEquals(2, count(points, CardinalRiteHumanityGeometry.Layer.EYE));
		assertEquals(4, count(points, CardinalRiteHumanityGeometry.Layer.BLOOD_WISP));
		assertTrue(points.size() <= 62,
				"the daemon must stay within its low-cost particle geometry budget");
		assertTrue(points.stream().anyMatch(point -> Math.abs(point.z()) > 0.05D),
				"the form must occupy depth instead of lying on one flat plane");
	}

	@Test
	void humanityCoreCondensesAsTheManifestationGrows() {
		List<CardinalRiteHumanityGeometry.Point> small =
				CardinalRiteHumanityGeometry.cloud(0.8D, 0.0D, 0.0D, 1.0D);
		List<CardinalRiteHumanityGeometry.Point> large =
				CardinalRiteHumanityGeometry.cloud(5.0D, 0.0D, 0.0D, 1.0D);

		assertEquals(16, count(small, CardinalRiteHumanityGeometry.Layer.VOID_CORE));
		assertTrue(count(large, CardinalRiteHumanityGeometry.Layer.VOID_CORE)
						>= count(small, CardinalRiteHumanityGeometry.Layer.VOID_CORE) + 8,
				"the mature daemon should fill in without returning to a dense point cloud");
	}

	@Test
	void voidCoreFillsMostOfTheOutlinedBodyWidth() {
		List<CardinalRiteHumanityGeometry.Point> cloud =
				CardinalRiteHumanityGeometry.cloud(4.0D, 0.0D, 0.0D, 1.0D);

		double auraHalfWidth = cloud.stream()
				.filter(point -> point.layer() == CardinalRiteHumanityGeometry.Layer.PALE_AURA)
				.mapToDouble(point -> Math.abs(point.x()))
				.max()
				.orElseThrow();
		double coreHalfWidth = cloud.stream()
				.filter(point -> point.layer() == CardinalRiteHumanityGeometry.Layer.VOID_CORE)
				.mapToDouble(point -> Math.abs(point.x()))
				.max()
				.orElseThrow();

		assertTrue(coreHalfWidth >= auraHalfWidth * 0.70D,
				"void core should occupy most of the silhouette instead of forming a narrow stripe");
	}

	@Test
	void humanitySilhouetteIsTallAndTapersBelowBroadShoulders() {
		List<CardinalRiteHumanityGeometry.Point> aura =
				CardinalRiteHumanityGeometry.cloud(4.0D, 0.0D, 0.0D, 1.0D).stream()
						.filter(point -> point.layer() == CardinalRiteHumanityGeometry.Layer.PALE_AURA)
						.toList();

		double bottomWidth = widthAtNearestHeight(aura, 0.0D);
		double shoulderWidth = widthAtNearestHeight(aura, 2.8D);
		double maximumY = aura.stream().mapToDouble(CardinalRiteHumanityGeometry.Point::y).max().orElseThrow();

		assertTrue(shoulderWidth > bottomWidth * 3.0D,
				"daemon outline should swell from a narrow tail into broad shoulders");
		assertTrue(maximumY >= 4.2D && maximumY <= 4.3D,
				"only the crown should add a modest amount of height");
		assertTrue(maximumVerticalGap(aura) <= 0.3701D,
				"enlarged aura particles should bridge the deliberately sparse outline");
	}

	@Test
	void tallerHeadKeepsAVisibleForeheadAboveTheEyes() {
		List<CardinalRiteHumanityGeometry.Point> cloud =
				CardinalRiteHumanityGeometry.cloud(4.0D, 0.0D, 0.0D, 1.0D);
		double eyeY = cloud.stream()
				.filter(point -> point.layer() == CardinalRiteHumanityGeometry.Layer.EYE)
				.mapToDouble(CardinalRiteHumanityGeometry.Point::y)
				.max()
				.orElseThrow();
		double coreTop = cloud.stream()
				.filter(point -> point.layer() == CardinalRiteHumanityGeometry.Layer.VOID_CORE)
				.mapToDouble(CardinalRiteHumanityGeometry.Point::y)
				.max()
				.orElseThrow();

		assertTrue(coreTop >= eyeY + 0.25D,
				"the dark head should extend clearly above the eyes instead of ending at their level");
	}

	@Test
	void entityScaleMultipliesTheEntireHumanitySilhouette() {
		List<CardinalRiteHumanityGeometry.Point> normal =
				CardinalRiteHumanityGeometry.scaledCloud(1.0D, 0.0D, 0.0D, 1.0D);
		List<CardinalRiteHumanityGeometry.Point> doubled =
				CardinalRiteHumanityGeometry.scaledCloud(2.0D, 0.0D, 0.0D, 1.0D);

		assertEquals(maximumY(normal) * 2.0D, maximumY(doubled), 0.0001D);
		assertEquals(maximumHorizontalRadius(normal) * 2.0D,
				maximumHorizontalRadius(doubled), 0.0001D);
	}

	@Test
	void daemonEyesRemainAnObviousDarkRedAtEveryScale() {
		CardinalRiteHumanityGeometry.Point smallEye =
				firstScaledEye(0.2D);
		CardinalRiteHumanityGeometry.Point largeEye =
				firstScaledEye(1.25D);

		assertDarkRedEye(smallEye);
		assertDarkRedEye(largeEye);
	}

	@Test
	void scaledEntityCondensesItsCoreAsItGrows() {
		List<CardinalRiteHumanityGeometry.Point> small =
				CardinalRiteHumanityGeometry.scaledCloud(0.2D, 0.0D, 0.0D, 1.0D);
		List<CardinalRiteHumanityGeometry.Point> large =
				CardinalRiteHumanityGeometry.scaledCloud(1.25D, 0.0D, 0.0D, 1.0D);

		assertEquals(16, count(small, CardinalRiteHumanityGeometry.Layer.VOID_CORE));
		assertTrue(count(large, CardinalRiteHumanityGeometry.Layer.VOID_CORE)
				>= count(small, CardinalRiteHumanityGeometry.Layer.VOID_CORE) + 8);
	}

	@Test
	void eyesFormVisibleClustersInFrontOfTheVoidCore() {
		List<CardinalRiteHumanityGeometry.Point> cloud =
				CardinalRiteHumanityGeometry.scaledCloud(1.0D, 0.0D, 0.0D, 1.0D);
		List<CardinalRiteHumanityGeometry.Point> eyes = cloud.stream()
				.filter(point -> point.layer() == CardinalRiteHumanityGeometry.Layer.EYE)
				.toList();
		double frontOfCore = cloud.stream()
				.filter(point -> point.layer() == CardinalRiteHumanityGeometry.Layer.VOID_CORE)
				.mapToDouble(CardinalRiteHumanityGeometry.Point::z)
				.max()
				.orElseThrow();

		assertEquals(2, eyes.size(), "one enlarged particle should define each eye");
		assertTrue(eyes.stream().allMatch(point -> point.z() > frontOfCore),
				"eyes should sit in front of the dark core instead of being buried inside it");
	}

	@Test
	void sparseDaemonUsesLargeShortLivedParticles() {
		assertTrue(CardinalRiteHumanityGeometry.particleScale(
				CardinalRiteHumanityGeometry.Layer.VOID_CORE, 1.0D) >= 0.04F,
				"the dedicated diffuse particle expands the compact void-core scale internally");
		assertTrue(CardinalRiteHumanityGeometry.particleScale(
				CardinalRiteHumanityGeometry.Layer.PALE_AURA, 1.0D) >= 0.10F);
		assertTrue(CardinalRiteHumanityGeometry.particleScale(
				CardinalRiteHumanityGeometry.Layer.EYE, 1.0D) >= 0.16F);
		for (CardinalRiteHumanityGeometry.Layer layer : CardinalRiteHumanityGeometry.Layer.values()) {
			assertTrue(CardinalRiteHumanityGeometry.particleLifetime(layer) <= 40);
		}
	}

	@Test
	void voidCoreUsesDedicatedDiffuseGlowInsteadOfOpaqueSmokeSquares() {
		assertEquals(CardinalRiteHumanityGeometry.ParticleStyle.DIFFUSE_GLOW,
				CardinalRiteHumanityGeometry.particleStyle(
						CardinalRiteHumanityGeometry.Layer.VOID_CORE));
		assertEquals(CardinalRiteHumanityGeometry.ParticleStyle.GLOW,
				CardinalRiteHumanityGeometry.particleStyle(
						CardinalRiteHumanityGeometry.Layer.PALE_AURA));
	}

	@Test
	void humanitySwaysCoherentlyAboveAnAnchoredBase() {
		List<CardinalRiteHumanityGeometry.Point> restingAura = auraAtPhase(0.0D);
		List<CardinalRiteHumanityGeometry.Point> leaningAura = auraAtPhase(Math.PI / (2.0D * 0.22D));

		double restingBaseCenter = pairCenterX(restingAura, 0);
		double leaningBaseCenter = pairCenterX(leaningAura, 0);
		double restingHeadCenter = pairCenterX(restingAura, restingAura.size() - 2);
		double leaningHeadCenter = pairCenterX(leaningAura, leaningAura.size() - 2);

		assertEquals(restingBaseCenter, leaningBaseCenter, 0.0001D,
				"the humanity tail should remain rooted while the body sways");
		assertTrue(Math.abs(leaningHeadCenter - restingHeadCenter) > 0.04D,
				"the upper silhouette should visibly lean as the sway phase advances");
	}

	@Test
	void largerHumanitySwaysFartherThanItsEarlyManifestation() {
		double peakPhase = Math.PI / (2.0D * 0.22D);
		double smallLean = Math.abs(headCenterX(0.8D, peakPhase) - headCenterX(0.8D, 0.0D));
		double largeLean = Math.abs(headCenterX(5.0D, peakPhase) - headCenterX(5.0D, 0.0D));

		assertTrue(largeLean > smallLean * 3.0D,
				"the sway should strengthen substantially as the humanity grows");
	}

	@Test
	void unscaledDaemonEyesAreAlsoDarkRed() {
		CardinalRiteHumanityGeometry.Point smallEye = firstEyeAtHeight(0.8D);
		CardinalRiteHumanityGeometry.Point largeEye = firstEyeAtHeight(5.0D);

		assertDarkRedEye(smallEye);
		assertDarkRedEye(largeEye);
	}

	@Test
	void culminationContractsTheHumanityOnlyAtTheEndOfTheRite() {
		assertEquals(0.0D, CardinalRiteHumanityGeometry.absorptionProgress(0.95D), 0.0001D);
		assertEquals(1.0D, CardinalRiteHumanityGeometry.contractionScale(0.95D), 0.0001D);

		double midFlight = CardinalRiteHumanityGeometry.absorptionProgress(0.975D);
		assertTrue(midFlight > 0.0D && midFlight < 1.0D);
		assertTrue(CardinalRiteHumanityGeometry.contractionScale(0.975D) < 1.0D);

		assertEquals(1.0D, CardinalRiteHumanityGeometry.absorptionProgress(1.0D), 0.0001D);
		assertTrue(CardinalRiteHumanityGeometry.contractionScale(1.0D) <= 0.15D);
	}

	@Test
	void daemonBodyTiltsProneAlongItsTravelDirection() {
		CardinalRiteHumanityGeometry.Point upright = CardinalRiteHumanityGeometry.orientPoint(
				new CardinalRiteHumanityGeometry.Point(
						CardinalRiteHumanityGeometry.Layer.VOID_CORE, 0.0D, 2.0D, 0.0D,
						3, 0, 2),
				0.0D, 1.0D, 0.0F);
		CardinalRiteHumanityGeometry.Point prone = CardinalRiteHumanityGeometry.orientPoint(
				new CardinalRiteHumanityGeometry.Point(
						CardinalRiteHumanityGeometry.Layer.VOID_CORE, 0.0D, 2.0D, 0.0D,
						3, 0, 2),
				0.0D, 1.0D, 90.0F);

		assertEquals(2.0D, upright.y(), 0.0001D);
		assertEquals(0.0D, upright.z(), 0.0001D);
		assertEquals(0.0D, prone.y(), 0.0001D);
		assertEquals(2.0D, prone.z(), 0.0001D,
				"a fully prone daemon points its former vertical body axis into its direction of travel");
	}

	private static long count(List<CardinalRiteHumanityGeometry.Point> points,
			CardinalRiteHumanityGeometry.Layer layer) {
		return points.stream().filter(point -> point.layer() == layer).count();
	}

	private static List<CardinalRiteHumanityGeometry.Point> auraAtPhase(double phase) {
		return CardinalRiteHumanityGeometry.cloud(4.0D, phase, 0.0D, 1.0D).stream()
				.filter(point -> point.layer() == CardinalRiteHumanityGeometry.Layer.PALE_AURA)
				.toList();
	}

	private static double headCenterX(double height, double phase) {
		List<CardinalRiteHumanityGeometry.Point> aura =
				CardinalRiteHumanityGeometry.cloud(height, phase, 0.0D, 1.0D).stream()
						.filter(point -> point.layer() == CardinalRiteHumanityGeometry.Layer.PALE_AURA)
						.toList();
		return pairCenterX(aura, aura.size() - 2);
	}

	private static CardinalRiteHumanityGeometry.Point firstEyeAtHeight(double height) {
		return CardinalRiteHumanityGeometry.cloud(height, 0.0D, 0.0D, 1.0D).stream()
				.filter(point -> point.layer() == CardinalRiteHumanityGeometry.Layer.EYE)
				.findFirst()
				.orElseThrow();
	}

	private static CardinalRiteHumanityGeometry.Point firstScaledEye(double scale) {
		return CardinalRiteHumanityGeometry.scaledCloud(scale, 0.0D, 0.0D, 1.0D).stream()
				.filter(point -> point.layer() == CardinalRiteHumanityGeometry.Layer.EYE)
				.findFirst()
				.orElseThrow();
	}

	private static void assertDarkRedEye(CardinalRiteHumanityGeometry.Point eye) {
		assertTrue(eye.red() >= 110 && eye.red() <= 180,
				"eyes should use a dark but readable red channel");
		assertTrue(eye.green() <= 12, "eyes should not wash toward white or pink");
		assertTrue(eye.blue() <= 12, "eyes should not wash toward white or pink");
	}

	private static double pairCenterX(List<CardinalRiteHumanityGeometry.Point> points, int pairStart) {
		return (points.get(pairStart).x() + points.get(pairStart + 1).x()) * 0.5D;
	}

	private static double widthAtNearestHeight(List<CardinalRiteHumanityGeometry.Point> points, double targetY) {
		double nearestY = points.stream()
				.mapToDouble(CardinalRiteHumanityGeometry.Point::y)
				.boxed()
				.min(java.util.Comparator.comparingDouble(y -> Math.abs(y - targetY)))
				.orElseThrow();
		return points.stream()
				.filter(point -> Math.abs(point.y() - nearestY) < 0.0001D)
				.mapToDouble(point -> Math.abs(point.x()))
				.max()
				.orElseThrow();
	}

	private static double maximumVerticalGap(List<CardinalRiteHumanityGeometry.Point> points) {
		List<Double> heights = points.stream()
				.map(CardinalRiteHumanityGeometry.Point::y)
				.distinct()
				.sorted()
				.toList();
		double maximum = 0.0D;
		for (int index = 1; index < heights.size(); index++) {
			maximum = Math.max(maximum, heights.get(index) - heights.get(index - 1));
		}
		return maximum;
	}

	private static double maximumY(List<CardinalRiteHumanityGeometry.Point> points) {
		return points.stream().mapToDouble(CardinalRiteHumanityGeometry.Point::y).max().orElseThrow();
	}

	private static double maximumHorizontalRadius(List<CardinalRiteHumanityGeometry.Point> points) {
		return points.stream()
				.mapToDouble(point -> Math.hypot(point.x(), point.z()))
				.max()
				.orElseThrow();
	}
}
