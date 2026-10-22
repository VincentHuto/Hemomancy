package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.SourceAssertions;

import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Reads the payload registrations in {@code PacketHandler} as payload name to registration method. */
final class NetworkRegistrations {
	static final String PACKET_HANDLER = "common/network/PacketHandler.java";
	private static final Pattern REGISTRATION =
			Pattern.compile("\\bnet\\.(play\\w+)\\(\\s*(?:[\\w.]+\\.)?(\\w+)\\.TYPE\\b");
	private static final Pattern PROTOCOL_VERSION =
			Pattern.compile("PROTOCOL_VERSION\\s*=\\s*\"([^\"]+)\"");

	private NetworkRegistrations() {
	}

	static Map<String, String> directions() {
		Map<String, String> directions = new TreeMap<>();
		Matcher matcher = REGISTRATION.matcher(SourceAssertions.readMainJava(PACKET_HANDLER));
		while (matcher.find()) {
			String previous = directions.put(matcher.group(2), matcher.group(1));
			if (previous != null) {
				throw new AssertionError(matcher.group(2) + " is registered twice");
			}
		}
		if (directions.isEmpty()) {
			throw new AssertionError("no payload registrations found in " + PACKET_HANDLER);
		}
		return directions;
	}

	static String protocolVersion() {
		Matcher matcher = PROTOCOL_VERSION.matcher(SourceAssertions.readMainJava(PACKET_HANDLER));
		if (!matcher.find()) {
			throw new AssertionError("PacketHandler.PROTOCOL_VERSION not found");
		}
		return matcher.group(1);
	}
}
