package com.vincenthuto.hemomancy.common.manipulation.congeatio;

import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingPulse;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbSculpting;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTarget;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTargetKind;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Glacial Grasp's narrow Sculpting of Projection (core-loop round two P4), tried ahead of its plant strand: held on open
 * water, the stream freezes the struck source and the water round it into Frozen Cruor, a few cells a pulse, nearest
 * first. Held on that ice while open water is still beside it, it keeps it frozen and goes on freezing that water, so a
 * held sweep walks a path across a pond. The ice is the player's edit and melts back to water ten seconds after the stream leaves it.
 * Anywhere else (ground, a wall) it does not claim and the plant strand opens as before. The R cast is unchanged.
 */
public final class GlacialFreezeSculpting implements VerbSculpting {
	public static final double BLOOD_PER_PULSE = 5.0D;
	public static final int CADENCE = 10;
	public static final double REACH_SCALE = 1.5D;
	/** The disc of water round the aim that one pulse can reach. */
	public static final int RADIUS = 1;
	/** Cells one pulse freezes by hand. */
	public static final int PER_PULSE = 5;
	/** How long the ice stands after the stream last touched it. */
	public static final int LIFETIME_TICKS = 200;

	@Override
	public Set<VerbTargetKind> wants() {
		return Set.of(VerbTargetKind.FLUID, VerbTargetKind.BLOCK);
	}

	/**
	 * An open water source (air above it), or Frozen Cruor with open water still beside it to freeze (a path being
	 * walked across a pond). Cruor with no water round it is left to the plant, so the plant still sinks into ice.
	 */
	@Override
	public boolean claims(Player player, VerbTarget target) {
		return switch (target) {
			case VerbTarget.Fluid fluid -> freezable(player.level(), fluid.pos());
			case VerbTarget.Block block -> block.state().is(BlockInit.frozen_cruor.get()) && !water(player.level(), block.pos()).isEmpty();
			default -> false;
		};
	}

	/** A plain water source block with air above it: the R cast's rule. Never a waterlogged block. */
	static boolean freezable(Level level, BlockPos pos) {
		var state = level.getBlockState(pos);
		return state.is(Blocks.WATER) && state.getFluidState().isSource() && level.getBlockState(pos.above()).isAir();
	}

	@Override
	public boolean admits(ServerPlayer player, VerbTarget target) {
		BlockPos pos = target instanceof VerbTarget.Fluid fluid ? fluid.pos()
				: target instanceof VerbTarget.Block block ? block.pos() : null;
		if (pos != null && ManipulationCombatHelper.mayAlter(player, pos)) return true;
		player.displayClientMessage(Component.translatable("message.hemomancy.player_block_edits.refused")
				.withStyle(ChatFormatting.DARK_RED), true);
		return false;
	}

	@Override
	public void pulse(ServerPlayer player, VerbTarget target, SculptingPulse pulse) {
		if (!(player.level() instanceof ServerLevel level)) return;
		BlockPos center = target instanceof VerbTarget.Fluid fluid ? fluid.pos()
				: target instanceof VerbTarget.Block block ? block.pos() : null;
		if (center == null) return;
		// The ice the stream already holds stays frozen while it is held.
		for (BlockPos pos : BlockPos.betweenClosed(center.offset(-RADIUS, 0, -RADIUS), center.offset(RADIUS, 0, RADIUS))) {
			TemporaryIceManager.extend(level, pos.immutable(), LIFETIME_TICKS);
		}
		int budget = Math.max(1, Mth.ceil(PER_PULSE * pulse.workScale()));
		int frozen = 0;
		for (BlockPos pos : water(level, center)) {
			if (frozen >= budget) break;
			if (TemporaryIceManager.placeFor(player, pos, BlockInit.frozen_cruor.get().defaultBlockState(), LIFETIME_TICKS)) {
				frozen++;
				ManipulationVisuals.burst(level, ManipulationVisuals.Form.FROST_ADVANCE, Vec3.atBottomCenterOf(pos.above()),
						player.position(), 0.45D, 30);
			}
		}
		if (frozen > 0) level.playSound(null, center, SoundEvents.GLASS_PLACE, SoundSource.PLAYERS, 0.6F, 1.4F);
	}

	/** The open water sources in the disc round {@code center} at its level, nearest first. */
	static List<BlockPos> water(Level level, BlockPos center) {
		List<BlockPos> found = new ArrayList<>();
		for (BlockPos pos : BlockPos.betweenClosed(center.offset(-RADIUS, 0, -RADIUS), center.offset(RADIUS, 0, RADIUS))) {
			if (freezable(level, pos)) found.add(pos.immutable());
		}
		found.sort(Comparator.comparingDouble(pos -> pos.distSqr(center)));
		return found;
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
