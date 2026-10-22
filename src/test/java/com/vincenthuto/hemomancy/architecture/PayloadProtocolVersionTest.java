package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * N-7: the registered payload set is pinned to {@code PacketHandler.PROTOCOL_VERSION}. Adding, removing or
 * re-directing a payload changes this list; when it does, bump {@code PROTOCOL_VERSION} and rewrite the snapshot in
 * {@code src/test/resources/network/payload-registry.txt}. Byte-layout changes cannot be detected here and need the
 * same bump by hand.
 */
class PayloadProtocolVersionTest {
	private static final String SNAPSHOT = "src/test/resources/network/payload-registry.txt";

	@Test
	void payloadSetMatchesTheSnapshotForThisProtocolVersion() {
		List<String> snapshot = SourceAssertions.read(RepoPaths.resolve(SNAPSHOT)).lines()
				.filter(line -> !line.isBlank() && !line.startsWith("#"))
				.toList();
		String recordedVersion = snapshot.getFirst();
		List<String> recordedPayloads = snapshot.subList(1, snapshot.size());

		List<String> current = new ArrayList<>();
		NetworkRegistrations.directions().forEach((payload, method) -> current.add(method + " " + payload));

		assertEquals(recordedVersion, "version " + NetworkRegistrations.protocolVersion(),
				"PROTOCOL_VERSION changed: rewrite " + SNAPSHOT + " for the new version");
		assertEquals(recordedPayloads, current,
				"The payload set changed under the same PROTOCOL_VERSION: bump it in PacketHandler and rewrite " + SNAPSHOT);
	}
}
