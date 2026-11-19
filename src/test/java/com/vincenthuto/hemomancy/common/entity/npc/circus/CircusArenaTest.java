package com.vincenthuto.hemomancy.common.entity.npc.circus;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Chunk 15 T13: one arena record for the ring, shared by the acts and the finale. */
class CircusArenaTest {
	@Test
	void theLeashHoldsTheBigTopAndNoFurther() {
		CircusArena arena = CircusArena.of(new BlockPos(100, 64, 100));
		assertTrue(arena.holds(new Vec3(100.5D, 64.0D, 100.5D)));
		assertTrue(arena.holds(new Vec3(100.5D + 23.0D, 64.0D, 100.5D)));
		assertFalse(arena.holds(new Vec3(100.5D + 25.0D, 64.0D, 100.5D)));
		assertEquals(200, arena.graceTicks(), "the finale's grace before a straying holder loses the site");
	}
}
