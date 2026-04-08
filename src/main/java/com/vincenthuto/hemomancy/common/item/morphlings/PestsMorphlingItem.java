package com.vincenthuto.hemomancy.common.item.morphlings;

import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.EffectInit;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Pests morphling that damages nearby hostile mobs by applying the
 * Verminous Aura effect while equipped. Maturity level scales the damage
 * radius and damage dealt. Prefers FLAMMEUS (fervent heat drives the swarm)
 * with TENEBRIS as secondary (darkness harbors vermin).
 */
public class PestsMorphlingItem extends MorphlingItem {

	public PestsMorphlingItem(Properties prop) {
		super(prop);
	}

	@Override
	public EnumBloodTendency getPreferredTendency() {
		return EnumBloodTendency.FLAMMEUS;
	}

	@Override
	public EnumBloodTendency getSecondaryTendency() {
		return EnumBloodTendency.TENEBRIS;
	}

	@Override
	public void onEquippedTick(Player player, ItemStack stack) {
		int maturity = MorphlingItem.getMaturityLevel(stack);
		if (!player.hasEffect(EffectInit.verminous_aura.get())) {
			player.addEffect(new MobEffectInstance(EffectInit.verminous_aura.get(),
					100, maturity, false, true, true));
		}
	}

}
