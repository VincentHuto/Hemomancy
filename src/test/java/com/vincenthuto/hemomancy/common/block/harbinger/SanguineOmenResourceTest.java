package com.vincenthuto.hemomancy.common.block.harbinger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SanguineOmenResourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");

	private SanguineOmenResourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String blockInit = read(SOURCE_ROOT.resolve("com/vincenthuto/hemomancy/common/init/BlockInit.java"));
		String packetHandler = read(SOURCE_ROOT.resolve("com/vincenthuto/hemomancy/common/network/PacketHandler.java"));
		String shaderInit = read(SOURCE_ROOT.resolve("com/vincenthuto/hemomancy/common/init/ShaderInit.java"));
		String clientEvents = read(SOURCE_ROOT.resolve("com/vincenthuto/hemomancy/client/event/ClientEvents.java"));
		String shatterRenderer = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/render/world/SanguineMonolithShatterRenderer.java"));
		String lang = read(RESOURCE_ROOT.resolve("assets/hemomancy/lang/en_us.json"));
		String pickaxeMineable = read(RESOURCE_ROOT.resolve("data/minecraft/tags/blocks/mineable/pickaxe.json"));

		assertContains("block registry includes sanguine omen", blockInit,
				"sanguine_omen = BASEBLOCKS.register(\"sanguine_omen\"");
		assertExists("sanguine omen block class",
				SOURCE_ROOT.resolve("com/vincenthuto/hemomancy/common/block/harbinger/functional/SanguineOmenBlock.java"));
		assertContains("packet handler registers omen payload", packetHandler,
				"net.playToClient(SpawnSanguineOmenEffectPacket.TYPE");
		assertContains("packet handler exposes nearby send helper", packetHandler,
				"sendSanguineOmenEffect(Vec3 pos, double radius, ServerLevel level, int durationTicks, float peakAlpha)");
		assertExists("sanguine omen packet",
				SOURCE_ROOT.resolve("com/vincenthuto/hemomancy/common/network/particle/SpawnSanguineOmenEffectPacket.java"));
		assertContains("shader holder registered", shaderInit,
				"SANGUINE_OMEN_OVERLAY = new ShaderHolder(Hemomancy.rloc(\"screen/sanguine_omen_overlay\")");
		assertContains("shader registration runs on reload", shaderInit,
				"registerShader(event, SANGUINE_OMEN_OVERLAY.createInstance(provider));");
		assertContains("client tick advances omen overlay", clientEvents,
				"SanguineOmenOverlay.instance.tick();");
		assertContains("gui layer renders omen overlay", clientEvents,
				"SanguineOmenOverlay.instance.renderHUD(graphics, graphics.guiWidth(), graphics.guiHeight(), partialTicks);");
		assertContains("world renderer exposes omen burst", shatterRenderer,
				"spawnOmenBurst(Vec3 center)");
		assertExists("blockstate", RESOURCE_ROOT.resolve("assets/hemomancy/blockstates/sanguine_omen.json"));
		assertExists("block model", RESOURCE_ROOT.resolve("assets/hemomancy/models/block/sanguine_omen.json"));
		assertExists("item model", RESOURCE_ROOT.resolve("assets/hemomancy/models/item/sanguine_omen.json"));
		assertExists("block texture", RESOURCE_ROOT.resolve("assets/hemomancy/textures/block/sanguine_omen.png"));
		assertExists("loot table", RESOURCE_ROOT.resolve("data/hemomancy/loot_table/blocks/sanguine_omen.json"));
		assertExists("overlay shader json",
				RESOURCE_ROOT.resolve("assets/hemomancy/shaders/core/screen/sanguine_omen_overlay.json"));
		assertExists("overlay fragment shader",
				RESOURCE_ROOT.resolve("assets/hemomancy/shaders/core/screen/sanguine_omen_overlay.fsh"));
		assertContains("display name is localized", lang,
				"\"block.hemomancy.sanguine_omen\": \"Sanguine Omen\"");
		assertContains("pickaxe mineable tag includes block", pickaxeMineable,
				"\"hemomancy:sanguine_omen\"");
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

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}
}
