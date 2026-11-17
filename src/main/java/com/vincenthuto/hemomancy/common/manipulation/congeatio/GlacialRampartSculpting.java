package com.vincenthuto.hemomancy.common.manipulation.congeatio;

import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingPulse;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingRules;
import com.vincenthuto.hemomancy.common.manipulation.verb.SweepTrail;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbSculpting;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTarget;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTargetKind;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.StrandSculpting;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.Set;

/**
 * Glacial Rampart as a Sculpting of Projection (core-loop round two P4): the wall is drawn along the path the player
 * sweeps. Each pulse raises a column of Frozen Cruor three high from the cell in front of the struck face, joined to
 * the last column across up to three cells, so a quick sweep still leaves a whole wall. Nobody is ever frozen in: a cell
 * any living body stands in, the caster's included, is left open. The ice is the player's edit and melts as the R
 * cast's does, sooner when the pulse ran dry. Aimed at the wall's own ice it raises nothing new, so the wall never
 * creeps back toward the player, and keeps that stretch standing instead. The R cast (an aimed wall, or a crouched
 * bastion) is unchanged.
 */
public final class GlacialRampartSculpting implements VerbSculpting {
	public static final double BLOOD_PER_PULSE = 12.0D;
	public static final int CADENCE = 5;
	public static final double REACH_SCALE = 1.5D;
	public static final int HEIGHT = 3;
	/** A sweep is joined up across at most this many cells. */
	public static final int JOIN = 3;

	@Override
	public Set<VerbTargetKind> wants() {
		return Set.of(VerbTargetKind.BLOCK);
	}

	/** A face with room in front of it, or the wall's own ice; never a block that takes blood. */
	@Override
	public boolean claims(Player player, VerbTarget target) {
		if (!(target instanceof VerbTarget.Block block) || StrandSculpting.takesBlood(player.level(), block)) return false;
		return block.state().is(BlockInit.frozen_cruor.get()) || room(player.level(), base(block));
	}

	/** The cell in front of the struck face, where the column starts. */
	static BlockPos base(VerbTarget.Block block) {
		return block.pos().relative(block.hit().getDirection());
	}

	static boolean room(Level level, BlockPos cell) {
		var state = level.getBlockState(cell);
		return state.isAir() || state.canBeReplaced();
	}

	@Override
	public boolean admits(ServerPlayer player, VerbTarget target) {
		if (target instanceof VerbTarget.Block block && (block.state().is(BlockInit.frozen_cruor.get())
				|| ManipulationCombatHelper.mayAlter(player, base(block)))) {
			return true;
		}
		player.displayClientMessage(Component.translatable("message.hemomancy.player_block_edits.refused")
				.withStyle(ChatFormatting.DARK_RED), true);
		return false;
	}

	@Override
	public void pulse(ServerPlayer player, VerbTarget target, SculptingPulse pulse) {
		if (!(target instanceof VerbTarget.Block block) || !(player.level() instanceof ServerLevel level)) return;
		int lifetime = SculptingRules.standingTicks(GlacialRampartManip.LIFETIME_TICKS, pulse.workScale());
		if (block.state().is(BlockInit.frozen_cruor.get())) {
			keepStanding(level, block.pos(), lifetime);
			return;
		}
		int raised = 0;
		for (BlockPos cell : SweepTrail.sweep(player, this, base(block), JOIN)) {
			for (int dy = 0; dy < HEIGHT; dy++) {
				BlockPos at = cell.above(dy);
				if (!room(level, at) || occupied(level, player, at)) continue;
				if (TemporaryIceManager.placeFor(player, at, BlockInit.frozen_cruor.get().defaultBlockState(), lifetime)) raised++;
			}
		}
		if (raised > 0) {
			level.playSound(null, base(block), SoundEvents.GLASS_PLACE, SoundSource.PLAYERS, 0.8F, 0.6F);
			level.playSound(null, base(block), SoundEvents.POWDER_SNOW_BREAK, SoundSource.PLAYERS, 0.5F, 0.8F);
		}
	}

	/**
	 * Held on the wall's own ice, the stream raises nothing new (the wall never creeps back toward the player) and keeps
	 * the stretch it is held on standing: every cell of tracked ice within a column's height and a cell to each side.
	 */
	static int keepStanding(ServerLevel level, BlockPos struck, int lifetime) {
		int kept = 0;
		for (BlockPos pos : BlockPos.betweenClosed(struck.offset(-1, -(HEIGHT - 1), -1), struck.offset(1, HEIGHT - 1, 1))) {
			if (TemporaryIceManager.extend(level, pos.immutable(), lifetime)) kept++;
		}
		return kept;
	}

	/** Whether a living body stands in the cell, the caster's own included. */
	static boolean occupied(Level level, Player caster, BlockPos cell) {
		AABB box = new AABB(cell);
		return caster.getBoundingBox().intersects(box)
				|| !level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive).isEmpty();
	}

	@Override
	public void end(ServerPlayer player, boolean released) {
		if (released) SweepTrail.forget(player);
	}

	@Override
	public int cadence() {
		return CADENCE;
	}

	@Override
	public double bloodPerPulse() {
		return BLOOD_PER_PULSE;
	}

	@Override
	public double reachScale() {
		return REACH_SCALE;
	}
}
