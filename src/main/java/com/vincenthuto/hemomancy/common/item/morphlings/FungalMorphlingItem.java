package com.vincenthuto.hemomancy.common.item.morphlings;

import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.EffectInit;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Fungal morphling that passively regenerates health by applying the
 * Mycorrhizal Mending effect while equipped. Maturity level scales the
 * healing amplifier. Prefers MORTEM (death/decay nourishes fungi) with
 * ANIMUS as secondary (life force aids regeneration).
 */
public class FungalMorphlingItem extends MorphlingItem {

	public FungalMorphlingItem(Properties prop) {
		super(prop);
	}

	@Override
	public EnumBloodTendency getPreferredTendency() {
		return EnumBloodTendency.MORTEM;
	}

	@Override
	public EnumBloodTendency getSecondaryTendency() {
		return EnumBloodTendency.ANIMUS;
	}

	@Override
	public void onEquippedTick(Player player, ItemStack stack) {
		int maturity = MorphlingItem.getMaturityLevel(stack);
		if (!player.hasEffect(EffectInit.mycorrhizal_mending.get())) {
			player.addEffect(new MobEffectInstance(EffectInit.mycorrhizal_mending.get(),
					100, maturity, false, true, true));
		}
	}

}
