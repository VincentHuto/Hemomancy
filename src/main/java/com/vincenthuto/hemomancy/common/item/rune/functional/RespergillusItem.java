package com.vincenthuto.hemomancy.common.item.rune.functional;

import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.item.rune.ItemContractRune;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class RespergillusItem extends ItemContractRune {

	public RespergillusItem(Properties properties, EnumBloodTendency tendencyIn, float deepenAmountIn) {
		super(properties, tendencyIn, deepenAmountIn);
	}

	@Override
	public void onWornTick(LivingEntity player) {
		super.onWornTick(player);
		player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING));
	}
	
	@Override
	public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, Player player) {
		return super.onBlockStartBreak(itemstack, pos, player);
		
	}

}
