package com.vincenthuto.hemomancy.architecture;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * F-2: payloads whose handlers run client code must be registered server-to-client only, so a client cannot send
 * them to a dedicated server.
 */
class ClientBoundPayloadRegistrationTest {
	private static final Path PACKET_HANDLER =
			Path.of("src/main/java/com/vincenthuto/hemomancy/common/network/PacketHandler.java");
	private static final Pattern REGISTRATION =
			Pattern.compile("\\bnet\\.(play\\w+)\\(\\s*(?:[\\w.]+\\.)?(\\w+)\\.TYPE\\b");
	private static final List<String> CLIENT_BOUND = List.of("PacketEquipmentSync", "PacketGourdScarSync",
			"SyncTrackingAvatarPacket", "PacketCurvedHornAnimation");

	@Test
	void clientHandledPayloadsAreRegisteredPlayToClientOnly() throws Exception {
		String source = Files.readString(PACKET_HANDLER);
		for (String payload : CLIENT_BOUND) {
			List<String> directions = new ArrayList<>();
			Matcher matcher = REGISTRATION.matcher(source);
			while (matcher.find()) {
				if (matcher.group(2).equals(payload)) {
					directions.add(matcher.group(1));
				}
			}
			assertEquals(List.of("playToClient"), directions, payload + " registrations");
		}
	}
}
