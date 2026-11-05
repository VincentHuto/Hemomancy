package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.ResourceReader;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The Lidless void is one GLSL include that both its shaders import: the door panel seen from outside and the
 * crossing view seen from inside must read as the same place. The include takes everything as parameters.
 */
class LidlessShaderContractTest {
	private static final String INCLUDE = "assets/hemomancy/shaders/include/noise/lidless_void.glsl";
	private static final String IMPORT = "#moj_import <hemomancy:noise/lidless_void.glsl>";
	private static final String MONOLITH = "#moj_import <hemomancy:noise/monolith_noise.glsl>";

	@Test
	void includeIsSelfContained() {
		String include = ResourceReader.text(INCLUDE);
		assertTrue(include.startsWith("// "), "the include says what it is for");
		assertFalse(include.contains("uniform"), "the include reads no uniform");
		assertFalse(include.contains("#moj_import"), "the include imports nothing");
		for (String function : List.of("vec3 lidlessVoid(vec3 ray, float time, float seed, float twist, float stare)",
				"vec4 lidlessEyes(vec3 ray, float time, float seed, float stare)")) {
			assertTrue(include.contains(function), function);
		}
	}

	@Test
	void viewShaderImportsTheIncludeAndDeclaresItsUniforms() {
		String fragment = ResourceReader.text("assets/hemomancy/shaders/core/screen/lidless_passage_view.fsh");
		assertTrue(fragment.contains(IMPORT), "the view imports the void");
		assertTrue(fragment.indexOf(MONOLITH) < fragment.indexOf(IMPORT), "the Monolith noise comes before the void");
		String program = ResourceReader.text("assets/hemomancy/shaders/core/screen/lidless_passage_view.json");
		for (String uniform : List.of("HemoTime", "Progress", "Length", "DoorSeed", "CamRight", "CamUp", "CamForward",
				"FovTan", "Aspect", "EyeHeight", "Mode", "Hole", "EyeShape")) {
			assertTrue(program.contains("\"" + uniform + "\""), uniform);
		}
		assertTrue(program.contains("\"Sampler0\""), "the view samples the scene");
		assertTrue(program.contains("\"Sampler1\""), "the view composites the seed mask");
		assertTrue(program.contains("\"Sampler2\"") && program.contains("\"Live\""), "and shows the Chamber in the hole on the walk in");
		assertTrue(fragment.contains("texture(Sampler2, "), "the hole reads the live view by screen position");
	}

	@Test
	void panelShaderImportsTheIncludeAndDeclaresItsUniforms() {
		String fragment = ResourceReader.text("assets/hemomancy/shaders/core/world/lidless_door_panel.fsh");
		assertTrue(fragment.contains(IMPORT), "the panel imports the void");
		assertTrue(fragment.indexOf(MONOLITH) < fragment.indexOf(IMPORT), "the Monolith noise comes before the void");
		String program = ResourceReader.text("assets/hemomancy/shaders/core/world/lidless_door_panel.json");
		for (String uniform : List.of("HemoTime", "DoorSeed", "PassageRight", "PassageUp", "PassageForward", "Diamond", "Live", "ScreenSize", "Hard")) {
			assertTrue(program.contains("\"" + uniform + "\""), uniform);
		}
		assertTrue(program.contains("\"Sampler0\""), "a live eye samples the Chamber drawn for it");
		assertTrue(fragment.contains("gl_FragCoord.xy / ScreenSize"), "by screen position, so it is a window");
	}

	@Test
	void aShutEyeLeavesNothingBehind() {
		String fragment = ResourceReader.text("assets/hemomancy/shaders/core/world/lidless_door_panel.fsh");
		assertTrue(fragment.contains("alpha *= smoothstep(0.0, 0.08, opening);"), "shut, the panel fades out completely");
		assertFalse(fragment.contains("mix(0.45, 1.0, smoothstep(0.0, 0.08, opening))"), "no faint seam is left between the halves");
	}
}
