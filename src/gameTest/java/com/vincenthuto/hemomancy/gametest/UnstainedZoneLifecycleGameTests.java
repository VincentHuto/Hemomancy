package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.rite.unstained.LetheCovenantSavedData;
import com.vincenthuto.hemomancy.common.rite.unstained.PaleConsecrationSavedData;
import com.vincenthuto.hemomancy.common.rite.unstained.StillWatersSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder("unstained_zone_validation")
@PrefixGameTestTemplate(false)
public final class UnstainedZoneLifecycleGameTests {
	@GameTest(template = "empty", batch = "unstained_zone", timeoutTicks = 130)
	public static void overworldCleanupPrunesAllThreeStoredDimensions(GameTestHelper helper) {
		var overworld = helper.getLevel().getServer().overworld();
		var nether = helper.getLevel().getServer().getLevel(Level.NETHER);
		helper.assertTrue(nether != null, "Nether level is unavailable");
		BlockPos center = helper.absolutePos(new BlockPos(4, 3, 4));
		String dimension = nether.dimension().location().toString();
		long currentTick = overworld.getGameTime();
		long expiryTick = (currentTick / 100 + 1) * 100;
		UUID owner = UUID.randomUUID();

		var still = StillWatersSavedData.get(overworld);
		var pale = PaleConsecrationSavedData.get(overworld);
		var lethe = LetheCovenantSavedData.get(overworld);
		still.addEntry(new StillWatersSavedData.StillWatersEntry(owner, center, dimension, 16, expiryTick));
		pale.addEntry(new PaleConsecrationSavedData.ConsecrationEntry(owner, center, dimension, 12, expiryTick));
		lethe.addEntry(new LetheCovenantSavedData.CovenantEntry(owner, center, dimension, 5, expiryTick));

		helper.assertTrue(still.isInZone(center, dimension, currentTick)
				&& !still.isInZone(center, overworld.dimension().location().toString(), currentTick),
				"Still Waters lost its stored-dimension route");
		helper.assertTrue(lethe.isInDomain(center, dimension, currentTick)
				&& !lethe.isInDomain(center, overworld.dimension().location().toString(), currentTick),
				"Lethe lost its stored-dimension route");
		helper.assertTrue(hasOwner(still.save(new CompoundTag(), overworld.registryAccess()), owner)
				&& hasOwner(pale.save(new CompoundTag(), overworld.registryAccess()), owner)
				&& hasOwner(lethe.save(new CompoundTag(), overworld.registryAccess()), owner),
				"A zone did not serialize to Overworld storage");

		helper.runAfterDelay((int) (expiryTick - currentTick + 1), () -> {
			helper.assertTrue(still.getEntries().stream().noneMatch(e -> e.ownerUUID().equals(owner)),
					"Still Waters was not pruned at the 100-tick boundary");
			helper.assertTrue(pale.getEntries().stream().noneMatch(e -> e.ownerUUID().equals(owner)),
					"Pale Consecration was not pruned at the 100-tick boundary");
			helper.assertTrue(lethe.getEntries().stream().noneMatch(e -> e.ownerUUID().equals(owner)),
					"Lethe Covenant was not pruned at the 100-tick boundary");
			helper.succeed();
		});
	}

	private static boolean hasOwner(CompoundTag root, UUID owner) {
		var entries = root.getList("entries", 10);
		for (int i = 0; i < entries.size(); i++) {
			if (owner.equals(entries.getCompound(i).getUUID("Owner"))) return true;
		}
		return false;
	}
}
