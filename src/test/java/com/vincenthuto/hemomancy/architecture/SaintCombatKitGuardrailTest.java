package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.common.entity.boss.saint.EnumSaintType;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintChamberLayout;
import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The Saint combat kit (Kit API, 2026-11-05 Saint overhauls design, section 21): a Saint moves, strikes, edits its
 * chamber, gathers players, takes held channels and ends only through the kit. The legacy pieces (goals, the shared
 * secondary cycle and its fixed patterns) were deleted at the compile gate on 2026-11-06 and may not come back. Rules 7
 * and 8's client half: one payload, one HUD layer, one footprint pass, and a style for every Saint in
 * {@code SaintClientStyles}.
 */
class SaintCombatKitGuardrailTest {
	private static final String SAINT = "common/entity/boss/saint/";
	/** The kit's own files, which own the pieces the rules keep out of the Saints. */
	private static final String TRIAL = "SaintTrialEntity.java";
	private static final String ARENA = "SaintArena.java";

	private static final Pattern LEGACY = Pattern.compile(
			"\\baddGoal\\(|\\bregisterTrialGoals\\(|\\btickSecondaryAttacks\\(|\\bsetVisualState\\(|\\bsendFloorRing\\(|\\bSaintAttackPattern\\b");
	private static final Pattern DAMAGE_OVERRIDE = Pattern.compile("\\bvoid\\s+(?:actuallyHurt|die)\\s*\\(");
	private static final Pattern RAW_DAMAGE = Pattern.compile(
			"damageSources\\(\\)\\.indirectMagic\\(|\\.generic\\(\\)|\\.mobAttack\\(");
	private static final Pattern BLOCK_EDIT = Pattern.compile("\\.(?:setBlock|setBlockAndUpdate|removeBlock|destroyBlock)\\s*\\(");
	private static final Pattern PLAYER_GATHER = Pattern.compile("getEntitiesOfClass\\(\\s*(?:Server)?Player\\.class");
	private static final Pattern FAKE_CHANNEL = Pattern.compile("isChannelingBloodAbsorption\\(|getUseItem\\(");
	private static final Pattern CANCEL = Pattern.compile("\\bcontroller(?:\\(\\))?\\.cancel\\(\\)");
	private static final Pattern MOVE_ENUM = Pattern.compile("\\benum\\s+\\w+\\s+implements\\s+[^{]*\\bSaintMove\\b");

	@Test
	void saintsUseTheKitNotTheLegacyPieces() throws Exception {
		assertEquals(Map.of(), new TreeMap<>(violations()), "keep these Saint files on the kit (Kit API rules 1 to 5)");
	}

	@Test
	void theChannelItemsNameOnlyTheRouter() {
		for (String item : List.of("common/item/harbinger/tool/living/BloodAbsorptionItem.java",
				"common/item/harbinger/tool/living/BloodProjectionItem.java",
				"common/item/harbinger/tool/living/LivingStaffItem.java")) {
			String text = SourceAssertions.readMainJava(item);
			SourceAssertions.assertContains(item + " asks the router", text, "SaintChannelRouter.");
			Matcher saint = Pattern.compile("entity\\.boss\\.saint\\.(\\w+)").matcher(text);
			while (saint.find()) assertEquals("SaintChannelRouter", saint.group(1), item + " names Saint code");
			for (String named : List.of("SaintTrialEntity", "SaintCompanion")) {
				SourceAssertions.assertNotContains(item + " leaves " + named + " to the router", text, named);
			}
		}
		String absorption = SourceAssertions.methodBody(SourceAssertions.readMainJava(
				"common/item/harbinger/tool/living/BloodAbsorptionItem.java"), "public static boolean isValidAbsorptionTarget(");
		SourceAssertions.assertContains("a Saint's body refuses the ordinary drain", absorption,
				"SaintChannelRouter.refusesOrdinaryDrain(entity)");
		String projection = SourceAssertions.readMainJava("common/item/harbinger/tool/living/BloodProjectionItem.java");
		int early = projection.indexOf("EarlyInitiation.tryProject(");
		int router = projection.indexOf("SaintChannelRouter.tryProject(");
		int will = projection.indexOf("WillBloodUtilityInteractions.tryProjectBanishFalteringWill(");
		assertTrue(early >= 0 && early < router && router < will,
				"the projection router runs after EarlyInitiation and before the Will, rite and block paths");
	}

