package com.vincenthuto.hemomancy.common.command;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class QliphothPomeCommandSourceTest {
	private static final Path COMMAND = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/command/HemoCommand.java");
	private static final Path DEGREE = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/degree/IInitiatoryDegree.java");
	private static final Path CHAMBER_CLIENT_DATA = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/data/ChamberOfWillClientData.java");
	private static final Path CHAMBER_PACKET = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/PacketSyncChamberOfWill.java");
	private static final Path CHAMBER_MANAGER = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/worldgen/ChamberOfWillManager.java");
	private static final Path REFERENCE = Path.of("docs/HEMOMANCY_REFERENCE.md");

	private QliphothPomeCommandSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String command = Files.readString(COMMAND).replace("\r\n", "\n");
		String compactCommand = command.replaceAll("\\s+", " ");
		String degree = Files.readString(DEGREE).replace("\r\n", "\n");
		String chamberClientData = Files.readString(CHAMBER_CLIENT_DATA).replace("\r\n", "\n");
		String chamberPacket = Files.readString(CHAMBER_PACKET).replace("\r\n", "\n");
		String chamberManager = Files.readString(CHAMBER_MANAGER).replace("\r\n", "\n");
		String reference = Files.readString(REFERENCE).replace("\r\n", "\n");

		assertContains("command exposes qliphoth pome set branch", compactCommand,
				"Commands.literal(\"set\") .then(Commands.argument(\"count\", IntegerArgumentType.integer(0, 9))");
		assertContains("command supports setting executor pome progress", compactCommand,
				"setPomeProgress(ctx.getSource(), ctx.getSource().getPlayerOrException(), IntegerArgumentType.getInteger(ctx, \"count\"))");
		assertContains("command supports setting target pome progress", compactCommand,
				"setPomeProgress(ctx.getSource(), EntityArgument.getPlayer(ctx, \"player\"), IntegerArgumentType.getInteger(ctx, \"count\"))");
		assertContains("command has pome progress setter implementation", command,
				"private static int setPomeProgress(CommandSourceStack source, ServerPlayer player, int count)");
		assertContains("command setter syncs HUD pome progress", command,
				"PacketHandler.sendToPlayer(player, new PacketSyncPomeProgress(clamped));");
		assertContains("command setter refreshes chamber sky sync", command,
				"ChamberOfWillManager.syncFor(player);");
		assertContains("command setter marks communion done at nine", command,
				"degree.setQliphothCommunionDone(clamped >= 9);");

		assertContains("degree capability exposes debug setter for total pome count", degree,
				"void syncTotalPomesConsumed(int count);");
		assertContains("chamber client data stores qliphoth pome count", chamberClientData,
				"private static int qliphothPomesConsumed = 0;");
		assertContains("chamber client data exposes qliphoth pome count", chamberClientData,
				"public static int qliphothPomesConsumed()");
		assertContains("chamber sync packet carries qliphoth pome count", chamberPacket,
				"private final int qliphothPomesConsumed;");
		assertContains("chamber sync packet writes qliphoth pome count", chamberPacket,
				"buf.writeInt(msg.qliphothPomesConsumed);");
		assertContains("chamber sync packet reads qliphoth pome count", chamberPacket,
				"buf.readInt(), buf.readInt(), buf.readInt()");
		assertContains("chamber manager computes qliphoth pome count for chamber sync", chamberManager,
				"private static int qliphothPomeCount(ServerPlayer player)");
		assertContains("chamber manager switches to qliphoth when owned bloom is active", chamberManager,
				"hasActiveQliphothBloom(player)");
		assertContains("reference documents qliphoth pome set debug command", reference,
				"/hemo qliphoth pome set <0-9> [player]");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}
}
