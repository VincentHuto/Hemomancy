package com.vincenthuto.hemomancy.item.rune;

import java.util.List;

import com.vincenthuto.hemomancy.capa.player.rune.IRune;
import com.vincenthuto.hemomancy.capa.player.tendency.EnumBloodTendency;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class ItemGuidanceRune extends ItemRune implements IRune {

	public ItemGuidanceRune(Properties properties, EnumBloodTendency tendencyIn, int deepenAmount) {
		super(properties, tendencyIn, deepenAmount);
		properties.stacksTo(1);

	}

	@Override
	public void onWornTick(LivingEntity player) {
		IRune.super.onWornTick(player);
		player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 250, 1, false, false));
	}

	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		tooltip.add(new TextComponent(ChatFormatting.GOLD + "MobEffect: Night Vision"));
	}

}
