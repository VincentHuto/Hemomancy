package com.vincenthuto.hemomancy.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.LockSupport;

/**
 * Holds open the chunks of a fixture that builds outside its test's own template. GameTest force-loads only the
 * template's chunks, so an entity placed beyond them is invisible to entity lookups unless some neighbouring test happens
 * to hold that chunk. Which neighbour that is depends on batch order, so such a test passes or fails with the rest of
 * the suite. On the GameTest server this forces the chunks and returns once entities in them are live; like the
 * framework's own forcing, it lasts the rest of the run. Elsewhere it does nothing, since a real player holds the chunks.
 */
public final class FixtureChunks {
	private static final long WAIT_NANOS = 4_000_000_000L;

	private FixtureChunks() {
	}

	public static void hold(ServerLevel level, AABB box) {
		if (!(level.getServer() instanceof GameTestServer)) return;
		List<BlockPos> pending = new ArrayList<>();
		ChunkPos from = new ChunkPos(BlockPos.containing(box.minX, box.minY, box.minZ));
		ChunkPos to = new ChunkPos(BlockPos.containing(box.maxX, box.minY, box.maxZ));
		for (int x = from.x; x <= to.x; x++) {
			for (int z = from.z; z <= to.z; z++) {
				level.setChunkForced(x, z, true);
				level.getChunk(x, z);
				pending.add(new ChunkPos(x, z).getMiddleBlockPosition(level.getMinBuildHeight() + 1));
			}
		}
		long deadline = System.nanoTime() + WAIT_NANOS;
		while (System.nanoTime() < deadline) {
			pending.removeIf(level::isPositionEntityTicking);
			if (pending.isEmpty()) return;
			if (!level.getChunkSource().pollTask()) LockSupport.parkNanos(1_000_000L);
		}
		throw new IllegalStateException("Fixture chunks never went live: " + pending);
	}
}
