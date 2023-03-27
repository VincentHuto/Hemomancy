package com.vincenthuto.hemomancy.gui.guide;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.init.ItemInit;
import com.vincenthuto.hutoslib.client.screen.guide.GuiGuideTitlePage;
import com.vincenthuto.hutoslib.client.screen.guide.TomeLib;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class HemoTitlePage extends GuiGuideTitlePage {

	public HemoTitlePage() {
		super(Component.literal("Hemomancy"), new ItemStack(ItemInit.sanguine_formation.get()), HemoLib.chapters,
				Hemomancy.rloc("textures/gui/hemo_overlay.png"));
	}

	@Override
	public TomeLib getOwnerTome() {
		return new HemoLib();
	}
}
