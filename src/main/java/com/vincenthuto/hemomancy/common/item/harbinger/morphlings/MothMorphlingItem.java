package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import java.util.ArrayList;
import java.util.List;

import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.init.EffectInit;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * Moth morphling that subtly deflects projectiles by applying the
 * Luminous Dissipation effect while equipped. Maturity level scales the
 * knockback resistance. Prefers LUX (light fuels the moth's luminescence)
 * with DUCTILIS as secondary (flexibility aids evasion).
 *
 * Maturity bonuses (unique reactive abilities):
 * - Developing (2): Dustwing Trail — sprinting blinds nearby hostiles with
 *   luminous dust particles
 * - Mature (3): Phototaxis Pulse — when damaged, emit a flash of light that
 *   applies Blindness and Slowness to the attacker and nearby hostiles
 * - Apex (4): Cocoon Rebirth — upon taking lethal damage, cancel death and
 *   restore health by consuming blood volume (10 minute cooldown)
 */
public class MothMorphlingItem extends MorphlingItem {

	/** Cooldown in ticks between Phototaxis Pulse triggers (4 seconds). */
	private static final int PHOTOTAXIS_COOLDOWN = 80;
	/** Cooldown in ticks between Cocoon Rebirth triggers (10 minutes). */
	private static final int COCOON_REBIRTH_COOLDOWN = 12000;

	public MothMorphlingItem(Properties prop) {
		super(prop);
	}

	@Override
	public EnumBloodTendency getPreferredTendency() {
		return EnumBloodTendency.LUX;
	}

	@Override
	public EnumBloodTendency getSecondaryTendency() {
		return EnumBloodTendency.DUCTILIS;
	}

	@Override
	public void onEquippedTick(Player player, ItemStack stack) {
		int maturity = MorphlingItem.getMaturityLevel(stack);

		// Base effect: Luminous Dissipation (knockback resistance, amplifier = maturity)
		if (!player.hasEffect(EffectInit.luminous_dissipation)) {
			player.addEffect(new MobEffectInstance(EffectInit.luminous_dissipation,
					100, maturity, false, true, true));
		}

		// Developing (2+): Dustwing Trail — blind nearby hostiles while sprinting
		if (maturity >= 2 && !player.level().isClientSide && player.isSprinting()) {
			if (player.tickCount % 20 == 0) { // Check every second while sprinting
				double radius = 4.0;
				AABB area = player.getBoundingBox().inflate(radius);
				List<Monster> hostiles = player.level().getEntitiesOfClass(Monster.class, area);
				int blindDuration = 30 + (maturity - 2) * 10; // 1.5s at Developing, 2s Mature, 2.5s Apex
				for (Monster mob : hostiles) {
					mob.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,
							blindDuration, 0, true, true, true));
				}
			}
		}
	}

	@Override
	public void onEquippedHurt(Player player, ItemStack stack, DamageSource source, float amount) {
		int maturity = MorphlingItem.getMaturityLevel(stack);

		// Mature (3+): Phototaxis Pulse — flash of light blinds attacker and nearby hostiles
		if (maturity >= 3 && source.getEntity() instanceof LivingEntity attacker) {
			long lastPulse = getLastAbilityTick(stack, "PhototaxisPulse");
			long now = player.level().getGameTime();
			if (now - lastPulse >= PHOTOTAXIS_COOLDOWN) {
				setLastAbilityTick(stack, "PhototaxisPulse", now);

				int blindDuration = 60 + (maturity - 3) * 20; // 3s at Mature, 4s at Apex
				attacker.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,
						blindDuration, 0, true, true, true));
				attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
						blindDuration, 1, true, true, true));

				// Also blind nearby hostiles in radius
				double radius = 6.0;
				AABB area = player.getBoundingBox().inflate(radius);
				List<Monster> hostiles = player.level().getEntitiesOfClass(Monster.class, area);
				for (Monster mob : hostiles) {
					mob.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,
							blindDuration / 2, 0, true, true, true));
				}
			}
		}

		// Apex (4): Cocoon Rebirth — prevent lethal damage by consuming blood
		if (maturity >= 4 && player.getHealth() - amount <= 0) {
			long lastCocoon = getLastAbilityTick(stack, "CocoonRebirth");
			long now = player.level().getGameTime();
			if (now - lastCocoon >= COCOON_REBIRTH_COOLDOWN) {
				HemoCapabilityAccess.getBloodVolume(player).ifPresent(volume -> {
					if (!volume.isActive()) return;
					double bloodCost = 500.0;
					if (volume.getBloodVolume() > bloodCost) {
						volume.drain(bloodCost);
						player.setHealth(8.0f); // Restore 4 hearts
						player.invulnerableTime = 40; // Brief invulnerability
						BloodVolumeEvents.syncVolume((ServerPlayer) player, volume);
						setLastAbilityTick(stack, "CocoonRebirth", now);
					}
				});
			}
		}
	}

	@Override
	public List<Component> getMaturityBonusDescriptions(int currentMaturity) {
		List<Component> list = new ArrayList<>();
		list.add(MorphlingItem.maturityBonusLine("Dustwing Trail (Blind hostiles while sprinting)", 2, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Phototaxis Pulse (Flash blinds attacker on hit)", 3, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Cocoon Rebirth (Prevent death using blood)", 4, currentMaturity));
		return list;
	}

}
