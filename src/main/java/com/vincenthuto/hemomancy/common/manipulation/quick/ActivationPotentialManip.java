package com.vincenthuto.hemomancy.common.manipulation.quick;

import java.util.List;

import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.common.network.HLPacketHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ActivationPotentialManip extends BloodManipulation {

	public ActivationPotentialManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		List<Entity> targets = player.level().getEntities(player, player.getBoundingBox().inflate(5.0));
		if (targets.size() > 0) {
			for (Entity target2 : targets) {
				if (target2 instanceof LivingEntity) {
					LivingEntity target = (LivingEntity) target2;
					Vec3 translation = new Vec3(0, 1, 0);
					Vec3 speedVec = new Vec3(target.blockPosition().getX(),
							target.blockPosition().getY() + target.getBbHeight() / 2.0f, target.blockPosition().getZ());
					HLPacketHandler.sendLightningSpawn(player.position().add(translation), speedVec, 64.0f,
							player.level().dimension(), ParticleColor.YELLOW, 2, 10, 9, 0.2f);
					HLPacketHandler.sendLightningSpawn(player.position().add(translation), speedVec, 64.0f,
							player.level().dimension(), ParticleColor.YELLOW, 2, 10, 9, 0.2f);
					HLPacketHandler.sendLightningSpawn(player.position().add(translation), speedVec, 64.0f,
							player.level().dimension(), ParticleColor.YELLOW, 2, 10, 9, 0.2f);
					HLPacketHandler.sendLightningSpawn(player.position().add(translation), speedVec, 64.0f,
							player.level().dimension(), ParticleColor.YELLOW, 2, 10, 9, 0.2f);
					target.hurt(player.damageSources().playerAttack(player), 5.0f);
				}
			}
		}
	}

}
