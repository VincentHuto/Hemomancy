package com.vincenthuto.hemomancy.client.model.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperWeaponAction;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

final class VesperWeaponAnimationRulesTest {
	@Test
	void actionsEaseInAndRecoverToIdleBeforeServerStateClears() throws Exception {
		Method blend = rules().getMethod("actionBlend", VesperWeaponAction.class, float.class);
		VesperWeaponAction action = VesperWeaponAction.LEAPING_CLEAVE;
		assertEquals(0.0F, invoke(blend, action, 0.0F), 0.0001F);
		assertTrue(invoke(blend, action, 2.5F) > 0.2F);
		assertTrue(invoke(blend, action, 6.0F) > 0.95F);
		assertTrue(invoke(blend, action, action.durationTicks() - 1.0F) < 0.1F);
		assertEquals(0.0F, invoke(blend, action, action.durationTicks()), 0.0001F);
	}

	@Test
	void heavySwingMovesContinuouslyThroughAnticipationContactAndFollowThrough() throws Exception {
		Method arc = rules().getMethod("swingArc", VesperWeaponAction.class, float.class);
		VesperWeaponAction action = VesperWeaponAction.ICHIMONJI;
		assertEquals(0.0F, invoke(arc, action, 0.0F), 0.0001F);
		assertTrue(invoke(arc, action, action.impactTick() - 3.0F) < -0.9F);
		assertTrue(invoke(arc, action, action.impactTick() + 1.0F) > 0.9F);
		assertTrue(Math.abs(invoke(arc, action, action.durationTicks() - 0.5F)) < 0.1F);

		float previous = invoke(arc, action, 0.0F);
		for (float tick = 0.25F; tick <= action.durationTicks(); tick += 0.25F) {
			float current = invoke(arc, action, tick);
			assertTrue(Math.abs(current - previous) < 0.28F,
					"swing snapped between fractional frames near tick " + tick);
			previous = current;
		}
	}

	@Test
	void comboContactsAlternateDirectionWithoutTriangularOneTickSnaps() throws Exception {
		Method motion = rules().getMethod("contactMotion", VesperWeaponAction.class, float.class);
		VesperWeaponAction action = VesperWeaponAction.CROSSCUT;
		assertTrue(invoke(motion, action, 15.0F) > 0.95F);
		assertTrue(invoke(motion, action, 26.0F) < -0.95F);

		float previous = invoke(motion, action, 0.0F);
		for (float tick = 0.25F; tick <= action.durationTicks(); tick += 0.25F) {
			float current = invoke(motion, action, tick);
			assertTrue(Math.abs(current - previous) < 0.32F,
					"combo snapped between fractional frames near tick " + tick);
			previous = current;
		}
	}

	@Test
	void stanceMorphUsesAnEasedContinuousBlend() throws Exception {
		Method stance = rules().getMethod("stanceBlend", float.class);
		assertEquals(0.0F, (float) stance.invoke(null, 0.0F), 0.0001F);
		assertTrue((float) stance.invoke(null, 7.5F) < 0.25F);
		assertEquals(0.5F, (float) stance.invoke(null, 15.0F), 0.0001F);
		assertTrue((float) stance.invoke(null, 22.5F) > 0.75F);
		assertEquals(1.0F, (float) stance.invoke(null, 30.0F), 0.0001F);
	}

	@Test
	void cycloneAcceleratesThenDeceleratesWithoutReversingAtRecovery() throws Exception {
		Method spin = rules().getMethod("cycloneSpin", VesperWeaponAction.class, float.class, float.class);
		VesperWeaponAction action = VesperWeaponAction.SICKLE_CYCLONE;
		assertEquals(0.0F, (float) spin.invoke(null, action, 0.0F, 1.0F), 0.0001F);
		assertTrue((float) spin.invoke(null, action, action.impactTick(), 1.0F) < 0.0F);

		float previous = (float) spin.invoke(null, action, action.impactTick(), 1.0F);
		for (float tick = action.impactTick() + 0.25F; tick <= action.durationTicks(); tick += 0.25F) {
			float current = (float) spin.invoke(null, action, tick, 1.0F);
			assertTrue(current >= previous, "cyclone reversed during recovery near tick " + tick);
			previous = current;
		}
		assertEquals((float) (Math.PI * 4.0D), previous, 0.001F);
	}

