package com.vincenthuto.hemomancy.manipulation.quick;

import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.capa.player.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.capa.player.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.network.PacketHandler;
import com.vincenthuto.hutoslib.HutosLib;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;

public class DeadlyGazeManip extends BloodManipulation {

	public DeadlyGazeManip(String name, double cost, double alignLevel, double xpCost, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		HitResult pick = rayTraceEntities(player, 100, (e) -> e instanceof LivingEntity);
		if (pick != null) {
			if (pick.getType() == Type.ENTITY) {
				EntityHitResult entResult = (EntityHitResult) pick;
				LivingEntity hitEntity = (LivingEntity) entResult.getEntity();
				hitEntity.push(0, 1, 0);
				RandomSource rand = world.random;
				for (int i = 0; i < 10; i++) {
					Vec3 entVec = hitEntity.position().add(rand.nextDouble() - rand.nextDouble(), 0,
							rand.nextDouble() - rand.nextDouble());
					Vec3 end = entVec.add(0, hitEntity.getBbHeight(), 0).add(rand.nextDouble() - rand.nextDouble(), 0,
							rand.nextDouble() - rand.nextDouble());

					PacketHandler.sendClawParticles(end, ParticleColor.BLOOD, 64f, world.dimension());

			
					HutosLib.proxy.lightningFX(entVec, end, 64f, ParticleColor.BLOOD);

				}
			}
		}

	}

	@Nullable
	public static EntityHitResult rayTraceEntities(Entity shooter, double range, @Nullable Predicate<Entity> filter) {
		Vec3 eyes = shooter.getEyePosition(1f);
		Vec3 end = eyes.add(shooter.getLookAngle().multiply(range, range, range));

		Entity result = null;
		double distance = range * range;
		for (Entity entity : shooter.level.getEntities(shooter, shooter.getBoundingBox().inflate(range), filter)) {
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
}
