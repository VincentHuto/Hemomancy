package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Step 6 (roadmap Phase 4): the small shared helpers in {@code common/util} are the only copies. A file may hold its
 * own version only when it is listed here with the reason it differs; stale entries fail.
 */
class SmallUtilityGuardrailTest {
	private static final String UTIL = "src/main/java/com/vincenthuto/hemomancy/common/util/";

	/** P-1 / G-5: attribute modifiers are held through {@code AttributeModifiers}, which skips unchanged values. */
	private static final Map<String, String> MODIFIER_WRITERS = Map.of(
			"src/main/java/com/vincenthuto/hemomancy/common/entity/projectile/VeinwingFeatherEntity.java",
			"Adds a one-shot max-health penalty once and clamps the target's health in the same step",
			"src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/scar/ScarDefinition.java",
			"Equip-time add-if-absent: keeping a held amount it did not write is the saved-modifier contract");
	private static final Pattern MODIFIER_WRITE = Pattern.compile("\\.add(?:Transient|Permanent)Modifier\\(");

	/** G-6: full armour sets are detected by {@code ArmorSets}. */
	private static final Pattern MATERIAL_COUNT = Pattern.compile("getMaterial\\(\\)\\.value\\(\\)\\s*==");
	private static final Pattern EXACT_CHEST = Pattern.compile("getItemBySlot\\(EquipmentSlot\\.CHEST\\)\\.is\\(");
	private static final Pattern EXACT_LEGS = Pattern.compile("getItemBySlot\\(EquipmentSlot\\.LEGS\\)\\.is\\(");

	/** L-7: block entities save and resync through {@code BlockEntitySync.markAndSync}. */
	private static final Map<String, String> SYNC_HELPERS = Map.of(
			"src/main/java/com/vincenthuto/hemomancy/common/tile/IBloodReservoir.java#sendUpdates",
			"The reservoir contract; its default delegates to BlockEntitySync",
			"src/main/java/com/vincenthuto/hemomancy/common/tile/IWhiteHumorTile.java#sendUpdates",
			"The white-humor contract; its default delegates to BlockEntitySync",
			"src/main/java/com/vincenthuto/hemomancy/common/tile/shared/FillerBlockEntity.java#sendUpdates",
			"Forwards to the multiblock's main reservoir",
			"src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/equipment/HarbingerEquipmentContainer.java#sync",
			"Resyncs the equipment attachment, not a block entity");
	private static final Pattern SYNC_HELPER = Pattern.compile(
			"void (sendUpdates|markDirtyAndSync|setChangedAndSync|sync)\\(\\)\\s*\\{");
	/** Immediate block-entity sends are kept only right before a menu opens, so the screen opens on current data. */
	private static final Set<String> IMMEDIATE_SENDS = Set.of(
			"src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/crafting/GhastlyAlembicBlock.java",
			"src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/crafting/MorphlingIncubatorBlock.java",
			"src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/crafting/MycelialCrucibleBlock.java",
			"src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/crafting/MycelialLanternBlock.java",
			"src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/crafting/ScarStationBlock.java",
			"src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/crafting/VialCentrifugeBlock.java",
			"src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/functional/MasonsEffigyBlock.java",
			"src/main/java/com/vincenthuto/hemomancy/common/block/unstained/crafting/PallidRetortBlock.java");
	private static final Pattern IMMEDIATE_SEND = Pattern.compile("VanillaPacketDispatcher\\.dispatchTEToNearbyPlayers\\(");

	/** O-3: block tickers are typed through {@code HemoTickers.create} (or vanilla's inherited helper). */
	private static final Pattern TICKER_COPY = Pattern.compile(
			"BlockEntityTicker<A> createTickerHelper\\(|\\(BlockEntityTicker<|==\\s*BlockEntityInit\\.\\w+\\.get\\(\\)\\s*\\?");

