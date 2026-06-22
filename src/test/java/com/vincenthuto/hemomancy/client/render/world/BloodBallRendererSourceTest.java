package com.vincenthuto.hemomancy.client.render.world;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class BloodBallRendererSourceTest {
	private static final Path RENDERER = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/world/BloodBallRenderer.java");
	private static final Path CLIENT_CONFIG = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/config/HemoClientConfig.java");
	private static final Path CLIENT_DATA = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/data/BloodBallClientData.java");

	private BloodBallRendererSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String renderer = Files.readString(RENDERER).replace("\r\n", "\n");
		String clientConfig = Files.readString(CLIENT_CONFIG).replace("\r\n", "\n");
		String clientData = Files.readString(CLIENT_DATA).replace("\r\n", "\n");

		assertContains("client config declares blood orb renderer toggle", clientConfig,
				"RENDER_BLOOD_ORB_RENDERER");
		assertContains("client config defaults blood orb renderer toggle on", clientConfig,
				"define(\"renderBloodOrbRenderer\", true)");
		assertNotContains("blood orb renderer does not globally hide blob visuals", renderer,
				"HemoClientConfig.RENDER_BLOOD_ORB_RENDERER");
		assertContains("blood orb client data imports client config", clientData,
				"import com.vincenthuto.hemomancy.config.HemoClientConfig;");
		assertContains("blood orb data tracks sanguine blob independently", clientData,
				"boolean holdsBlob = player.getMainHandItem().getItem() instanceof SanguineBlobItem");
		assertContains("blood orb data tracks sanguine conduit independently", clientData,
				"boolean holdsConduit = player.getMainHandItem().getItem() instanceof ItemSanguineConduit");
		assertContains("blood orb data keeps blob active regardless of config", clientData,
				"if (!holdsBlob && !(holdsConduit && bloodOrbConduitRendererEnabled())) return false;");
		assertContains("blood orb data gates only the conduit renderer path", clientData,
				"bloodOrbConduitRendererEnabled()");
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
