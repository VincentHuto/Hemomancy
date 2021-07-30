package com.huto.hemomancy.manipulation.quick;

import java.util.List;

import com.huto.hemomancy.capa.tendency.EnumBloodTendency;
import com.huto.hemomancy.capa.vascular.EnumVeinSections;
import com.huto.hemomancy.manipulation.BloodManipulation;
import com.huto.hemomancy.manipulation.EnumManipulationRank;
import com.huto.hemomancy.manipulation.EnumManipulationType;
import com.hutoslib.client.particle.util.ParticleColor;
import com.hutoslib.common.HutosLibPacketHandler;

import net.minecraft.world.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.RegistryKey;
import net.minecraft.core.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.level.Level;

public class ManipActivationPotential extends BloodManipulation {

	public ManipActivationPotential(String name, double cost, double alignLevel, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignLevel, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		List<Entity> targets = player.world.getEntitiesWithinAABBExcludingEntity(player,
				player.getBoundingBox().grow(5.0));
		if (targets.size() > 0) {
			for (int i = 0; i < targets.size(); ++i) {
				if (targets.get(i) instanceof LivingEntity) {
					LivingEntity target = (LivingEntity) targets.get(i);
					Vector3d translation = new Vector3d(0, 1, 0);
					Vector3d speedVec = new Vector3d(target.getPosition().getX(),
							(float) target.getPosition().getY() + target.getHeight() / 2.0f,
							target.getPosition().getZ());
					HutosLibPacketHandler.sendLightningSpawn(player.getPositionVec().add(translation), speedVec, 64.0f,
							(RegistryKey<World>) player.world.getDimensionKey(), ParticleColor.YELLOW, 2, 10, 9, 0.2f);
					target.attackEntityFrom(DamageSource.causePlayerDamage((PlayerEntity) player), 5.0f);
				}
			}
		}
	}

}
