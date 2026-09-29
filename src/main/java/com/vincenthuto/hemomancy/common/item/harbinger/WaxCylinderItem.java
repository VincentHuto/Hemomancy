package com.vincenthuto.hemomancy.common.item.harbinger;

import com.vincenthuto.hemomancy.common.init.DataComponentInit;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class WaxCylinderItem extends Item {
	public WaxCylinderItem() {
		super(new Properties().stacksTo(16));
	}

	@Override public Component getName(ItemStack stack) {
		return stack.has(DataComponentInit.RESONANT_PATTERN.get())
				? Component.translatable("item.hemomancy.wax_cylinder.pattern") : super.getName(stack);
	}

	@Override public void appendHoverText(ItemStack stack, TooltipContext context,
			List<Component> lines, TooltipFlag flags) {
		AmbergrisCylinderItem.appendCylinderHoverText(stack, context, lines, flags);
	}
}
