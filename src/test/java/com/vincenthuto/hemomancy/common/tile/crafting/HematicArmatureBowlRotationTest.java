package com.vincenthuto.hemomancy.common.tile.crafting;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

class HematicArmatureBowlRotationTest {
	@Test
	void particlesUseTheSameHorizontalRotationAsRenderedBowlItems() throws Exception {
		Method rotate = HematicArmatureBlockEntity.class.getDeclaredMethod(
				"rotateBowlOffset", double.class, double.class, Direction.class);
		rotate.setAccessible(true);

		assertEquals(new Vec3(2, 0, 1), rotate.invoke(null, 2D, 1D, Direction.SOUTH));
		assertEquals(new Vec3(-2, 0, -1), rotate.invoke(null, 2D, 1D, Direction.NORTH));
		assertEquals(new Vec3(1, 0, -2), rotate.invoke(null, 2D, 1D, Direction.EAST));
		assertEquals(new Vec3(-1, 0, 2), rotate.invoke(null, 2D, 1D, Direction.WEST));
	}
}
