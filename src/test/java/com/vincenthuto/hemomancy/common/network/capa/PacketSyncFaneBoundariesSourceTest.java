package com.vincenthuto.hemomancy.common.network.capa;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PacketSyncFaneBoundariesSourceTest {
	private static final Path PACKET = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/PacketSyncFaneBoundaries.java");

	private PacketSyncFaneBoundariesSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String source = Files.readString(PACKET).replace("\r\n", "\n");
		assertContains("declares envelope sync entry", source,
				"record Entry(BlockPos heart, List<BlockPos> stakes, float radius, UUID ownerUuid, FaneBoundaryRelation relation)");
		assertContains("writes heart", source, "buf.writeBlockPos(entry.heart())");
		assertContains("writes stake count", source, "buf.writeInt(entry.stakes().size())");
		assertContains("writes stakes", source, "buf.writeBlockPos(stake)");
		assertContains("writes radius", source, "buf.writeFloat(entry.radius())");
		assertContains("writes owner", source, "buf.writeUUID(entry.ownerUuid())");
		assertContains("writes relation", source, "buf.writeEnum(entry.relation())");
		assertContains("updates client cache", source, "FaneBoundaryClientData.setEntries(msg.entries)");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}
}
