package com.vincenthuto.hemomancy.gui.guide;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hutoslib.client.screen.guide.GuiGuideTitlePage;
import com.vincenthuto.hutoslib.client.screen.guide.TomeLib;

import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class HemoTitlePage extends GuiGuideTitlePage {

	public HemoTitlePage() {
		super(new TextComponent("Hemomancy"), new ItemStack(Items.LAVA_BUCKET), HemoLib.chapters,
				new ResourceLocation(Hemomancy.MOD_ID, "textures/gui/hemo_overlay.png"));
	}

	@Override
	public TomeLib getOwnerTome() {
		return new HemoLib();
	}
}
