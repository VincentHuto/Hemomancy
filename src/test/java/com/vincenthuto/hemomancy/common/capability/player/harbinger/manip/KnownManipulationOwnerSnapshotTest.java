package com.vincenthuto.hemomancy.common.capability.player.harbinger.manip;

import com.vincenthuto.hemomancy.common.capability.block.vein.VeinLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

class KnownManipulationOwnerSnapshotTest {
	@Test
	void veinSelectionChangesSavedState() {
		KnownManipulations known = new KnownManipulations();
		var before = known.serializeNBT(null);
		VeinLocation selected = new VeinLocation("Cranial Vein", ResourceLocation.withDefaultNamespace("overworld"),
				new BlockPos(12, 70, 8));
		known.setVeinList(java.util.List.of(selected));
		known.setSelectedVein(selected);
		assertNotEquals(before, known.serializeNBT(null));
	}
}
