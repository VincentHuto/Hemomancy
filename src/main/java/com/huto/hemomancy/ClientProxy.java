package com.huto.hemomancy;

import com.huto.hemomancy.event.ClientEventSubscriber;
import com.huto.hemomancy.gui.mindrunes.GuiRuneBinderViewer;
import com.huto.hemomancy.gui.morphlingjar.GuiMorphlingJarViewer;
import com.huto.hemomancy.init.ItemInit;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

public class ClientProxy implements IProxy {



	@Override
	public void openBinderGui() {
		Minecraft.getInstance().displayGuiScreen(new GuiRuneBinderViewer(new ItemStack(ItemInit.rune_binder.get()),
				ClientEventSubscriber.getClientPlayer()));
	}
	
	@Override
	public void openJarGui() {
		Minecraft.getInstance().displayGuiScreen(new GuiMorphlingJarViewer(new ItemStack(ItemInit.morphling_jar.get()),
				ClientEventSubscriber.getClientPlayer()));
	}
}
