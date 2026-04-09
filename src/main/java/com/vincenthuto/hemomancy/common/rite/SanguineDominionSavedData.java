package com.vincenthuto.hemomancy.common.rite;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * World-level persistence for Blood Domains established via the Rite of
 * Sanguine Dominion. Each domain has an owner, center position, dimension,
 * chunk radius, and creation timestamp.
 * <p>
 * Effects within a domain:
 * <ul>
 *   <li>Owner's manipulations cost 25% less blood</li>
 *   <li>Non-allied entities take slow bleed damage (0.5/2s)</li>
 *   <li>Blood-related blocks are visually empowered</li>
 * </ul>
 */
public class SanguineDominionSavedData extends SavedData {

	private static final String DATA_NAME = "hemomancy_sanguine_dominions";

	private final List<DominionEntry> dominions = new ArrayList<>();

	public SanguineDominionSavedData() {}

	public static SanguineDominionSavedData get(ServerLevel overworld) {
		return overworld.getDataStorage().computeIfAbsent(
				SanguineDominionSavedData::load, SanguineDominionSavedData::new, DATA_NAME);
	}

	public static SanguineDominionSavedData load(CompoundTag tag) {
		SanguineDominionSavedData data = new SanguineDominionSavedData();
		if (tag.contains("dominions", Tag.TAG_LIST)) {
			ListTag list = tag.getList("dominions", Tag.TAG_COMPOUND);
			for (int i = 0; i < list.size(); i++) {
				CompoundTag entry = list.getCompound(i);
				UUID ownerUUID = entry.getUUID("Owner");
				BlockPos center = BlockPos.of(entry.getLong("Center"));
				String dimension = entry.getString("Dimension");
				int chunkRadius = entry.getInt("ChunkRadius");
				long createdTick = entry.getLong("CreatedTick");
				data.dominions.add(new DominionEntry(ownerUUID, center, dimension, chunkRadius, createdTick));
			}
		}
		return data;
	}

	@Override
	@Nonnull
	public CompoundTag save(@Nonnull CompoundTag tag) {
		ListTag list = new ListTag();
		for (DominionEntry entry : dominions) {
			CompoundTag domTag = new CompoundTag();
			domTag.putUUID("Owner", entry.ownerUUID());
			domTag.putLong("Center", entry.center().asLong());
			domTag.putString("Dimension", entry.dimension());
			domTag.putInt("ChunkRadius", entry.chunkRadius());
			domTag.putLong("CreatedTick", entry.createdTick());
			list.add(domTag);
		}
		tag.put("dominions", list);
		return tag;
	}

	public void addDominion(DominionEntry entry) {
		dominions.add(entry);
		setDirty();
	}

	public List<DominionEntry> getDominions() {
		return dominions;
	}

	/**
	 * Find the dominion that contains a given block position in a given dimension.
	 * Returns null if no dominion covers that location.
	 */
	public DominionEntry getDominionAt(BlockPos pos, String dimension) {
		for (DominionEntry entry : dominions) {
			if (!entry.dimension().equals(dimension)) continue;
			int blockRadius = entry.chunkRadius() * 16;
			BlockPos center = entry.center();
			if (Math.abs(pos.getX() - center.getX()) <= blockRadius
					&& Math.abs(pos.getZ() - center.getZ()) <= blockRadius) {
				return entry;
			}
		}
		return null;
	}

	/**
	 * Check if a given player owns the dominion at a specific position.
	 */
	public boolean isOwnerAt(UUID playerUUID, BlockPos pos, String dimension) {
		DominionEntry dom = getDominionAt(pos, dimension);
		return dom != null && dom.ownerUUID().equals(playerUUID);
	}

	/**
	 * A persistent blood domain entry.
	 */
	public record DominionEntry(UUID ownerUUID, BlockPos center, String dimension,
			int chunkRadius, long createdTick) {}
}
