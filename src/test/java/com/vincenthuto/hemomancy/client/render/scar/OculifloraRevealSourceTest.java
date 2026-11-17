package com.vincenthuto.hemomancy.client.render.scar;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.vincenthuto.hemomancy.support.SourceAssertions.assertContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.assertNotContains;

public final class OculifloraRevealSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private OculifloraRevealSourceTest() {
	}

	@Test
	void allChecksPass() throws IOException {
		oculifloraExposesNetworkSightGateForWillTelegraphs();
		rendererIsClientOnlyRevealAndDoesNotAddPacketsOrTapBehavior();
	}

	private static void oculifloraExposesNetworkSightGateForWillTelegraphs() throws IOException {
		String item = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/scar/fungal/OculifloraReticularisItem.java");
		assertContains("network sight API", item, "public static boolean networkSightActive(Player player)");
		assertContains("network sight checks scar slot", item, "ItemInit.oculiflora_reticularis");
	}

	private static void rendererIsClientOnlyRevealAndDoesNotAddPacketsOrTapBehavior() throws IOException {
		String renderer = read("src/main/java/com/vincenthuto/hemomancy/client/render/scar/OculifloraRevealRenderer.java");
		String clientEvents = read("src/main/java/com/vincenthuto/hemomancy/client/render/world/WorldEffectDispatcher.java");
		String config = read("src/main/java/com/vincenthuto/hemomancy/config/HemoClientConfig.java");

		assertContains("renderer hooked from client events", clientEvents, "OculifloraRevealRenderer.render");
		assertContains("renderer checks config toggle", renderer, "HemoClientConfig.RENDER_OCULIFLORA_REVEAL");
		assertContains("renderer checks equipped scar", renderer, "networkSightActive");
		assertContains("renderer uses existing bloom sync", renderer, "QliphothBloomClientData.getActiveBlooms()");
		assertContains("renderer caps POI markers", renderer, "MAX_RENDERED_MARKERS = 16");
		assertContains("client config toggle", config, "RENDER_OCULIFLORA_REVEAL");
		assertNotContains("no new packet dependency", renderer, "PacketHandler");
		assertNotContains("no sight tap terrain drain", renderer, "drain");
		assertNotContains("no sight tap terrain drain in item", read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/scar/fungal/OculifloraReticularisItem.java"), "drain");
	}

	private static String read(String path) throws IOException {
		Path resolved = ROOT.resolve(path);
		if (!Files.exists(resolved)) {
			throw new AssertionError("missing " + resolved);
		}
		return Files.readString(resolved).replace("\r\n", "\n");
	}
}
