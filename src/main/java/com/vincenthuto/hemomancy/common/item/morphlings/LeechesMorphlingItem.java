package com.vincenthuto.hemomancy.common.item.morphlings;

import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.EffectInit;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Leeches morphling that passively replenishes blood volume by applying the
 * Sanguine Siphon effect while equipped. Maturity level scales the blood
 * fill rate. Prefers ANIMUS (life force feeds the leeches) with
 * CONGEATIO as secondary (cold-blooded affinity).
 */
public class LeechesMorphlingItem extends MorphlingItem {

	public LeechesMorphlingItem(Properties prop) {
		super(prop);
	}

	@Override
	public EnumBloodTendency getPreferredTendency() {
		return EnumBloodTendency.ANIMUS;
	}

	@Override
	public EnumBloodTendency getSecondaryTendency() {
		return EnumBloodTendency.CONGEATIO;
	}

	@Override
	public void onEquippedTick(Player player, ItemStack stack) {
		int maturity = MorphlingItem.getMaturityLevel(stack);
		if (!player.hasEffect(EffectInit.sanguine_siphon.get())) {
			player.addEffect(new MobEffectInstance(EffectInit.sanguine_siphon.get(),
					100, maturity, false, true, true));
		}
	}

}
