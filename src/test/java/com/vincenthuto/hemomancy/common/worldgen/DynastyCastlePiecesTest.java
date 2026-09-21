package com.vincenthuto.hemomancy.common.worldgen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.vincenthuto.hemomancy.common.data.gen.DynastyCastlePieces;
import org.junit.jupiter.api.Test;

class DynastyCastlePiecesTest {

	@Test
	void everyPieceHasBlocksWithinItsDeclaredSize() {
		assertEquals(15, DynastyCastlePieces.pieceNames().size());
		for (String piece : DynastyCastlePieces.pieceNames()) {
			int[] size = DynastyCastlePieces.size(piece);
			var blocks = DynastyCastlePieces.blocks(piece);
			assertFalse(blocks.isEmpty(), piece + " has no blocks");
			for (var b : blocks) {
				assertTrue(b.x() >= 0 && b.x() < size[0], piece + " x out of range: " + b.x());
				assertTrue(b.y() >= 0 && b.y() < size[1], piece + " y out of range: " + b.y());
				assertTrue(b.z() >= 0 && b.z() < size[2], piece + " z out of range: " + b.z());
			}
		}
	}

	@Test
	void normalizedPiecesTouchEveryMinPlane() {
		// normalization guarantees the piece is flush against x=0, y=0, z=0 on at least one block
		for (String piece : DynastyCastlePieces.pieceNames()) {
			var blocks = DynastyCastlePieces.blocks(piece);
			assertTrue(blocks.stream().anyMatch(b -> b.x() == 0), piece + " not flush to x=0");
			assertTrue(blocks.stream().anyMatch(b -> b.y() == 0), piece + " not flush to y=0");
			assertTrue(blocks.stream().anyMatch(b -> b.z() == 0), piece + " not flush to z=0");
		}
	}

	@Test
	void keepGrandBakesFargoneHousehold() {
		var entities = DynastyCastlePieces.entities("keep_grand");
		assertFalse(entities.isEmpty());
		assertTrue(entities.stream().anyMatch(e -> e.entityId().equals("hemomancy:fargone")));
	}

	@Test
	void brokenTowerBakesThirster() {
		var entities = DynastyCastlePieces.entities("tower_broken");
		assertTrue(entities.stream().anyMatch(e -> e.entityId().equals("hemomancy:thirster")));
	}
}
