package com.huto.hemomancy.gui.guide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.init.BlockInit;
import com.huto.hemomancy.init.ItemInit;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuideBookLib {

	public static List<GuiGuidePage> IntroPageList = new ArrayList<GuiGuidePage>();
	public static List<GuiGuidePage> VascularPageList = new ArrayList<GuiGuidePage>();
	public static List<GuiGuidePage> TendencyPageList = new ArrayList<GuiGuidePage>();
	public static List<GuiGuidePage> ManipulationPageList = new ArrayList<GuiGuidePage>();
	public static List<GuiGuidePageTOC> TOCPageList = new ArrayList<GuiGuidePageTOC>();
	public static List<List<GuiGuidePage>> ChapterList = new ArrayList<List<GuiGuidePage>>();

	// Text Locations
	public static String INTRO_PAGE_1 = "The big bang created the universe, making existence out of nothing, pushing everything out from a central point with incredible momentum. With this massive amount of force, came an equal production of both antimatter and standard matter. From the start, everything had it's opposite. This fact can be claimed to be one of the few principles in existence that always holds true, be it science or magic. The forging of the universe is not a relic of ages past, literal reverberations from the explosion can still be felt and interacted with.";
	public static String INTRO_PAGE_2 = "The reverberations previously mentioned are in a sense, cosmic whiplash on a universal scale. As the singularity expanded, every particle was thrown around in every which way. While at first, this was cause for chaos, the wheels of time have marched on and calmed things down as they do rendering the vibrations very minute, but minute is not zero. Minute for one atom can be magnified exponentially when concentrated enough, resonating with one another allowing for the force of the big bang to be felt once more";
	public static String INTRO_PAGE_3 = "One of the quirks of the antimatter/matter duality is that the vibrations of these particles are opposite and equal as well. These vibrations follow the same principle that all other forms of vibration be it sound/light/electricity etc, in that they amplify when met with similar waves and cancel when met with opposing waves. Building on the concentration effect of these vibrations, the idea of karma can even be traced back as the balance of these vibrations within your body, and mana is just the net force of the anti and positive waves manifesting as a physical and invisible force.";
	public static String INTRO_PAGE_4 = "Matter and antimatter can become \"vibratory\" in some rare examples simply by happenstance when enough of a single frequency of resonating matter comes into close proximity and will somewhat meld together. When they coalesce they are called \"drops\" or \"tears\" for vibrations and antivibration respectively with \"essence\" being the overarching term for both forms of concentrated vibrational matter. Essence can be found in all forms of life with a very low concentration but can be coerced out of them upon death with specialized tools discussed later.";
	public static String INTRO_PAGE_5 = "The process of inducing vibrations is known as \"Channeling\", somnolental sounding in nature, but the hiddens can be blamed for this. This can be done in a variety of ways all modeled off of trying to artificially induce the natural essence production, by essentially taking a chunk of vibratory or \"conductive\" matter and allowing it to collect more and more matter that begins to resonate with it, which is an exponential process. The beauty of this process is that once an original drop or tear of matter is created, it will naturally start to produce more of itself with little aide.If only there was a way to contain this growth...";
	public static String INTRO_PAGE_6 = "As mentioned prior, these vibrations behave the same way that any other wave does, and as such can be stopped or dampened with an opposite waveform. Producing this waveform can be tricky but with a little bit of experimenting, discovering that the more your around concentrated matter, the more sensitive you become to it, beginning to be able to sense slight differences in direction and amplitude each drop. By using matter of specific frequency you can change the characteristics of other matter by forcing contact with the target. The process of canceling vibrations intentionally is called \"Nullification\" and is incredibly useful.";
	public static String INTRO_PAGE_7 = "Due to the nature of life itself, being inherently organized, living beings are naturally enriched with essence, almost completely filled with positive vibrations in fact. This is not to say that ALL creatures are positive, there are some exceptions that have net negative vibrations(withers and end creatures), and some that have none at all(skeletons and other inorganic life)! Because of this, the essence can be farmed quite readily through the process of death, with the correct tools, of course, ones that nullified vibrations maybe?";
	public static String INTRO_PAGE_8 = "title.somnolenttome.intro.page.8.text";

	public static String VASCULAR_PAGE_1 = "The big bang created the universe, making existence out of nothing, pushing everything out from a central point with incredible momentum. With this massive amount of force, came an equal production of both antimatter and standard matter. From the start, everything had it's opposite. This fact can be claimed to be one of the few principles in existence that always holds true, be it science or magic. The forging of the universe is not a relic of ages past, literal reverberations from the explosion can still be felt and interacted with.";
	public static String VASCULAR_PAGE_2 = "The reverberations previously mentioned are in a sense, cosmic whiplash on a universal scale. As the singularity expanded, every particle was thrown around in every which way. While at first, this was cause for chaos, the wheels of time have marched on and calmed things down as they do rendering the vibrations very minute, but minute is not zero. Minute for one atom can be magnified exponentially when concentrated enough, resonating with one another allowing for the force of the big bang to be felt once more";
	public static String VASCULAR_PAGE_3 = "One of the quirks of the antimatter/matter duality is that the vibrations of these particles are opposite and equal as well. These vibrations follow the same principle that all other forms of vibration be it sound/light/electricity etc, in that they amplify when met with similar waves and cancel when met with opposing waves. Building on the concentration effect of these vibrations, the idea of karma can even be traced back as the balance of these vibrations within your body, and mana is just the net force of the anti and positive waves manifesting as a physical and invisible force.";
	public static String VASCULAR_PAGE_4 = "Matter and antimatter can become \"vibratory\" in some rare examples simply by happenstance when enough of a single frequency of resonating matter comes into close proximity and will somewhat meld together. When they coalesce they are called \"drops\" or \"tears\" for vibrations and antivibration respectively with \"essence\" being the overarching term for both forms of concentrated vibrational matter. Essence can be found in all forms of life with a very low concentration but can be coerced out of them upon death with specialized tools discussed later.";
	public static String VASCULAR_PAGE_5 = "The process of inducing vibrations is known as \"Channeling\", somnolental sounding in nature, but the hiddens can be blamed for this. This can be done in a variety of ways all modeled off of trying to artificially induce the natural essence production, by essentially taking a chunk of vibratory or \"conductive\" matter and allowing it to collect more and more matter that begins to resonate with it, which is an exponential process. The beauty of this process is that once an original drop or tear of matter is created, it will naturally start to produce more of itself with little aide.If only there was a way to contain this growth...";
	public static String VASCULAR_PAGE_6 = "As mentioned prior, these vibrations behave the same way that any other wave does, and as such can be stopped or dampened with an opposite waveform. Producing this waveform can be tricky but with a little bit of experimenting, discovering that the more your around concentrated matter, the more sensitive you become to it, beginning to be able to sense slight differences in direction and amplitude each drop. By using matter of specific frequency you can change the characteristics of other matter by forcing contact with the target. The process of canceling vibrations intentionally is called \"Nullification\" and is incredibly useful.";
	public static String VASCULAR_PAGE_7 = "Due to the nature of life itself, being inherently organized, living beings are naturally enriched with essence, almost completely filled with positive vibrations in fact. This is not to say that ALL creatures are positive, there are some exceptions that have net negative vibrations(withers and end creatures), and some that have none at all(skeletons and other inorganic life)! Because of this, the essence can be farmed quite readily through the process of death, with the correct tools, of course, ones that nullified vibrations maybe?";
	public static String VASCULAR_PAGE_8 = "title.somnolenttome.intro.page.8.text";

	public static String TENDENCY_PAGE_1 = "The big bang created the universe, making existence out of nothing, pushing everything out from a central point with incredible momentum. With this massive amount of force, came an equal production of both antimatter and standard matter. From the start, everything had it's opposite. This fact can be claimed to be one of the few principles in existence that always holds true, be it science or magic. The forging of the universe is not a relic of ages past, literal reverberations from the explosion can still be felt and interacted with.";
	public static String TENDENCY_PAGE_2 = "The reverberations previously mentioned are in a sense, cosmic whiplash on a universal scale. As the singularity expanded, every particle was thrown around in every which way. While at first, this was cause for chaos, the wheels of time have marched on and calmed things down as they do rendering the vibrations very minute, but minute is not zero. Minute for one atom can be magnified exponentially when concentrated enough, resonating with one another allowing for the force of the big bang to be felt once more";
	public static String TENDENCY_PAGE_3 = "One of the quirks of the antimatter/matter duality is that the vibrations of these particles are opposite and equal as well. These vibrations follow the same principle that all other forms of vibration be it sound/light/electricity etc, in that they amplify when met with similar waves and cancel when met with opposing waves. Building on the concentration effect of these vibrations, the idea of karma can even be traced back as the balance of these vibrations within your body, and mana is just the net force of the anti and positive waves manifesting as a physical and invisible force.";
	public static String TENDENCY_PAGE_4 = "Matter and antimatter can become \"vibratory\" in some rare examples simply by happenstance when enough of a single frequency of resonating matter comes into close proximity and will somewhat meld together. When they coalesce they are called \"drops\" or \"tears\" for vibrations and antivibration respectively with \"essence\" being the overarching term for both forms of concentrated vibrational matter. Essence can be found in all forms of life with a very low concentration but can be coerced out of them upon death with specialized tools discussed later.";
	public static String TENDENCY_PAGE_5 = "The process of inducing vibrations is known as \"Channeling\", somnolental sounding in nature, but the hiddens can be blamed for this. This can be done in a variety of ways all modeled off of trying to artificially induce the natural essence production, by essentially taking a chunk of vibratory or \"conductive\" matter and allowing it to collect more and more matter that begins to resonate with it, which is an exponential process. The beauty of this process is that once an original drop or tear of matter is created, it will naturally start to produce more of itself with little aide.If only there was a way to contain this growth...";
	public static String TENDENCY_PAGE_6 = "As mentioned prior, these vibrations behave the same way that any other wave does, and as such can be stopped or dampened with an opposite waveform. Producing this waveform can be tricky but with a little bit of experimenting, discovering that the more your around concentrated matter, the more sensitive you become to it, beginning to be able to sense slight differences in direction and amplitude each drop. By using matter of specific frequency you can change the characteristics of other matter by forcing contact with the target. The process of canceling vibrations intentionally is called \"Nullification\" and is incredibly useful.";
	public static String TENDENCY_PAGE_7 = "Due to the nature of life itself, being inherently organized, living beings are naturally enriched with essence, almost completely filled with positive vibrations in fact. This is not to say that ALL creatures are positive, there are some exceptions that have net negative vibrations(withers and end creatures), and some that have none at all(skeletons and other inorganic life)! Because of this, the essence can be farmed quite readily through the process of death, with the correct tools, of course, ones that nullified vibrations maybe?";
	public static String TENDENCY_PAGE_8 = "title.somnolenttome.intro.page.8.text";

	public static String MANIPULATION_PAGE_1 = "The big bang created the universe, making existence out of nothing, pushing everything out from a central point with incredible momentum. With this massive amount of force, came an equal production of both antimatter and standard matter. From the start, everything had it's opposite. This fact can be claimed to be one of the few principles in existence that always holds true, be it science or magic. The forging of the universe is not a relic of ages past, literal reverberations from the explosion can still be felt and interacted with.";
	public static String MANIPULATION_PAGE_2 = "The reverberations previously mentioned are in a sense, cosmic whiplash on a universal scale. As the singularity expanded, every particle was thrown around in every which way. While at first, this was cause for chaos, the wheels of time have marched on and calmed things down as they do rendering the vibrations very minute, but minute is not zero. Minute for one atom can be magnified exponentially when concentrated enough, resonating with one another allowing for the force of the big bang to be felt once more";
	public static String MANIPULATION_PAGE_3 = "One of the quirks of the antimatter/matter duality is that the vibrations of these particles are opposite and equal as well. These vibrations follow the same principle that all other forms of vibration be it sound/light/electricity etc, in that they amplify when met with similar waves and cancel when met with opposing waves. Building on the concentration effect of these vibrations, the idea of karma can even be traced back as the balance of these vibrations within your body, and mana is just the net force of the anti and positive waves manifesting as a physical and invisible force.";
	public static String MANIPULATION_PAGE_4 = "Matter and antimatter can become \"vibratory\" in some rare examples simply by happenstance when enough of a single frequency of resonating matter comes into close proximity and will somewhat meld together. When they coalesce they are called \"drops\" or \"tears\" for vibrations and antivibration respectively with \"essence\" being the overarching term for both forms of concentrated vibrational matter. Essence can be found in all forms of life with a very low concentration but can be coerced out of them upon death with specialized tools discussed later.";
	public static String MANIPULATION_PAGE_5 = "The process of inducing vibrations is known as \"Channeling\", somnolental sounding in nature, but the hiddens can be blamed for this. This can be done in a variety of ways all modeled off of trying to artificially induce the natural essence production, by essentially taking a chunk of vibratory or \"conductive\" matter and allowing it to collect more and more matter that begins to resonate with it, which is an exponential process. The beauty of this process is that once an original drop or tear of matter is created, it will naturally start to produce more of itself with little aide.If only there was a way to contain this growth...";
	public static String MANIPULATION_PAGE_6 = "As mentioned prior, these vibrations behave the same way that any other wave does, and as such can be stopped or dampened with an opposite waveform. Producing this waveform can be tricky but with a little bit of experimenting, discovering that the more your around concentrated matter, the more sensitive you become to it, beginning to be able to sense slight differences in direction and amplitude each drop. By using matter of specific frequency you can change the characteristics of other matter by forcing contact with the target. The process of canceling vibrations intentionally is called \"Nullification\" and is incredibly useful.";
	public static String MANIPULATION_PAGE_7 = "Due to the nature of life itself, being inherently organized, living beings are naturally enriched with essence, almost completely filled with positive vibrations in fact. This is not to say that ALL creatures are positive, there are some exceptions that have net negative vibrations(withers and end creatures), and some that have none at all(skeletons and other inorganic life)! Because of this, the essence can be farmed quite readily through the process of death, with the correct tools, of course, ones that nullified vibrations maybe?";
	public static String MANIPULATION_PAGE_8 = "title.somnolenttome.intro.page.8.text";

	public static void registerPages() {

		IntroPageList.clear();
		VascularPageList.clear();
		TendencyPageList.clear();
		ManipulationPageList.clear();
		ChapterList.clear();
		TOCPageList.clear();

		// Intro
		IntroPageList.add(new GuiGuidePage(1, EnumTomeCatagories.INTRO, "In the Begining", "It Started Somewhere",
				new ItemStack(ItemInit.sanguine_formation.get()), I18n.get(INTRO_PAGE_1)));
		IntroPageList.add(new GuiGuidePage(2, EnumTomeCatagories.INTRO, "Duality of Nature", "Yin and Yang",
				new ItemStack(ItemInit.sanguine_formation.get()), I18n.get(INTRO_PAGE_2)));
		IntroPageList.add(new GuiGuidePage(3, EnumTomeCatagories.INTRO, "Vibes", "Cosmic Vibrations",
				new ItemStack(ItemInit.sanguine_formation.get()), I18n.get(INTRO_PAGE_3)));
		IntroPageList.add(new GuiGuidePage(4, EnumTomeCatagories.INTRO, "A Level of Essence", "Only the Essentials",
				new ItemStack(ItemInit.sanguine_formation.get()), I18n.get(INTRO_PAGE_4)));
		IntroPageList.add(new GuiGuidePage(5, EnumTomeCatagories.INTRO, "Channeling Basics", "Use the Force",
				new ItemStack(ItemInit.sanguine_formation.get()), I18n.get(INTRO_PAGE_5)));
		IntroPageList.add(new GuiGuidePage(6, EnumTomeCatagories.INTRO, "Nullification", "Counter Attack",
				new ItemStack(ItemInit.sanguine_formation.get()), I18n.get(INTRO_PAGE_6)));
		IntroPageList.add(new GuiGuidePage(7, EnumTomeCatagories.INTRO, "Vital Essence", "The Power of Vitals",
				new ItemStack(ItemInit.sanguine_formation.get()), I18n.get(INTRO_PAGE_7)));
		IntroPageList.add(new GuiGuideImagePage(8, EnumTomeCatagories.INTRO, "ImageTest", "Picture Yo", "",
				new GuiTomeImage(new ResourceLocation(Hemomancy.MOD_ID, "textures/gui/pageimages/blue.png"), 0, 0)));
		List<Component> textIn = new ArrayList<Component>();
		textIn.add(new TextComponent("Hello"));
		textIn.add(new TextComponent("I "));
		textIn.add(new TextComponent("Am"));
		textIn.add(new TextComponent("Red"));
		IntroPageList.add(new GuiGuideImagePage(9, EnumTomeCatagories.INTRO, "ImageTest2", "Pictures Yo", "",
				new GuiTomeImage(new ResourceLocation(Hemomancy.MOD_ID, "textures/gui/pageimages/altar1.png"), 0, 0),
				new GuiTomeImage(new ResourceLocation(Hemomancy.MOD_ID, "textures/gui/pageimages/altar2.png"), 1, 1),
				new GuiTomeImage(new ResourceLocation(Hemomancy.MOD_ID, "textures/gui/pageimages/altar3.png"), 2, 2),
				new GuiTomeImage(new ResourceLocation(Hemomancy.MOD_ID, "textures/gui/pageimages/altar4.png"), 3, 3),
				new GuiTomeImage(new ResourceLocation(Hemomancy.MOD_ID, "textures/gui/pageimages/altar5.png"), 4, 4),
				new GuiTomeImage(new ResourceLocation(Hemomancy.MOD_ID, "textures/gui/pageimages/blue.png"), 5, 5),
				new GuiTomeImage(new ResourceLocation(Hemomancy.MOD_ID, "textures/gui/pageimages/red.png"), 6, 6),
				new GuiTomeImage(new ResourceLocation(Hemomancy.MOD_ID, "textures/gui/pageimages/green.png"), 7, 7),
				new GuiTomeImage(new ResourceLocation(Hemomancy.MOD_ID, "textures/gui/pageimages/white.png"), 8, 8),
				new GuiTomeImage(new ResourceLocation(Hemomancy.MOD_ID, "textures/gui/pageimages/black.png"), 9, 9),
				new GuiTomeImage(new ResourceLocation(Hemomancy.MOD_ID, "textures/gui/pageimages/blue.png"), 10, 10),
				new GuiTomeImage(new ResourceLocation(Hemomancy.MOD_ID, "textures/gui/pageimages/red.png"), 11, 11,
						textIn)));
		IntroPageList.add(new GuiGuideImagePage(10, EnumTomeCatagories.INTRO, "ImageTest3", "Stretchy Yo", "",
				new GuiTomeImage(new ResourceLocation(Hemomancy.MOD_ID, "textures/gui/pageimages/horizextend1.png"), 0,
						0, 128, 64),
				new GuiTomeImage(new ResourceLocation(Hemomancy.MOD_ID, "textures/gui/pageimages/scaled1.png"), 1, 3,
						128, 128),
				new GuiTomeImage(new ResourceLocation(Hemomancy.MOD_ID, "textures/gui/pageimages/vertextend1.png"), 6,
						8, 64, 128)));

		// Vascular
		VascularPageList.add(new GuiGuidePage(1, EnumTomeCatagories.VASCULARSYSTEM, "In the Begining",
				"It Started Somewhere", new ItemStack(ItemInit.sanguine_formation.get()), I18n.get(VASCULAR_PAGE_1)));
		VascularPageList.add(new GuiGuidePage(2, EnumTomeCatagories.VASCULARSYSTEM, "Duality of Nature", "Yin and Yang",
				new ItemStack(ItemInit.sanguine_formation.get()), I18n.get(VASCULAR_PAGE_2)));
		VascularPageList.add(new GuiGuidePage(3, EnumTomeCatagories.VASCULARSYSTEM, "Vibes", "Cosmic Vibrations",
				new ItemStack(ItemInit.sanguine_formation.get()), I18n.get(VASCULAR_PAGE_3)));
		VascularPageList.add(new GuiGuidePage(4, EnumTomeCatagories.VASCULARSYSTEM, "A Level of Essence",
				"Only the Essentials", new ItemStack(ItemInit.sanguine_formation.get()), I18n.get(VASCULAR_PAGE_4)));
		VascularPageList.add(new GuiGuidePage(5, EnumTomeCatagories.VASCULARSYSTEM, "Channeling Basics",
				"Use the Force", new ItemStack(ItemInit.sanguine_formation.get()), I18n.get(VASCULAR_PAGE_5)));
		VascularPageList.add(new GuiGuidePage(6, EnumTomeCatagories.VASCULARSYSTEM, "Nullification", "Counter Attack",
				new ItemStack(ItemInit.sanguine_formation.get()), I18n.get(VASCULAR_PAGE_6)));
		VascularPageList.add(new GuiGuidePage(7, EnumTomeCatagories.VASCULARSYSTEM, "Vital Essence",
				"The Power of Vitals", new ItemStack(ItemInit.sanguine_formation.get()), I18n.get(VASCULAR_PAGE_7)));
		VascularPageList.add(new GuiGuideImagePage(8, EnumTomeCatagories.VASCULARSYSTEM, "ImageTest", "Picture Yo", "",
				new GuiTomeImage(new ResourceLocation(Hemomancy.MOD_ID, "textures/gui/pageimages/blue.png"), 0, 0)));

		// Tendency
		TendencyPageList.add(new GuiGuidePage(1, EnumTomeCatagories.TENDENCY, "In the Begining", "It Started Somewhere",
				new ItemStack(ItemInit.sanguine_formation.get()), I18n.get(TENDENCY_PAGE_1)));
		TendencyPageList.add(new GuiGuidePage(2, EnumTomeCatagories.TENDENCY, "Duality of Nature", "Yin and Yang",
				new ItemStack(ItemInit.sanguine_formation.get()), I18n.get(TENDENCY_PAGE_2)));
		TendencyPageList.add(new GuiGuidePage(3, EnumTomeCatagories.TENDENCY, "Vibes", "Cosmic Vibrations",
				new ItemStack(ItemInit.sanguine_formation.get()), I18n.get(TENDENCY_PAGE_3)));
		TendencyPageList.add(new GuiGuidePage(4, EnumTomeCatagories.TENDENCY, "A Level of Essence",
				"Only the Essentials", new ItemStack(ItemInit.sanguine_formation.get()), I18n.get(TENDENCY_PAGE_4)));
		TendencyPageList.add(new GuiGuidePage(5, EnumTomeCatagories.TENDENCY, "Channeling Basics", "Use the Force",
				new ItemStack(ItemInit.sanguine_formation.get()), I18n.get(TENDENCY_PAGE_5)));
		TendencyPageList.add(new GuiGuidePage(6, EnumTomeCatagories.TENDENCY, "Nullification", "Counter Attack",
				new ItemStack(ItemInit.sanguine_formation.get()), I18n.get(TENDENCY_PAGE_6)));
		TendencyPageList.add(new GuiGuidePage(7, EnumTomeCatagories.TENDENCY, "Vital Essence", "The Power of Vitals",
				new ItemStack(ItemInit.sanguine_formation.get()), I18n.get(TENDENCY_PAGE_7)));
		TendencyPageList.add(new GuiGuideImagePage(8, EnumTomeCatagories.TENDENCY, "ImageTest", "Picture Yo", "",
				new GuiTomeImage(new ResourceLocation(Hemomancy.MOD_ID, "textures/gui/pageimages/blue.png"), 0, 0)));

		// Manipulation
		ManipulationPageList
				.add(new GuiGuidePage(1, EnumTomeCatagories.MANIPULATION, "In the Begining", "It Started Somewhere",
						new ItemStack(ItemInit.sanguine_formation.get()), I18n.get(MANIPULATION_PAGE_1)));
		ManipulationPageList.add(new GuiGuidePage(2, EnumTomeCatagories.MANIPULATION, "Duality of Nature",
				"Yin and Yang", new ItemStack(ItemInit.sanguine_formation.get()), I18n.get(MANIPULATION_PAGE_2)));
		ManipulationPageList.add(new GuiGuidePage(3, EnumTomeCatagories.MANIPULATION, "Vibes", "Cosmic Vibrations",
				new ItemStack(ItemInit.sanguine_formation.get()), I18n.get(MANIPULATION_PAGE_3)));
		ManipulationPageList
				.add(new GuiGuidePage(4, EnumTomeCatagories.MANIPULATION, "A Level of Essence", "Only the Essentials",
						new ItemStack(ItemInit.sanguine_formation.get()), I18n.get(MANIPULATION_PAGE_4)));
		ManipulationPageList.add(new GuiGuidePage(5, EnumTomeCatagories.MANIPULATION, "Channeling Basics",
				"Use the Force", new ItemStack(ItemInit.sanguine_formation.get()), I18n.get(MANIPULATION_PAGE_5)));
		ManipulationPageList.add(new GuiGuidePage(6, EnumTomeCatagories.MANIPULATION, "Nullification", "Counter Attack",
				new ItemStack(ItemInit.sanguine_formation.get()), I18n.get(MANIPULATION_PAGE_6)));
		ManipulationPageList
				.add(new GuiGuidePage(7, EnumTomeCatagories.MANIPULATION, "Vital Essence", "The Power of Vitals",
						new ItemStack(ItemInit.sanguine_formation.get()), I18n.get(MANIPULATION_PAGE_7)));
		ManipulationPageList.add(new GuiGuideImagePage(8, EnumTomeCatagories.MANIPULATION, "ImageTest", "Picture Yo",
				"",
				new GuiTomeImage(new ResourceLocation(Hemomancy.MOD_ID, "textures/gui/pageimages/blue.png"), 0, 0)));

		// Hiddenn

		// Adding Chapters
		Collections.addAll(ChapterList, IntroPageList, VascularPageList, TendencyPageList, ManipulationPageList);

		// TOC PAGES
		TOCPageList
				.add(new GuiGuidePageTOC(EnumTomeCatagories.INTRO, new ItemStack(ItemInit.sanguine_formation.get())));
		TOCPageList.add(new GuiGuidePageTOC(EnumTomeCatagories.VASCULARSYSTEM,
				new ItemStack(ItemInit.sanguine_formation.get())));
		TOCPageList.add(new GuiGuidePageTOC(EnumTomeCatagories.TENDENCY, new ItemStack(BlockInit.venous_stone.get())));
		TOCPageList.add(
				new GuiGuidePageTOC(EnumTomeCatagories.MANIPULATION, new ItemStack(ItemInit.sanguine_formation.get())));

		// Adding the table of contents to each chapter
		IntroPageList.add(0, TOCPageList.get(0));
		VascularPageList.add(0, TOCPageList.get(1));
		TendencyPageList.add(0, TOCPageList.get(2));
		ManipulationPageList.add(0, TOCPageList.get(3));

	}

	public static List<GuiGuidePage> getIntroPageList() {
		return IntroPageList;
	}

	public static List<GuiGuidePage> getVascularPageList() {
		return VascularPageList;
	}

	public static List<GuiGuidePage> getManipulationPageList() {
		return ManipulationPageList;
	}

	public static List<GuiGuidePage> getTendencyPageList() {
		return TendencyPageList;
	}

	public static List<List<GuiGuidePage>> getChapterList() {
		return ChapterList;
	}

}
