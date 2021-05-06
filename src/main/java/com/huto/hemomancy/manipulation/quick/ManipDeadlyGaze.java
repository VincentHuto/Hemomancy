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
import com.hutoslib.client.particle.ParticleColor;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class ManipDeadlyGaze extends BloodManipulation {

	public ManipDeadlyGaze(String name, double cost, double alignLevel, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignLevel, type, rank, tendency, section);
	}

	@Override
	public void getAction(PlayerEntity player, World world, ItemStack heldItemMainhand, BlockPos position) {
		RayTraceResult pick = rayTraceEntities(player, 100, (e) -> e instanceof LivingEntity);
		if (pick != null) {
			if (pick.getType() == Type.ENTITY) {
				EntityRayTraceResult entResult = (EntityRayTraceResult) pick;
				LivingEntity hitEntity = (LivingEntity) entResult.getEntity();
				hitEntity.addVelocity(0, 1, 0);
				Random rand = world.rand;
				for (int i = 0; i < 10; i++) {
					Vector3d entVec = hitEntity.getPositionVec().add(rand.nextDouble() - rand.nextDouble(), 0,
							rand.nextDouble() - rand.nextDouble());
					Vector3d endVec = entVec.add(0, hitEntity.getHeight(), 0).add(rand.nextDouble() - rand.nextDouble(),
							0, rand.nextDouble() - rand.nextDouble());
					PacketHandler.sendClawParticles(endVec, ParticleColor.BLOOD, 64f,
							(RegistryKey<World>) world.getDimensionKey());

					com.hutoslib.common.PacketHandler.sendLightningSpawn(entVec, endVec, 64.0f,
							(RegistryKey<World>) player.world.getDimensionKey(), ParticleColor.RED, 2, 10, 9, 1.2f);

				}
			}
		}

	}

	@Nullable
	public static EntityRayTraceResult rayTraceEntities(Entity shooter, double range,
			@Nullable Predicate<Entity> filter) {
		Vector3d eyes = shooter.getEyePosition(1f);
		Vector3d end = eyes.add(shooter.getLookVec().mul(range, range, range));

		Entity result = null;
		double distance = range * range;
		for (Entity entity : shooter.world.getEntitiesInAABBexcluding(shooter, shooter.getBoundingBox().grow(range),
				filter)) {
			Optional<Vector3d> opt = entity.getBoundingBox().grow(0.3).rayTrace(eyes, end);
			if (opt.isPresent()) {
				double dist = eyes.squareDistanceTo(opt.get());
				if (dist < distance) {
					result = entity;
					distance = dist;
				}
			}
		}

		return result == null ? null : new EntityRayTraceResult(result);
	}
}
