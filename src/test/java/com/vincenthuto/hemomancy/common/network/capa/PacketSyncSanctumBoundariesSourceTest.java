package com.vincenthuto.hemomancy.common.network.capa;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PacketSyncSanctumBoundariesSourceTest {
	private static final Path PACKET = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/network/capa/PacketSyncSanctumBoundaries.java");

	private PacketSyncSanctumBoundariesSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String source = Files.readString(PACKET).replace("\r\n", "\n");
		assertContains("declares compact sync entry", source,
				"record Entry(BlockPos center, float radius, UUID ownerUuid, SanctumBoundaryRelation relation)");
		assertContains("writes center", source, "buf.writeBlockPos(entry.center())");
		assertContains("writes radius", source, "buf.writeFloat(entry.radius())");
		assertContains("writes owner", source, "buf.writeUUID(entry.ownerUuid())");
		assertContains("writes relation", source, "buf.writeEnum(entry.relation())");
		assertContains("updates client cache", source, "SanctumBoundaryClientData.setEntries(msg.entries)");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}
}
