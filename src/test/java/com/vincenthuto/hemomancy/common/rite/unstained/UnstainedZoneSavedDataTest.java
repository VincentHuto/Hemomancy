package com.vincenthuto.hemomancy.common.rite.unstained;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

final class UnstainedZoneSavedDataTest {
	private static final UUID OWNER = UUID.fromString("038c14ca-e582-4fbb-bd8e-18e14af61233");
	private static final BlockPos CENTER = new BlockPos(12, 70, -9);
	private static final String DIMENSION = "minecraft:the_nether";

	private static CompoundTag oldWorldEntry(String radiusKey, int radius) {
		CompoundTag entry = new CompoundTag();
		entry.putUUID("Owner", OWNER);
		entry.putLong("Center", CENTER.asLong());
		entry.putString("Dimension", DIMENSION);
		entry.putInt(radiusKey, radius);
		entry.putLong("ExpiryTick", 200);
		ListTag entries = new ListTag();
		entries.add(entry);
		CompoundTag root = new CompoundTag();
		root.put("entries", entries);
		return root;
	}

	@Test
	void stillWatersReadsOldWorldRecordAndExpiresAtBoundary() {
		CompoundTag oldWorld = oldWorldEntry("BlockRadius", 16);
		StillWatersSavedData data = StillWatersSavedData.load(oldWorld, null);
		assertEquals(new StillWatersSavedData.StillWatersEntry(OWNER, CENTER, DIMENSION, 16, 200),
				data.getEntries().getFirst());
		assertEquals(oldWorld, data.save(new CompoundTag(), null));
		assertEquals(data.getEntries(), StillWatersSavedData.load(data.save(new CompoundTag(), null), null).getEntries());
		assertTrue(data.isInZone(CENTER.offset(16, 200, -16), DIMENSION, 199));
		assertFalse(data.isInZone(CENTER, "minecraft:overworld", 199));
		assertFalse(data.removeExpired(199));
		assertFalse(data.isDirty());
		assertTrue(data.removeExpired(200));
		assertTrue(data.isDirty());
		assertTrue(data.getEntries().isEmpty());
	}

	@Test
	void paleReadsOldWorldRecordAndExpiresAtBoundary() {
		CompoundTag oldWorld = oldWorldEntry("BlockRadius", 12);
		PaleConsecrationSavedData data = PaleConsecrationSavedData.load(oldWorld, null);
		assertEquals(new PaleConsecrationSavedData.ConsecrationEntry(OWNER, CENTER, DIMENSION, 12, 200),
				data.getEntries().getFirst());
		assertEquals(oldWorld, data.save(new CompoundTag(), null));
		assertEquals(data.getEntries(), PaleConsecrationSavedData.load(data.save(new CompoundTag(), null), null).getEntries());
		assertFalse(data.removeExpired(199));
		assertFalse(data.isDirty());
		assertTrue(data.removeExpired(200));
		assertTrue(data.isDirty());
		assertTrue(data.getEntries().isEmpty());
	}

	@Test
	void letheReadsOldWorldChunkRadiusAndExpiresAtBoundary() {
		CompoundTag oldWorld = oldWorldEntry("ChunkRadius", 5);
		LetheCovenantSavedData data = LetheCovenantSavedData.load(oldWorld, null);
		assertEquals(new LetheCovenantSavedData.CovenantEntry(OWNER, CENTER, DIMENSION, 5, 200),
				data.getEntries().getFirst());
		assertEquals(oldWorld, data.save(new CompoundTag(), null));
		assertEquals(data.getEntries(), LetheCovenantSavedData.load(data.save(new CompoundTag(), null), null).getEntries());
		assertTrue(data.isInDomain(CENTER.offset(80, 200, -80), DIMENSION, 199));
		assertFalse(data.isInDomain(CENTER.offset(81, 0, 0), DIMENSION, 199));
		assertFalse(data.isInDomain(CENTER, "minecraft:overworld", 199));
		assertFalse(data.removeExpired(199));
		assertFalse(data.isDirty());
		assertTrue(data.removeExpired(200));
		assertTrue(data.isDirty());
		assertTrue(data.getEntries().isEmpty());
	}
}
