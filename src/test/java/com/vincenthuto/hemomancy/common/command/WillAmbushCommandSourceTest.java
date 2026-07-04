package com.vincenthuto.hemomancy.common.command;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class WillAmbushCommandSourceTest {
	private static final Path COMMAND = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/command/HemoCommand.java");
	private static final Path REFERENCE = Path.of("docs/HEMOMANCY_REFERENCE.md");

	private WillAmbushCommandSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String command = Files.readString(COMMAND).replace("\r\n", "\n");
		String reference = Files.readString(REFERENCE).replace("\r\n", "\n");
		String compact = compact(command);

		assertContains("command exposes will root", command, "Commands.literal(\"will\")");
		assertContains("command exposes ambush branch", command, "Commands.literal(\"ambush\")");
		assertContains("command exposes anchor ambush branch", command, "Commands.literal(\"anchor\")");
		assertContains("command exposes immediate ambush branch", command, "Commands.literal(\"immediate\")");
		assertContains("anchor command accepts school", command, "Commands.argument(\"school\", StringArgumentType.word())");
		assertContains("anchor command accepts tier", command, "Commands.argument(\"tier\", IntegerArgumentType.integer(1, 4))");
		assertContains("anchor command accepts broken count", command,
				"Commands.argument(\"broken_count\", IntegerArgumentType.integer(0, 8))");
		assertContains("anchor command accepts sent flag", command, "Commands.argument(\"sent_present\", BoolArgumentType.bool())");
		assertContains("immediate command accepts origin", command, "Commands.argument(\"origin\", StringArgumentType.word())");
		assertContains("immediate command accepts count", command, "Commands.argument(\"count\", IntegerArgumentType.integer(1, 8))");
		assertContains("command suggests tendencies", command, "suggestBloodTendencies");
		assertContains("command suggests will origins", command, "suggestWillOrigins");
		assertContains("command parses tendencies", command, "parseBloodTendency");
		assertContains("command parses origins", command, "parseWillOrigin");
		assertContains("anchor command spawns the real anchor visual", command, "EntityInit.will_anchor.get().create(level)");
		assertContains("anchor command configures composition", command,
				"new WillCompositionRules.Composition(tier, brokenCount, Math.min(2, tier), sentPresent)");
		assertContains("anchor command uses real anchor configure", command, "anchor.configure(school, composition, player)");
		assertContains("immediate command creates Wills", command, "EntityInit.will.get().create(level)");
		assertContains("immediate command uses real Will configure", command, "will.configure(origin, school, tier, player, true)");
		assertContains("anchor executor uses target player fallback", compact,
				"summonWillAmbushAnchor(ctx.getSource(), ctx.getSource().getPlayerOrException(),");
		assertContains("immediate executor uses target player fallback", compact,
				"summonWillAmbushImmediate(ctx.getSource(), ctx.getSource().getPlayerOrException(),");

		assertContains("reference documents anchor command", reference, "/hemo will ambush anchor");
		assertContains("reference documents immediate command", reference, "/hemo will ambush immediate");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}

	private static String compact(String text) {
		return text.replaceAll("\\s+", " ");
	}
}
