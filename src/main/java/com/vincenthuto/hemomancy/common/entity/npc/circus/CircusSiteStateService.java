package com.vincenthuto.hemomancy.common.entity.npc.circus;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.mixin.core.VigilTemplateAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * D-44 (chunk 15 T11): a settled pavilion shows its ending. A Liberation lays the ruin over it and a Succession the
 * new tenancy, each an overlay template the Circus generator writes ({@code tools/worldgen/circus/site_states.py}) in
 * the pavilion's own coordinates. The overlay is laid one loaded chunk at a time, the chunks still to lay saved on the
 * site, and a cell is changed only while the world still holds the pavilion's own block there, so whatever a player
 * placed or broke is left as it is.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class CircusSiteStateService {
	public enum State {
		RUIN("crimson_troupe_pavilion_ruin"), TENANCY("crimson_troupe_pavilion_tenancy");

		private final String template;

		State(String template) {
			this.template = template;
		}
	}

	/** The carousel's cell in the pavilion template: the site's saved origin. */
	public static final BlockPos CAROUSEL = new BlockPos(40, 1, 32);
	/** The lower half of the Ringmaster's podium, which tells the pavilion's rotation. */
	public static final BlockPos PODIUM = new BlockPos(40, 8, 46);
	private static final String PAVILION = "crimson_troupe_pavilion";
	private static final int TICKS = 20;

	private CircusSiteStateService() {
	}

	/** Starts laying {@code state} over the pavilion whose carousel stood at {@code origin}, from the loaded chunks now. */
	public static void begin(ServerLevel level, BlockPos origin, State state, String holderName) {
		var overlay = template(level, state.template);
		if (overlay == null) return;
		Rotation rotation = rotation(level, origin);
		Set<Long> chunks = new LinkedHashSet<>();
		for (var info : blocks(overlay)) chunks.add(ChunkPos.asLong(at(origin, info.pos(), rotation)));
		CircusPavilionSavedData data = CircusPavilionSavedData.get(level);
		data.beginAfterState(level, origin, state, rotation.ordinal(), chunks, holderName);
		lay(level, data, data.site(level, origin));
	}

	@SubscribeEvent
	public static void tick(LevelTickEvent.Post event) {
		if (!(event.getLevel() instanceof ServerLevel level) || level.getGameTime() % TICKS != 0) return;
		CircusPavilionSavedData data = CircusPavilionSavedData.get(level);
		for (var site : data.sitesLayingAfterState(level)) lay(level, data, site);
	}

	/** Lays the site's overlay in every chunk still owed that is loaded now. */
	static void lay(ServerLevel level, CircusPavilionSavedData data, CircusPavilionSavedData.Site site) {
		List<Long> ready = site.pendingChunks().stream()
				.filter(chunk -> level.hasChunk(ChunkPos.getX(chunk), ChunkPos.getZ(chunk))).toList();
		if (ready.isEmpty()) return;
		var overlay = template(level, site.afterState().template);
		var pavilion = template(level, PAVILION);
		if (overlay == null || pavilion == null) return;
		Rotation rotation = Rotation.values()[site.afterRotation()];
		Map<BlockPos, BlockState> original = new HashMap<>();
		for (var info : blocks(pavilion)) original.put(info.pos(), info.state());
		for (var info : blocks(overlay)) {
			BlockPos world = at(site.origin(), info.pos(), rotation);
			if (!ready.contains(ChunkPos.asLong(world))) continue;
			BlockState expected = original.get(info.pos());
			if (expected == null || !level.getBlockState(world).is(expected.getBlock())) continue;
			level.setBlock(world, info.state().rotate(level, world, rotation), Block.UPDATE_ALL);
			var entity = level.getBlockEntity(world);
			if (info.nbt() != null && entity != null) {
				var tag = info.nbt().copy();
				if (site.holderName() != null) tag.putString("CustomName", Component.Serializer.toJson(
						Component.translatable("hemomancy.circus.tenancy.banner", site.holderName()), level.registryAccess()));
				entity.loadWithComponents(tag, level.registryAccess());
				entity.setChanged();
			}
		}
		for (long chunk : ready) data.afterChunkLaid(level, site.origin(), chunk);
	}

	/** The pavilion's rotation, read from where its podium stands relative to the carousel. */
	public static Rotation rotation(ServerLevel level, BlockPos origin) {
		for (Rotation rotation : Rotation.values())
			if (level.getBlockState(at(origin, PODIUM, rotation)).is(BlockInit.circus_podium.get())) return rotation;
		return Rotation.NONE;
	}

	/** Where a cell of the pavilion template lies in the world, the carousel's cell standing at {@code origin}. */
	static BlockPos at(BlockPos origin, BlockPos templatePos, Rotation rotation) {
		return origin.offset(templatePos.subtract(CAROUSEL).rotate(rotation));
	}

	private static StructureTemplate template(ServerLevel level, String path) {
		return level.getStructureManager().get(Hemomancy.rloc(path)).orElse(null);
	}

	private static List<StructureTemplate.StructureBlockInfo> blocks(StructureTemplate template) {
		return ((VigilTemplateAccessor) template).hemomancy$palettes().getFirst().blocks();
	}
}
