package com.vincenthuto.hemomancy.common.manipulation.lux;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.BodyRefinementSkillRules;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.manipulation.*;
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
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class HematicFlareManip extends BloodManipulation {
	private static final double BASE_RANGE = 16.0D;
	private static final double RAY_DOT = 0.975D;
	private static final int GLOWING_TICKS = 180;
	private static final float BASE_DAMAGE = 3.0F;
	private static final float CONCEALED_BONUS = 2.0F;

	public HematicFlareManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		if (!(world instanceof ServerLevel sLevel)) return;

		int brightEyed = SkillPointHelper.getBrightEyedLevel(player);
		double range = BASE_RANGE * SkillPointHelper.getSanguineReachMultiplier(player)
				* BodyRefinementSkillRules.perceptionRangeMultiplier(brightEyed);
		LivingEntity target = findTarget(player, world, range);
		Vec3 eye = player.getEyePosition();
		Vec3 look = player.getLookAngle().normalize();

		if (target != null) {
			boolean concealed = target.hasEffect(MobEffects.INVISIBILITY);
			if (concealed) {
				target.removeEffect(MobEffects.INVISIBILITY);
			}
			target.addEffect(new MobEffectInstance(MobEffects.GLOWING,
					BodyRefinementSkillRules.revealTicks(GLOWING_TICKS, brightEyed), 0, false, true));
			float damage = (float) ((BASE_DAMAGE + (concealed ? CONCEALED_BONUS : 0.0F))
					* SkillPointHelper.getCrimsonMasteryMultiplier(player));
			float adjusted = TendencyAffinityRules.adjustManipulationDamage(player, target, this, damage);
			if (target.hurt(world.damageSources().magic(), adjusted)) {
				SchoolHitHelper.tryTriggerConductiveArc(player, target, EnumBloodTendency.LUX, getSecondaryTend(),
						adjusted);
			}
			world.playSound(null, target.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS,
					0.85F, concealed ? 1.75F : 1.45F);
			sendImpactParticles(sLevel, target);
		} else {
			world.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS,
					0.55F, 1.2F);
		}

		sendRayParticles(sLevel, eye, look, range);
	}

	@Nullable
	private LivingEntity findTarget(Player player, Level world, double range) {
		Vec3 eye = player.getEyePosition();
		Vec3 look = player.getLookAngle().normalize();
		return world.getEntitiesOfClass(LivingEntity.class, new AABB(player.blockPosition()).inflate(range),
						target -> target != player && target.isAlive() && !target.isAlliedTo(player)
								&& player.hasLineOfSight(target))
				.stream()
				.filter(target -> {
					Vec3 toTarget = target.getEyePosition().subtract(eye);
					double distance = toTarget.length();
					return distance > 0.001D && distance <= range && look.dot(toTarget.normalize()) >= RAY_DOT;
				})
				.min((a, b) -> Double.compare(player.distanceToSqr(a), player.distanceToSqr(b)))
				.orElse(null);
	}

	private void sendRayParticles(ServerLevel level, Vec3 eye, Vec3 look, double range) {
		RandomSource random = level.random;
		for (int i = 0; i < 24; i++) {
			double distance = 0.8D + random.nextDouble() * range;
			Vec3 pos = eye.add(look.scale(distance));
			level.sendParticles(GlowParticleFactory.createData(new ParticleColor(
							235 + random.nextFloat() * 20,
							215 + random.nextFloat() * 35,
							165 + random.nextFloat() * 55)),
					pos.x + (random.nextDouble() - 0.5D) * 0.16D,
					pos.y + (random.nextDouble() - 0.5D) * 0.16D,
					pos.z + (random.nextDouble() - 0.5D) * 0.16D,
					1, 0.0D, 0.0D, 0.0D, 0.012D);
		}
	}

	private void sendImpactParticles(ServerLevel level, LivingEntity target) {
		Vec3 center = target.getEyePosition();
		RandomSource random = level.random;
		for (int i = 0; i < 16; i++) {
			level.sendParticles(GlowParticleFactory.createData(new ParticleColor(255, 235, 175)),
					center.x + (random.nextDouble() - 0.5D) * 0.7D,
					center.y + (random.nextDouble() - 0.5D) * 0.7D,
					center.z + (random.nextDouble() - 0.5D) * 0.7D,
					1, 0.0D, 0.03D, 0.0D, 0.01D);
		}
	}
}
