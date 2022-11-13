package com.vincenthuto.hemomancy.gui.guide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.init.BlockInit;
import com.vincenthuto.hemomancy.init.ItemInit;
import com.vincenthuto.hutoslib.client.screen.guide.GuiGuidePage;
import com.vincenthuto.hutoslib.client.screen.guide.GuiGuideTitlePage;
import com.vincenthuto.hutoslib.client.screen.guide.TomeCategoryTab.TabColor;
import com.vincenthuto.hutoslib.client.screen.guide.TomeChapter;
import com.vincenthuto.hutoslib.client.screen.guide.TomeLib;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class HemoLib extends TomeLib {

	public static List<TomeChapter> chapters = new ArrayList<>();
	public static List<GuiGuidePage> introPages = new ArrayList<>();
	public static List<GuiGuidePage> vasuclarPages = new ArrayList<>();
	public static List<GuiGuidePage> tendencyPages = new ArrayList<>();
	public static List<GuiGuidePage> manipPages = new ArrayList<>();
	public static List<GuiGuidePage> multiblockPages = new ArrayList<>();

	public static TomeChapter introChapter, vasuclarChapter, tendencyChapter, manipChapter, multiblockChapter;

	@Override
	public List<TomeChapter> getChapters() {
		return chapters;
	}

	@Override
	public GuiGuideTitlePage getTitle() {
		return new HemoTitlePage();
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
	public void registerTome() {

		introPages.add(new HemoGuideTOC("Intro"));
		introPages.add(new HemoGuidePage(1, "Intro", "Hemomancy", "No its not Blood Magic",
				"Welcome to Hemomancy! My first(released) major mod, This is a magic mod centered around blood, and blood control, focusing more so on the quality and efficency of blood rather than quantity."));
		introPages.add(new HemoGuidePage(2, "Intro", "Intro", "Getting to know yourself",
				"ejfiuoejhwiofehjwiofhnjewifnhjuew", new ItemStack(Items.BOOK)));

		vasuclarPages.add(new HemoGuideTOC("Vascularity"));
		vasuclarPages.add(new HemoGuidePage(1, "Vascularity", "Hemomancy", "No its not Blood Magic", ""));

		manipPages.add(new HemoGuideTOC("Manips"));
		manipPages.add(new HemoGuidePage(1, "Manips", "Manipulations", "Directing the flow",
				"ejfiuoejhwiofehjwiofhnjewifnhjuew", new ItemStack(ItemInit.blood_stained_stone.get())));
		manipPages.add(new HemoGuidePage(2, "Manips", "Hematic Memory", "Remembering your potential",
				"ejfiuoejhwiofehjwiofhnjewifnhjuew", new ItemStack(ItemInit.hematic_memory.get())));
		manipPages.add(new HemoGuidePage(3, "Manips", "Visceral Recaller", "Jog your memory",
				"ejfiuoejhwiofehjwiofhnjewifnhjuew", new ItemStack(BlockInit.visceral_artificial_recaller.get())));

		tendencyPages.add(new HemoGuideTOC("Tendency"));
		tendencyPages.add(new HemoGuidePage(1, "Tendency", "Old Habits Never Die"));

		multiblockPages.add(new HemoGuideTOC("Multiblocks"));
		multiblockPages.add(new HemoGuideBloodStructurePage(1, "Multiblocks", "Liber Sanguinum", "Bloody Book",
				"This is a test for when I need to type up a description underneath the multiblock itself",
				new ItemStack(ItemInit.liber_sanguinum.get()),
				Hemomancy.rloc("blood_structure/liber_sanguinum")));

		multiblockPages.add(new HemoGuideBloodStructurePage(2, "Multiblocks", "Living Staff", "",
				"This is a test for when I need to type up a description underneath the multiblock itself",
				new ItemStack(ItemInit.living_staff.get()),
				Hemomancy.rloc("blood_structure/living_staff")));

		multiblockPages.add(new HemoGuideBloodStructurePage(3, "Multiblocks", "S.S.C", "",
				"This is a test for when I need to type up a description underneath the multiblock itself",
				new ItemStack(BlockInit.semi_sentient_construct.get()),
				Hemomancy.rloc("blood_structure/semi_sentient_construct")));

		multiblockPages.add(new HemoGuideBloodStructurePage(4, "Multiblocks", "Hematic Iron", "Bloody Book",
				"This is a test for when I need to type up a description underneath the multiblock itself",
				new ItemStack(BlockInit.hematic_iron_block.get()), Hemomancy.rloc("blood_structure/hematic_iron_block")));


		multiblockPages.add(new HemoGuideBloodStructurePage(5, "Multiblocks", "Unstained Pillar", "",
				"This is a test for when I need to type up a description underneath the multiblock itself",
				new ItemStack(BlockInit.unstained_podium.get()), Hemomancy.rloc("blood_structure/unstained_pillar")));
		multiblockPages.add(new HemoGuideBloodStructurePage(6, "Multiblocks", "Morphling Incubator", "",
				"This is a test for when I need to type up a description underneath the multiblock itself",
				new ItemStack(BlockInit.morphling_incubator.get()), Hemomancy.rloc("blood_structure/morphling_incubator")));

		registerChapters();
	}

}
