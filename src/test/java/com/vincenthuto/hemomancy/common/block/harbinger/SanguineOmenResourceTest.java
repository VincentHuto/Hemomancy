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
		String omenBlock = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/block/harbinger/functional/SanguineOmenBlock.java"));
		String omenPacket = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/network/particle/SpawnSanguineOmenEffectPacket.java"));
		String shaderInit = read(SOURCE_ROOT.resolve("com/vincenthuto/hemomancy/common/init/ShaderInit.java"));
		String clientEvents = read(SOURCE_ROOT.resolve("com/vincenthuto/hemomancy/client/event/ClientEvents.java"));
		String omenOverlay = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/screen/overlay/SanguineOmenOverlay.java"));
		String shatterRenderer = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/render/world/SanguineMonolithShatterRenderer.java"));
		String omenShaderJson = read(RESOURCE_ROOT.resolve(
				"assets/hemomancy/shaders/core/screen/sanguine_omen_overlay.json"));
		String omenShader = read(RESOURCE_ROOT.resolve(
				"assets/hemomancy/shaders/core/screen/sanguine_omen_overlay.fsh"));
		String screenShaderJson = read(RESOURCE_ROOT.resolve(
				"assets/hemomancy/shaders/core/screen/sanguine_omen_screen_overlay.json"));
		String screenShader = read(RESOURCE_ROOT.resolve(
				"assets/hemomancy/shaders/core/screen/sanguine_omen_screen_overlay.fsh"));
		String lang = read(RESOURCE_ROOT.resolve("assets/hemomancy/lang/en_us.json"));
		String pickaxeMineable = read(RESOURCE_ROOT.resolve("data/minecraft/tags/blocks/mineable/pickaxe.json"));

		assertContains("block registry includes sanguine omen", blockInit,
				"sanguine_omen = BASEBLOCKS.register(\"sanguine_omen\"");
		assertExists("sanguine omen block class",
				SOURCE_ROOT.resolve("com/vincenthuto/hemomancy/common/block/harbinger/functional/SanguineOmenBlock.java"));
		assertContains("block use checks shift state for shader mode", omenBlock,
				"triggerEffect(level, pos, player.isShiftKeyDown());");
		assertContains("block forwards shader mode to packet helper", omenBlock,
				"EFFECT_PEAK_ALPHA, screenOverlay);");
		assertContains("packet handler registers omen payload", packetHandler,
				"net.playToClient(SpawnSanguineOmenEffectPacket.TYPE");
		assertContains("packet handler exposes nearby send helper", packetHandler,
				"sendSanguineOmenEffect(Vec3 pos, double radius, ServerLevel level, int durationTicks, float peakAlpha)");
		assertContains("packet handler exposes mode-aware send helper", packetHandler,
				"sendSanguineOmenEffect(Vec3 pos, double radius, ServerLevel level, int durationTicks, float peakAlpha, boolean screenOverlay)");
		assertExists("sanguine omen packet",
				SOURCE_ROOT.resolve("com/vincenthuto/hemomancy/common/network/particle/SpawnSanguineOmenEffectPacket.java"));
		assertContains("packet encodes overlay mode", omenPacket,
				"buf.writeBoolean(msg.isScreenOverlay());");
		assertContains("packet starts screen mode", omenPacket,
				"SanguineOmenOverlay.Mode.SCREEN_OVERLAY");
		assertContains("packet starts world mode", omenPacket,
				"SanguineOmenOverlay.Mode.WORLD_GRADE");
		assertContains("shader holder registered", shaderInit,
				"SANGUINE_OMEN_WORLD = new ShaderHolder(Hemomancy.rloc(\"screen/sanguine_omen_overlay\")");
		assertContains("screen overlay shader holder registered", shaderInit,
				"SANGUINE_OMEN_SCREEN_OVERLAY = new ShaderHolder(Hemomancy.rloc(\"screen/sanguine_omen_screen_overlay\")");
		assertContains("shader registration runs on reload", shaderInit,
				"registerShader(event, SANGUINE_OMEN_WORLD.createInstance(provider));");
		assertContains("screen overlay shader registration runs on reload", shaderInit,
				"registerShader(event, SANGUINE_OMEN_SCREEN_OVERLAY.createInstance(provider));");
		assertContains("client tick advances omen overlay", clientEvents,
				"SanguineOmenOverlay.instance.tick();");
		assertContains("gui layer renders omen overlay", clientEvents,
				"SanguineOmenOverlay.instance.renderHUD(graphics, graphics.guiWidth(), graphics.guiHeight(), partialTicks);");
		assertContains("omen color grade runs before hud", clientEvents,
				"renderSanguineOmenWorldGrade(RenderGuiEvent.Pre event)");
		assertContains("world renderer exposes omen burst", shatterRenderer,
				"spawnOmenBurst(Vec3 center)");
		assertContains("overlay exposes effect modes", omenOverlay,
				"public enum Mode");
		assertContains("world grade only renders in world mode", omenOverlay,
				"mode != Mode.WORLD_GRADE");
		assertContains("screen overlay only renders in screen mode", omenOverlay,
				"mode != Mode.SCREEN_OVERLAY");
		assertContains("screen overlay uses screen shader", omenOverlay,
				"ShaderInit.SANGUINE_OMEN_SCREEN_OVERLAY");
		assertContains("overlay owns a copied frame target", omenOverlay,
				"private TextureTarget frameCopyTarget;");
		assertContains("overlay copies the main frame before sampling", omenOverlay,
				"copyMainRenderTarget(minecraft);");
		assertContains("overlay avoids sampling the write target", omenOverlay,
				"RenderSystem.setShaderTexture(0, frameCopyTarget.getColorTextureId());");
		assertContains("shader json declares world sampler", omenShaderJson,
				"{ \"name\": \"Sampler0\" }");
		assertContains("fragment shader flips framebuffer y coordinates", omenShader,
				"vec2 baseUv = vec2(texCoord0.x, 1.0 - texCoord0.y);");
		assertContains("fragment shader samples the frame", omenShader,
				"texture(Sampler0, sampleUv)");
		assertContains("fragment shader uses a red and black scale", omenShader,
				"vec3 redScale = vec3(redValue, 0.0, 0.0);");
		assertContains("fragment shader crushes cloud-like pixels", omenShader,
				"float cloudMask");
		assertContains("fragment shader crushes water-like pixels", omenShader,
				"float waterMask");
		assertContains("fragment shader excludes warm terrain from cloud mask", omenShader,
				"float warmCloudExclusion");
		assertContains("fragment shader preserves warm terrain shadow texture", omenShader,
				"float warmTerrainMask");
		assertContains("fragment shader lifts warm terrain dark flecks", omenShader,
				"float shadowLift = 0.10 * warmTerrainMask * (1.0 - blackMask);");
		assertContains("screen shader json has no world sampler", screenShaderJson,
				"\"samplers\": []");
		assertContains("screen shader is procedural", screenShader,
				"float foam = smoothstep");
		assertExists("blockstate", RESOURCE_ROOT.resolve("assets/hemomancy/blockstates/sanguine_omen.json"));
		assertExists("block model", RESOURCE_ROOT.resolve("assets/hemomancy/models/block/sanguine_omen.json"));
		assertExists("item model", RESOURCE_ROOT.resolve("assets/hemomancy/models/item/sanguine_omen.json"));
		assertExists("block texture", RESOURCE_ROOT.resolve("assets/hemomancy/textures/block/sanguine_omen.png"));
		assertExists("loot table", RESOURCE_ROOT.resolve("data/hemomancy/loot_table/blocks/sanguine_omen.json"));
		assertExists("overlay shader json",
				RESOURCE_ROOT.resolve("assets/hemomancy/shaders/core/screen/sanguine_omen_overlay.json"));
		assertExists("overlay fragment shader",
				RESOURCE_ROOT.resolve("assets/hemomancy/shaders/core/screen/sanguine_omen_overlay.fsh"));
		assertExists("screen overlay shader json",
				RESOURCE_ROOT.resolve("assets/hemomancy/shaders/core/screen/sanguine_omen_screen_overlay.json"));
		assertExists("screen overlay fragment shader",
				RESOURCE_ROOT.resolve("assets/hemomancy/shaders/core/screen/sanguine_omen_screen_overlay.fsh"));
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
