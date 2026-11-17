package com.vincenthuto.hemomancy.common.manipulation.congeatio;

import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingPulse;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbSculpting;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTarget;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTargetKind;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.StrandSculpting;
import com.vincenthuto.hemomancy.common.util.PlayerBlockEdits;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Glacial Grasp as a Sculpting of Absorption (core-loop round two P4): held on ice, it draws the cold back out. Frozen
 * Cruor melts at once to what it froze (water, or air), and plain or frosted ice turns to water, a few cells a pulse,
 * nearest first, each as the player's edit. Packed and blue ice are beyond it.
 */
public final class GlacialThawSculpting implements VerbSculpting {
	public static final double BLOOD_PER_PULSE = 2.0D;
	public static final int CADENCE = 10;
	public static final double REACH_SCALE = 1.5D;
	public static final int RADIUS = 1;
	/** Cells one pulse thaws by hand. */
	public static final int PER_PULSE = 3;

	@Override
	public Set<VerbTargetKind> wants() {
		return Set.of(VerbTargetKind.BLOCK);
	}

	@Override
	public boolean claims(Player player, VerbTarget target) {
		return target instanceof VerbTarget.Block block && thawable(block.state()) && !StrandSculpting.takesBlood(player.level(), block);
	}

	static boolean thawable(BlockState state) {
		return state.is(BlockInit.frozen_cruor.get()) || state.is(Blocks.ICE) || state.is(Blocks.FROSTED_ICE);
	}

	@Override
	public boolean admits(ServerPlayer player, VerbTarget target) {
		if (target instanceof VerbTarget.Block block && ManipulationCombatHelper.mayAlter(player, block.pos())) return true;
		player.displayClientMessage(Component.translatable("message.hemomancy.player_block_edits.refused")
				.withStyle(ChatFormatting.DARK_RED), true);
		return false;
	}

	@Override
	public void pulse(ServerPlayer player, VerbTarget target, SculptingPulse pulse) {
		if (!(target instanceof VerbTarget.Block block) || !(player.level() instanceof ServerLevel level)) return;
		int budget = Math.max(1, Mth.ceil(PER_PULSE * pulse.workScale()));
		int thawed = 0;
		for (BlockPos pos : ice(level, block.pos())) {
			if (thawed >= budget) break;
			if (thaw(level, player, pos)) thawed++;
		}
		if (thawed > 0) level.playSound(null, block.pos(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.4F, 1.5F);
	}

	private static boolean thaw(ServerLevel level, ServerPlayer player, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		if (state.is(BlockInit.frozen_cruor.get())) return TemporaryIceManager.thaw(player, pos);
		// Vanilla ice melts as it would beside a torch: to water, or to nothing where water cannot stand.
		boolean done = level.dimensionType().ultraWarm() ? PlayerBlockEdits.lift(player, pos) != null
				: PlayerBlockEdits.replace(player, pos, Blocks.WATER.defaultBlockState());
		if (done) {
			ManipulationVisuals.burst(level, ManipulationVisuals.Form.CRUOR_BREAK, Vec3.atBottomCenterOf(pos), Vec3.atCenterOf(pos), 0.45D, 8);
		}
		return done;
	}

	/** Thawable ice within {@link #RADIUS} of {@code around}, nearest first. */
	static List<BlockPos> ice(Level level, BlockPos around) {
		List<BlockPos> found = new ArrayList<>();
		for (BlockPos pos : BlockPos.betweenClosed(around.offset(-RADIUS, -RADIUS, -RADIUS), around.offset(RADIUS, RADIUS, RADIUS))) {
			if (thawable(level.getBlockState(pos))) found.add(pos.immutable());
		}
		found.sort(Comparator.comparingDouble(pos -> pos.distSqr(around)));
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
