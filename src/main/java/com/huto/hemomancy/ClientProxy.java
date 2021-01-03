package com.huto.hemomancy;

import com.huto.hemomancy.event.ClientEventSubscriber;
import com.huto.hemomancy.gui.mindrunes.GuiRuneBinderViewer;
import com.huto.hemomancy.registries.ItemInit;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

public class ClientProxy implements IProxy {



	@Override
	public void openBinderGui() {
		Minecraft.getInstance().displayGuiScreen(new GuiRuneBinderViewer(new ItemStack(ItemInit.rune_binder.get()),
				ClientEventSubscriber.getClientPlayer()));
	}
}
