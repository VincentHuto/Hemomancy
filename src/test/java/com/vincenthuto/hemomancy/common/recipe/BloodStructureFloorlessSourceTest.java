package com.vincenthuto.hemomancy.common.recipe;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/** Brazier offerings belong to Cardinal Rites; floorless Blood Structures carry none. */
final class BloodStructureFloorlessSourceTest {
	private static final Path ROOT = Path.of("src/main/java/com/vincenthuto/hemomancy");

	@Test
	void structureOfferingTypesAndPacketAreGone() {
		assertFalse(Files.exists(ROOT.resolve("common/recipe/BloodStructureOffering.java")));
		assertFalse(Files.exists(ROOT.resolve("common/recipe/BloodStructureOfferingPlacement.java")));
		assertFalse(Files.exists(ROOT.resolve("common/network/capa/harbinger/PacketBloodStructureOfferingBurst.java")));
		assertFalse(Files.exists(ROOT.resolve("client/data/ActiveBloodStructureOfferingBurstClientData.java")));
	}

	@Test
	void serializerRejectsOfferingsAndRecipeHasNone() throws IOException {
		String serializer = read("common/recipe/serializer/BloodStructureRecipeSerializer.java");
		assertTrue(serializer.contains("Blood Structures take no brazier offerings"));
		assertFalse(read("common/recipe/BloodStructureRecipe.java").contains("offerings"));
	}

	@Test
	void craftingHasNoBrazierOrRiteExclusiveSpecialCases() throws IOException {
		String helper = read("common/network/capa/harbinger/BloodStructureCraftingHelper.java");
		assertFalse(helper.contains("findOfferingMatch"));
		assertFalse(helper.contains("isRiteExclusive"));
		assertFalse(read("common/network/capa/harbinger/BloodCraftingKeyPressPacket.java").contains("isRiteExclusive"));
		assertFalse(read("common/event/BloodStructureFeedManager.java").contains("offering"));
	}

	@Test
	void floorlessCompletionUsesHemomancyEffects() throws IOException {
		String pending = read("common/event/PendingBloodCraftManager.java");
		assertFalse(pending.contains("levelEvent(2001"), "no vanilla block-break particles");
		assertFalse(pending.contains("ENDERMAN_SCREAM"), "no Enderman scream");
		assertTrue(pending.contains("HemoParticleData.bloodCell"));
	}

	@Test
	void puppeteerTrialsUseTheRiteOfferingBeat() throws IOException {
		String trial = read("common/rite/harbinger/PuppeteerTrialRiteController.java");
		assertFalse(trial.contains("PacketBloodStructureOfferingBurst"));
		assertTrue(trial.contains("CardinalRiteEventKind.OFFERING_DRAWN"));
	}

	private static String read(String path) throws IOException {
		return Files.readString(ROOT.resolve(path));
	}
}
