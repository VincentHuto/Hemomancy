package com.vincenthuto.hemomancy.common.manipulation.ductilis;

import com.vincenthuto.hemomancy.common.damage.*;

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
        try (var schoolCast = com.vincenthuto.hemomancy.common.damage.SchoolDamage.cast(this, player, 1)) {

		if (!(world instanceof ServerLevel)) return;
        Discharge discharge = new Discharge();
		double range = BASE_RANGE * SkillPointHelper.getSanguineReachMultiplier(player);
		LivingEntity target = findTarget(player, world, range);
		if (target == null) {
            if (ConductionManager.energizeAimed(player,range,discharge)) return;
			world.playSound(null, player.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS,
					0.25F, 1.9F);
			return;
		}

		DuctilisLightningEffects.synapticJolt(player, target);
		float damage = (float) (BASE_DAMAGE * SkillPointHelper.getCrimsonMasteryMultiplier(player));
		float adjusted = (damage);
		if (!ConductionManager.claimHit(player,target,discharge)) return;
        if (ManipulationParticles.hurt(this, target, world.damageSources().magic(), adjusted)) {
            ConductionManager.energizeTouching(player,target,discharge);

		}
		world.playSound(null, target.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS,
				0.45F, 1.75F);
	        }
    }

	public static void staggerTarget(LivingEntity target) {
        SchoolStates.apply(null, target, SchoolState.DISRUPTED, 10, 1);
    }

	@Nullable
	private LivingEntity findTarget(Player player, Level world, double range) {
		Vec3 eye = player.getEyePosition();
		Vec3 look = player.getLookAngle().normalize();
        double unobstructedRange = eye.distanceTo(ManipulationCombatHelper.clipToGeometry(player,eye.add(look.scale(range))));
		return world.getEntitiesOfClass(LivingEntity.class, new AABB(player.blockPosition()).inflate(range),
						target -> ConductionManager.canHarm(player,target)
								&& player.hasLineOfSight(target))
				.stream()
				.filter(target -> {
					Vec3 toTarget = target.getEyePosition().subtract(eye);
					double distance = toTarget.length();
					return distance > 0.001D && distance <= unobstructedRange && look.dot(toTarget.normalize()) >= TARGET_DOT;
				})
				.min((a, b) -> Double.compare(player.distanceToSqr(a), player.distanceToSqr(b)))
				.orElse(null);
	}
}
