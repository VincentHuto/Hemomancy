package com.vincenthuto.hemomancy.common.manipulation.animus;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintArena;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintTrialEntity;
import com.vincenthuto.hemomancy.common.event.MausoleumEvents;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.rite.zone.RiteBound;
import com.vincenthuto.hemomancy.common.rite.zone.RiteSpaces;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/**
 * What ground Living Land may take, read from the level. A block is liftable when it is solid terrain the player could
 * break by hand rules: not air or fluid, no block entity (its contents would be lost), not half of a two-cell block,
 * not unbreakable, not in
 * {@code hemomancy:living_land_immune} or a rite-bound tag. Ground under a Saint's ward, inside a rite space or a
 * Saint's arena is refused outright, with a reason the player is told.
 */
public final class LivingLandTerrain {
	public static final TagKey<Block> IMMUNE = TagKey.create(Registries.BLOCK, Hemomancy.rloc("living_land_immune"));
	/** How far a Saint can stand from ground in its arena for the arena to be found. */
	private static final double ARENA_SEARCH = 48.0D;

	/** Why the ground at a patch is refused; the lang key is {@code message.hemomancy.living_land.refused.<id>}. */
	public enum Refusal {
		WARD("ward"), RITE_SPACE("rite_space"), ARENA("arena"), BLOCK_ENTITY("block_entity"), PROTECTED("protected");

		private final String id;

		Refusal(String id) {
			this.id = id;
		}

		public String langKey() {
			return "message.hemomancy.living_land.refused." + id;
		}
	}

	private LivingLandTerrain() {
	}

	/**
	 * Whether the block's kind can be woken at all, from its state alone. Decidable on both sides, so the claim, the
	 * HUD reticle and the hand's stream agree.
	 */
	public static boolean wakeable(BlockGetter level, BlockPos pos, BlockState state) {
		return !state.isAir() && state.getFluidState().isEmpty() && !state.hasBlockEntity()
				&& state.getDestroySpeed(level, pos) >= 0.0F && !state.is(IMMUNE) && !RiteBound.of(state).bound()
				&& !partOfAWhole(state) && !state.getCollisionShape(level, pos).isEmpty();
	}

	/**
	 * Half of a block that stands in two cells (a door, a bed) or a piston's extended pair: lifting one half breaks the
	 * other, and the half put back alone falls apart, so the whole would come back as an item or not at all.
	 */
	static boolean partOfAWhole(BlockState state) {
		return state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF) || state.hasProperty(BlockStateProperties.BED_PART)
				|| state.getBlock() instanceof PistonHeadBlock
				|| state.getBlock() instanceof PistonBaseBlock && state.getValue(PistonBaseBlock.EXTENDED);
	}

	/** Why the patch at {@code pos} is refused to {@code player}, or empty when it may wake. Server only. */
	public static Optional<Refusal> refusal(ServerPlayer player, BlockPos pos) {
		ServerLevel level = player.serverLevel();
		if (level.getBlockState(pos).hasBlockEntity()) return Optional.of(Refusal.BLOCK_ENTITY);
		Optional<Refusal> place = placeRefusal(level, pos);
		if (place.isPresent()) return place;
		if (!ManipulationCombatHelper.mayAlter(player, pos)) return Optional.of(Refusal.PROTECTED);
		return Optional.empty();
	}

	/** The ward, rite-space and arena refusals, which belong to the place, not the player. */
	public static Optional<Refusal> placeRefusal(ServerLevel level, BlockPos pos) {
		if (MausoleumEvents.hallowed(level, pos)) return Optional.of(Refusal.WARD);
		// A space covers the air over its floor; the floor itself (the block under a covered cell) is the rite's too.
		if (RiteSpaces.spaceAt(level, pos).isPresent() || RiteSpaces.spaceAt(level, pos.above()).isPresent()) {
			return Optional.of(Refusal.RITE_SPACE);
		}
		if (inSaintArena(level, pos)) return Optional.of(Refusal.ARENA);
		return Optional.empty();
	}

	/** Whether one cell of a column may be lifted for {@code player}: the kind, the place, and its loaded chunk. */
	public static boolean liftable(ServerPlayer player, BlockPos pos) {
		ServerLevel level = player.serverLevel();
		if (!level.isLoaded(pos) || level.isOutsideBuildHeight(pos)) return false;
		BlockState state = level.getBlockState(pos);
		return wakeable(level, pos, state) && placeRefusal(level, pos).isEmpty();
	}

	/** Whether a strike or a raised block can come out into {@code pos}: loaded, and nothing solid stands there. */
	public static boolean open(ServerLevel level, BlockPos pos) {
		return level.isLoaded(pos) && !level.isOutsideBuildHeight(pos) && level.getBlockState(pos).canBeReplaced();
	}

	private static boolean inSaintArena(ServerLevel level, BlockPos pos) {
		Vec3 point = Vec3.atCenterOf(pos);
		for (SaintTrialEntity saint : level.getEntitiesOfClass(SaintTrialEntity.class, new AABB(pos).inflate(ARENA_SEARCH))) {
			SaintArena arena = saint.arena();
			if (arena != null && arena.chamberFound() && arena.contains(point)) return true;
		}
		return false;
	}
}
