package com.vincenthuto.hemomancy.common.entity.npc.harbinger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class HarbingerHermitFarewellDeathSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private HarbingerHermitFarewellDeathSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String hermit = read("src/main/java/com/vincenthuto/hemomancy/common/entity/npc/harbinger/HarbingerHermitEntity.java");
		String handler = read("src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/DialogueEventHandler.java");
		String renderer = read("src/main/java/com/vincenthuto/hemomancy/client/render/entity/npc/HarbingerHermitRenderer.java");
		String renderTypes = read("src/main/java/com/vincenthuto/hemomancy/client/render/HemoRenderTypes.java");
		String shaders = read("src/main/java/com/vincenthuto/hemomancy/common/init/ShaderInit.java");
		String shaderJson = read("src/main/resources/assets/hemomancy/shaders/core/entity/hermit_farewell_dissolve.json");
		String shaderVertex = read("src/main/resources/assets/hemomancy/shaders/core/entity/hermit_farewell_dissolve.vsh");
		String shaderFragment = read("src/main/resources/assets/hemomancy/shaders/core/entity/hermit_farewell_dissolve.fsh");
		String particleInit = read("src/main/java/com/vincenthuto/hemomancy/common/init/ParticleInit.java");
		String edgeGlowParticle = read("src/main/java/com/vincenthuto/hemomancy/client/particle/HermitEdgeGlowParticle.java");
		String edgeGlowFactory = read("src/main/java/com/vincenthuto/hemomancy/client/particle/factory/HermitEdgeGlowParticleFactory.java");
		String edgeGlowData = read("src/main/java/com/vincenthuto/hemomancy/client/particle/data/HermitEdgeGlowParticleData.java");
		String edgeGlowType = read("src/main/java/com/vincenthuto/hemomancy/client/particle/type/HermitEdgeGlowParticleType.java");
		String edgeGlowJson = read("src/main/resources/assets/hemomancy/particles/hermit_edge_glow.json");
		String docs = read("docs/HEMOMANCY_REFERENCE.md");

		assertContains("hermit owns timed farewell death state", hermit, "FAREWELL_DEATH_DURATION");
		assertContains("hermit syncs farewell death ticks", hermit, "DATA_FAREWELL_DEATH_TICKS");
		assertContains("hermit exposes ritual death entrypoint", hermit, "public void beginFarewellDeath()");
		assertContains("hermit freezes during ritual death", hermit, "this.setNoAi(true)");
		assertContains("hermit removes only after ritual death duration", hermit, "this.remove(RemovalReason.KILLED)");
		assertContains("hermit uses purpose-built glow particles for dissolve edge motes", hermit, "HermitEdgeGlowParticleFactory.createData");
		assertContains("hermit colors motes with the shader edge red", hermit, "FAREWELL_EDGE_GLOW");
		assertContains("hermit uses particle colors for edge continuity", hermit, "ParticleColor");
		assertContains("hermit emits hybrid dissolve edge particles", hermit, "spawnFarewellDissolveEdgeParticles");
		assertContains("hermit approximates the shader hole field for particles", hermit, "farewellHoleField");
		assertContains("hermit tests particles near the current dissolve threshold", hermit, "isNearFarewellDissolveEdge");
		assertContains("hermit maps sampled local surface points into world space", hermit, "transformFarewellLocalToWorld");
		assertContains("hermit emits a dense but controlled field of small glow motes", hermit, "targetCount = 10 + (int) (progress * 28.0F)");
		assertContains("hermit has a dedicated edge glow spawn helper", hermit, "spawnFarewellEdgeGlowParticle");
		assertContains("hermit samples motes from the torso instead of the ground", hermit, "double localY = 0.58D + this.random.nextDouble() * 1.03D");
		assertContains("hermit defines a forward-facing particle source plane", hermit, "FAREWELL_FRONT_SURFACE_Z = -0.46D");
		assertContains("hermit samples motes from multiple local robe surfaces", hermit, "sampleFarewellDissolveSurface");
		assertContains("hermit carries local surface normals for particle drift", hermit, "FarewellSurfaceSample");
		assertContains("hermit rotates particles with body yaw like the renderer", hermit, "180.0D - this.yBodyRot");
		assertNotContains("hermit no longer anchors all motes to the old single front plane", hermit,
				"double localZ = -0.245D + (this.random.nextDouble() - 0.5D) * 0.050D");
		assertContains("hermit keeps glow motes close to the dissolve edge", hermit, "velocity.x() * 0.08D");
		assertContains("hermit prevents glow motes from fountaining upward", hermit, "0.0D, velocity.z() * 0.08D");
		assertNotContains("hermit no longer imports the generic long-lived glow factory for farewell motes", hermit,
				"com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory");
		assertNotContains("hermit no longer samples the full body cylinder for glow motes", hermit, "double angle = this.random.nextDouble() * Math.PI * 2.0D");
		assertNotContains("hermit no longer uses bulky dust particles for the dissolve edge", hermit, "DustParticleOptions");
		assertNotContains("hermit no longer mixes vanilla ash into the edge glow pass", hermit, "ParticleTypes.ASH");
		assertContains("particle init registers hermit edge glow type", particleInit, "hermit_edge_glow");
		assertContains("particle init registers hermit edge glow provider", particleInit, "HermitEdgeGlowParticleFactory::new");
		assertContains("hermit edge glow uses glow render type", edgeGlowParticle, "HLRenderTypeInit.GLOW_RENDER");
		assertContains("hermit edge glow has short lifetime", edgeGlowParticle, "this.lifetime = 12 + this.random.nextInt(7)");
		assertContains("hermit edge glow is small", edgeGlowFactory, "0.016f");
		assertContains("hermit edge glow damps horizontal drift", edgeGlowParticle, "this.xd *= 0.42D");
		assertContains("hermit edge glow damps vertical drift", edgeGlowParticle, "this.yd *= 0.22D");
		assertContains("hermit edge glow is non-physical", edgeGlowParticle, "this.hasPhysics = false");
		assertContains("hermit edge glow factory creates hermit data", edgeGlowFactory, "new HermitEdgeGlowParticleData(ParticleInit.hermit_edge_glow.get(), color)");
		assertContains("hermit edge glow data defaults to hermit particle type", edgeGlowData, "ParticleInit.hermit_edge_glow.get()");
		assertContains("hermit edge glow type wires its codec", edgeGlowType, "HermitEdgeGlowParticleData.MAP_CODEC");
		assertContains("hermit edge glow particle json uses glow sprite", edgeGlowJson, "hemomancy:hit_particle_glow");
		assertContains("dialogue starts ritual death instead of normal kill", handler, "hermit.beginFarewellDeath()");
		assertNotContains("dialogue no longer kills hermit immediately", handler, "hermit.kill()");
		assertContains("renderer draws farewell death beam flares", renderer, "renderFarewellDeathBeams");
		assertContains("renderer draws a visible chest flare for buried heart beams", renderer, "renderFarewellChestFlare");
		assertContains("renderer offsets heart light toward the front of the chest", renderer, "CHEST_FLARE_Z");
		assertContains("renderer can keep farewell death cycling for visual testing", renderer, "DEBUG_LOOP_FAREWELL_DEATH");
		assertContains("renderer drives test-cycle progress from entity age", renderer, "tickCount % HarbingerHermitEntity.FAREWELL_DEATH_DURATION");
		assertContains("renderer uses living body yaw instead of render callback yaw for beams", renderer, "getFarewellBodyYaw");
		assertContains("renderer interpolates the same body yaw vanilla model rendering uses", renderer,
				"Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot)");
		assertContains("renderer uses enough heart beams to read through dissolve", renderer, "FAREWELL_BEAM_COUNT = 9");
		assertContains("renderer keeps heart beams broad enough to read", renderer, "FAREWELL_MIN_BEAM_WIDTH");
		assertContains("renderer keeps heart beams bright enough to read", renderer, "FAREWELL_BEAM_ALPHA");
		assertContains("renderer uses dragon death ray render type for flare beams", renderer, "RenderType.dragonRays()");
		assertContains("renderer includes depth-pass dragon rays", renderer, "RenderType.dragonRaysDepth()");
		assertContains("renderer uses dissolve body render type during farewell", renderer, "HemoRenderTypes.hermitFarewellDissolve");
		assertNotContains("renderer no longer relies on uniform shrink disappearance", renderer, "scaleFarewellDyingHermit");
		assertContains("renderer reads hermit death progress", renderer, "entity.getFarewellDeathProgress(partialTick)");
		assertContains("render type wires dissolve progress uniform", renderTypes, "HermitDissolveProgress");
		assertContains("render type wires dissolve seed uniform", renderTypes, "HermitDissolveSeed");
		assertContains("shader init registers hermit dissolve shader", shaders, "HERMIT_FAREWELL_DISSOLVE");
		assertContains("shader json points at hermit dissolve shader", shaderJson, "entity/hermit_farewell_dissolve");
		assertContains("shader json accepts vanilla entity overlay sampler", shaderJson, "\"Sampler1\"");
		assertContains("shader json accepts vanilla entity normal attribute", shaderJson, "\"Normal\"");
		assertContains("shader json declares first entity light direction", shaderJson, "\"Light0_Direction\"");
		assertContains("shader json declares second entity light direction", shaderJson, "\"Light1_Direction\"");
		assertContains("vertex shader uses 1.21 fog distance signature", shaderVertex, "fog_distance(Position, FogShape)");
		assertNotContains("vertex shader avoids legacy fog distance signature", shaderVertex, "fog_distance(ModelViewMat");
		assertContains("vertex shader imports vanilla entity lighting helpers", shaderVertex, "#moj_import <light.glsl>");
		assertContains("vertex shader receives entity normals", shaderVertex, "in vec3 Normal;");
		assertContains("vertex shader preserves vanilla diffuse entity shading", shaderVertex, "minecraft_mix_light");
		assertContains("vertex shader passes light map separately", shaderVertex, "out vec4 lightMapColor;");
		assertContains("vertex shader passes overlay color separately", shaderVertex, "out vec4 overlayColor;");
		assertContains("fragment shader makes real cutout holes", shaderFragment, "discard");
		assertContains("fragment shader uses a swiss-cheese hole field", shaderFragment, "holeField");
		assertContains("fragment shader keeps a dissolve cut for body holes", shaderFragment, "cutForProgress");
		assertContains("fragment shader grows dissolve spots outward over progress", shaderFragment, "spotGrowthForProgress");
		assertContains("fragment shader roughens circles with angular wobble", shaderFragment, "angularWobble");
		assertContains("fragment shader adds torn edge noise to spot distance", shaderFragment, "tornNoise");
		assertContains("fragment shader passes progress into the hole field", shaderFragment, "holeField(texCoord0, localPosition, progress)");
		assertContains("fragment shader widens edge glow as holes grow", shaderFragment, "edgeBand = mix(0.070, 0.145");
		assertContains("fragment shader keeps red edge glow on the body", shaderFragment, "dissolveColor += vec3(0.92, 0.025, 0.014)");
		assertContains("fragment shader leaves ash motion to real client particles", shaderFragment, "bodyVisible < 0.5");
		assertNotContains("fragment shader no longer renders shader-native ash grains", shaderFragment, "edgeAshGrain");
		assertNotContains("fragment shader no longer owns ash flow grid", shaderFragment, "ASH_FLOW_GRID_SCALE");
		assertNotContains("fragment shader no longer owns ash trail segments", shaderFragment, "ashFlowTrail");
		assertNotContains("fragment shader no longer samples ash source colors", shaderFragment, "sampleAshSourceColor");
		assertNotContains("fragment shader no longer computes ash spawn timing", shaderFragment, "edgeSpawnProgress");
		assertNotContains("fragment shader no longer carries ash source uv", shaderFragment, "ashSourceUv");
		assertNotContains("fragment shader no longer carries shader ash alpha", shaderFragment, "ashAlpha");
		assertNotContains("fragment shader no longer mixes visible ash flecks", shaderFragment, "visibleAshFleck");
		assertNotContains("fragment shader avoids dense fuzzy grain grid", shaderFragment, "vec2(42.0, 38.0)");
		assertNotContains("fragment shader avoids overactive fuzz scatter", shaderFragment, "smoothstep(0.12, 0.52");
		assertNotContains("fragment shader avoids circular visible ash blobs", shaderFragment, "length(fleckLocal - fleckCenter)");
		assertNotContains("fragment shader avoids static rectangular ash patches", shaderFragment, "rectangularAshPatch");
		assertNotContains("fragment shader avoids whole-patch ash lifetimes", shaderFragment, "ashPatchLife");
		assertNotContains("fragment shader avoids random flat ash coloring", shaderFragment, "vec3 edgeAshColor = mix(");
		assertNotContains("fragment shader avoids tiny static-scale grain", shaderFragment, "texCoord0 * 96.0");
		assertNotContains("fragment shader avoids over-dark ash muting", shaderFragment, "ash * 0.66");
		assertContains("fragment shader applies vanilla hurt/overlay color", shaderFragment, "mix(overlayColor.rgb, color.rgb, overlayColor.a)");
		assertContains("fragment shader applies vanilla lightmap shading", shaderFragment, "color *= lightMapColor");
		assertContains("docs describe farewell animation", docs, "ritual farewell death animation");
	}

	private static String read(String path) throws IOException {
		Path file = ROOT.resolve(path);
		if (!Files.exists(file)) {
			throw new AssertionError("missing expected source file '" + path + "'");
		}
		return Files.readString(file);
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + " (missing '" + expected + "')");
		}
	}

	private static void assertNotContains(String label, String text, String unexpected) {
		if (text.contains(unexpected)) {
			throw new AssertionError(label + " (unexpected '" + unexpected + "')");
		}
	}
}
