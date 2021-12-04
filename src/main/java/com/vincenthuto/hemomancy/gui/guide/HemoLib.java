package com.vincenthuto.hemomancy.gui.guide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.vincenthuto.hutoslib.client.screen.guide.GuiGuidePage;
import com.vincenthuto.hutoslib.client.screen.guide.GuiGuideTitlePage;
import com.vincenthuto.hutoslib.client.screen.guide.TomeCategoryTab.TabColor;
import com.vincenthuto.hutoslib.client.screen.guide.TomeChapter;
import com.vincenthuto.hutoslib.client.screen.guide.TomeLib;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class HemoLib extends TomeLib {

	public static List<TomeChapter> chapters = new ArrayList<TomeChapter>();;
	public static List<GuiGuidePage> introPages = new ArrayList<GuiGuidePage>();
	public static List<GuiGuidePage> vasuclarPages = new ArrayList<GuiGuidePage>();
	public static List<GuiGuidePage> tendencyPages = new ArrayList<GuiGuidePage>();
	public static List<GuiGuidePage> manipPages = new ArrayList<GuiGuidePage>();
	public static TomeChapter introChapter, vasuclarChapter, tendencyChapter, manipChapter;

	@Override
	public void registerTome() {

		introPages.add(new HemoGuideTOC("Intro"));
		introPages.add(new HemoGuidePage(1, "Intro", "MANUIUIDI*WQJHDUIWQHNUIJDWQHBNUDJIHNUIQ"));
		introPages.add(new HemoGuidePage(2, "Intro", "Intro", "Getting to know yourself",
				"ejfiuoejhwiofehjwiofhnjewifnhjuew", new ItemStack(Items.BOOK)));

		vasuclarPages.add(new HemoGuideTOC("Vascularity"));
		vasuclarPages.add(new HemoGuidePage(1, "Vascularity"));
		vasuclarPages.add(new HemoGuidePage(2, "Vascularity", "efuwijjjjjjjjjfejiufhewiufhewuifhweuifhewif"));

		tendencyPages.add(new HemoGuideTOC("Tendency"));
		tendencyPages.add(new HemoGuidePage(1, "Tendency", "MANUIUIDI*WQJHDUIWQHNUIJDWQHBNUDJIHNUIQ"));
		tendencyPages.add(new HemoGuidePage(2, "Tendency", "Manips", "Control", "ejfiuoejhwiofehjwiofhnjewifnhjuew",
				new ItemStack(Items.PUMPKIN)));

		manipPages.add(new HemoGuideTOC("Manips"));
		manipPages.add(new HemoGuidePage(1, "Manips", "MANUIUIDI*WQJHDUIWQHNUIJDWQHBNUDJIHNUIQ"));
		manipPages.add(new HemoGuidePage(2, "Manips", "Manips", "Control", "ejfiuoejhwiofehjwiofhnjewifnhjuew",
				new ItemStack(Items.APPLE)));
		registerChapters();
	}

	@Override
	public void registerChapters() {
		introChapter = new TomeChapter("Intro", TabColor.BLACK, introPages);
		vasuclarChapter = new TomeChapter("Vascularity", TabColor.RED, vasuclarPages);
		tendencyChapter = new TomeChapter("Tendency", TabColor.BLACK, tendencyPages);
		manipChapter = new TomeChapter("Manips", TabColor.RED, manipPages);
		Collections.addAll(chapters, introChapter, vasuclarChapter, tendencyChapter, manipChapter);
	}

	@Override
	public GuiGuideTitlePage getTitle() {
		return new HemoTitlePage();
	}

	@Override
	public List<TomeChapter> getChapters() {
		return chapters;
	}

}