	@Test
	void blockEntitiesSyncOnlyThroughTheSharedHelper() throws IOException {
		Set<String> found = new TreeSet<>();
		try (Stream<Path> files = Files.walk(RepoPaths.MAIN_JAVA)) {
			for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
				var matcher = SYNC_HELPER.matcher(SourceAssertions.read(file));
				while (matcher.find()) found.add(RepoPaths.display(file) + "#" + matcher.group(1));
			}
		}
		assertEquals(new TreeSet<>(SYNC_HELPERS.keySet()), found,
				"call BlockEntitySync.markAndSync instead of declaring a sync helper, or list it here with a reason");
	}

	@Test
	void immediateBlockEntitySendsOnlyPrecedeAMenu() throws IOException {
		assertEquals(new TreeSet<>(IMMEDIATE_SENDS), filesMatching(text -> IMMEDIATE_SEND.matcher(text).find()),
				"BlockEntitySync already resends the block entity; an extra immediate send is only for opening a menu");
	}

	@Test
	void blockTickersAreTypedByHemoTickers() throws IOException {
		assertEquals(Set.of(), filesMatching(text -> TICKER_COPY.matcher(text).find(), UTIL + "HemoTickers.java"),
				"return HemoTickers.create(type, BlockEntityInit.x.get(), X::tick) from getTicker");
	}

	/** S-6 / D-3: a degree is compared through {@code ProgressionGates}, never against a literal. */
	private static final Pattern LITERAL_DEGREE = Pattern.compile(
			"(?:getDegreeNumber|getPlayerDegreeNumber)\\([^()]*(?:\\([^()]*\\)[^()]*)*\\)\\s*(?:>=|<=|==|!=|<|>)\\s*\\(?\\s*\\d");

	@Test
	void degreesAreComparedOnlyThroughProgressionGates() throws IOException {
		assertEquals(Set.of(), filesMatching(text -> LITERAL_DEGREE.matcher(text).find(), UTIL + "ProgressionGates.java"),
				"use ProgressionGates.hasDegree / isDegree instead of comparing a degree number with a literal");
	}

	/** L-4: pattern origins, orientation loops and match bounds come from {@code PatternOrigins}. */
	private static final Map<String, String> PATTERN_FRAMES = Map.of(
			"src/main/java/com/vincenthuto/hemomancy/common/succession/SuccessionRites.java",
			"Places the effigy body two blocks to the floor's right; not a pattern origin");
	private static final Pattern PATTERN_FRAME = Pattern.compile(
			"getNormal\\(\\)\\.cross\\(|== finger\\.getOpposite\\(\\)|getBlock\\(i, j, k\\)\\.getPos\\(\\);\\s*if \\(\\w+\\.getX\\(\\) <");

	@Test
	void patternOriginsAreSolvedOnlyByPatternOrigins() throws IOException {
		assertEquals(new TreeSet<>(PATTERN_FRAMES.keySet()),
				filesMatching(text -> PATTERN_FRAME.matcher(text).find(), UTIL + "PatternOrigins.java"),
				"use PatternOrigins (originForCell, offset, findContaining, scanAround, bounds) or list the file with a reason");
	}

	@Test
	void attributeModifiersAreWrittenOnlyThroughTheSharedHelper() throws IOException {
		assertEquals(new TreeSet<>(MODIFIER_WRITERS.keySet()),
				filesMatching(text -> MODIFIER_WRITE.matcher(text).find(), UTIL + "AttributeModifiers.java"),
				"hold modifiers with AttributeModifiers.sync (a no-op when unchanged) or list the file with a reason");
	}

	@Test
	void fullArmourSetsAreDetectedOnlyByArmorSets() throws IOException {
		assertEquals(Set.of(), filesMatching(text -> MATERIAL_COUNT.matcher(text).find()
						|| EXACT_CHEST.matcher(text).find() && EXACT_LEGS.matcher(text).find(), UTIL + "ArmorSets.java"),
				"use ArmorSets.isFullSet for armour-set checks");
	}

	static Set<String> filesMatching(Predicate<String> matches, String... exempt) throws IOException {
		Set<String> skipped = Set.of(exempt);
		Set<String> found = new TreeSet<>();
		try (Stream<Path> files = Files.walk(RepoPaths.MAIN_JAVA)) {
			for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
				String shown = RepoPaths.display(file);
				if (!skipped.contains(shown) && matches.test(SourceAssertions.read(file))) found.add(shown);
			}
		}
		return found;
	}
}
