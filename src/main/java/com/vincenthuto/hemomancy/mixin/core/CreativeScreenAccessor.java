package com.vincenthuto.hemomancy.mixin.core;

import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/** What the main tab's frame reads from the creative screen: the open tab, the scroll and a tab's column. */
@Mixin(CreativeModeInventoryScreen.class)
public interface CreativeScreenAccessor {
	@Accessor(value = "selectedTab", remap = false)
	static CreativeModeTab hemomancy$selectedTab() {
		throw new AssertionError();
	}

	@Accessor(value = "scrollOffs", remap = false)
	float hemomancy$scrollOffs();

	@Invoker(value = "getTabX", remap = false)
	int hemomancy$tabX(CreativeModeTab tab);
}
