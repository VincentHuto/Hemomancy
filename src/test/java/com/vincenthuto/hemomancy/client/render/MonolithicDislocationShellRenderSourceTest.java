package com.vincenthuto.hemomancy.client.render;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MonolithicDislocationShellRenderSourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");
	private static final Path DOCS_ROOT = Path.of("docs");

	private MonolithicDislocationShellRenderSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String renderTypes = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/render/HemoRenderTypes.java"));
		String layer = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/render/layer/MonolithicDislocationShellLayer.java"));
		String clientState = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/data/MonolithicDislocationClientState.java"));
		String clientEvents = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/event/ClientEvents.java"));
		String layerEvents = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/event/LayerEvents.java"));
		String mixin = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/mixin/core/MixinLivingEntityRenderer.java"));
		String docs = read(DOCS_ROOT.resolve("HEMOMANCY_REFERENCE.md"));

		assertContains("render type method exists", renderTypes, "monolithicDislocationShell");
		assertContains("render type name", renderTypes, "monolithic_dislocation_shell");
		assertContains("render type uses monolith shader", renderTypes, "ShaderInit.MONOLITH_FRAGMENT_ENTITY");
		assertContains("render type is translucent", renderTypes, "RenderType.TRANSLUCENT_TRANSPARENCY");
		assertContains("render type writes color only", renderTypes, "RenderType.COLOR_WRITE");

		assertContains("layer class exists", layer, "class MonolithicDislocationShellLayer");
		assertContains("layer extends render layer", layer, "extends RenderLayer<T, M>");
		assertContains("layer checks synced visual state", layer,
				"MonolithicDislocationClientState.isActive(entity)");
		assertContains("layer skips invisible entities", layer, "entity.isInvisible()");
		assertContains("layer uses stable entity seed", layer, "entity.getUUID()");
		assertContains("layer uses monolith shell render type", layer, "HemoRenderTypes.monolithicDislocationShell");
		assertContains("layer renders supplied model", layer, "model.renderToBuffer");
		assertContains("layer exposes in-render fallback", layer, "renderMixinFallback");
		assertContains("layer suppresses fallback double render", layer, "LAYER_RENDERED_ENTITIES");
		assertContains("layer scales shell thinly", layer, "SHELL_SCALE");
		assertContains("layer uses translucent color", layer, "SHELL_COLOR");
		assertDoesNotContain("layer no longer uses post-render fallback event", layer, "RenderLivingEvent");

		assertContains("client visual state class exists", clientState,
				"class MonolithicDislocationClientState");
		assertContains("client visual state stores target ids", clientState,
				"ACTIVE_UNTIL_BY_ENTITY_ID");
		assertContains("client visual state accepts server sync", clientState,
				"markActive(int entityId, int durationTicks)");
		assertContains("client visual state still trusts local mob effects", clientState,
				"entity.hasEffect(EffectInit.monolithic_dislocation)");
		assertContains("client visual state expires entries", clientState,
				"ACTIVE_UNTIL_BY_ENTITY_ID.entrySet().removeIf");
		assertContains("client ticks monolithic visual state", clientEvents,
				"MonolithicDislocationClientState.tick()");

		assertContains("layer events imports shell layer", layerEvents, "MonolithicDislocationShellLayer");
		assertContains("layer events iterates entity renderer types", layerEvents, "event.getEntityTypes()");
		assertContains("layer events checks living renderer", layerEvents, "renderer instanceof LivingEntityRenderer");
		assertContains("layer events adds shell layer", layerEvents, "new MonolithicDislocationShellLayer");
		assertContains("layer events calls global shell registration", layerEvents,
				"addMonolithicDislocationShellLayers(event)");
		assertDoesNotContain("layer events no longer handles post-render fallback", layerEvents,
				"RenderLivingEvent.Post");
		assertDoesNotContain("layer events no longer delegates post-render fallback", layerEvents,
				"MonolithicDislocationShellLayer.renderFallback");

		assertContains("mixin owns in-render shell fallback", mixin, "MonolithicDislocationShellLayer");
		assertContains("mixin injects living render", mixin, "method = \"render(");
		assertContains("mixin targets pose pop before transform is removed", mixin,
				"target = \"Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V\"");
		assertContains("mixin renders shell fallback before pop", mixin,
				"MonolithicDislocationShellLayer.renderMixinFallback");
		assertDoesNotContain("mixin no longer injects renderer constructor", mixin, "method = \"<init>\"");

		assertContains("docs mention visual shell", docs, "thin translucent monolith shell");
		assertContains("docs mention charged creeper", docs, "charged-creeper-style");
		assertContains("docs mention in-render fallback", docs, "in-render fallback");
	}

	private static String read(Path path) throws IOException {
		if (!Files.exists(path)) {
			throw new AssertionError("missing " + path);
		}
		return Files.readString(path).replace("\r\n", "\n");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}

	private static void assertDoesNotContain(String label, String text, String forbidden) {
		if (text.contains(forbidden)) {
			throw new AssertionError(label + ": still contains " + forbidden);
		}
	}
}
