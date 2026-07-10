package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MorphlingStage4To8SourceTest {
	private static final Path SRC = Path.of("src/main/java");
	private static final Path RES = Path.of("src/main/resources");

	private MorphlingStage4To8SourceTest() {
	}

	public static void main(String[] args) throws Exception {
		foxfireLeavesContactFlashLane();
		deadmansPurseUsesBorrowedBloodFeedBanking();
		winterShroudUsesCryptobiosisLastRite();
		bootlaceUsesProximityWebNestInsteadOfHurtCocoon();
		irontoothHasConservativeShedAndHusbandryHooks();
		hungerConfigAndStackStateAreDormantByDefault();
		wildCradleStaffAndClientPipelinesAreRetargeted();
		oldAnimalLanguageIsNotLeftInPlayerFacingMorphlingCode();
	}

	private static void foxfireLeavesContactFlashLane() throws IOException {
		String foxfire = read("com/vincenthuto/hemomancy/common/item/harbinger/morphlings/FoxfireMorphlingItem.java");
		assertContains("foxfire camouflage helper", foxfire, "FoxfireCamouflageRules");
		assertContains("foxfire tracks stillness", foxfire, "CamouflageStillSince");
		assertDoesNotContain("foxfire removed contact flash", foxfire, "ChromatophoreFlash");
		assertDoesNotContain("foxfire no attacker retaliation", foxfire, "attacker.addEffect");
	}

	private static void deadmansPurseUsesBorrowedBloodFeedBanking() throws IOException {
		String purse = read("com/vincenthuto/hemomancy/common/item/harbinger/morphlings/DeadmansPurseMorphlingItem.java");
		assertContains("borrowed reserve adapter", purse, "BorrowedBloodReserve");
		assertContains("feed banking helper", purse, "DeadmansPurseFeedBankRules");
		assertContains("kill deposits borrowed blood", purse, "BorrowedBloodReserve.deposit");
		assertDoesNotContain("no direct hit lifesteal", purse, "player.heal(healAmount)");
		assertDoesNotContain("tooltip no longer advertises life steal", purse, "Life Steal");
	}

	private static void winterShroudUsesCryptobiosisLastRite() throws IOException {
		String shroud = read("com/vincenthuto/hemomancy/common/item/harbinger/morphlings/WinterShroudMorphlingItem.java");
		String lastRite = read("com/vincenthuto/hemomancy/common/event/LastRiteHelper.java");
		assertContains("cryptobiosis id exists", lastRite, "CRYPTOBIOSIS_ID");
		assertContains("cryptobiosis arms from winter shroud", lastRite, "WinterShroudMorphlingItem");
		assertContains("cryptobiosis consumes shared rite", shroud, "LastRiteHelper.CRYPTOBIOSIS_ID");
		assertContains("winter shroud resilience helper", shroud, "WinterShroudResilienceRules");
		assertDoesNotContain("winter shroud no on-hit weakness", shroud, "onEquippedAttack");
		assertDoesNotContain("winter shroud no burrowing strike", shroud, "Burrowing Strike");
	}

	private static void bootlaceUsesProximityWebNestInsteadOfHurtCocoon() throws IOException {
		String bootlace = read("com/vincenthuto/hemomancy/common/item/harbinger/morphlings/BootlaceMorphlingItem.java");
		assertContains("proximity web nest", bootlace, "runProximityWebNest");
		assertContains("web terrain placement", bootlace, "placeTemporaryWeb");
		assertDoesNotContain("bootlace no hurt reaction override", bootlace, "onEquippedHurt");
		assertDoesNotContain("bootlace no attacker cocoon", bootlace, "source.getEntity()");
	}

	private static void irontoothHasConservativeShedAndHusbandryHooks() throws IOException {
		String irontooth = read("com/vincenthuto/hemomancy/common/item/harbinger/morphlings/IrontoothMorphlingItem.java");
		String events = read("com/vincenthuto/hemomancy/common/capability/player/harbinger/morphling/EquippedMorphlingEvents.java");
		String iface = read("com/vincenthuto/hemomancy/common/item/harbinger/morphlings/IMorphling.java");
		assertContains("block break hook interface", iface, "onEquippedBlockBreak");
		assertContains("block break event delegates", events, "onPlayerBreakBlock");
		assertContains("irontooth block break implementation", irontooth, "onEquippedBlockBreak");
		assertContains("irontooth shed helper", irontooth, "tryShedUnderground");
		assertContains("husbandry progress increments", irontooth, "addHusbandryProgress");
	}

	private static void hungerConfigAndStackStateAreDormantByDefault() throws IOException {
		String config = read("com/vincenthuto/hemomancy/config/HemoServerConfig.java");
		String morphling = read("com/vincenthuto/hemomancy/common/item/harbinger/morphlings/MorphlingItem.java");
		assertContains("hunger config field", config, "MORPHLING_HUNGER_ENABLED");
		assertContains("hunger default false", config, "define(\"hungerEnabled\", false)");
		assertContains("husbandry quota config field", config, "MORPHLING_HUSBANDRY_STAGE_QUOTA");
		assertContains("husbandry default zero", config, "defineInRange(\"husbandryStageQuota\", 0");
		assertContains("last fed stack data", morphling, "LAST_FED_GAME_TIME_KEY");
		assertContains("experience stack data", morphling, "EXPERIENCE_PROGRESS_KEY");
		assertContains("hunger rule helper", morphling, "MorphlingHungerRules");
	}

	private static void wildCradleStaffAndClientPipelinesAreRetargeted() throws IOException {
		String bestiaryState = read("com/vincenthuto/hemomancy/common/capability/player/harbinger/bestiary/SpecimenBestiaryState.java");
		String cradle = read("com/vincenthuto/hemomancy/common/tile/functional/MorphlingCradleBlockEntity.java");
		String clientRegistry = read("com/vincenthuto/hemomancy/client/morphling/MorphlingMutationRegistry.java");
		String staffModel = Files.readString(RES.resolve("assets/hemomancy/models/item/living_staff.json"));
		assertContains("bestiary sync migrates layer families", bestiaryState, "MorphlingMigrationRules.migrateLayerFamily");
		assertContains("cradle primal filter exists", cradle, "isCradleSupportedPrimalStrain");
		assertContains("cradle gravecap support", cradle, "ItemInit.morphling_gravecap");
		assertContains("client registry marks interim assets", clientRegistry, "interim strain silhouette");
		for (int i = 1; i <= 8; i++) {
			assertContains("staff predicate " + i, staffModel, "\"hemomancy:staff_visual\": " + i);
		}
	}

	private static void oldAnimalLanguageIsNotLeftInPlayerFacingMorphlingCode() throws IOException {
		String combined = read("com/vincenthuto/hemomancy/common/item/harbinger/morphlings/BootlaceMorphlingItem.java")
				+ read("com/vincenthuto/hemomancy/common/item/harbinger/morphlings/DeadmansPurseMorphlingItem.java")
				+ read("com/vincenthuto/hemomancy/common/item/harbinger/morphlings/FoxfireMorphlingItem.java")
				+ read("com/vincenthuto/hemomancy/common/item/harbinger/morphlings/IrontoothMorphlingItem.java")
				+ read("com/vincenthuto/hemomancy/common/item/harbinger/morphlings/WinterShroudMorphlingItem.java")
				+ read("com/vincenthuto/hemomancy/common/item/harbinger/morphlings/WitchsEarMorphlingItem.java");
		assertDoesNotContain("no bat comments", combined, "Bat morphling");
		assertDoesNotContain("no spider comments", combined, "Spider morphling");
		assertDoesNotContain("no mole comments", combined, "Mole morphling");
		assertDoesNotContain("no centipede comments", combined, "Centipede morphling");
		assertDoesNotContain("no leech comments", combined, "Leeches morphling");
	}

	private static String read(String relative) throws IOException {
		return Files.readString(SRC.resolve(relative));
	}

	private static void assertContains(String label, String haystack, String needle) {
		if (!haystack.contains(needle)) {
			throw new AssertionError(label + ": missing " + needle);
		}
	}

	private static void assertDoesNotContain(String label, String haystack, String needle) {
		if (haystack.contains(needle)) {
			throw new AssertionError(label + ": still contains " + needle);
		}
	}
}
