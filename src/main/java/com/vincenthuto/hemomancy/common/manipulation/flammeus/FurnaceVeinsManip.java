package com.vincenthuto.hemomancy.common.manipulation.flammeus;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class FurnaceVeinsManip extends BloodManipulation {
	public FurnaceVeinsManip(String name, double cost, double alignment, double xpCost, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignment, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position, float heldTicks) {
		if (!(world instanceof ServerLevel level)) return;
		for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(5), LivingEntity::isAlive)) {
			if (target == player || player.isAlliedTo(target)) {
				target.clearFire();
				target.setTicksFrozen(0);
			} else {
				target.igniteForSeconds(3);
				ManipulationCombatHelper.hurt(this, player, target, level, 2.0F);
			}
		}
		for (BlockPos pos : BlockPos.betweenClosed(player.blockPosition().offset(-5, -5, -5),
				player.blockPosition().offset(5, 5, 5))) {
			if (level.getBlockState(pos).is(Blocks.FROSTED_ICE)) level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
		}
		level.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 1, player.getZ(), 24, 2.5, 1, 2.5, 0.03);
	}
}
