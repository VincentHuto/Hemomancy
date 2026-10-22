package com.vincenthuto.hemomancy.common.tile;

import java.io.IOException;

import static com.vincenthuto.hemomancy.support.SourceAssertions.assertContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.assertNotContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.read;

public final class LivingWeaponGraftRiteSourceTest {
	private LivingWeaponGraftRiteSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String blockEntity = read("src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/rite/IronBrazierBlockEntity.java");
		String block = read("src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/rite/BrazierBlock.java");
		String rite = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/memories/LivingWeaponGraftRite.java");
		String memoryOfVesper = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/MemoryOfVesperItem.java");
		String packetHandler = read("src/main/java/com/vincenthuto/hemomancy/common/network/PacketHandler.java");
		String cellHandParticles = read("src/main/java/com/vincenthuto/hemomancy/client/render/item/harbinger/CellHandParticleEffects.java");
		String rites = read("src/main/java/com/vincenthuto/hemomancy/common/rite/brazier/BrazierRites.java");

		assertContains("brazier tracks absorption player", blockEntity, "itemAbsorptionPlayer");
		assertContains("brazier tracks absorption rite", blockEntity, "itemAbsorptionRiteId");
		assertContains("brazier tracks absorption progress", blockEntity, "itemAbsorptionProgressTicks");
		assertContains("brazier can advance absorption", blockEntity, "advanceItemAbsorption");
		assertContains("brazier can reset absorption", blockEntity, "resetItemAbsorptionProgress");
		assertContains("brazier clears absorption when offering changes", blockEntity, "resetItemAbsorptionProgress()");
		assertNotContains("shared brazier state is no longer graft-specific", blockEntity, "graftRiteProgressTicks");

		assertContains("brazier absorb endpoint delegates to the brazier rites", block, "BrazierRites.absorb");
		assertContains("brazier rites include the graft rite", rites, "LivingWeaponGraftRite.INSTANCE");
		assertContains("brazier still uses block blood endpoint", block, "implements EntityBlock, SimpleWaterloggedBlock, BlockBloodEndpoint");

		assertContains("rite shares the standard brazier item absorption duration", rite,
				"REQUIRED_CHANNEL_TICKS = BrazierItemAbsorptionRite.REQUIRED_CHANNEL_TICKS");
		assertContains("rite uses shared completion lifecycle", rite, "implements BrazierRite<");
		assertContains("shared lifecycle completes through the absorption rite", rites,
				"BrazierItemAbsorptionRite.complete");
		assertNotContains("rite no longer duplicates offering consumption", rite, "consumeOffering()");
		assertNotContains("rite no longer duplicates brazier extinguishing", rite, "extinguishBrazier");
		assertContains("rite uses the shared item particle channel", rites, "BrazierItemAbsorptionRite.advance");
		assertContains("absorption particle packet is registered", packetHandler, "SpawnBrazierItemAbsorptionParticlesPacket.TYPE");
		assertContains("absorption particles use the first-person fallback outside an item render callback",
				cellHandParticles, "fallbackFirstPersonHandOrigin(activeArm)");
		assertNotContains("absorbed offerings no longer break into vanilla item particles", cellHandParticles,
				"ParticleTypes.ITEM");
		assertContains("absorbed offerings stream in as tinted absorbed cells", cellHandParticles,
				"spawnAbsorbedBloodParticle(mc, casterId, false, origin, source, absorptionItemColor, rand, firstPersonTargetAnchor)");
		assertContains("absorbed offering cells use a sparse per-tick count", cellHandParticles,
				"ABSORBED_ITEM_PARTICLES_PER_TICK = 2");
		assertContains("the offering stream follows whichever player channels it", cellHandParticles,
				"mc.level.getEntity(casterId) instanceof Player player");
		assertContains("absorption particles read graft form data when present", cellHandParticles, "LivingWeaponGraftData.fromStack(offeringStack)");
		assertContains("graft particles use manipulation tendency color", cellHandParticles, "form.manipulationHolder().get().getTend().getColor()");
		assertContains("absorption particles add hutoslib glow", cellHandParticles, "GlowParticleFactory.createData(absorptionItemColor)");
		assertContains("generic client absorption entry point", cellHandParticles, "spawnBrazierItemAbsorptionParticles");
		assertContains("graft rite requires a lit brazier", rite, "if (!channel.lit())");
		assertContains("brazier rites read the lit state from the block", rites, "BrazierBlock.isLit(state)");
		assertBefore("preflight happens before channel progress", rites,
				"rite.preflight(channel)", "BrazierItemAbsorptionRite.advance");
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
		assertContains("rite lifecycle returns handled blood amount", rites, "return maxAmount");

		assertContains("vesper memory item guides to brazier rite", memoryOfVesper,
				"hemomancy.memory_of_vesper.rite_guidance");
		assertNotContains("vesper memory item no longer awakens staff directly", memoryOfVesper,
				"progress.awakenVesperMemory()");
		assertNotContains("vesper memory item no longer consumes itself directly", memoryOfVesper,
				"stack.shrink(1)");
	}

	private static void assertBefore(String label, String text, String first, String second) {
		int firstIndex = text.indexOf(first);
		int secondIndex = text.indexOf(second);
		if (firstIndex < 0 || secondIndex < 0 || firstIndex >= secondIndex) {
			throw new AssertionError(label + " (expected '" + first + "' before '" + second + "')");
		}
	}
}
