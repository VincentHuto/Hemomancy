package com.vincenthuto.hemomancy.client.render.world;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SilentArchonMonolithDepthSourceTest {
	private static final Path SILENT_ARCHON_EFFECTS = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/world/chamberofwill/SilentArchonChamberEffects.java");
	private static final Path VESPER_FLOOR_RENDERER = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/world/chamberofwill/VesperFightFloorRenderer.java");
	private static final Path SHADER_INIT = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/init/ShaderInit.java");
	private static final Path RENDER_TYPES = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/HemoRenderTypes.java");
	private static final Path NORMAL_MONOLITH_FRAGMENT = Path.of(
			"src/main/resources/assets/hemomancy/shaders/core/item/monolith_fragment.fsh");
	private static final Path SKY_SHADER_JSON = Path.of(
			"src/main/resources/assets/hemomancy/shaders/core/world/silent_archon_sky_monolith.json");
	private static final Path REFERENCE = Path.of("docs/HEMOMANCY_REFERENCE.md");

	private SilentArchonMonolithDepthSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String silentArchonEffects = read(SILENT_ARCHON_EFFECTS);
		String vesperFloorRenderer = read(VESPER_FLOOR_RENDERER);
		String shaderInit = read(SHADER_INIT);
		String renderTypes = read(RENDER_TYPES);
		String normalMonolithFragment = read(NORMAL_MONOLITH_FRAGMENT);
		String reference = read(REFERENCE);

		assertContains("silent archon sky pillars use fogged monolith material for depth", silentArchonEffects,
				"HemoRenderTypes.monolithEntitySurface(");
		assertNotContains("silent archon sky pillars should not use no-fog sky monolith material", silentArchonEffects,
				"HemoRenderTypes.silentArchonSkyMonolithSurface(");
		assertContains("vesper boundary rocks use the same monolith shader family", vesperFloorRenderer,
				"HemoRenderTypes.monolithEntitySurface(");
		assertContains("vesper boundary rocks render in their own dynamic material pass", vesperFloorRenderer,
				"renderMonolithRocks(event, center);");
		assertContains("vesper monolith pass embeds the tested world-anchored arena transform", vesperFloorRenderer,
				"Matrix4f rockModelView = VesperFightFloorTransform.arenaModelView(");
		assertContains("vesper monolith pass isolates itself from ambient camera transforms", vesperFloorRenderer,
				"modelViewStack.identity();");
		assertNotContains("vesper monolith pass should not depend on an ambient camera-relative pose stack",
				vesperFloorRenderer, "translateToArena(poseStack, event.getCamera(), center);");
		assertNotContains("vesper boundary rocks should not remain in the flat cached floor mesh", vesperFloorRenderer,
				"emitRock(buffer, rock);");
		assertContains("normal monolith fragment remains fog-aware for sky depth", normalMonolithFragment,
				"linear_fog(");
		assertContains("silent archon lower monoliths should draw a base-anchored shadow skirt", silentArchonEffects,
				"renderSilentArchonMonolithBaseShadow(");
		assertContains("silent archon base shadow should be tied to the actual pillar base", silentArchonEffects,
				"float baseShadowY = Mth.lerp(0.34F, baseY, cloudCenterY);");
		assertContains("silent archon near pillar tops should fade before the visible end", silentArchonEffects,
				"float topFadeStart = Mth.lerp(0.78F, baseY, topY);");
		assertNotContains("silent archon near pillar tops should not draw a storm-cloud crown", silentArchonEffects,
				"renderSilentArchonPillarTopCrown(");
		assertContains("silent archon near pillar bases should extend below their old endpoint", silentArchonEffects,
				"float extendedBaseY = baseY - height * 0.24F;");
		assertContains("silent archon near pillar bases should fade before the full shaft", silentArchonEffects,
				"float bottomFadeEnd = Mth.lerp(0.16F, baseY, topY);");
		assertNotContains("shader init should not keep unused no-fog sky monolith shader", shaderInit,
				"SILENT_ARCHON_SKY_MONOLITH");
		assertNotContains("render types should not keep unused no-fog sky monolith material", renderTypes,
				"silentArchonSkyMonolithSurface(");
		assertMissing("unused no-fog sky monolith shader json", SKY_SHADER_JSON);
		assertContains("reference documents depth-preserving fogged monolith pillars", reference,
				"fog-aware Monolith surface shader family");
	}

	private static String read(Path path) throws IOException {
		return Files.readString(path).replace("\r\n", "\n");
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
			throw new AssertionError(label + ": still contains " + unexpected);
		}
	}
}
