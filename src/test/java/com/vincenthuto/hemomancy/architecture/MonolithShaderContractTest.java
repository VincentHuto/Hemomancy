package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.ResourceReader;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The Monolith's shaders. The fissure shader carries a Fill uniform: how far up the halves the light in the cracks has climbed. A
 * shut husk's cracks are dark; blood projected into it lights them from the base up; the open eye keeps them lit.
 */
class MonolithShaderContractTest {
	@Test
	void theFissureShaderLightsItsCracksByFill() {
		String fragment = ResourceReader.text("assets/hemomancy/shaders/core/world/monolith_fissure.fsh");
		assertTrue(fragment.contains("uniform float Fill;"), "the fragment shader declares Fill");
		assertTrue(fragment.contains("texCoord0.y"), "and reads the slab height the light climbs");
		String program = ResourceReader.text("assets/hemomancy/shaders/core/world/monolith_fissure.json");
		assertTrue(program.contains("\"name\": \"Fill\""), "the program declares Fill");
		String holders = SourceAssertions.readMainJava("client/render/shader/ShaderInit.java");
		assertTrue(holders.contains("\"Overgrown\", \"Fill\""), "the holder lists Fill");
		String types = SourceAssertions.readMainJava("client/render/HemoRenderTypes.java");
		assertTrue(types.contains("setUniform(shader, \"Fill\", u.fill());"), "the render type sets it from its uniforms record");
		String renderer = SourceAssertions.readMainJava("client/render/tile/harbinger/functional/SanguineMonolithRenderer.java");
		assertTrue(renderer.contains("te.glow(partialTicks)"), "the renderer fills the cracks from the husk's glow");
	}

	@Test
	void theVeinsRideTheStonesOwnSurface() {
		// The stone's surface shader breathes its vertices in and out; the tendrils drawn over it must move the same way
		// or they hover. So the veins have their own vertex stage with the surface's morph, word for word, and the
		// quads sit a hair off the stone instead of a visible gap.
		String program = ResourceReader.text("assets/hemomancy/shaders/core/world/monolith_veins.json");
		assertTrue(program.contains("\"vertex\": \"hemomancy:world/monolith_veins\""), "the veins have their own vertex stage");
		String veins = ResourceReader.text("assets/hemomancy/shaders/core/world/monolith_veins.vsh");
		String surface = ResourceReader.text("assets/hemomancy/shaders/core/item/monolith_fragment.vsh");
		for (String line : List.of("p.y * 10.0", "+ p.x * 15.0", "- p.z * 13.0", "smoothstep(0.0, 0.16, centerDistance)",
				"p.xz += normalize(fromCenter) * morph", "sin((p.x - p.z) * 12.0")) {
			assertTrue(surface.contains(line), "the surface morph still reads: " + line);
			assertTrue(veins.contains(line), "and the veins morph the same way: " + line);
		}
		assertTrue(veins.contains("uniform float MorphTime;") && veins.contains("uniform float MorphStrength;"),
				"the veins take the surface's clock and strength");
		String holders = SourceAssertions.readMainJava("client/render/shader/ShaderInit.java");
		assertTrue(holders.contains("\"Zone\", \"Tint\", \"MorphTime\", \"MorphStrength\""), "the holder lists them");
		String renderer = SourceAssertions.readMainJava("client/render/tile/harbinger/functional/SanguineMonolithRenderer.java");
		assertTrue(renderer.contains("float x = VEIN_OVERLAY_X;") && renderer.contains("float z = VEIN_OVERLAY_Z;"),
				"the vein quads have their own, closer, offsets");
		assertTrue(renderer.contains("surfaceMorphStrength()"), "and hand the shader the strength the surface really uses");
	}

	@Test
	void theEyesAndTheBackSymbolRideTheSurfaceToo() {
		// The three eyes and the back symbol are colour-only geometry; they get an emblem shader whose vertex stage
		// carries the same morph, and sit the same hair off the slab as the veins.
		String program = ResourceReader.text("assets/hemomancy/shaders/core/world/monolith_emblem.json");
		assertTrue(program.contains("\"vertex\": \"hemomancy:world/monolith_emblem\""), "the emblems have their own vertex stage");
		String emblem = ResourceReader.text("assets/hemomancy/shaders/core/world/monolith_emblem.vsh");
		for (String line : List.of("p.y * 10.0", "+ p.x * 15.0", "- p.z * 13.0", "smoothstep(0.0, 0.16, centerDistance)",
				"p.xz += normalize(fromCenter) * morph", "sin((p.x - p.z) * 12.0")) {
			assertTrue(emblem.contains(line), "the emblems morph the same way: " + line);
		}
		// A slab face is morphed at its four corners and interpolated across; an emblem morphed at its own small
		// quads would differ from the face under it by up to twice the morph, and the stone would show through.
		assertTrue(emblem.contains("uniform vec3 FaceOrigin;") && emblem.contains("uniform vec3 FaceRight;") && emblem.contains("uniform vec3 FaceUp;"),
				"the emblem is told the face it lies on");
		// A quad is two triangles on the GPU, split on its first-to-third diagonal, so the face's surface is linear per
		// triangle, not bilinear: the emblem interpolates the same way, told which diagonal its face splits on.
		assertTrue(emblem.contains("uniform float FaceDiagonal;"), "told which diagonal the face splits on");
		assertTrue(emblem.contains("u >= v") && emblem.contains("u + v <= 1.0"), "and interpolates within the right triangle");
		assertFalse(emblem.contains("mix(mix(morphAt(a), morphAt(b), u), mix(morphAt(d), morphAt(c), u), v)"), "never bilinearly");
		String renderer2 = SourceAssertions.readMainJava("client/render/tile/harbinger/functional/SanguineMonolithRenderer.java");
		assertTrue(renderer2.contains("FRONT_FACE_DIAGONAL") && renderer2.contains("BACK_FACE_DIAGONAL"), "the renderer names each face's split");
		// The back face's type is keyed apart from the front's; that key once stood in for the seed, and the back emblem
		// breathed half a turn out of step with the stone.
		String types = SourceAssertions.readMainJava("client/render/HemoRenderTypes.java");
		assertTrue(types.contains("setUniform(shader, \"StoneSeed\", u.stoneSeed());"), "the morph's seed comes from the uniforms, never the type's key");
		String holders = SourceAssertions.readMainJava("client/render/shader/ShaderInit.java");
		assertTrue(holders.contains("MONOLITH_EMBLEM = new ShaderHolder(Hemomancy.rloc(\"world/monolith_emblem\")"), "the holder exists");
		assertTrue(holders.contains("\"MorphStrength\", \"FaceOrigin\", \"FaceRight\", \"FaceUp\", \"FaceDiagonal\""), "and lists the face");
		String renderer = SourceAssertions.readMainJava("client/render/tile/harbinger/functional/SanguineMonolithRenderer.java");
		assertFalse(renderer.contains("RenderTypeInit.RADIANT_RENDER_TYPE"), "neither emblem is drawn with the flat radiant type any more");
		assertTrue(renderer.contains("float cz = EMBLEM_FRONT_Z;") && renderer.contains("float cz = EMBLEM_BACK_Z;"),
				"the emblems sit a hair off the slab, front and back");
	}
}
