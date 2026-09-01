package com.vincenthuto.hemomancy.common.manipulation.congeatio;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.manipulation.TendencyAffinityRules;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Cryogenic Pulse — a T1 (HUMILIS) CONGEATIO quick manipulation that detonates
 * a burst of crystallised blood-ice in a 5-block radius around the caster.
 *
 * <p>All living entities caught in the pulse receive:
 * <ul>
 *   <li>Slowness III for 3 seconds (soft freeze)</li>
 *   <li>Mining Fatigue I for 4 seconds (frozen limbs)</li>
 *   <li>1.5 hearts of cold damage</li>
 * </ul>
 * The caster is immune. Useful for crowd control in tight situations when
 * Glacial Grasp (the traversal tool) is not appropriate.
 */
public class CryogenicPulseManip extends BloodManipulation {

	private static final double RADIUS = 5.0;
	private static final float DAMAGE = 3.0f; // 1.5 hearts
	private static final int SLOWNESS_DURATION = 60;  // 3 s
	private static final int SLOWNESS_AMPLIFIER = 2;  // Slowness III
	private static final int FATIGUE_DURATION = 80;   // 4 s
	private static final int FATIGUE_AMPLIFIER = 0;   // Mining Fatigue I

	public CryogenicPulseManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		if (!(world instanceof ServerLevel sLevel)) return;

		BlockPos center = player.blockPosition();
		AABB searchBox = new AABB(center).inflate(RADIUS);
		List<LivingEntity> targets = world.getEntitiesOfClass(LivingEntity.class, searchBox,
				e -> e != player && e.isAlive());

		for (LivingEntity target : targets) {
			if (target.distanceTo(player) <= RADIUS) {
				target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
						SLOWNESS_DURATION, SLOWNESS_AMPLIFIER, false, true));
				target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN,
						FATIGUE_DURATION, FATIGUE_AMPLIFIER, false, true));
				float damage = (float) (DAMAGE * SkillPointHelper.getCrimsonMasteryMultiplier(player));
				target.hurt(world.damageSources().freeze(),
						TendencyAffinityRules.adjustManipulationDamage(player, target, this, damage));
			}
		}

		world.playSound(null, center, SoundEvents.GLASS_PLACE, SoundSource.PLAYERS, 1.0f, 0.7f);
		world.playSound(null, center, SoundEvents.POWDER_SNOW_STEP, SoundSource.PLAYERS, 0.8f, 0.5f);

		RandomSource random = world.random;
		for (int i = 0; i < 40; i++) {
			sLevel.sendParticles(
					GlowParticleFactory.createData(new ParticleColor(
							100 + random.nextFloat() * 60,
							160 + random.nextFloat() * 60,
							255)),
					center.getX() + 0.5 + (random.nextDouble() - 0.5) * RADIUS * 2,
					center.getY() + 0.5 + random.nextDouble() * 1.5,
					center.getZ() + 0.5 + (random.nextDouble() - 0.5) * RADIUS * 2,
					1, 0f, 0.1f, 0f, 0.015f);
		}
	}
}
