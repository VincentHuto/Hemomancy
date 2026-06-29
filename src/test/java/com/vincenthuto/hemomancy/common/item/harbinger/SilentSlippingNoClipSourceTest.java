package com.vincenthuto.hemomancy.common.item.harbinger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SilentSlippingNoClipSourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");
	private static final Path DOCS_ROOT = Path.of("docs");

	private SilentSlippingNoClipSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String handler = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/armor/ability/SilentArchonArmorAbilityHandler.java"));
		String entityMixin = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/mixin/core/MixinEntity.java"));
		String mixins = read(RESOURCE_ROOT.resolve("hemomancy.mixins.json"));
		String docs = read(DOCS_ROOT.resolve("HEMOMANCY_REFERENCE.md"));

		assertContains("handler exposes unified no-clip active query", handler,
				"isSilentSlippingNoClipActive(Player player)");
		assertContains("handler checks client sync timer", handler, "CLIENT_SILENT_SLIPPING_UNTIL_KEY");
		assertContains("handler checks server state", handler, "player instanceof ServerPlayer serverPlayer");
		assertContains("handler exposes no-clip applier", handler, "applySilentSlippingNoClip(Player player)");
		assertContains("handler no-clip applier sets noPhysics", handler, "player.noPhysics = true");
		assertContains("server movement uses no-clip applier", handler, "applySilentSlippingNoClip(player);");

		assertContains("entity mixin targets base entity movement", entityMixin, "@Mixin(Entity.class)");
		assertContains("entity mixin hooks move", entityMixin, "method = \"move\"");
		assertContains("entity mixin runs before collision resolution", entityMixin, "at = @At(\"HEAD\")");
		assertContains("entity mixin only handles players", entityMixin, "instanceof Player player");
		assertContains("entity mixin reinforces silent slip no-clip", entityMixin,
				"SilentArchonArmorAbilityHandler.applySilentSlippingNoClip(player)");
		assertContains("entity mixin registered", mixins, "\"MixinEntity\"");

		assertContains("docs mention movement-collision reinforcement", docs,
				"reinforced at the movement collision boundary");
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
