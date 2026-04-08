package com.vincenthuto.hemomancy.common.item.morphlings;

import java.util.ArrayList;
import java.util.List;

import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.EffectInit;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * Lamprey morphling that grants absorption hearts by applying the
 * Parasitic Anchor effect while equipped. Maturity level scales the
 * absorption cap. Prefers ANIMUS (life force feeds the parasite) with
 * MORTEM as secondary (death energy fuels parasitic hunger).
 *
 * Maturity bonuses (unique reactive abilities):
 * - Developing (2): Latching Bite — melee attacks apply Mining Fatigue to
 *   the target, slowing their attack cadence
 * - Mature (3): Blood Tide — when the player has absorption hearts, nearby
 *   allies slowly gain absorption too
 * - Apex (4): Exsanguinating Grasp — melee attacks drain a small amount of
 *   health from the target as bonus magic damage, healing the player
 */
public class LampreyMorphlingItem extends MorphlingItem {

	public LampreyMorphlingItem(Properties prop) {
		super(prop);
	}

	@Override
	public EnumBloodTendency getPreferredTendency() {
		return EnumBloodTendency.ANIMUS;
	}

	@Override
	public EnumBloodTendency getSecondaryTendency() {
		return EnumBloodTendency.MORTEM;
	}

	@Override
	public void onEquippedTick(Player player, ItemStack stack) {
		int maturity = MorphlingItem.getMaturityLevel(stack);

		// Base effect: Parasitic Anchor (absorption hearts, amplifier = maturity)
		if (!player.hasEffect(EffectInit.parasitic_anchor.get())) {
			player.addEffect(new MobEffectInstance(EffectInit.parasitic_anchor.get(),
					100, maturity, false, true, true));
		}

		// Mature (3+): Blood Tide — share absorption with nearby allies
		if (maturity >= 3 && !player.level().isClientSide) {
			if (player.getAbsorptionAmount() > 0 && player.tickCount % 40 == 0) {
				double radius = 8.0;
				AABB area = player.getBoundingBox().inflate(radius);
				List<Player> nearbyPlayers = player.level().getEntitiesOfClass(Player.class, area,
						p -> p != player);
				float shareAmount = 1.0f + (maturity - 3) * 0.5f; // 1.0 at Mature, 1.5 at Apex
				float maxAllyAbsorption = 4.0f;
				for (Player ally : nearbyPlayers) {
					if (ally.getAbsorptionAmount() < maxAllyAbsorption) {
						ally.setAbsorptionAmount(Math.min(ally.getAbsorptionAmount() + shareAmount,
								maxAllyAbsorption));
					}
				}
			}
		}
	}

	@Override
	public void onEquippedAttack(Player player, ItemStack stack, LivingEntity target, float amount) {
		int maturity = MorphlingItem.getMaturityLevel(stack);

		// Developing (2+): Latching Bite — apply Mining Fatigue on melee hit
		if (maturity >= 2) {
			int fatigueDuration = 60 + (maturity - 2) * 40; // 3s at Developing, 5s Mature, 7s Apex
			target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN,
					fatigueDuration, 0, true, true, true));
		}

		// Apex (4): Exsanguinating Grasp — drain health from target
		if (maturity >= 4 && !player.level().isClientSide) {
			float drainAmount = 2.0f;
			target.hurt(player.damageSources().magic(), drainAmount);
			player.heal(drainAmount);
		}
	}

	@Override
	public List<Component> getMaturityBonusDescriptions(int currentMaturity) {
		List<Component> list = new ArrayList<>();
		list.add(MorphlingItem.maturityBonusLine("Latching Bite (Mining Fatigue on melee hit)", 2, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Blood Tide (Share absorption with allies)", 3, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Exsanguinating Grasp (Drain health on hit)", 4, currentMaturity));
		return list;
	}

}
