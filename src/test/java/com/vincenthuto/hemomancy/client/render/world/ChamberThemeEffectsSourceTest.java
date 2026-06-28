package com.vincenthuto.hemomancy.client.render.world;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ChamberThemeEffectsSourceTest {
	private static final Path PACKAGE = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/world/chamberofwill");
	private static final Path RENDERER = PACKAGE.resolve("ChamberOfWillEffects.java");
	private static final Path RENDER_CONTEXT = PACKAGE.resolve("ChamberThemeRenderContext.java");
	private static final Path HELPERS = PACKAGE.resolve("ChamberOfWillRenderHelpers.java");
	private static final Path REGISTRY = PACKAGE.resolve("ChamberSkyThemeRegistry.java");
	private static final Path QLIPHOTH_EFFECTS = PACKAGE.resolve("QliphothCommunionChamberEffects.java");
	private static final Path SILENT_ARCHON_EFFECTS = PACKAGE.resolve("SilentArchonChamberEffects.java");
	private static final Path LOWTIDE_EFFECTS = PACKAGE.resolve("MnemonicLowtideChamberEffects.java");
	private static final Path REFERENCE = Path.of("docs/HEMOMANCY_REFERENCE.md");

	private ChamberThemeEffectsSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String renderer = Files.readString(RENDERER).replace("\r\n", "\n");
		String renderContext = Files.readString(RENDER_CONTEXT).replace("\r\n", "\n");
		String helpers = Files.readString(HELPERS).replace("\r\n", "\n");
		String registry = Files.readString(REGISTRY).replace("\r\n", "\n");
		String qliphothEffects = Files.readString(QLIPHOTH_EFFECTS).replace("\r\n", "\n");
		String silentArchonEffects = Files.readString(SILENT_ARCHON_EFFECTS).replace("\r\n", "\n");
		String lowtideEffects = Files.readString(LOWTIDE_EFFECTS).replace("\r\n", "\n");
		String reference = Files.readString(REFERENCE).replace("\r\n", "\n");

		assertFileExists("theme effects interface exists", "ChamberThemeEffects.java");
		assertFileExists("render context exists", "ChamberThemeRenderContext.java");
		assertFileExists("abstract shared effects base exists", "AbstractChamberThemeEffects.java");
		assertFileExists("blank theme effects exists", "BlankChamberThemeEffects.java");
		assertFileExists("default theme effects exists", "WillDefaultChamberEffects.java");
		assertFileExists("archon revelation theme effects exists", "ArchonRevelationChamberEffects.java");
		assertFileExists("qliphoth communion theme effects exists", "QliphothCommunionChamberEffects.java");
		assertFileExists("silent archon theme effects exists", "SilentArchonChamberEffects.java");
		assertFileExists("apotheos theme effects exists", "ApotheosChamberEffects.java");
		assertFileExists("mnemonic lowtide effects exists", "MnemonicLowtideChamberEffects.java");

		assertContains("renderer delegates sky rendering to active theme effects", renderer,
				"return ChamberSkyThemeRegistry.activeEffects().renderSky(context);");
		assertContains("renderer derives animated sky time from render callback ticks", renderer,
				"float f = (ticks + partialTick) * theme.motionMultiplier();");
		assertNotContains("renderer should not use client level game time for sky animation", renderer,
				"level.getGameTime()");
		assertContains("renderer delegates fog color to active theme effects", renderer,
				"return ChamberSkyThemeRegistry.activeEffects().getBrightnessDependentFogColor(fogColor, brightness);");
		assertContains("renderer delegates fogginess to active theme effects", renderer,
				"return ChamberSkyThemeRegistry.activeEffects().isFoggyAt(x, y);");
		assertNotContains("renderer should not gate qliphoth visuals directly", renderer,
				"THEME_QLIPHOTH_COMMUNION.equals(theme.id())");
		assertNotContains("renderer should not gate silent archon visuals directly", renderer,
				"THEME_SILENT_ARCHON.equals(theme.id())");
		assertContains("render context is public because public theme effect classes expose it in overrides", renderContext,
				"public record ChamberThemeRenderContext(");

		assertContains("registry stores effect strategies", registry,
				"private static final Map<ResourceLocation, ChamberThemeEffects> EFFECTS");
		assertContains("registry exposes active effect strategy", registry,
				"public static ChamberThemeEffects activeEffects()");
		assertContains("will default effects are registered", registry,
				"new WillDefaultChamberEffects(");
		assertContains("archon revelation effects are registered", registry,
				"new ArchonRevelationChamberEffects(");
		assertContains("qliphoth communion effects are registered", registry,
				"new QliphothCommunionChamberEffects(");
		assertContains("silent archon effects are registered", registry,
				"new SilentArchonChamberEffects(");
		assertContains("mnemonic lowtide effects are registered", registry,
				"new MnemonicLowtideChamberEffects(mnemonicLowtide)");
		assertNotContains("mnemonic lowtide no longer uses blank theme effects", registry,
				"new BlankChamberThemeEffects(mnemonicLowtide)");
		assertContains("lowtide effects own skybox lake render entry", lowtideEffects,
				"renderLowtideSkyLake(");
		assertContains("lowtide effects use lowtide lake render type", lowtideEffects,
				"HemoRenderTypes.mnemonicLowtideLake(");
		assertContains("lowtide effects place lake in skybox space", lowtideEffects,
				"context.skyDistance()");
		assertNotContains("lowtide effects should not place lake below physical floor", lowtideEffects,
				"ChamberOfWillManager.FLOOR_Y");
		assertContains("apotheos effects are registered", registry,
				"new ApotheosChamberEffects(");

		assertContains("qliphoth effects own qliphoth backdrop rendering", qliphothEffects,
				"static void renderQliphothCommunionBackdrop(");
		assertContains("qliphoth effects own qliphoth sky rendering", qliphothEffects,
				"static void renderQliphothCommunionSky(");
		assertContains("qliphoth effects own qliphoth black hole rendering", qliphothEffects,
				"static void renderQliphothBlackHoles(");
		assertContains("qliphoth effects own qliphoth sky point helpers", qliphothEffects,
				"static Vector3f qliphothSkyPoint(");
		assertNotContains("qliphoth effects should not delegate themed render bodies to helpers", qliphothEffects,
				"ChamberOfWillRenderHelpers.renderQliphoth");
		assertNotContains("shared helpers should not contain qliphoth render bodies", helpers,
				"renderQliphoth");

		assertContains("silent archon effects own depth pass rendering", silentArchonEffects,
				"static void renderSilentArchonDepthEffects(");
		assertContains("silent archon effects own foreground storm cloud rendering", silentArchonEffects,
				"static void renderSilentArchonForegroundStormClouds(");
		assertContains("silent archon effects own monolith pillar rendering", silentArchonEffects,
				"static void renderSilentArchonMonolithPillars(");
		assertContains("silent archon effects own fog color", silentArchonEffects,
				"static Vec3 silentArchonFogColor(");
		assertNotContains("silent archon effects should not delegate themed render bodies to helpers", silentArchonEffects,
				"ChamberOfWillRenderHelpers.renderSilentArchon");
		assertNotContains("shared helpers should not contain silent archon render bodies", helpers,
				"renderSilentArchon");

		assertNotContains("shared helpers should not contain mnemonic lowtide render bodies", helpers,
				"renderMnemonicLowtide");

		assertContains("reference documents per-theme effects ownership", reference,
				"Each registered Chamber sky theme also owns a `ChamberThemeEffects` strategy");
	}

	private static void assertFileExists(String label, String fileName) {
		Path file = PACKAGE.resolve(fileName);
		if (!Files.exists(file)) {
			throw new AssertionError(label + ": missing " + file);
		}
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}

	private static void assertNotContains(String label, String text, String unexpected) {
		if (text.contains(unexpected)) {
			throw new AssertionError(label + ": still contains " + unexpected);
		}
	}
}
