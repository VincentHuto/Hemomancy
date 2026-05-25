package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class CuttlefishMorphlingResourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");
	private static final Path DOC_ROOT = Path.of("docs");

	private CuttlefishMorphlingResourceTest() {
	}

	public static void main(String[] args) throws IOException {
		assertExists("cuttlefish morphling item class", SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/item/harbinger/morphlings/CuttlefishMorphlingItem.java"));
		assertMissing("old moth morphling item class", SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/item/harbinger/morphlings/MothMorphlingItem.java"));
		assertExists("cuttlefish attachment model", SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/model/entity/summon/MorphlingCuttlefishHeadAttachmentModel.java"));
		assertMissing("old moth attachment model", SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/model/entity/summon/MorphlingMothHeadAttachmentModel.java"));

		assertExists("cuttlefish incubator recipe",
				RESOURCE_ROOT.resolve("data/hemomancy/recipe/incubator/morphling_cuttlefish.json"));
		assertMissing("old moth incubator recipe",
				RESOURCE_ROOT.resolve("data/hemomancy/recipe/incubator/morphling_moth.json"));
		assertExists("cuttlefish item model",
				RESOURCE_ROOT.resolve("assets/hemomancy/models/item/morphling_cuttlefish.json"));
		assertMissing("old moth item model",
				RESOURCE_ROOT.resolve("assets/hemomancy/models/item/morphling_moth.json"));
		assertExists("cuttlefish item texture",
				RESOURCE_ROOT.resolve("assets/hemomancy/textures/item/morphling_cuttlefish.png"));
		assertExists("cuttlefish attachment texture",
				RESOURCE_ROOT.resolve("assets/hemomancy/textures/models/morphling/cuttlefish_head_attachment.png"));
		assertMissing("old moth attachment texture",
				RESOURCE_ROOT.resolve("assets/hemomancy/textures/models/morphling/moth_head_attachment.png"));

		String itemInit = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/init/ItemInit.java"));
		assertContains("item init registers cuttlefish", itemInit,
				"morphling_cuttlefish = BASEITEMS.register(\"morphling_cuttlefish\"");
		assertContains("item init constructs cuttlefish item", itemInit,
				"new CuttlefishMorphlingItem(new Item.Properties().stacksTo(1))");
		assertNotContains("item init no longer registers moth", itemInit, "morphling_moth");

		String clientEvents = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/event/ClientEvents.java"));
		assertContains("living staff selects cuttlefish", clientEvents,
				"selectedStack.getItem() == ItemInit.morphling_cuttlefish.get()");
		assertNotContains("living staff no longer selects moth", clientEvents, "morphling_moth");

		String mutationRegistry = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/morphling/MorphlingMutationRegistry.java"));
		assertContains("mutation registry maps cuttlefish", mutationRegistry,
				"register(ItemInit.morphling_cuttlefish.get()");
		assertContains("mutation registry uses cuttlefish attachment", mutationRegistry,
				"cuttlefishHeadAttachment()");
		assertNotContains("mutation registry no longer maps moth", mutationRegistry, "morphling_moth");
		assertNotContains("mutation registry no longer loads moth model", mutationRegistry, "MorphlingMoth");

		String layerEvents = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/event/LayerEvents.java"));
		assertContains("layer events registers cuttlefish model layer", layerEvents,
				"MorphlingCuttlefishHeadAttachmentModel.LAYER_LOCATION");
		assertNotContains("layer events no longer registers moth model layer", layerEvents, "MorphlingMoth");

		String materials = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/screen/skilltree/shared/MaterialsData.java"));
		assertContains("materials list includes cuttlefish", materials, "morphling_cuttlefish");
		assertNotContains("materials list no longer includes moth morphling", materials, "morphling_moth");

		String language = read(RESOURCE_ROOT.resolve("assets/hemomancy/lang/en_us.json"));
		assertContains("language has cuttlefish item", language,
				"\"item.hemomancy.morphling_cuttlefish\": \"Morphling Cuttlefish\"");
		assertContains("language keeps unstained moth animal", language,
				"\"entity.hemomancy.verdigris_moth\": \"Verdigris Moth\"");
		assertNotContains("language has no moth morphling item", language, "item.hemomancy.morphling_moth");

		String reference = read(DOC_ROOT.resolve("HEMOMANCY_REFERENCE.md"));
		assertContains("reference documents cuttlefish morphling", reference, "Cuttlefish");
		assertContains("reference keeps verdigris moth documentation", reference, "Verdigris Moths");
		assertNotContains("reference no longer documents moth morphling item", reference, "morphling_moth.png");
		assertNotContains("reference no longer documents moth morphling class", reference, "MothMorphlingItem");
	}

	private static String read(Path path) throws IOException {
		if (!Files.exists(path)) {
			throw new AssertionError("missing " + path);
		}
		return Files.readString(path).replace("\r\n", "\n");
	}

	private static void assertExists(String label, Path path) {
		if (!Files.exists(path)) {
			throw new AssertionError(label + ": missing " + path);
		}
	}

	private static void assertMissing(String label, Path path) {
		if (Files.exists(path)) {
			throw new AssertionError(label + ": still exists " + path);
		}
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}

	private static void assertNotContains(String label, String text, String unexpected) {
		if (text.contains(unexpected)) {
			throw new AssertionError(label + ": contained " + unexpected);
		}
	}
}
