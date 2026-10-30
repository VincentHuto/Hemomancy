package com.vincenthuto.hemomancy.mixin.core;

import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** The creative grid's first visible row, for the main tab's section banners. */
@Mixin(CreativeModeInventoryScreen.ItemPickerMenu.class)
public interface CreativePickerMenuInvoker {
	@Invoker(value = "getRowIndexForScroll", remap = false)
	int hemomancy$rowIndexForScroll(float scrollOffs);
}
