package com.vincenthuto.hemomancy.common.item.harbinger;

import com.vincenthuto.hemomancy.common.init.EffectInit;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

public final class CheapBloodInfusionHelper {
	private CheapBloodInfusionHelper() {
	}

	public static int getActiveAmplifier(Player player) {
		MobEffectInstance instance = player.getEffect(EffectInit.blood_drunkenness);
		return instance != null ? instance.getAmplifier() : -1;
	}

	public static double getManipulationCostMultiplier(Player player) {
		return getManipulationCostMultiplier(getActiveAmplifier(player));
	}

	public static double getManipulationCostMultiplier(int amplifier) {
		return CheapBloodInfusionRules.getManipulationCostMultiplier(amplifier);
	}

	public static double getManipulationCooldownMultiplier(Player player) {
		return getManipulationCooldownMultiplier(getActiveAmplifier(player));
	}

	public static double getManipulationCooldownMultiplier(int amplifier) {
		return CheapBloodInfusionRules.getManipulationCooldownMultiplier(amplifier);
	}

	public static void applySuccessfulInfusion(Player player, Item item) {
		int currentAmplifier = getActiveAmplifier(player);
		int nextAmplifier = CheapBloodInfusionRules.nextAmplifier(currentAmplifier);
		player.addEffect(new MobEffectInstance(EffectInit.blood_drunkenness,
				CheapBloodInfusionRules.DURATION_TICKS, nextAmplifier,
				false, true, true));
		player.getCooldowns().addCooldown(item, CheapBloodInfusionRules.COOLDOWN_TICKS);
		player.displayClientMessage(Component.translatable("message.hemomancy.blood_drunkenness")
				.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), true);
	}
}