	@Test
	void phaseOneTrampleFlowsFromCrouchThroughImpactAndRecovery() throws Exception {
		Method pitch = rules().getMethod("broodTramplePitch", float.class);
		assertEquals(0.0F, (float) pitch.invoke(null, 0.0F), 0.0001F);
		assertTrue((float) pitch.invoke(null, 22.0F) < -0.14F);
		assertTrue((float) pitch.invoke(null, 38.0F) > 0.2F);
		assertEquals(0.0F, (float) pitch.invoke(null, 62.0F), 0.0001F);
		assertContinuous(pitch, 0.0F, 62.0F, 0.04F);
	}

	@Test
	void phaseOneStingerLinksRepeatedStrikesWithoutTickSnaps() throws Exception {
		Method motion = rules().getMethod("stingerMotion", float.class);
		assertEquals(0.0F, (float) motion.invoke(null, 0.0F), 0.0001F);
		assertTrue((float) motion.invoke(null, 16.0F) > 0.9F);
		assertTrue((float) motion.invoke(null, 28.0F) < -0.9F);
		assertTrue((float) motion.invoke(null, 40.0F) > 0.9F);
		assertEquals(0.0F, (float) motion.invoke(null, 68.0F), 0.0001F);
		assertContinuous(motion, 0.0F, 68.0F, 0.12F);
	}

	@Test
	void bladeIdleOffHandRestsOnTheHandle() throws Exception {
		Method grip = rules().getMethod("twoHandedGrip", EnumBloodTendency.class, float.class);
		assertIdleHandleContact(grip, EnumBloodTendency.ANIMUS, 3.5F);
	}

	@Test
	void axeIdleOffHandRestsFartherDownItsLongHaft() throws Exception {
		Method grip = rules().getMethod("twoHandedGrip", EnumBloodTendency.class, float.class);
		assertIdleHandleContact(grip, EnumBloodTendency.MORTEM, 7.0F);
	}

	@Test
	void offHandStaysOnEachRenderedHandleThroughTheSwingArc() throws Exception {
		Method grip = rules().getMethod("twoHandedGrip", VesperWeaponAction.class, float.class);
		for (float motion : new float[] { -1.0F, -0.5F, 0.0F, 0.5F, 1.0F }) {
			assertHandleContact(grip, VesperWeaponAction.ICHIMONJI, motion, 3.5F, 0.2F);
			assertHandleContact(grip, VesperWeaponAction.CROSSCUT, motion, 3.5F, 0.2F);
			assertHandleContact(grip, VesperWeaponAction.LEAPING_CLEAVE, motion, 7.0F, 2.0F);
			assertHandleContact(grip, VesperWeaponAction.REAPER_SWEEP, motion, 7.0F, 1.1F);
		}
	}

	@Test
	void flailShoulderCarriesMomentumThroughContactWithoutRapidCorrections() throws Exception {
		Method motion = rules().getMethod("flailArmMotion", VesperWeaponAction.class, float.class);
		assertEquals(0.0F, invoke(motion, VesperWeaponAction.CHAIN_SWEEP, 0.0F), 0.0001F);
		assertTrue(invoke(motion, VesperWeaponAction.CHAIN_SWEEP, 12.0F) < -0.7F);
		assertTrue(invoke(motion, VesperWeaponAction.CHAIN_SWEEP, 22.0F) > 0.9F);
		assertEquals(0.0F, invoke(motion, VesperWeaponAction.CHAIN_SWEEP, 30.0F), 0.0001F);
		assertFractionalContinuity(motion, VesperWeaponAction.CHAIN_SWEEP, 0.11F);

		assertTrue(invoke(motion, VesperWeaponAction.HOOK_AND_CRUSH, 16.0F) > 0.8F);
		assertTrue(invoke(motion, VesperWeaponAction.HOOK_AND_CRUSH, 28.0F) < -0.8F);
		assertEquals(0.0F, invoke(motion, VesperWeaponAction.HOOK_AND_CRUSH, 40.0F), 0.0001F);
		assertFractionalContinuity(motion, VesperWeaponAction.HOOK_AND_CRUSH, 0.11F);
	}

