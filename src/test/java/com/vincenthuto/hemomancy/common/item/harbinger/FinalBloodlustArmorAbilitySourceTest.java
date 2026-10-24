package com.vincenthuto.hemomancy.common.item.harbinger;

import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static com.vincenthuto.hemomancy.support.SourceAssertions.assertContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.assertNotContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.assertShowsEnglish;

class FinalBloodlustArmorAbilitySourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");

	@Test
	void finalBloodlustArmorAbilitySourceContracts() {
		String registry = SourceAssertions.read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/armor/ability/ArmorSetAbilityRegistry.java"));
		String ability = SourceAssertions.read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/armor/ability/ArmorSetAbility.java"));
		String simpleAbility = SourceAssertions.read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/armor/ability/SimpleArmorSetAbility.java"));
		String packet = SourceAssertions.read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/network/capa/harbinger/ActivateArmorSetAbilityC2SPacket.java"));
		String cooldownPacket = SourceAssertions.read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/network/capa/harbinger/SyncArmorSetAbilityCooldownS2CPacket.java"));
		String packetHandler = SourceAssertions.read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/network/PacketHandler.java"));
		String radial = SourceAssertions.read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/screen/manips/RadialChooseManipScreen.java"));
		String edaciousHandler = SourceAssertions.read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/armor/ability/EdaciousBloodburstArmorAbilityHandler.java"));
		String sheolicHandler = SourceAssertions.read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/armor/ability/SheolicBastionBloodlustArmorAbilityHandler.java"));
		String phantasmalHandler = SourceAssertions.read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/armor/ability/PhanstmalBloodlustArmorAbilityHandler.java"));

		assertContains("edacious id", registry, "edacious_bloodburst");
		assertContains("sheolic id", registry, "sheolic_bastion_stance");
		assertContains("phantasmal id", registry, "masquerade_of_the_forgotten");
		assertContains("phantasmal constant", registry, "MASQUERADE_OF_THE_FORGOTTEN");
		assertShowsEnglish("phantasmal display", registry, "Masquerade of the Forgotten");
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
		assertContains("cooldown packet carries resource id", cooldownPacket, "ResourceLocation abilityId");
		assertContains("cooldown packet carries remaining ticks", cooldownPacket, "remainingTicks");
		assertContains("cooldown packet writes client cooldown state", cooldownPacket, "handleClientCooldownSync");
		assertContains("packet registered", packetHandler, "ActivateArmorSetAbilityC2SPacket.TYPE");
		assertContains("cooldown packet registered", packetHandler, "SyncArmorSetAbilityCooldownS2CPacket.TYPE");
		assertContains("server syncs cooldown when it changes", registry, "syncCooldown(player, ability, cooldownUntil)");
		assertContains("radial queries active ability", radial, "ArmorSetAbilityRegistry.getActiveAbility");
		assertContains("radial uses item icon", radial, "ItemStackRadialMenuItem");
		assertContains("radial sends activation packet", radial, "new ActivateArmorSetAbilityC2SPacket");
		assertContains("flight passive handled", edaciousHandler, "updateEdaciousFlight");
		assertContains("bastion damage handled", sheolicHandler, "isBastionActive");
		assertContains("phantasmal reactive handled", phantasmalHandler, "triggerDistortedPresence");
		assertContains("masquerade activation handled", phantasmalHandler, "activateMasqueradeOfTheForgotten");
		assertContains("masquerade spawns echoes", phantasmalHandler, "PhantasmalEchoEntity");
		assertContains("masquerade count", phantasmalHandler, "MASQUERADE_ECHO_COUNT = 8");
		assertContains("masquerade duration", phantasmalHandler, "MASQUERADE_ECHO_DURATION_TICKS = 200");
	}
}
