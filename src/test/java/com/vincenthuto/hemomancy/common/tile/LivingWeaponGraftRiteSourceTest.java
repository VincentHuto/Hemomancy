package com.vincenthuto.hemomancy.common.tile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class LivingWeaponGraftRiteSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private LivingWeaponGraftRiteSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String blockEntity = read("src/main/java/com/vincenthuto/hemomancy/common/tile/IronBrazierBlockEntity.java");
		String block = read("src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/BrazierBlock.java");
		String rite = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/memories/LivingWeaponGraftRite.java");
		String memoryOfVesper = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/MemoryOfVesperItem.java");
		String packetHandler = read("src/main/java/com/vincenthuto/hemomancy/common/network/PacketHandler.java");
		String cellHandParticles = read("src/main/java/com/vincenthuto/hemomancy/client/render/item/hematic/CellHandParticleEffects.java");

		assertContains("brazier tracks absorption player", blockEntity, "itemAbsorptionPlayer");
		assertContains("brazier tracks absorption rite", blockEntity, "itemAbsorptionRiteId");
		assertContains("brazier tracks absorption progress", blockEntity, "itemAbsorptionProgressTicks");
		assertContains("brazier can advance absorption", blockEntity, "advanceItemAbsorption");
		assertContains("brazier can reset absorption", blockEntity, "resetItemAbsorptionProgress");
		assertContains("brazier clears absorption when offering changes", blockEntity, "resetItemAbsorptionProgress()");
		assertNotContains("shared brazier state is no longer graft-specific", blockEntity, "graftRiteProgressTicks");

		assertContains("brazier absorb endpoint delegates graft rite", block, "LivingWeaponGraftRite.tryAbsorb");
		assertContains("brazier still uses block blood endpoint", block, "implements EntityBlock, SimpleWaterloggedBlock, BlockBloodEndpoint");

		assertContains("rite shares the standard brazier item absorption duration", rite,
				"REQUIRED_CHANNEL_TICKS = BrazierItemAbsorptionRite.REQUIRED_CHANNEL_TICKS");
		assertContains("rite uses shared completion lifecycle", rite, "BrazierItemAbsorptionRite.complete");
		assertNotContains("rite no longer duplicates offering consumption", rite, "consumeOffering()");
		assertNotContains("rite no longer duplicates brazier extinguishing", rite, "extinguishBrazier");
		assertContains("rite uses the shared item particle channel", rite, "BrazierItemAbsorptionRite.advance");
		assertContains("absorption particle packet is registered", packetHandler, "SpawnBrazierItemAbsorptionParticlesPacket.TYPE");
		assertContains("absorption particles use the first-person fallback outside an item render callback",
				cellHandParticles, "fallbackFirstPersonHandOrigin(activeArm)");
		assertContains("absorption particles use item particle option", cellHandParticles, "new ItemParticleOption(ParticleTypes.ITEM, offeringStack)");
		assertContains("absorbed item fragments use a sparse per-tick count", cellHandParticles,
				"ABSORBED_ITEM_PARTICLES_PER_TICK = 2");
		assertContains("absorbed item fragments render at a reduced scale", cellHandParticles,
				"ABSORBED_ITEM_PARTICLE_SCALE = 0.55F");
		assertContains("absorbed item fragments apply their reduced scale", cellHandParticles,
				"itemParticle.scale(ABSORBED_ITEM_PARTICLE_SCALE)");
		assertContains("absorption particles read graft form data when present", cellHandParticles, "LivingWeaponGraftData.fromStack(offeringStack)");
		assertContains("graft particles use manipulation tendency color", cellHandParticles, "form.manipulationHolder().get().getTend().getColor()");
		assertContains("absorption particles add hutoslib glow", cellHandParticles, "GlowParticleFactory.createData(absorptionItemColor)");
		assertContains("generic client absorption entry point", cellHandParticles, "spawnBrazierItemAbsorptionParticles");
		assertContains("graft rite requires a lit brazier", rite, "getValue(BrazierBlock.RITUAL_PHASE) > 0");
		assertBefore("lit check happens before channel progress", rite,
				"getValue(BrazierBlock.RITUAL_PHASE) > 0", "BrazierItemAbsorptionRite.advance");
		assertNotContains("graft rite no longer auto-lights the brazier", rite,
				"setValue(BrazierBlock.RITUAL_PHASE, 2)");
		assertContains("rite grants through form memory helper", rite, "LivingWeaponMemoryUnlocks.grantFormMemory");
		assertContains("rite accepts memory of vesper directly", rite, "offering.is(ItemInit.memory_of_vesper.get())");
		assertContains("rite awakens vesper staff progress", rite, "progress.awakenVesperMemory()");
		assertContains("rite syncs vesper staff progress", rite, "LivingStaffBondHelper.syncProgress(player)");
		assertContains("rite requires living staff absorption", rite, "isLivingStaffAbsorptionUse");
		assertContains("rite checks earned condition", rite, "hasEarnedRecipeUnlock");
		assertContains("rite bypasses earned condition for creative testing", rite, "player.isCreative()");
		assertContains("rite refuses already known forms", rite, "ALREADY_KNOWN");
		assertContains("rite returns handled blood amount", rite, "return maxAmount");

		assertContains("vesper memory item guides to brazier rite", memoryOfVesper,
				"hemomancy.memory_of_vesper.rite_guidance");
		assertNotContains("vesper memory item no longer awakens staff directly", memoryOfVesper,
				"progress.awakenVesperMemory()");
		assertNotContains("vesper memory item no longer consumes itself directly", memoryOfVesper,
				"stack.shrink(1)");
	}

	private static String read(String path) throws IOException {
		Path absolute = ROOT.resolve(path);
		if (!Files.exists(absolute)) {
			throw new AssertionError("missing " + path);
		}
		return Files.readString(absolute).replace("\r\n", "\n");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + " (missing '" + expected + "')");
		}
	}

	private static void assertNotContains(String label, String text, String unexpected) {
		if (text.contains(unexpected)) {
			throw new AssertionError(label + " (found '" + unexpected + "')");
		}
	}

	private static void assertBefore(String label, String text, String first, String second) {
		int firstIndex = text.indexOf(first);
		int secondIndex = text.indexOf(second);
		if (firstIndex < 0 || secondIndex < 0 || firstIndex >= secondIndex) {
			throw new AssertionError(label + " (expected '" + first + "' before '" + second + "')");
		}
	}
}
