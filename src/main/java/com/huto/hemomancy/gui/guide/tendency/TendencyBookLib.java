package com.huto.hemomancy.gui.guide.tendency;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.huto.hemomancy.init.BlockInit;
import com.huto.hemomancy.init.ItemInit;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TendencyBookLib {

	public static List<GuiTendencyPage> AnimusPageList = new ArrayList<GuiTendencyPage>();
	public static List<GuiTendencyPage> MortemPageList = new ArrayList<GuiTendencyPage>();
	public static List<GuiTendencyPage> DuctilisPageList = new ArrayList<GuiTendencyPage>();
	public static List<GuiTendencyPage> FerricPageList = new ArrayList<GuiTendencyPage>();
	public static List<GuiTendencyPage> LuxPageList = new ArrayList<GuiTendencyPage>();
	public static List<GuiTendencyPage> TenebrisPageList = new ArrayList<GuiTendencyPage>();
	public static List<GuiTendencyPage> FlammeusPageList = new ArrayList<GuiTendencyPage>();
	public static List<GuiTendencyPage> CongeatioPageList = new ArrayList<GuiTendencyPage>();
	public static List<GuiTendencyPage> HiddenPageList = new ArrayList<GuiTendencyPage>();

	public static List<GuiTendencyPageTOC> TOCPageList = new ArrayList<GuiTendencyPageTOC>();
	public static List<List<GuiTendencyPage>> ChapterList = new ArrayList<List<GuiTendencyPage>>();

	// Text Locations
	public static String ANIMUS_PAGE_1 = "The big bang created the universe, making existence out of nothing, pushing everything out from a central point with incredible momentum. With this massive amount of force, came an equal production of both antimatter and standard matter. From the start, everything had it's opposite. This fact can be claimed to be one of the few principles in existence that always holds true, be it science or magic. The forging of the universe is not a relic of ages past, literal reverberations from the explosion can still be felt and interacted with.";
	public static String ANIMUS_PAGE_2 = "The reverberations previously mentioned are in a sense, cosmic whiplash on a universal scale. As the singularity expanded, every particle was thrown around in every which way. While at first, this was cause for chaos, the wheels of time have marched on and calmed things down as they do rendering the vibrations very minute, but minute is not zero. Minute for one atom can be magnified exponentially when concentrated enough, resonating with one another allowing for the force of the big bang to be felt once more";
	public static String ANIMUS_PAGE_3 = "One of the quirks of the antimatter/matter duality is that the vibrations of these particles are opposite and equal as well. These vibrations follow the same principle that all other forms of vibration be it sound/light/electricity etc, in that they amplify when met with similar waves and cancel when met with opposing waves. Building on the concentration effect of these vibrations, the idea of karma can even be traced back as the balance of these vibrations within your body, and mana is just the net force of the anti and positive waves manifesting as a physical and invisible force.";
	public static String ANIMUS_PAGE_4 = "Matter and antimatter can become \"vibratory\" in some rare examples simply by happenstance when enough of a single frequency of resonating matter comes into close proximity and will somewhat meld together. When they coalesce they are called \"drops\" or \"tears\" for vibrations and antivibration respectively with \"essence\" being the overarching term for both forms of concentrated vibrational matter. Essence can be found in all forms of life with a very low concentration but can be coerced out of them upon death with specialized tools discussed later.";
	public static String ANIMUS_PAGE_5 = "The process of inducing vibrations is known as \"Channeling\", somnolental sounding in nature, but the hiddens can be blamed for this. This can be done in a variety of ways all modeled off of trying to artificially induce the natural essence production, by essentially taking a chunk of vibratory or \"conductive\" matter and allowing it to collect more and more matter that begins to resonate with it, which is an exponential process. The beauty of this process is that once an original drop or tear of matter is created, it will naturally start to produce more of itself with little aide.If only there was a way to contain this growth...";
	public static String ANIMUS_PAGE_6 = "As mentioned prior, these vibrations behave the same way that any other wave does, and as such can be stopped or dampened with an opposite waveform. Producing this waveform can be tricky but with a little bit of experimenting, discovering that the more your around concentrated matter, the more sensitive you become to it, beginning to be able to sense slight differences in direction and amplitude each drop. By using matter of specific frequency you can change the characteristics of other matter by forcing contact with the target. The process of canceling vibrations intentionally is called \"Nullification\" and is incredibly useful.";
	public static String ANIMUS_PAGE_7 = "Due to the nature of life itself, being inherently organized, living beings are naturally enriched with essence, almost completely filled with positive vibrations in fact. This is not to say that ALL creatures are positive, there are some exceptions that have net negative vibrations(withers and end creatures), and some that have none at all(skeletons and other inorganic life)! Because of this, the essence can be farmed quite readily through the process of death, with the correct tools, of course, ones that nullified vibrations maybe?";
	public static String ANIMUS_PAGE_8 = "title.somnolenttome.intro.page.8.text";

	public static String MORTEM_PAGE_1 = "The big bang created the universe, making existence out of nothing, pushing everything out from a central point with incredible momentum. With this massive amount of force, came an equal production of both antimatter and standard matter. From the start, everything had it's opposite. This fact can be claimed to be one of the few principles in existence that always holds true, be it science or magic. The forging of the universe is not a relic of ages past, literal reverberations from the explosion can still be felt and interacted with.";
	public static String MORTEM_PAGE_2 = "The reverberations previously mentioned are in a sense, cosmic whiplash on a universal scale. As the singularity expanded, every particle was thrown around in every which way. While at first, this was cause for chaos, the wheels of time have marched on and calmed things down as they do rendering the vibrations very minute, but minute is not zero. Minute for one atom can be magnified exponentially when concentrated enough, resonating with one another allowing for the force of the big bang to be felt once more";
	public static String MORTEM_PAGE_3 = "One of the quirks of the antimatter/matter duality is that the vibrations of these particles are opposite and equal as well. These vibrations follow the same principle that all other forms of vibration be it sound/light/electricity etc, in that they amplify when met with similar waves and cancel when met with opposing waves. Building on the concentration effect of these vibrations, the idea of karma can even be traced back as the balance of these vibrations within your body, and mana is just the net force of the anti and positive waves manifesting as a physical and invisible force.";
	public static String MORTEM_PAGE_4 = "Matter and antimatter can become \"vibratory\" in some rare examples simply by happenstance when enough of a single frequency of resonating matter comes into close proximity and will somewhat meld together. When they coalesce they are called \"drops\" or \"tears\" for vibrations and antivibration respectively with \"essence\" being the overarching term for both forms of concentrated vibrational matter. Essence can be found in all forms of life with a very low concentration but can be coerced out of them upon death with specialized tools discussed later.";
	public static String MORTEM_PAGE_5 = "The process of inducing vibrations is known as \"Channeling\", somnolental sounding in nature, but the hiddens can be blamed for this. This can be done in a variety of ways all modeled off of trying to artificially induce the natural essence production, by essentially taking a chunk of vibratory or \"conductive\" matter and allowing it to collect more and more matter that begins to resonate with it, which is an exponential process. The beauty of this process is that once an original drop or tear of matter is created, it will naturally start to produce more of itself with little aide.If only there was a way to contain this growth...";
	public static String MORTEM_PAGE_6 = "As mentioned prior, these vibrations behave the same way that any other wave does, and as such can be stopped or dampened with an opposite waveform. Producing this waveform can be tricky but with a little bit of experimenting, discovering that the more your around concentrated matter, the more sensitive you become to it, beginning to be able to sense slight differences in direction and amplitude each drop. By using matter of specific frequency you can change the characteristics of other matter by forcing contact with the target. The process of canceling vibrations intentionally is called \"Nullification\" and is incredibly useful.";
	public static String MORTEM_PAGE_7 = "Due to the nature of life itself, being inherently organized, living beings are naturally enriched with essence, almost completely filled with positive vibrations in fact. This is not to say that ALL creatures are positive, there are some exceptions that have net negative vibrations(withers and end creatures), and some that have none at all(skeletons and other inorganic life)! Because of this, the essence can be farmed quite readily through the process of death, with the correct tools, of course, ones that nullified vibrations maybe?";
	public static String MORTEM_PAGE_8 = "title.somnolenttome.intro.page.8.text";

	public static String DUCTILIS_PAGE_1 = "The big bang created the universe, making existence out of nothing, pushing everything out from a central point with incredible momentum. With this massive amount of force, came an equal production of both antimatter and standard matter. From the start, everything had it's opposite. This fact can be claimed to be one of the few principles in existence that always holds true, be it science or magic. The forging of the universe is not a relic of ages past, literal reverberations from the explosion can still be felt and interacted with.";
	public static String DUCTILIS_PAGE_2 = "The reverberations previously mentioned are in a sense, cosmic whiplash on a universal scale. As the singularity expanded, every particle was thrown around in every which way. While at first, this was cause for chaos, the wheels of time have marched on and calmed things down as they do rendering the vibrations very minute, but minute is not zero. Minute for one atom can be magnified exponentially when concentrated enough, resonating with one another allowing for the force of the big bang to be felt once more";
	public static String DUCTILIS_PAGE_3 = "One of the quirks of the antimatter/matter duality is that the vibrations of these particles are opposite and equal as well. These vibrations follow the same principle that all other forms of vibration be it sound/light/electricity etc, in that they amplify when met with similar waves and cancel when met with opposing waves. Building on the concentration effect of these vibrations, the idea of karma can even be traced back as the balance of these vibrations within your body, and mana is just the net force of the anti and positive waves manifesting as a physical and invisible force.";
	public static String DUCTILIS_PAGE_4 = "Matter and antimatter can become \"vibratory\" in some rare examples simply by happenstance when enough of a single frequency of resonating matter comes into close proximity and will somewhat meld together. When they coalesce they are called \"drops\" or \"tears\" for vibrations and antivibration respectively with \"essence\" being the overarching term for both forms of concentrated vibrational matter. Essence can be found in all forms of life with a very low concentration but can be coerced out of them upon death with specialized tools discussed later.";
	public static String DUCTILIS_PAGE_5 = "The process of inducing vibrations is known as \"Channeling\", somnolental sounding in nature, but the hiddens can be blamed for this. This can be done in a variety of ways all modeled off of trying to artificially induce the natural essence production, by essentially taking a chunk of vibratory or \"conductive\" matter and allowing it to collect more and more matter that begins to resonate with it, which is an exponential process. The beauty of this process is that once an original drop or tear of matter is created, it will naturally start to produce more of itself with little aide.If only there was a way to contain this growth...";
	public static String DUCTILIS_PAGE_6 = "As mentioned prior, these vibrations behave the same way that any other wave does, and as such can be stopped or dampened with an opposite waveform. Producing this waveform can be tricky but with a little bit of experimenting, discovering that the more your around concentrated matter, the more sensitive you become to it, beginning to be able to sense slight differences in direction and amplitude each drop. By using matter of specific frequency you can change the characteristics of other matter by forcing contact with the target. The process of canceling vibrations intentionally is called \"Nullification\" and is incredibly useful.";
	public static String DUCTILIS_PAGE_7 = "Due to the nature of life itself, being inherently organized, living beings are naturally enriched with essence, almost completely filled with positive vibrations in fact. This is not to say that ALL creatures are positive, there are some exceptions that have net negative vibrations(withers and end creatures), and some that have none at all(skeletons and other inorganic life)! Because of this, the essence can be farmed quite readily through the process of death, with the correct tools, of course, ones that nullified vibrations maybe?";
	public static String DUCTILIS_PAGE_8 = "title.somnolenttome.intro.page.8.text";

	public static String FERRIC_PAGE_1 = "The big bang created the universe, making existence out of nothing, pushing everything out from a central point with incredible momentum. With this massive amount of force, came an equal production of both antimatter and standard matter. From the start, everything had it's opposite. This fact can be claimed to be one of the few principles in existence that always holds true, be it science or magic. The forging of the universe is not a relic of ages past, literal reverberations from the explosion can still be felt and interacted with.";
	public static String FERRIC_PAGE_2 = "The reverberations previously mentioned are in a sense, cosmic whiplash on a universal scale. As the singularity expanded, every particle was thrown around in every which way. While at first, this was cause for chaos, the wheels of time have marched on and calmed things down as they do rendering the vibrations very minute, but minute is not zero. Minute for one atom can be magnified exponentially when concentrated enough, resonating with one another allowing for the force of the big bang to be felt once more";
	public static String FERRIC_PAGE_3 = "One of the quirks of the antimatter/matter duality is that the vibrations of these particles are opposite and equal as well. These vibrations follow the same principle that all other forms of vibration be it sound/light/electricity etc, in that they amplify when met with similar waves and cancel when met with opposing waves. Building on the concentration effect of these vibrations, the idea of karma can even be traced back as the balance of these vibrations within your body, and mana is just the net force of the anti and positive waves manifesting as a physical and invisible force.";
	public static String FERRIC_PAGE_4 = "Matter and antimatter can become \"vibratory\" in some rare examples simply by happenstance when enough of a single frequency of resonating matter comes into close proximity and will somewhat meld together. When they coalesce they are called \"drops\" or \"tears\" for vibrations and antivibration respectively with \"essence\" being the overarching term for both forms of concentrated vibrational matter. Essence can be found in all forms of life with a very low concentration but can be coerced out of them upon death with specialized tools discussed later.";
	public static String FERRIC_PAGE_5 = "The process of inducing vibrations is known as \"Channeling\", somnolental sounding in nature, but the hiddens can be blamed for this. This can be done in a variety of ways all modeled off of trying to artificially induce the natural essence production, by essentially taking a chunk of vibratory or \"conductive\" matter and allowing it to collect more and more matter that begins to resonate with it, which is an exponential process. The beauty of this process is that once an original drop or tear of matter is created, it will naturally start to produce more of itself with little aide.If only there was a way to contain this growth...";
	public static String FERRIC_PAGE_6 = "As mentioned prior, these vibrations behave the same way that any other wave does, and as such can be stopped or dampened with an opposite waveform. Producing this waveform can be tricky but with a little bit of experimenting, discovering that the more your around concentrated matter, the more sensitive you become to it, beginning to be able to sense slight differences in direction and amplitude each drop. By using matter of specific frequency you can change the characteristics of other matter by forcing contact with the target. The process of canceling vibrations intentionally is called \"Nullification\" and is incredibly useful.";
	public static String FERRIC_PAGE_7 = "Due to the nature of life itself, being inherently organized, living beings are naturally enriched with essence, almost completely filled with positive vibrations in fact. This is not to say that ALL creatures are positive, there are some exceptions that have net negative vibrations(withers and end creatures), and some that have none at all(skeletons and other inorganic life)! Because of this, the essence can be farmed quite readily through the process of death, with the correct tools, of course, ones that nullified vibrations maybe?";
	public static String FERRIC_PAGE_8 = "title.somnolenttome.intro.page.8.text";

	public static String LUX_PAGE_1 = "The big bang created the universe, making existence out of nothing, pushing everything out from a central point with incredible momentum. With this massive amount of force, came an equal production of both antimatter and standard matter. From the start, everything had it's opposite. This fact can be claimed to be one of the few principles in existence that always holds true, be it science or magic. The forging of the universe is not a relic of ages past, literal reverberations from the explosion can still be felt and interacted with.";
	public static String LUX_PAGE_2 = "The reverberations previously mentioned are in a sense, cosmic whiplash on a universal scale. As the singularity expanded, every particle was thrown around in every which way. While at first, this was cause for chaos, the wheels of time have marched on and calmed things down as they do rendering the vibrations very minute, but minute is not zero. Minute for one atom can be magnified exponentially when concentrated enough, resonating with one another allowing for the force of the big bang to be felt once more";
	public static String LUX_PAGE_3 = "One of the quirks of the antimatter/matter duality is that the vibrations of these particles are opposite and equal as well. These vibrations follow the same principle that all other forms of vibration be it sound/light/electricity etc, in that they amplify when met with similar waves and cancel when met with opposing waves. Building on the concentration effect of these vibrations, the idea of karma can even be traced back as the balance of these vibrations within your body, and mana is just the net force of the anti and positive waves manifesting as a physical and invisible force.";
	public static String LUX_PAGE_4 = "Matter and antimatter can become \"vibratory\" in some rare examples simply by happenstance when enough of a single frequency of resonating matter comes into close proximity and will somewhat meld together. When they coalesce they are called \"drops\" or \"tears\" for vibrations and antivibration respectively with \"essence\" being the overarching term for both forms of concentrated vibrational matter. Essence can be found in all forms of life with a very low concentration but can be coerced out of them upon death with specialized tools discussed later.";
	public static String LUX_PAGE_5 = "The process of inducing vibrations is known as \"Channeling\", somnolental sounding in nature, but the hiddens can be blamed for this. This can be done in a variety of ways all modeled off of trying to artificially induce the natural essence production, by essentially taking a chunk of vibratory or \"conductive\" matter and allowing it to collect more and more matter that begins to resonate with it, which is an exponential process. The beauty of this process is that once an original drop or tear of matter is created, it will naturally start to produce more of itself with little aide.If only there was a way to contain this growth...";
	public static String LUX_PAGE_6 = "As mentioned prior, these vibrations behave the same way that any other wave does, and as such can be stopped or dampened with an opposite waveform. Producing this waveform can be tricky but with a little bit of experimenting, discovering that the more your around concentrated matter, the more sensitive you become to it, beginning to be able to sense slight differences in direction and amplitude each drop. By using matter of specific frequency you can change the characteristics of other matter by forcing contact with the target. The process of canceling vibrations intentionally is called \"Nullification\" and is incredibly useful.";
	public static String LUX_PAGE_7 = "Due to the nature of life itself, being inherently organized, living beings are naturally enriched with essence, almost completely filled with positive vibrations in fact. This is not to say that ALL creatures are positive, there are some exceptions that have net negative vibrations(withers and end creatures), and some that have none at all(skeletons and other inorganic life)! Because of this, the essence can be farmed quite readily through the process of death, with the correct tools, of course, ones that nullified vibrations maybe?";
	public static String LUX_PAGE_8 = "title.somnolenttome.intro.page.8.text";

	public static String TENEBRIS_PAGE_1 = "The big bang created the universe, making existence out of nothing, pushing everything out from a central point with incredible momentum. With this massive amount of force, came an equal production of both antimatter and standard matter. From the start, everything had it's opposite. This fact can be claimed to be one of the few principles in existence that always holds true, be it science or magic. The forging of the universe is not a relic of ages past, literal reverberations from the explosion can still be felt and interacted with.";
	public static String TENEBRIS_PAGE_2 = "The reverberations previously mentioned are in a sense, cosmic whiplash on a universal scale. As the singularity expanded, every particle was thrown around in every which way. While at first, this was cause for chaos, the wheels of time have marched on and calmed things down as they do rendering the vibrations very minute, but minute is not zero. Minute for one atom can be magnified exponentially when concentrated enough, resonating with one another allowing for the force of the big bang to be felt once more";
	public static String TENEBRIS_PAGE_3 = "One of the quirks of the antimatter/matter duality is that the vibrations of these particles are opposite and equal as well. These vibrations follow the same principle that all other forms of vibration be it sound/light/electricity etc, in that they amplify when met with similar waves and cancel when met with opposing waves. Building on the concentration effect of these vibrations, the idea of karma can even be traced back as the balance of these vibrations within your body, and mana is just the net force of the anti and positive waves manifesting as a physical and invisible force.";
	public static String TENEBRIS_PAGE_4 = "Matter and antimatter can become \"vibratory\" in some rare examples simply by happenstance when enough of a single frequency of resonating matter comes into close proximity and will somewhat meld together. When they coalesce they are called \"drops\" or \"tears\" for vibrations and antivibration respectively with \"essence\" being the overarching term for both forms of concentrated vibrational matter. Essence can be found in all forms of life with a very low concentration but can be coerced out of them upon death with specialized tools discussed later.";
	public static String TENEBRIS_PAGE_5 = "The process of inducing vibrations is known as \"Channeling\", somnolental sounding in nature, but the hiddens can be blamed for this. This can be done in a variety of ways all modeled off of trying to artificially induce the natural essence production, by essentially taking a chunk of vibratory or \"conductive\" matter and allowing it to collect more and more matter that begins to resonate with it, which is an exponential process. The beauty of this process is that once an original drop or tear of matter is created, it will naturally start to produce more of itself with little aide.If only there was a way to contain this growth...";
	public static String TENEBRIS_PAGE_6 = "As mentioned prior, these vibrations behave the same way that any other wave does, and as such can be stopped or dampened with an opposite waveform. Producing this waveform can be tricky but with a little bit of experimenting, discovering that the more your around concentrated matter, the more sensitive you become to it, beginning to be able to sense slight differences in direction and amplitude each drop. By using matter of specific frequency you can change the characteristics of other matter by forcing contact with the target. The process of canceling vibrations intentionally is called \"Nullification\" and is incredibly useful.";
	public static String TENEBRIS_PAGE_7 = "Due to the nature of life itself, being inherently organized, living beings are naturally enriched with essence, almost completely filled with positive vibrations in fact. This is not to say that ALL creatures are positive, there are some exceptions that have net negative vibrations(withers and end creatures), and some that have none at all(skeletons and other inorganic life)! Because of this, the essence can be farmed quite readily through the process of death, with the correct tools, of course, ones that nullified vibrations maybe?";
	public static String TENEBRIS_PAGE_8 = "title.somnolenttome.intro.page.8.text";

	public static String FLAMMEUS_PAGE_1 = "The big bang created the universe, making existence out of nothing, pushing everything out from a central point with incredible momentum. With this massive amount of force, came an equal production of both antimatter and standard matter. From the start, everything had it's opposite. This fact can be claimed to be one of the few principles in existence that always holds true, be it science or magic. The forging of the universe is not a relic of ages past, literal reverberations from the explosion can still be felt and interacted with.";
	public static String FLAMMEUS_PAGE_2 = "The reverberations previously mentioned are in a sense, cosmic whiplash on a universal scale. As the singularity expanded, every particle was thrown around in every which way. While at first, this was cause for chaos, the wheels of time have marched on and calmed things down as they do rendering the vibrations very minute, but minute is not zero. Minute for one atom can be magnified exponentially when concentrated enough, resonating with one another allowing for the force of the big bang to be felt once more";
	public static String FLAMMEUS_PAGE_3 = "One of the quirks of the antimatter/matter duality is that the vibrations of these particles are opposite and equal as well. These vibrations follow the same principle that all other forms of vibration be it sound/light/electricity etc, in that they amplify when met with similar waves and cancel when met with opposing waves. Building on the concentration effect of these vibrations, the idea of karma can even be traced back as the balance of these vibrations within your body, and mana is just the net force of the anti and positive waves manifesting as a physical and invisible force.";
	public static String FLAMMEUS_PAGE_4 = "Matter and antimatter can become \"vibratory\" in some rare examples simply by happenstance when enough of a single frequency of resonating matter comes into close proximity and will somewhat meld together. When they coalesce they are called \"drops\" or \"tears\" for vibrations and antivibration respectively with \"essence\" being the overarching term for both forms of concentrated vibrational matter. Essence can be found in all forms of life with a very low concentration but can be coerced out of them upon death with specialized tools discussed later.";
	public static String FLAMMEUS_PAGE_5 = "The process of inducing vibrations is known as \"Channeling\", somnolental sounding in nature, but the hiddens can be blamed for this. This can be done in a variety of ways all modeled off of trying to artificially induce the natural essence production, by essentially taking a chunk of vibratory or \"conductive\" matter and allowing it to collect more and more matter that begins to resonate with it, which is an exponential process. The beauty of this process is that once an original drop or tear of matter is created, it will naturally start to produce more of itself with little aide.If only there was a way to contain this growth...";
	public static String FLAMMEUS_PAGE_6 = "As mentioned prior, these vibrations behave the same way that any other wave does, and as such can be stopped or dampened with an opposite waveform. Producing this waveform can be tricky but with a little bit of experimenting, discovering that the more your around concentrated matter, the more sensitive you become to it, beginning to be able to sense slight differences in direction and amplitude each drop. By using matter of specific frequency you can change the characteristics of other matter by forcing contact with the target. The process of canceling vibrations intentionally is called \"Nullification\" and is incredibly useful.";
	public static String FLAMMEUS_PAGE_7 = "Due to the nature of life itself, being inherently organized, living beings are naturally enriched with essence, almost completely filled with positive vibrations in fact. This is not to say that ALL creatures are positive, there are some exceptions that have net negative vibrations(withers and end creatures), and some that have none at all(skeletons and other inorganic life)! Because of this, the essence can be farmed quite readily through the process of death, with the correct tools, of course, ones that nullified vibrations maybe?";
	public static String FLAMMEUS_PAGE_8 = "title.somnolenttome.intro.page.8.text";

	public static String CONGEATIO_PAGE_1 = "The big bang created the universe, making existence out of nothing, pushing everything out from a central point with incredible momentum. With this massive amount of force, came an equal production of both antimatter and standard matter. From the start, everything had it's opposite. This fact can be claimed to be one of the few principles in existence that always holds true, be it science or magic. The forging of the universe is not a relic of ages past, literal reverberations from the explosion can still be felt and interacted with.";
	public static String CONGEATIO_PAGE_2 = "The reverberations previously mentioned are in a sense, cosmic whiplash on a universal scale. As the singularity expanded, every particle was thrown around in every which way. While at first, this was cause for chaos, the wheels of time have marched on and calmed things down as they do rendering the vibrations very minute, but minute is not zero. Minute for one atom can be magnified exponentially when concentrated enough, resonating with one another allowing for the force of the big bang to be felt once more";
	public static String CONGEATIO_PAGE_3 = "One of the quirks of the antimatter/matter duality is that the vibrations of these particles are opposite and equal as well. These vibrations follow the same principle that all other forms of vibration be it sound/light/electricity etc, in that they amplify when met with similar waves and cancel when met with opposing waves. Building on the concentration effect of these vibrations, the idea of karma can even be traced back as the balance of these vibrations within your body, and mana is just the net force of the anti and positive waves manifesting as a physical and invisible force.";
	public static String CONGEATIO_PAGE_4 = "Matter and antimatter can become \"vibratory\" in some rare examples simply by happenstance when enough of a single frequency of resonating matter comes into close proximity and will somewhat meld together. When they coalesce they are called \"drops\" or \"tears\" for vibrations and antivibration respectively with \"essence\" being the overarching term for both forms of concentrated vibrational matter. Essence can be found in all forms of life with a very low concentration but can be coerced out of them upon death with specialized tools discussed later.";
	public static String CONGEATIO_PAGE_5 = "The process of inducing vibrations is known as \"Channeling\", somnolental sounding in nature, but the hiddens can be blamed for this. This can be done in a variety of ways all modeled off of trying to artificially induce the natural essence production, by essentially taking a chunk of vibratory or \"conductive\" matter and allowing it to collect more and more matter that begins to resonate with it, which is an exponential process. The beauty of this process is that once an original drop or tear of matter is created, it will naturally start to produce more of itself with little aide.If only there was a way to contain this growth...";
	public static String CONGEATIO_PAGE_6 = "As mentioned prior, these vibrations behave the same way that any other wave does, and as such can be stopped or dampened with an opposite waveform. Producing this waveform can be tricky but with a little bit of experimenting, discovering that the more your around concentrated matter, the more sensitive you become to it, beginning to be able to sense slight differences in direction and amplitude each drop. By using matter of specific frequency you can change the characteristics of other matter by forcing contact with the target. The process of canceling vibrations intentionally is called \"Nullification\" and is incredibly useful.";
	public static String CONGEATIO_PAGE_7 = "Due to the nature of life itself, being inherently organized, living beings are naturally enriched with essence, almost completely filled with positive vibrations in fact. This is not to say that ALL creatures are positive, there are some exceptions that have net negative vibrations(withers and end creatures), and some that have none at all(skeletons and other inorganic life)! Because of this, the essence can be farmed quite readily through the process of death, with the correct tools, of course, ones that nullified vibrations maybe?";
	public static String CONGEATIO_PAGE_8 = "title.somnolenttome.intro.page.8.text";

	public static String HIDDEN_PAGE_1 = "The big bang created the universe, making existence out of nothing, pushing everything out from a central point with incredible momentum. With this massive amount of force, came an equal production of both antimatter and standard matter. From the start, everything had it's opposite. This fact can be claimed to be one of the few principles in existence that always holds true, be it science or magic. The forging of the universe is not a relic of ages past, literal reverberations from the explosion can still be felt and interacted with.";
	public static String HIDDEN_PAGE_2 = "The reverberations previously mentioned are in a sense, cosmic whiplash on a universal scale. As the singularity expanded, every particle was thrown around in every which way. While at first, this was cause for chaos, the wheels of time have marched on and calmed things down as they do rendering the vibrations very minute, but minute is not zero. Minute for one atom can be magnified exponentially when concentrated enough, resonating with one another allowing for the force of the big bang to be felt once more";
	public static String HIDDEN_PAGE_3 = "One of the quirks of the antimatter/matter duality is that the vibrations of these particles are opposite and equal as well. These vibrations follow the same principle that all other forms of vibration be it sound/light/electricity etc, in that they amplify when met with similar waves and cancel when met with opposing waves. Building on the concentration effect of these vibrations, the idea of karma can even be traced back as the balance of these vibrations within your body, and mana is just the net force of the anti and positive waves manifesting as a physical and invisible force.";
	public static String HIDDEN_PAGE_4 = "Matter and antimatter can become \"vibratory\" in some rare examples simply by happenstance when enough of a single frequency of resonating matter comes into close proximity and will somewhat meld together. When they coalesce they are called \"drops\" or \"tears\" for vibrations and antivibration respectively with \"essence\" being the overarching term for both forms of concentrated vibrational matter. Essence can be found in all forms of life with a very low concentration but can be coerced out of them upon death with specialized tools discussed later.";
	public static String HIDDEN_PAGE_5 = "The process of inducing vibrations is known as \"Channeling\", somnolental sounding in nature, but the hiddens can be blamed for this. This can be done in a variety of ways all modeled off of trying to artificially induce the natural essence production, by essentially taking a chunk of vibratory or \"conductive\" matter and allowing it to collect more and more matter that begins to resonate with it, which is an exponential process. The beauty of this process is that once an original drop or tear of matter is created, it will naturally start to produce more of itself with little aide.If only there was a way to contain this growth...";
	public static String HIDDEN_PAGE_6 = "As mentioned prior, these vibrations behave the same way that any other wave does, and as such can be stopped or dampened with an opposite waveform. Producing this waveform can be tricky but with a little bit of experimenting, discovering that the more your around concentrated matter, the more sensitive you become to it, beginning to be able to sense slight differences in direction and amplitude each drop. By using matter of specific frequency you can change the characteristics of other matter by forcing contact with the target. The process of canceling vibrations intentionally is called \"Nullification\" and is incredibly useful.";
	public static String HIDDEN_PAGE_7 = "Due to the nature of life itself, being inherently organized, living beings are naturally enriched with essence, almost completely filled with positive vibrations in fact. This is not to say that ALL creatures are positive, there are some exceptions that have net negative vibrations(withers and end creatures), and some that have none at all(skeletons and other inorganic life)! Because of this, the essence can be farmed quite readily through the process of death, with the correct tools, of course, ones that nullified vibrations maybe?";
	public static String HIDDEN_PAGE_8 = "title.somnolenttome.intro.page.8.text";

	public static void registerPages() {

		AnimusPageList.clear();
		MortemPageList.clear();
		DuctilisPageList.clear();
		FerricPageList.clear();
		LuxPageList.clear();
		TenebrisPageList.clear();
		FlammeusPageList.clear();
		CongeatioPageList.clear();
		HiddenPageList.clear();
		ChapterList.clear();
		TOCPageList.clear();

		// Animus
		AnimusPageList.add(new GuiTendencyPage(1, EnumTendencyCatagories.ANIMUS, "In the Begining",
				"It Started Somewhere", new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(ANIMUS_PAGE_1)));
		AnimusPageList.add(new GuiTendencyPage(2, EnumTendencyCatagories.ANIMUS, "Duality of Nature", "Yin and Yang",
				new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(ANIMUS_PAGE_2)));
		AnimusPageList.add(new GuiTendencyPage(3, EnumTendencyCatagories.ANIMUS, "Vibes", "Cosmic Vibrations",
				new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(ANIMUS_PAGE_3)));
		AnimusPageList.add(new GuiTendencyPage(4, EnumTendencyCatagories.ANIMUS, "A World of Essence",
				"Only the Essentials", new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(ANIMUS_PAGE_4)));
		AnimusPageList.add(new GuiTendencyPage(5, EnumTendencyCatagories.ANIMUS, "Channeling Basics", "Use the Force",
				new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(ANIMUS_PAGE_5)));
		AnimusPageList.add(new GuiTendencyPage(6, EnumTendencyCatagories.ANIMUS, "Nullification", "Counter Attack",
				new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(ANIMUS_PAGE_6)));
		AnimusPageList.add(new GuiTendencyPage(7, EnumTendencyCatagories.ANIMUS, "Vital Essence", "The Power of Vitals",
				new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(ANIMUS_PAGE_7)));

		// Mortem
		MortemPageList.add(new GuiTendencyPage(1, EnumTendencyCatagories.MORTEM, "In the Begining",
				"It Started Somewhere", new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(MORTEM_PAGE_1)));
		MortemPageList.add(new GuiTendencyPage(2, EnumTendencyCatagories.MORTEM, "Duality of Nature", "Yin and Yang",
				new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(MORTEM_PAGE_2)));
		MortemPageList.add(new GuiTendencyPage(3, EnumTendencyCatagories.MORTEM, "Vibes", "Cosmic Vibrations",
				new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(MORTEM_PAGE_3)));
		MortemPageList.add(new GuiTendencyPage(4, EnumTendencyCatagories.MORTEM, "A World of Essence",
				"Only the Essentials", new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(MORTEM_PAGE_4)));
		MortemPageList.add(new GuiTendencyPage(5, EnumTendencyCatagories.MORTEM, "Channeling Basics", "Use the Force",
				new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(MORTEM_PAGE_5)));
		MortemPageList.add(new GuiTendencyPage(6, EnumTendencyCatagories.MORTEM, "Nullification", "Counter Attack",
				new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(MORTEM_PAGE_6)));
		MortemPageList.add(new GuiTendencyPage(7, EnumTendencyCatagories.MORTEM, "Vital Essence", "The Power of Vitals",
				new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(MORTEM_PAGE_7)));

		// Ductilis
		DuctilisPageList
				.add(new GuiTendencyPage(1, EnumTendencyCatagories.DUCTILIS, "In the Begining", "It Started Somewhere",
						new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(DUCTILIS_PAGE_1)));
		DuctilisPageList.add(new GuiTendencyPage(2, EnumTendencyCatagories.DUCTILIS, "Duality of Nature",
				"Yin and Yang", new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(DUCTILIS_PAGE_2)));
		DuctilisPageList.add(new GuiTendencyPage(3, EnumTendencyCatagories.DUCTILIS, "Vibes", "Cosmic Vibrations",
				new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(DUCTILIS_PAGE_3)));
		DuctilisPageList.add(new GuiTendencyPage(4, EnumTendencyCatagories.DUCTILIS, "A World of Essence",
				"Only the Essentials", new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(DUCTILIS_PAGE_4)));
		DuctilisPageList.add(new GuiTendencyPage(5, EnumTendencyCatagories.DUCTILIS, "Channeling Basics",
				"Use the Force", new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(DUCTILIS_PAGE_5)));
		DuctilisPageList.add(new GuiTendencyPage(6, EnumTendencyCatagories.DUCTILIS, "Nullification", "Counter Attack",
				new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(DUCTILIS_PAGE_6)));
		DuctilisPageList.add(new GuiTendencyPage(7, EnumTendencyCatagories.DUCTILIS, "Vital Essence",
				"The Power of Vitals", new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(DUCTILIS_PAGE_7)));

		// Ferric
		FerricPageList.add(new GuiTendencyPage(1, EnumTendencyCatagories.FERRIC, "In the Begining",
				"It Started Somewhere", new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(FERRIC_PAGE_1)));
		FerricPageList.add(new GuiTendencyPage(2, EnumTendencyCatagories.FERRIC, "Duality of Nature", "Yin and Yang",
				new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(FERRIC_PAGE_2)));
		FerricPageList.add(new GuiTendencyPage(3, EnumTendencyCatagories.FERRIC, "Vibes", "Cosmic Vibrations",
				new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(FERRIC_PAGE_3)));
		FerricPageList.add(new GuiTendencyPage(4, EnumTendencyCatagories.FERRIC, "A World of Essence",
				"Only the Essentials", new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(FERRIC_PAGE_4)));
		FerricPageList.add(new GuiTendencyPage(5, EnumTendencyCatagories.FERRIC, "Channeling Basics", "Use the Force",
				new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(FERRIC_PAGE_5)));
		FerricPageList.add(new GuiTendencyPage(6, EnumTendencyCatagories.FERRIC, "Nullification", "Counter Attack",
				new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(FERRIC_PAGE_6)));
		FerricPageList.add(new GuiTendencyPage(7, EnumTendencyCatagories.FERRIC, "Vital Essence", "The Power of Vitals",
				new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(FERRIC_PAGE_7)));

		// Lux
		LuxPageList.add(new GuiTendencyPage(1, EnumTendencyCatagories.LUX, "In the Begining", "It Started Somewhere",
				new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(LUX_PAGE_1)));
		LuxPageList.add(new GuiTendencyPage(2, EnumTendencyCatagories.LUX, "Duality of Nature", "Yin and Yang",
				new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(LUX_PAGE_2)));
		LuxPageList.add(new GuiTendencyPage(3, EnumTendencyCatagories.LUX, "Vibes", "Cosmic Vibrations",
				new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(LUX_PAGE_3)));
		LuxPageList.add(new GuiTendencyPage(4, EnumTendencyCatagories.LUX, "A World of Essence", "Only the Essentials",
				new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(LUX_PAGE_4)));
		LuxPageList.add(new GuiTendencyPage(5, EnumTendencyCatagories.LUX, "Channeling Basics", "Use the Force",
				new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(LUX_PAGE_5)));
		LuxPageList.add(new GuiTendencyPage(6, EnumTendencyCatagories.LUX, "Nullification", "Counter Attack",
				new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(LUX_PAGE_6)));
		LuxPageList.add(new GuiTendencyPage(7, EnumTendencyCatagories.LUX, "Vital Essence", "The Power of Vitals",
				new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(LUX_PAGE_7)));
		// Tenebris
		TenebrisPageList
				.add(new GuiTendencyPage(1, EnumTendencyCatagories.TENEBRIS, "In the Begining", "It Started Somewhere",
						new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(TENEBRIS_PAGE_1)));
		TenebrisPageList.add(new GuiTendencyPage(2, EnumTendencyCatagories.TENEBRIS, "Duality of Nature",
				"Yin and Yang", new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(TENEBRIS_PAGE_2)));
		TenebrisPageList.add(new GuiTendencyPage(3, EnumTendencyCatagories.TENEBRIS, "Vibes", "Cosmic Vibrations",
				new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(TENEBRIS_PAGE_3)));
		TenebrisPageList.add(new GuiTendencyPage(4, EnumTendencyCatagories.TENEBRIS, "A World of Essence",
				"Only the Essentials", new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(TENEBRIS_PAGE_4)));
		TenebrisPageList.add(new GuiTendencyPage(5, EnumTendencyCatagories.TENEBRIS, "Channeling Basics",
				"Use the Force", new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(TENEBRIS_PAGE_5)));
		TenebrisPageList.add(new GuiTendencyPage(6, EnumTendencyCatagories.TENEBRIS, "Nullification", "Counter Attack",
				new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(TENEBRIS_PAGE_6)));
		TenebrisPageList.add(new GuiTendencyPage(7, EnumTendencyCatagories.TENEBRIS, "Vital Essence",
				"The Power of Vitals", new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(TENEBRIS_PAGE_7)));

		// Flammeus
		FlammeusPageList
				.add(new GuiTendencyPage(1, EnumTendencyCatagories.FLAMMEUS, "In the Begining", "It Started Somewhere",
						new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(FLAMMEUS_PAGE_1)));
		FlammeusPageList.add(new GuiTendencyPage(2, EnumTendencyCatagories.FLAMMEUS, "Duality of Nature",
				"Yin and Yang", new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(FLAMMEUS_PAGE_2)));
		FlammeusPageList.add(new GuiTendencyPage(3, EnumTendencyCatagories.FLAMMEUS, "Vibes", "Cosmic Vibrations",
				new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(FLAMMEUS_PAGE_3)));
		FlammeusPageList.add(new GuiTendencyPage(4, EnumTendencyCatagories.FLAMMEUS, "A World of Essence",
				"Only the Essentials", new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(FLAMMEUS_PAGE_4)));
		FlammeusPageList.add(new GuiTendencyPage(5, EnumTendencyCatagories.FLAMMEUS, "Channeling Basics",
				"Use the Force", new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(FLAMMEUS_PAGE_5)));
		FlammeusPageList.add(new GuiTendencyPage(6, EnumTendencyCatagories.FLAMMEUS, "Nullification", "Counter Attack",
				new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(FLAMMEUS_PAGE_6)));
		FlammeusPageList.add(new GuiTendencyPage(7, EnumTendencyCatagories.FLAMMEUS, "Vital Essence",
				"The Power of Vitals", new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(FLAMMEUS_PAGE_7)));
		// Congeatio
		CongeatioPageList
				.add(new GuiTendencyPage(1, EnumTendencyCatagories.CONGEATIO, "In the Begining", "It Started Somewhere",
						new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(CONGEATIO_PAGE_1)));
		CongeatioPageList.add(new GuiTendencyPage(2, EnumTendencyCatagories.CONGEATIO, "Duality of Nature",
				"Yin and Yang", new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(CONGEATIO_PAGE_2)));
		CongeatioPageList.add(new GuiTendencyPage(3, EnumTendencyCatagories.CONGEATIO, "Vibes", "Cosmic Vibrations",
				new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(CONGEATIO_PAGE_3)));
		CongeatioPageList.add(
				new GuiTendencyPage(4, EnumTendencyCatagories.CONGEATIO, "A World of Essence", "Only the Essentials",
						new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(CONGEATIO_PAGE_4)));
		CongeatioPageList.add(new GuiTendencyPage(5, EnumTendencyCatagories.CONGEATIO, "Channeling Basics",
				"Use the Force", new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(CONGEATIO_PAGE_5)));
		CongeatioPageList.add(new GuiTendencyPage(6, EnumTendencyCatagories.CONGEATIO, "Nullification",
				"Counter Attack", new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(CONGEATIO_PAGE_6)));
		CongeatioPageList
				.add(new GuiTendencyPage(7, EnumTendencyCatagories.CONGEATIO, "Vital Essence", "The Power of Vitals",
						new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(CONGEATIO_PAGE_7)));

		// Hidden
		HiddenPageList.add(new GuiTendencyPage(1, EnumTendencyCatagories.HIDDEN, "In the Begining",
				"It Started Somewhere", new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(HIDDEN_PAGE_1)));
		HiddenPageList.add(new GuiTendencyPage(2, EnumTendencyCatagories.HIDDEN, "Duality of Nature", "Yin and Yang",
				new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(HIDDEN_PAGE_2)));
		HiddenPageList.add(new GuiTendencyPage(3, EnumTendencyCatagories.HIDDEN, "Vibes", "Cosmic Vibrations",
				new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(HIDDEN_PAGE_3)));
		HiddenPageList.add(new GuiTendencyPage(4, EnumTendencyCatagories.HIDDEN, "A World of Essence",
				"Only the Essentials", new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(HIDDEN_PAGE_4)));
		HiddenPageList.add(new GuiTendencyPage(5, EnumTendencyCatagories.HIDDEN, "Channeling Basics", "Use the Force",
				new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(HIDDEN_PAGE_5)));
		HiddenPageList.add(new GuiTendencyPage(6, EnumTendencyCatagories.HIDDEN, "Nullification", "Counter Attack",
				new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(HIDDEN_PAGE_6)));
		HiddenPageList.add(new GuiTendencyPage(7, EnumTendencyCatagories.HIDDEN, "Vital Essence", "The Power of Vitals",
				new ItemStack(ItemInit.sanguine_formation.get()), I18n.format(HIDDEN_PAGE_7)));

		// Adding Chapters
		Collections.addAll(ChapterList, AnimusPageList, MortemPageList, DuctilisPageList, FerricPageList, LuxPageList,
				TenebrisPageList, FlammeusPageList, CongeatioPageList, HiddenPageList);

		// TOC PAGES
		TOCPageList.add(new GuiTendencyPageTOC(EnumTendencyCatagories.ANIMUS,
				new ItemStack(ItemInit.sanguine_formation.get())));
		TOCPageList.add(new GuiTendencyPageTOC(EnumTendencyCatagories.MORTEM,
				new ItemStack(ItemInit.sanguine_formation.get())));
		TOCPageList.add(
				new GuiTendencyPageTOC(EnumTendencyCatagories.DUCTILIS, new ItemStack(BlockInit.venous_stone.get())));
		TOCPageList.add(new GuiTendencyPageTOC(EnumTendencyCatagories.FERRIC,
				new ItemStack(ItemInit.sanguine_formation.get())));
		TOCPageList
				.add(new GuiTendencyPageTOC(EnumTendencyCatagories.LUX, new ItemStack(BlockInit.venous_stone.get())));
		TOCPageList.add(new GuiTendencyPageTOC(EnumTendencyCatagories.DUCTILIS,
				new ItemStack(ItemInit.sanguine_formation.get())));
		TOCPageList.add(
				new GuiTendencyPageTOC(EnumTendencyCatagories.FLAMMEUS, new ItemStack(BlockInit.venous_stone.get())));
		TOCPageList.add(new GuiTendencyPageTOC(EnumTendencyCatagories.CONGEATIO,
				new ItemStack(ItemInit.sanguine_formation.get())));
		TOCPageList.add(new GuiTendencyPageTOC(EnumTendencyCatagories.HIDDEN,
				new ItemStack(ItemInit.sanguine_formation.get())));

		// Adding the table of contents to each chapter
		AnimusPageList.add(0, TOCPageList.get(0));
		MortemPageList.add(0, TOCPageList.get(1));
		DuctilisPageList.add(0, TOCPageList.get(2));
		FerricPageList.add(0, TOCPageList.get(3));
		LuxPageList.add(0, TOCPageList.get(4));
		TenebrisPageList.add(0, TOCPageList.get(5));
		FlammeusPageList.add(0, TOCPageList.get(6));
		CongeatioPageList.add(0, TOCPageList.get(7));
		HiddenPageList.add(0, TOCPageList.get(8));

	}

	public static List<GuiTendencyPage> getAnimusPageList() {
		return AnimusPageList;
	}

	public static List<GuiTendencyPage> getMortemPageList() {
		return MortemPageList;
	}

	public static List<GuiTendencyPage> getFerricPageList() {
		return FerricPageList;
	}

	public static List<GuiTendencyPage> getDuctilisPageList() {
		return DuctilisPageList;
	}

	public static List<GuiTendencyPage> getLuxPageList() {
		return LuxPageList;
	}

	public static List<GuiTendencyPage> getTenebrisPageList() {
		return TenebrisPageList;
	}

	public static List<GuiTendencyPage> getFlammeusPageList() {
		return FlammeusPageList;
	}

	public static List<GuiTendencyPage> getCongeatioPageList() {
		return CongeatioPageList;
	}

	public static List<GuiTendencyPage> getHiddenPageList() {
		return HiddenPageList;
	}

	public static List<List<GuiTendencyPage>> getChapterList() {
		return ChapterList;
	}

}
