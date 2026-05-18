package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.FluidInit;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

/**
 * Mole morphling that grants increased mining speed and bonus underground
 * regeneration by applying the Burrower's Instinct effect while equipped.
 * Also grants Night Vision underground. Maturity level scales the mining
 * speed bonus. Prefers FERRIC (iron-rich soil nourishes the mole) with
 * MORTEM as secondary (decay enriches the earth it tunnels through).
 *
 * Maturity bonuses (unique reactive abilities):
 * - Developing (2): Burrow Sense — while underground (below Y=50), nearby
 *   ores and entities glow briefly (echolocation through vibration)
 * - Mature (3): Earthen Bulwark — when taking damage underground, gain brief
 *   Resistance (the mole retreats into compacted earth)
 * - Apex (4): Seismic Slam — while sneaking and jumping underground, emit a
 *   shockwave that damages and launches nearby hostiles (mole stomps)
 */
public class MoleMorphlingItem extends MorphlingItem {

	/** Cooldown in ticks between Earthen Bulwark triggers (15 seconds). */
	private static final int EARTHEN_BULWARK_COOLDOWN = 300;
	/** Cooldown in ticks between Seismic Slam triggers (20 seconds). */
	private static final int SEISMIC_SLAM_COOLDOWN = 400;
	/** Y-level below which the mole is considered underground. */
	private static final double UNDERGROUND_THRESHOLD = 50.0;

	public MoleMorphlingItem(Properties prop) {
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
	public void use(Player playerIn, InteractionHand handIn, ItemStack itemStack, Level worldIn) {
		if (!MorphlingItem.tryBeginPrimalAbility(playerIn, itemStack, "DeepTremorSense",
				320.0, 600, 180, 0)) return;
		playerIn.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED,
				300, 2, true, true, true));
		playerIn.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION,
				320, 0, true, false, true));
		AABB area = playerIn.getBoundingBox().inflate(30.0);
		for (LivingEntity entity : worldIn.getEntitiesOfClass(LivingEntity.class, area,
				entity -> entity != playerIn && entity.isAlive())) {
			entity.addEffect(new MobEffectInstance(MobEffects.GLOWING,
					180, 0, true, false, true));
		}
		for (net.minecraft.world.entity.monster.Monster mob :
				worldIn.getEntitiesOfClass(net.minecraft.world.entity.monster.Monster.class,
						playerIn.getBoundingBox().inflate(8.0), net.minecraft.world.entity.monster.Monster::isAlive)) {
			mob.hurt(playerIn.damageSources().magic(), 7.0f);
			mob.push(0, 0.75, 0);
		}
		if (worldIn instanceof ServerLevel serverLevel) {
			BlockPos origin = playerIn.blockPosition();
			for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-12, -8, -12), origin.offset(12, 8, 12))) {
				if (serverLevel.getFluidState(pos).is(FluidInit.MORPHIC_NECTAR.get())
						|| serverLevel.getFluidState(pos).is(FluidInit.MORPHIC_NECTAR_FLOWING.get())) {
					serverLevel.sendParticles(ParticleTypes.WITCH,
							pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
							4, 0.2, 0.2, 0.2, 0.01);
				}
			}
		}
	}

	@Override
	public void onEquippedTick(Player player, ItemStack stack) {
		int maturity = MorphlingItem.getMaturityLevel(stack);

		// Base effect: Burrower's Instinct (mining speed + underground regen)
		if (!player.hasEffect(EffectInit.burrowers_instinct)) {
			player.addEffect(new MobEffectInstance(EffectInit.burrowers_instinct,
					100, maturity, false, true, true));
		}

		boolean isUnderground = player.getY() < UNDERGROUND_THRESHOLD;

		// Grant Night Vision while underground
		if (isUnderground && !player.hasEffect(MobEffects.NIGHT_VISION)) {
			player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION,
					220, 0, true, false, true)); // Slightly longer than tick interval to prevent flicker
		}

		// Developing (2+): Burrow Sense — reveal nearby entities while underground
		if (maturity >= 2 && isUnderground && !player.level().isClientSide) {
			if (player.tickCount % 40 == 0) { // Every 2 seconds
				double radius = 16.0 + (maturity - 2) * 8.0; // 16 at Developing, 24 Mature, 32 Apex
				AABB area = player.getBoundingBox().inflate(radius);
				List<LivingEntity> nearbyEntities = player.level().getEntitiesOfClass(
						LivingEntity.class, area, e -> e != player);
				for (LivingEntity target : nearbyEntities) {
					if (!target.hasEffect(MobEffects.GLOWING)) {
						target.addEffect(new MobEffectInstance(MobEffects.GLOWING,
								45, 0, true, false, false));
					}
				}
			}
		}

		// Apex (4): Seismic Slam — shockwave when sneaking + jumping on ground
		if (maturity >= 4 && !player.level().isClientSide && isUnderground) {
			if (player.isShiftKeyDown() && !player.onGround() && player.getDeltaMovement().y > 0) {
				long lastSlam = getLastAbilityTick(stack, "SeismicSlam");
				long now = player.level().getGameTime();
				if (now - lastSlam >= SEISMIC_SLAM_COOLDOWN) {
					setLastAbilityTick(stack, "SeismicSlam", now);

					double radius = 6.0;
					AABB area = player.getBoundingBox().inflate(radius);
					List<net.minecraft.world.entity.monster.Monster> hostiles =
							player.level().getEntitiesOfClass(
									net.minecraft.world.entity.monster.Monster.class, area);
					for (net.minecraft.world.entity.monster.Monster mob : hostiles) {
						mob.hurt(player.damageSources().magic(), 6.0f);
						double dx = mob.getX() - player.getX();
						double dz = mob.getZ() - player.getZ();
						double dist = Math.sqrt(dx * dx + dz * dz);
						if (dist > 0) {
							mob.push(dx / dist * 1.5, 0.5, dz / dist * 1.5);
						}
					}
				}
			}
		}
	}

	@Override
	public void onEquippedHurt(Player player, ItemStack stack, DamageSource source, float amount) {
		int maturity = MorphlingItem.getMaturityLevel(stack);

		// Mature (3+): Earthen Bulwark — Resistance when taking damage underground
		if (maturity >= 3 && player.getY() < UNDERGROUND_THRESHOLD) {
			long lastBulwark = getLastAbilityTick(stack, "EarthenBulwark");
			long now = player.level().getGameTime();
			if (now - lastBulwark >= EARTHEN_BULWARK_COOLDOWN) {
				setLastAbilityTick(stack, "EarthenBulwark", now);
				int resDuration = 60 + (maturity - 3) * 40; // 3s at Mature, 5s at Apex
				player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,
						resDuration, 0, true, true, true));
			}
		}
	}

	@Override
	public List<Component> getMaturityBonusDescriptions(int currentMaturity) {
		List<Component> list = new ArrayList<>();
		list.add(MorphlingItem.maturityBonusLine("Burrow Sense (Reveal entities underground)", 2, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Earthen Bulwark (Resistance when hit underground)", 3, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Seismic Slam (Shockwave attack underground)", 4, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Deep Tremor Sense (Staff active maps life, nectar, and tunneling threats)", 5, currentMaturity));
		return list;
	}

}
