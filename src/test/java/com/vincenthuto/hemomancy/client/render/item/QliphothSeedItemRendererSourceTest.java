package com.vincenthuto.hemomancy.client.render.item;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class QliphothSeedItemRendererSourceTest {
	private static final Path ENTITY_RENDERER = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/item/QliphothSeedItemEntityRenderer.java");
	private static final Path TENDRIL_EFFECTS = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/item/QliphothSeedTendrilEffects.java");
	private static final Path FOCUS_RENDERER = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/tile/functional/CardinalFocusRenderer.java");
	private static final Path ITEM_RENDERER = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/item/QliphothSeedItemRenderer.java");
	private static final Path ITEM_SOURCE = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/QliphothSeedItem.java");
	private static final Path ITEM_MODEL = Path.of(
			"src/main/resources/assets/hemomancy/models/item/qliphoth_seed.json");
	private static final Path LANG = Path.of(
			"src/main/resources/assets/hemomancy/lang/en_us.json");
	private static final Path LANGUAGE_PROVIDER = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/data/gen/HemoLanguageProvider.java");

	public static void main(String[] args) throws IOException {
		String entityRenderer = Files.readString(ENTITY_RENDERER);
		String tendrilEffects = Files.readString(TENDRIL_EFFECTS);
		String focusRenderer = Files.readString(FOCUS_RENDERER);
		String itemRenderer = Files.readString(ITEM_RENDERER);
		String itemSource = Files.readString(ITEM_SOURCE);
		String itemModel = Files.readString(ITEM_MODEL);
		String lang = Files.readString(LANG);
		String languageProvider = Files.readString(LANGUAGE_PROVIDER);
		assertContains("dropped seed renderer should be explicitly named as an entity renderer",
				entityRenderer, "class QliphothSeedItemEntityRenderer");
		assertContains("seed item renderer should keep the requested renderer name",
				itemRenderer, "class QliphothSeedItemRenderer extends BlockEntityWithoutLevelRenderer");
		assertContains("seed item renderer should expose a reusable body renderer for dropped entities",
				itemRenderer, "renderSeedBody");
		assertContains("seed body should be horizontally elongated",
				itemRenderer, "HORIZONTAL_SCALE");
		assertContains("seed body elongation should be subtle, not a stretched capsule",
				itemRenderer, "HORIZONTAL_SCALE = 1.38f");
		assertDoesNotContain("seed body should no longer use the overly dramatic horizontal stretch",
				itemRenderer, "HORIZONTAL_SCALE = 1.72f");
		assertContains("seed body should keep a smaller seed scale than the pome",
				itemRenderer, "SEED_RADIUS");
		assertContains("seed body should use pome-style Qliphoth core rendering",
				itemRenderer, "HemoRenderTypes.QLIPHOTH_CORE");
		assertContains("seed item should provide a custom renderer",
				itemSource, "implements HemoClientItemExtensionsProvider");
		assertContains("seed item should return QliphothSeedItemRenderer",
				itemSource, "new QliphothSeedItemRenderer");
		assertContains("seed item should append a usage tooltip",
				itemSource, "appendHoverText");
		assertContains("seed item should use a translatable tooltip key",
				itemSource, "item.hemomancy.qliphoth_seed.tooltip.use");
		assertContains("seed tooltip should be present in the runtime language file",
				lang, "\"item.hemomancy.qliphoth_seed.tooltip.use\"");
		assertContains("seed tooltip should be preserved by data generation",
				languageProvider, "item.hemomancy.qliphoth_seed.tooltip.use");
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
		assertContains("dropped seed renderer should emit enough roots to read as radial",
				tendrilEffects, "ROOTS_PER_PULSE = 12");
		assertContains("dropped seed renderer should keep each HutosLib root narrow enough to avoid giant fan planes",
				tendrilEffects, "withShape(16, 1, 0.024F, 0.045F)");
		assertContains("dropped seed renderer should keep branch bursts modest around the seed",
				tendrilEffects, "withBranching(1, 1, 0.12F, 0.45F)");
		assertContains("dropped seed roots should spread outward more than downward to avoid HutosLib vertical basis bias",
				tendrilEffects, "return 0.46D + 0.18D");
		assertContains("dropped seed roots should use a shallow drop instead of a vertical curtain",
				tendrilEffects, "return 0.08D + 0.08D");
		assertContains("dropped seed roots should not use sag because sag bends near-vertical HutosLib roots to one world side",
				tendrilEffects, "withWrithe(0.04F, 0.09F, 0.08F, 0.0F)");
		assertDoesNotContain("seed roots should not keep the downward-heavy reach that collapsed into a one-direction fan",
				tendrilEffects, "return 0.24D + 0.10D");
		assertDoesNotContain("seed roots should not keep the deep drop that triggered the vertical basis fallback",
				tendrilEffects, "return 0.32D + 0.12D");
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

	private QliphothSeedItemRendererSourceTest() {
	}
}
