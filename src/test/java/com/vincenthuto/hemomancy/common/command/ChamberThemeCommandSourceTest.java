package com.vincenthuto.hemomancy.common.command;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

public final class ChamberThemeCommandSourceTest {
	private static final Path COMMAND = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/command/HemoCommand.java");
	private static final Path MANAGER = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/worldgen/ChamberOfWillManager.java");
	private static final Path THEME_REGISTRY = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/world/chamberofwill/ChamberSkyThemeRegistry.java");
	private static final Path VESPER_EFFECTS = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/world/chamberofwill/VesperFightChamberEffects.java");
	private static final Path MYCOPHANT_EFFECTS = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/world/chamberofwill/MycophantNurseryChamberEffects.java");
	private static final Path REFERENCE = Path.of("docs/HEMOMANCY_REFERENCE.md");

	ChamberThemeCommandSourceTest() {
	}

	@Test
	void chamberThemeRegistrationContract() throws IOException {
		main(new String[0]);
	}

	@Test
	void chamberSizeIsRegisteredBesideTheme() throws IOException {
		String command = Files.readString(COMMAND).replace("\r\n", "\n");
		int chamber = command.indexOf("Commands.literal(\"chamber\")");
		int theme = command.indexOf("Commands.literal(\"theme\")", chamber);
		int size = command.indexOf("Commands.literal(\"size\")", chamber);
		if (chamber < 0 || theme < 0 || size < 0) {
			throw new AssertionError("chamber command branches are missing");
		}

		int themeDepth = parenthesisDepthAt(command, theme);
		int sizeDepth = parenthesisDepthAt(command, size);
		if (sizeDepth != themeDepth) {
			throw new AssertionError("size must be a direct child of chamber beside theme: theme depth "
					+ themeDepth + ", size depth " + sizeDepth);
		}
	}

	public static void main(String[] args) throws IOException {
		String command = Files.readString(COMMAND).replace("\r\n", "\n");
		String manager = Files.readString(MANAGER).replace("\r\n", "\n");
		String registry = Files.readString(THEME_REGISTRY).replace("\r\n", "\n");
		String vesperEffects = Files.readString(VESPER_EFFECTS).replace("\r\n", "\n");
		String mycophantEffects = Files.exists(MYCOPHANT_EFFECTS)
				? Files.readString(MYCOPHANT_EFFECTS).replace("\r\n", "\n") : "";
		String reference = Files.readString(REFERENCE).replace("\r\n", "\n");
		String commandCompact = compact(command);

		assertContains("command exposes chamber root", command, "Commands.literal(\"chamber\")");
		assertContains("command exposes theme branch", command, "Commands.literal(\"theme\")");
		assertContains("command exposes theme cycle", command, "Commands.literal(\"cycle\")");
		assertContains("command exposes theme next", command, "Commands.literal(\"next\")");
		assertContains("command exposes theme previous", command, "Commands.literal(\"previous\")");
		assertContains("command exposes theme set", command, "Commands.literal(\"set\")");
		assertContains("command exposes theme reset", command, "Commands.literal(\"reset\")");
		assertContains("command exposes chamber size", command, "Commands.literal(\"size\")");
		assertContains("command exposes chamber size set", command, "setChamberSize");
		assertContains("command exposes chamber size reset", command, "resetChamberSize");
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
		assertNotContains("command does not suggest namespaced theme IDs", command,
				"builder.suggest(id.toString())");

		assertContains("manager keeps stable theme order", manager, "ORDERED_SKY_THEMES");
		assertContains("manager command theme order includes mnemonic lowtide", manager, "mnemonic_lowtide");
		assertContains("manager declares the Vesper fight preview theme", manager, "THEME_VESPER_FIGHT");
		assertContains("manager exposes the Nursery as a command-selectable environment", compact(manager),
				"THEME_APOTHEOS, THEME_VESPER_FIGHT, THEME_MYCOPHANT_NURSERY");
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
		assertContains("registry owns a dedicated Nursery theme", registry,
				"ChamberSkyTheme.builder(ChamberOfWillManager.THEME_MYCOPHANT_NURSERY)");
		assertContains("registry routes Nursery rendering through its own effects module", registry,
				"new MycophantNurseryChamberEffects(mycophantNursery)");
		assertContains("active Mycophant ordeal wins environment priority", registry,
				"MycophantFightClientData.isActive()");
		assertContains("active effects follow the selected whole theme", registry,
				"effectsById(activeTheme().id())");
		assertContains("Vesper fight has an independent effects module", vesperEffects,
				"final class VesperFightChamberEffects extends AbstractChamberThemeEffects");
		assertContains("Vesper fight draws an untextured black environment", vesperEffects,
				"ChamberOfWillRenderHelpers.renderSolidBox");
		assertContains("Nursery has an independent effects module", mycophantEffects,
				"final class MycophantNurseryChamberEffects extends AbstractChamberThemeEffects");
		assertContains("manager stores testing overrides", manager, "skyThemeOverrides");
		assertContains("manager sets sky override", manager, "setSkyThemeOverride");
		assertContains("manager clears sky override", manager, "clearSkyThemeOverride");
		assertContains("manager cycles sky override", manager, "cycleSkyThemeOverride");
		assertContains("manager applies override during refresh", manager, "applySkyThemeOverride");
		assertContains("manager persists overrides", manager, "tag.put(\"skyThemeOverrides\", overrideList)");
		assertContains("manager loads overrides", manager, "tag.getList(\"skyThemeOverrides\", Tag.TAG_COMPOUND)");
		assertContains("manager stores chamber size overrides", manager, "chamberRadiusOverrides");
		assertContains("manager sets chamber size overrides", manager, "setChamberRadiusOverride");
		assertContains("manager clears chamber size overrides", manager, "clearChamberRadiusOverride");
		assertContains("manager persists chamber size overrides", manager,
				"tag.put(\"chamberRadiusOverrides\", radiusOverrideList)");
		assertContains("manager loads chamber size overrides", manager,
				"tag.getList(\"chamberRadiusOverrides\", Tag.TAG_COMPOUND)");

		assertContains("reference documents testing command", reference, "/hemo chamber theme cycle");
		assertContains("reference documents reset behavior", reference, "reset` clears the override");
		assertContains("reference documents chamber size set", reference,
				"/hemo chamber size set <radius> [player]");
		assertContains("reference documents chamber size reset", reference,
				"/hemo chamber size reset [player]");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}

	private static void assertNotContains(String label, String text, String unexpected) {
		if (text.contains(unexpected)) {
			throw new AssertionError(label + ": found " + unexpected);
		}
	}

	private static String compact(String text) {
		return text.replaceAll("\\s+", " ");
	}

	private static int parenthesisDepthAt(String text, int end) {
		int depth = 0;
		for (int i = 0; i < end; i++) {
			if (text.charAt(i) == '(') depth++;
			if (text.charAt(i) == ')') depth--;
		}
		return depth;
	}
}
