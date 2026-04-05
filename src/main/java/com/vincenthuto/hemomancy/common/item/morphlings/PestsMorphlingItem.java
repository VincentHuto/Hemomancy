package com.vincenthuto.hemomancy.common.item.morphlings;

import com.vincenthuto.hemomancy.common.init.EffectInit;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

/**
 * Pests morphling that damages nearby hostile mobs by applying the
 * Verminous Aura effect while equipped.
 */
public class PestsMorphlingItem extends MorphlingItem {

	public PestsMorphlingItem(Properties prop) {
		super(prop);
	}

	@Override
	public void onEquippedTick(Player player) {
		if (!player.hasEffect(EffectInit.verminous_aura.get())) {
			player.addEffect(new MobEffectInstance(EffectInit.verminous_aura.get(),
					100, 0, false, true, true));
		}
	}

}
