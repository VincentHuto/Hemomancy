package com.vincenthuto.hemomancy.common.item.harbinger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SilentArchonInputAndCombatSourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");
	private static final Path DOCS_ROOT = Path.of("docs");

	private SilentArchonInputAndCombatSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String registry = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/armor/ability/ArmorSetAbilityRegistry.java"));
		String handler = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/armor/ability/SilentArchonArmorAbilityHandler.java"));
		String packet = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/network/capa/harbinger/ToggleSilentSlippingC2SPacket.java"));
		String packetHandler = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/network/PacketHandler.java"));
		String clientEvents = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/event/ClientEvents.java"));
		String docs = read(DOCS_ROOT.resolve("HEMOMANCY_REFERENCE.md"));

		assertContains("registry exposes silent severance id", registry, "SILENT_ARCHON_SEVERANCE");
		assertContains("registry id path", registry, "silent_archon_severance");
		assertContains("registry display", registry, "Silent Severance");
		assertContains("registry combat activation", registry, "activateSilentArchonSeverance");

		assertContains("handler exposes full-set check", handler, "hasFullSilentArchonSet(Player player)");
		assertContains("handler exposes slipping toggle", handler, "tryToggleSilentSlipping(ServerPlayer player)");
		assertContains("handler applies blood cost", handler, "SILENT_SLIPPING_BLOOD_COST");
		assertContains("handler applies slipping cooldown", handler, "SILENT_SLIPPING_COOLDOWN_TICKS");
		assertContains("handler combat method", handler, "activateSilentArchonSeverance(ServerPlayer player)");
		assertContains("handler combat radius", handler, "SILENT_SEVERANCE_RADIUS");
		assertContains("handler combat damage", handler, "SILENT_SEVERANCE_DAMAGE");
		assertContains("handler uses monolith burst", handler, "sendMonolithShatterBurst");
		assertContains("handler applies severance effect", handler, "EffectInit.monolithic_dislocation");
		assertDoesNotContain("handler no longer applies darkness", handler,
				"new MobEffectInstance(MobEffects.DARKNESS");
		assertDoesNotContain("handler no longer applies weakness", handler,
				"new MobEffectInstance(MobEffects.WEAKNESS");
		assertDoesNotContain("handler no longer applies slowness", handler,
				"new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN");
		assertDoesNotContain("handler no longer applies blood loss", handler,
				"new MobEffectInstance(EffectInit.blood_loss");

		assertContains("toggle packet type", packet, "ToggleSilentSlippingC2SPacket");
		assertContains("toggle packet has no fields", packet, "record ToggleSilentSlippingC2SPacket()");
		assertContains("toggle packet calls server toggle", packet, "SilentArchonArmorAbilityHandler.tryToggleSilentSlipping");
		assertContains("toggle packet registered", packetHandler, "ToggleSilentSlippingC2SPacket.TYPE");

		assertContains("client tracks previous jump state", clientEvents, "silentArchonJumpWasDown");
		assertContains("client tracks last tap", clientEvents, "lastSilentArchonJumpTapTick");
		assertContains("client has double tap window", clientEvents, "SILENT_ARCHON_DOUBLE_TAP_JUMP_WINDOW");
		assertContains("client reads vanilla jump key", clientEvents, "mc.options.keyJump.isDown()");
		assertContains("client checks no screen", clientEvents, "mc.screen == null");
		assertContains("client checks full set", clientEvents, "SilentArchonArmorAbilityHandler.hasFullSilentArchonSet");
		assertContains("client sends toggle packet", clientEvents, "new ToggleSilentSlippingC2SPacket()");

		assertContains("docs mention radial combat", docs, "radial combat ability");
		assertContains("docs mention double tap", docs, "double-tap jump");
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

	private static void assertDoesNotContain(String label, String text, String forbidden) {
		if (text.contains(forbidden)) {
			throw new AssertionError(label + ": still contains " + forbidden);
		}
	}
}
