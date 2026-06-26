package com.vincenthuto.hemomancy.common.command;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ChamberThemeCommandSourceTest {
	private static final Path COMMAND = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/command/HemoCommand.java");
	private static final Path MANAGER = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/worldgen/ChamberOfWillManager.java");
	private static final Path REFERENCE = Path.of("docs/HEMOMANCY_REFERENCE.md");

	private ChamberThemeCommandSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String command = Files.readString(COMMAND).replace("\r\n", "\n");
		String manager = Files.readString(MANAGER).replace("\r\n", "\n");
		String reference = Files.readString(REFERENCE).replace("\r\n", "\n");
		String commandCompact = compact(command);

		assertContains("command exposes chamber root", command, "Commands.literal(\"chamber\")");
		assertContains("command exposes theme branch", command, "Commands.literal(\"theme\")");
		assertContains("command exposes theme cycle", command, "Commands.literal(\"cycle\")");
		assertContains("command exposes theme next", command, "Commands.literal(\"next\")");
		assertContains("command exposes theme previous", command, "Commands.literal(\"previous\")");
		assertContains("command exposes theme set", command, "Commands.literal(\"set\")");
		assertContains("command exposes theme reset", command, "Commands.literal(\"reset\")");
		assertContains("command cycles executor theme", commandCompact,
				"cycleChamberTheme(ctx.getSource(), ctx.getSource().getPlayerOrException(), 1)");
		assertContains("command supports explicit target", commandCompact,
				"cycleChamberTheme(ctx.getSource(), EntityArgument.getPlayer(ctx, \"player\"), 1)");
		assertContains("command sets executor theme", commandCompact,
				"setChamberTheme(ctx.getSource(), ctx.getSource().getPlayerOrException(),");
		assertContains("command resets executor theme", commandCompact,
				"resetChamberTheme(ctx.getSource(), ctx.getSource().getPlayerOrException())");
		assertContains("command suggests registered themes", command,
				"ChamberOfWillManager.orderedSkyThemes()");

		assertContains("manager keeps stable theme order", manager, "ORDERED_SKY_THEMES");
		assertContains("manager stores testing overrides", manager, "skyThemeOverrides");
		assertContains("manager sets sky override", manager, "setSkyThemeOverride");
		assertContains("manager clears sky override", manager, "clearSkyThemeOverride");
		assertContains("manager cycles sky override", manager, "cycleSkyThemeOverride");
		assertContains("manager applies override during refresh", manager, "applySkyThemeOverride");
		assertContains("manager persists overrides", manager, "tag.put(\"skyThemeOverrides\", overrideList)");
		assertContains("manager loads overrides", manager, "tag.getList(\"skyThemeOverrides\", Tag.TAG_COMPOUND)");

		assertContains("reference documents testing command", reference, "/hemo chamber theme cycle");
		assertContains("reference documents reset behavior", reference, "reset` clears the override");
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
