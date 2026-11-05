package com.vincenthuto.hemomancy.client.render.item;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class QliphothSeedItemRendererSourceTest {
	private static final Path ENTITY_RENDERER = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/item/QliphothSeedItemEntityRenderer.java");
	private static final Path TENDRIL_EFFECTS = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/item/QliphothSeedTendrilEffects.java");
	private static final Path FOCUS_RENDERER = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/tile/harbinger/functional/CardinalFocusRenderer.java");
	private static final Path ITEM_RENDERER = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/item/QliphothSeedItemRenderer.java");
	private static final Path ITEM_SOURCE = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/QliphothSeedItem.java");
	private static final Path ITEM_MODEL = Path.of(
			"src/main/resources/assets/hemomancy/models/item/qliphoth_seed.json");
	private static final Path LANG = Path.of(
			"src/main/resources/assets/hemomancy/lang/en_us.json");

	@Test
	void seedRendererKeepsItsShapeAndTooltip() throws IOException {
		String entityRenderer = Files.readString(ENTITY_RENDERER);
		String tendrilEffects = Files.readString(TENDRIL_EFFECTS);
		String focusRenderer = Files.readString(FOCUS_RENDERER);
		String itemRenderer = Files.readString(ITEM_RENDERER);
		String itemSource = Files.readString(ITEM_SOURCE);
		String itemModel = Files.readString(ITEM_MODEL);
		String lang = Files.readString(LANG);
		String extensions = Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/client/item/ItemClientExtensions.java"));
		assertContains("dropped seed renderer should be explicitly named as an entity renderer",
				entityRenderer, "class QliphothSeedItemEntityRenderer");
		assertContains("seed item renderer should keep the requested renderer name",
				itemRenderer, "class QliphothSeedItemRenderer extends BlockEntityWithoutLevelRenderer");
		assertContains("seed item renderer should expose a reusable body renderer for dropped entities",
				itemRenderer, "renderSeedBody");
		assertContains("seed body should be an upright almond, long from tip to tip",
				itemRenderer, "LENGTH_SCALE = 1.7f");
		assertContains("seed body should close to a point at its tips",
				itemRenderer, "TIP_SHARPNESS");
		assertDoesNotContain("seed body should no longer lie on its side",
				itemRenderer, "HORIZONTAL_SCALE");
		assertContains("seed body should keep a smaller seed scale than the pome",
				itemRenderer, "SEED_RADIUS");
		assertContains("seed body should use pome-style Qliphoth core rendering",
				itemRenderer, "HemoRenderTypes.QLIPHOTH_CORE");
		assertContains("seed item should provide a custom renderer",
				extensions, "Map.entry(QliphothSeedItem.class");
		assertContains("seed item should return QliphothSeedItemRenderer",
				extensions, "new QliphothSeedItemRenderer");
		assertContains("seed item should append a usage tooltip",
				itemSource, "appendHoverText");
		assertContains("seed item should use a translatable tooltip key",
				itemSource, "item.hemomancy.qliphoth_seed.tooltip.use");
		assertContains("seed tooltip should be present in the runtime language file",
				lang, "\"item.hemomancy.qliphoth_seed.tooltip.use\"");
		assertContains("seed item model should use the builtin entity renderer path",
				itemModel, "\"parent\": \"builtin/entity\"");
		assertContains("dropped seed renderer should use the shared tendril effect",
				entityRenderer, "QliphothSeedTendrilEffects.spawnForEntity");
		assertContains("seated seed should use the same shared tendril effect",
				focusRenderer, "QliphothSeedTendrilEffects.spawnAt");
		assertContains("shared seed effect should use HutosLib tendril renderer",
				tendrilEffects, "TendrilRenderer.INSTANCE.add");
		assertContains("dropped seed renderer should create HutosLib tendril effect data",
				tendrilEffects, "new TendrilEffectData");
		assertContains("dropped seed renderer should use entity anchors so roots follow the item entity",
				tendrilEffects, "new TendrilAnchor.Entity");
		assertContains("dropped seed renderer should configure HutosLib tendril colors",
				tendrilEffects, "TendrilEffectConfig.defaults()");
		assertContains("dropped seed renderer should distribute HutosLib roots around a full circle",
				tendrilEffects, "FULL_CIRCLE");
		assertContains("dropped seed renderer should rotate root directions between pulses",
				tendrilEffects, "ROOT_PULSE_SPIN");
		assertContains("dropped seed renderer should use asymmetric golden-angle root spacing",
				tendrilEffects, "ROOT_GOLDEN_ANGLE");
		assertContains("each tentacle should writhe in its own plane, not fall back on one world axis",
				tendrilEffects, "root.outward()");
		assertContains("dropped seed renderer should keep each HutosLib root narrow enough to avoid giant fan planes",
				tendrilEffects, "withShape(24, 1, 0.024F * scale, 0.045F)");
		assertContains("seed tentacles should not branch, which kinks them near the tips",
				tendrilEffects, "withBranching(0, 0, 0.12F, 0.45F)");
		assertContains("seed tentacles should hang steeply, to suit the upright almond",
				tendrilEffects, "return 0.36D + 0.28D");
		assertContains("seed tentacles should reach out far less than they hang",
				tendrilEffects, "return 0.09D + 0.13D");
		assertContains("seed tentacles should writhe visibly",
				tendrilEffects, "withWrithe(0.1F * scale, 0.2F");
		assertContains("dropped seed renderer should include pulse in root endpoint angles",
				tendrilEffects, "animatedAngle(gameTime, index, pulse)");
		assertDoesNotContain("seed renderer should not use a tiny fixed root angle table",
				tendrilEffects, "ROOT_ANGLES");
		assertDoesNotContain("seed renderer should not keep its old local quad tendril count",
				entityRenderer, "TENDRIL_COUNT");
		assertDoesNotContain("seed renderer should not keep old local tendril drawing",
				entityRenderer, "drawTendril");
		assertDoesNotContain("seed renderer should not keep old cross-quad drawing",
				entityRenderer, "drawCrossQuad");
		assertDoesNotContain("seed renderer should not use Hemomancy's local Qliphoth render type for roots",
				entityRenderer, "HemoRenderTypes.QLIPHOTH_CORE");
		assertDoesNotContain("seed item model should not be the old flat sprite",
				itemModel, "\"parent\": \"item/generated\"");
	}

	private static void assertContains(String label, String haystack, String needle) {
		if (!haystack.contains(needle)) {
			throw new AssertionError(label + " (missing '" + needle + "')");
		}
	}

	private static void assertDoesNotContain(String label, String haystack, String needle) {
		if (haystack.contains(needle)) {
			throw new AssertionError(label + " (still contains '" + needle + "')");
		}
	}

}
