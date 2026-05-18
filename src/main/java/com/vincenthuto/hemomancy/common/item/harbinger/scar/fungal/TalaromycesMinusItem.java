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

public class TalaromycesMinusItem extends ItemFungalScar {

	public TalaromycesMinusItem(Properties properties, EnumBloodTendency tendencyIn, float deepenAmountIn) {
		super(properties, tendencyIn, deepenAmountIn);
	}

	@Override
	public void onWornTick(LivingEntity player) {
		super.onWornTick(player);
		player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED));
	}
	
	
	@Override
	public void onEquipped(LivingEntity player) {
		super.onEquipped(player);
	}
	
	@Override
	public void onUnequipped(LivingEntity player) {
		super.onUnequipped(player);
	}
	
	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, context, tooltip, flagIn);
		tooltip.add(Component.literal("A quiet decomposer. The hands quicken — the scar is impatient.").withStyle(ChatFormatting.ITALIC));
	}

}
