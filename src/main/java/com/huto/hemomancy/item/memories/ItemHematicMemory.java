package com.huto.hemomancy.item.memories;

import java.util.List;

import com.hutoslib.util.TextUtils;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemHematicMemory extends Item {

	public ItemHematicMemory(Properties properties) {
		super(properties.maxStackSize(64));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public ITextComponent getDisplayName(ItemStack stack) {
		return new StringTextComponent(
				TextUtils.stringToBloody(TextUtils.convertInitToLang(stack.getItem().getRegistryName().getPath())))
						.mergeStyle(TextFormatting.DARK_RED);
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		tooltip.add(new StringTextComponent("Used to recall ancient whispers"));
	}
}