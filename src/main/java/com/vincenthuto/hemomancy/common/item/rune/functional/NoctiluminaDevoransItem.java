package com.vincenthuto.hemomancy.common.item.rune.functional;

import java.util.List;

import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.item.rune.ItemContractRune;

import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

public class NoctiluminaDevoransItem extends ItemContractRune {

	public NoctiluminaDevoransItem(Properties properties, EnumBloodTendency tendencyIn, float deepenAmountIn) {
		super(properties, tendencyIn, deepenAmountIn);
	}

	@Override
	public void onWornTick(LivingEntity player) {
		super.onWornTick(player);
		if (player.level().getBrightness(LightLayer.BLOCK, player.blockPosition()) < 4) {
		//System.out.println("BRIGHT" + player.level().getBrightness(LightLayer.BLOCK, player.blockPosition()));
			player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION,250, 100, true, true));
			player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 100, 100, true, true));
			player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 100, true, true));

		}else { 
			player.removeEffect(MobEffects.NIGHT_VISION);
			player.removeEffect(MobEffects.DAMAGE_BOOST);
			player.removeEffect(MobEffects.DAMAGE_RESISTANCE);

			player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 10, 1, true, true));

		}
	}

	@Override
	public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, Player player) {
		return super.onBlockStartBreak(itemstack, pos, player);

	}

	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		tooltip.add(Component.literal("Consumes light and thrives in darkness").withStyle(ChatFormatting.ITALIC));
	}

}
