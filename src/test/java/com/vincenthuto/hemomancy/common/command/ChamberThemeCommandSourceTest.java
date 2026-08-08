package com.vincenthuto.hemomancy.common.command;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ChamberThemeCommandSourceTest {
	private static final Path COMMAND = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/command/HemoCommand.java");
	private static final Path MANAGER = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/worldgen/ChamberOfWillManager.java");
	private static final Path THEME_REGISTRY = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/world/chamberofwill/ChamberSkyThemeRegistry.java");
	private static final Path VESPER_EFFECTS = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/world/chamberofwill/VesperFightChamberEffects.java");
	private static final Path REFERENCE = Path.of("docs/HEMOMANCY_REFERENCE.md");

	private ChamberThemeCommandSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String command = Files.readString(COMMAND).replace("\r\n", "\n");
		String manager = Files.readString(MANAGER).replace("\r\n", "\n");
		String registry = Files.readString(THEME_REGISTRY).replace("\r\n", "\n");
		String vesperEffects = Files.readString(VESPER_EFFECTS).replace("\r\n", "\n");
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
				"ChamberOfWillManager.commandSkyThemes()");

		assertContains("manager keeps stable theme order", manager, "ORDERED_SKY_THEMES");
		assertContains("manager command theme order includes mnemonic lowtide", manager, "mnemonic_lowtide");
		assertContains("manager declares the Vesper fight preview theme", manager, "THEME_VESPER_FIGHT");
		assertContains("manager exposes separately settable themes", manager, "commandSkyThemes()");
		assertContains("manager syncs the Vesper floor preview", manager,
				"PacketSyncVesperFightScene.activate(cellPos(idFor(player.getUUID())))");
		String registryCompact = compact(registry);
		assertContains("registry owns a dedicated Vesper fight theme", registry,
				"ChamberSkyTheme.builder(ChamberOfWillManager.THEME_VESPER_FIGHT)");
		assertContains("Vesper fight starts from black", registryCompact,
				".skybox(0xFF000000, 0xFF000000)");
		assertContains("Vesper fight inherits no shared chamber layers", registryCompact,
				".layers(0, 0, 0, 0)");
		assertContains("Vesper fight inherits no cloud or nebula effects", registryCompact,
				".toggles(true, false, false, false)");
		assertContains("active ordeal selects the whole Vesper theme", registry,
				"VesperFightClientData.isActive()");
		assertContains("active effects follow the selected whole theme", registry,
				"effectsById(activeTheme().id())");
		assertContains("Vesper fight has an independent effects module", vesperEffects,
				"final class VesperFightChamberEffects extends AbstractChamberThemeEffects");
		assertContains("Vesper fight draws an untextured black environment", vesperEffects,
				"ChamberOfWillRenderHelpers.renderSolidBox");
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
