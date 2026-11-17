package com.vincenthuto.hemomancy.common.manipulation.flammeus;

import com.vincenthuto.hemomancy.common.block.harbinger.rite.BrazierBlock;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingPulse;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbSculpting;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTarget;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTargetKind;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.StrandSculpting;
import com.vincenthuto.hemomancy.common.util.PlayerBlockEdits;
import com.vincenthuto.hemomancy.common.world.ground.SwitchableLights;
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
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Crimson Flame Conjuration as a Sculpting of Projection (core-loop round two P4), the mirror of Blood Eclipse's snuff:
 * held near unlit campfires, candles and candle cakes, it kindles them, nearest first, a couple a pulse, each as the
 * player's edit. Held on TNT itself it primes it, with the player as the one who lit it. It leaves Iron Braziers to
 * plain Projection (their ignition is the brazier's own lifecycle), furnaces to Furnace Veins, and anything that takes
 * blood alone. The R cast (crimson flames on the aimed block) is unchanged.
 */
public final class FlameKindleSculpting implements VerbSculpting {
	public static final double BLOOD_PER_PULSE = 6.0D;
	public static final int CADENCE = 10;
	public static final double REACH_SCALE = 1.5D;
	/** Lights it finds around the point it is aimed at. */
	public static final int RADIUS = 2;
	/** Lights one pulse kindles by hand. */
	public static final int PER_PULSE = 2;

	@Override
	public Set<VerbTargetKind> wants() {
		return Set.of(VerbTargetKind.BLOCK);
	}

	@Override
	public boolean claims(Player player, VerbTarget target) {
		if (!(target instanceof VerbTarget.Block block)) return false;
		BlockState state = block.state();
		if (state.getBlock() instanceof AbstractFurnaceBlock || state.getBlock() instanceof BrazierBlock
				|| StrandSculpting.takesBlood(player.level(), block)) return false;
		return state.is(Blocks.TNT) || !kindlable(player.level(), block.pos(), 1).isEmpty();
	}

	/** Up to {@code wanted} unlit lights within {@link #RADIUS} of {@code around}, nearest first. */
	static List<BlockPos> kindlable(Level level, BlockPos around, int wanted) {
		List<BlockPos> found = new ArrayList<>();
		for (BlockPos pos : BlockPos.betweenClosed(around.offset(-RADIUS, -RADIUS, -RADIUS), around.offset(RADIUS, RADIUS, RADIUS))) {
			if (SwitchableLights.kindled(level.getBlockState(pos)).isPresent()) found.add(pos.immutable());
		}
		found.sort(Comparator.comparingDouble(pos -> pos.distSqr(around)));
		return found.size() > wanted ? found.subList(0, wanted) : found;
	}

	/** A player who may not alter the struck block is refused before any blood is paid. */
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
		BlockPos struck = block.pos();
		if (level.getBlockState(struck).is(Blocks.TNT)) {
			prime(level, player, block);
			return;
		}
		int budget = Math.max(1, Mth.ceil(PER_PULSE * pulse.workScale()));
		int kindled = 0;
		for (BlockPos pos : kindlable(level, struck, budget)) {
			BlockState lit = SwitchableLights.kindled(level.getBlockState(pos)).orElse(null);
			if (lit == null || !PlayerBlockEdits.replace(player, pos, lit)) continue;
			kindled++;
			ManipulationVisuals.burst(level, ManipulationVisuals.Form.FLAME_CONJURE, Vec3.atBottomCenterOf(pos), Vec3.atCenterOf(pos),
					0.4D, 18);
		}
		if (kindled > 0) level.playSound(null, struck, SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.45F, 1.4F);
	}

	/** TNT takes fire from the blood with the player as its igniter, as flint and steel would light it. */
	private static void prime(ServerLevel level, ServerPlayer player, VerbTarget.Block block) {
		BlockPos pos = block.pos();
		BlockState tnt = PlayerBlockEdits.lift(player, pos);
		if (tnt == null) {
			player.displayClientMessage(Component.translatable("message.hemomancy.player_block_edits.refused")
					.withStyle(ChatFormatting.DARK_RED), true);
			return;
		}
		tnt.onCaughtFire(level, pos, block.hit().getDirection(), player);
		ManipulationVisuals.burst(level, ManipulationVisuals.Form.IGNITION, Vec3.atBottomCenterOf(pos), Vec3.atCenterOf(pos), 0.6D, 16);
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
