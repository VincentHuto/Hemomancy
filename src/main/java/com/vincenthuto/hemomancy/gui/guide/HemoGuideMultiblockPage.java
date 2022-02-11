package com.vincenthuto.hemomancy.gui.guide;

import com.vincenthuto.hutoslib.client.render.block.MultiblockPattern;
import com.vincenthuto.hutoslib.client.screen.guide.GuiGuideMultiblockPage;
import com.vincenthuto.hutoslib.client.screen.guide.TomeLib;

import net.minecraft.world.item.ItemStack;

public class HemoGuideMultiblockPage extends GuiGuideMultiblockPage {

	public HemoGuideMultiblockPage(int pageNumIn, String catagoryIn, MultiblockPattern pattern) {
		super(pageNumIn, catagoryIn, pattern);
	}

	public HemoGuideMultiblockPage(int pageNumIn, String catagoryIn, String titleIn, MultiblockPattern pattern) {
		super(pageNumIn, catagoryIn, titleIn, pattern);
	}

	public HemoGuideMultiblockPage(int pageNumIn, String catagoryIn, String titleIn, String textIn,
			MultiblockPattern pattern) {
		super(pageNumIn, catagoryIn, titleIn, textIn, pattern);
	}

	public HemoGuideMultiblockPage(int pageNumIn, String catagoryIn, String titleIn, String subtitleIn, String textIn,
			MultiblockPattern pattern) {
		super(pageNumIn, catagoryIn, titleIn, subtitleIn, textIn, pattern);
	}

	public HemoGuideMultiblockPage(int pageNumIn, String categoryIn, String titleIn, String subtitleIn, String textIn,
			ItemStack iconIn, MultiblockPattern pattern) {
		super(pageNumIn, categoryIn, titleIn, subtitleIn, textIn, pattern);

	}

	public HemoGuideMultiblockPage(int pageNumIn, String categoryIn, String titleIn, ItemStack iconIn,
			MultiblockPattern pattern) {
		super(pageNumIn, categoryIn, titleIn, "", "", iconIn, pattern);
	}

	@Override
	public TomeLib getOwnerTome() {
		return new HemoLib();
	}

}
