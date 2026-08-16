package com.vincenthuto.hemomancy.common.world.fold;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class NonEuclideanHallwayPlacementSourceTest {
	private NonEuclideanHallwayPlacementSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String block = read("src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/functional/NonEuclideanHallwayBlock.java");
		String blockEntity = read("src/main/java/com/vincenthuto/hemomancy/common/tile/functional/NonEuclideanHallwayBlockEntity.java");

		assertContains("fresh placement should force a server-to-client block update",
				block, "level.sendBlockUpdated(pos, state, state, 3)");
		assertContains("fresh placement should mark the new block entity dirty",
				block, "blockEntity.setChanged()");
		assertContains("fresh placement should notify neighbors for immediate client/server shape refresh",
				block, "level.updateNeighborsAt(pos, state.getBlock())");
		assertContains("hallway block entity should provide a client update packet",
				blockEntity, "ClientboundBlockEntityDataPacket.create(this)");
	}

	private static String read(String path) throws IOException {
		return Files.readString(Path.of(path)).replace("\r\n", "\n");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}
}
