package com.vincenthuto.hemomancy.common.manipulation.animus;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCastingRules;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationReactiveEvents;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hutoslib.HutosLib;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Predicate;

public class DeadlyGazeManip extends BloodManipulation {
	private static final int CHARGE_TICKS = 40;

	@Nullable
	public static EntityHitResult rayTraceEntities(Entity shooter, double range, @Nullable Predicate<Entity> filter) {
		Vec3 eyes = shooter.getEyePosition(1f);
		Vec3 end = eyes.add(shooter.getLookAngle().multiply(range, range, range));

		Entity result = null;
		double distance = range * range;
		for (Entity entity : shooter.level().getEntities(shooter, shooter.getBoundingBox().inflate(range), filter)) {
			Optional<Vec3> opt = entity.getBoundingBox().inflate(0.3).clip(eyes, end);
			if (opt.isPresent()) {
				double dist = eyes.distanceToSqr(opt.get());
				if (dist < distance) {
					result = entity;
					distance = dist;
				}
			}
		}

		return result == null ? null : new EntityHitResult(result);
	}

	public DeadlyGazeManip(String name, double cost, double alignLevel, double xpCost, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		getAction(player, world, heldItemMainhand, position, CHARGE_TICKS);
	}

	@Override
	public int getRequiredChargeTicks() {
		return CHARGE_TICKS;
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position, float heldTicks) {
		float charge = ManipulationCastingRules.chargeFraction(heldTicks, CHARGE_TICKS);
		HitResult pick = rayTraceEntities(player, 12 + 20 * charge, e -> e instanceof LivingEntity && e != player);
		if (pick != null) {
			if (pick.getType() == Type.ENTITY) {
				EntityHitResult entResult = (EntityHitResult) pick;
				LivingEntity hitEntity = (LivingEntity) entResult.getEntity();
				if (hitEntity instanceof Mob mob) mob.getNavigation().stop();
				hitEntity.push(0, .25D + .75D * charge, 0);
				if (charge >= 1.0F && world instanceof ServerLevel serverLevel) {
					ManipulationReactiveEvents.scheduleDeadlyGazeSlam(serverLevel, player, hitEntity, 12, 6.0F);
				}
				RandomSource rand = world.random;
				for (int i = 0; i < 10; i++) {
					Vec3 entVec = hitEntity.position().add(rand.nextDouble() - rand.nextDouble(), 0,
							rand.nextDouble() - rand.nextDouble());
					Vec3 end = entVec.add(0, hitEntity.getBbHeight(), 0).add(rand.nextDouble() - rand.nextDouble(), 0,
							rand.nextDouble() - rand.nextDouble());

					if (world instanceof ServerLevel serverLevel) {
						PacketHandler.sendClawParticles(end, ParticleColor.BLOOD, 64f, serverLevel);
					}


					HutosLib.proxy.lightningFX(entVec, end, 64f, ParticleColor.BLOOD);

				}
			}
		}

	}
}
