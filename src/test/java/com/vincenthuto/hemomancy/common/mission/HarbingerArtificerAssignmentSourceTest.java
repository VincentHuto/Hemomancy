package com.vincenthuto.hemomancy.common.mission;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class HarbingerArtificerAssignmentSourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");

	private HarbingerArtificerAssignmentSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		artificerMilestonesExist();
		artificerMilestonesAreGrantedByRites();
		assignmentLedgerCarriesArtificerProgress();
		artificerDialogueClaimsRewardsAndFittings();
		artificerAdvancementsAndDocsExist();
	}

	private static void artificerMilestonesExist() throws IOException {
		String advancementGranter = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/event/HarbingerAdvancementGranter.java"));
		String helper = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/mission/artificer/ArtificerAssignments.java"));
		String armorSets = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/armor/ArmorSetHelper.java"));

		assertContains("Artificer tracks armature placement", advancementGranter,
				"ADV_ARTIFICER_ARMATURE_PLACED");
		assertContains("Artificer tracks first Hematic Iron upgrade", advancementGranter,
				"ADV_ARTIFICER_FIRST_HEMATIC_UPGRADE");
		assertContains("Artificer tracks Hematic Iron fitting", advancementGranter,
				"ADV_ARTIFICER_HEMATIC_IRON_FITTING");
		assertContains("Artificer tracks first fork upgrade", advancementGranter,
				"ADV_ARTIFICER_FIRST_FORK_UPGRADE");
		assertContains("Artificer tracks Barbed fitting", advancementGranter,
				"ADV_ARTIFICER_BARBED_FITTING");
		assertContains("Artificer tracks Chitinite fitting", advancementGranter,
				"ADV_ARTIFICER_CHITINITE_FITTING");
		assertContains("Artificer tracks Prismatic fitting", advancementGranter,
				"ADV_ARTIFICER_PRISMATIC_FITTING");
		assertContains("Artificer tracks frame consecration", advancementGranter,
				"ADV_ARTIFICER_FRAME_CONSECRATED");
		assertContains("Artificer tracks first Blood Lust upgrade", advancementGranter,
				"ADV_ARTIFICER_FIRST_BLOOD_LUST_UPGRADE");
		assertContains("Artificer tracks Blood Lust fitting", advancementGranter,
				"ADV_ARTIFICER_BLOOD_LUST_FITTING");
		assertContains("Artificer tracks monolithic frame", advancementGranter,
				"ADV_ARTIFICER_MONOLITHIC_FRAME");
		assertContains("Artificer tracks first D7 upgrade", advancementGranter,
				"ADV_ARTIFICER_FIRST_D7_UPGRADE");
		assertContains("Artificer tracks D7 fitting", advancementGranter,
				"ADV_ARTIFICER_D7_FITTING");
		assertContains("Artificer tracks first living graft", advancementGranter,
				"ADV_ARTIFICER_FIRST_LIVING_GRAFT");
		assertContains("Artificer tracks full living arsenal fitting", advancementGranter,
				"ADV_ARTIFICER_LIVING_ARSENAL_FITTING");
		assertContains("Artificer helper responds to armature placement", helper, "onArmaturePlaced");
		assertContains("Artificer helper responds to armature upgrades", helper, "onArmatureUpgrade");
		assertContains("Artificer helper responds to armature tier applications", helper,
				"onArmatureTierApplied");
		assertContains("Artificer helper responds to graft completion", helper,
				"onLivingWeaponGraftComplete");
		assertContains("Artificer helper counts known living weapon forms", helper,
				"knownLivingWeaponFormCount");
		assertContains("Armor set helper validates full Hematic Iron set", armorSets,
				"hasFullHematicIronSet");
		assertContains("Armor set helper validates full fork set", armorSets, "hasFullForkSet");
		assertContains("Armor set helper validates full Blood Lust set", armorSets,
				"hasFullBloodLustSet");
		assertContains("Armor set helper validates full D7 set", armorSets, "hasFullD7Set");
	}

	private static void artificerMilestonesAreGrantedByRites() throws IOException {
		String block = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/block/harbinger/crafting/HematicArmatureBlock.java"));
		String blockEntity = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/tile/crafting/HematicArmatureBlockEntity.java"));
		String graftRite = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/item/harbinger/memories/LivingWeaponGraftRite.java"));

		assertContains("Armature placement grants Artificer placement milestone", block,
				"ArtificerAssignments.onArmaturePlaced");
		assertContains("Armature upgrade success grants Artificer upgrade milestones", blockEntity,
				"ArtificerAssignments.onArmatureUpgrade");
		assertContains("Armature tier item success grants Artificer tier milestones", blockEntity,
				"ArtificerAssignments.onArmatureTierApplied");
		assertContains("Graft rite success grants Artificer graft milestone", graftRite,
				"ArtificerAssignments.onLivingWeaponGraftComplete");
	}

	private static void assignmentLedgerCarriesArtificerProgress() throws IOException {
		String ledgerItem = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/item/harbinger/HarbingerAssignmentLedgerItem.java"));
		String packet = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/network/mission/OpenHarbingerAssignmentLedgerPacket.java"));
		String screen = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/screen/item/HarbingerAssignmentLedgerScreen.java"));
		String language = read(RESOURCE_ROOT.resolve("assets/hemomancy/lang/en_us.json"));

		for (String field : new String[] {
				"artificerArmaturePlaced",
				"artificerFirstHematicUpgrade",
				"artificerHematicIronFitting",
				"artificerFirstForkUpgrade",
				"artificerForkFitting",
				"artificerFrameConsecrated",
				"artificerFirstBloodLustUpgrade",
				"artificerBloodLustFitting",
				"artificerMonolithicFrame",
				"artificerFirstD7Upgrade",
				"artificerD7Fitting",
				"artificerFirstLivingGraft",
				"artificerLivingWeaponFormCount",
				"artificerLivingArsenalFitting"
		}) {
			assertContains("ledger item carries " + field, ledgerItem, field);
			assertContains("ledger packet carries " + field, packet, field);
			assertContains("ledger screen reads " + field, screen, field);
		}

		assertContains("ledger renders The Worn Vow", screen, "renderTheWornVow");
		assertContains("ledger renders The Three Answers", screen, "renderTheThreeAnswers");
		assertContains("ledger renders Crimson Vestment", screen, "renderCrimsonVestment");
		assertContains("ledger renders Weight of the Frame", screen, "renderWeightOfTheFrame");
		assertContains("ledger renders The Assumed Limb", screen, "renderTheAssumedLimb");
		assertContains("ledger language names The Worn Vow", language,
				"screen.hemomancy.harbinger_assignment_ledger.the_worn_vow.title");
		assertContains("ledger language names The Three Answers", language,
				"screen.hemomancy.harbinger_assignment_ledger.the_three_answers.title");
		assertContains("ledger language names Crimson Vestment", language,
				"screen.hemomancy.harbinger_assignment_ledger.crimson_vestment.title");
		assertContains("ledger language names Weight of the Frame", language,
				"screen.hemomancy.harbinger_assignment_ledger.weight_of_the_frame.title");
		assertContains("ledger language names The Assumed Limb", language,
				"screen.hemomancy.harbinger_assignment_ledger.the_assumed_limb.side_title");
	}

	private static void artificerDialogueClaimsRewardsAndFittings() throws IOException {
		String dialogue = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/entity/npc/dialogue/HarbingerArtificerDialogueTrees.java"));
		String eventHandler = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/entity/npc/dialogue/DialogueEventHandler.java"));

		for (String event : new String[] {
				"EVENT_CLAIM_WORN_VOW_REWARD",
				"EVENT_CLAIM_THREE_ANSWERS_REWARD",
				"EVENT_CLAIM_CRIMSON_VESTMENT_REWARD",
				"EVENT_CLAIM_ASSUMED_LIMB_REWARD",
				"EVENT_CLAIM_HEMATIC_IRON_FITTING",
				"EVENT_CLAIM_FORK_FITTING",
				"EVENT_CLAIM_BLOOD_LUST_FITTING",
				"EVENT_CLAIM_D7_FITTING",
				"EVENT_CLAIM_LIVING_ARSENAL_FITTING"
		}) {
			assertContains("Artificer dialogue exposes " + event, dialogue, event);
			assertContains("Dialogue event handler handles " + event, eventHandler, event);
		}

		assertContains("Artificer dialogue has assignment branch", dialogue, "\"assignments\"");
		assertContains("full set fittings check worn armor through helper", eventHandler,
				"ArtificerAssignments.tryGrant");
		assertContains("Artificer fitting claims can reissue physical fittings", eventHandler,
				"handleArtificerFittingClaim");
		assertContains("Artificer fitting claims check for inventory/equipment copies", eventHandler,
				"playerHasFitting");
		assertDoesNotContain("Artificer code no longer exposes Hematic Iron seal events", dialogue,
				"EVENT_CLAIM_HEMATIC_IRON_SEAL");
		assertDoesNotContain("Artificer handler no longer handles seal claims", eventHandler,
				"handleArtificerSealClaim");
	}

	private static void artificerAdvancementsAndDocsExist() throws IOException {
		String docs = read(Path.of("docs/HEMOMANCY_REFERENCE.md"));
		String lore = read(Path.of("docs/LORE_REFERENCE.md"));

		for (String advancement : new String[] {
				"artificer_armature_placed",
				"artificer_first_hematic_upgrade",
				"artificer_hematic_iron_fitting",
				"artificer_first_fork_upgrade",
				"artificer_barbed_fitting",
				"artificer_chitinite_fitting",
				"artificer_prismatic_fitting",
				"artificer_frame_consecrated",
				"artificer_first_blood_lust_upgrade",
				"artificer_blood_lust_fitting",
				"artificer_monolithic_frame",
				"artificer_first_d7_upgrade",
				"artificer_d7_fitting",
				"artificer_first_living_graft",
				"artificer_living_arsenal_fitting"
		}) {
			assertContains("Artificer advancement " + advancement + " exists", read(RESOURCE_ROOT.resolve(
					"data/hemomancy/advancement/hemomancy/" + advancement + ".json")),
					"advancements.hemomancy." + advancement + ".title");
		}

		assertContains("reference docs describe The Worn Vow", docs, "**The Worn Vow** (D2)");
		assertContains("reference docs describe The Three Answers", docs,
				"**The Three Answers** (D3)");
		assertContains("reference docs describe Crimson Vestment", docs,
				"**Crimson Vestment** (D5)");
		assertContains("reference docs describe Weight of the Frame", docs,
				"**Weight of the Frame** (D7)");
		assertContains("reference docs describe The Assumed Limb", docs,
				"**The Assumed Limb** (D5)");
		assertContains("lore docs preserve Artificer rite tone", lore,
				"staff fittings mark witnessed obligations rather than new powers");
	}

	private static String read(Path path) throws IOException {
		if (!Files.exists(path)) {
			throw new AssertionError("missing " + path);
		}
		return Files.readString(path).replace("\r\n", "\n");
	}

	private static void assertContains(String label, String text, String needle) {
		if (!text.contains(needle)) {
			throw new AssertionError(label + " missing: " + needle);
		}
	}

	private static void assertDoesNotContain(String label, String text, String needle) {
		if (text.contains(needle)) {
			throw new AssertionError(label + " still contains: " + needle);
		}
	}
}
