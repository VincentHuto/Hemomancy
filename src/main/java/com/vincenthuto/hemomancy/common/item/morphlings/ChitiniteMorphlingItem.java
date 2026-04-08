package com.vincenthuto.hemomancy.common.item.morphlings;

import java.util.ArrayList;
import java.util.List;

import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.EffectInit;

import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Chitinite morphling that grants damage resistance by applying the
 * Chitinous Bulwark effect while equipped. Maturity level scales the armor
 * toughness bonus (capped at amplifier 2 to prevent extreme stacking).
 * Prefers FERRIC (iron/metal strengthens the carapace) with
 * CONGEATIO as secondary (cold hardens chitin).
 *
 * Maturity bonuses (unique reactive abilities):
 * - Developing (2): Carapace Thorns — reflect a percentage of melee damage
 *   back at attackers (chitin spines lacerate on contact)
 * - Mature (3): Molt — periodically purge all negative status effects as the
 *   morphling sheds its outer layer (like a molting insect)
 * - Apex (4): Ironhide — after taking a hit greater than 6 damage, gain brief
 *   invulnerability (60 tick cooldown, simulates hardened carapace)
 */
public class ChitiniteMorphlingItem extends MorphlingItem {

	/** Cooldown in ticks between Ironhide triggers (3 seconds). */
	private static final int IRONHIDE_COOLDOWN = 60;
	/** Molt interval in ticks (15 seconds). */
	private static final int MOLT_INTERVAL = 300;

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

		// Mature (3+): Molt — periodically shed all negative effects
		if (maturity >= 3 && !player.level().isClientSide) {
			long now = player.level().getGameTime();
			long lastMolt = getLastAbilityTick(stack, "Molt");
			if (now - lastMolt >= MOLT_INTERVAL) {
				List<net.minecraft.world.effect.MobEffect> toRemove = new ArrayList<>();
				for (MobEffectInstance effectInstance : player.getActiveEffects()) {
					if (!effectInstance.getEffect().isBeneficial()) {
						toRemove.add(effectInstance.getEffect());
					}
				}
				if (!toRemove.isEmpty()) {
					for (net.minecraft.world.effect.MobEffect effect : toRemove) {
						player.removeEffect(effect);
					}
					setLastAbilityTick(stack, "Molt", now);
				}
			}
		}
	}

	@Override
	public void onEquippedHurt(Player player, ItemStack stack, DamageSource source, float amount) {
		int maturity = MorphlingItem.getMaturityLevel(stack);

		// Developing (2+): Carapace Thorns — reflect damage to melee attackers
		if (maturity >= 2 && source.getEntity() instanceof LivingEntity attacker) {
			float thornsPct = 0.20f + (maturity - 2) * 0.10f; // 20% at Developing, 30% Mature, 40% Apex
			float thornsDamage = amount * thornsPct;
			if (thornsDamage > 0.5f) {
				attacker.hurt(player.damageSources().thorns(player), thornsDamage);
			}
		}

		// Apex (4): Ironhide — brief invulnerability after a heavy hit
		if (maturity >= 4 && amount >= 6.0f) {
			long lastIronhide = getLastAbilityTick(stack, "Ironhide");
			long now = player.level().getGameTime();
			if (now - lastIronhide >= IRONHIDE_COOLDOWN) {
				setLastAbilityTick(stack, "Ironhide", now);
				// Grant 1.5 seconds of invulnerability
				player.invulnerableTime = 30;
			}
		}
	}

	@Override
	public List<Component> getMaturityBonusDescriptions(int currentMaturity) {
		List<Component> list = new ArrayList<>();
		list.add(MorphlingItem.maturityBonusLine("Carapace Thorns (Reflect melee damage)", 2, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Molt (Purge negative effects periodically)", 3, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Ironhide (Brief invulnerability after heavy hit)", 4, currentMaturity));
		return list;
	}

}
