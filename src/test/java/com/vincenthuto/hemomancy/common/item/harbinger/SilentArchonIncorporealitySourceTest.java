package com.vincenthuto.hemomancy.common.item.harbinger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SilentArchonIncorporealitySourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");
	private static final Path DOCS_ROOT = Path.of("docs");

	private SilentArchonIncorporealitySourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String registry = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/armor/ability/ArmorSetAbilityRegistry.java"));
		String handler = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/armor/ability/SilentArchonArmorAbilityHandler.java"));
		String docs = read(DOCS_ROOT.resolve("HEMOMANCY_REFERENCE.md"));

		assertContains("silent archon combat ability id", registry, "silent_archon_severance");
		assertContains("silent archon combat display", registry, "Silent Severance");
		assertContains("silent archon helmet requirement", registry, "ItemInit.silent_archon_helm");
		assertContains("silent archon chest requirement", registry, "ItemInit.silent_archon_chestplate");
		assertContains("silent archon leg requirement", registry, "ItemInit.silent_archon_leggings");
		assertContains("silent archon boot requirement", registry, "ItemInit.silent_archon_boots");
		assertContains("silent severance callback", registry, "SilentArchonArmorAbilityHandler::activateSilentArchonSeverance");
		assertDoesNotContain("silent slipping is no longer the radial display", registry, "Component.literal(\"Silent Slipping\")");
		assertDoesNotContain("silent slipping is no longer the radial callback", registry,
				"SilentArchonArmorAbilityHandler::activateSilentSlipping");

		assertContains("activation entry point", handler, "activateSilentSlipping");
		assertContains("double-tap server entry point", handler, "tryToggleSilentSlipping");
		assertContains("combat entry point", handler, "activateSilentArchonSeverance");
		assertContains("active state query", handler, "isSilentSlippingActive");
		assertContains("tick update", handler, "updateSilentSlipping");
		assertContains("incoming physical defense", handler, "negateIncomingPhysicalDamage");
		assertContains("incoming damage hook checks silent archon player", handler,
				"event.getEntity() instanceof ServerPlayer player");
		assertContains("incoming damage hook cancels archon physical hits", handler,
				"negateIncomingPhysicalDamage(player, event)");
		assertContains("early physical defense overload", handler,
				"negateIncomingPhysicalDamage(ServerPlayer player, LivingIncomingDamageEvent event)");
		assertContains("early physical defense cancels hit", handler,
				"event.setCanceled(true)");
		assertContains("early physical defense clears incoming amount", handler,
				"event.setAmount(0.0F)");
		assertContains("outgoing physical restriction", handler, "suppressMundanePhysicalOffense");
		assertContains("physical damage classifier", handler, "isPhysicalDamage");
		assertContains("living weapon allowance", handler, "isLivingWeapon");
		assertContains("blood manipulation projectile allowance", handler, "isHemomancyProjectile");
		assertContains("no physics state", handler, "player.noPhysics = true");
		assertContains("safe exit handling", handler, "findNearestSafePosition");
		assertContains("flight granted during slipping", handler, "player.getAbilities().mayfly = true");

		assertContains("docs mention incorporeality", docs, "incorporeal");
		assertContains("docs mention silent slipping", docs, "Silent Slipping");
		assertContains("docs mention silent severance", docs, "Silent Severance");
		assertContains("docs mention double-tap jump", docs, "double-tap jump");
		assertContains("docs mention living weapons", docs, "living weapons");
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
