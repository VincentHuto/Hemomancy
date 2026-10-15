package com.vincenthuto.hemomancy.client.render.world.rite;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class SwallowedFragmentRulesTest {
	private static final Vec3 START = new Vec3(0.0D, -1.2D, 0.4D);
	private static final double GIRTH = 0.3D;
	private static final double HALF_LENGTH = 0.8D;

	@Test
	void crumbsLeaveTheBreakAndCoilOntoTheBody() {
		assertTrue(SwallowedFragmentRules.offset(0.0F, 0, 0.0D, START, GIRTH, HALF_LENGTH).distanceTo(START) < 1.0E-6);
		Vec3 coiled = SwallowedFragmentRules.offset(SwallowedFragmentRules.TRAVEL_TICKS, 0, 0.0D, START, GIRTH, HALF_LENGTH);
		assertEquals(GIRTH, Math.hypot(coiled.x, coiled.z), 1.0E-6, "wound round the body at its girth");
		assertTrue(Math.abs(coiled.y) <= HALF_LENGTH, "along the body, not floating past its ends");
	}

	@Test
	void theySnakeAlongTheBodyAndDrawTighter() {
		float settled = SwallowedFragmentRules.TRAVEL_TICKS + 2.0F;
		Vec3 now = SwallowedFragmentRules.offset(settled, 3, 10.0D, START, GIRTH, HALF_LENGTH);
		Vec3 later = SwallowedFragmentRules.offset(settled, 3, 20.0D, START, GIRTH, HALF_LENGTH);
		assertTrue(now.distanceTo(later) > 0.05D, "they keep snaking");
		Vec3 early = SwallowedFragmentRules.offset(settled, 3, 0.0D, START, GIRTH, HALF_LENGTH);
		Vec3 condensed = SwallowedFragmentRules.offset(settled + 150.0F, 3, 0.0D, START, GIRTH, HALF_LENGTH);
		assertTrue(Math.hypot(condensed.x, condensed.z) < Math.hypot(early.x, early.z), "they draw in toward the body");
		assertTrue(SwallowedFragmentRules.size(settled + 400.0F) > 0.0F, "they stay until the daemon is gone");
	}

	@Test
	void theCoilTipsAndTurnsWithTheDaemon() {
		Vec3 local = new Vec3(0.3D, 0.6D, 0.0D);
		Vec3[] upright = SwallowedFragmentRules.frame(new Vec3(0, 1, 0), 0.0D);
		assertEquals(0.6D, SwallowedFragmentRules.toWorld(local, upright).y, 1.0E-6, "upright, along the body is up");
		Vec3[] flying = SwallowedFragmentRules.frame(new Vec3(1, 0, 0), Math.toRadians(-90.0D));
		assertEquals(0.6D, SwallowedFragmentRules.toWorld(local, flying).x, 1.0E-6,
				"lying along its path, along the body is the way it flies");
		Vec3[] turned = SwallowedFragmentRules.frame(new Vec3(0, 1, 0), Math.toRadians(90.0D));
		assertTrue(SwallowedFragmentRules.toWorld(local, turned).distanceTo(SwallowedFragmentRules.toWorld(local, upright)) > 0.1D,
				"turning the daemon turns the coil");
		Vec3 world = new Vec3(0.2D, -0.4D, 0.7D);
		assertTrue(SwallowedFragmentRules.toWorld(SwallowedFragmentRules.toLocal(world, flying), flying).distanceTo(world) < 1.0E-9);
	}
}
