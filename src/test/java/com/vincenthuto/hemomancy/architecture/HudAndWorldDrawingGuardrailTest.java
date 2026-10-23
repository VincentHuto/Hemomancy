package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Step 10 (roadmap Phase 8): HUD drawing goes through layers registered in {@code RegisterGuiLayersEvent}, never
 * {@code RenderGuiEvent} handlers, and world-space effects draw from {@code WorldEffectDispatcher} rather than their
 * own {@code RenderLevelStageEvent} subscribers. The dispatcher keeps the order the separate subscribers ran in.
 */
class HudAndWorldDrawingGuardrailTest {
	private static final Pattern GUI_EVENT = Pattern.compile("\\bRenderGuiEvent\\b");
	private static final Pattern STAGE_SUBSCRIBER = Pattern.compile(
			"@SubscribeEvent(?:\\([^)]*\\))?\\s+(?:public\\s+)?static\\s+void\\s+\\w+\\(\\s*"
					+ "(?:net\\.neoforged\\.neoforge\\.client\\.event\\.)?RenderLevelStageEvent\\s+\\w+\\s*\\)");
	private static final String DISPATCHER = "client/render/world/WorldEffectDispatcher.java";

	@Test
	void hudDrawsThroughGuiLayers() throws IOException {
		Set<String> found = new TreeSet<>();
		for (Path file : sources()) {
			if (GUI_EVENT.matcher(SourceAssertions.read(file)).find()) found.add(RepoPaths.display(file));
		}
		assertEquals(Set.of(), found, "register a layer in GuiLayerRegistrar instead of handling RenderGuiEvent");
	}

	@Test
	void worldEffectsDrawFromTheDispatcher() throws IOException {
		Set<String> found = new TreeSet<>();
		for (Path file : sources()) {
			Matcher matcher = STAGE_SUBSCRIBER.matcher(SourceAssertions.read(file));
			while (matcher.find()) found.add(RepoPaths.display(file));
		}
		assertEquals(Set.of(RepoPaths.display(RepoPaths.mainJava(DISPATCHER))), found,
				"call the effect from WorldEffectDispatcher instead of subscribing to RenderLevelStageEvent");
	}

	@Test
	void theDispatcherRunsSelfStagingEffectsFirstAndInOrder() {
		String body = SourceAssertions.methodBody(SourceAssertions.readMainJava(DISPATCHER),
				"public static void renderLevelLastEvent(RenderLevelStageEvent event)");
		List<String> order = List.of("AxonalTravelView.renderWorld(event)",
				"EarthenVeinTravelClientEvents.renderWorld(event)", "MycophantIntroClientEvents.renderGlimpse(event)",
				"CrossbarHolderTracker.onStage(event)", "AvatarFirstPersonRenderer.render(event)",
				"OutpouringRenderer.render(event)", "ManipulationVisualRenderer.render(event)",
				"event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY");
		assertInOrder(body, order);
		assertTrue(SourceAssertions.readMainJava(DISPATCHER).contains(
				"@SubscribeEvent(priority = EventPriority.HIGH)\n    public static void renderScreenPasses(RenderLevelStageEvent event) {\n        OutpouringScreenPasses.render(event);"),
				"the Outpouring screen passes run at high priority, before other AFTER_LEVEL drawers");
	}

	@Test
	void hudLayersKeepTheOldEventOrder() {
		String registrar = SourceAssertions.readMainJava("client/registry/GuiLayerRegistrar.java");
		String under = SourceAssertions.methodBody(SourceAssertions.readMainJava("client/event/ScreenOverlayEvents.java"),
				"public static void renderUnderHud(GuiGraphics graphics)");
		assertInOrder(under, List.of("renderMorphicNectarScreenOverlay(graphics)", "renderSanguineOmenWorldGrade(graphics)",
				"renderWhiteHumorScreenOverlay(graphics)"));
		assertTrue(registrar.contains("event.registerBelowAll(Hemomancy.rloc(\"screen_effects\")"),
				"full-screen effects draw under every HUD layer");
		String markers = SourceAssertions.section(registrar, "Hemomancy.rloc(\"hud_markers\")", "});");
		assertInOrder(markers, List.of("translate(0.0F, 0.0F, -graphics.pose().last().pose().m32())",
				"EarthenVeinTravelClientEvents.renderTooltip(graphics)", "ManipulationVisualRenderer.hud(graphics)",
				"AntecedentClientEffects.render(graphics)"));
		assertTrue(registrar.lastIndexOf("registerAboveAll(") == registrar.indexOf("registerAboveAll(Hemomancy.rloc(\"hud_markers\")"),
				"the HUD markers are the last Hemomancy layer");
	}

	private static void assertInOrder(String text, List<String> expected) {
		int from = 0;
		List<String> missing = new ArrayList<>();
		for (String call : expected) {
			int at = text.indexOf(call, from);
			if (at < 0) missing.add(call);
			else from = at + call.length();
		}
		assertEquals(List.of(), missing, "missing or out of order");
	}

	private static List<Path> sources() throws IOException {
		try (Stream<Path> files = Files.walk(RepoPaths.MAIN_JAVA)) {
			return files.filter(path -> path.toString().endsWith(".java"))
					.filter(path -> !RepoPaths.display(path).contains("/compat/mna/")).toList();
		}
	}
}
