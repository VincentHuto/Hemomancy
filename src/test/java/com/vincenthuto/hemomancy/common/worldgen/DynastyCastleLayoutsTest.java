package com.vincenthuto.hemomancy.common.worldgen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.vincenthuto.hemomancy.common.data.gen.DynastyCastlePieces;
import com.vincenthuto.hemomancy.common.worldgen.structure.DynastyCastleLayouts;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;

class DynastyCastleLayoutsTest {

	@Test
	void everyReferencedPieceExists() {
		Set<String> valid = new HashSet<>(DynastyCastlePieces.pieceNames());
		valid.add("empty");
		assertFalse(DynastyCastleLayouts.all().isEmpty());
		for (var layout : DynastyCastleLayouts.all()) {
			assertTrue(valid.contains(layout.keep()), "unknown keep " + layout.keep());
			for (var slot : layout.slots()) {
				for (var candidate : slot.candidates()) {
					assertTrue(valid.contains(candidate), "unknown piece " + candidate);
				}
			}
		}
	}

	@Test
	void pickIsDeterministicForASeed() {
		var a = DynastyCastleLayouts.pick(RandomSource.create(42L));
		var b = DynastyCastleLayouts.pick(RandomSource.create(42L));
		assertEquals(a.keep(), b.keep());
	}
}
