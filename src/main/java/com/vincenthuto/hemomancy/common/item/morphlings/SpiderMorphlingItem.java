package com.vincenthuto.hemomancy.common.item.morphlings;

import com.vincenthuto.hemomancy.common.init.EffectInit;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

/**
 * Spider morphling that passively repairs vascular damage by applying the
 * Arachnid Anastomosis effect while equipped.
 */
public class SpiderMorphlingItem extends MorphlingItem {

	public SpiderMorphlingItem(Properties prop) {
		super(prop);
	}

	@Override
	public void onEquippedTick(Player player) {
		// Duration of 100 ticks (5 sec) exceeds the drain interval so the effect stays
		// active while equipped, but expires quickly if the morphling is removed.
		if (!player.hasEffect(EffectInit.arachnid_anastomosis.get())) {
			player.addEffect(new MobEffectInstance(EffectInit.arachnid_anastomosis.get(),
					100, 0, false, true, true));
		}
	}

}
