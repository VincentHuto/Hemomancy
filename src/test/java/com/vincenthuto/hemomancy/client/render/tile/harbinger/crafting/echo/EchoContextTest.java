package com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.echo;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EchoContextTest {
	private static EchoCast.EchoContext at(float figure) {
		return new EchoCast.EchoContext(null, new Vec3(0, 1.25 * figure, 0), Vec3.ZERO, new Vec3(0, 0, -1), figure, null);
	}

	@Test
	void castsAimAheadAlongTheLoomsFrontAtChestHeight() {
		var ctx = at(1f);
		Vec3 target = ctx.target();
		assertTrue(target.z < -2.0 && Math.abs(target.x) < 1e-9, "the target stands ahead of the echo");
		assertEquals(ctx.chest().y, target.y, 1e-9);
		assertEquals(-3.0, ctx.ahead(3.0).z, 1e-9);
	}

	@Test
	void everyDistanceScalesWithTheFigure() {
		var small = at(0.42f);
		var whole = at(1f);
		assertEquals(small.target().z * (1 / 0.42), whole.target().z, 1e-6);
		assertEquals(0.42 * 4.0, small.scaled(4.0), 1e-6);
		assertTrue(small.chest().y < whole.chest().y);
	}

	@Test
	void theSideIsHorizontalAndPerpendicular() {
		Vec3 side = at(1f).side();
		assertEquals(0, side.y, 1e-9);
		assertEquals(0, side.dot(new Vec3(0, 0, -1)), 1e-9);
		assertEquals(1, side.length(), 1e-9);
	}
}
