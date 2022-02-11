package com.vincenthuto.hemomancy.gui.guide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.vincenthuto.hemomancy.init.BlockInit;
import com.vincenthuto.hemomancy.init.ItemInit;
import com.vincenthuto.hemomancy.recipe.BloodCraftingRecipes;
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
	public static List<GuiGuidePage> multiblockPages = new ArrayList<GuiGuidePage>();

	public static TomeChapter introChapter, vasuclarChapter, tendencyChapter, manipChapter, multiblockChapter;

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

		multiblockPages.add(new HemoGuideTOC("Multiblocks"));
		multiblockPages.add(new HemoGuideMultiblockPage(1, "Multiblocks", "Liber Sanguinum", "Bloody Book",
				"This is a test for when I need to type up a description underneath the multiblock itself",
				new ItemStack(ItemInit.liber_sanguinum.get()),
				BloodCraftingRecipes.liber_sanguinum_recipe.getMultiblockPattern()));
		multiblockPages.add(new HemoGuideMultiblockPage(2, "Multiblocks", "Living Staff",
				new ItemStack(ItemInit.living_staff.get()),
				BloodCraftingRecipes.living_staff_recipe.getMultiblockPattern()));
		multiblockPages.add(new HemoGuideMultiblockPage(3, "Multiblocks", "Living Grasp",
				new ItemStack(ItemInit.living_grasp.get()),
				BloodCraftingRecipes.living_grip_recipe.getMultiblockPattern()));
		multiblockPages.add(new HemoGuideMultiblockPage(4, "Multiblocks", "S.S.C",
				new ItemStack(BlockInit.semi_sentient_construct.get()),
				BloodCraftingRecipes.ssc_recipe.getMultiblockPattern()));
		multiblockPages.add(new HemoGuideMultiblockPage(5, "Multiblocks", "Hematic Iron",
				new ItemStack(BlockInit.hematic_iron_block.get()),
				BloodCraftingRecipes.hematic_iron_recipe.getMultiblockPattern()));
		multiblockPages.add(new HemoGuideMultiblockPage(6, "Multiblocks", "Unstained Pillar",
				new ItemStack(BlockInit.unstained_podium.get()),
				BloodCraftingRecipes.unstained_pillar_recipe.getMultiblockPattern()));
		multiblockPages.add(new HemoGuideMultiblockPage(7, "Multiblocks", "Morphling Incubator",
				new ItemStack(BlockInit.morphling_incubator.get()),
				BloodCraftingRecipes.morphling_incubator_recipe.getMultiblockPattern()));

		registerChapters();
	}

	@Override
	public void registerChapters() {
		introChapter = new TomeChapter("Intro", TabColor.BLACK, new HemoGuideTOC("Intro"), introPages);
		vasuclarChapter = new TomeChapter("Vascularity", TabColor.RED, new HemoGuideTOC("Vascularity"), vasuclarPages);
		tendencyChapter = new TomeChapter("Tendency", TabColor.BLACK, new HemoGuideTOC("Tendency"), tendencyPages);
		manipChapter = new TomeChapter("Manips", TabColor.RED, new HemoGuideTOC("Manips"), manipPages);
		multiblockChapter = new TomeChapter("Multiblocks", TabColor.PURPLE, new HemoGuideTOC("Multiblocks"),
				multiblockPages);

		Collections.addAll(chapters, introChapter, vasuclarChapter, tendencyChapter, manipChapter, multiblockChapter);

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
