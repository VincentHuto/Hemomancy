package com.vincenthuto.hemomancy.common.item.harbinger.memories;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.livingstaff.ILivingStaffProgress;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.livingstaff.LivingStaffBondHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationGrantHelper.MemoryGrantResult;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationGrantHelper.MemoryGrantStatus;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.component.LivingWeaponForm;
import com.vincenthuto.hemomancy.common.item.component.LivingWeaponGraftData;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffItem;
import com.vincenthuto.hemomancy.common.mission.artificer.ArtificerAssignments;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.rite.BrazierItemAbsorptionRite;
import com.vincenthuto.hemomancy.common.rite.brazier.BrazierRite;
import com.vincenthuto.hemomancy.common.rite.brazier.BrazierRites;
import com.vincenthuto.hemomancy.common.util.HemoText;
import com.vincenthuto.hemomancy.common.util.ProgressionGates;
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

/**
 * Receiving a Living Weapon graft (or Vesper's memory) from a lit brazier through Living Staff absorption: the blood
 * remembers the limb's form, or the staff-bond takes Vesper's refusal.
 */
public final class LivingWeaponGraftRite implements BrazierRite<LivingWeaponGraftRite.GraftPlan> {
	public static final int REQUIRED_CHANNEL_TICKS = BrazierItemAbsorptionRite.REQUIRED_CHANNEL_TICKS;
	public static final LivingWeaponGraftRite INSTANCE = new LivingWeaponGraftRite();

	private LivingWeaponGraftRite() {
	}

	public static double tryAbsorb(ServerLevel level, BlockPos pos, BlockState state, ServerPlayer player,
			double maxAmount) {
		return BrazierRites.run(INSTANCE, level, pos, state, player, maxAmount);
	}

	/** A graft offering's form, or none for Vesper's memory. */
	public record GraftPlan(boolean vesperMemory, LivingWeaponForm form) {
	}

	@Override
	public boolean accepts(ItemStack offering) {
		return LivingWeaponGraftData.fromStack(offering).isPresent() || offering.is(ItemInit.memory_of_vesper.get());
	}

	@Override
	public Gate<GraftPlan> preflight(Channel channel) {
		if (channel.maxAmount() <= 0.0D) {
			return Gate.refuse(false, false);
		}
		ServerLevel level = channel.level();
		ServerPlayer player = channel.player();
		ItemStack offering = channel.offering();
		LivingWeaponGraftData data = LivingWeaponGraftData.fromStack(offering).orElse(null);
		boolean vesperMemoryOffering = offering.is(ItemInit.memory_of_vesper.get());
		LivingWeaponForm form = data == null ? null : data.form();
		if (!channel.lit()) {
			messageEverySecond(level, player, Component.translatable("message.hemomancy.living_weapon_graft_rite.light_the_brazier_before_receiving_its"));
			return Gate.refuse(true, false);
		}
		if (!LivingStaffItem.isLivingStaffAbsorptionUse(player, player.getUseItem())) {
			messageEverySecond(level, player, (vesperMemoryOffering ? Component.translatable("message.hemomancy.living_weapon_graft_rite.hold_the_living_staff_to_receive") : Component.translatable("message.hemomancy.living_weapon_graft_rite.hold_the_living_staff_to_receive_2")));
			return Gate.refuse(true, false);
		}
		if (vesperMemoryOffering) {
			if (!checkVesperMemory(level, player)) {
				return Gate.refuse(true, false);
			}
		} else {
			MemoryGrantResult check = LivingWeaponMemoryUnlocks.checkFormMemory(player, form);
			if (!check.success()) {
				reportFailure(level, player, check);
				return Gate.refuse(true, false);
			}
			if (!hasEarnedRecipeUnlockOrCreative(player, form)) {
				messageEverySecond(level, player, Component.translatable("message.hemomancy.living_weapon_graft_rite.the_graft_knows_the_shape_your"));
				return Gate.refuse(true, false);
			}
		}
		return Gate.pass(vesperMemoryOffering ? "memory_of_vesper" : form.serializedName(),
				new GraftPlan(vesperMemoryOffering, form));
	}

