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
 * Chitinite morphling that grants damage resistance by applying the
 * Chitinous Bulwark effect while equipped. Maturity level scales the armor
 * toughness bonus (capped at amplifier 2 to prevent extreme stacking).
 * Prefers FERRIC (iron/metal strengthens the carapace) with
 * CONGEATIO as secondary (cold hardens chitin).
 *
 * Maturity bonuses:
 * - Developing (2): Fire Resistance (insulating carapace)
 * - Mature (3): Damage Resistance (thickened exoskeleton)
 * - Apex (4): Absorption hearts (layered chitin plates)
 */
public class ChitiniteMorphlingItem extends MorphlingItem {

	public ChitiniteMorphlingItem(Properties prop) {
		super(prop);
	}

	@Override
	public EnumBloodTendency getPreferredTendency() {
		return EnumBloodTendency.FERRIC;
	}

	@Override
	public EnumBloodTendency getSecondaryTendency() {
		return EnumBloodTendency.CONGEATIO;
	}

	@Override
	public void onEquippedTick(Player player, ItemStack stack) {
		int maturity = MorphlingItem.getMaturityLevel(stack);

		// Base effect: Chitinous Bulwark (armor toughness, amplifier capped at 2)
		int amplifier = Math.min(maturity, 2);
		if (!player.hasEffect(EffectInit.chitinous_bulwark.get())) {
			player.addEffect(new MobEffectInstance(EffectInit.chitinous_bulwark.get(),
					100, amplifier, false, true, true));
		}

		// Developing (2+): Fire Resistance — insulating carapace
		if (maturity >= 2) {
			if (!player.hasEffect(MobEffects.FIRE_RESISTANCE)) {
				player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE,
						200, 0, true, false, true));
			}
		}

		// Mature (3+): Damage Resistance — thickened exoskeleton
		if (maturity >= 3) {
			if (!player.hasEffect(MobEffects.DAMAGE_RESISTANCE)) {
				player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,
						100, 0, true, true, true));
			}
		}

		// Apex (4): Absorption — layered chitin plates create extra defense
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
		list.add(MorphlingItem.maturityBonusLine("Fire Resistance (Insulating Carapace)", 2, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Damage Resistance (Thickened Exoskeleton)", 3, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Absorption Hearts (Chitin Plates)", 4, currentMaturity));
		return list;
	}

}
