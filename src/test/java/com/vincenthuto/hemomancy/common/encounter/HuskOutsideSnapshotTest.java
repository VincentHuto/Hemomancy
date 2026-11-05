package com.vincenthuto.hemomancy.common.encounter;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

final class HuskOutsideSnapshotTest {
	@Test
	void whatIsWrittenIsReadBack() {
		long[] blocks = { ChamberProxySnapshot.pack(-12, -4, 12, 7), ChamberProxySnapshot.pack(0, 8, 0, 99) };
		var taken = new HuskOutsideSnapshot(new BlockPos(10, 64, -3), Direction.WEST, 0x78A7FF, 0.25F, blocks, 123L);
		CompoundTag tag = new CompoundTag();
		taken.write(tag);
		var back = HuskOutsideSnapshot.read(tag);
		assertEquals(taken.husk(), back.husk());
		assertEquals(Direction.WEST, back.facing());
		assertEquals(0x78A7FF, back.skyColor());
		assertEquals(0.25F, back.timeOfDay(), 1e-6);
		assertArrayEquals(blocks, back.blocks());
		assertEquals(123L, back.stamp());
		assertNull(HuskOutsideSnapshot.read(new CompoundTag()), "nothing written, nothing read");
	}

	@Test
	void theSkyIsBrightestAtNoonAndDarkAtMidnight() {
		assertEquals(1.0F, HuskOutsideSnapshot.skyBrightness(0.0F), 1e-6, "dawn-side noon in Minecraft's clock");
		assertEquals(0.0F, HuskOutsideSnapshot.skyBrightness(0.5F), 1e-6);
	}
}
