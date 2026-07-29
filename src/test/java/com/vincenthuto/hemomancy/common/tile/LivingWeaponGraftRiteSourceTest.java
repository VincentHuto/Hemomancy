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

		assertContains("brazier tracks graft rite player", blockEntity, "graftRitePlayer");
		assertContains("brazier tracks graft rite form", blockEntity, "graftRiteForm");
		assertContains("brazier tracks graft rite progress", blockEntity, "graftRiteProgressTicks");
		assertContains("brazier can advance graft rite", blockEntity, "advanceGraftRite");
		assertContains("brazier can reset graft rite", blockEntity, "resetGraftRiteProgress");
		assertContains("brazier clears rite when offering changes", blockEntity, "resetGraftRiteProgress()");

		assertContains("brazier absorb endpoint delegates graft rite", block, "LivingWeaponGraftRite.tryAbsorb");
		assertContains("brazier still uses block blood endpoint", block, "implements EntityBlock, SimpleWaterloggedBlock, BlockBloodEndpoint");

		assertContains("rite requires channel duration", rite, "REQUIRED_CHANNEL_TICKS = 60");
		assertContains("rite consumes offering on success", rite, "consumeOffering()");
		assertContains("rite extinguishes brazier on success", rite, "extinguishBrazier");
		assertContains("rite resets ritual phase to unlit", rite, "setValue(BrazierBlock.RITUAL_PHASE, 0)");
		assertContains("rite sends graft pulse packet", rite, "SpawnGraftRiteItemParticlesPacket");
		assertContains("rite sends matching graft stack as particle", rite, "particleStack.setCount(1)");
		assertContains("rite anchors graft particles to absorption path", rite, "spawnGraftDrawParticles(level, pos, player, offering)");
		assertContains("graft particle packet is registered", packetHandler, "SpawnGraftRiteItemParticlesPacket.TYPE");
		assertContains("graft particles use the first-person fallback outside an item render callback",
				cellHandParticles, "fallbackFirstPersonHandOrigin(activeArm)");
		assertContains("graft particles use item particle option", cellHandParticles, "new ItemParticleOption(ParticleTypes.ITEM, graftStack)");
		assertContains("graft particles read graft form data", cellHandParticles, "LivingWeaponGraftData.fromStack(graftStack)");
		assertContains("graft particles use manipulation tendency color", cellHandParticles, "form.manipulationHolder().get().getTend().getColor()");
		assertContains("graft particles add hutoslib glow", cellHandParticles, "GlowParticleFactory.createData(graftColor)");
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
}
