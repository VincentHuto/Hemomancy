package com.vincenthuto.hemomancy.common.world.ground;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * A lit cell keeps the player who lit it through a save, so a hot or cold run still edits for its owner after a restart;
 * a cell nobody owns saves no owner key and loads as ownerless. The furnaces a hot cell may burn are kept too.
 */
class GroundRunSavedDataTest {
	@Test
	void aLitCellKeepsItsOwnerThroughASave() {
		UUID owner = UUID.fromString("6f1c2a54-0d3e-4b8a-9a63-6b0f6b2c1d7e");
		GroundRunSavedData data = new GroundRunSavedData();
		data.put(new BlockPos(1, 2, 3), new GroundRunSavedData.Lit(10L, 410L, BlockPos.ZERO.asLong(), GroundFunction.HOT, false, owner, 0b100100));
		data.put(new BlockPos(4, 2, 3), new GroundRunSavedData.Lit(11L, 411L, BlockPos.ZERO.asLong(), GroundFunction.COLD, true, null, 0));

		CompoundTag saved = data.save(new CompoundTag(), null);
		GroundRunSavedData loaded = GroundRunSavedData.load(saved, null);

		assertEquals(data.get(new BlockPos(1, 2, 3)), loaded.get(new BlockPos(1, 2, 3)));
		assertEquals(owner, loaded.get(new BlockPos(1, 2, 3)).owner());
		assertNull(loaded.get(new BlockPos(4, 2, 3)).owner());
		assertEquals(GroundFunction.COLD, loaded.get(new BlockPos(4, 2, 3)).function());
		assertEquals(0b100100, loaded.get(new BlockPos(1, 2, 3)).furnaceSides());
	}
}
