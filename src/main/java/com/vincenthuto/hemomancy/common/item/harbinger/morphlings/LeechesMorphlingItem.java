package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Leeches morphling that passively replenishes blood volume by applying the
 * Sanguine Siphon effect while equipped. Maturity level scales the blood
 * fill rate. Prefers ANIMUS (life force feeds the leeches) with
 * CONGEATIO as secondary (cold-blooded affinity).
 *
 * Maturity bonuses (unique reactive abilities):
 * - Developing (2): Life Steal — melee attacks heal the player for a portion
 *   of damage dealt (leeches siphon life from wounds)
 * - Mature (3): Blood Transfusion — when health drops below 30%, the leeches
 *   automatically consume blood volume to rapidly heal the player
 * - Apex (4): Sanguine Frenzy — bonus melee damage that scales inversely
 *   with current health (the hungrier the leeches, the more ferocious);
 *   additionally, attacking targets below 20% health triggers an
 *   Exsanguinate execution — dealing 8 bonus damage, healing the player,
 *   and recovering blood volume (vampiric finishing blow)
 */
public class LeechesMorphlingItem extends MorphlingItem {

	/** Cooldown in ticks between Blood Transfusion triggers (10 seconds). */
	private static final int TRANSFUSION_COOLDOWN = 200;
	/** Cooldown in ticks between Exsanguinate triggers (10 seconds). */
	private static final int EXSANGUINATE_COOLDOWN = 200;

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
		if (!player.hasEffect(EffectInit.sanguine_siphon)) {
			player.addEffect(new MobEffectInstance(EffectInit.sanguine_siphon,
					100, maturity, false, true, true));
		}

		// Mature (3+): Blood Transfusion — auto-heal by spending blood volume when low
		if (maturity >= 3 && !player.level().isClientSide) {
			if (player.getHealth() <= player.getMaxHealth() * 0.3f && player.getHealth() > 0) {
				long lastTransfusion = getLastAbilityTick(stack, "BloodTransfusion");
				long now = player.level().getGameTime();
				if (now - lastTransfusion >= TRANSFUSION_COOLDOWN) {
					HemoCapabilityAccess.getBloodVolume(player).ifPresent(volume -> {
						if (!volume.isActive()) return;
						double bloodCost = 200.0; // Spend blood to heal
						if (volume.getBloodVolume() > bloodCost) {
							volume.drain(bloodCost);
							float healAmount = 6.0f + (maturity - 3) * 3.0f; // 6 at Mature, 9 at Apex
							player.heal(healAmount);
							BloodVolumeEvents.syncVolume((ServerPlayer) player, volume);
							setLastAbilityTick(stack, "BloodTransfusion", now);
						}
					});
				}
			}
		}
	}

	@Override
	public void onEquippedAttack(Player player, ItemStack stack, LivingEntity target, float amount) {
		int maturity = MorphlingItem.getMaturityLevel(stack);

		// Developing (2+): Life Steal — heal from melee damage dealt
		if (maturity >= 2) {
			float lifeStealPct = 0.15f + (maturity - 2) * 0.05f; // 15% at Developing, 20% Mature, 25% Apex
			float healAmount = amount * lifeStealPct;
			if (healAmount > 0) {
				player.heal(healAmount);
			}
		}

		// Apex (4): Sanguine Frenzy — bonus damage scaling with missing health
		// PLUS Exsanguinate: execute targets below 20% health
		if (maturity >= 4 && !player.level().isClientSide) {
			// Missing health damage scaling (kept from original)
			float missingHealthPct = 1.0f - (player.getHealth() / player.getMaxHealth());
			float bonusDamage = missingHealthPct * 6.0f; // Up to 6 bonus damage at 0% health
			if (bonusDamage > 0.5f) {
				target.hurt(player.damageSources().magic(), bonusDamage);
			}

			// Exsanguinate: execute weakened targets for bonus heal + blood volume
			if (target.getHealth() <= target.getMaxHealth() * 0.2f) {
				long lastExec = getLastAbilityTick(stack, "Exsanguinate");
				long now = player.level().getGameTime();
				if (now - lastExec >= EXSANGUINATE_COOLDOWN) {
					setLastAbilityTick(stack, "Exsanguinate", now);
					// Execute damage burst
					target.hurt(player.damageSources().magic(), 8.0f);
					// Heal player from the feeding frenzy
					player.heal(4.0f);
					// Recover blood volume
					HemoCapabilityAccess.getBloodVolume(player).ifPresent(volume -> {
						if (!volume.isActive()) return;
						volume.fill(100.0);
						BloodVolumeEvents.syncVolume((ServerPlayer) player, volume);
					});
				}
			}
		}
	}

	@Override
	public List<Component> getMaturityBonusDescriptions(int currentMaturity) {
		List<Component> list = new ArrayList<>();
		list.add(MorphlingItem.maturityBonusLine("Life Steal (Heal from melee damage)", 2, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Blood Transfusion (Emergency heal using blood)", 3, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Sanguine Frenzy (Missing HP damage + execute low targets)", 4, currentMaturity));
		return list;
	}

}
