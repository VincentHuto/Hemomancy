package com.vincenthuto.hemomancy.common.capability.player.harbinger.scar;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ScarStateSyncSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private ScarStateSyncSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String events = read("src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/scar/ScarStateSyncEvents.java");

		assertContains("scar sync event subscriber", events, "@EventBusSubscriber(modid = Hemomancy.MOD_ID)");
		assertContains("sync packet used", events, "new PacketSyncScarsState(source, scars)");
		assertContains("self and tracking sync used", events, "PacketDistributor.sendToPlayersTrackingEntityAndSelf");
		assertContains("tracking player sync used", events, "PacketDistributor.sendToPlayer(receiver");
		assertContains("login hook", events, "PlayerEvent.PlayerLoggedInEvent");
		assertContains("dimension hook", events, "PlayerEvent.PlayerChangedDimensionEvent");
		assertContains("respawn hook", events, "PlayerEvent.PlayerRespawnEvent");
		assertContains("start tracking hook", events, "PlayerEvent.StartTracking");
	}

	private static String read(String path) throws IOException {
		Path fullPath = ROOT.resolve(path);
		if (!Files.exists(fullPath)) {
			throw new AssertionError("missing " + fullPath);
		}
		return Files.readString(fullPath).replace("\r\n", "\n");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}
}
