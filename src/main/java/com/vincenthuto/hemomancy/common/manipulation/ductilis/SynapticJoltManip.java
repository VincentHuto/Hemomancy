package com.vincenthuto.hemomancy.common.manipulation.ductilis;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.manipulation.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class SynapticJoltManip extends BloodManipulation {
	private static final double BASE_RANGE = 7.0D;
	private static final double TARGET_DOT = 0.72D;
	private static final float BASE_DAMAGE = 3.0F;

	public SynapticJoltManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		if (!(world instanceof ServerLevel)) return;
		double range = BASE_RANGE * SkillPointHelper.getSanguineReachMultiplier(player);
		LivingEntity target = findTarget(player, world, range);
		if (target == null) {
			world.playSound(null, player.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS,
					0.25F, 1.9F);
			return;
		}

		staggerTarget(target);
		DuctilisLightningEffects.synapticJolt(player, target);
		float damage = (float) (BASE_DAMAGE * SkillPointHelper.getCrimsonMasteryMultiplier(player));
		float adjusted = TendencyAffinityRules.adjustManipulationDamage(player, target, this, damage);
		if (target.hurt(world.damageSources().magic(), adjusted)) {
			SchoolHitHelper.tryTriggerConductiveArc(player, target, EnumBloodTendency.DUCTILIS, getSecondaryTend(),
					adjusted);
		}
		world.playSound(null, target.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS,
				0.45F, 1.75F);
	}

	public static void staggerTarget(LivingEntity target) {
		if (target instanceof Mob mob) {
			mob.getNavigation().stop();
		}
		target.setDeltaMovement(0.0D, target.getDeltaMovement().y * 0.2D, 0.0D);
		target.hurtMarked = true;
		target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 2, false, true, true));
		target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 0, false, true, true));
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
					return distance > 0.001D && distance <= range && look.dot(toTarget.normalize()) >= TARGET_DOT;
				})
				.min((a, b) -> Double.compare(player.distanceToSqr(a), player.distanceToSqr(b)))
				.orElse(null);
	}
}
