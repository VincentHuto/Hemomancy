package com.vincenthuto.hemomancy.common.item.morphlings;

import com.vincenthuto.hemomancy.common.init.EffectInit;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

/**
 * Serpent morphling that grants increased reflexes by applying the
 * Serpentine Guile effect while equipped.
 */
public class SerpentMorphlingItem extends MorphlingItem {

	public SerpentMorphlingItem(Properties prop) {
		super(prop);
	}

	@Override
	public void onEquippedTick(Player player) {
		if (!player.hasEffect(EffectInit.serpentine_guile.get())) {
			player.addEffect(new MobEffectInstance(EffectInit.serpentine_guile.get(),
					100, 0, false, true, true));
		}
	}

}
