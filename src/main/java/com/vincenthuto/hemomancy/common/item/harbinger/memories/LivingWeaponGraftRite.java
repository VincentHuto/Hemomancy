package com.vincenthuto.hemomancy.common.item.harbinger.memories;

import com.vincenthuto.hemomancy.common.block.harbinger.BrazierBlock;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationGrantHelper.MemoryGrantResult;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationGrantHelper.MemoryGrantStatus;
import com.vincenthuto.hemomancy.common.item.component.LivingWeaponForm;
import com.vincenthuto.hemomancy.common.item.component.LivingWeaponGraftData;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffItem;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.particle.SpawnGraftRiteItemParticlesPacket;
import com.vincenthuto.hemomancy.common.tile.IronBrazierBlockEntity;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class LivingWeaponGraftRite {
	public static final int REQUIRED_CHANNEL_TICKS = 60;

	private LivingWeaponGraftRite() {
	}

	public static double tryAbsorb(ServerLevel level, BlockPos pos, BlockState state, ServerPlayer player,
			double maxAmount) {
		if (!(level.getBlockEntity(pos) instanceof IronBrazierBlockEntity brazier) || maxAmount <= 0.0D) {
			return 0.0D;
		}
		ItemStack offering = brazier.getOfferingForMatching();
		LivingWeaponGraftData data = LivingWeaponGraftData.fromStack(offering).orElse(null);
		if (data == null) {
			return 0.0D;
		}
		LivingWeaponForm form = data.form();
		if (!LivingStaffItem.isLivingStaffAbsorptionUse(player, player.getUseItem())) {
			brazier.resetGraftRiteProgress();
			messageEverySecond(level, player, "Hold the Living Staff to receive the limb.");
			return 0.0D;
		}
		MemoryGrantResult check = LivingWeaponMemoryUnlocks.checkFormMemory(player, form);
		if (!check.success()) {
			brazier.resetGraftRiteProgress();
			reportFailure(level, player, check);
			return 0.0D;
		}
		if (!hasEarnedRecipeUnlockOrCreative(player, form)) {
			brazier.resetGraftRiteProgress();
			messageEverySecond(level, player, "The graft knows the shape. Your blood has not earned it.");
			return 0.0D;
		}
		if (state.hasProperty(BrazierBlock.RITUAL_PHASE) && state.getValue(BrazierBlock.RITUAL_PHASE) == 0) {
			level.setBlock(pos, state.setValue(BrazierBlock.RITUAL_PHASE, 2), Block.UPDATE_ALL);
		}
		int progress = brazier.advanceGraftRite(player, form, REQUIRED_CHANNEL_TICKS);
		if (progress % 10 == 0) {
			spawnChannelParticles(level, pos);
			spawnGraftDrawParticles(level, pos, player, offering);
		}
		if (progress < REQUIRED_CHANNEL_TICKS) {
			return maxAmount;
		}
		MemoryGrantResult result = LivingWeaponMemoryUnlocks.grantFormMemory(player, form);
		if (!result.success()) {
			brazier.resetGraftRiteProgress();
			reportFailure(level, player, result);
			return 0.0D;
		}
		brazier.consumeOffering();
		brazier.resetGraftRiteProgress();
		extinguishBrazier(level, pos);
		player.displayClientMessage(Component.literal("Your blood remembers " + form.manipulationDisplayName()
						+ ". The staff will answer this shape when called.")
				.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), false);
		level.playSound(null, pos, SoundEvents.BLAZE_SHOOT, SoundSource.BLOCKS, 0.55F, 0.65F);
		spawnCompletionParticles(level, pos);
		return maxAmount;
	}

	private static boolean hasEarnedRecipeUnlockOrCreative(ServerPlayer player, LivingWeaponForm form) {
		return player.isCreative() || LivingWeaponGraftRecipeUnlocks.hasEarnedRecipeUnlock(player, form);
	}

	private static void extinguishBrazier(ServerLevel level, BlockPos pos) {
		BlockState currentState = level.getBlockState(pos);
		if (currentState.hasProperty(BrazierBlock.RITUAL_PHASE)
				&& currentState.getValue(BrazierBlock.RITUAL_PHASE) != 0) {
			level.setBlock(pos, currentState.setValue(BrazierBlock.RITUAL_PHASE, 0), Block.UPDATE_ALL);
		}
	}

	private static void reportFailure(ServerLevel level, ServerPlayer player, MemoryGrantResult result) {
		if (result.status() == MemoryGrantStatus.ALREADY_KNOWN) {
			messageEverySecond(level, player, "Your blood already remembers this limb.");
		} else if (result.status() == MemoryGrantStatus.NO_ACTIVE_BLOOD) {
			messageEverySecond(level, player, "The graft dries before it can speak.");
		} else if (result.status() == MemoryGrantStatus.RANK_TOO_LOW) {
			messageEverySecond(level, player, "The memory recoils from a smaller vessel.");
		} else {
			messageEverySecond(level, player, "The brazier keeps its hunger.");
		}
	}

	private static void messageEverySecond(ServerLevel level, ServerPlayer player, String text) {
		if (level.getGameTime() % 20L == 0L) {
			player.displayClientMessage(Component.literal(text).withStyle(ChatFormatting.DARK_RED), true);
		}
	}

	private static void spawnChannelParticles(ServerLevel level, BlockPos pos) {
		Vec3 center = Vec3.atCenterOf(pos).add(0.0D, 0.55D, 0.0D);
		level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, center.x, center.y, center.z,
				4, 0.25D, 0.12D, 0.25D, 0.01D);
	}

	private static void spawnGraftDrawParticles(ServerLevel level, BlockPos pos, ServerPlayer player,
			ItemStack offering) {
		ItemStack particleStack = offering.copy();
		particleStack.setCount(1);
		PacketHandler.sendToPlayer(player,
				new SpawnGraftRiteItemParticlesPacket(Vec3.atCenterOf(pos).add(0.0D, 0.65D, 0.0D), particleStack));
	}

	private static void spawnCompletionParticles(ServerLevel level, BlockPos pos) {
		Vec3 center = Vec3.atCenterOf(pos).add(0.0D, 0.75D, 0.0D);
		level.sendParticles(GlowParticleFactory.createData(new ParticleColor(180, 10, 30)),
				center.x, center.y, center.z, 36, 0.55D, 0.35D, 0.55D, 0.02D);
	}
}
