package com.vincenthuto.hemomancy.mixin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MixinConfigResourceTest {
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");
	private static final Path SOURCE_ROOT = Path.of("src/main/java");
	private static final Path MODS_TOML = RESOURCE_ROOT.resolve("META-INF/neoforge.mods.toml");
	private static final Path MAIN_MIXIN_CONFIG = RESOURCE_ROOT.resolve("hemomancy.mixins.json");
	private static final Path ENTITY_MIXIN = SOURCE_ROOT.resolve(
			"com/vincenthuto/hemomancy/mixin/core/MixinEntity.java");
	private static final Path LOCAL_PLAYER_MIXIN = SOURCE_ROOT.resolve(
			"com/vincenthuto/hemomancy/mixin/core/MixinLocalPlayer.java");
	private static final Path LIVING_ENTITY_RENDERER_MIXIN = SOURCE_ROOT.resolve(
			"com/vincenthuto/hemomancy/mixin/core/MixinLivingEntityRenderer.java");

	private MixinConfigResourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String modsToml = read(MODS_TOML);
		String mixinConfig = read(MAIN_MIXIN_CONFIG);
		String entityMixin = read(ENTITY_MIXIN);
		String localPlayerMixin = read(LOCAL_PLAYER_MIXIN);
		String livingEntityRendererMixin = read(LIVING_ENTITY_RENDERER_MIXIN);

		assertContains("neoforge.mods.toml should declare the main mixin config",
				modsToml, "config=\"hemomancy.mixins.json\"");
		assertContains("main mixin config should include the Silent Archon player core renderer hook",
				mixinConfig, "\"MixinLivingEntityRenderer\"");
		assertContains("main mixin config should match the Java 21 mod target",
				mixinConfig, "\"compatibilityLevel\": \"JAVA_21\"");
		assertContains("LocalPlayer elytra redirect should use a valid owner descriptor",
				localPlayerMixin,
				"target = \"Lnet/minecraft/client/player/LocalPlayer;getItemBySlot");
		assertNotContains("entity movement mixin should not print every movement",
				entityMixin, "System.out.println");
		assertNotContains("living entity renderer mixin should not print every render pass",
				livingEntityRendererMixin, "System.out.println");
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

	private static void assertNotContains(String label, String text, String unexpected) {
		if (text.contains(unexpected)) {
			throw new AssertionError(label + ": found " + unexpected);
		}
	}
}
