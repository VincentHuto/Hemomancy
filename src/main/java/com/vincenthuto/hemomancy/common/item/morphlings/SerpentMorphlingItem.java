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
 * Serpent morphling that grants increased reflexes by applying the
 * Serpentine Guile effect while equipped. Maturity level scales the speed
 * bonuses (capped at amplifier 2 to prevent extreme stacking).
 * Prefers DUCTILIS (flexibility/neurotic energy fuels reflexes) with
 * FLAMMEUS as secondary (fervent heat drives quickness).
 *
 * Maturity bonuses:
 * - Developing (2): Night Vision (thermal pit sensing)
 * - Mature (3): Slow Falling (serpentine grace)
 * - Apex (4): Invisibility (apex camouflage)
 */
public class SerpentMorphlingItem extends MorphlingItem {

	public SerpentMorphlingItem(Properties prop) {
		super(prop);
	}

	@Override
	public EnumBloodTendency getPreferredTendency() {
		return EnumBloodTendency.DUCTILIS;
	}

	@Override
	public EnumBloodTendency getSecondaryTendency() {
		return EnumBloodTendency.FLAMMEUS;
	}

	@Override
	public void onEquippedTick(Player player, ItemStack stack) {
		int maturity = MorphlingItem.getMaturityLevel(stack);

		// Base effect: Serpentine Guile (move/attack speed, amplifier capped at 2)
		int amplifier = Math.min(maturity, 2);
		if (!player.hasEffect(EffectInit.serpentine_guile.get())) {
			player.addEffect(new MobEffectInstance(EffectInit.serpentine_guile.get(),
					100, amplifier, false, true, true));
		}

		// Developing (2+): Night Vision — thermal pit sensing
		// Use 300 tick duration to prevent the vanilla flickering at low duration
		if (maturity >= 2) {
			if (!player.hasEffect(MobEffects.NIGHT_VISION)) {
				player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION,
						300, 0, true, false, true));
			}
		}

		// Mature (3+): Slow Falling — serpentine grace
		if (maturity >= 3) {
			if (!player.hasEffect(MobEffects.SLOW_FALLING)) {
				player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING,
						100, 0, true, false, true));
			}
		}

		// Apex (4): Invisibility — apex camouflage
		if (maturity >= 4) {
			if (!player.hasEffect(MobEffects.INVISIBILITY)) {
				player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY,
						100, 0, true, false, true));
			}
		}
	}

	@Override
	public List<Component> getMaturityBonusDescriptions(int currentMaturity) {
		List<Component> list = new ArrayList<>();
		list.add(MorphlingItem.maturityBonusLine("Night Vision (Thermal Sensing)", 2, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Slow Falling (Serpentine Grace)", 3, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Invisibility (Apex Camouflage)", 4, currentMaturity));
		return list;
	}

}
