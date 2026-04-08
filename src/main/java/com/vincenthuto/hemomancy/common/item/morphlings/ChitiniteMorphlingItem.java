package com.vincenthuto.hemomancy.common.item.morphlings;

import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.EffectInit;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Chitinite morphling that grants damage resistance by applying the
 * Chitinous Bulwark effect while equipped. Maturity level scales the armor
 * toughness bonus (capped at amplifier 2 to prevent extreme stacking).
 * Prefers FERRIC (iron/metal strengthens the carapace) with
 * CONGEATIO as secondary (cold hardens chitin).
 */
public class ChitiniteMorphlingItem extends MorphlingItem {

	public ChitiniteMorphlingItem(Properties prop) {
		super(prop);
	}

	@Override
	public EnumBloodTendency getPreferredTendency() {
		return EnumBloodTendency.FERRIC;
	}

	@Override
	public EnumBloodTendency getSecondaryTendency() {
		return EnumBloodTendency.CONGEATIO;
	}

	@Override
	public void onEquippedTick(Player player, ItemStack stack) {
		int maturity = MorphlingItem.getMaturityLevel(stack);
		// Cap amplifier at 2 for attribute-based effects to prevent extreme stacking
		int amplifier = Math.min(maturity, 2);
		if (!player.hasEffect(EffectInit.chitinous_bulwark.get())) {
			player.addEffect(new MobEffectInstance(EffectInit.chitinous_bulwark.get(),
					100, amplifier, false, true, true));
		}
	}

}
