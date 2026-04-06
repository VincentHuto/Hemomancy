package com.vincenthuto.hemomancy.common.item.morphlings;

import com.vincenthuto.hemomancy.common.init.EffectInit;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

/**
 * Chitinite morphling that grants damage resistance by applying the
 * Chitinous Bulwark effect while equipped.
 */
public class ChitiniteMorphlingItem extends MorphlingItem {

	public ChitiniteMorphlingItem(Properties prop) {
		super(prop);
	}

	@Override
	public void onEquippedTick(Player player) {
		if (!player.hasEffect(EffectInit.chitinous_bulwark.get())) {
			player.addEffect(new MobEffectInstance(EffectInit.chitinous_bulwark.get(),
					100, 0, false, true, true));
		}
	}

}
