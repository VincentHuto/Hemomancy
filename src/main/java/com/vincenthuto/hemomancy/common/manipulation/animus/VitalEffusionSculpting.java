package com.vincenthuto.hemomancy.common.manipulation.animus;

import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingPulse;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbSculpting;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTarget;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTargetKind;
import com.vincenthuto.hemomancy.common.world.farm.BloodFedGround;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

/**
 * Vital Effusion as a Sculpting of Projection (core-loop Track 7): growth on demand. Held on a plant that bone meal can
 * grow, each pulse soaks its soil (fed ground, level 1, so a gourd stem takes it) and works bone meal's effect on it,
 * through the vanilla bone-meal path and its {@code BonemealEvent}, so other mods' crops behave as with bone meal. It
 * costs blood per pulse and Animus Temper like any Sculpting. The R cast (a burst over a small area) is unchanged.
 */
public final class VitalEffusionSculpting implements VerbSculpting {
	public static final double BLOOD_PER_PULSE = 15.0D;
	public static final int CADENCE = 20;
	/** Bone-meal applications one pulse works by hand; the Staff's work scale raises it. */
	public static final int GROWTHS_PER_PULSE = 1;
	/** How long a pulse keeps the plant's soil fed. */
	public static final int SOAK_TICKS = 1_200;

	@Override
	public Set<VerbTargetKind> wants() {
		return Set.of(VerbTargetKind.BLOCK);
	}

	@Override
	public boolean claims(Player player, VerbTarget target) {
		return target instanceof VerbTarget.Block block && growable(player.level(), block.pos(), block.state());
	}

	static boolean growable(Level level, BlockPos pos, BlockState state) {
		return state.getBlock() instanceof BonemealableBlock plant && plant.isValidBonemealTarget(level, pos, state);
	}

	/** A player who may not use bone meal on the plant is refused before any blood is paid. */
	@Override
	public boolean admits(ServerPlayer player, VerbTarget target) {
		if (!(target instanceof VerbTarget.Block block)) return false;
		BlockPos pos = block.pos();
		if (player.level().mayInteract(player, pos)
				&& player.mayUseItemAt(pos, block.hit().getDirection(), new ItemStack(Items.BONE_MEAL))) {
			return true;
		}
		player.displayClientMessage(Component.translatable("message.hemomancy.player_block_edits.refused")
				.withStyle(ChatFormatting.DARK_RED), true);
		return false;
	}

	@Override
	public void pulse(ServerPlayer player, VerbTarget target, SculptingPulse pulse) {
		if (!(target instanceof VerbTarget.Block block) || !(player.level() instanceof ServerLevel level)) return;
		BlockPos pos = block.pos();
		// The blood soaks the soil first: the plant is fed, so a crop that needs blood takes the growth.
		BloodFedGround.stamp(level, BloodFedGround.soilOf(pos), SOAK_TICKS);
		int growths = Math.max(1, Mth.floor(GROWTHS_PER_PULSE * pulse.workScale() + 0.5D));
		int grew = 0;
		for (int i = 0; i < growths; i++) {
			if (!growable(level, pos, level.getBlockState(pos))) break;
			if (BoneMealItem.applyBonemeal(new ItemStack(Items.BONE_MEAL), level, pos, player)) grew++;
		}
		if (grew > 0) {
			ManipulationVisuals.burst(level, ManipulationVisuals.Form.GROWTH, Vec3.atBottomCenterOf(pos.above()),
					Vec3.atCenterOf(pos), 0.35D, 20);
			level.playSound(null, pos, SoundEvents.BONE_MEAL_USE, SoundSource.PLAYERS, 0.7F, 0.8F + level.random.nextFloat() * 0.2F);
		}
	}

	@Override
	public int cadence() {
		return CADENCE;
	}

	@Override
	public double bloodPerPulse() {
		return BLOOD_PER_PULSE;
	}
}
