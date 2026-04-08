package com.vincenthuto.hemomancy.common.item.morphlings;

import java.util.ArrayList;
import java.util.List;

import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.EffectInit;

import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * Coral morphling that passively regenerates health with bonus healing in
 * water, by applying the Tidal Mending effect while equipped. Also grants
 * water breathing. Maturity level scales the healing rate. Prefers CONGEATIO
 * (cold water nourishes the coral) with ANIMUS as secondary (life force
 * aids regeneration).
 *
 * Maturity bonuses (unique reactive abilities):
 * - Developing (2): Reef Barbs — when damaged by a melee attacker, apply
 *   Slowness and minor thorns damage (stinging coral barbs)
 * - Mature (3): Tidal Surge — periodically push nearby hostile mobs away
 *   with a water-surge knockback wave
 * - Apex (4): Polyp Colony — on kill, heal all nearby allies for a burst
 *   of regeneration (symbiotic colony healing)
 */
public class CoralMorphlingItem extends MorphlingItem {

	/** Cooldown in ticks between Tidal Surge triggers (15 seconds). */
	private static final int TIDAL_SURGE_COOLDOWN = 300;

	public CoralMorphlingItem(Properties prop) {
		super(prop);
	}

	@Override
	public EnumBloodTendency getPreferredTendency() {
		return EnumBloodTendency.CONGEATIO;
	}

	@Override
	public EnumBloodTendency getSecondaryTendency() {
		return EnumBloodTendency.ANIMUS;
	}

	@Override
	public void onEquippedTick(Player player, ItemStack stack) {
		int maturity = MorphlingItem.getMaturityLevel(stack);

		// Base effect: Tidal Mending (regen, doubled in water, amplifier = maturity)
		if (!player.hasEffect(EffectInit.tidal_mending.get())) {
			player.addEffect(new MobEffectInstance(EffectInit.tidal_mending.get(),
					100, maturity, false, true, true));
		}

		// Always grant Water Breathing while equipped
		if (!player.hasEffect(MobEffects.WATER_BREATHING)) {
			player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING,
					200, 0, true, false, true));
		}

		// Mature (3+): Tidal Surge — periodically push away nearby hostile mobs
		if (maturity >= 3 && !player.level().isClientSide) {
			long lastSurge = getLastAbilityTick(stack, "TidalSurge");
			long now = player.level().getGameTime();
			if (now - lastSurge >= TIDAL_SURGE_COOLDOWN) {
				double radius = 6.0;
				AABB area = player.getBoundingBox().inflate(radius);
				List<net.minecraft.world.entity.monster.Monster> hostiles =
						player.level().getEntitiesOfClass(
								net.minecraft.world.entity.monster.Monster.class, area);
				if (!hostiles.isEmpty()) {
					setLastAbilityTick(stack, "TidalSurge", now);
					double knockbackStrength = 1.0 + (maturity - 3) * 0.5;
					for (net.minecraft.world.entity.monster.Monster mob : hostiles) {
						double dx = mob.getX() - player.getX();
						double dz = mob.getZ() - player.getZ();
						double dist = Math.sqrt(dx * dx + dz * dz);
						if (dist > 0) {
							mob.push(dx / dist * knockbackStrength, 0.3,
									dz / dist * knockbackStrength);
						}
					}
				}
			}
		}
	}

	@Override
	public void onEquippedHurt(Player player, ItemStack stack, DamageSource source, float amount) {
		int maturity = MorphlingItem.getMaturityLevel(stack);

		// Developing (2+): Reef Barbs — slow and damage melee attackers
		if (maturity >= 2 && source.getEntity() instanceof LivingEntity attacker) {
			int slowDuration = 40 + (maturity - 2) * 20; // 2s at Developing, 3s Mature, 4s Apex
			attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
					slowDuration, 0, true, true, true));
			// Minor thorns damage
			float thornsDamage = 1.0f + (maturity - 2) * 0.5f;
			attacker.hurt(player.damageSources().thorns(player), thornsDamage);
		}
	}

	@Override
	public void onEquippedKill(Player player, ItemStack stack, LivingEntity victim) {
		int maturity = MorphlingItem.getMaturityLevel(stack);

		// Apex (4): Polyp Colony — heal nearby allies on kill
		if (maturity >= 4 && !player.level().isClientSide) {
			double radius = 8.0;
			AABB area = player.getBoundingBox().inflate(radius);
			List<Player> nearbyPlayers = player.level().getEntitiesOfClass(Player.class, area);
			for (Player ally : nearbyPlayers) {
				ally.addEffect(new MobEffectInstance(MobEffects.REGENERATION,
						60, 1, true, true, true)); // Regen II for 3s
			}
		}
	}

	@Override
	public List<Component> getMaturityBonusDescriptions(int currentMaturity) {
		List<Component> list = new ArrayList<>();
		list.add(MorphlingItem.maturityBonusLine("Reef Barbs (Slow & damage attackers)", 2, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Tidal Surge (Push away hostile mobs)", 3, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Polyp Colony (Heal allies on kill)", 4, currentMaturity));
		return list;
	}

}
