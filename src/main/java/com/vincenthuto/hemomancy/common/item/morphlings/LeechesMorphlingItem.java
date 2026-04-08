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
 * Leeches morphling that passively replenishes blood volume by applying the
 * Sanguine Siphon effect while equipped. Maturity level scales the blood
 * fill rate. Prefers ANIMUS (life force feeds the leeches) with
 * CONGEATIO as secondary (cold-blooded affinity).
 *
 * Maturity bonuses:
 * - Developing (2): Regeneration (leech saliva promotes wound healing)
 * - Mature (3): Health Boost (parasitic vitality, +2 max hearts)
 * - Apex (4): Damage Resistance (blood cocoon absorbs impacts)
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

		// Base effect: Sanguine Siphon (blood fill, amplifier = maturity)
		if (!player.hasEffect(EffectInit.sanguine_siphon.get())) {
			player.addEffect(new MobEffectInstance(EffectInit.sanguine_siphon.get(),
					100, maturity, false, true, true));
		}

		// Developing (2+): Regeneration — leech saliva promotes wound healing
		if (maturity >= 2) {
			if (!player.hasEffect(MobEffects.REGENERATION)) {
				player.addEffect(new MobEffectInstance(MobEffects.REGENERATION,
						100, 0, true, true, true));
			}
		}

		// Mature (3+): Health Boost — parasitic vitality grants extra hearts
		if (maturity >= 3) {
			if (!player.hasEffect(MobEffects.HEALTH_BOOST)) {
				player.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST,
						100, 0, true, true, true));
			}
		}

		// Apex (4): Damage Resistance — blood cocoon absorbs impacts
		if (maturity >= 4) {
			if (!player.hasEffect(MobEffects.DAMAGE_RESISTANCE)) {
				player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,
						100, 0, true, true, true));
			}
		}
	}

	@Override
	public List<Component> getMaturityBonusDescriptions(int currentMaturity) {
		List<Component> list = new ArrayList<>();
		list.add(MorphlingItem.maturityBonusLine("Regeneration (Leech Saliva)", 2, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Health Boost (Parasitic Vitality)", 3, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Damage Resistance (Blood Cocoon)", 4, currentMaturity));
		return list;
	}

}
