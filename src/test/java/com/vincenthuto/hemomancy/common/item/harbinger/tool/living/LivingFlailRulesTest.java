package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

final class LivingFlailRulesTest {
	private static Class<?> rules() throws ClassNotFoundException {
		return Class.forName("com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingFlailRules");
	}

	private static float number(String method, Class<?> argumentType, Object argument) throws Exception {
		Method target = rules().getMethod(method, argumentType);
		return ((Number) target.invoke(null, argument)).floatValue();
	}

	@Test
	void chargeClampsAtTheFiveSecondCap() throws Exception {
		assertEquals(0.0F, number("charge", int.class, 0), 0.0001F);
		assertEquals(0.5F, number("charge", int.class, 50), 0.0001F);
		assertEquals(1.0F, number("charge", int.class, 100), 0.0001F);
		assertEquals(1.0F, number("charge", int.class, 180), 0.0001F);
	}

	@Test
	void everyImpactValueInterpolatesAtEndpointsAndMidpoint() throws Exception {
		assertEquals(0.9F, number("launchSpeed", float.class, 0.0F), 0.0001F);
		assertEquals(1.35F, number("launchSpeed", float.class, 0.5F), 0.0001F);
		assertEquals(1.8F, number("launchSpeed", float.class, 1.0F), 0.0001F);
		assertEquals(0.75F, number("visualScale", float.class, 0.0F), 0.0001F);
		assertEquals(1.125F, number("visualScale", float.class, 0.5F), 0.0001F);
		assertEquals(1.5F, number("visualScale", float.class, 1.0F), 0.0001F);
		assertEquals(35.0F, number("lifetimeTicks", float.class, 0.0F), 0.0001F);
		assertEquals(52.5F, number("lifetimeTicks", float.class, 0.5F), 0.0001F);
		assertEquals(70.0F, number("lifetimeTicks", float.class, 1.0F), 0.0001F);
		assertEquals(6.0F, number("damage", float.class, 0.0F), 0.0001F);
		assertEquals(10.0F, number("damage", float.class, 0.5F), 0.0001F);
		assertEquals(14.0F, number("damage", float.class, 1.0F), 0.0001F);
		assertEquals(2.5F, number("impactRadius", float.class, 0.0F), 0.0001F);
		assertEquals(4.25F, number("impactRadius", float.class, 0.5F), 0.0001F);
		assertEquals(6.0F, number("impactRadius", float.class, 1.0F), 0.0001F);
		assertEquals(0.6F, number("knockback", float.class, 0.0F), 0.0001F);
		assertEquals(1.1F, number("knockback", float.class, 0.5F), 0.0001F);
		assertEquals(1.6F, number("knockback", float.class, 1.0F), 0.0001F);
		assertEquals(60.0F, number("slownessTicks", float.class, 0.0F), 0.0001F);
		assertEquals(110.0F, number("slownessTicks", float.class, 0.5F), 0.0001F);
		assertEquals(160.0F, number("slownessTicks", float.class, 1.0F), 0.0001F);
		assertEquals(1.0F, number("snowRadius", float.class, 0.0F), 0.0001F);
		assertEquals(3.0F, number("snowRadius", float.class, 0.5F), 0.0001F);
		assertEquals(5.0F, number("snowRadius", float.class, 1.0F), 0.0001F);
	}

	@Test
	void releaseAndMaximumCueUseExactTickBoundaries() throws Exception {
		Method mayFire = rules().getMethod("mayFire", int.class);
		Method maxCue = rules().getMethod("shouldPlayMaximumCue", int.class);
		assertFalse((boolean) mayFire.invoke(null, 4));
		assertTrue((boolean) mayFire.invoke(null, 5));
		assertFalse((boolean) maxCue.invoke(null, 99));
		assertTrue((boolean) maxCue.invoke(null, 100));
		assertFalse((boolean) maxCue.invoke(null, 101));
		assertEquals(1, ((Number) rules().getMethod("slownessAmplifier", float.class).invoke(null, 0.49F)).intValue());
		assertEquals(2, ((Number) rules().getMethod("slownessAmplifier", float.class).invoke(null, 0.5F)).intValue());
	}

	@Test
	void orbitAccelerationReachesFullSpeedAfterThirtyTicks() throws Exception {
		assertEquals(0.0F, number("orbitSpeedScale", int.class, 0), 0.0001F);
		assertEquals(0.5F, number("orbitSpeedScale", int.class, 15), 0.0001F);
		assertEquals(1.0F, number("orbitSpeedScale", int.class, 30), 0.0001F);
		assertEquals(1.0F, number("orbitSpeedScale", int.class, 100), 0.0001F);
	}

	@Test
	void maximumChargeFlashEndsWithoutReplayingDuringAnExtendedHold() throws Exception {
		Method flash = rules().getMethod("maximumChargeFlash", int.class, float.class);
		assertEquals(0.0F, ((Number) flash.invoke(null, 99, 0.0F)).floatValue(), 0.0001F);
		assertTrue(((Number) flash.invoke(null, 100, 0.0F)).floatValue() > 0.0F);
		assertEquals(0.0F, ((Number) flash.invoke(null, 108, 0.0F)).floatValue(), 0.0001F);
		assertEquals(0.0F, ((Number) flash.invoke(null, 180, 0.0F)).floatValue(), 0.0001F);
	}

	@Test
	void integratedOrbitAngleIsContinuousAtTheAccelerationBoundary() throws Exception {
		Method angle = rules().getMethod("orbitAngle", int.class, float.class);
		float before = ((Number) angle.invoke(null, 29, 1.0F)).floatValue();
		float boundary = ((Number) angle.invoke(null, 30, 0.0F)).floatValue();
		float after = ((Number) angle.invoke(null, 30, 1.0F)).floatValue();
		assertEquals(before, boundary, 0.0001F);
		assertTrue(after > boundary);
		assertTrue(after - boundary <= 0.8F + 0.0001F);
	}

	@Test
	void launchDirectionStaysInsideThePlayersAimConeAtEveryOrbitPhase() {
		Vec3 look = new Vec3(0.0D, -0.2D, 1.0D).normalize();
		Vec3[] orbitTangents = {
				new Vec3(1.0D, 0.0D, 0.0D),
				new Vec3(-1.0D, 0.0D, 0.0D),
				new Vec3(0.0D, 0.0D, 1.0D),
				new Vec3(0.0D, 0.0D, -1.0D)
		};
		for (Vec3 tangent : orbitTangents) {
			Vec3 launch = LivingFlailRules.launchDirection(look, tangent, 1.0F);
			assertEquals(1.0D, launch.length(), 0.0001D);
			assertTrue(launch.dot(look) >= Math.cos(Math.toRadians(10.0D)),
					() -> "launch escaped the 10-degree aim cone for tangent " + tangent + ": " + launch);
		}
	}

	@Test
	void northeastFacingRightHandLaunchNeverStartsOverTheLeftShoulder() {
		Vec3 look = new Vec3(1.0D, 0.0D, -1.0D).normalize();
		Vec3 right = new Vec3(1.0D, 0.0D, 1.0D).normalize();
		double[] phases = { 0.0D, Math.PI * 0.5D, Math.PI, Math.PI * 1.5D };
		for (double phase : phases) {
			LivingFlailRules.LaunchGeometry geometry = LivingFlailRules.launchGeometry(look, phase, true);
			assertTrue(geometry.offset().dot(right) >= 0.4D,
					() -> "right-hand launch crossed the player at orbit phase " + phase + ": " + geometry.offset());
		}
	}
}
