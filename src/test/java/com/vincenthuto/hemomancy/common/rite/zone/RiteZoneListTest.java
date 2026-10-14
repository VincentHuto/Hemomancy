package com.vincenthuto.hemomancy.common.rite.zone;

import com.vincenthuto.hemomancy.common.rite.harbinger.SanguineDominionSavedData;
import com.vincenthuto.hemomancy.common.rite.harbinger.SanguineFervorSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

final class RiteZoneListTest {
	private static final UUID OWNER = UUID.fromString("5b0e8f6c-2a7d-4c1e-9f3b-6d2a1c0e7b44");
	private static final BlockPos CENTER = new BlockPos(-40, 64, 25);

	private static CompoundTag oldWorld(String listTag, String timeTag, long time) {
		CompoundTag entry = new CompoundTag();
		entry.putUUID("Owner", OWNER);
		entry.putLong("Center", CENTER.asLong());
		entry.putString("Dimension", "minecraft:the_end");
		entry.putInt("ChunkRadius", 3);
		entry.putLong(timeTag, time);
		ListTag list = new ListTag();
		list.add(entry);
		CompoundTag root = new CompoundTag();
		root.put(listTag, list);
		return root;
	}

	@Test
	void dominionKeepsItsOldListAndCreatedTickAndNeverExpires() {
		CompoundTag saved = oldWorld("dominions", "CreatedTick", 1234L);
		SanguineDominionSavedData data = SanguineDominionSavedData.load(saved, null);

		assertEquals(new SanguineDominionSavedData.DominionEntry(OWNER, CENTER, "minecraft:the_end", 3, 1234L),
				data.getDominions().getFirst());
		assertEquals(saved, data.save(new CompoundTag(), null));
		RiteZone zone = data.zones().getFirst();
		assertEquals(48, zone.blockRadius());
		assertTrue(zone.isPermanent());
		assertFalse(data.removeExpired(Long.MAX_VALUE - 1));
		assertFalse(data.isDirty());
	}

	@Test
	void fervorRoundTripsChunkRadiusAndExpiry() {
		CompoundTag saved = oldWorld("entries", "ExpiryTick", 600L);
		SanguineFervorSavedData data = SanguineFervorSavedData.load(saved, null);

		assertEquals(new SanguineFervorSavedData.FervorEntry(OWNER, CENTER, "minecraft:the_end", 3, 600L),
				data.getEntries().getFirst());
		assertEquals(saved, data.save(new CompoundTag(), null));
		assertTrue(data.isInFervorRange(CENTER.offset(48, -30, -48), "minecraft:the_end", 599));
		assertFalse(data.isInFervorRange(CENTER.offset(49, 0, 0), "minecraft:the_end", 599));
		assertFalse(data.isInFervorRange(CENTER, "minecraft:the_end", 600));
		assertFalse(data.isInFervorRange(CENTER, "minecraft:overworld", 599));
	}

	@Test
	void containmentIsDimensionScopedAndExpiryIsExclusive() {
		RiteZoneList list = new RiteZoneList(RiteZoneKind.STILL_WATERS);
		var nether = RiteZone.requireDimensionKey("minecraft:the_nether");
		var overworld = RiteZone.requireDimensionKey("minecraft:overworld");
		list.add(RiteZone.timed(RiteZoneKind.STILL_WATERS, OWNER, CENTER, nether, 8, 100L));

		assertTrue(list.contains(CENTER.offset(8, 0, 8), nether, 99));
		assertFalse(list.contains(CENTER.offset(9, 0, 0), nether, 99));
		assertFalse(list.contains(CENTER, overworld, 99));
		assertFalse(list.contains(CENTER, nether, 100));
		assertEquals(1, list.activeIn(nether, 99).size());
		assertTrue(list.activeIn(overworld, 99).isEmpty());
		assertFalse(list.pruneExpired(99));
		assertTrue(list.pruneExpired(100));
		assertTrue(list.isEmpty());
	}

	@Test
	void replaceForOwnerKeepsOneZonePerOwner() {
		RiteZoneList list = new RiteZoneList(RiteZoneKind.SANGUINE_DOMINION);
		var overworld = RiteZone.requireDimensionKey("minecraft:overworld");
		UUID other = UUID.randomUUID();
		RiteZone first = RiteZone.permanent(RiteZoneKind.SANGUINE_DOMINION, OWNER, BlockPos.ZERO, overworld, 48, 1L);
		RiteZone rival = RiteZone.permanent(RiteZoneKind.SANGUINE_DOMINION, other, BlockPos.ZERO, overworld, 48, 2L);
		RiteZone moved = RiteZone.permanent(RiteZoneKind.SANGUINE_DOMINION, OWNER, new BlockPos(500, 70, 0), overworld, 48, 3L);

		assertNull(list.replaceForOwner(first));
		assertNull(list.replaceForOwner(rival));
		assertEquals(first, list.replaceForOwner(moved));
		assertEquals(2, list.zones().size());
		assertTrue(list.ownerCovers(OWNER, new BlockPos(510, 0, 10), overworld, 0));
		assertFalse(list.ownerCovers(OWNER, BlockPos.ZERO, overworld, 0));
		assertTrue(list.ownerCovers(other, BlockPos.ZERO, overworld, 0));
	}

	@Test
	void unreadableEntriesAreSkippedNotFatal() {
		CompoundTag broken = new CompoundTag();
		broken.putString("Dimension", "minecraft:overworld");
		ListTag list = new ListTag();
		list.add(broken);
		CompoundTag root = new CompoundTag();
		root.put("entries", list);

		RiteZoneList zones = new RiteZoneList(RiteZoneKind.LETHE_COVENANT);
		zones.load(root);
		assertTrue(zones.isEmpty());
	}

	@Test
	void letheWinsInsideItsOwnDomain() {
		assertEquals(RiteZoneSpawnRules.Verdict.DENY, RiteZoneSpawnRules.resolve(true, true, true));
		assertEquals(RiteZoneSpawnRules.Verdict.DEFAULT, RiteZoneSpawnRules.resolve(true, true, false));
		assertEquals(RiteZoneSpawnRules.Verdict.DEFAULT, RiteZoneSpawnRules.resolve(true, false, false));
		assertEquals(RiteZoneSpawnRules.Verdict.FORCE, RiteZoneSpawnRules.resolve(false, true, true));
		assertEquals(RiteZoneSpawnRules.Verdict.DEFAULT, RiteZoneSpawnRules.resolve(false, false, true));
	}
}
