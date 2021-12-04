package com.vincenthuto.hemomancy.gui.guide;

import java.util.List;

import com.vincenthuto.hutoslib.client.screen.guide.GuiGuidePage;
import com.vincenthuto.hutoslib.client.screen.guide.GuiGuidePageTOC;
import com.vincenthuto.hutoslib.client.screen.guide.TomeLib;

public class HemoGuideTOC extends GuiGuidePageTOC {

	public HemoGuideTOC(String catagoryIn) {
		super(catagoryIn);
	}

	@Override
	public List<GuiGuidePage> getPages() {
		return getOwnerTome().getMatchingChapters(getCatagory()).pages;

	}

	@Override
	public TomeLib getOwnerTome() {
		return new HemoLib();
	}

}
