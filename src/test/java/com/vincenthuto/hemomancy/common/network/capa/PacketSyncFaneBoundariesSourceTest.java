package com.vincenthuto.hemomancy.common.network.capa;

import org.junit.jupiter.api.Test;
import com.vincenthuto.hemomancy.support.SourceAssertions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.vincenthuto.hemomancy.support.SourceAssertions.assertContains;

public final class PacketSyncFaneBoundariesSourceTest {
	private static final Path PACKET = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/PacketSyncFaneBoundaries.java");

	private PacketSyncFaneBoundariesSourceTest() {
	}

	@Test
	void allChecksPass() throws IOException {
		String source = Files.readString(PACKET).replace("\r\n", "\n");
		assertContains("declares envelope sync entry", source,
				"record Entry(BlockPos heart, List<BlockPos> stakes, float radius, UUID ownerUuid, FaneBoundaryRelation relation)");
		assertContains("writes heart", source, "buf.writeBlockPos(entry.heart())");
		assertContains("writes stake count", source, "buf.writeInt(entry.stakes().size())");
		assertContains("writes stakes", source, "buf.writeBlockPos(stake)");
		assertContains("writes radius", source, "buf.writeFloat(entry.radius())");
		assertContains("writes owner", source, "buf.writeUUID(entry.ownerUuid())");
		assertContains("writes relation", source, "buf.writeEnum(entry.relation())");
		assertContains("updates client cache",
				SourceAssertions.read("src/main/java/com/vincenthuto/hemomancy/client/network/ClientboundStateHandlers.java"), "FaneBoundaryClientData.setEntries(msg.entries())");
	}
}
