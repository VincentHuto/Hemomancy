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
		int introPgCount = 0,vascPgCount= 0,tendPgCount= 0,manipPgCount= 0,multiblockPgCount= 0;
		
		introPages.add(new HemoGuideTOC("Intro"));
		introPages.add(new HemoGuidePage(introPgCount++, "Intro", "Hemomancy", "No its not Blood Magic",
				"Welcome to Hemomancy! My first(released) major mod, This is a magic mod centered around blood, and blood control, focusing more so on the quality and efficency of blood rather than quantity."));
		introPages.add(new HemoGuidePage(introPgCount++, "Intro", "Intro", "Getting to know yourself",
				"ejfiuoejhwiofehjwiofhnjewifnhjuew", new ItemStack(Items.BOOK)));

		vasuclarPages.add(new HemoGuideTOC("Vascularity"));
		vasuclarPages.add(new HemoGuidePage(vascPgCount++, "Vascularity", "Hemomancy", "No its not Blood Magic", ""));

		manipPages.add(new HemoGuideTOC("Manips"));
		manipPages.add(new HemoGuidePage(manipPgCount++, "Manips", "Manipulations", "Directing the flow",
				"ejfiuoejhwiofehjwiofhnjewifnhjuew", new ItemStack(ItemInit.blood_stained_stone.get())));
		manipPages.add(new HemoGuidePage(manipPgCount++, "Manips", "Hematic Memory", "Remembering your potential",
				"ejfiuoejhwiofehjwiofhnjewifnhjuew", new ItemStack(ItemInit.hematic_memory.get())));
		manipPages.add(new HemoGuidePage(manipPgCount++, "Manips", "Visceral Recaller", "Jog your memory",
				"ejfiuoejhwiofehjwiofhnjewifnhjuew", new ItemStack(BlockInit.visceral_artificial_recaller.get())));

		tendencyPages.add(new HemoGuideTOC("Tendency"));
		tendencyPages.add(new HemoGuidePage(tendPgCount++, "Tendency", "Old Habits Never Die"));

		
		multiblockPages.add(new HemoGuideTOC("Multiblocks"));
		multiblockPages.add(new HemoGuideBloodStructurePage(multiblockPgCount++, "Multiblocks", "Liber Sanguinum", "Bloody Book",
				"This is a test for when I need to type up a description underneath the multiblock itself",
				new ItemStack(ItemInit.liber_sanguinum.get()), BloodCraftingRecipes.liber_sanguinum_recipe));
		multiblockPages.add(new HemoGuideBloodStructurePage(multiblockPgCount++, "Multiblocks", "Living Staff", "",
				"This is a test for when I need to type up a description underneath the multiblock itself",
				new ItemStack(ItemInit.living_staff.get()), BloodCraftingRecipes.living_staff_recipe));
		multiblockPages.add(new HemoGuideBloodStructurePage(multiblockPgCount++, "Multiblocks", "S.S.C", "",
				"This is a test for when I need to type up a description underneath the multiblock itself",
				new ItemStack(BlockInit.semi_sentient_construct.get()), BloodCraftingRecipes.ssc_recipe));
		multiblockPages.add(new HemoGuideBloodStructurePage(multiblockPgCount++, "Multiblocks", "Hematic Iron", "Bloody Book",
				"This is a test for when I need to type up a description underneath the multiblock itself",
				new ItemStack(BlockInit.hematic_iron_block.get()), BloodCraftingRecipes.hematic_iron_recipe));
		multiblockPages.add(new HemoGuideBloodStructurePage(multiblockPgCount++, "Multiblocks", "Unstained Pillar", "",
				"This is a test for when I need to type up a description underneath the multiblock itself",
				new ItemStack(BlockInit.unstained_podium.get()), BloodCraftingRecipes.unstained_pillar_recipe));
		multiblockPages.add(new HemoGuideBloodStructurePage(multiblockPgCount++, "Multiblocks", "Morphling Incubator", "",
				"This is a test for when I need to type up a description underneath the multiblock itself",
				new ItemStack(BlockInit.morphling_incubator.get()), BloodCraftingRecipes.morphling_incubator_recipe));

		registerChapters();
	}

	@Override
	public void registerChapters() {
		introChapter = new TomeChapter("Intro", TabColor.BLACK, new HemoGuideTOC("Intro"), introPages);
		vasuclarChapter = new TomeChapter("Vascularity", TabColor.RED, new HemoGuideTOC("Vascularity"), vasuclarPages);
		manipChapter = new TomeChapter("Manips", TabColor.BLACK, new HemoGuideTOC("Manips"), manipPages);
		tendencyChapter = new TomeChapter("Tendency", TabColor.RED, new HemoGuideTOC("Tendency"), tendencyPages);
		multiblockChapter = new TomeChapter("Multiblocks", TabColor.PURPLE, new HemoGuideTOC("Multiblocks"),
				multiblockPages);
		Collections.addAll(chapters, introChapter, vasuclarChapter, manipChapter, tendencyChapter, multiblockChapter);

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