	@Test
	void flailElbowFollowsTheShoulderInsteadOfSnappingWithIt() throws Exception {
		Method shoulder = rules().getMethod("flailArmMotion", VesperWeaponAction.class, float.class);
		Method follow = rules().getMethod("flailFollowMotion", VesperWeaponAction.class, float.class);
		float shoulderAtContact = invoke(shoulder, VesperWeaponAction.CHAIN_SWEEP, 18.0F);
		float elbowAtContact = invoke(follow, VesperWeaponAction.CHAIN_SWEEP, 18.0F);
		assertTrue(elbowAtContact < shoulderAtContact - 0.15F,
				"flail elbow has no visible inertial lag at contact");
		assertFractionalContinuity(follow, VesperWeaponAction.CHAIN_SWEEP, 0.11F);
	}

	private static void assertContinuous(Method method, float start, float end, float maxStep) throws Exception {
		float previous = (float) method.invoke(null, start);
		for (float tick = start + 0.25F; tick <= end; tick += 0.25F) {
			float current = (float) method.invoke(null, tick);
			assertTrue(Math.abs(current - previous) < maxStep, "motion snapped near tick " + tick);
			previous = current;
		}
	}

	private static void assertFractionalContinuity(Method method, VesperWeaponAction action,
			float maxStep) throws Exception {
		float previous = invoke(method, action, 0.0F);
		for (float tick = 0.25F; tick <= action.durationTicks(); tick += 0.25F) {
			float current = invoke(method, action, tick);
			assertTrue(Math.abs(current - previous) < maxStep,
					action + " flail motion jerked near tick " + tick);
			previous = current;
		}
	}

	private static float component(Object pose, String name) throws Exception {
		return (float) pose.getClass().getMethod(name).invoke(pose);
	}

	private static void assertHandleContact(Method grip, VesperWeaponAction action, float motion,
			float handSpacing, float tolerance) throws Exception {
		Object pose = grip.invoke(null, action, motion);
		Vec arm = new Vec(component(pose, "armX"), component(pose, "armY"), component(pose, "armZ"));
		Vec elbow = new Vec(component(pose, "elbowX"), component(pose, "elbowY"), component(pose, "elbowZ"));
		Vec leftHand = new Vec(4.1D, -4.1615D, -0.1846D).add(rotate(
				new Vec(1.6173D, 8.7239D, 0.5D).add(rotate(new Vec(-0.0924D, 10.1809D, 0.4669D), elbow)), arm));

		Vec[] right = rightPose(action, motion);
		Vec rightShoulder = new Vec(-4.1D, -4.1615D, -1.1846D);
		Vec rightElbow = new Vec(-2.2396D, 9.3559D, 2.5D);
		Vec anchor = rightShoulder.add(rotate(rightElbow.add(
				rotate(new Vec(-0.8D, 6.72D, -0.48D), right[1])), right[0]));
		Vec handleAxis = rotate(rotate(new Vec(0.0D, 0.0D, -1.0D), right[1]), right[0]);
		Vec target = anchor.add(handleAxis.scale(handSpacing));
		assertTrue(leftHand.subtract(target).length() <= tolerance,
				action + " off-hand missed the handle at motion " + motion);
	}

