package com.vincenthuto.hemomancy.common.item.harbinger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FinalBloodlustArmorAbilitySourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");

	private FinalBloodlustArmorAbilitySourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String registry = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/armor/ability/ArmorSetAbilityRegistry.java"));
		String ability = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/armor/ability/ArmorSetAbility.java"));
		String simpleAbility = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/armor/ability/SimpleArmorSetAbility.java"));
		String packet = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/network/capa/harbinger/ActivateArmorSetAbilityC2SPacket.java"));
		String packetHandler = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/network/PacketHandler.java"));
		String radial = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/screen/manips/RadialChooseManipScreen.java"));
		String handler = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/armor/ability/FinalBloodlustArmorAbilityHandler.java"));

		assertContains("edacious id", registry, "edacious_bloodburst");
		assertContains("sheolic id", registry, "sheolic_bastion_stance");
		assertContains("phantasmal id", registry, "masquerade_of_the_forgotten");
		assertContains("phantasmal constant", registry, "MASQUERADE_OF_THE_FORGOTTEN");
		assertContains("phantasmal display", registry, "Masquerade of the Forgotten");
		assertContains("armor ability exposes tooltip lines", ability, "List<Component> tooltip()");
		assertContains("simple ability stores tooltip lines separately from display icon", simpleAbility, "tooltip");
		assertContains("edacious tooltip describes the power", registry, "ability.hemomancy.edacious_bloodburst.tooltip");
		assertContains("sheolic tooltip describes the power", registry, "ability.hemomancy.sheolic_bastion_stance.tooltip");
		assertContains("phantasmal tooltip describes the power", registry, "ability.hemomancy.masquerade_of_the_forgotten.tooltip");
		assertContains("silent archon tooltip describes the power", registry, "ability.hemomancy.silent_archon_severance.tooltip");
		assertNotContains("phantasmal radial should not be duplicate teleport", registry, "phantasmal_step");
		assertContains("edacious full set", registry, "ItemInit.edacious_blood_lust_helm");
		assertContains("sheolic full set", registry, "ItemInit.sheolic_blood_lust_helm");
		assertContains("phantasmal full set", registry, "ItemInit.phantasmal_blood_lust_helm");
		assertContains("server rejects dead players", registry, "!player.isAlive()");
		assertContains("packet carries resource id", packet, "ResourceLocation abilityId");
		assertContains("packet validates via registry", packet, "ArmorSetAbilityRegistry.tryActivate");
		assertContains("packet registered", packetHandler, "ActivateArmorSetAbilityC2SPacket.TYPE");
		assertContains("radial queries active ability", radial, "ArmorSetAbilityRegistry.getActiveAbility");
		assertContains("radial uses item icon", radial, "ItemStackRadialMenuItem");
		assertContains("radial sends activation packet", radial, "new ActivateArmorSetAbilityC2SPacket");
		assertContains("flight passive handled", handler, "updateEdaciousFlight");
		assertContains("bastion damage handled", handler, "isBastionActive");
		assertContains("phantasmal reactive handled", handler, "triggerDistortedPresence");
		assertContains("masquerade activation handled", handler, "activateMasqueradeOfTheForgotten");
		assertContains("masquerade spawns echoes", handler, "PhantasmalEchoEntity");
		assertContains("masquerade count", handler, "MASQUERADE_ECHO_COUNT = 8");
		assertContains("masquerade duration", handler, "MASQUERADE_ECHO_DURATION_TICKS = 200");
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

	private static void assertNotContains(String label, String text, String unexpected) {
		if (text.contains(unexpected)) {
			throw new AssertionError(label + ": still contains " + unexpected);
		}
	}
}
