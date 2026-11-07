package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.ResourceReader;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Velorum's presentation (2026-11-05 overhaul design, section 16): every effect is world geometry in the rime and umbra
 * materials or a fine glow mote; no vanilla particle, no hit glow, no Blindness or Nausea (the darkness is sensory, never
 * a blackout), and no "martyr" anywhere in his files (OPEN_QUESTIONS C-5, the code half). The rime geometry still hooks
 * before the shared branch, and his bar has three notches.
 */
class VelorumPresentationGuardrailTest {
	@Test
	void bespokeRimeAndShadowUseTheSharedWorldPipeline() {
		String renderer = SourceAssertions.readMainJava("client/render/world/ManipulationVisualRenderer.java");
		assertTrue(renderer.contains("VelorumRimeGeometry.collect(world, poses, FLOWS"));
		assertTrue(renderer.indexOf("if(VelorumRimeGeometry.handles") < renderer.indexOf("if(AnimusMortemGeometry.handles(packet.form())) {"));
		assertTrue(SourceAssertions.readMainJava("client/render/world/ManipulationMotes.java").contains("if(VelorumRimeGeometry.handles(form))return;"));
		assertTrue(SourceAssertions.readMainJava("client/render/shader/ShaderInit.java").contains("registerShader(event, MANIPULATION_RIME.createInstance(provider))"));
	}

	@Test
	void hisOwnPassDrawsEveryMoveInHisOwnMaterials() {
		// visual pass 2 (2026-11-06): his own pass in both of the visuals' passes, his own shaders, the kit painter declined
		String renderer = SourceAssertions.readMainJava("client/render/world/ManipulationVisualRenderer.java");
		assertTrue(renderer.contains("VelorumTelegraphPass.collect(world, poses, FLOWS, camera, right, up, time, partial, solids);"));
		String shaders = SourceAssertions.readMainJava("client/render/shader/ShaderInit.java");
		assertTrue(shaders.contains("registerShader(event, VELORUM_FROST.createInstance(provider))"));
		assertTrue(shaders.contains("registerShader(event, VELORUM_ICE.createInstance(provider))"));
		String types = SourceAssertions.readMainJava("client/render/world/VelorumRenderTypes.java");
		assertEquals(3, types.split("static final RenderType ", -1).length - 1, "three materials, each built once");
		String style = SourceAssertions.readMainJava("client/render/world/VelorumTelegraphStyle.java");
		assertTrue(style.contains("return draw.move() instanceof VelorumMove;"), "every move is his pass's");
		assertFalse(style.contains("SaintTelegraphPainter"), "the kit's flat tints never show for him");
	}

	@Test
	void noVanillaParticleNoHitGlowNoBlackoutAndNoMartyr() throws Exception {
		List<String> found = new ArrayList<>();
		for (Path file : velorumFiles()) {
			String source = SourceAssertions.read(file);
			for (String banned : List.of("ParticleTypes", "hitGlow", "MobEffects.BLINDNESS", "MobEffects.CONFUSION")) {
				if (source.contains(banned)) found.add(RepoPaths.display(file) + ": " + banned);
			}
			if (source.toLowerCase(Locale.ROOT).contains("martyr")) found.add(RepoPaths.display(file) + ": martyr");
		}
		assertEquals(List.of(), found);
	}

	@Test
	void theBarHasThreePhasesAndTheMeterPaintsTheFrostEdge() {
		String frame = SourceAssertions.readMainJava("client/screen/overlay/boss/frame/VelorumFrame.java");
		assertTrue(frame.contains("return new float[] { LAST_TENTH, HOUR_AT, CONGREGATION_AT };"));
		String meter = SourceAssertions.readMainJava("client/screen/overlay/boss/frame/VelorumMeterStyle.java");
		assertTrue(meter.contains("public void paintScreen(SaintMeterContext c)"));
		assertTrue(meter.contains("public float muffle(SaintMeterContext c, SoundInstance sound)"));
	}

	@Test
	void theGeneratorsKeepSixteenPixelsAndNoSilver() {
		String saint = SourceAssertions.read("tools/model_export/velorum.py");
		assertFalse(saint.contains("'silver'"), "no silver, brass, bronze or copper on a blood-side body");
		assertTrue(saint.contains("kit.SCALE = 1"), "16 px per block");
		assertTrue(saint.contains("'cruor'") && saint.contains("'rime_iron'"));
		String devotee = SourceAssertions.read("tools/model_export/velorum_devotee.py");
		assertTrue(devotee.contains("from court_humanoid import humanoid, check_rig"), "the jointed rig, never the vanilla humanoid");
		assertTrue(devotee.contains("check_rig(r)"));
	}

