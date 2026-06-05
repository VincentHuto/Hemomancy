package com.vincenthuto.hemomancy.client.render.world;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SanctumBoundaryRendererSourceTest {
	private static final Path RENDERER = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/world/SanctumBoundaryRenderer.java");
	private static final Path SHADER_INIT = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/init/ShaderInit.java");
	private static final Path CLIENT_CONFIG = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/config/HemoClientConfig.java");

	private SanctumBoundaryRendererSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String renderer = Files.readString(RENDERER).replace("\r\n", "\n");
		String shaderInit = Files.readString(SHADER_INIT).replace("\r\n", "\n");
		String clientConfig = Files.readString(CLIENT_CONFIG).replace("\r\n", "\n");

		assertContains("renderer respects client toggle", renderer, "sanctumBoundaryRendererEnabled()");
		assertContains("client config defines sanctum border toggle", clientConfig, "RENDER_SANCTUM_BOUNDARY");
		assertContains("client config defaults sanctum border toggle on", clientConfig,
				"define(\"renderSanctumBoundary\", true)");
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
		assertNotContains("renderer no longer applies outside post shader", renderer, "SANCTUM_BOUNDARY_DISTORTION");
		assertContains("renderer draws member shimmer", renderer, "drawMemberShimmer");
		assertContains("inside fullscreen check uses dome height", renderer, "double dy = Math.max(0.0D");
		assertContains("inside fullscreen check uses dome volume", renderer,
				"dx * dx + dy * dy + dz * dz <= radius * radius");
		assertContains("renderer avoids unnecessary frame copies", renderer, "requiresPostPass");
		assertNotContains("shader init no longer registers outside post dome shader", shaderInit,
				"SANCTUM_BOUNDARY_DISTORTION");
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
