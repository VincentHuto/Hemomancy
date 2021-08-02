package com.huto.hemomancy.item.rune;

import java.util.List;

import com.huto.hemomancy.capa.rune.IRune;
import com.huto.hemomancy.capa.tendency.EnumBloodTendency;

import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.potion.Effects;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class ItemGuidanceRune extends ItemRune implements IRune {

	public ItemGuidanceRune(Properties properties, EnumBloodTendency tendencyIn, int deepenAmount) {
		super(properties, tendencyIn, deepenAmount);
		properties.maxStackSize(1);

	}

	@Override
	public void onWornTick(LivingEntity player) {
		IRune.super.onWornTick(player);
		player.addPotionEffect(new EffectInstance(Effects.NIGHT_VISION, 250, 1, false, false));
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		tooltip.add(new StringTextComponent(TextFormatting.GOLD + "Effect: Night Vision"));
	}

}
