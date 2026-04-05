package com.vincenthuto.hemomancy.common.item.morphlings;

import com.vincenthuto.hemomancy.common.init.EffectInit;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

/**
 * Fungal morphling that passively regenerates health by applying the
 * Mycorrhizal Mending effect while equipped.
 */
public class FungalMorphlingItem extends MorphlingItem {

	public FungalMorphlingItem(Properties prop) {
		super(prop);
	}

	@Override
	public void onEquippedTick(Player player) {
		if (!player.hasEffect(EffectInit.mycorrhizal_mending.get())) {
			player.addEffect(new MobEffectInstance(EffectInit.mycorrhizal_mending.get(),
					100, 0, false, true, true));
		}
	}

}
