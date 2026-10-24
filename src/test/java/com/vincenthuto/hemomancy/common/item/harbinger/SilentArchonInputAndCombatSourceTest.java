package com.vincenthuto.hemomancy.common.item.harbinger;

import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static com.vincenthuto.hemomancy.support.SourceAssertions.assertContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.assertNotContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.assertShowsEnglish;
import static com.vincenthuto.hemomancy.support.SourceAssertions.methodBody;

class SilentArchonInputAndCombatSourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");
	private static final Path DOCS_ROOT = Path.of("docs");

	@Test
	void silentArchonInputAndCombatSourceContracts() {
		String registry = SourceAssertions.read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/armor/ability/ArmorSetAbilityRegistry.java"));
		String handler = SourceAssertions.read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/armor/ability/SilentArchonArmorAbilityHandler.java"));
		String tendrils = SourceAssertions.read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/manipulation/HemomancyTendrilEffects.java"));
		String packet = SourceAssertions.read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/network/capa/harbinger/ToggleSilentSlippingC2SPacket.java"));
		String packetHandler = SourceAssertions.read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/network/PacketHandler.java"));
		String clientEvents = SourceAssertions.read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/event/ClientInputEvents.java"));
		String docs = SourceAssertions.read(DOCS_ROOT.resolve("HEMOMANCY_REFERENCE.md"));

		assertContains("registry exposes silent severance id", registry, "SILENT_ARCHON_SEVERANCE");
		assertContains("registry id path", registry, "silent_archon_severance");
		assertShowsEnglish("registry display", registry, "Silent Severance");
		assertContains("registry combat activation", registry, "activateSilentArchonSeverance");

		assertContains("handler exposes full-set check", handler, "hasFullSilentArchonSet(Player player)");
		assertContains("handler exposes slipping toggle", handler, "tryToggleSilentSlipping(ServerPlayer player)");
		assertContains("handler applies blood cost", handler, "SILENT_SLIPPING_BLOOD_COST");
		assertContains("handler applies slipping cooldown", handler, "SILENT_SLIPPING_COOLDOWN_TICKS");
		assertContains("handler combat method", handler, "activateSilentArchonSeverance(ServerPlayer player)");
		assertContains("handler combat radius", handler, "SILENT_SEVERANCE_RADIUS");
		assertContains("handler combat damage", handler, "SILENT_SEVERANCE_DAMAGE");
		assertContains("handler uses monolith burst", handler, "sendMonolithShatterBurst");
		assertContains("handler uses severance tendril burst", handler, "HemomancyTendrilEffects.silentSeverance");
		String severanceBody = methodBody(handler, "public static void activateSilentArchonSeverance(ServerPlayer player)");
		assertNotContains("silent severance no longer emits sculk soul particles", severanceBody,
				"ParticleTypes.SCULK_SOUL");
		assertNotContains("silent severance no longer emits reverse portal particles", severanceBody,
				"ParticleTypes.REVERSE_PORTAL");
		assertContains("handler applies severance effect", handler, "EffectInit.monolithic_dislocation");
		assertNotContains("handler no longer applies darkness", handler,
				"new MobEffectInstance(MobEffects.DARKNESS");
		assertNotContains("handler no longer applies weakness", handler,
				"new MobEffectInstance(MobEffects.WEAKNESS");
		assertNotContains("handler no longer applies slowness", handler,
				"new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN");
		assertNotContains("handler no longer applies blood loss", handler,
				"new MobEffectInstance(EffectInit.blood_loss");

		assertContains("tendril helper exposes silent severance burst", tendrils,
				"silentSeverance(ServerPlayer player, double radius)");
		assertContains("silent severance tendrils use qliphoth seed black core", tendrils,
				"QLIPHOTH_SEED_ROOT_CORE = 0xF0050003");
		assertContains("silent severance tendrils use qliphoth seed red glow", tendrils,
				"QLIPHOTH_SEED_ROOT_GLOW = 0xB8D10B1A");
		assertNotContains("silent severance tendrils avoid yellow root glow", tendrils, "0xA8D8B74A");
		assertContains("silent severance tendrils are radial", tendrils, "SILENT_SEVERANCE_TENDRILS");
		assertContains("silent severance tendrils use hutoslib spawner", tendrils, "TendrilEffectSpawner.spawn");

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
		assertContains("docs mention silent severance tendrils", docs, "red-black Qliphoth Seed-style tendrils");
		assertContains("docs mention double tap", docs, "double-tap jump");
	}
}
