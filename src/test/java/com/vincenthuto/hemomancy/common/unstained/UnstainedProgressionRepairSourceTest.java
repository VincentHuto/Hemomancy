package com.vincenthuto.hemomancy.common.unstained;

import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedProgress;
import com.vincenthuto.hemomancy.common.mission.UnstainedObservanceHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class UnstainedProgressionRepairSourceTest {
	private UnstainedProgressionRepairSourceTest() {}

	public static void main(String[] args) throws IOException {
		observanceStateRoundTripsAndUnlocksByStage();
		String itemInit = read("src/main/java/com/vincenthuto/hemomancy/common/init/ItemInit.java");
		assertContains(itemInit, "lethean_chalice = SPECIALITEMS.register(\"lethean_chalice\"");
		assertNotContains(itemInit, "\n    public static final DeferredHolder<Item, Item> pale_silver_pickaxe");
		assertNotContains(itemInit, "\n    public static final DeferredHolder<Item, Item> verdigris_censer");

		String glassLungs = read("src/main/resources/data/hemomancy/recipe/cardinal_rite/glass_lungs.json");
		assertContains(glassLungs, "\"required_clarity\": 50");
		assertContains(glassLungs, "\"id\": \"hemomancy:lethean_chalice\"");

		String moonWashed = read("src/main/resources/data/hemomancy/recipe/cardinal_rite/moon_washed_copper.json");
		assertContains(moonWashed, "\"required_clarity\": 75");
		assertContains(moonWashed, "\"id\": \"hemomancy:pale_silver_bell\"");
		assertNotContains(moonWashed, "verdigris_censer");

		String riteSerializer = read("src/main/java/com/vincenthuto/hemomancy/common/recipe/serializer/CardinalRiteRecipeSerializer.java");
		assertContains(riteSerializer, "required_purity");
		assertContains(riteSerializer, "required_clarity");
		String gates = read("src/main/java/com/vincenthuto/hemomancy/common/recipe/RecipeDegreeGates.java");
		assertContains(gates, "progress.getClarity() >= requiredClarity");
		assertContains(gates, "progress.getPurity() >= requiredPurity");

		String artGrants = read("src/main/java/com/vincenthuto/hemomancy/common/capability/player/unstained/stillart/KnownStillArtEvents.java");
		assertContains(artGrants, "if (!art.isUnlockedFor(player))");

		String eventHandler = read("src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/DialogueEventHandler.java");
		for (String event : new String[] {
				"acolyte_task_gather_ghost_pipe", "acolyte_task_wreath", "acolyte_task_hemolytic",
				"acolyte_task_consecrate", "acolyte_task_chalice" }) {
			assertContains(eventHandler, "case \"" + event + "\"");
		}

		String progress = read("src/main/java/com/vincenthuto/hemomancy/common/capability/player/unstained/UnstainedProgress.java");
		assertContains(progress, "acceptedObservances");
		assertContains(progress, "claimedObservances");
		String observances = read("src/main/java/com/vincenthuto/hemomancy/common/mission/UnstainedObservanceHelper.java");
		assertContains(observances, "GATHER_GHOST_PIPE");
		assertContains(observances, "OFFER_CHALICE");
		assertContains(observances, "progress.setClaimedObservances");

		String blockInit = read("src/main/java/com/vincenthuto/hemomancy/common/init/BlockInit.java");
		assertContains(blockInit, "stillwater_condenser");
		assertContains(blockInit, "verdigris_lattice");
		assertContains(read("src/main/java/com/vincenthuto/hemomancy/common/block/unstained/crafting/StillwaterCondenserBlock.java"),
				"progress.getPurity() >= 50f");
		assertContains(read("src/main/java/com/vincenthuto/hemomancy/common/block/unstained/crafting/VerdigrisLatticeBlock.java"),
				"MobEffects.DAMAGE_RESISTANCE");
	}

	private static void observanceStateRoundTripsAndUnlocksByStage() {
		UnstainedProgress original = new UnstainedProgress();
		original.setBegunPurification(true);
		original.setPurity(50f);
		original.setAcceptedObservances(0b00111);
		original.setClaimedObservances(0b00011);
		if (!UnstainedObservanceHelper.isAvailable(original,
				UnstainedObservanceHelper.Observance.PREPARE_HEMOLYTIC)) {
			throw new AssertionError("50 Purity should unlock the hemolytic observance");
		}
		if (UnstainedObservanceHelper.isAvailable(original,
				UnstainedObservanceHelper.Observance.CONSECRATE_COPPER)) {
			throw new AssertionError("consecration should remain locked before full Purity");
		}
		UnstainedProgress restored = new UnstainedProgress();
		restored.deserializeNBT(null, original.serializeNBT(null));
		if (restored.getAcceptedObservances() != 0b00111 || restored.getClaimedObservances() != 0b00011) {
			throw new AssertionError("observance masks did not survive NBT round trip");
		}
	}

	private static String read(String path) throws IOException {
		return Files.readString(Path.of(path));
	}

	private static void assertContains(String source, String expected) {
		if (!source.contains(expected)) throw new AssertionError("missing " + expected);
	}

	private static void assertNotContains(String source, String unexpected) {
		if (source.contains(unexpected)) throw new AssertionError("unexpected " + unexpected);
	}
}
