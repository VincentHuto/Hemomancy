package com.vincenthuto.hemomancy.common.worldgen;

import java.util.ArrayList;
import java.util.List;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hutoslib.common.lightning.LightningTestConfig;
import com.vincenthuto.hutoslib.common.lightning.LightningTesterSpawner;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * Ambient nerve lightning for the Cortical Drift: the biome's synaptic nodes arc at each other
 * so the fiber bridges read as one living nervous system rather than static scenery.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class CorticalArcManager {
	/**
	 * The Drift deliberately tints the *same* effect Ferric Ductilis casts use rather than being a
	 * different effect: {@code DuctilisLightningEffects} drives this exact bolt with a yellow outer
	 * (0xE8FFE84A) over a white inner, and here the outer is shifted violet over a pale lilac inner.
	 * Keep the two in step - if the manipulation bolt's shape changes, this ambience should follow.
	 */
	private static final int OUTER_VIOLET = 0xE8C77BFF;
	private static final int INNER_PALE = 0xFFF2E6FF;

	private static final int MAX_ARCS_PER_TICK = 3;
	private static final int MAX_NODES = 24;

	private static final LightningTestConfig NERVE_ARC_CONFIG = new LightningTestConfig(
			LightningTestConfig.Backend.BOLT, OUTER_VIOLET, OUTER_VIOLET, INNER_PALE, 64.0F, 0.0F, 0.0F, 0.0F, 62.0F,
			2.1F, 8, 7, 0.08F, 0.012F, false, 0L, false, 20);

	private CorticalArcManager() {
	}

	@SubscribeEvent
	public static void onTick(LevelTickEvent.Post event) {
		if (!(event.getLevel() instanceof ServerLevel level)) {
			return;
		}

		long time = level.getGameTime();
		boolean inEnd = level.dimension() == Level.END;
		for (ServerPlayer player : level.players()) {
			// Stagger by entity id so a full server never resolves every player's scan on one tick.
			if (!CorticalArcActivity.shouldScan(inEnd, player.isSpectator(), time, player.getId())) {
				continue;
			}
			arc(level, player);
		}
	}

	private static void arc(ServerLevel level, ServerPlayer player) {
		List<BlockPos> nodes = collectNodes(level, player.blockPosition());
		if (nodes.size() < 2) {
			return;
		}

		// Tracks nodes already used as an endpoint this pass, so a mutual-nearest pair (i's nearest is
		// j, and j's nearest is i) is arced once instead of twice - once forward, once reversed.
		boolean[] consumed = new boolean[nodes.size()];
		int fired = 0;
		for (int i = 0; i < nodes.size() && fired < MAX_ARCS_PER_TICK; i++) {
			if (consumed[i]) {
				continue;
			}
			int j = CorticalArcActivity.nearest(nodes, i, consumed);
			if (j < 0) {
				continue;
			}
			consumed[i] = true;
			consumed[j] = true;

			Vec3 start = Vec3.atCenterOf(nodes.get(i));
			Vec3 end = Vec3.atCenterOf(nodes.get(j));
			LightningTesterSpawner.spawn(level, start, end, NERVE_ARC_CONFIG);
			fired++;
		}
	}

	/**
	 * Walks only the loaded chunks overlapping the scan cube, and within them only the chunk
	 * sections whose palette actually contains a synaptic node. That keeps the common case - an End
	 * player with no cortical network nearby - at a handful of palette lookups instead of walking
	 * every block in the search cube, and it never forces a chunk to load.
	 */
	private static List<BlockPos> collectNodes(ServerLevel level, BlockPos center) {
		List<BlockPos> nodes = new ArrayList<>(MAX_NODES);
		Block target = BlockInit.synaptic_node.get();
		CorticalArcActivity.ScanBounds bounds = CorticalArcActivity.scanBounds(center,
				level.getMinBuildHeight(), level.getMaxBuildHeight());
		int minX = bounds.minX(), maxX = bounds.maxX();
		int minY = bounds.minY(), maxY = bounds.maxY();
		int minZ = bounds.minZ(), maxZ = bounds.maxZ();
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

		for (int chunkX = minX >> 4; chunkX <= (maxX >> 4); chunkX++) {
			for (int chunkZ = minZ >> 4; chunkZ <= (maxZ >> 4); chunkZ++) {
				if (!level.hasChunk(chunkX, chunkZ)) {
					continue;
				}
				LevelChunk chunk = level.getChunk(chunkX, chunkZ);
				LevelChunkSection[] sections = chunk.getSections();
				for (int index = 0; index < sections.length; index++) {
					int sectionBottom = (chunk.getMinSection() + index) << 4;
					if (sectionBottom + 15 < minY || sectionBottom > maxY) {
						continue;
					}
					LevelChunkSection section = sections[index];
					// Palette-level reject: skips the whole 4096-block section in one call.
					if (section.hasOnlyAir() || !section.maybeHas(state -> state.is(target))) {
						continue;
					}
					if (scanSection(section, sectionBottom, chunkX << 4, chunkZ << 4, target, minX, maxX, minY, maxY,
							minZ, maxZ, cursor, nodes)) {
						return nodes;
					}
				}
			}
		}
		return nodes;
	}

	/** Returns true once the node budget is full. */
	private static boolean scanSection(LevelChunkSection section, int sectionBottom, int originX, int originZ,
			Block target, int minX, int maxX, int minY, int maxY, int minZ, int maxZ, BlockPos.MutableBlockPos cursor,
			List<BlockPos> nodes) {
		for (int y = 0; y < 16; y++) {
			int worldY = sectionBottom + y;
			if (worldY < minY || worldY > maxY) {
				continue;
			}
			for (int x = 0; x < 16; x++) {
				int worldX = originX + x;
				if (worldX < minX || worldX > maxX) {
					continue;
				}
				for (int z = 0; z < 16; z++) {
					int worldZ = originZ + z;
					if (worldZ < minZ || worldZ > maxZ) {
						continue;
					}
					if (!section.getBlockState(x, y, z).is(target)) {
						continue;
					}
					nodes.add(cursor.set(worldX, worldY, worldZ).immutable());
					if (nodes.size() >= MAX_NODES) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
