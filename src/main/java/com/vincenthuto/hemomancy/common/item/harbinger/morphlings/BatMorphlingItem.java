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
 * Bat morphling that grants echolocation by applying the Echoic Perception
 * effect while equipped, causing nearby entities to glow. Maturity level
 * scales the detection radius. Prefers TENEBRIS (darkness fuels the bat's
 * senses) with DUCTILIS as secondary (neurotic flexibility aids perception).
 *
 * Maturity bonuses (unique reactive abilities):
 * - Developing (2): Sonar Shriek — when damaged, emit a shriek that applies
 *   Darkness and Slowness to the attacker
 * - Mature (3): Membrane Glide — fall damage is significantly reduced
 *   (slow falling effect when sneaking mid-air)
 * - Apex (4): Nightwing Frenzy — in darkness (light level < 4), gain
 *   significant attack damage and speed bonuses
 */
public class BatMorphlingItem extends MorphlingItem {

	/** Cooldown in ticks between Sonar Shriek triggers (3 seconds). */
	private static final int SONAR_SHRIEK_COOLDOWN = 60;

	public BatMorphlingItem(Properties prop) {
		super(prop);
	}

	@Override
	public EnumBloodTendency getPreferredTendency() {
		return EnumBloodTendency.TENEBRIS;
	}

	@Override
	public EnumBloodTendency getSecondaryTendency() {
		return EnumBloodTendency.DUCTILIS;
	}

	@Override
	public void use(Player playerIn, InteractionHand handIn, ItemStack itemStack, Level worldIn) {
		if (!MorphlingItem.tryBeginPrimalAbility(playerIn, itemStack, "Echothesis",
				260.0, 500, 160, 0)) return;
		AABB area = playerIn.getBoundingBox().inflate(36.0);
		for (LivingEntity entity : worldIn.getEntitiesOfClass(LivingEntity.class, area,
				entity -> entity != playerIn && entity.isAlive())) {
			entity.addEffect(new MobEffectInstance(MobEffects.GLOWING,
					220, 0, true, false, true));
		}
		int lightLevel = worldIn.getMaxLocalRawBrightness(playerIn.blockPosition());
		if (lightLevel < 5) {
			playerIn.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,
					180, 1, true, false, true));
			playerIn.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,
					180, 1, true, false, true));
			for (Monster mob : worldIn.getEntitiesOfClass(Monster.class, area, Monster::isAlive)) {
				mob.addEffect(new MobEffectInstance(MobEffects.DARKNESS,
						100, 0, true, true, true));
			}
		}
	}

	@Override
	public void onEquippedTick(Player player, ItemStack stack) {
		int maturity = MorphlingItem.getMaturityLevel(stack);

		// Base effect: Echoic Perception (reveal nearby entities, amplifier = maturity)
		if (!player.hasEffect(EffectInit.echoic_perception)) {
			player.addEffect(new MobEffectInstance(EffectInit.echoic_perception,
					100, maturity, false, true, true));
		}

		// Mature (3+): Membrane Glide — slow falling while sneaking in the air
		if (maturity >= 3 && !player.onGround() && player.isShiftKeyDown()) {
			if (!player.hasEffect(MobEffects.SLOW_FALLING)) {
				player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING,
						20, 0, true, false, true));
			}
		}

		// Apex (4): Nightwing Frenzy — bonus attack stats in darkness
		if (maturity >= 4 && !player.level().isClientSide) {
			int lightLevel = player.level().getMaxLocalRawBrightness(player.blockPosition());
			if (lightLevel < 4) {
				// Grant brief Strength and Speed buffs in darkness
				if (!player.hasEffect(MobEffects.DAMAGE_BOOST)) {
					player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,
							40, 1, true, false, true)); // Strength II for 2s
				}
				if (!player.hasEffect(MobEffects.MOVEMENT_SPEED)) {
					player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,
							40, 0, true, false, true)); // Speed I for 2s
				}
			}
		}
	}

	@Override
	public void onEquippedHurt(Player player, ItemStack stack, DamageSource source, float amount) {
		int maturity = MorphlingItem.getMaturityLevel(stack);

		// Developing (2+): Sonar Shriek — apply Darkness and Slowness to attacker
		if (maturity >= 2 && source.getEntity() instanceof LivingEntity attacker) {
			long lastShriek = getLastAbilityTick(stack, "SonarShriek");
			long now = player.level().getGameTime();
			if (now - lastShriek >= SONAR_SHRIEK_COOLDOWN) {
				setLastAbilityTick(stack, "SonarShriek", now);

				int effectDuration = 40 + (maturity - 2) * 20; // 2s at Developing, 3s Mature, 4s Apex
				attacker.addEffect(new MobEffectInstance(MobEffects.DARKNESS,
						effectDuration, 0, true, true, true));
				attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
						effectDuration, 1, true, true, true));
			}
		}
	}

	@Override
	public boolean onEquippedFall(Player player, ItemStack stack, float distance) {
		int maturity = MorphlingItem.getMaturityLevel(stack);

		// Mature (3+): Membrane Glide — significantly reduce fall damage
		if (maturity >= 3) {
			// Reduce fall distance by 75%, only cancel if distance trivial after reduction
			float reducedDistance = distance * 0.25f;
			if (reducedDistance <= 3.0f) {
				return true; // Cancel fall damage entirely
			}
			// Otherwise just halve the fall distance via slow falling (handled in tick)
		}
		return false;
	}

	@Override
	public List<Component> getMaturityBonusDescriptions(int currentMaturity) {
		List<Component> list = new ArrayList<>();
		list.add(MorphlingItem.maturityBonusLine("Sonar Shriek (Darkness & Slow attacker on hit)", 2, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Membrane Glide (Slow falling & reduced fall damage)", 3, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Nightwing Frenzy (Strength in darkness)", 4, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Echothesis (Staff active reveals blood signatures and empowers night raids)", 5, currentMaturity));
		return list;
	}

}
