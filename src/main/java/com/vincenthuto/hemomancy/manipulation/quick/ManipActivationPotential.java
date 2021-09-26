package com.vincenthuto.hemomancy.manipulation.quick;

import java.util.List;

import com.vincenthuto.hemomancy.capa.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.capa.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.manipulation.EnumManipulationType;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.common.network.HLPacketHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ManipActivationPotential extends BloodManipulation {

	public ManipActivationPotential(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		List<Entity> targets = player.level.getEntities(player, player.getBoundingBox().inflate(5.0));
		if (targets.size() > 0) {
			for (int i = 0; i < targets.size(); ++i) {
				if (targets.get(i) instanceof LivingEntity) {
					LivingEntity target = (LivingEntity) targets.get(i);
					Vec3 translation = new Vec3(0, 1, 0);
					Vec3 speedVec = new Vec3(target.blockPosition().getX(),
							target.blockPosition().getY() + target.getBbHeight() / 2.0f, target.blockPosition().getZ());
					HLPacketHandler.sendLightningSpawn(player.position().add(translation), speedVec, 64.0f,
							player.level.dimension(), ParticleColor.YELLOW, 2, 10, 9, 0.2f);
					HLPacketHandler.sendLightningSpawn(player.position().add(translation), speedVec, 64.0f,
							player.level.dimension(), ParticleColor.YELLOW, 2, 10, 9, 0.2f);
					HLPacketHandler.sendLightningSpawn(player.position().add(translation), speedVec, 64.0f,
							player.level.dimension(), ParticleColor.YELLOW, 2, 10, 9, 0.2f);
					HLPacketHandler.sendLightningSpawn(player.position().add(translation), speedVec, 64.0f,
							player.level.dimension(), ParticleColor.YELLOW, 2, 10, 9, 0.2f);
					target.hurt(DamageSource.playerAttack(player), 5.0f);
				}
			}
		}
	}

}