	private static void assertIdleHandleContact(Method grip, EnumBloodTendency tendency,
			float handSpacing) throws Exception {
		Object pose = grip.invoke(null, tendency, 0.0F);
		Vec arm = new Vec(component(pose, "armX"), component(pose, "armY"), component(pose, "armZ"));
		Vec elbow = new Vec(component(pose, "elbowX"), component(pose, "elbowY"), component(pose, "elbowZ"));
		Vec leftHand = new Vec(4.1D, -4.1615D, -0.1846D).add(rotate(
				new Vec(1.6173D, 8.7239D, 0.5D).add(rotate(new Vec(-0.0924D, 10.1809D, 0.4669D), elbow)), arm));
		double rightArmX = tendency == EnumBloodTendency.ANIMUS ? -0.55D : -0.85D;
		Vec rightArm = new Vec(rightArmX, 0.0D, 0.445D);
		Vec rightElbowRotation = new Vec(-1.0908D, 0.0D, 0.0D);
		Vec anchor = new Vec(-4.1D, -4.1615D, -1.1846D).add(rotate(
				new Vec(-2.2396D, 9.3559D, 2.5D).add(
						rotate(new Vec(-0.8D, 6.72D, -0.48D), rightElbowRotation)), rightArm));
		Vec axis = rotate(rotate(new Vec(0.0D, 0.0D, -1.0D), rightElbowRotation), rightArm);
		assertTrue(leftHand.subtract(anchor.add(axis.scale(handSpacing))).length() < 0.2D,
				tendency + " idle off-hand missed its handle");
	}

	private static Vec[] rightPose(VesperWeaponAction action, float motion) {
		return switch (action) {
			case ICHIMONJI -> new Vec[] {
					new Vec(-1.2D + motion * 1.28D, 0.0D, -motion * 0.16D),
					new Vec(-0.52D + Math.max(0.0D, motion) * 0.22D, 0.0D, 0.0D) };
			case CROSSCUT -> new Vec[] {
					new Vec(-1.32D + Math.abs(motion) * 0.12D, 0.0D, motion * 1.08D),
					new Vec(-1.0908D, 0.0D, motion * 0.18D) };
			case LEAPING_CLEAVE -> new Vec[] {
					new Vec(-1.28D + motion * 1.18D, 0.0D, 0.2D + motion * 0.12D),
					new Vec(-0.48D + Math.max(0.0D, motion) * 0.2D, 0.0D, 0.0D) };
			case REAPER_SWEEP -> new Vec[] {
					new Vec(-1.38D, 0.0D, motion * 1.12D),
					new Vec(-0.58D + Math.abs(motion) * 0.18D, 0.0D, 0.0D) };
			default -> throw new IllegalArgumentException(action.name());
		};
	}

	private static Vec rotate(Vec vector, Vec angles) {
		double cx = Math.cos(angles.x), sx = Math.sin(angles.x);
		double cy = Math.cos(angles.y), sy = Math.sin(angles.y);
		double cz = Math.cos(angles.z), sz = Math.sin(angles.z);
		Vec xRotated = new Vec(vector.x, vector.y * cx - vector.z * sx, vector.y * sx + vector.z * cx);
		Vec yRotated = new Vec(xRotated.x * cy + xRotated.z * sy, xRotated.y,
				-xRotated.x * sy + xRotated.z * cy);
		return new Vec(yRotated.x * cz - yRotated.y * sz, yRotated.x * sz + yRotated.y * cz, yRotated.z);
	}

	private record Vec(double x, double y, double z) {
		Vec add(Vec other) { return new Vec(x + other.x, y + other.y, z + other.z); }
		Vec subtract(Vec other) { return new Vec(x - other.x, y - other.y, z - other.z); }
		Vec scale(double amount) { return new Vec(x * amount, y * amount, z * amount); }
		double length() { return Math.sqrt(x * x + y * y + z * z); }
	}

	private static float invoke(Method method, VesperWeaponAction action, float tick) throws Exception {
		return (float) method.invoke(null, action, tick);
	}

	private static Class<?> rules() {
		try {
			return Class.forName("com.vincenthuto.hemomancy.client.model.entity.boss.endgame.VesperWeaponAnimationRules");
		} catch (ClassNotFoundException missing) {
			return fail("Vesper weapon animation curves are missing");
		}
	}
}
