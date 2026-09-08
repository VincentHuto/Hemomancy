package com.vincenthuto.hemomancy.common.manipulation.lux;

import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.manipulation.*;
import com.vincenthuto.hutoslib.client.particle.data.ColorParticleData;
import com.vincenthuto.hutoslib.common.registry.HLParticleInit;
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
import net.minecraft.world.phys.Vec3;

public class PrismaticReproofManip extends BloodManipulation {
	private static final double RANGE = 14.0;
	private static final double HALF_CONE_DOT = 0.72;

	public PrismaticReproofManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		if (!(world instanceof ServerLevel sLevel)) return;

		Vec3 eye = player.getEyePosition();
		Vec3 look = player.getLookAngle().normalize();
		int struck = 0;

		for (LivingEntity target : world.getEntitiesOfClass(LivingEntity.class,
				new AABB(player.blockPosition()).inflate(RANGE), e -> ManipulationCombatHelper.canHarm(player, e))) {
			if (!ManipulationCombatHelper.visible(player, target)) continue;
			Vec3 toTarget = target.getEyePosition().subtract(eye);
			double distance = toTarget.length();
			if (distance <= 0.001 || distance > RANGE) continue;
			if (look.dot(toTarget.normalize()) < HALF_CONE_DOT) continue;
			target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0, false, true));
			target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 140, 0, false, true));
			float damage = (float) ((target.hasEffect(MobEffects.GLOWING) ? 4.0F : 2.0F)
					* SkillPointHelper.getCrimsonMasteryMultiplier(player));
			float adjusted = TendencyAffinityRules.adjustManipulationDamage(player, target, this, damage);
			if (target.hurt(world.damageSources().magic(), adjusted)) {
				SchoolHitHelper.tryTriggerConductiveArc(player, target, EnumBloodTendency.LUX, getSecondaryTend(),
						adjusted);
			}
			ManipulationVisuals.burst(sLevel, ManipulationVisuals.Form.VERDICT, eye, target.getEyePosition(), .25, 12);
            struck++;
		}

		world.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS,
				0.8F, struck > 0 ? 1.4F : 0.9F);
		RandomSource random = world.random;
		for (int i = 0; i < 42; i++) {
			double distance = 1.0 + random.nextDouble() * RANGE;
			Vec3 scatter = look.scale(distance).add(
					(random.nextDouble() - 0.5) * distance * 0.55,
					(random.nextDouble() - 0.5) * distance * 0.20,
					(random.nextDouble() - 0.5) * distance * 0.55);
			Vec3 particlePos = ManipulationCombatHelper.clipToGeometry(player, eye.add(scatter));
			sLevel.sendParticles(new ColorParticleData(HLParticleInit.glow.get(), new ParticleColor(
							190 + random.nextFloat() * 65,
							170 + random.nextFloat() * 85,
							230 + random.nextFloat() * 25)),
					particlePos.x, particlePos.y, particlePos.z,
					1, 0, 0, 0, 0.02);
		}
	}
}
