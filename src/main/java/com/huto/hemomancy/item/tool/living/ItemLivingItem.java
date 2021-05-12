package com.huto.hemomancy.item.tool.living;

import com.hutoslib.util.TextUtils;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemLivingItem extends Item {

	public ItemLivingItem(Properties properties) {
		super(properties);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public ITextComponent getDisplayName(ItemStack stack) {
		return new StringTextComponent(TextUtils
				.stringToBloody(TextUtils.convertInitToLang(stack.getItem().getRegistryName().getPath())))
						.mergeStyle(TextFormatting.DARK_RED);
	}

	@Override
	public int getEntityLifespan(ItemStack itemStack, World world) {
		return 0;
	}
}
