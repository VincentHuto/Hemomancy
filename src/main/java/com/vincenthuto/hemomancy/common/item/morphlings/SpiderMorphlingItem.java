package com.vincenthuto.hemomancy.common.item.morphlings;

import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.EffectInit;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Spider morphling that passively repairs vascular damage by applying the
 * Arachnid Anastomosis effect while equipped. Maturity level scales the
 * vascular repair rate. Prefers TENEBRIS (darkness/shadow fuels the web)
 * with LUX as secondary (light reveals and mends hidden damage).
 */
public class SpiderMorphlingItem extends MorphlingItem {

	public SpiderMorphlingItem(Properties prop) {
		super(prop);
	}

	@Override
	public EnumBloodTendency getPreferredTendency() {
		return EnumBloodTendency.TENEBRIS;
	}

	@Override
	public EnumBloodTendency getSecondaryTendency() {
		return EnumBloodTendency.LUX;
	}

	@Override
	public void onEquippedTick(Player player, ItemStack stack) {
		int maturity = MorphlingItem.getMaturityLevel(stack);
		// Duration of 100 ticks (5 sec) exceeds the drain interval so the effect stays
		// active while equipped, but expires quickly if the morphling is removed.
		if (!player.hasEffect(EffectInit.arachnid_anastomosis.get())) {
			player.addEffect(new MobEffectInstance(EffectInit.arachnid_anastomosis.get(),
					100, maturity, false, true, true));
		}
	}

}
