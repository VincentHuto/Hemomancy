package com.vincenthuto.hemomancy.common.rite.zone;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/** The zones of one {@link RiteZoneKind}, with the NBT layout that kind has always saved. */
public final class RiteZoneList {
	private static final String OWNER = "Owner";
	private static final String CENTER = "Center";
	private static final String DIMENSION = "Dimension";
	private static final String EXPIRY_TICK = "ExpiryTick";
	private static final String CREATED_TICK = "CreatedTick";

	private final RiteZoneKind kind;
	private final List<RiteZone> zones = new ArrayList<>();

	public RiteZoneList(RiteZoneKind kind) {
		this.kind = kind;
	}

	public RiteZoneKind kind() {
		return kind;
	}

	public List<RiteZone> zones() {
		return Collections.unmodifiableList(zones);
	}

	public boolean isEmpty() {
		return zones.isEmpty();
	}

	public void add(RiteZone zone) {
		zones.add(zone);
	}

	/** Keeps one zone per owner: removes the owner's earlier zones and returns the first of them. */
	@Nullable
	public RiteZone replaceForOwner(RiteZone zone) {
		RiteZone previous = zones.stream()
				.filter(existing -> existing.owner().equals(zone.owner()))
				.findFirst().orElse(null);
		zones.removeIf(existing -> existing.owner().equals(zone.owner()));
		zones.add(zone);
		return previous;
	}

	/** Removes zones whose expiry has been reached. Returns true when anything was removed. */
	public boolean pruneExpired(long gameTime) {
		return zones.removeIf(zone -> !zone.isActive(gameTime));
	}

	@Nullable
	public RiteZone zoneAt(BlockPos pos, ResourceKey<Level> dimension, long gameTime) {
		for (RiteZone zone : zones) {
			if (zone.contains(pos, dimension, gameTime)) return zone;
		}
		return null;
	}

	public boolean contains(BlockPos pos, ResourceKey<Level> dimension, long gameTime) {
		return zoneAt(pos, dimension, gameTime) != null;
	}

	public boolean ownerCovers(UUID owner, BlockPos pos, ResourceKey<Level> dimension, long gameTime) {
		for (RiteZone zone : zones) {
			if (zone.owner().equals(owner) && zone.contains(pos, dimension, gameTime)) return true;
		}
		return false;
	}

	public List<RiteZone> activeIn(ResourceKey<Level> dimension, long gameTime) {
		List<RiteZone> result = new ArrayList<>();
		for (RiteZone zone : zones) {
			if (zone.dimension().equals(dimension) && zone.isActive(gameTime)) result.add(zone);
		}
		return result;
	}

	public void load(CompoundTag root) {
		zones.clear();
		if (!root.contains(kind.listTag(), Tag.TAG_LIST)) return;
		ListTag list = root.getList(kind.listTag(), Tag.TAG_COMPOUND);
		for (int i = 0; i < list.size(); i++) {
			RiteZone zone = read(list.getCompound(i));
			if (zone != null) zones.add(zone);
		}
	}

	public CompoundTag save(CompoundTag root) {
		ListTag list = new ListTag();
		for (RiteZone zone : zones) {
			list.add(write(zone));
		}
		root.put(kind.listTag(), list);
		return root;
	}

	@Nullable
	private RiteZone read(CompoundTag tag) {
		if (!tag.hasUUID(OWNER)) return null;
		ResourceKey<Level> dimension = RiteZone.dimensionKey(tag.getString(DIMENSION));
		if (dimension == null) return null;
		UUID owner = tag.getUUID(OWNER);
		BlockPos center = BlockPos.of(tag.getLong(CENTER));
		int blockRadius = tag.getInt(kind.radiusTag()) * kind.blocksPerRadiusUnit();
		return kind.timed()
				? RiteZone.timed(kind, owner, center, dimension, blockRadius, tag.getLong(EXPIRY_TICK))
				: RiteZone.permanent(kind, owner, center, dimension, blockRadius, tag.getLong(CREATED_TICK));
	}

	private CompoundTag write(RiteZone zone) {
		CompoundTag tag = new CompoundTag();
		tag.putUUID(OWNER, zone.owner());
		tag.putLong(CENTER, zone.center().asLong());
		tag.putString(DIMENSION, zone.dimensionId());
		tag.putInt(kind.radiusTag(), zone.blockRadius() / kind.blocksPerRadiusUnit());
		if (kind.timed()) {
			tag.putLong(EXPIRY_TICK, zone.expiryTick());
		} else {
			tag.putLong(CREATED_TICK, zone.createdTick());
		}
		return tag;
	}
}