	@Test
	void onlyTheEndingAndAnImmediatePhaseCancelTheActiveMove() throws Exception {
		Map<String, Integer> calls = new TreeMap<>();
		for (Path file : javaFiles(RepoPaths.MAIN_JAVA)) {
			Matcher matcher = CANCEL.matcher(SourceAssertions.read(file));
			int count = 0;
			while (matcher.find()) count++;
			if (count > 0) calls.put(RepoPaths.MAIN_JAVA.relativize(file).toString().replace('\\', '/'), count);
		}
		assertEquals(Map.of(SAINT + TRIAL, 1, SAINT + "SaintPhaseTable.java", 1), calls,
				"cancel the active move only from the ending and a Cut.IMMEDIATE phase change");
		String ending = SourceAssertions.methodBody(SourceAssertions.readMainJava(SAINT + TRIAL),
				"protected final void beginEnding(");
		SourceAssertions.assertContains("the ending cancels the move", ending, "controller.cancel()");
		String phases = SourceAssertions.readMainJava(SAINT + "SaintPhaseTable.java");
		SourceAssertions.assertContains("only an immediate cut cancels", phases,
				"if (pendingCut == SaintCombatController.Cut.IMMEDIATE && pending()) controller.cancel();");
	}

	@Test
	void everySaintMoveTableLivesInItsSaintsPackage() throws Exception {
		List<String> misplaced = new ArrayList<>();
		for (Path file : javaFiles(RepoPaths.MAIN_JAVA)) {
			String relative = RepoPaths.MAIN_JAVA.relativize(file).toString().replace('\\', '/');
			if (!MOVE_ENUM.matcher(SourceAssertions.read(file)).find()) continue;
			if (!relative.matches(SAINT.replace("/", "\\/") + "(hemorath|seraphae|putriciel|velorum)/\\w+\\.java")) {
				misplaced.add(relative);
			}
		}
		assertEquals(List.of(), misplaced, "a Saint's move enum lives in common/entity/boss/saint/<saint>/");
	}

	@Test
	void oneSaintPayloadOneHudLayerAndOneFootprintPass() throws Exception {
		Set<String> payloads = new TreeSet<>();
		for (Path file : javaFiles(RepoPaths.mainJava("common/network/boss"))) {
			String name = file.getFileName().toString();
			if (name.startsWith("Saint")) payloads.add(name);
		}
		assertEquals(Set.of("SaintMeterPacket.java"), payloads, "SaintMeterPacket is the kit's only payload (rule 7)");

		String registrar = SourceAssertions.readMainJava("client/registry/GuiLayerRegistrar.java");
		assertEquals(Set.of("SaintMeterLayer"), saintNames(registrar), "a Saint draws its HUD only through SaintMeterLayer");
		SourceAssertions.assertContains("the layer sits above the boss bars, behind the HUD rules", registrar,
				"event.registerAbove(VanillaGuiLayers.BOSS_OVERLAY, Hemomancy.rloc(\"saint_meter\"), hud(SaintMeterLayer::render))");

		String dispatcher = SourceAssertions.readMainJava("client/render/world/WorldEffectDispatcher.java");
		assertEquals(Set.of("SaintTelegraphRenderer"), saintNames(dispatcher),
				"SaintTelegraphRenderer is the only Saint footprint pass");
		int manipulation = dispatcher.indexOf("ManipulationVisualRenderer.render(event);");
		int telegraph = dispatcher.indexOf("SaintTelegraphRenderer.render(event);");
		assertTrue(manipulation >= 0 && telegraph > manipulation
						&& dispatcher.substring(manipulation, telegraph).split(";").length == 2,
				"the footprint pass runs straight after the manipulation visuals");

		Set<String> footprintDrawers = new TreeSet<>();
		for (Path file : javaFiles(RepoPaths.mainJava("client"))) {
			String text = SourceAssertions.read(file);
			if (!text.contains("BossFootprint") && !text.contains("TelegraphDraw") && !text.contains("HazardDraw")) continue;
			String name = file.getFileName().toString();
			boolean saintLane = name.matches("(Hemorath|Seraphae|Putriciel|Velorum)\\w*\\.java");
			if (!TELEGRAPH_KIT.contains(name) && !saintLane) footprintDrawers.add(RepoPaths.display(file));
		}
		assertEquals(Set.of(), footprintDrawers,
				"footprints are drawn only by the telegraph pass and the Saints' own styles and geometry");
	}

