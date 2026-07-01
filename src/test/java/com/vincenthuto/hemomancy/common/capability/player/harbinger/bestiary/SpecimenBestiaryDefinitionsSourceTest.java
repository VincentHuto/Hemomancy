package com.vincenthuto.hemomancy.common.capability.player.harbinger.bestiary;

import com.vincenthuto.hemomancy.common.entity.summon.MorphlingPolypLayer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SpecimenBestiaryDefinitionsSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();
	private static final Path DEFINITIONS = ROOT.resolve(
			"src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/bestiary/SpecimenBestiaryDefinitions.java");
	private static final Path LANG = ROOT.resolve("src/main/resources/assets/hemomancy/lang/en_us.json");

	private SpecimenBestiaryDefinitionsSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String definitions = Files.readString(DEFINITIONS).replace("\r\n", "\n");
		String lang = Files.readString(LANG).replace("\r\n", "\n");

		assertContains("ordered specimen entries are exposed", definitions, "orderedResearchEntries()");
		assertContains("ordered morphling entries are exposed", definitions, "orderedMorphlingEntries()");
		assertContains("research metadata record exists", definitions, "record ResearchEntry");
		assertContains("morphling metadata record exists", definitions, "record MorphlingEntry");
		assertContains("tooth pecks has species-specific alchemist record dialogue", definitions,
				"recordedDialogueKey(ResourceLocation id)");
		assertContains("tooth pecks has species-specific alchemist surrender dialogue", definitions,
				"surrenderedDialogueKey(ResourceLocation id)");

		for (String id : researchIds()) {
			String path = id.substring("hemomancy:".length());
			assertContains("research entry exists for " + id, definitions, "specimen(\"" + path + "\")");
			assertContains("title lang exists for " + id, lang, "\"bestiary.hemomancy.specimen." + path + ".title\"");
			assertContains("description lang exists for " + id, lang, "\"bestiary.hemomancy.specimen." + path + ".description\"");
			assertContains("source lang exists for " + id, lang, "\"bestiary.hemomancy.specimen." + path + ".source\"");
		}
		assertContains("tooth pecks record dialogue lang exists", lang,
				"\"hemomancy.dialogue.event.alchemist_bestiary_recorded.tooth_pecks\"");
		assertContains("tooth pecks surrender dialogue lang exists", lang,
				"\"hemomancy.dialogue.event.alchemist_bestiary_surrendered.tooth_pecks\"");
		assertContains("living bestiary morphling option is localized", lang,
				"\"hemomancy.dialogue.alchemist.option.tell_me_about_morphlings\"");

		for (MorphlingPolypLayer layer : MorphlingPolypLayer.values()) {
			String id = layer.serializedName();
			assertContains("morphling entry exists for " + id, definitions, "MorphlingPolypLayer." + layer.name());
			assertContains("morphling title lang exists for " + id, lang, "\"bestiary.hemomancy.morphling." + id + ".title\"");
			assertContains("morphling description lang exists for " + id, lang, "\"bestiary.hemomancy.morphling." + id + ".description\"");
			assertContains("morphling source lang exists for " + id, lang, "\"bestiary.hemomancy.morphling." + id + ".source\"");
		}
	}

	private static String[] researchIds() {
		return new String[] {
				"hemomancy:barbed_urchin",
				"hemomancy:blood_lantern_jelly",
				"hemomancy:chalybeate_snail",
				"hemomancy:chitinite",
				"hemomancy:crimson_doe",
				"hemomancy:desiccant",
				"hemomancy:fervent_chitinite",
				"hemomancy:hematic_burrower",
				"hemomancy:hemojelly",
				"hemomancy:hemolymphopoda",
				"hemomancy:lantern_tick",
				"hemomancy:morphling_polyp",
				"hemomancy:prism_cuttle",
				"hemomancy:scarlet_serpent",
				"hemomancy:tooth_pecks",
				"hemomancy:venom_rib_centipede",
				"hemomancy:venous_strider",
				"hemomancy:verdigris_moth"
		};
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}
}
