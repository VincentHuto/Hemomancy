package com.vincenthuto.hemomancy.client.render.world;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FaneBoundaryRendererSourceTest {
	private static final Path RENDERER = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/world/FaneBoundaryRenderer.java");
	private static final Path SHADER_INIT = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/init/ShaderInit.java");
	private static final Path CLIENT_CONFIG = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/config/HemoClientConfig.java");
	private static final Path CLIENT_DATA = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/data/FaneBoundaryClientData.java");

	private FaneBoundaryRendererSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String renderer = Files.readString(RENDERER).replace("\r\n", "\n");
		String shaderInit = Files.readString(SHADER_INIT).replace("\r\n", "\n");
		String clientConfig = Files.readString(CLIENT_CONFIG).replace("\r\n", "\n");
		String clientData = Files.readString(CLIENT_DATA).replace("\r\n", "\n");

		assertContains("renderer respects client toggle", renderer, "faneBoundaryRendererEnabled()");
		assertContains("renderer respects per-player fane sight mode", renderer, "FaneBoundaryClientData.viewMode()");
		assertContains("renderer can force outsider sight intensity", renderer, "effectiveRelation");
		assertContains("renderer can force mundane outsider sight", renderer, "ViewMode.MUNDANE");
		assertContains("renderer hidden mode disables all fane visuals", renderer, "ViewMode.HIDDEN");
		assertContains("client fane data stores view mode locally", clientData, "private static ViewMode viewMode");
		assertContains("client fane data cycles fane sight modes", clientData, "cycleViewMode()");
		assertContains("client fane data offers mundane outsider view", clientData, "MUNDANE(\"Fane Sight: Mundane\")");
		assertContains("client fane data offers revealed outsider view", clientData, "REVEALED(\"Fane Sight: Revealed\")");
		assertContains("client config defines fane border toggle", clientConfig, "RENDER_FANE_BOUNDARY");
		assertContains("client config defaults fane border toggle on", clientConfig,
				"define(\"renderFaneBoundary\", true)");
		assertContains("renderer copies main scene", renderer, "copyMainRenderTarget");
		assertContains("renderer keeps inside post shader", renderer, "renderFullWorldGrade");
		assertContains("renderer renders outsider dome in world", renderer, "renderHostileWorldDomes");
		assertContains("renderer uses existing world shell shader pattern", renderer, "HemoRenderTypes.loomOrbShell");
		assertContains("renderer draws hostile world dome mesh", renderer, "drawHostileDomeShell");
		assertContains("renderer flushes hostile dome shell render type", renderer, "buffer.endBatch(coreType)");
		assertContains("renderer flushes hostile dome glow render type", renderer, "buffer.endBatch(glowType)");
		assertNotContains("renderer no longer uses the old broken offscreen mask name", renderer, "maskTarget");
		assertNotContains("renderer no longer uses the old broken depth blit name", renderer, "copyMainDepthToMask");
		assertNotContains("renderer no longer uses a visibility render target", renderer, "visibilityTarget");
		assertNotContains("renderer no longer projects hostile domes to screen", renderer, "ProjectedDome");
		assertNotContains("renderer no longer renders projected hostile domes", renderer, "renderProjectedHostileDomes");
		assertNotContains("renderer no longer renders projected distortion", renderer, "renderProjectedDomeDistortion");
		assertNotContains("renderer no longer copies depth into visibility target", renderer,
				"copyMainDepthToVisibilityTarget");
		assertNotContains("renderer no longer applies outside post shader", renderer, "FANE_BOUNDARY_DISTORTION");
		assertContains("renderer draws member shimmer", renderer, "drawMemberShimmer");
		assertContains("inside fullscreen check uses full sphere height", renderer, "double dy = pos.y -");
		assertContains("inside fullscreen check uses sphere volume", renderer,
				"dx * dx + dy * dy + dz * dz <= radius * radius");
		assertContains("renderer uses full sphere latitude sweep", renderer, "SPHERE_LATITUDE_END = Math.PI");
		assertContains("renderer uses smoother sphere tessellation", renderer, "DOME_RINGS = 64");
		assertContains("member shimmer is readable but not over-opaque", renderer,
				"MEMBER_SHIMMER_ALPHA = 0.18F");
		assertContains("member shells use flat alpha to avoid latitude banding", renderer,
				"MEMBER_SHELL_ALPHA_SCALE = 0.42F");
		assertContains("mundane outsiders get their own deeper red shell", renderer,
				"MUNDANE_OUTSIDER_SHELL_RED = 0.32F");
		assertContains("mundane outsiders stay less drastic than clarity outsiders", renderer,
				"MUNDANE_OUTSIDER_DOME_ALPHA = 0.52F");
		assertContains("mundane outsiders do not receive screen omen post-pass", renderer,
				"targetRelation == FaneBoundaryRelation.MUNDANE_OUTSIDER");
		assertContains("hostile shells use flat alpha to avoid latitude banding", renderer,
				"HOSTILE_SHELL_ALPHA_SCALE = 0.70F");
		assertNotContains("renderer no longer uses per-ring member fade", renderer, "memberDomeFade");
		assertNotContains("renderer no longer uses per-ring hostile fade", renderer, "hostileDomeFade");
		assertNotContains("renderer no longer computes latitude edge fades", renderer, "edgeFade");
		assertNotContains("renderer no longer limits member shells to hemispheres", renderer, "Math.PI * 0.5D");
		assertNotContains("renderer no longer uses dome ground overrun", renderer, "HOSTILE_DOME_GROUND_OVERRUN");
		assertContains("renderer avoids unnecessary frame copies", renderer, "requiresPostPass");
		assertNotContains("shader init no longer registers outside post dome shader", shaderInit,
				"FANE_BOUNDARY_DISTORTION");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}

	private static void assertNotContains(String label, String text, String unexpected) {
		if (text.contains(unexpected)) {
			throw new AssertionError(label + ": still contains " + unexpected);
		}
	}
}
