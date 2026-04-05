package com.vincenthuto.hemomancy.common.item.morphlings;

import com.vincenthuto.hemomancy.common.init.EffectInit;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

/**
 * Leeches morphling that passively replenishes blood volume by applying the
 * Sanguine Siphon effect while equipped.
 */
public class LeechesMorphlingItem extends MorphlingItem {

	public LeechesMorphlingItem(Properties prop) {
		super(prop);
	}

	@Override
	public void onEquippedTick(Player player) {
		if (!player.hasEffect(EffectInit.sanguine_siphon.get())) {
			player.addEffect(new MobEffectInstance(EffectInit.sanguine_siphon.get(),
					100, 0, false, true, true));
		}
	}

}
