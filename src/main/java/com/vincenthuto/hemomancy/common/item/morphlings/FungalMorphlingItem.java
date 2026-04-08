package com.vincenthuto.hemomancy.common.item.morphlings;

import java.util.ArrayList;
import java.util.List;

import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.EffectInit;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Fungal morphling that passively regenerates health by applying the
 * Mycorrhizal Mending effect while equipped. Maturity level scales the
 * healing amplifier. Prefers MORTEM (death/decay nourishes fungi) with
 * ANIMUS as secondary (life force aids regeneration).
 *
 * Maturity bonuses:
 * - Developing (2): Saturation (fungal nutritional symbiosis)
 * - Mature (3): Poison immunity (mycorrhizal toxin breakdown)
 * - Apex (4): Absorption hearts (living fungal shield)
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

		// Base effect: Mycorrhizal Mending (health regen, amplifier = maturity)
		if (!player.hasEffect(EffectInit.mycorrhizal_mending.get())) {
			player.addEffect(new MobEffectInstance(EffectInit.mycorrhizal_mending.get(),
					100, maturity, false, true, true));
		}

		// Developing (2+): Saturation — fungal nutritional symbiosis
		if (maturity >= 2) {
			if (!player.hasEffect(MobEffects.SATURATION)) {
				player.addEffect(new MobEffectInstance(MobEffects.SATURATION,
						100, 0, true, false, true));
			}
		}

		// Mature (3+): Poison immunity — mycorrhizal toxin breakdown
		if (maturity >= 3) {
			if (player.hasEffect(MobEffects.POISON)) {
				player.removeEffect(MobEffects.POISON);
			}
		}

		// Apex (4): Absorption — living fungal shield
		if (maturity >= 4) {
			if (!player.hasEffect(MobEffects.ABSORPTION)) {
				player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION,
						200, 1, true, true, true));
			}
		}
	}

	@Override
	public List<Component> getMaturityBonusDescriptions(int currentMaturity) {
		List<Component> list = new ArrayList<>();
		list.add(MorphlingItem.maturityBonusLine("Saturation (Fungal Symbiosis)", 2, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Poison Immunity (Toxin Breakdown)", 3, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Absorption Hearts (Fungal Shield)", 4, currentMaturity));
		return list;
	}

}
