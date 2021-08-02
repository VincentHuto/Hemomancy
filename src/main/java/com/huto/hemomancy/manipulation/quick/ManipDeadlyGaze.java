package com.huto.hemomancy.manipulation.quick;

import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.huto.hemomancy.capa.tendency.EnumBloodTendency;
import com.huto.hemomancy.capa.vascular.EnumVeinSections;
import com.huto.hemomancy.manipulation.BloodManipulation;
import com.huto.hemomancy.manipulation.EnumManipulationRank;
import com.huto.hemomancy.manipulation.EnumManipulationType;
import com.huto.hemomancy.network.PacketHandler;
import com.hutoslib.client.particle.util.ParticleColor;
import com.hutoslib.common.network.HutosLibPacketHandler;
import com.mojang.math.Vector3d;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;

public class ManipDeadlyGaze extends BloodManipulation {

	public ManipDeadlyGaze(String name, double cost, double alignLevel, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignLevel, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		HitResult pick = rayTraceEntities(player, 100, (e) -> e instanceof LivingEntity);
		if (pick != null) {
			if (pick.getType() == Type.ENTITY) {
				EntityHitResult entResult = (EntityHitResult) pick;
				LivingEntity hitEntity = (LivingEntity) entResult.getEntity();
				hitEntity.push(0, 1, 0);
				Random rand = world.random;
				for (int i = 0; i < 10; i++) {
					Vec3 entVec = hitEntity.position().add(rand.nextDouble() - rand.nextDouble(), 0,
							rand.nextDouble() - rand.nextDouble());
					Vec3 endVec = entVec.add(0, hitEntity.getBbHeight(), 0).add(rand.nextDouble() - rand.nextDouble(),
							0, rand.nextDouble() - rand.nextDouble());
					PacketHandler.sendClawParticles(endVec, ParticleColor.BLOOD, 64f,
							(ResourceKey<Level>) world.dimension());
					HutosLibPacketHandler.sendLightningSpawn(entVec, endVec, 64.0f,
							(ResourceKey<Level>) player.level.dimension(), ParticleColor.RED, 3, 10, 9, 1.2f);
				}
			}
		}

	}

	@Nullable
	public static EntityRayTraceResult rayTraceEntities(Entity shooter, double range,
			@Nullable Predicate<Entity> filter) {
		Vector3d eyes = shooter.getEyePosition(1f);
		Vector3d end = eyes.add(shooter.getLookAngle().multiply(range, range, range));

		Entity result = null;
		double distance = range * range;
		for (Entity entity : shooter.level.getEntities(shooter, shooter.getBoundingBox().inflate(range), filter)) {
			Optional<Vector3d> opt = entity.getBoundingBox().inflate(0.3).clip(eyes, end);
			if (opt.isPresent()) {
				double dist = eyes.distanceToSqr(opt.get());
				if (dist < distance) {
					result = entity;
					distance = dist;
				}
			}
		}

		return result == null ? null : new EntityRayTraceResult(result);
	}
}
