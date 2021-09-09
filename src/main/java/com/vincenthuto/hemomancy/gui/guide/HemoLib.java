package com.vincenthuto.hemomancy.gui.guide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.vincenthuto.hutoslib.client.screen.guide.GuiGuidePage;
import com.vincenthuto.hutoslib.client.screen.guide.GuiGuideTitlePage;
import com.vincenthuto.hutoslib.client.screen.guide.TomeChapter;
import com.vincenthuto.hutoslib.client.screen.guide.TomeLib;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class HemoLib extends TomeLib {

	public static List<TomeChapter> chapters = new ArrayList<TomeChapter>();;
	public static List<GuiGuidePage> bloodPages = new ArrayList<GuiGuidePage>();
	public static List<GuiGuidePage> manipPages = new ArrayList<GuiGuidePage>();
	public static TomeChapter bloodChapter;
	public static TomeChapter manipChapter;

	@Override
	public void registerTome() {
		bloodPages.add(new HemoGuideTOC("Blood"));
		bloodPages.add(new HemoGuidePage(1, "Blood"));

		bloodPages.add(new HemoGuidePage(2, "Blood", "efuwijjjjjjjjjfejiufhewiufhewuifhweuifhewif"));

		manipPages.add(new HemoGuideTOC("Manips"));
		manipPages.add(new HemoGuidePage(1, "Manips", "MANUIUIDI*WQJHDUIWQHNUIJDWQHBNUDJIHNUIQ"));
		manipPages.add(new HemoGuidePage(2, "Manips", "Manips", "Control", "ejfiuoejhwiofehjwiofhnjewifnhjuew",
				new ItemStack(Items.APPLE)));
		registerChapters();
	}

	@Override
	public void registerChapters() {
		bloodChapter = new TomeChapter("Blood", new HemoGuideTOC("Blood"), bloodPages);
		manipChapter = new TomeChapter("Manips", new HemoGuideTOC("Manips"), manipPages);
		Collections.addAll(chapters, bloodChapter, manipChapter);
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
