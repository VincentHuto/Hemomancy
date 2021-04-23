package com.huto.hemomancy.block;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.fml.RegistryObject;

public class BlockTestSupplier extends Block {
	RegistryObject<Item> item;

	public BlockTestSupplier(Properties properties, RegistryObject<Item> item) {
		super(properties);
		this.item = item;
	}


	@Override
	public void addInformation(ItemStack stack, IBlockReader worldIn, List<ITextComponent> tooltip,
			ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		if (item != null) {
			tooltip.add(new StringTextComponent(item.get().toString()));
		}
	}

}
