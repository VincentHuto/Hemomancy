package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

/**
 * Urchin morphling that grants passive armor and thorns damage reflection
 * by applying the Spined Barricade effect while equipped. Maturity level
 * scales the armor bonus. Prefers FERRIC (iron-rich minerals harden the
 * spines) with CONGEATIO as secondary (cold deep-sea calcification).
 *
 * Maturity bonuses (unique reactive abilities):
 * - Developing (2): Spine Lash — when damaged by melee, reflect a portion
 *   back as thorns and apply Slowness (barbed spines snag the attacker)
 * - Mature (3): Tidal Anchor — periodically push away nearby hostile mobs
 *   with a defensive knockback pulse (like an urchin expelling water)
 * - Apex (4): Calcareous Shell — after taking a heavy hit (>6 damage),
 *   gain brief Resistance II and root in place (hardened calcite shell)
 */
public class UrchinMorphlingItem extends MorphlingItem {

	/** Cooldown in ticks between Tidal Anchor triggers (15 seconds). */
	private static final int TIDAL_ANCHOR_COOLDOWN = 300;
	/** Cooldown in ticks between Calcareous Shell triggers (20 seconds). */
	private static final int CALCAREOUS_SHELL_COOLDOWN = 400;

	public UrchinMorphlingItem(Properties prop) {
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
	public void use(Player playerIn, InteractionHand handIn, ItemStack itemStack, Level worldIn) {
		if (!MorphlingItem.tryBeginPrimalAbility(playerIn, itemStack, "ReefheartBastion",
				480.0, 900, 260, 0)) return;
		playerIn.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,
				220, 2, true, true, true));
		playerIn.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
				220, 4, true, true, true));
		playerIn.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,
				220, 0, true, true, true));
		AABB area = playerIn.getBoundingBox().inflate(7.0);
		for (Monster mob : worldIn.getEntitiesOfClass(Monster.class, area, Monster::isAlive)) {
			mob.hurt(playerIn.damageSources().thorns(playerIn), 6.0f);
			double dx = mob.getX() - playerIn.getX();
			double dz = mob.getZ() - playerIn.getZ();
			double dist = Math.sqrt(dx * dx + dz * dz);
			if (dist > 0) {
				mob.push(dx / dist * 1.2, 0.3, dz / dist * 1.2);
			}
		}
	}

	@Override
	public void onEquippedTick(Player player, ItemStack stack) {
		int maturity = MorphlingItem.getMaturityLevel(stack);

		// Base effect: Spined Barricade (armor via attribute, amplifier = maturity)
		if (!player.hasEffect(EffectInit.spined_barricade)) {
			player.addEffect(new MobEffectInstance(EffectInit.spined_barricade,
					100, maturity, false, true, true));
		}

		// Mature (3+): Tidal Anchor — periodically push away nearby hostile mobs
		if (maturity >= 3 && !player.level().isClientSide) {
			long lastAnchor = getLastAbilityTick(stack, "TidalAnchor");
			long now = player.level().getGameTime();
			if (now - lastAnchor >= TIDAL_ANCHOR_COOLDOWN) {
				double radius = 5.0;
				AABB area = player.getBoundingBox().inflate(radius);
				List<net.minecraft.world.entity.monster.Monster> hostiles =
						player.level().getEntitiesOfClass(
								net.minecraft.world.entity.monster.Monster.class, area);
				if (!hostiles.isEmpty()) {
					setLastAbilityTick(stack, "TidalAnchor", now);
					double knockbackStrength = 0.8 + (maturity - 3) * 0.4;
					for (net.minecraft.world.entity.monster.Monster mob : hostiles) {
						double dx = mob.getX() - player.getX();
						double dz = mob.getZ() - player.getZ();
						double dist = Math.sqrt(dx * dx + dz * dz);
						if (dist > 0) {
							mob.push(dx / dist * knockbackStrength, 0.25,
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

		// Developing (2+): Spine Lash — thorns + Slowness to melee attackers
		if (maturity >= 2 && source.getEntity() instanceof LivingEntity attacker) {
			float thornsPct = 0.25f + (maturity - 2) * 0.10f; // 25% at Developing, 35% Mature, 45% Apex
			float thornsDamage = amount * thornsPct;
			if (thornsDamage > 0.5f) {
				attacker.hurt(player.damageSources().thorns(player), thornsDamage);
			}
			int slowDuration = 30 + (maturity - 2) * 15; // 1.5s at Developing, 2.25s Mature, 3s Apex
			attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
					slowDuration, 0, true, true, true));
		}

		// Apex (4): Calcareous Shell — brief Resistance after heavy hit
		if (maturity >= 4 && amount >= 6.0f) {
			long lastShell = getLastAbilityTick(stack, "CalcareousShell");
			long now = player.level().getGameTime();
			if (now - lastShell >= CALCAREOUS_SHELL_COOLDOWN) {
				setLastAbilityTick(stack, "CalcareousShell", now);
				// Resistance II for 3 seconds
				player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,
						60, 1, true, true, true));
				// Brief Slowness on self (rooted in place)
				player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
						40, 2, true, true, true));
			}
		}
	}

	@Override
	public List<Component> getMaturityBonusDescriptions(int currentMaturity) {
		List<Component> list = new ArrayList<>();
		list.add(MorphlingItem.maturityBonusLine("Spine Lash (Thorns + Slow attackers)", 2, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Tidal Anchor (Push away hostile mobs)", 3, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Calcareous Shell (Resistance after heavy hit)", 4, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Reefheart Bastion (Staff active roots into a reflecting ritual anchor)", 5, currentMaturity));
		return list;
	}

}
