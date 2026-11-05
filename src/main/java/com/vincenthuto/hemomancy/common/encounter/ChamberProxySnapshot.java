package com.vincenthuto.hemomancy.common.encounter;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.entity.utility.ArborOfWillEntity;
import com.vincenthuto.hemomancy.common.entity.utility.arbor.ArborOfWillVisualRules;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * What a husk carries so a client can draw its owner's Chamber of Will through the eye: the room's theme, size, the
 * arbor's state and every block in the room, packed. Taken on the server from the Chamber level when the eye wakes;
 * without a Chamber level the room has no blocks and only its sky, arches and arbor are drawn.
 */
public record ChamberProxySnapshot(ResourceLocation theme, BlockPos cell, int radius, int degree, int pomes, long[] blocks,
		long stamp) {
	/** The most blocks a snapshot carries; the floor of the largest room is a few hundred. */
	public static final int CAP = 6000;
	private static final int OFFSET = 32;

	/** One block: its offsets from the cell (each kept in 0..63) and its state id. */
	public static long pack(int dx, int dy, int dz, int stateId) {
		return ((long) stateId << 24) | ((long) (dx + OFFSET) << 16) | ((long) (dy + OFFSET) << 8) | (dz + OFFSET);
	}

	public static int dx(long packed) {
		return (int) ((packed >> 16) & 0xFF) - OFFSET;
	}

	public static int dy(long packed) {
		return (int) ((packed >> 8) & 0xFF) - OFFSET;
	}

	public static int dz(long packed) {
		return (int) (packed & 0xFF) - OFFSET;
	}

	public static int stateId(long packed) {
		return (int) (packed >>> 24);
	}

	/**
	 * The owner's room as it stands. {@code room} is the Chamber level, or null when the server has none; the arbor's
	 * degree and pomes come from the owner when online, else from the arbor standing in the room.
	 */
	public static ChamberProxySnapshot take(ChamberOfWillManager manager, @Nullable ServerLevel room, UUID owner,
			MinecraftServer server) {
		ResourceLocation theme = manager.skyThemeOf(owner);
		int radius = manager.builtRadiusFor(owner);
		BlockPos cell = manager.cellPos(manager.idFor(owner));
		ServerPlayer online = server.getPlayerList().getPlayer(owner);
		int degree = 7;
		int pomes = 0;
		if (online != null) {
			var state = HemoCapabilityAccess.getInitiatoryDegree(online);
			degree = ArborOfWillVisualRules.visualDegree(state.getDegreeNumber(), state.getArchonPath());
			pomes = ChamberOfWillManager.pomeCountFor(online);
		}
		long[] blocks = new long[0];
		if (room != null) {
			blocks = scan(room, cell, radius);
			if (online == null) {
				AABB around = new AABB(cell).inflate(3.0D, 2.0D, 3.0D);
				for (ArborOfWillEntity arbor : room.getEntitiesOfClass(ArborOfWillEntity.class, around)) {
					degree = arbor.degree();
					pomes = arbor.pomesConsumed();
				}
			}
		}
		return new ChamberProxySnapshot(theme, cell.immutable(), radius, degree, pomes, blocks, server.overworld().getGameTime());
	}

	/** Every block in the room's bounds but air and the gate, up to the cap. */
	static long[] scan(ServerLevel room, BlockPos cell, int radius) {
		List<Long> out = new ArrayList<>();
		int reach = ChamberRoomShape.reach(radius);
		int minY = Math.max(room.getMinBuildHeight(), cell.getY() - OFFSET);
		int maxY = Math.min(room.getMaxBuildHeight() - 1, cell.getY() + OFFSET - 1);
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int dx = -radius; dx <= radius; dx++) {
			for (int dz = -radius; dz <= reach; dz++) {
				room.getChunk(cell.getX() + dx >> 4, cell.getZ() + dz >> 4);
				for (int y = minY; y <= maxY; y++) {
					pos.set(cell.getX() + dx, y, cell.getZ() + dz);
					BlockState state = room.getBlockState(pos);
					if (state.isAir() || state.is(BlockInit.chamber_gate.get())) continue;
					out.add(pack(dx, y - cell.getY(), dz, Block.getId(state)));
					if (out.size() >= CAP) return toArray(out);
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

	public void write(CompoundTag tag) {
		tag.putString("Theme", theme.toString());
		tag.putLong("Cell", cell.asLong());
		tag.putInt("Radius", radius);
		tag.putInt("Degree", degree);
		tag.putInt("Pomes", pomes);
		tag.putLongArray("Blocks", blocks);
		tag.putLong("Stamp", stamp);
	}

	@Nullable
	public static ChamberProxySnapshot read(CompoundTag tag) {
		if (!tag.contains("Theme")) return null;
		return new ChamberProxySnapshot(ResourceLocation.parse(tag.getString("Theme")), BlockPos.of(tag.getLong("Cell")),
				tag.getInt("Radius"), tag.getInt("Degree"), tag.getInt("Pomes"), tag.getLongArray("Blocks"), tag.getLong("Stamp"));
	}
}
