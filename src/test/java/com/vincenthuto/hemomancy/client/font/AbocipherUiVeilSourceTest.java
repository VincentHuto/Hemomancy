package com.vincenthuto.hemomancy.client.font;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class AbocipherUiVeilSourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");

	private AbocipherUiVeilSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		assertPathExists("client veil helper",
				SOURCE_ROOT.resolve("com/vincenthuto/hemomancy/client/screen/util/AbocipherText.java"));

		String entries = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/capability/player/shared/knowledge/discovery/LiberEntryDefinitions.java"));
		assertContains("knowledge entry constant", entries, "ABOCIPHER_LITERACY");
		assertContains("knowledge entry dialogue source", entries,
				"register(ABOCIPHER_LITERACY, HemomancyDiscoverySource.DIALOGUE");

		String vicar = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/entity/npc/dialogue/HarbingerVicarDialogueTrees.java"));
		assertContains("vicar literacy-aware factory", vicar,
				"forDegree(int degree, int entityId, boolean hasBloodline, boolean hasAbocipherLiteracy)");
		assertContains("degree 3 should use ordinary initiate dialogue", vicar, "case 3 -> initiate(entityId);");
		assertContains("degree 4 should keep literacy-aware adept dialogue", vicar,
				"case 4 -> adept(entityId, hasAbocipherLiteracy);");
		assertContains("vicar blood-shotting event", vicar, "vicar_blood_shotting");
		assertContains("vicar ritual option", vicar, "hemomancy.dialogue.vicar.option.open_blood_script");
		assertNotContains("initiate dialogue should not include blood-script ritual",
				section(vicar, "public static DialogueTree initiate", "public static DialogueTree adept"),
				"blood_script_ritual");

		String handler = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/entity/npc/dialogue/DialogueEventHandler.java"));
		assertContains("dialogue handler unlocks literacy", handler, "ABOCIPHER_LITERACY");
		assertContains("dialogue handler ritual duration", handler, "1200");
		assertContains("dialogue handler uses screen overlay packet", handler, "SpawnSanguineOmenEffectPacket");

		String vicarEntity = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/entity/npc/harbinger/HarbingerVicarEntity.java"));
		assertContains("vicar entity checks literacy", vicarEntity, "hasAbocipherLiteracy");

		String inscription = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/screen/discovery/DiscoveryInscriptionScreen.java"));
		assertContains("inscriptions use abocipher veil", inscription, "AbocipherText");
		assertContains("revealed blood echoes can still be veiled", inscription,
				"(!revealed || requiresAbocipherLiteracy)");

		String inscriptionPacket = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/network/discovery/OpenInscriptionPacket.java"));
		assertContains("inscription packet carries abocipher literacy gate", inscriptionPacket,
				"requiresAbocipherLiteracy");
		assertContains("packet forwards abocipher literacy gate to screen", inscriptionPacket,
				"msg.requiresAbocipherLiteracy");

		String inscriptionBlock = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/block/inscription/DiscoveryInscriptionBlock.java"));
		assertContains("blood echoes request abocipher literacy", inscriptionBlock,
				"requiresAbocipherLiteracy(definition)");
		assertContains("abocipher literacy gate is blood echo specific", inscriptionBlock,
				"definition.kind() == DiscoveryInscriptionDefinition.Kind.BLOOD_ECHO");

		String manipulations = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/screen/skilltree/harbinger/ManipulationsTabController.java"));
		assertContains("manipulation screen uses abocipher veil", manipulations, "AbocipherText");

		String lang = read(RESOURCE_ROOT.resolve("assets/hemomancy/lang/en_us.json"));
		assertContains("ritual option translation", lang, "hemomancy.dialogue.vicar.option.open_blood_script");
		assertContains("ritual completion translation", lang, "hemomancy.dialogue.event.vicar_blood_shotting");
	}

	private static String read(Path path) throws IOException {
		if (!Files.exists(path)) {
			throw new AssertionError("missing " + path);
		}
		return Files.readString(path).replace("\r\n", "\n");
	}

	private static void assertPathExists(String label, Path path) {
		if (!Files.exists(path)) {
			throw new AssertionError(label + ": missing " + path);
		}
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}

	private static void assertNotContains(String label, String text, String unexpected) {
		if (text.contains(unexpected)) {
			throw new AssertionError(label + ": found " + unexpected);
		}
	}

	private static String section(String text, String startMarker, String endMarker) {
		int start = text.indexOf(startMarker);
		if (start < 0) {
			throw new AssertionError("missing section start " + startMarker);
		}
		int end = text.indexOf(endMarker, start + startMarker.length());
		if (end < 0) {
			throw new AssertionError("missing section end " + endMarker);
		}
		return text.substring(start, end);
	}
}