	@Test
	void hisIceIsFrozenCruorAndBlueIsOnlyFrostLight() {
		// revision 2026-11-06 (R.2): every body colour of his ice is red-dominant; blue lives only in the frost light
		Pattern constant = Pattern.compile("const vec3 (\\w+) = vec3\\(([0-9.]+), ([0-9.]+), ([0-9.]+)\\);");
		Set<String> frostLight = Set.of("HOAR", "FROST_LIGHT", "FROST_CORE", "ICE", "CORE");
		for (String shader : List.of("velorum_ice", "velorum_frost")) {
			String source = SourceAssertions.read("src/main/resources/assets/hemomancy/shaders/core/world/" + shader + ".fsh");
			Matcher m = constant.matcher(source);
			int body = 0;
			while (m.find()) {
				if (frostLight.contains(m.group(1))) continue;
				double r = Double.parseDouble(m.group(2)), g = Double.parseDouble(m.group(3)), b = Double.parseDouble(m.group(4));
				boolean dark = Math.max(r, Math.max(g, b)) < 0.2D;
				assertTrue(r > b || dark && shader.equals("velorum_frost"), shader + " " + m.group(1) + " is red-dominant or the liquid dark");
				if (r > b) body++;
			}
			assertTrue(body >= 4, shader + " carries the cruor palette");
			assertTrue(source.contains("uniform float CruorGlow;"), shader + " reads the cruor's own glow");
		}
		assertFalse(SourceAssertions.read("src/main/resources/assets/hemomancy/shaders/core/world/velorum_ice.fsh").contains("CLEAR_LIT"),
				"no clear blue-white ice");
	}

	@Test
	void hisDarkIsHisOwnAndDrawnBeforeEveryTelegraph() throws Exception {
		// R.4: Velorum never applies the obscuring school state; his gloam is his own fog
		List<String> found = new ArrayList<>();
		for (Path file : velorumFiles()) if (SourceAssertions.read(file).contains("OBSCURED")) found.add(RepoPaths.display(file));
		assertEquals(List.of(), found);
		String fog = SourceAssertions.readMainJava("client/event/VelorumGloamFog.java");
		assertTrue(fog.contains("ViewportEvent.RenderFog") && fog.contains("ViewportEvent.ComputeFogColor"));
		// R.2: the dark is batched before the telegraphs, so none is hidden by it
		String pass = SourceAssertions.readMainJava("client/render/world/VelorumTelegraphPass.java");
		int dark = pass.indexOf("new Scene(p, Phase.DARK"), floor = pass.indexOf("new Scene(p, Phase.FLOOR");
		assertTrue(dark >= 0 && floor > dark, "the dark's scene runs before the floor's");
		assertTrue(pass.substring(dark, floor).contains("buffers.endBatch(VelorumRenderTypes.FROST);"), "and its batch ends first");
		String style = SourceAssertions.readMainJava("client/render/world/VelorumTelegraphStyle.java");
		assertTrue(style.contains("VelorumDarkSight.pool(") && style.contains("VelorumDarkSight.reach("), "his pools and reaches are his pass's");
	}

	@Test
	void noBlackIceInHisLang() {
		// R.15: every piece of his ice is Frozen Cruor; "black ice" left the player text with BLACK_ICE's rename
		List<String> found = new ArrayList<>();
		for (var entry : ResourceReader.asset("lang/en_us.json").entrySet()) {
			if (!entry.getKey().contains("velorum")) continue;
			String text = (entry.getKey() + " " + entry.getValue().getAsString()).toLowerCase(Locale.ROOT);
			if (text.contains("black ice") || text.contains("black-ice") || text.contains("black_ice")) found.add(entry.getKey());
		}
		assertEquals(List.of(), found);
	}

	private static List<Path> velorumFiles() throws Exception {
		try (Stream<Path> files = Files.walk(RepoPaths.MAIN_JAVA)) {
			return files.filter(path -> path.getFileName().toString().startsWith("Velorum") && path.toString().endsWith(".java"))
					.toList();
		}
	}
}
