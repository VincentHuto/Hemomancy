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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * Spider morphling that passively repairs vascular damage by applying the
 * Arachnid Anastomosis effect while equipped. Maturity level scales the
 * vascular repair rate. Prefers TENEBRIS (darkness/shadow fuels the web)
 * with LUX as secondary (light reveals and mends hidden damage).
 *
 * Maturity bonuses:
 * - Developing (2): Nearby mobs slowed (web snare)
 * - Mature (3): Slow Falling (silk parachute)
 * - Apex (4): Damage Resistance (web-reinforced body)
 */
public class SpiderMorphlingItem extends MorphlingItem {

	public SpiderMorphlingItem(Properties prop) {
		super(prop);
	}

	@Override
	public EnumBloodTendency getPreferredTendency() {
		return EnumBloodTendency.TENEBRIS;
	}

	@Override
	public EnumBloodTendency getSecondaryTendency() {
		return EnumBloodTendency.LUX;
	}

	@Override
	public void onEquippedTick(Player player, ItemStack stack) {
		int maturity = MorphlingItem.getMaturityLevel(stack);

		// Base effect: Arachnid Anastomosis (vascular repair, amplifier = maturity)
		// Duration of 100 ticks (5 sec) exceeds the drain interval so the effect stays
		// active while equipped, but expires quickly if the morphling is removed.
		if (!player.hasEffect(EffectInit.arachnid_anastomosis.get())) {
			player.addEffect(new MobEffectInstance(EffectInit.arachnid_anastomosis.get(),
					100, maturity, false, true, true));
		}

		// Developing (2+): Web snare — slow nearby mobs
		if (maturity >= 2 && !player.level().isClientSide) {
			Level level = player.level();
			double radius = 5.0;
			AABB area = player.getBoundingBox().inflate(radius);
			List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class, area,
					e -> e != player && !(e instanceof Player));
			for (LivingEntity mob : nearby) {
				if (!mob.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
					mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
							100, 0, true, true, true));
				}
			}
		}

		// Mature (3+): Slow Falling — silk parachute
		if (maturity >= 3) {
			if (!player.hasEffect(MobEffects.SLOW_FALLING)) {
				player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING,
						100, 0, true, false, true));
			}
		}

		// Apex (4): Damage Resistance — web-reinforced body
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
		list.add(MorphlingItem.maturityBonusLine("Web Snare (Slow Nearby Mobs)", 2, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Slow Falling (Silk Parachute)", 3, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Damage Resistance (Web Armor)", 4, currentMaturity));
		return list;
	}

}
