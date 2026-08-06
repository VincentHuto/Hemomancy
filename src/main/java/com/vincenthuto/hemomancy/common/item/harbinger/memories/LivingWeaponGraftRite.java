package com.vincenthuto.hemomancy.common.item.harbinger.memories;

import com.vincenthuto.hemomancy.common.block.harbinger.BrazierBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.livingstaff.ILivingStaffProgress;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.livingstaff.LivingStaffBondHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationGrantHelper.MemoryGrantResult;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationGrantHelper.MemoryGrantStatus;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.item.component.LivingWeaponForm;
import com.vincenthuto.hemomancy.common.item.component.LivingWeaponGraftData;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffItem;
import com.vincenthuto.hemomancy.common.mission.HarbingerArtificerAssignmentHelper;
import com.vincenthuto.hemomancy.common.rite.BrazierItemAbsorptionRite;
import com.vincenthuto.hemomancy.common.tile.IronBrazierBlockEntity;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class LivingWeaponGraftRite {
	public static final int REQUIRED_CHANNEL_TICKS = BrazierItemAbsorptionRite.REQUIRED_CHANNEL_TICKS;

	private LivingWeaponGraftRite() {
	}

	public static double tryAbsorb(ServerLevel level, BlockPos pos, BlockState state, ServerPlayer player,
			double maxAmount) {
		if (!(level.getBlockEntity(pos) instanceof IronBrazierBlockEntity brazier) || maxAmount <= 0.0D) {
			return 0.0D;
		}
		ItemStack offering = brazier.getOfferingForMatching();
		LivingWeaponGraftData data = LivingWeaponGraftData.fromStack(offering).orElse(null);
		boolean vesperMemoryOffering = offering.is(ItemInit.memory_of_vesper.get());
		if (data == null && !vesperMemoryOffering) {
			return 0.0D;
		}
		LivingWeaponForm form = data == null ? null : data.form();
		boolean lit = state.hasProperty(BrazierBlock.RITUAL_PHASE)
				&& state.getValue(BrazierBlock.RITUAL_PHASE) > 0;
		if (!lit) {
			brazier.resetItemAbsorptionProgress();
			messageEverySecond(level, player, "Light the brazier before receiving its offering.");
			return 0.0D;
		}
		if (!LivingStaffItem.isLivingStaffAbsorptionUse(player, player.getUseItem())) {
			brazier.resetItemAbsorptionProgress();
			messageEverySecond(level, player, vesperMemoryOffering
					? "Hold the Living Staff to receive the memory."
					: "Hold the Living Staff to receive the limb.");
			return 0.0D;
		}
		if (vesperMemoryOffering) {
			if (!checkVesperMemory(level, player)) {
				brazier.resetItemAbsorptionProgress();
				return 0.0D;
			}
		} else {
			MemoryGrantResult check = LivingWeaponMemoryUnlocks.checkFormMemory(player, form);
			if (!check.success()) {
				brazier.resetItemAbsorptionProgress();
				reportFailure(level, player, check);
				return 0.0D;
			}
			if (!hasEarnedRecipeUnlockOrCreative(player, form)) {
				brazier.resetItemAbsorptionProgress();
				messageEverySecond(level, player, "The graft knows the shape. Your blood has not earned it.");
				return 0.0D;
			}
		}
		int progress = BrazierItemAbsorptionRite.advance(level, pos, player, brazier,
				vesperMemoryOffering ? "memory_of_vesper" : form.serializedName(), offering);
		if (!BrazierItemAbsorptionRite.isComplete(progress)) {
			return maxAmount;
		}
		if (vesperMemoryOffering) {
			if (!awakenVesperMemory(level, player)) {
				brazier.resetItemAbsorptionProgress();
				return 0.0D;
			}
		} else {
			MemoryGrantResult result = LivingWeaponMemoryUnlocks.grantFormMemory(player, form);
			if (!result.success()) {
				brazier.resetItemAbsorptionProgress();
				reportFailure(level, player, result);
				return 0.0D;
			}
			HarbingerArtificerAssignmentHelper.onLivingWeaponGraftComplete(player);
		}
		BrazierItemAbsorptionRite.complete(level, pos, brazier);
		if (vesperMemoryOffering) {
			player.displayClientMessage(Component.translatable("hemomancy.memory_of_vesper.awakened")
					.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), false);
		} else {
			player.displayClientMessage(Component.literal("Your blood remembers " + form.manipulationDisplayName()
							+ ". The staff will answer this shape when called.")
					.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), false);
		}
		level.playSound(null, pos, SoundEvents.BLAZE_SHOOT, SoundSource.BLOCKS, 0.55F, 0.65F);
		spawnCompletionParticles(level, pos);
		return maxAmount;
	}

	private static boolean checkVesperMemory(ServerLevel level, ServerPlayer player) {
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player).orElse(null);
		if (volume == null || !volume.isActive()) {
			messageEverySecond(level, player, "The graft dries before it can speak.");
			return false;
		}
		ILivingStaffProgress progress = HemoCapabilityAccess.getLivingStaffProgress(player).orElse(null);
		if (progress == null || !progress.hasLivingStaffBond()) {
			messageEverySecond(level, player, "The memory refuses a hand without a Living Staff bond.");
			return false;
		}
		if (progress.isVesperMemoryAwakened()) {
			messageEverySecond(level, player, "Vesper's refusal is already disciplined within your staff-bond.");
			return false;
		}
		boolean earnedRefusal = HemoCapabilityAccess.getInitiatoryDegree(player)
				.map(degree -> degree.getDegreeNumber() == 7
						&& degree.getArchonPath() == EnumArchonPath.SILENT_ARCHON)
				.orElse(false)
				&& HarbingerAdvancementGranter.hasAdvancement(player,
						HarbingerAdvancementGranter.ADV_VESPER_DEFEATED);
		if (!earnedRefusal) {
			messageEverySecond(level, player, "Vesper's memory answers only the refusal that defeated him.");
			return false;
		}
		return true;
	}

	private static boolean awakenVesperMemory(ServerLevel level, ServerPlayer player) {
		ILivingStaffProgress progress = HemoCapabilityAccess.getLivingStaffProgress(player).orElse(null);
		if (progress == null || !progress.hasLivingStaffBond()) {
			messageEverySecond(level, player, "The memory refuses a hand without a Living Staff bond.");
			return false;
		}
		if (!progress.awakenVesperMemory()) {
			messageEverySecond(level, player, "Vesper's refusal is already disciplined within your staff-bond.");
			return false;
		}
		if (!LivingStaffBondHelper.ensureVesperSickleKnown(player)) {
			progress.setVesperMemoryAwakened(false);
			messageEverySecond(level, player, "The refusal cannot find its final shape.");
			return false;
		}
		LivingStaffBondHelper.syncProgress(player);
		return true;
	}

	private static boolean hasEarnedRecipeUnlockOrCreative(ServerPlayer player, LivingWeaponForm form) {
		return player.isCreative() || LivingWeaponGraftRecipeUnlocks.hasEarnedRecipeUnlock(player, form);
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

	private static void spawnCompletionParticles(ServerLevel level, BlockPos pos) {
		Vec3 center = Vec3.atCenterOf(pos).add(0.0D, 0.75D, 0.0D);
		level.sendParticles(GlowParticleFactory.createData(new ParticleColor(180, 10, 30)),
				center.x, center.y, center.z, 36, 0.55D, 0.35D, 0.55D, 0.02D);
	}
}
