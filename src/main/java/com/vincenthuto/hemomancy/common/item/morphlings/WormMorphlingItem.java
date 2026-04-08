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
 * Worm morphling that grants increased attack speed (Haste-like) and bonus
 * regeneration underground by applying the Subterranean Vitality effect while
 * equipped. Maturity level scales the speed bonus. Prefers FERRIC (iron-rich
 * soil nourishes the worm) with MORTEM as secondary (decay enriches the earth).
 *
 * Maturity bonuses (unique reactive abilities):
 * - Developing (2): Earthen Camouflage — when sneaking on a solid block, gain
 *   brief Invisibility (worm blends into the ground)
 * - Mature (3): Regenerative Split — when health drops below 40%, instantly
 *   heal a burst of health (cooldown-gated emergency regen)
 * - Apex (4): Tremorsense — all living entities within 32 blocks glow
 *   (revealed through walls, enhanced detection range)
 */
public class WormMorphlingItem extends MorphlingItem {

	/** Cooldown in ticks between Regenerative Split triggers (30 seconds). */
	private static final int REGEN_SPLIT_COOLDOWN = 600;

	public WormMorphlingItem(Properties prop) {
		super(prop);
	}

	@Override
	public EnumBloodTendency getPreferredTendency() {
		return EnumBloodTendency.FERRIC;
	}

	@Override
	public EnumBloodTendency getSecondaryTendency() {
		return EnumBloodTendency.MORTEM;
	}

	@Override
	public void onEquippedTick(Player player, ItemStack stack) {
		int maturity = MorphlingItem.getMaturityLevel(stack);

		// Base effect: Subterranean Vitality (attack speed + underground regen)
		if (!player.hasEffect(EffectInit.subterranean_vitality.get())) {
			player.addEffect(new MobEffectInstance(EffectInit.subterranean_vitality.get(),
					100, maturity, false, true, true));
		}

		// Developing (2+): Earthen Camouflage — Invisibility while sneaking on ground
		if (maturity >= 2 && !player.level().isClientSide) {
			if (player.isShiftKeyDown() && player.onGround()) {
				if (!player.hasEffect(MobEffects.INVISIBILITY)) {
					player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY,
							30, 0, true, false, true)); // 1.5 seconds, refreshed while sneaking
				}
			}
		}

		// Apex (4): Tremorsense — reveal all entities in large radius
		if (maturity >= 4 && !player.level().isClientSide) {
			if (player.tickCount % 40 == 0) { // Check every 2 seconds
				double radius = 32.0;
				AABB area = player.getBoundingBox().inflate(radius);
				List<LivingEntity> nearbyEntities = player.level().getEntitiesOfClass(
						LivingEntity.class, area, e -> e != player);
				for (LivingEntity target : nearbyEntities) {
					if (!target.hasEffect(MobEffects.GLOWING)) {
						// Sneaking mobs detected at half range
						double dist = player.distanceTo(target);
						double effectiveRange = target.isShiftKeyDown() ? radius / 2 : radius;
						if (dist <= effectiveRange) {
							target.addEffect(new MobEffectInstance(MobEffects.GLOWING,
									45, 0, true, false, false));
						}
					}
				}
			}
		}
	}

	@Override
	public void onEquippedHurt(Player player, ItemStack stack, DamageSource source, float amount) {
		int maturity = MorphlingItem.getMaturityLevel(stack);

		// Mature (3+): Regenerative Split — emergency heal when critically low
		if (maturity >= 3 && player.getHealth() <= player.getMaxHealth() * 0.4f) {
			long lastSplit = getLastAbilityTick(stack, "RegenSplit");
			long now = player.level().getGameTime();
			if (now - lastSplit >= REGEN_SPLIT_COOLDOWN) {
				setLastAbilityTick(stack, "RegenSplit", now);

				float healAmount = 6.0f + (maturity - 3) * 3.0f; // 6 at Mature, 9 at Apex
				player.heal(healAmount);
			}
		}
	}

	@Override
	public List<Component> getMaturityBonusDescriptions(int currentMaturity) {
		List<Component> list = new ArrayList<>();
		list.add(MorphlingItem.maturityBonusLine("Earthen Camouflage (Invisible while sneaking)", 2, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Regenerative Split (Emergency heal at low HP)", 3, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Tremorsense (Reveal all entities in huge radius)", 4, currentMaturity));
		return list;
	}

}
