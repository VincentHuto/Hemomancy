package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * Centipede morphling that grants poison immunity and a speed boost by
 * applying the Venomous Resilience effect while equipped. Maturity level
 * scales the speed bonus. Prefers CONGEATIO (cold hardens the carapace)
 * with FERRIC as secondary (iron strengthens the segments).
 *
 * Maturity bonuses (unique reactive abilities):
 * - Developing (2): Burrowing Strike — melee attacks have a chance to
 *   bypass armor (applies Weakness to simulate armor penetration)
 * - Mature (3): Segmented Defense — when taking damage above 4, excess
 *   damage is converted to a bleed-over-time instead of taken instantly
 * - Apex (4): Myriapod Swarm — when health drops below 40%, gain brief
 *   Invisibility and Speed burst to escape danger
 */
public class CentipedeMorphlingItem extends MorphlingItem {

	/** Cooldown in ticks between Myriapod Swarm triggers (30 seconds). */
	private static final int MYRIAPOD_SWARM_COOLDOWN = 600;

	public CentipedeMorphlingItem(Properties prop) {
		super(prop);
	}

	@Override
	public EnumBloodTendency getPreferredTendency() {
		return EnumBloodTendency.CONGEATIO;
	}

	@Override
	public EnumBloodTendency getSecondaryTendency() {
		return EnumBloodTendency.FERRIC;
	}

	@Override
	public void use(Player playerIn, InteractionHand handIn, ItemStack itemStack, Level worldIn) {
		if (!MorphlingItem.tryBeginPrimalAbility(playerIn, itemStack, "HundredfoldMolt",
				300.0, 700, 180, 0)) return;
		playerIn.removeEffect(MobEffects.POISON);
		playerIn.removeEffect(MobEffects.WITHER);
		playerIn.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
		playerIn.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY,
				120, 0, true, false, true));
		playerIn.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,
				140, 3, true, true, true));
		playerIn.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,
				60, 4, true, false, true));
		playerIn.invulnerableTime = Math.max(playerIn.invulnerableTime, 40);
		if (worldIn instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(ParticleTypes.POOF,
					playerIn.getX(), playerIn.getY() + 0.4, playerIn.getZ(),
					30, 0.45, 0.35, 0.45, 0.05);
		}
	}

	@Override
	public void onEquippedTick(Player player, ItemStack stack) {
		int maturity = MorphlingItem.getMaturityLevel(stack);

		// Base effect: Venomous Resilience (speed + poison immunity, amplifier = maturity)
		int amplifier = Math.min(maturity, 2);
		if (!player.hasEffect(EffectInit.venomous_resilience)) {
			player.addEffect(new MobEffectInstance(EffectInit.venomous_resilience,
					100, maturity, false, true, true));
		}
	}

	@Override
	public void onEquippedAttack(Player player, ItemStack stack, LivingEntity target, float amount) {
		int maturity = MorphlingItem.getMaturityLevel(stack);

		// Developing (2+): Burrowing Strike — apply Weakness to simulate armor bypass
		if (maturity >= 2) {
			int weaknessDuration = 40 + (maturity - 2) * 20; // 2s at Developing, 3s Mature, 4s Apex
			int weaknessAmplifier = (maturity >= 4) ? 1 : 0; // Weakness II at Apex
			target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,
					weaknessDuration, weaknessAmplifier, true, true, true));
		}
	}

	@Override
	public void onEquippedHurt(Player player, ItemStack stack, DamageSource source, float amount) {
		int maturity = MorphlingItem.getMaturityLevel(stack);

		// Mature (3+): Segmented Defense — compensate excess damage with regeneration
		// (Grants Regeneration to partially offset heavy hits, spreading recovery over time)
		if (maturity >= 3 && amount > 4.0f) {
			// The excess is already taken, but we grant Regeneration to partially compensate
			float excess = amount - 4.0f;
			int regenDuration = (int) (excess * 10); // Scale duration with excess
			player.addEffect(new MobEffectInstance(MobEffects.REGENERATION,
					regenDuration, 0, true, true, true));
		}

		// Apex (4): Myriapod Swarm — escape burst when critically low
		if (maturity >= 4 && player.getHealth() <= player.getMaxHealth() * 0.4f) {
			long lastSwarm = getLastAbilityTick(stack, "MyriapodSwarm");
			long now = player.level().getGameTime();
			if (now - lastSwarm >= MYRIAPOD_SWARM_COOLDOWN) {
				setLastAbilityTick(stack, "MyriapodSwarm", now);

				// Brief invisibility and speed burst
				player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY,
						40, 0, true, false, true)); // 2 seconds
				player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,
						60, 2, true, true, true)); // 3 seconds, Speed III
			}
		}
	}

	@Override
	public List<Component> getMaturityBonusDescriptions(int currentMaturity) {
		List<Component> list = new ArrayList<>();
		list.add(MorphlingItem.maturityBonusLine("Burrowing Strike (Weaken targets on hit)", 2, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Segmented Defense (Regen from heavy hits)", 3, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Myriapod Swarm (Invisibility escape at low HP)", 4, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Hundredfold Molt (Staff active sheds danger and vanishes)", 5, currentMaturity));
		return list;
	}

}