	@Test
	void everySaintHasBothClientStylesAndTheyExist() {
		String styles = SourceAssertions.readMainJava("client/registry/SaintClientStyles.java");
		for (EnumSaintType saint : EnumSaintType.values()) {
			String name = saint.name().charAt(0) + saint.name().substring(1).toLowerCase(java.util.Locale.ROOT);
			SourceAssertions.assertContains(saint + " has its row", styles, "STYLES.put(EnumSaintType." + saint.name()
					+ ", new Entry(new " + name + "MeterStyle(), new " + name + "TelegraphStyle(),");
			assertTrue(Files.isRegularFile(RepoPaths.mainJava("client/screen/overlay/boss/frame/" + name + "MeterStyle.java")),
					name + "MeterStyle exists in the frame package");
			assertTrue(Files.isRegularFile(RepoPaths.mainJava("client/render/world/" + name + "TelegraphStyle.java")),
					name + "TelegraphStyle exists in the world render package");
		}
	}

	@Test
	void theChamberLayoutMatchesTheGenerator() {
		String pieces = SourceAssertions.read("tools/worldgen/mausoleum/pieces.py");
		int[] size = tuple(pieces, "CHAMBER_L, CHAMBER_H, CHAMBER_W");
		assertArrayEquals(new int[]{SaintChamberLayout.LENGTH, SaintChamberLayout.HEIGHT, SaintChamberLayout.WIDTH}, size,
				"chamber size");
		assertEquals(SaintChamberLayout.ZC, scalar(pieces, "CHAMBER_ZC"), "CHAMBER_ZC");
		assertEquals(SaintChamberLayout.ENTRY, scalar(pieces, "CHAMBER_ENTRY"), "CHAMBER_ENTRY");
		assertArrayEquals(SaintChamberLayout.PILLARS, tuple(pieces, "CHAMBER_PILLARS"), "CHAMBER_PILLARS");
		assertEquals(SaintChamberLayout.RANK, scalar(pieces, "CHAMBER_RANK"), "CHAMBER_RANK");
		assertArrayEquals(SaintChamberLayout.BAYS, tuple(pieces, "CHAMBER_BAYS"), "CHAMBER_BAYS");
		assertEquals(SaintChamberLayout.AISLE, scalar(pieces, "CHAMBER_AISLE"), "CHAMBER_AISLE");
		assertArrayEquals(new int[]{SaintChamberLayout.CLEAR_MIN_X, SaintChamberLayout.CLEAR_MAX_X},
				tuple(pieces, "CHAMBER_CLEAR"), "CHAMBER_CLEAR");
		SourceAssertions.assertContains("the boss floor's half width", pieces,
				"z zc +- " + SaintChamberLayout.CLEAR_HALF);
		assertArrayEquals(SaintChamberLayout.STEPS, tuple(pieces, "CHAMBER_STEPS"), "CHAMBER_STEPS");
		assertEquals(SaintChamberLayout.DAIS, scalar(pieces, "CHAMBER_DAIS"), "CHAMBER_DAIS");
		assertEquals(SaintChamberLayout.BACK, scalar(pieces, "CHAMBER_BACK"), "CHAMBER_BACK");
		SourceAssertions.assertContains("the tomb's main block", pieces,
				"CHAMBER_TOMB = (" + SaintChamberLayout.TOMB_X + ", CHAMBER_DAIS + 1, CHAMBER_ZC)");
		assertEquals(SaintChamberLayout.DAIS + 1, SaintChamberLayout.TOMB_Y);
		assertEquals(SaintChamberLayout.ZC, SaintChamberLayout.TOMB_Z);
		SourceAssertions.assertContains("the tomb faces the door", pieces,
				"room.set(*CHAMBER_TOMB, 'hemomancy:saint_sarcophagus', {'facing': 'west'");
	}

