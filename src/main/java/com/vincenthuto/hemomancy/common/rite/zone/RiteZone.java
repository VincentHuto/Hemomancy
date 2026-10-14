package com.vincenthuto.hemomancy.common.rite.zone;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * One persistent rite area: a square column of {@code blockRadius} blocks on
 * each side of {@code center}, ignoring height.
 *
 * @param expiryTick  game tick at which the zone ends, or {@link #PERMANENT}
 * @param createdTick game tick the zone was cast; only permanent kinds save it
 */
public record RiteZone(RiteZoneKind kind, UUID owner, BlockPos center, ResourceKey<Level> dimension,
		int blockRadius, long expiryTick, long createdTick) {
	public static final long PERMANENT = -1L;

	public static RiteZone timed(RiteZoneKind kind, UUID owner, BlockPos center, ResourceKey<Level> dimension,
			int blockRadius, long expiryTick) {
		return new RiteZone(kind, owner, center, dimension, blockRadius, expiryTick, 0L);
	}

	public static RiteZone permanent(RiteZoneKind kind, UUID owner, BlockPos center, ResourceKey<Level> dimension,
			int blockRadius, long createdTick) {
		return new RiteZone(kind, owner, center, dimension, blockRadius, PERMANENT, createdTick);
	}

	public boolean isPermanent() {
		return expiryTick == PERMANENT;
	}

	public boolean isActive(long gameTime) {
		return isPermanent() || gameTime < expiryTick;
	}

	/** Inclusive square test on X/Z; height is ignored. */
	public boolean covers(BlockPos pos) {
		return Math.abs(pos.getX() - center.getX()) <= blockRadius
				&& Math.abs(pos.getZ() - center.getZ()) <= blockRadius;
	}

	public boolean contains(BlockPos pos, ResourceKey<Level> level, long gameTime) {
		return isActive(gameTime) && dimension.equals(level) && covers(pos);
	}

	public String dimensionId() {
		return dimension.location().toString();
	}

	/** Parses a saved dimension id, or null when it is not a valid resource location. */
	@Nullable
	public static ResourceKey<Level> dimensionKey(String id) {
		ResourceLocation location = ResourceLocation.tryParse(id);
		return location == null ? null : ResourceKey.create(Registries.DIMENSION, location);
	}

	public static ResourceKey<Level> requireDimensionKey(String id) {
		ResourceKey<Level> key = dimensionKey(id);
		if (key == null) throw new IllegalArgumentException("Invalid dimension id: " + id);
		return key;
	}
}
