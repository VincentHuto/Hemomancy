package com.vincenthuto.hemomancy.common.item.morphlings;

import java.util.ArrayList;
import java.util.List;

import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.EffectInit;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * Pests morphling that damages nearby hostile mobs by applying the
 * Verminous Aura effect while equipped. Maturity level scales the damage
 * radius and damage dealt. Prefers FLAMMEUS (fervent heat drives the swarm)
 * with TENEBRIS as secondary (darkness harbors vermin).
 *
 * Maturity bonuses (unique reactive abilities):
 * - Developing (2): Swarm Retaliation — when damaged, spawn tracking pest
 *   projectiles that hunt the attacker
 * - Mature (3): Carrion Harvest — kills grant bonus XP from the swarm
 *   scavenging nutrients
 * - Apex (4): Plague Burst — when health drops below 25%, emit a massive
 *   AoE burst that withers all nearby hostiles
 */
public class PestsMorphlingItem extends MorphlingItem {

	/** Cooldown in ticks between Swarm Retaliation triggers (1 second). */
	private static final int SWARM_RETALIATION_COOLDOWN = 20;
	/** Cooldown in ticks between Plague Burst triggers (30 seconds). */
	private static final int PLAGUE_BURST_COOLDOWN = 600;

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

		// Apex (4): Plague Burst — emergency AoE Wither when health is critically low
		if (maturity >= 4 && !player.level().isClientSide) {
			if (player.getHealth() <= player.getMaxHealth() * 0.25f) {
				long lastBurst = getLastAbilityTick(stack, "PlagueBurst");
				long now = player.level().getGameTime();
				if (now - lastBurst >= PLAGUE_BURST_COOLDOWN) {
					setLastAbilityTick(stack, "PlagueBurst", now);

					double radius = 8.0;
					AABB area = player.getBoundingBox().inflate(radius);
					List<Monster> hostiles = player.level().getEntitiesOfClass(Monster.class, area);
					for (Monster mob : hostiles) {
						mob.addEffect(new MobEffectInstance(MobEffects.WITHER,
								100, 1, true, true, true));
						mob.hurt(player.damageSources().magic(), 6.0F);
					}
				}
			}
		}
	}

	@Override
	public void onEquippedHurt(Player player, ItemStack stack, DamageSource source, float amount) {
		int maturity = MorphlingItem.getMaturityLevel(stack);

		// Developing (2+): Swarm Retaliation — spawn tracking pests at attacker
		if (maturity >= 2 && source.getEntity() instanceof LivingEntity attacker) {
			long lastSwarm = getLastAbilityTick(stack, "SwarmRetaliation");
			long now = player.level().getGameTime();
			if (now - lastSwarm >= SWARM_RETALIATION_COOLDOWN) {
				setLastAbilityTick(stack, "SwarmRetaliation", now);

				int pestCount = 1 + (maturity - 2); // 1 at Developing, 2 at Mature, 3 at Apex
				for (int i = 0; i < pestCount; i++) {
					var pest = new com.vincenthuto.hemomancy.common.entity.projectile.TrackingPestsEntity(
							player, false);
					pest.setTarget(attacker);
					player.level().addFreshEntity(pest);
				}
			}
		}
	}

	@Override
	public void onEquippedKill(Player player, ItemStack stack, LivingEntity victim) {
		int maturity = MorphlingItem.getMaturityLevel(stack);

		// Mature (3+): Carrion Harvest — kills grant bonus XP
		if (maturity >= 3 && player.level() instanceof ServerLevel serverLevel) {
			int bonusXp = 3 + (maturity - 3) * 3; // 3 at Mature, 6 at Apex
			// Award bonus XP orbs at the victim's position (on top of normal drops)
			net.minecraft.world.entity.ExperienceOrb.award(serverLevel,
					victim.position(), bonusXp);
		}
	}

	@Override
	public List<Component> getMaturityBonusDescriptions(int currentMaturity) {
		List<Component> list = new ArrayList<>();
		list.add(MorphlingItem.maturityBonusLine("Swarm Retaliation (Pests hunt your attacker)", 2, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Carrion Harvest (Bonus XP from kills)", 3, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Plague Burst (AoE Wither at low health)", 4, currentMaturity));
		return list;
	}

}
