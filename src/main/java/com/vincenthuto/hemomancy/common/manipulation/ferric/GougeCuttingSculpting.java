package com.vincenthuto.hemomancy.common.manipulation.ferric;

import com.vincenthuto.hemomancy.common.block.harbinger.ground.BloodGougeBlock;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingPulse;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingRules;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbSculpting;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTarget;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTargetKind;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.util.PlayerBlockEdits;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Vascular Dowsing as a Sculpting of Projection (core-loop Track 6): held along a surface, the blood cuts a gouge into
 * each face it passes over, a narrow trench in the block's own grid that stays until Absorption fills it in. A sweep
 * faster than the pulse still leaves an unbroken run: each cut joins the last one with the cells between, when they lie
 * on the same kind of face a short way apart. The run is dormant until blood is projected into it.
 *
 * <p>The last cut is a session map keyed by player UUID, cleared through {@code TransientServerStateEvents}.
 */
public final class GougeCuttingSculpting implements VerbSculpting {
	public static final double BLOOD_PER_PULSE = 3.0D;
	public static final int CADENCE = 4;
	/** A sweep is joined up across at most this many cells. */
	private static final int JOIN = 3;
	private static final ParticleColor CUT = new ParticleColor(120, 24, 28);
	private static final Map<UUID, Cut> LAST = new HashMap<>();

	private record Cut(BlockPos cell, Direction face) {
	}

	@Override
	public Set<VerbTargetKind> wants() {
		return Set.of(VerbTargetKind.BLOCK);
	}

	/** A solid face with room in front of it and no gouge on it yet. */
	@Override
	public boolean claims(Player player, VerbTarget target) {
		return target instanceof VerbTarget.Block block && cuttable(player.level(), block.pos(), block.hit().getDirection());
	}

	static boolean cuttable(Level level, BlockPos host, Direction side) {
		BlockPos cell = host.relative(side);
		Direction face = side.getOpposite();
		BlockState there = level.getBlockState(cell);
		boolean room = there.isAir() || there.getBlock() instanceof BloodGougeBlock && !MultifaceBlock.hasFace(there, face)
				|| there.is(BlockInit.blood_stain.get());
		return room && MultifaceBlock.canAttachTo(level, face, host, level.getBlockState(host));
	}

	@Override
	public void pulse(ServerPlayer player, VerbTarget target, SculptingPulse pulse) {
		if (!(target instanceof VerbTarget.Block block) || !(player.level() instanceof ServerLevel level)) return;
		Direction face = block.hit().getDirection().getOpposite();
		BlockPos cell = block.pos().relative(block.hit().getDirection());
		List<BlockPos> cells = new ArrayList<>();
		Cut last = LAST.get(player.getUUID());
		if (last != null && last.face() == face) cells.addAll(SculptingRules.sweepBetween(last.cell(), cell, JOIN));
		cells.add(cell);
		int cut = 0;
		for (BlockPos at : cells) {
			if (cut(level, player, at, face)) cut++;
		}
		LAST.put(player.getUUID(), new Cut(cell.immutable(), face));
		if (cut == 0) return;
		Vec3 point = block.point();
		level.sendParticles(HemoParticleData.bloodCell(CUT), point.x, point.y, point.z, 3, 0.1D, 0.1D, 0.1D, 0.02D);
		level.playSound(null, cell, SoundEvents.SCULK_VEIN_PLACE, SoundSource.PLAYERS, 0.35F, 1.3F);
	}

	/** Cuts a gouge on {@code face} of the cell, through the player's own block-edit rules. */
	private static boolean cut(ServerLevel level, ServerPlayer player, BlockPos cell, Direction face) {
		BlockPos host = cell.relative(face);
		if (!cuttable(level, host, face.getOpposite())) return false;
		if (!ManipulationCombatHelper.mayAlter(player, host)) {
			player.displayClientMessage(Component.translatable("message.hemomancy.player_block_edits.refused")
					.withStyle(ChatFormatting.DARK_RED), true);
			return false;
		}
		BlockState there = level.getBlockState(cell);
		BlockState gouge = (there.getBlock() instanceof BloodGougeBlock ? there : BlockInit.blood_gouge.get().defaultBlockState())
				.setValue(MultifaceBlock.getFaceProperty(face), true);
		return there.isAir() ? PlayerBlockEdits.place(player, cell, gouge) : PlayerBlockEdits.replace(player, cell, gouge);
	}

	@Override
	public void end(ServerPlayer player, boolean released) {
		if (released) LAST.remove(player.getUUID());
	}

	@Override
	public int cadence() {
		return CADENCE;
	}

	@Override
	public double bloodPerPulse() {
		return BLOOD_PER_PULSE;
	}

	public static void clearSessionState() {
		LAST.clear();
	}

	public static void logout(Player player) {
		LAST.remove(player.getUUID());
	}
}
