package com.vincenthuto.hemomancy.common.encounter;

import com.vincenthuto.hemomancy.common.init.BlockInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * What a corridor gate carries so a client walking back can see the overworld through the hole: the blocks around the
 * husk, nearest first, the sky's colour there and the hour. Taken by the husk when it takes the room, kept by
 * {@link ChamberOfWillManager} and handed to the gate's clients. Blocks are packed as {@link ChamberProxySnapshot}
 * packs them, relative to the husk.
 */
public record HuskOutsideSnapshot(BlockPos husk, Direction facing, int skyColor, float timeOfDay, long[] blocks, long stamp) {
	/** The most blocks carried; the ground around a husk fills it, so the nearest come first. */
	public static final int CAP = 6000;
	/** How far around the husk is taken, and how far below and above it. */
	public static final int RADIUS = 16;
	public static final int BELOW = 4;
	public static final int ABOVE = 8;

	public static HuskOutsideSnapshot take(ServerLevel level, BlockPos husk, Direction facing) {
		int sky = level.getBiome(husk).value().getSkyColor();
		return new HuskOutsideSnapshot(husk.immutable(), facing, sky, level.getTimeOfDay(1.0F), scan(level, husk),
				level.getServer().overworld().getGameTime());
	}

	/** Every block around the husk but air and the husk itself, ring by ring outward, up to the cap. */
	static long[] scan(ServerLevel level, BlockPos husk) {
		List<Long> out = new ArrayList<>();
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		int minY = Math.max(level.getMinBuildHeight(), husk.getY() - BELOW);
		int maxY = Math.min(level.getMaxBuildHeight() - 1, husk.getY() + ABOVE);
		for (int ring = 0; ring <= RADIUS; ring++) {
			for (int dx = -ring; dx <= ring; dx++) {
				for (int dz = -ring; dz <= ring; dz++) {
					if (Math.max(Math.abs(dx), Math.abs(dz)) != ring) continue;
					if (!level.hasChunkAt(husk.getX() + dx, husk.getZ() + dz)) continue;
					for (int y = minY; y <= maxY; y++) {
						if (dx == 0 && dz == 0 && (y == husk.getY() || y == husk.getY() + 1)) continue;
						pos.set(husk.getX() + dx, y, husk.getZ() + dz);
						BlockState state = level.getBlockState(pos);
						if (state.isAir() || state.is(BlockInit.sanguine_monolith.get())) continue;
						out.add(ChamberProxySnapshot.pack(dx, y - husk.getY(), dz, Block.getId(state)));
						if (out.size() >= CAP) return toArray(out);
					}
				}
			}
		}
		return toArray(out);
	}

	private static long[] toArray(List<Long> list) {
		long[] array = new long[list.size()];
		for (int i = 0; i < array.length; i++) array[i] = list.get(i);
		return array;
	}

	/** How bright the sky is at this hour, as the level colours its own sky: full at noon, dark at midnight. */
	public static float skyBrightness(float timeOfDay) {
		return Mth.clamp(Mth.cos(timeOfDay * Mth.TWO_PI) * 2.0F + 0.5F, 0.0F, 1.0F);
	}

	public void write(CompoundTag tag) {
		tag.putLong("Husk", husk.asLong());
		tag.putInt("Facing", facing.get3DDataValue());
		tag.putInt("Sky", skyColor);
		tag.putFloat("Hour", timeOfDay);
		tag.putLongArray("Blocks", blocks);
		tag.putLong("Stamp", stamp);
	}

	@Nullable
	public static HuskOutsideSnapshot read(CompoundTag tag) {
		if (!tag.contains("Husk")) return null;
		return new HuskOutsideSnapshot(BlockPos.of(tag.getLong("Husk")), Direction.from3DDataValue(tag.getInt("Facing")),
				tag.getInt("Sky"), tag.getFloat("Hour"), tag.getLongArray("Blocks"), tag.getLong("Stamp"));
	}
}
