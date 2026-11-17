package com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.vincenthuto.hemomancy.support.SourceAssertions.assertContains;

public final class BloodlineDisbandFaneSourceTest {
	private static final Path HELPER = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/bloodvolume/BloodlineDisbandHelper.java");
	private static final Path LEDGER_PACKET = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/PacketLedgerAction.java");
	private static final Path COMMAND = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/command/BloodlineSubcommand.java");
	private static final Path RITE_EVENTS = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/HarbingerCardinalRiteEvents.java");

	private BloodlineDisbandFaneSourceTest() {
	}

	@Test
	void allChecksPass() throws IOException {
		String helper = Files.readString(HELPER).replace("\r\n", "\n");
		String ledgerPacket = Files.readString(LEDGER_PACKET).replace("\r\n", "\n");
		String command = Files.readString(COMMAND).replace("\r\n", "\n");
		String riteEvents = (Files.readString(RITE_EVENTS) + Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/CardinalRiteCompletionEffects.java"))).replace("\r\n", "\n");

		assertContains("helper removes fanes from the fane saved data", helper, "FoundingFaneSavedData");
		assertContains("helper checks every loaded dimension", helper, "server.getAllLevels()");
		assertContains("helper removes every player-owned fane in the disbanded bloodline", helper,
				"disbandedLine.getPlayerUUIDS()");
		assertContains("helper removes fane entries by owner uuid", helper, "faneData.remove(memberUuid)");
		assertContains("helper captures heart position before disband cleanup", helper, "faneData.getHeart(memberUuid)");
		assertContains("helper captures stake positions before disband cleanup", helper,
				"faneData.removeStakesAndGet(memberUuid)");
		assertContains("helper removes physical stake blocks on disband", helper, "level.removeBlock(stakePos, false)");
		assertContains("helper removes physical bloodwell block on disband", helper, "level.removeBlock(heartPos, false)");
		assertContains("helper checks hematic stake blocks before removal", helper, "BlockInit.hematic_stake");
		assertContains("helper checks bloodwell blocks before removal", helper, "BlockInit.consecrated_bloodwell");
		String membership = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/bloodvolume/BloodlineMembership.java"))
				.replace("\r\n", "\n");
		assertContains("ledger disband goes through the one departure service", ledgerPacket,
				"BloodlineMembership.dissolve(player.server, globalLine, BloodlineMembership.Dissolution.DISBAND");
		assertContains("command disband goes through the one departure service", command,
				"BloodlineMembership.dissolve(source.getServer(), globalLine, BloodlineMembership.Dissolution.COMMAND");
		assertContains("hematic unbinding goes through the one departure service", riteEvents,
				"BloodlineMembership.dissolve(sLevel.getServer(), bloodline, BloodlineMembership.Dissolution.UNBINDING");
		assertOrder("the service clears fanes before removing bloodline data", membership,
				"BloodlineDisbandHelper.removeOwnedFanes(server, line)",
				"data.disbandBloodline(line.getBloodlineUUID())");
	}

	private static void assertOrder(String label, String text, String first, String second) {
		int firstIndex = text.indexOf(first);
		int secondIndex = text.indexOf(second);
		if (firstIndex < 0 || secondIndex < 0 || firstIndex > secondIndex) {
			throw new AssertionError(label + ": expected `" + first + "` before `" + second + "`");
		}
	}
}
