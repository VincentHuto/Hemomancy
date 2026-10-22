package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * N-5: every client-to-server payload validates what the client claims through {@code ServerboundGuards}, or is
 * listed here with the reason it needs none. A new serverbound payload therefore fails this test until its author
 * decides which.
 */
class ServerboundGuardsCoverageTest {
	private static final Map<String, String> EXEMPT = Map.ofEntries(
			Map.entry("ActivateArmorSetAbilityC2SPacket", "Acts on the sender's own worn armour; ArmorSetAbilityRegistry.tryActivate checks availability."),
			Map.entry("AntecedentEffectPacket", "The client half only records the sender's own epilogue, gated on completed research and degree."),
			Map.entry("ArborFruitInteractPacket", "Reach and line of sight follow ArborOfWillInteractionRules, which differ from block reach."),
			Map.entry("AxonalInputPacket", "AxonalTransductionManager checks the session token and clamps the direction."),
			Map.entry("BloodCraftingKeyPressPacket", "Trusts no client data: the server re-derives the structure from the world."),
			Map.entry("BloodFormationKeyPressPacket", "Acts on the sender's held sword, gated by chamber visits and Unstained progress."),
			Map.entry("BloodVolumeClientPacket", "Requests a resync of the sender's own data."),
			Map.entry("CPacketFlight", "The server re-checks every flight condition itself."),
			Map.entry("CastingChargePacket", "ManipulationChargeClock and CastingAnimationManager.charge bound the reported charge."),
			Map.entry("ChangeSelectedManipPacket", "Cycles the sender's own equipped selection."),
			Map.entry("ClairaudiographActionPacket", "Matches the open menu by container id, which is stricter than a menu-type check."),
			Map.entry("ClosePendingWhisperPacket", "Closes one of the sender's own pending whispers."),
			Map.entry("DialogueRewardChoicePacket", "Validated by DialogueOptionPacket.dispatch, which uses the guards."),
			Map.entry("EquipManipulationPacket", "Equips from the sender's own knowledge, checked against what is known and slot capacity."),
			Map.entry("GroundBloodDrawPacket", "Acts on the sender's held Living Staff; the client partial tick is clamped in F-8."),
			Map.entry("ImprintMnemonicBlueprintPacket", "Uses the sender's own knowledge and a blank blueprint from their inventory."),
			Map.entry("KnownManipulationClientPacket", "Requests a resync of the sender's own data."),
			Map.entry("KnownSummonsRequestPacket", "Requests a resync of the sender's own data."),
			Map.entry("ManipulationChargeVisualPacket", "Cast eligibility comes from BloodManipulation.isEquippedCastReady; held ticks are bounded."),
			Map.entry("OpenMorphlingJarPacket", "Opens a jar the sender carries."),
			Map.entry("PacketBloodlineMessage", "Length-capped text to the sender's own bloodline."),
			Map.entry("PacketCrossbarRadialAction", "Acts on the sender's equipped crossbar; the action text is length-capped."),
			Map.entry("PacketKickBloodlinePlayer", "Checks bloodline leadership."),
			Map.entry("PacketLedgerAction", "Checks bloodline leadership."),
			Map.entry("PacketLumpDonate", "BloodVolumeTransferRules.donationAmount accepts only finite, positive amounts."),
			Map.entry("PacketOpenTendencyView", "Opens a read-only view of the sender's own tendency."),
			Map.entry("PacketRequestPoolData", "Requests a resync of the sender's own bloodline pool."),
			Map.entry("PacketRequestSpecimenBestiary", "Requests a resync of the sender's own data."),
			Map.entry("PacketToggleSkill", "Checks the sender's own skill tree."),
			Map.entry("PacketToggleUnstainedBonus", "Toggles one of the sender's own Unstained bonuses by a fixed id."),
			Map.entry("PacketUnlockSkill", "Checks the sender's own skill tree and skill points."),
			Map.entry("PacketUpdateLivingStaffMorph", "Acts on a jar and staff the sender carries."),
			Map.entry("PacketUpdatePoolSettings", "Changes the sender's own bloodline pool settings; finite checks land in F-8."),
			Map.entry("PlaceStructurePacket", "Creative-only debug tool."),
			Map.entry("SelectEarthenVeinDestinationPacket", "EarthenVeinTravelManager checks the session nonce, phase and distance to the source vein."),
			Map.entry("ToggleEquipmentLayerVisibilityPacket", "Toggles one of the sender's own equipment layers; the slot id is checked."),
			Map.entry("ToggleGourdKeyPacket", "Toggles the gourd in the sender's own scar slot."),
			Map.entry("ToggleSilentSlippingC2SPacket", "Toggles the sender's own armour ability."),
			Map.entry("UpdateCurrentManipPacket", "Selects from the sender's own list; the index is bounds-checked."),
			Map.entry("UpdateCurrentMemoryPacket", "Selects from the sender's own known muscle memories."),
			Map.entry("UpdateSelectedStillArtPacket", "Selects from the sender's own unlocked Still Arts."),
			Map.entry("UseManipKeyPacket", "BloodManipulation.tryPerformAction holds the cast gates; the reported charge is clamped by ManipulationCastingRules."),
			Map.entry("UseStillArtKeyPacket", "StillArt.performAction holds the gates."));

	@Test
	void everyServerboundPayloadIsGuardedOrExplained() throws Exception {
		Map<String, Path> sources;
		try (Stream<Path> files = Files.walk(RepoPaths.MAIN_JAVA)) {
			sources = files.filter(path -> path.toString().endsWith(".java"))
					.collect(Collectors.toMap(path -> path.getFileName().toString().replace(".java", ""), path -> path,
							(first, second) -> first, TreeMap::new));
		}
		List<String> problems = new ArrayList<>();
		NetworkRegistrations.directions().forEach((payload, method) -> {
			if (method.equals("playToClient")) {
				return;
			}
			boolean guarded = SourceAssertions.read(sources.get(payload)).contains("ServerboundGuards.");
			boolean exempt = EXEMPT.containsKey(payload);
			if (!guarded && !exempt) {
				problems.add(payload + " neither uses ServerboundGuards nor is listed as exempt");
			} else if (guarded && exempt) {
				problems.add(payload + " now uses ServerboundGuards; remove its exemption");
			}
		});
		EXEMPT.keySet().stream()
				.filter(payload -> !"playToServer".equals(NetworkRegistrations.directions().get(payload))
						&& !"playBidirectional".equals(NetworkRegistrations.directions().get(payload)))
				.forEach(payload -> problems.add(payload + " is exempt but no longer a serverbound payload"));
		assertEquals(List.of(), problems);
	}
}
