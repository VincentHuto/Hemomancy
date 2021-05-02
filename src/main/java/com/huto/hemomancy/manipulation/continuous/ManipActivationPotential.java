package com.huto.hemomancy.manipulation.continuous;

import java.util.List;
import java.util.stream.Collectors;

import com.huto.hemomancy.capa.tendency.EnumBloodTendency;
import com.huto.hemomancy.capa.vascular.EnumVeinSections;
import com.huto.hemomancy.manipulation.BloodManipulation;
import com.huto.hemomancy.manipulation.EnumManipulationRank;
import com.huto.hemomancy.manipulation.EnumManipulationType;
import com.huto.hemomancy.network.PacketHandler;
import com.huto.hemomancy.particle.util.ParticleColor;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class ManipActivationPotential extends BloodManipulation {

	public ManipActivationPotential(String name, double cost, double alignLevel, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignLevel, type, rank, tendency, section);
	}

	public void getAction(PlayerEntity player, ServerWorld world, ItemStack heldItemMainhand, BlockPos position) {
		List<MobEntity> targets = player.world.getEntitiesWithinAABB(MobEntity.class, player.getBoundingBox().grow(5.0))
				.stream().filter(e -> e.canEntityBeSeen((Entity) player)).collect(Collectors.toList());
		if (targets.size() > 0) {
			for (int i = 0; i < targets.size(); ++i) {
				MobEntity target = targets.get(i);
				Vector3d translation = new Vector3d(0, 1, 0);
				Vector3d speedVec = new Vector3d(target.getPosition().getX(),
						(float) target.getPosition().getY() + target.getHeight() / 2.0f, target.getPosition().getZ());
				PacketHandler.sendLightningSpawn(player.getPositionVec().add(translation), speedVec, 64.0f,
						(RegistryKey<World>) player.world.getDimensionKey(), ParticleColor.YELLOW, 2, 10, 9, 0.2f);
				target.attackEntityFrom(DamageSource.causePlayerDamage((PlayerEntity) player), 5.0f);
			}
		}
	}

}
