package com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class BloodlineDisbandSanctumSourceTest {
	private static final Path HELPER = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/bloodvolume/BloodlineDisbandHelper.java");
	private static final Path LEDGER_PACKET = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/network/capa/PacketLedgerAction.java");
	private static final Path COMMAND = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/command/HemoCommand.java");
	private static final Path RITE_EVENTS = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/HarbingerCardinalRiteEvents.java");

	private BloodlineDisbandSanctumSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String helper = Files.readString(HELPER).replace("\r\n", "\n");
		String ledgerPacket = Files.readString(LEDGER_PACKET).replace("\r\n", "\n");
		String command = Files.readString(COMMAND).replace("\r\n", "\n");
		String riteEvents = Files.readString(RITE_EVENTS).replace("\r\n", "\n");

		assertContains("helper removes sanctums from the sanctum saved data", helper, "FoundingSanctumSavedData");
		assertContains("helper checks every loaded dimension", helper, "server.getAllLevels()");
		assertContains("helper removes every player-owned sanctum in the disbanded bloodline", helper,
				"disbandedLine.getPlayerUUIDS()");
		assertContains("helper removes sanctum entries by owner uuid", helper, "sanctumData.remove(memberUuid)");
		assertContains("ledger disband clears sanctums for the disbanded bloodline", ledgerPacket,
				"BloodlineDisbandHelper.removeOwnedSanctums(player.server, globalLine)");
		assertContains("command disband clears sanctums for the disbanded bloodline", command,
				"BloodlineDisbandHelper.removeOwnedSanctums(source.getServer(), globalLine)");
		assertContains("hematic unbinding clears sanctums for the disbanded bloodline", riteEvents,
				"BloodlineDisbandHelper.removeOwnedSanctums(sLevel.getServer(), bloodline)");
		assertOrder("ledger clears sanctums before removing bloodline data", ledgerPacket,
				"BloodlineDisbandHelper.removeOwnedSanctums(player.server, globalLine)",
				"savedData.disbandBloodline(globalLine.getBloodlineUUID())");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}

	private static void assertOrder(String label, String text, String first, String second) {
		int firstIndex = text.indexOf(first);
		int secondIndex = text.indexOf(second);
		if (firstIndex < 0 || secondIndex < 0 || firstIndex > secondIndex) {
			throw new AssertionError(label + ": expected `" + first + "` before `" + second + "`");
		}
	}
}
