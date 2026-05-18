package com.vincenthuto.hemomancy.common.item.harbinger.scar.fungal;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class RespergillusItem extends ItemFungalScar {

	public RespergillusItem(Properties properties, EnumBloodTendency tendencyIn, float deepenAmountIn) {
		super(properties, tendencyIn, deepenAmountIn);
	}

	@Override
	public void onWornTick(LivingEntity player) {
		super.onWornTick(player);
		player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING));
	}
	
	@Override
	public void onEquipped(LivingEntity player) {
		super.onEquipped(player);
		player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, -1, 1, true, true));

	}

	@Override
	public void onUnequipped(LivingEntity player) {
		super.onUnequipped(player);
		player.removeEffect(MobEffects.WATER_BREATHING);
	}


	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, context, tooltip, flagIn);
		tooltip.add(Component.literal("Draws moisture from the blood. The gills feel borrowed.").withStyle(ChatFormatting.ITALIC));
	}

}
