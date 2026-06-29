package com.vincenthuto.hemomancy.client.render;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SilentArchonPlayerCoreRenderSourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");
	private static final Path DOCS_ROOT = Path.of("docs");

	private SilentArchonPlayerCoreRenderSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String helper = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/render/armor/SilentArchonPlayerRenderHelper.java"));
		String mixin = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/mixin/core/MixinLivingEntityRenderer.java"));
		String mixinJson = read(RESOURCE_ROOT.resolve("hemomancy.mixins.json"));
		String docs = read(DOCS_ROOT.resolve("HEMOMANCY_REFERENCE.md"));

		assertContains("helper checks helmet", helper, "ItemInit.silent_archon_helm");
		assertContains("helper checks chest", helper, "ItemInit.silent_archon_chestplate");
		assertContains("helper checks legs", helper, "ItemInit.silent_archon_leggings");
		assertContains("helper checks boots", helper, "ItemInit.silent_archon_boots");
		assertContains("helper exposes full-set predicate", helper, "hasFullSilentArchonSet");
		assertContains("helper uses monolith render type", helper, "HemoRenderTypes.monolithEntitySurface");
		assertContains("helper uses stable player seed", helper, "player.getUUID()");

		assertContains("mixin targets living entity renderer", mixin, "@Mixin(LivingEntityRenderer.class)");
		assertContains("mixin injects getRenderType", mixin, "getRenderType");
		assertContains("mixin follows local client hook remap pattern", mixin, "remap = false");
		assertContains("mixin guards player type", mixin, "entity instanceof AbstractClientPlayer");
		assertContains("mixin guards visible body", mixin, "bodyVisible");
		assertContains("mixin avoids translucent vanilla path", mixin, "!translucent");
		assertContains("mixin uses helper predicate", mixin, "SilentArchonPlayerRenderHelper.hasFullSilentArchonSet");
		assertContains("mixin returns helper render type", mixin, "SilentArchonPlayerRenderHelper.playerCoreRenderType");
		assertContains("mixin is registered", mixinJson, "MixinLivingEntityRenderer");

		assertContains("docs mention monolith core", docs, "monolith core");
		assertContains("docs mention translucent armor", docs, "translucent Silent Archon armor");
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
}