	// ── helpers ──────────────────────────────────────────────────────

	/** The kit's own telegraph files, which may name footprints and draws. */
	private static final Set<String> TELEGRAPH_KIT = Set.of("SaintTelegraphRenderer.java", "SaintTelegraphPainter.java",
			"SaintTelegraphStyle.java", "TelegraphCanvas.java", "TelegraphDraw.java", "HazardDraw.java",
			"SaintTelegraphTiming.java");
	private static final Pattern SAINT_NAME = Pattern.compile("\\bSaint\\w*");

	/** The Saint-named identifiers a source mentions. */
	private static Set<String> saintNames(String text) {
		Set<String> names = new TreeSet<>();
		Matcher matcher = SAINT_NAME.matcher(text.replaceAll("(?s)/\\*.*?\\*/", "").replaceAll("(?m)//.*$", ""));
		while (matcher.find()) names.add(matcher.group());
		return names;
	}

	/** Saint file (relative to saint/) to the rules it breaks. */
	private static Map<String, List<String>> violations() throws Exception {
		Map<String, List<String>> found = new LinkedHashMap<>();
		Path root = RepoPaths.MAIN_JAVA.resolve(SAINT);
		for (Path file : javaFiles(root)) {
			String relative = root.relativize(file).toString().replace('\\', '/');
			String text = SourceAssertions.read(file);
			List<String> broken = new ArrayList<>();
			if (LEGACY.matcher(text).find()) broken.add("1 legacy goals, powers or patterns");
			if (!relative.equals(TRIAL) && DAMAGE_OVERRIDE.matcher(text).find()) broken.add("2 overrides actuallyHurt or die");
			// the trial's reward roll keeps a generic source as its fallback when no one is named
			if (!relative.equals(TRIAL) && RAW_DAMAGE.matcher(text).find()) broken.add("2 damage outside SaintMoves.hurt");
			if (!relative.equals(ARENA) && BLOCK_EDIT.matcher(text).find()) broken.add("3 block edits outside SaintArena");
			if (!relative.equals(ARENA) && PLAYER_GATHER.matcher(text).find()) broken.add("4 players gathered outside the arena");
			if (FAKE_CHANNEL.matcher(text).find()) broken.add("5 a channel faked from the held item");
			if (!broken.isEmpty()) found.put(relative, broken);
		}
		return found;
	}

	private static int scalar(String python, String name) {
		Matcher matcher = Pattern.compile("(?m)^" + Pattern.quote(name) + "\\s*=\\s*(\\d+)").matcher(python);
		assertTrue(matcher.find(), name + " in pieces.py");
		return Integer.parseInt(matcher.group(1));
	}

	private static int[] tuple(String python, String name) {
		Matcher matcher = Pattern.compile("(?m)^" + Pattern.quote(name) + "\\s*=\\s*\\(?([\\d,\\s]+)\\)?").matcher(python);
		assertTrue(matcher.find(), name + " in pieces.py");
		return Arrays.stream(matcher.group(1).split(",")).map(String::strip).filter(part -> !part.isEmpty())
				.mapToInt(Integer::parseInt).toArray();
	}

	private static List<Path> javaFiles(Path root) throws Exception {
		try (Stream<Path> files = Files.walk(root)) {
			return files.filter(path -> path.toString().endsWith(".java")).sorted().toList();
		}
	}
}
