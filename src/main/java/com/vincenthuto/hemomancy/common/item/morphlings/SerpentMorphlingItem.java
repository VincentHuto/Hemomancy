package com.vincenthuto.hemomancy.common.item.morphlings;

import java.util.ArrayList;
import java.util.List;

import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.EffectInit;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Serpent morphling that grants increased reflexes by applying the
 * Serpentine Guile effect while equipped. Maturity level scales the speed
 * bonuses (capped at amplifier 2 to prevent extreme stacking).
 * Prefers DUCTILIS (flexibility/neurotic energy fuels reflexes) with
 * FLAMMEUS as secondary (fervent heat drives quickness).
 *
 * Maturity bonuses (unique reactive abilities):
 * - Developing (2): Venom Strike — melee attacks apply scaling Poison to
 *   targets (serpent injects venom through the player's strikes)
 * - Mature (3): Shed Skin — periodically purge all negative status effects
 *   (serpent helps the player shed like a snake shedding its skin)
 * - Apex (4): Predator's Mark — melee attacks apply Glowing to the target
 *   and reduce their armor with Weakness, making them take more damage
 *   from all sources (marked prey of the apex predator)
 */
public class SerpentMorphlingItem extends MorphlingItem {

	/** Interval in ticks between Shed Skin activations (20 seconds). */
	private static final int SHED_SKIN_INTERVAL = 400;

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

		// Mature (3+): Shed Skin — periodically purge all negative effects
		if (maturity >= 3 && !player.level().isClientSide) {
			long now = player.level().getGameTime();
			long lastShed = getLastAbilityTick(stack, "ShedSkin");
			if (now - lastShed >= SHED_SKIN_INTERVAL) {
				boolean hadNegative = player.getActiveEffects().stream()
						.anyMatch(e -> !e.getEffect().isBeneficial());
				if (hadNegative) {
					// Collect harmful effects and remove them
					List<MobEffect> toRemove = new ArrayList<>();
					for (MobEffectInstance effectInstance : player.getActiveEffects()) {
						if (!effectInstance.getEffect().isBeneficial()) {
							toRemove.add(effectInstance.getEffect());
						}
					}
					for (MobEffect effect : toRemove) {
						player.removeEffect(effect);
					}
					setLastAbilityTick(stack, "ShedSkin", now);
				}
			}
		}
	}

	@Override
	public void onEquippedAttack(Player player, ItemStack stack, LivingEntity target, float amount) {
		int maturity = MorphlingItem.getMaturityLevel(stack);

		// Developing (2+): Venom Strike — apply Poison on melee hit
		if (maturity >= 2) {
			int venomDuration = 60 + (maturity - 2) * 40; // 3s at Developing, 5s Mature, 7s Apex
			int venomAmplifier = (maturity >= 4) ? 1 : 0; // Poison II at Apex
			target.addEffect(new MobEffectInstance(MobEffects.POISON,
					venomDuration, venomAmplifier, true, true, true));
		}

		// Apex (4): Predator's Mark — mark target with Glowing + Weakness
		if (maturity >= 4) {
			target.addEffect(new MobEffectInstance(MobEffects.GLOWING,
					200, 0, true, false, true));
			target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,
					200, 1, true, true, true));
		}
	}

	@Override
	public List<Component> getMaturityBonusDescriptions(int currentMaturity) {
		List<Component> list = new ArrayList<>();
		list.add(MorphlingItem.maturityBonusLine("Venom Strike (Poison on melee hit)", 2, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Shed Skin (Purge negative effects periodically)", 3, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Predator's Mark (Weaken & expose targets)", 4, currentMaturity));
		return list;
	}

}
