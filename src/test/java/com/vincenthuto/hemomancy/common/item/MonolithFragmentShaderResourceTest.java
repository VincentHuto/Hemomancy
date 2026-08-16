package com.vincenthuto.hemomancy.common.item;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MonolithFragmentShaderResourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");

	private MonolithFragmentShaderResourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String shaderInit = read(SOURCE_ROOT.resolve("com/vincenthuto/hemomancy/common/init/ShaderInit.java"));
		String renderTypes = read(SOURCE_ROOT.resolve("com/vincenthuto/hemomancy/client/render/HemoRenderTypes.java"));
		String renderer = read(SOURCE_ROOT.resolve("com/vincenthuto/hemomancy/client/render/item/MonolithFragmentItemRenderer.java"));
		String fragmentItem = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/item/harbinger/MonolithFragmentItem.java"));
		String shaderJson = read(RESOURCE_ROOT.resolve("assets/hemomancy/shaders/core/item/monolith_fragment.json"));
		String vertexShader = read(RESOURCE_ROOT.resolve("assets/hemomancy/shaders/core/item/monolith_fragment.vsh"));
		String fragmentShader = read(RESOURCE_ROOT.resolve("assets/hemomancy/shaders/core/item/monolith_fragment.fsh"));

		assertContains("shader holder registered", shaderInit,
				"MONOLITH_FRAGMENT = new ShaderHolder(Hemomancy.rloc(\"item/monolith_fragment\")");
		assertContains("entity shader holder registered for armor overlays", shaderInit,
				"MONOLITH_FRAGMENT_ENTITY = new ShaderHolder(Hemomancy.rloc(\"item/monolith_fragment\")");
		assertContains("entity shader holder uses entity vertex format", shaderInit,
				"DefaultVertexFormat.NEW_ENTITY");
		assertContains("shader registration runs on resource reload", shaderInit,
				"registerShader(event, MONOLITH_FRAGMENT.createInstance(provider));");
		assertContains("entity shader registration runs on resource reload", shaderInit,
				"registerShader(event, MONOLITH_FRAGMENT_ENTITY.createInstance(provider));");
		assertContains("render type uses fragment shader holder", renderTypes,
				"ShaderInit.MONOLITH_FRAGMENT.getShard()");
		assertContains("armor overlay render type exists", renderTypes,
				"silentArchonArmorOverlay");
		assertContains("armor overlay uses entity model vertex format", renderTypes,
				"DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS");
		assertContains("armor overlay uses entity shader holder", renderTypes,
				"ShaderInit.MONOLITH_FRAGMENT_ENTITY.getShard()");
		assertContains("render type carries burden uniform", renderTypes,
				"setUniform(shader, \"Burden\", burden)");
		assertContains("render type carries attunement uniform", renderTypes,
				"setUniform(shader, \"Attuned\", attuned)");
		assertContains("render type carries custom seconds time uniform", renderTypes,
				"setUniform(shader, \"HemoTime\", gameTime)");
		assertNotContains("render type avoids Minecraft's built-in GameTime overwrite", renderTypes,
				"setUniform(shader, \"GameTime\"");
		assertContains("monolith render type emits uv-mapped low-poly triangles", renderTypes,
				"DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP");
		assertContains("renderer subdivides shards for shader resolution", renderer,
				"private static final int SUBDIVISIONS = 8;");
		assertContains("renderer uses seconds for readable fragment morph timing", renderer,
				"float timeSeconds =");
		assertContains("renderer keeps item silhouette calm while the material animates", renderer,
				"MORPH_PHASE_RATE = 0.0f;");
		assertContains("renderer has a bounded GUI scale for inventory cells", renderer,
				"GUI_SCALE = 1.55f;");
		assertContains("renderer uses a dedicated inventory silhouette", renderer,
				"drawGuiBoundTriangle(vc, mat, timeSeconds, combinedLight)");
		assertContains("renderer keeps inventory geometry inside a tight cell-safe radius", renderer,
				"GUI_BOUND_RADIUS = 0.22f;");
		assertContains("renderer tells the shader when the item is in GUI", renderer,
				"float guiClamp = displayContext == ItemDisplayContext.GUI ? 1.0f : 0.0f;");
		assertContains("renderer keeps gui silhouette radius static", renderer,
				"float radius = GUI_BOUND_RADIUS;");
		assertNotContains("renderer does not breathe the inventory silhouette", renderer,
				"Math.sin(time * 0.9f)");
		assertContains("renderer keeps inventory morphing visual-only, not positional", renderer,
				"displayContext == ItemDisplayContext.GUI");
		assertContains("renderer disables shard orbit drift in GUI", renderer,
				"GUI_ORBIT_DRIFT_RATE = 0.0f;");
		assertContains("renderer uses smaller GUI shard geometry", renderer,
				"GUI_SHARD_SIZE_SCALE = 0.68f;");
		assertContains("renderer uses smaller GUI orbit radius", renderer,
				"GUI_SHARD_SPREAD_SCALE = 0.25f;");
		assertContains("renderer resolves display-context render tuning", renderer,
				"resolveRenderTuning(displayContext)");
		assertContains("renderer applies tuning when drawing triangles", renderer,
				"drawMorphingTriangle(vc, mat, i, timeSeconds, morphPhase, combinedLight, tuning)");
		assertContains("renderer interpolates between triangle shape states", renderer,
				"morphNoise(");
		assertContains("renderer eases triangle shape transitions", renderer,
				"smoothStep(");
		assertNotContains("renderer avoids discrete per-frame shape targets", renderer,
				"Math.floor(timeSeconds * MORPH_PHASE_RATE)");
		assertNotContains("renderer avoids old unreadable tick-fast morph rate", renderer,
				"time * 3.75f");
		assertContains("renderer passes burden state to material", renderer,
				"MonolithFragmentBurdenRules.shouldBurdenCarrier");
		assertContains("fragment item applies inventory burden", fragmentItem,
				"applyApotheosBurden(player)");
		assertContains("shader json references vertex program", shaderJson,
				"\"vertex\": \"hemomancy:item/monolith_fragment\"");
		assertContains("shader json references fragment program", shaderJson,
				"\"fragment\": \"hemomancy:item/monolith_fragment\"");
		assertContains("shader json exposes shard seed", shaderJson,
				"\"name\": \"ShardSeed\"");
		assertContains("shader json exposes custom seconds time", shaderJson,
				"\"name\": \"HemoTime\"");
		assertNotContains("shader json avoids Minecraft's built-in GameTime uniform", shaderJson,
				"\"name\": \"GameTime\"");
		assertNotContains("shader json does not request unused view rotation uniform", shaderJson,
				"\"name\": \"IViewRotMat\"");
		assertContains("vertex shader morphs geometry", vertexShader,
				"p.xz += normalize(fromCenter) * morph * strength * edgeWeight");
		assertContains("vertex shader animates silhouette noise", vertexShader,
				"float morph = sin(");
		assertNotContains("vertex shader avoids discrete GameTime shape targets", vertexShader,
				"floor(GameTime * (SNAP_RATE_BASE");
		assertContains("vertex shader keeps morph changes readable", vertexShader,
				"VERTEX_MORPH_RATE = 1.45");
		assertContains("vertex shader uses custom seconds time", vertexShader,
				"uniform float HemoTime;");
		assertNotContains("vertex shader avoids overwritten GameTime input", vertexShader,
				"GameTime");
		assertNotContains("vertex shader avoids old tick-fast snap rate", vertexShader,
				"GameTime * (7.0 + Burden * 8.0)");
		assertNotContains("vertex shader does not declare unused view rotation uniform", vertexShader,
				"IViewRotMat");
		assertContains("vertex shader uses the 1.21 fog helper signature", vertexShader,
				"fog_distance(p, FogShape)");
		assertNotContains("vertex shader avoids the removed pre-1.21 fog helper signature", vertexShader,
				"fog_distance(ModelViewMat");
		assertContains("fragment shader creates fractal fault lines", fragmentShader,
				"fractalFault");
		assertContains("fragment shader keeps fractal drift slow enough to read", fragmentShader,
				"FRACTAL_WEB_DRIFT_RATE = 0.11");
		assertContains("fragment shader uses custom seconds time", fragmentShader,
				"uniform float HemoTime;");
		assertNotContains("fragment shader avoids overwritten GameTime input", fragmentShader,
				"GameTime");
		assertContains("fragment shader animates red vein flow", fragmentShader,
				"veinFlow");
		assertContains("fragment shader keeps vein flow readable", fragmentShader,
				"VEIN_FLOW_RATE = 1.35");
		assertContains("fragment shader separates raw fault distance from display mask", fragmentShader,
				"faultDistance");
		assertContains("fragment shader restricts bright animation to vein cores", fragmentShader,
				"veinCore");
		assertContains("fragment shader has a visible moving vein head", fragmentShader,
				"flowHead");
		assertContains("fragment shader keeps animated veins from becoming a solid plate", fragmentShader,
				"flowDarkGap");
		assertContains("fragment shader animates vein geometry separately from brightness", fragmentShader,
				"movingFaultDistance");
		assertContains("fragment shader visibly advects the red vein mask", fragmentShader,
				"flowOffset");
		assertContains("fragment shader carries a moving red vein core", fragmentShader,
				"movingCore");
		assertNotContains("fragment shader avoids only pulsing the static core mask", fragmentShader,
				"return core * (flowHead + flowTrail) * flowDarkGap * pulse;");
		assertContains("fragment shader applies additive vein glow", fragmentShader,
				"veinGlow");
		assertNotContains("fragment shader avoids constant glow filling all veins", fragmentShader,
				"VEIN_BASE_GLOW");
		assertNotContains("fragment shader avoids saturating the entire red mask", fragmentShader,
				"color = max(color");
		assertNotContains("fragment shader avoids using the broad fault mask as the flow mask", fragmentShader,
				"smoothstep(0.18, 0.78, fault)");
		assertNotContains("fragment shader avoids old fast web drift", fragmentShader,
				"GameTime * 0.85");
		assertContains("fragment shader has red blood-memory lines", fragmentShader,
				"redMemory");
		assertContains("fragment shader reacts to apotheos burden", fragmentShader,
				"Burden");
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

	private static void assertNotContains(String label, String text, String forbidden) {
		if (text.contains(forbidden)) {
			throw new AssertionError(label + ": contains " + forbidden);
		}
	}
}
