package com.vincenthuto.hemomancy.common.manipulation.flammeus;

import com.vincenthuto.hemomancy.common.manipulation.BloodFlowVisuals;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingPulse;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbSculpting;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTarget;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTargetKind;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.StrandSculpting;
import com.vincenthuto.hemomancy.common.network.particle.BloodFlowPacket;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.world.ground.FurnaceHeat;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Furnace Veins as a Sculpting of Projection (core-loop round two P4): blood as furnace fuel for exactly as long as it is
 * held. Held on a furnace, smoker or blast furnace, each pulse keeps it and every furnace within {@link #BANK_RADIUS}
 * that has something to cook burning for one more pulse and a little over, through the furnace's own burn time
 * ({@link FurnaceHeat#stoke}, which a hot gouge run shares); the furnace's own tick does the cooking. Nothing is banked:
 * let go and the fire dies within a second, and vanilla unwinds the cook progress as it does when coal runs out. A longer burn already in a furnace is never raised. A bank
 * with nothing to cook is refused before any blood is paid. The R cast (heat round the caster) is unchanged.
 */
public final class FurnaceVeinsSculpting implements VerbSculpting {
	public static final double BLOOD_PER_PULSE = 6.0D;
	public static final int CADENCE = 10;
	/** Furnaces this far from the struck one (in each axis) burn on the same stream. */
	public static final int BANK_RADIUS = 2;
	/** At most this many furnaces burn on one stream, nearest first. */
	public static final int BANK_MAX = 8;
	/** Burn time a pulse leaves past the next pulse, so a held stream never flickers. */
	public static final int BURN_MARGIN = 5;
	private static final ParticleColor EMBER = new ParticleColor(255, 110, 40);

	@Override
	public Set<VerbTargetKind> wants() {
		return Set.of(VerbTargetKind.BLOCK);
	}

	/** Decided by the block alone: the client cannot see what a furnace holds. */
	@Override
	public boolean claims(Player player, VerbTarget target) {
		return target instanceof VerbTarget.Block block && block.state().getBlock() instanceof AbstractFurnaceBlock
				&& !StrandSculpting.takesBlood(player.level(), block);
	}

	/**
	 * Refused, unpaid, where the player may not use the furnace, or when no furnace in its bank has anything it can
	 * cook into its output.
	 */
	@Override
	public boolean admits(ServerPlayer player, VerbTarget target) {
		if (!(target instanceof VerbTarget.Block block)) return false;
		if (!player.level().mayInteract(player, block.pos())) {
			player.displayClientMessage(Component.translatable("message.hemomancy.player_block_edits.refused")
					.withStyle(ChatFormatting.DARK_RED), true);
			return false;
		}
		if (!bank(player.level(), block.pos()).isEmpty()) return true;
		player.displayClientMessage(Component.translatable("message.hemomancy.furnace_veins.nothing_to_burn")
				.withStyle(ChatFormatting.GOLD), true);
		return false;
	}

	/** The furnaces within the bank of {@code struck}, the struck one included, that have something to cook. */
	static List<BlockPos> bank(Level level, BlockPos struck) {
		List<BlockPos> found = new ArrayList<>();
		for (BlockPos pos : BlockPos.betweenClosed(struck.offset(-BANK_RADIUS, -BANK_RADIUS, -BANK_RADIUS),
				struck.offset(BANK_RADIUS, BANK_RADIUS, BANK_RADIUS))) {
			if (level.getBlockEntity(pos) instanceof AbstractFurnaceBlockEntity furnace && cooks(level, furnace)) found.add(pos.immutable());
		}
		found.sort(Comparator.comparingDouble(pos -> pos.distSqr(struck)));
		return found.size() > BANK_MAX ? found.subList(0, BANK_MAX) : found;
	}

	/** Whether {@code furnace} has an input its recipe type can cook and room for the result in its output. */
	static boolean cooks(Level level, AbstractFurnaceBlockEntity furnace) {
		return FurnaceHeat.cooks(level, furnace);
	}

	@Override
	public void pulse(ServerPlayer player, VerbTarget target, SculptingPulse pulse) {
		if (!(target instanceof VerbTarget.Block block) || !(player.level() instanceof ServerLevel level)) return;
		int burn = burnTicks(pulse.workScale());
		int fed = 0;
		for (BlockPos pos : bank(level, block.pos())) {
			if (!FurnaceHeat.stoke(player, level, pos, burn)) continue;
			fed++;
			Vec3 mouth = Vec3.atBottomCenterOf(pos.above());
			ManipulationVisuals.burst(level, ManipulationVisuals.Form.FURNACE, mouth, Vec3.atCenterOf(pos), 0.5D, 14);
			level.sendParticles(HemoParticleData.ember(EMBER, 0.12F, 0.8F, 16), mouth.x, mouth.y - 0.4D, mouth.z, 4,
					0.3D, 0.2D, 0.3D, 0.01D);
			if (!pos.equals(block.pos())) BloodFlowVisuals.toward(player, Vec3.atCenterOf(pos), pos.asLong() ^ level.getGameTime(),
					BloodFlowPacket.Style.LIVING);
		}
		if (fed > 0) level.playSound(null, block.pos(), SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 0.8F, 0.9F);
	}

	/**
	 * The burn one pulse leaves: a pulse and a little over at full strength. A dry pulse leaves less, so the fire gutters
	 * between pulses and the cook goes slower; the Staff never makes a furnace cook faster than coal would.
	 */
	public static int burnTicks(double workScale) {
		return Math.max(1, (int) Math.round((CADENCE + BURN_MARGIN) * Math.min(1.0D, Math.max(0.0D, workScale))));
	}

	@Override
	public int cadence() {
		return CADENCE;
	}

	/** The price of one furnace's pulse: what the client's readout shows, since it cannot see what a furnace holds. */
	@Override
	public double bloodPerPulse() {
		return BLOOD_PER_PULSE;
	}

	/** Priced per furnace fed (CQ-15, NQ-02-9): {@link #BLOOD_PER_PULSE} for each furnace in the bank with something to cook. */
	@Override
	public double bloodPerPulse(ServerPlayer player, VerbTarget target) {
		if (!(target instanceof VerbTarget.Block block)) return BLOOD_PER_PULSE;
		return pulsePrice(bank(player.level(), block.pos()).size());
	}

	/** The blood a pulse feeding {@code furnaces} furnaces costs. */
	public static double pulsePrice(int furnaces) {
		return BLOOD_PER_PULSE * Math.max(1, furnaces);
	}
}