	@Override
	public Completion complete(Channel channel, GraftPlan plan) {
		ServerLevel level = channel.level();
		ServerPlayer player = channel.player();
		LivingWeaponForm form = plan.form();
		if (plan.vesperMemory()) {
			if (!awakenVesperMemory(level, player)) {
				return Completion.kept(false);
			}
		} else {
			MemoryGrantResult result = LivingWeaponMemoryUnlocks.grantFormMemory(player, form);
			if (!result.success()) {
				reportFailure(level, player, result);
				return Completion.kept(false);
			}
			ArtificerAssignments.onLivingWeaponGraftComplete(player);
		}
		if (plan.vesperMemory()) {
			player.displayClientMessage(Component.translatable("hemomancy.memory_of_vesper.awakened")
					.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), false);
		} else {
			player.displayClientMessage(HemoText.translatable("message.hemomancy.living_weapon_graft_rite.your_blood_remembers_the_staff_will",
					form.manipulationDisplayName()).withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), false);
		}
		level.playSound(null, channel.pos(), SoundEvents.BLAZE_SHOOT, SoundSource.BLOCKS, 0.55F, 0.65F);
		spawnCompletionParticles(level, channel.pos());
		return Completion.CONSUMED;
	}

	private static boolean checkVesperMemory(ServerLevel level, ServerPlayer player) {
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player);
		if (volume == null || !volume.isActive()) {
			messageEverySecond(level, player, Component.translatable("message.hemomancy.living_weapon_graft_rite.the_graft_dries_before_it_can"));
			return false;
		}
		ILivingStaffProgress progress = HemoCapabilityAccess.getLivingStaffProgress(player);
		if (progress == null || !progress.hasLivingStaffBond()) {
			messageEverySecond(level, player, Component.translatable("message.hemomancy.living_weapon_graft_rite.the_memory_refuses_a_hand_without"));
			return false;
		}
		if (progress.isVesperMemoryAwakened()) {
			messageEverySecond(level, player, Component.translatable("message.hemomancy.living_weapon_graft_rite.vesper_s_refusal_is_already_disciplined"));
			return false;
		}
		boolean earnedRefusal = (ProgressionGates.isDegree(player, 7)
						&& HemoCapabilityAccess.getInitiatoryDegree(player).getArchonPath() == EnumArchonPath.SILENT_ARCHON)
				&& HarbingerAdvancementGranter.hasAdvancement(player,
						HarbingerAdvancementGranter.ADV_VESPER_DEFEATED);
		if (!earnedRefusal) {
			messageEverySecond(level, player, Component.translatable("message.hemomancy.living_weapon_graft_rite.vesper_s_memory_answers_only_the"));
			return false;
		}
		return true;
	}

	private static boolean awakenVesperMemory(ServerLevel level, ServerPlayer player) {
		ILivingStaffProgress progress = HemoCapabilityAccess.getLivingStaffProgress(player);
		if (progress == null || !progress.hasLivingStaffBond()) {
			messageEverySecond(level, player, Component.translatable("message.hemomancy.living_weapon_graft_rite.the_memory_refuses_a_hand_without"));
			return false;
		}
		if (!progress.awakenVesperMemory()) {
			messageEverySecond(level, player, Component.translatable("message.hemomancy.living_weapon_graft_rite.vesper_s_refusal_is_already_disciplined"));
			return false;
		}
		if (!LivingStaffBondHelper.ensureVesperSickleKnown(player)) {
			progress.setVesperMemoryAwakened(false);
			messageEverySecond(level, player, Component.translatable("message.hemomancy.living_weapon_graft_rite.the_refusal_cannot_find_its_final"));
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
			messageEverySecond(level, player, Component.translatable("message.hemomancy.living_weapon_graft_rite.your_blood_already_remembers_this_limb"));
		} else if (result.status() == MemoryGrantStatus.NO_ACTIVE_BLOOD) {
			messageEverySecond(level, player, Component.translatable("message.hemomancy.living_weapon_graft_rite.the_graft_dries_before_it_can"));
		} else if (result.status() == MemoryGrantStatus.RANK_TOO_LOW) {
			messageEverySecond(level, player, Component.translatable("message.hemomancy.living_weapon_graft_rite.the_memory_recoils_from_a_smaller"));
		} else {
			messageEverySecond(level, player, Component.translatable("message.hemomancy.living_weapon_graft_rite.the_brazier_keeps_its_hunger"));
		}
	}

	private static void messageEverySecond(ServerLevel level, ServerPlayer player, Component text) {
		BrazierRites.messageEverySecond(level, player, text, ChatFormatting.DARK_RED);
	}

	private static void spawnCompletionParticles(ServerLevel level, BlockPos pos) {
		Vec3 center = Vec3.atCenterOf(pos).add(0.0D, 0.75D, 0.0D);
		level.sendParticles(HemoParticleData.glow(new ParticleColor(180, 10, 30)),
				center.x, center.y, center.z, 36, 0.55D, 0.35D, 0.55D, 0.02D);
	}
}
