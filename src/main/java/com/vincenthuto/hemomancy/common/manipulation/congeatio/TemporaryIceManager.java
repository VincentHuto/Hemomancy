package com.vincenthuto.hemomancy.common.manipulation.congeatio;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import com.vincenthuto.hemomancy.common.util.PlayerBlockEdits;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Glacial casts raise Frozen Cruor that melts after its lifetime. What is waiting to melt is saved with the level
 * ({@link TemporaryIceSavedData}), so a restart never leaves the ice standing. Ice in an unloaded chunk waits there and
 * melts on the first tick its chunk is loaded again.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class TemporaryIceManager {
	/** Owners whose arena ice was already swept this session (Vesper's reload fallback runs once per owner). */
	private static final Set<UUID> RECOVERY_CLEARED = new HashSet<>();
	private static final int SURFACE_INTERVAL_TICKS = 40;

	private TemporaryIceManager() {
	}

	public static void clearSessionState() {
		RECOVERY_CLEARED.clear();
	}

	public static boolean place(ServerLevel level, BlockPos pos, BlockState state, int lifetimeTicks) {
		return placeOwned(level, pos, state, lifetimeTicks, null);
	}

	/**
	 * Freezes {@code pos} (air, a replaceable plant, or a water source) for {@code lifetimeTicks}. Whoever laid it, a
	 * frozen water source melts back to water (CQ-19, NQ-05-13), as {@link #placeFor} does.
	 */
	public static boolean placeOwned(ServerLevel level, BlockPos pos, BlockState state, int lifetimeTicks,
			UUID owner) {
		BlockState current = level.getBlockState(pos);
		if (!current.isAir() && !current.canBeReplaced()) {
			return false;
		}
		boolean water = current.is(Blocks.WATER) && current.getFluidState().isSource();
		level.setBlock(pos, state, 3);
		ManipulationVisuals.burst(level, ManipulationVisuals.Form.CRUOR_FORM, Vec3.atBottomCenterOf(pos),
				Vec3.atCenterOf(pos), .45, 12);
		surface(level, pos, lifetimeTicks);
		TemporaryIceSavedData.get(level).put(pos, level.getGameTime() + lifetimeTicks, owner, water);
		if (owner != null) RECOVERY_CLEARED.remove(owner);
		return true;
	}

	/**
	 * A player's Sculpting freezes {@code pos} (air, a replaceable plant, or a water source) for {@code lifetimeTicks},
	 * as that player's block edit ({@link PlayerBlockEdits#place}: game mode, spawn protection, the place event). A
	 * frozen water source comes back as water when the ice melts or is thawed. False when the cell is not free or the
	 * edit is refused.
	 */
	public static boolean placeFor(ServerPlayer player, BlockPos pos, BlockState state, int lifetimeTicks) {
		ServerLevel level = player.serverLevel();
		BlockState current = level.getBlockState(pos);
		if (!current.isAir() && !current.canBeReplaced()) return false;
		boolean water = current.is(Blocks.WATER) && current.getFluidState().isSource();
		if (!PlayerBlockEdits.place(player, pos, state)) return false;
		ManipulationVisuals.burst(level, ManipulationVisuals.Form.CRUOR_FORM, Vec3.atBottomCenterOf(pos),
				Vec3.atCenterOf(pos), .45, 12);
		surface(level, pos, lifetimeTicks);
		TemporaryIceSavedData.get(level).put(pos, level.getGameTime() + lifetimeTicks, player.getUUID(), water);
		RECOVERY_CLEARED.remove(player.getUUID());
		return true;
	}

	/** Keeps tracked ice at {@code pos} standing at least {@code lifetimeTicks} more; false when none is tracked there. */
	public static boolean extend(ServerLevel level, BlockPos pos, int lifetimeTicks) {
		TemporaryIceSavedData data = TemporaryIceSavedData.get(level);
		TemporaryIceSavedData.Entry entry = data.get(pos);
		if (entry == null || !level.getBlockState(pos).is(BlockInit.frozen_cruor.get())) return false;
		long meltsAt = Math.max(entry.meltsAt(), level.getGameTime() + lifetimeTicks);
		if (meltsAt != entry.meltsAt()) data.put(pos, meltsAt, entry.owner(), entry.water());
		return true;
	}

	/**
	 * A player thaws the Frozen Cruor at {@code pos} before its time, as their block edit: it melts to what it froze
	 * (water, or air), exactly as it would have at expiry. False when there is no cruor there or the edit is refused.
	 */
	public static boolean thaw(ServerPlayer player, BlockPos pos) {
		ServerLevel level = player.serverLevel();
		if (!level.getBlockState(pos).is(BlockInit.frozen_cruor.get())) return false;
		TemporaryIceSavedData data = TemporaryIceSavedData.get(level);
		BlockState melted = melted(data.get(pos));
		boolean done = melted.isAir() ? PlayerBlockEdits.lift(player, pos) != null : PlayerBlockEdits.replace(player, pos, melted);
		if (!done) return false;
		data.remove(pos);
		ManipulationVisuals.burst(level, ManipulationVisuals.Form.CRUOR_BREAK, Vec3.atBottomCenterOf(pos), Vec3.atCenterOf(pos), .45, 8);
		return true;
	}

	/** What a block of ice leaves when it melts: the water it froze, or air. */
	static BlockState melted(TemporaryIceSavedData.Entry entry) {
		return entry != null && entry.water() ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
	}

	public static void clearOwned(ServerLevel level, AABB bounds, UUID owner) {
		TemporaryIceSavedData data = TemporaryIceSavedData.get(level);
		for (Map.Entry<BlockPos, TemporaryIceSavedData.Entry> entry : List.copyOf(data.entries().entrySet())) {
			BlockPos pos = entry.getKey();
			if (!owner.equals(entry.getValue().owner())
					|| !bounds.contains(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D)) continue;
			if (level.getBlockState(pos).is(BlockInit.frozen_cruor.get())) {
				level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
			}
			data.remove(pos);
		}
		RECOVERY_CLEARED.add(owner);
	}

	public static void clearEncounterOwned(ServerLevel level, AABB bounds, UUID owner, boolean allowReloadFallback) {
		boolean hasTrackedIce = TemporaryIceSavedData.get(level).hasIceOwnedBy(owner);
		clearOwned(level, bounds, owner);
		if (hasTrackedIce || !allowReloadFallback || !RECOVERY_CLEARED.add(owner)) return;
		BlockPos min = BlockPos.containing(bounds.minX, bounds.minY, bounds.minZ);
		BlockPos max = BlockPos.containing(bounds.maxX, bounds.maxY, bounds.maxZ);
		for (BlockPos cursor : BlockPos.betweenClosed(min, max)) {
			if (level.getBlockState(cursor).is(BlockInit.frozen_cruor.get())) {
				level.setBlock(cursor, Blocks.AIR.defaultBlockState(), 3);
			}
		}
	}

	@SubscribeEvent
	public static void onLevelTick(LevelTickEvent.Post event) {
		if (!(event.getLevel() instanceof ServerLevel level)) {
			return;
		}
		TemporaryIceSavedData data = TemporaryIceSavedData.get(level);
		if (data.isEmpty()) {
			return;
		}
		long now = level.getGameTime();
		for (Map.Entry<BlockPos, TemporaryIceSavedData.Entry> entry : List.copyOf(data.entries().entrySet())) {
			BlockPos pos = entry.getKey();
			if (!level.isLoaded(pos)) continue;
			long meltsAt = entry.getValue().meltsAt();
			boolean ice = level.getBlockState(pos).is(BlockInit.frozen_cruor.get());
			if (now < meltsAt) {
				if (now % SURFACE_INTERVAL_TICKS == 0 && ice) surface(level, pos, (int) (meltsAt - now));
				continue;
			}
			if (ice) {
				ManipulationVisuals.burst(level, ManipulationVisuals.Form.CRUOR_BREAK, Vec3.atBottomCenterOf(pos),
						Vec3.atCenterOf(pos), .45, 8);
				level.setBlock(pos, melted(entry.getValue()), 3);
			}
			data.remove(pos);
		}
	}

	private static void surface(ServerLevel level, BlockPos pos, int ticks) {
		Vec3 at = Vec3.atBottomCenterOf(pos);
		ManipulationVisuals.burst(level, ManipulationVisuals.Form.CRUOR_SURFACE, at, at, .5, ticks);
	}
}
