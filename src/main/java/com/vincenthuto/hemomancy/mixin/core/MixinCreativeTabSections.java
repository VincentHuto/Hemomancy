package com.vincenthuto.hemomancy.mixin.core;

import java.util.Collection;

import com.vincenthuto.hemomancy.client.screen.inventory.CreativeTabChrome;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Pads the main Hemomancy tab's grid into banded sections whenever its items are loaded, and keeps clicks off the
 * banner rows. The layout and drawing live in {@link CreativeTabChrome}.
 */
@Mixin(CreativeModeInventoryScreen.class)
public abstract class MixinCreativeTabSections {
	@Shadow(remap = false)
	private static CreativeModeTab selectedTab;

	@Shadow(remap = false)
	private float scrollOffs;

	@Inject(method = "selectTab", at = @At("TAIL"), remap = false)
	private void hemomancy$layoutOnSelect(CreativeModeTab tab, CallbackInfo ci) {
		hemomancy$layout();
	}

	@Inject(method = "refreshCurrentTabContents", at = @At("TAIL"), remap = false)
	private void hemomancy$layoutOnRefresh(Collection<ItemStack> items, CallbackInfo ci) {
		hemomancy$layout();
	}

	@Inject(method = "slotClicked", at = @At("HEAD"), cancellable = true, remap = false)
	private void hemomancy$ignoreBannerRows(Slot slot, int slotId, int mouseButton, ClickType type, CallbackInfo ci) {
		CreativeModeInventoryScreen screen = (CreativeModeInventoryScreen) (Object) this;
		if (CreativeTabChrome.isHemomancyTab(selectedTab) && slot != null
				&& CreativeTabChrome.isBannerSlot(screen, slotId)) {
			ci.cancel();
		}
	}

	private void hemomancy$layout() {
		CreativeModeInventoryScreen.ItemPickerMenu menu = ((CreativeModeInventoryScreen) (Object) this).getMenu();
		CreativeTabChrome.layout(selectedTab, menu.items);
		menu.scrollTo(scrollOffs);
	}
}
