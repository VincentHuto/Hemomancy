package com.vincenthuto.hemomancy.common.manipulation.flammeus;

import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
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
			if (ManipulationCombatHelper.allied(player, target)) {
				target.clearFire();
				target.setTicksFrozen(0);
				target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE, 25, 0, false, true));
			} else if (ManipulationCombatHelper.canHarm(player, target)) {
				target.igniteForSeconds(3);
				ManipulationCombatHelper.hurt(this, player, target, level, 2.0F);
			}
		}
		for (BlockPos pos : BlockPos.betweenClosed(player.blockPosition().offset(-5, -5, -5),
				player.blockPosition().offset(5, 5, 5))) {
			if (level.getBlockState(pos).is(Blocks.FROSTED_ICE) || level.getBlockState(pos).is(BlockInit.frozen_cruor.get())) {
				level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
			}
		}
		ManipulationVisuals.attached(player, ManipulationVisuals.Form.FURNACE, 5, 25, 1);
	}
}
