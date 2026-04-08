package com.vincenthuto.hemomancy.common.item.morphlings;

import java.util.ArrayList;
import java.util.List;

import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.EffectInit;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * Pests morphling that damages nearby hostile mobs by applying the
 * Verminous Aura effect while equipped. Maturity level scales the damage
 * radius and damage dealt. Prefers FLAMMEUS (fervent heat drives the swarm)
 * with TENEBRIS as secondary (darkness harbors vermin).
 *
 * Maturity bonuses:
 * - Developing (2): Luck (vermin scavenging instinct)
 * - Mature (3): Strength (swarm frenzy empowers attacks)
 * - Apex (4): Nearby hostiles get Weakness + Poison (pestilent cloud)
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

		// Base effect: Verminous Aura (AoE damage, amplifier = maturity)
		if (!player.hasEffect(EffectInit.verminous_aura.get())) {
			player.addEffect(new MobEffectInstance(EffectInit.verminous_aura.get(),
					100, maturity, false, true, true));
		}

		// Developing (2+): Luck — vermin scavenging instinct
		if (maturity >= 2) {
			if (!player.hasEffect(MobEffects.LUCK)) {
				player.addEffect(new MobEffectInstance(MobEffects.LUCK,
						100, 0, true, false, true));
			}
		}

		// Mature (3+): Strength — swarm frenzy empowers attacks
		if (maturity >= 3) {
			if (!player.hasEffect(MobEffects.DAMAGE_BOOST)) {
				player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,
						100, 0, true, true, true));
			}
		}

		// Apex (4): Pestilent cloud — nearby hostiles get Weakness + Poison
		if (maturity >= 4 && !player.level().isClientSide) {
			Level level = player.level();
			double radius = 6.0;
			AABB area = player.getBoundingBox().inflate(radius);
			List<Monster> hostiles = level.getEntitiesOfClass(Monster.class, area);
			for (Monster mob : hostiles) {
				if (!mob.hasEffect(MobEffects.WEAKNESS)) {
					mob.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,
							100, 0, true, true, true));
				}
				if (!mob.hasEffect(MobEffects.POISON)) {
					mob.addEffect(new MobEffectInstance(MobEffects.POISON,
							60, 0, true, true, true));
				}
			}
		}
	}

	@Override
	public List<Component> getMaturityBonusDescriptions(int currentMaturity) {
		List<Component> list = new ArrayList<>();
		list.add(MorphlingItem.maturityBonusLine("Luck (Vermin Scavenging)", 2, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Strength (Swarm Frenzy)", 3, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Pestilent Cloud (Weakness + Poison Aura)", 4, currentMaturity));
		return list;
	}

}
