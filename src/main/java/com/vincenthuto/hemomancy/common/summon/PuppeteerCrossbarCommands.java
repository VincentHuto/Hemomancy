package com.vincenthuto.hemomancy.common.summon;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.entity.summon.BoundSummonBehavior;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.MarionetteCrossbarItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class PuppeteerCrossbarCommands {
	private PuppeteerCrossbarCommands() {
	}

	public static boolean setMode(ServerPlayer player, ItemStack crossbar, PuppeteerCommandMode mode) {
		if (!MarionetteCrossbarItem.validateControl(crossbar, player, false) || mode == null) {
			return false;
		}
		MarionetteCrossbarItem.setCommandMode(crossbar, mode);
		if (mode == PuppeteerCommandMode.GUARD) {
			MarionetteCrossbarItem.setGuardAnchor(crossbar, player.blockPosition(), player.level().dimension());
		} else {
			MarionetteCrossbarItem.clearGuardAnchor(crossbar);
		}
		player.playSound(SoundEvents.WOODEN_BUTTON_CLICK_ON, 0.35F, 0.75F);
		player.displayClientMessage(Component.translatable("hemomancy.summon.command." + mode.serializedName())
				.withStyle(ChatFormatting.RED), true);
		return true;
	}

	public static boolean hotSwap(ServerPlayer player, ItemStack crossbar, String selectedName) {
		if (!MarionetteCrossbarItem.validateControl(crossbar, player, false)
				|| SkillPointHelper.getSkeinTranspositionLevel(player) <= 0
				|| player.getCooldowns().isOnCooldown(crossbar.getItem())) {
			return false;
		}
		String oldName = MarionetteCrossbarItem.getSelectedSummonName(crossbar);
		if (selectedName == null || selectedName.isBlank() || selectedName.equals(oldName)) {
			return false;
		}
		Optional<PuppeteerSummonDefinition> definitionOpt = PuppeteerSummonDefinitions.byName(selectedName);
		if (definitionOpt.isEmpty() || !HemoCapabilityAccess.getKnownSummons(player)
				.map(known -> known.isKnown(definitionOpt.get())).orElse(false)) {
			return false;
		}

		PuppeteerSummonDefinition definition = definitionOpt.get();
		UUID crossbarId = MarionetteCrossbarItem.ensureCrossbarId(crossbar);
		List<Mob> activeBodies = MarionetteCrossbarItem.activeSummonsForOwner(player);
		List<Mob> oldCohort = activeBodies.stream()
				.filter(body -> !BoundSummonBehavior.isClaimedWill(body))
				.filter(body -> body instanceof com.vincenthuto.hemomancy.common.entity.summon.BoundPuppeteerSummon bound
						&& crossbarId.equals(bound.hemomancy$getCrossbarUUID())
						&& oldName.equals(bound.hemomancy$getSummonName()))
				.toList();
		int shapedBodies = (int) activeBodies.stream().filter(body -> !BoundSummonBehavior.isClaimedWill(body)).count();
		int projectedShaped = PuppeteerSummonRules.projectedShapedCount(shapedBodies, oldCohort.size(), 1);
		int shapedCap = PuppeteerSummonRules.activeSummonCap(SkillPointHelper.getPuppetSkeinLevel(player));
		int projectedTotal = activeBodies.size() - oldCohort.size() + 1;
		if (projectedShaped > shapedCap || projectedTotal > shapedCap + BoundSummonBehavior.claimedWillBonusCap(player)) {
			return false;
		}
		int summonCost = MarionetteCrossbarItem.summonThreadCost(player, definition);
		if (MarionetteCrossbarItem.getThread(crossbar) < summonCost) {
			return false;
		}
		Optional<Mob> candidateOpt = PuppeteerSummonFactory.create(definition, player.level(), player,
				crossbarId, SkillPointHelper.getLivingSinewLevel(player));
		if (candidateOpt.isEmpty()) {
			return false;
		}
		Mob candidate = candidateOpt.get();
		if (!player.level().addFreshEntity(candidate)) {
			candidate.discard();
			return false;
		}
		if (!MarionetteCrossbarItem.consumeThread(crossbar, summonCost)) {
			candidate.discard();
			return false;
		}
		for (Mob oldBody : oldCohort) {
			oldBody.discard();
		}
		MarionetteCrossbarItem.setSelectedSummonName(crossbar, selectedName);
		player.getCooldowns().addCooldown(crossbar.getItem(), 20);
		player.playSound(SoundEvents.EVOKER_PREPARE_SUMMON, 0.55F, 0.9F);
		player.displayClientMessage(Component.translatable("hemomancy.summon.swapped",
				Component.translatable(definition.translationKey())).withStyle(ChatFormatting.RED), true);
		return true;
	}
}
