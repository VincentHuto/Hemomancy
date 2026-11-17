package com.vincenthuto.hemomancy.client.render.world.circus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

class ShowMarksTest {
	@Test
	void emptyHasNoRing() {
		assertFalse(ShowMarks.EMPTY.hasRing());
	}

	@Test
	void ringCentreIsTheCurbMean() {
		List<BlockPos> curb = new ArrayList<>();
		for (int i = 0; i < 16; i++) {
			double a = i * Math.PI / 8.0D;
			curb.add(new BlockPos((int) Math.round(10 + 5 * Math.cos(a)), 1, (int) Math.round(20 + 5 * Math.sin(a))));
		}
		var ring = ShowMarks.ring(curb);
		assertEquals(10.5D, ring.centre().x, 0.2D);
		assertEquals(20.5D, ring.centre().z, 0.2D);
		assertEquals(1.0D, ring.centre().y, 1.0E-6D);
		assertEquals(5.0D, ring.radius(), 0.4D);
		assertTrue(ShowMarks.ring(List.of()).radius() == 0.0D);
	}
}
