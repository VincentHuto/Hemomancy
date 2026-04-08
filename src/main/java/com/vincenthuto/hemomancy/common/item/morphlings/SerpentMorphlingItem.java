package com.vincenthuto.hemomancy.common.item.morphlings;

import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.EffectInit;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Serpent morphling that grants increased reflexes by applying the
 * Serpentine Guile effect while equipped. Maturity level scales the speed
 * bonuses (capped at amplifier 2 to prevent extreme stacking).
 * Prefers DUCTILIS (flexibility/neurotic energy fuels reflexes) with
 * FLAMMEUS as secondary (fervent heat drives quickness).
 */
public class SerpentMorphlingItem extends MorphlingItem {

	public SerpentMorphlingItem(Properties prop) {
		super(prop);
	}

	@Override
	public EnumBloodTendency getPreferredTendency() {
		return EnumBloodTendency.DUCTILIS;
	}

	@Override
	public EnumBloodTendency getSecondaryTendency() {
		return EnumBloodTendency.FLAMMEUS;
	}

	@Override
	public void onEquippedTick(Player player, ItemStack stack) {
		int maturity = MorphlingItem.getMaturityLevel(stack);
		// Cap amplifier at 2 for attribute-based effects to prevent extreme stacking
		int amplifier = Math.min(maturity, 2);
		if (!player.hasEffect(EffectInit.serpentine_guile.get())) {
			player.addEffect(new MobEffectInstance(EffectInit.serpentine_guile.get(),
					100, amplifier, false, true, true));
		}
	}

}
