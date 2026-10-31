package com.vincenthuto.hemomancy.common.worldgen;

import com.google.gson.JsonObject;
import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.ResourceReader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/** Section 5: each Saint's mausoleum is a complete jigsaw chain that ends at that Saint's own tomb. */
class MausoleumComplexResourceTest {
	private record Placed(String name, CompoundTag properties, List<Integer> pos, CompoundTag nbt) {}

	private static List<Placed> blocks(String saint, String piece) throws IOException {
		Path file = RepoPaths.DATA.resolve("structure/mausoleum/" + saint + "/" + piece + ".nbt");
		CompoundTag root = NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap());
		ListTag palette = root.getList("palette", Tag.TAG_COMPOUND);
		List<Placed> placed = new ArrayList<>();
		for (Tag element : root.getList("blocks", Tag.TAG_COMPOUND)) {
			CompoundTag block = (CompoundTag) element;
			CompoundTag state = palette.getCompound(block.getInt("state"));
			ListTag pos = block.getList("pos", Tag.TAG_INT);
			placed.add(new Placed(state.getString("Name"), state.getCompound("Properties"),
					List.of(pos.getInt(0), pos.getInt(1), pos.getInt(2)), block.getCompound("nbt")));
		}
		return placed;
	}

	private static long count(List<Placed> placed, String name) {
		return placed.stream().filter(block -> block.name().equals(name)).count();
	}

	/** The Chapel kinds a Saint's complex tells, from its focus templates (beholding, litany, ...). */
	private static List<String> kinds(String saint) throws IOException {
		try (Stream<Path> files = Files.list(RepoPaths.DATA.resolve("structure/mausoleum/" + saint))) {
			return files.map(file -> file.getFileName().toString()).filter(name -> name.endsWith("_focus.nbt"))
					.map(name -> name.substring(0, name.length() - "_focus.nbt".length())).sorted().toList();
		}
	}

	@ParameterizedTest
	@ValueSource(strings = {"hemorath", "seraphae", "putriciel", "velorum"})
	void theChainRunsFromTheEntranceToTheSaintsOwnTomb(String saint) throws IOException {
		JsonObject structure = ResourceReader.data("worldgen/structure/mausoleum_" + saint + ".json");
		assertEquals("hemomancy:mausoleum/" + saint + "/top", structure.get("start_pool").getAsString());
		assertFalse(ResourceReader.data("tags/worldgen/biome/has_structure/mausoleum_" + saint + ".json")
				.getAsJsonArray("values").isEmpty(), "the Saint's mausoleum generates somewhere");

		List<Placed> chamber = blocks(saint, "chamber");
		Placed tomb = chamber.stream().filter(block -> block.name().equals("hemomancy:saint_sarcophagus")).findFirst()
				.orElseThrow();
		assertEquals(saint.toUpperCase(), tomb.nbt().getString("SaintType"));

		List<Placed> nave = blocks(saint, "nave");
		assertEquals(3, nave.stream().filter(block -> block.name().equals("minecraft:jigsaw")
				&& block.nbt().getString("pool").contains("/wing_")).count(), "three wings leave the nave");
		assertEquals(1, count(nave, "hemomancy:ring_door"), "one ring door closes the chamber");

		assertTrue(blocks(saint, "antechamber").stream().filter(block -> block.name().equals("minecraft:barrel"))
				.allMatch(block -> block.nbt().getString("LootTable").equals("hemomancy:chests/mausoleum")));

		Set<Integer> slots = new HashSet<>();
		List<String> kinds = kinds(saint);
		assertEquals(3, kinds.size(), "three Chapels");
		for (String kind : kinds) {
			List<Placed> foci = blocks(saint, kind + "_focus").stream()
					.filter(block -> block.name().equals("hemomancy:chapel_focus")).toList();
			assertEquals(1, foci.size(), kind + " has one focus");
			assertEquals(kind.toUpperCase(), foci.get(0).nbt().getString("Kind"));
			assertEquals(saint.toUpperCase(), foci.get(0).nbt().getString("Saint"));
			assertEquals("north", foci.get(0).properties().getString("facing"),
					"authored facing north: its marks turn with it");
			slots.add(foci.get(0).nbt().getInt("Slot"));
		}
		assertEquals(Set.of(0, 1, 2), slots, "one Chapel per ring-door node");
	}

	/**
	 * Track 1, issue 0. {@code JigsawPlacement} moves a rigid start piece so that {@code minY + 1} (its ground-level
	 * delta) equals {@code start_height + first free block}: template row r lands at
	 * {@code start_height + groundTop + r}, where groundTop is the ground's top block, and {@code beard_thin} levels
	 * the land around to that same block. Row 0 is the pavilion's footing, so it must land on groundTop (start_height
	 * 0); the step ring, the portico stair and the floor stand one course up, on the ground rather than half sunk into
	 * it. {@code MausoleumStructure} then lifts the whole complex onto the highest ground under the entrance.
	 * Changing the entrance template or the structure JSON without the other breaks this.
	 */
	@ParameterizedTest
	@ValueSource(strings = {"hemorath", "seraphae", "putriciel", "velorum"})
	void theEntranceStandsOnTheGround(String saint) throws IOException {
		CompoundTag top = NbtIo.readCompressed(RepoPaths.DATA.resolve("structure/mausoleum/" + saint + "/top.nbt"),
				NbtAccounter.unlimitedHeap());
		ListTag palette = top.getList("palette", Tag.TAG_COMPOUND);
		Map<List<Integer>, String> cells = new HashMap<>();
		for (Tag element : top.getList("blocks", Tag.TAG_COMPOUND)) {
			CompoundTag block = (CompoundTag) element;
			ListTag pos = block.getList("pos", Tag.TAG_INT);
			cells.put(List.of(pos.getInt(0), pos.getInt(1), pos.getInt(2)),
					palette.getCompound(block.getInt("state")).getString("Name"));
		}
		// The first cell inside the entrance gap in the west colonnade (x 0-2, z 7-9).
		int floorRow = -1;
		for (int y = 0; y < 3 && floorRow < 0; y++) {
			boolean solid = !cells.getOrDefault(List.of(3, y, 8), "minecraft:air").equals("minecraft:air");
			boolean open = cells.getOrDefault(List.of(3, y + 1, 8), "minecraft:air").equals("minecraft:air");
			if (solid && open) floorRow = y;
		}
		assertEquals(1, floorRow, "the entrance floor is one course over the footing row");
		for (int z = 0; z < 17; z++) {
			String footing = cells.getOrDefault(List.of(0, 0, z), "minecraft:air");
			String step = cells.getOrDefault(List.of(0, 1, z), "minecraft:air");
			if (step.endsWith("_stairs")) {
				assertFalse(footing.equals("minecraft:air") || footing.endsWith("_stairs"),
						"the step at z " + z + " stands on the footing, not in the ground's row");
			}
		}
		JsonObject structure = ResourceReader.data("worldgen/structure/mausoleum_" + saint + ".json");
		assertEquals(0, structure.getAsJsonObject("start_height").getAsJsonObject("value").get("absolute")
				.getAsInt(), "the footing row replaces the ground's top block");
		assertEquals("rigid", ResourceReader.data("worldgen/template_pool/mausoleum/" + saint + "/top.json")
				.getAsJsonArray("elements").get(0).getAsJsonObject().getAsJsonObject("element").get("projection")
				.getAsString(), "a rigid element's ground-level delta is the 1 the arithmetic assumes");
	}

	/**
	 * Track 1: the outer halls are an ossuary descent and a crypt gallery, not the old offering-gate trial. The gallery
	 * holds the only treasure barrel (behind its iron door), three opened loculi with ordinary loot, and both
	 * discovery inscriptions. Track 3: the gallery leads on to the spiral, the spiral to the threshold.
	 */
	@ParameterizedTest
	@ValueSource(strings = {"hemorath", "seraphae", "putriciel", "velorum"})
	void theOuterHallsAreTheDescentAndTheCryptGallery(String saint) throws IOException {
		assertEquals(List.of("hemomancy:mausoleum/" + saint + "/descent"), forwardPools(saint, "antechamber"));
		assertEquals(List.of("hemomancy:mausoleum/" + saint + "/gallery"), forwardPools(saint, "descent"));
		assertEquals(List.of("hemomancy:mausoleum/" + saint + "/descent_turn_0"), forwardPools(saint, "gallery"));
		assertEquals(List.of("hemomancy:mausoleum/" + saint + "/descent_turn_1"), forwardPools(saint, "descent_turn_0"));
		assertEquals(List.of("hemomancy:mausoleum/" + saint + "/threshold"), forwardPools(saint, "descent_turn_1"));
		assertFalse(Files.exists(RepoPaths.DATA.resolve("structure/mausoleum/" + saint + "/trialroom.nbt")));

		List<Placed> gallery = blocks(saint, "gallery");
		List<String> loot = gallery.stream().filter(block -> block.name().equals("minecraft:barrel"))
				.map(block -> block.nbt().getString("LootTable")).sorted().toList();
		assertEquals(List.of("hemomancy:chests/mausoleum", "hemomancy:chests/mausoleum", "hemomancy:chests/mausoleum",
				"hemomancy:chests/mausoleum_treasure"), loot);
		assertEquals(2, count(gallery, "hemomancy:hematic_iron_door"), "the treasure room's door, both halves");
		assertEquals(1, gallery.stream().filter(block -> block.name().endsWith("_button")).count(), "opens it from the aisle");
		assertEquals(1, gallery.stream().filter(block -> block.name().endsWith("_pressure_plate")).count(),
				"opens it from inside: nobody can dig or build their way out of consecrated stone");
		List<String> inscriptions = gallery.stream().filter(block -> block.name().endsWith("_inscription"))
				.map(block -> block.nbt().getString("InscriptionId")).sorted().toList();
		assertEquals(List.of("hemomancy:mausoleum/saints_echo", "hemomancy:mausoleum/sanguine_brotherhood_fragment"),
				inscriptions);

		try (Stream<Path> files = Files.list(RepoPaths.DATA.resolve("structure/mausoleum/" + saint))) {
			for (Path file : files.toList()) {
				String piece = file.getFileName().toString().replace(".nbt", "");
				for (String trial : List.of("hemomancy:offering_gate", "hemomancy:blood_trial_altar")) {
					assertEquals(0, count(blocks(saint, piece), trial), piece + " still places " + trial);
				}
			}
		}
	}

	/**
	 * Track 2: no seal block closes the inner complex. The threshold hall is an open corridor (x 1-13, y 1-5, z 3-7)
	 * with a line of polished venous stone across its floor at x=7, and a ward anchor buried in its north wall behind
	 * the line whose threshold slab is that corridor's x=7 cross-section (offsets from the anchor at (7, 3, 1)).
	 */
	@ParameterizedTest
	@ValueSource(strings = {"hemorath", "seraphae", "putriciel", "velorum"})
	void theThresholdHallIsHeldByItsWardAnchor(String saint) throws IOException {
		List<Placed> hall = blocks(saint, "threshold");
		Map<List<Integer>, String> cells = new HashMap<>();
		for (Placed block : hall) cells.put(block.pos(), block.name());

		List<Placed> anchors = hall.stream().filter(block -> block.name().equals("hemomancy:ward_anchor")).toList();
		assertEquals(1, anchors.size(), "one ward anchor per complex");
		Placed anchor = anchors.get(0);
		assertEquals(List.of(7, 3, 1), anchor.pos(), "buried in the north wall behind the line");
		assertEquals("east", anchor.properties().getString("facing"), "authored facing inward");
		assertEquals("hemomancy:ward_anchor", anchor.nbt().getString("id"));
		assertEquals(saint.toUpperCase(), anchor.nbt().getString("Saint"));
		assertEquals(Tag.TAG_INT_ARRAY, anchor.nbt().getTagType("ThresholdFrom"));
		assertEquals(Tag.TAG_INT_ARRAY, anchor.nbt().getTagType("ThresholdTo"));
		assertArrayEquals(new int[] {0, -2, 2}, anchor.nbt().getIntArray("ThresholdFrom"));
		assertArrayEquals(new int[] {0, 2, 6}, anchor.nbt().getIntArray("ThresholdTo"));

		for (int z = 3; z <= 7; z++) {
			assertEquals("hemomancy:polished_venous_stone", cells.get(List.of(7, 0, z)), "the ward line at z=" + z);
		}
		for (int x = 1; x <= 13; x++) {
			for (int y = 1; y <= 5; y++) {
				for (int z = 3; z <= 7; z++) {
					assertEquals("minecraft:air", cells.get(List.of(x, y, z)),
							"the corridor is open at " + x + "," + y + "," + z);
				}
			}
		}

		assertEquals(List.of("hemomancy:mausoleum/" + saint + "/nave"), forwardPools(saint, "threshold"));
		Placed naveEntry = blocks(saint, "nave").stream().filter(block -> block.name().equals("minecraft:jigsaw")
				&& block.nbt().getString("name").equals("hemomancy:mausoleum/nave_in")).findFirst().orElseThrow();
		assertEquals("minecraft:empty", naveEntry.nbt().getString("pool"), "a back connector grows nothing");
		assertEquals("hemomancy:mausoleum/threshold_out", naveEntry.nbt().getString("target"));
		assertFalse(Files.exists(RepoPaths.DATA.resolve("structure/mausoleum/" + saint + "/seal.nbt")));
		assertFalse(Files.exists(RepoPaths.DATA.resolve("worldgen/template_pool/mausoleum/" + saint + "/seal.json")));
	}

	/** A removed block (the old inner seal among them) would quietly place as air: every one placed must still exist. */
	@Test
	void everyHemomancyBlockATemplatePlacesStillExists() throws IOException {
		try (Stream<Path> files = Files.walk(RepoPaths.DATA.resolve("structure/mausoleum"))) {
			for (Path file : files.filter(path -> path.toString().endsWith(".nbt")).toList()) {
				CompoundTag root = NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap());
				for (Tag state : root.getList("palette", Tag.TAG_COMPOUND)) {
					String name = ((CompoundTag) state).getString("Name");
					if (!name.startsWith("hemomancy:")) continue;
					assertTrue(Files.exists(RepoPaths.ASSETS.resolve("blockstates/" + name.substring(10) + ".json")),
							file.getFileName() + " places " + name + ", which has no blockstate");
				}
			}
		}
	}

	/** The pools a piece's jigsaws lead on to; its back connector points at minecraft:empty. */
	private static List<String> forwardPools(String saint, String piece) throws IOException {
		return blocks(saint, piece).stream()
				.filter(block -> block.name().equals("minecraft:jigsaw"))
				.map(block -> block.nbt().getString("pool"))
				.filter(pool -> !pool.equals("minecraft:empty"))
				.toList();
	}

	/**
	 * Track 3. Vanilla drops a jigsaw child whose box meets a placed piece, silently, and reaches no further than
	 * {@code max_distance_from_center} or {@code size} deep. For every choice of approach in every wing, no two
	 * mandatory pieces meet, nothing lies past the size or the reach, and no optional niche (nor its fallback) could
	 * take room a mandatory piece needs.
	 */
	@ParameterizedTest
	@ValueSource(strings = {"hemorath", "seraphae", "putriciel", "velorum"})
	void noTwoPiecesOfAnyLayoutMeet(String saint) {
		JsonObject structure = ResourceReader.data("worldgen/structure/mausoleum_" + saint + ".json");
		int size = structure.get("size").getAsInt();
		int reach = structure.get("max_distance_from_center").getAsInt();
		assertTrue(reach + 12 <= 128, "max distance plus beard_thin's padding stays inside vanilla's 128");
		MausoleumLayout complex = new MausoleumLayout(saint);
		List<Map<String, String>> choices = complex.choices();
		assertEquals(8, choices.size(), "two approaches for each of three wings");
		for (Map<String, String> choice : choices) {
			MausoleumLayout.Layout layout = complex.layOut(choice);
			List<MausoleumLayout.Piece> pieces = layout.pieces();
			MausoleumLayout.Piece top = pieces.get(0);
			double cx = (top.min()[0] + top.max()[0]) / 2.0;
			double cz = (top.min()[2] + top.max()[2]) / 2.0;
			for (int a = 0; a < pieces.size(); a++) {
				MausoleumLayout.Piece one = pieces.get(a);
				assertTrue(one.depth() <= size, choice + ": " + one.name() + " lies past size " + size);
				assertTrue(Math.abs(one.min()[0] - cx) <= reach && Math.abs(one.max()[0] - cx) <= reach
						&& Math.abs(one.min()[2] - cz) <= reach && Math.abs(one.max()[2] - cz) <= reach
						&& Math.abs(one.min()[1] - top.min()[1]) <= reach, choice + ": " + one.name() + " lies past the reach");
				for (int b = a + 1; b < pieces.size(); b++) {
					assertFalse(one.meets(pieces.get(b)), choice + ": " + one.name() + " meets " + pieces.get(b).name());
				}
			}
			for (MausoleumLayout.Piece niche : layout.niches()) {
				assertTrue(niche.depth() <= size, choice + ": a niche lies past size " + size);
				for (MausoleumLayout.Piece piece : pieces) {
					assertFalse(niche.meets(piece), choice + ": niche " + niche.name() + " could take " + piece.name()
							+ "'s room");
				}
			}
		}
	}

	/**
	 * Track 3's shape. The spiral leaves under its way in, sixteen down; the nave lies under the outer halls; one wing
	 * leaves at the nave's floor, one from its gallery and one down a flight; the chamber is the lowest room; and the
	 * walk to the tomb changes level at least three times.
	 */
	@ParameterizedTest
	@ValueSource(strings = {"hemorath", "seraphae", "putriciel", "velorum"})
	void theComplexSpiralsDownAndFoldsBackUnderItself(String saint) {
		MausoleumLayout complex = new MausoleumLayout(saint);
		MausoleumLayout.Layout layout = complex.layOut(complex.choices().get(0));
		MausoleumLayout.Piece upper = layout.first("descent_turn_0");
		MausoleumLayout.Piece lower = layout.first("descent_turn_1");
		assertEquals(upper.min()[0], lower.min()[0]);
		assertEquals(upper.min()[2], lower.min()[2], "the second turn lies under the first");
		assertEquals(16, connectorHeight(upper, "descent_turn_in") - connectorHeight(lower, "descent_turn_out"),
				"two turns of eight");

		MausoleumLayout.Piece nave = layout.first("nave");
		MausoleumLayout.Piece gallery = layout.first("gallery");
		assertTrue(nave.max()[1] < gallery.min()[1], "the nave lies under the crypt gallery");
		assertTrue(nave.min()[0] <= gallery.max()[0] && gallery.min()[0] <= nave.max()[0], "turned back beneath it");

		int naveFloor = nave.min()[1];
		Map<Integer, MausoleumLayout.Piece> approaches = new HashMap<>();
		for (MausoleumLayout.Piece piece : layout.pieces()) {
			if (piece.name().startsWith("wing_")) approaches.put(Integer.parseInt(piece.name().substring(5, 6)), piece);
		}
		assertEquals(naveFloor, connectorHeight(approaches.get(0), "wing_in"), "slot 0 leaves at the nave's floor");
		assertTrue(connectorHeight(approaches.get(1), "wing_in") > naveFloor, "slot 1 leaves from the gallery");
		MausoleumLayout.Piece crypt = approaches.get(2);
		assertTrue(connectorHeight(crypt, "approach_out") < naveFloor, "slot 2 goes a flight down");
		int chamberFloor = layout.first("chamber").min()[1];
		for (MausoleumLayout.Piece piece : layout.pieces()) {
			assertTrue(chamberFloor <= piece.min()[1], "the chamber is the lowest room, not " + piece.name());
		}
		int top = layout.first("top").min()[1];
		Set<Integer> levels = new TreeSet<>();
		for (String piece : List.of("top", "antechamber", "gallery", "nave", "chamber")) {
			levels.add(layout.first(piece).min()[1] - top);
		}
		assertTrue(levels.size() >= 4, "the walk to the tomb changes level at least three times: " + levels);
	}

	private static int connectorHeight(MausoleumLayout.Piece piece, String name) {
		return piece.world(piece.template().jigsaws().stream().filter(jigsaw -> jigsaw.name().endsWith(name))
				.findFirst().orElseThrow().pos())[1];
	}

	/** Back connectors grow nothing (a child's way back would only meet its parent): only *_out and *_down lead on. */
	@ParameterizedTest
	@ValueSource(strings = {"hemorath", "seraphae", "putriciel", "velorum"})
	void onlyForwardConnectorsLeadOn(String saint) throws IOException {
		try (Stream<Path> files = Files.list(RepoPaths.DATA.resolve("structure/mausoleum/" + saint))) {
			for (Path file : files.toList()) {
				String piece = file.getFileName().toString().replace(".nbt", "");
				for (Placed block : blocks(saint, piece)) {
					if (!block.name().equals("minecraft:jigsaw")) continue;
					String name = block.nbt().getString("name");
					boolean forward = name.endsWith("_out") || name.endsWith("_down");
					assertEquals(forward, !block.nbt().getString("pool").equals("minecraft:empty"),
							piece + "'s " + name + " -> " + block.nbt().getString("pool"));
				}
			}
		}
	}

	/**
	 * An optional niche that cannot fit falls back to a blind arch, never a hole into rock; a mandatory piece never
	 * relies on a fallback (the assembly GameTest proves it is placed).
	 */
	@ParameterizedTest
	@ValueSource(strings = {"hemorath", "seraphae", "putriciel", "velorum"})
	void optionalNichesFallBackToAWall(String saint) throws IOException {
		try (Stream<Path> files = Files.list(RepoPaths.DATA.resolve("worldgen/template_pool/mausoleum/" + saint))) {
			for (Path file : files.toList()) {
				String pool = file.getFileName().toString().replace(".json", "");
				String fallback = ResourceReader.data("worldgen/template_pool/mausoleum/" + saint + "/" + pool + ".json")
						.get("fallback").getAsString();
				assertEquals(pool.equals("niche") ? "hemomancy:mausoleum/" + saint + "/niche_end" : "minecraft:empty",
						fallback, pool);
			}
		}
		assertEquals(List.of("niche_wall"), new MausoleumLayout(saint).elements("hemomancy:mausoleum/" + saint
				+ "/niche_end"));
	}

	/**
	 * Track 3: each trial plays across its wing. The focus's marks (offsets from it, authored facing north) are the
	 * kind's: four witnesses; a stone and six stands holding the six candidates; a gate and seals for three waves; a
	 * ring's centre and its pylons. Track 6: each lands, through the hall-to-focus connection, on its set-piece block,
	 * which the focus drives only when it finds that block at the mark.
	 */
	@ParameterizedTest
	@ValueSource(strings = {"hemorath", "seraphae", "putriciel", "velorum"})
	void everyChapelMarksItsWing(String saint) throws IOException {
		List<String> items = List.of("minecraft:bone", "minecraft:rotten_flesh", "minecraft:spider_eye",
				"minecraft:ink_sac", "minecraft:glowstone_dust", "minecraft:redstone");
		MausoleumLayout complex = new MausoleumLayout(saint);
		for (String kind : kinds(saint)) {
			MausoleumLayout.Template hallTemplate = complex.template(kind + "_hall");
			MausoleumLayout.Piece hall = MausoleumLayout.piece(hallTemplate, new int[3], 0, 0);
			MausoleumLayout.Jigsaw out = hallTemplate.jigsaws().stream().filter(j -> j.name().endsWith("hall_out"))
					.findFirst().orElseThrow();
			MausoleumLayout.Piece focusRoom = complex.attach(hall, out, complex.template(kind + "_focus"));
			assertEquals(0, focusRoom.rotation(), "the focus room continues its hall unturned");
			MausoleumLayout.Block focus = focusRoom.template().blocks().values().stream()
					.filter(block -> block.name().equals("hemomancy:chapel_focus")).findFirst().orElseThrow();
			int[] at = focusRoom.world(focus.pos());
			Map<String, List<Integer>> indices = new HashMap<>();
			for (Tag element : focus.nbt().getList("Marks", Tag.TAG_COMPOUND)) {
				CompoundTag mark = (CompoundTag) element;
				String markKind = mark.getString("Kind");
				int index = mark.getInt("Index");
				indices.computeIfAbsent(markKind, k -> new ArrayList<>()).add(index);
				int x = at[0] + mark.getInt("x"), y = at[1] + mark.getInt("y"), z = at[2] + mark.getInt("z");
				String where = saint + " " + kind + " " + markKind + " " + index + " at " + x + "," + y + "," + z;
				MausoleumLayout.Block here = hallTemplate.at(x, y, z);
				assertNotNull(here, where + " lies outside the hall");
				switch (markKind) {
					case "START", "CIRCLE" -> {
						assertEquals("minecraft:air", here.name(), where + " is open floor");
						assertFalse(hallTemplate.at(x, y - 1, z).name().equals("minecraft:air"), where + " has a floor");
					}
					case "WITNESS" -> {
						// Track 11: Velorum's witnesses are devotees of the Procession, the mark on the lower half.
						if (here.name().equals("hemomancy:witness_devotee")) {
							assertEquals("lower", here.properties().getString("half"), where);
							MausoleumLayout.Block upper = hallTemplate.at(x, y + 1, z);
							assertNotNull(upper, where + " has its upper half above");
							assertEquals("hemomancy:witness_devotee", upper.name(), where + " has its upper half above");
							assertEquals("upper", upper.properties().getString("half"), where);
							for (String key : List.of("facing", "lit", "turn")) {
								assertEquals(here.properties().getString(key), upper.properties().getString(key),
										where + " halves agree on " + key);
							}
						} else {
							assertEquals("hemomancy:witness_stone", here.name(), where);
						}
					}
					case "LITANY_STONE" -> {
						assertEquals("hemomancy:litany_stone", here.name(), where);
						assertEquals("lower", here.properties().getString("half"), where);
						MausoleumLayout.Block upper = hallTemplate.at(x, y + 1, z);
						assertEquals("hemomancy:litany_stone", upper.name(), where + " has its upper half above");
						assertEquals("upper", upper.properties().getString("half"), where);
					}
					case "OFFERING_STAND" -> {
						assertEquals("hemomancy:offering_stand", here.name(), where);
						assertEquals(items.get(index), here.nbt().getCompound("Item").getString("id"),
								where + " holds its candidate");
						assertEquals(4, here.nbt().getCompound("Item").getInt("count"), where + " holds a stack of four");
					}
					case "SPAWN" -> {
						assertEquals("hemomancy:spawn_mark", here.name(), where);
						assertEquals(String.valueOf(index), here.properties().getString("wave"), where + " answers its wave");
						assertFalse(hallTemplate.at(x, y - 1, z).name().equals("minecraft:air"), where + " has a floor");
					}
					case "PYLON" -> {
						assertEquals("hemomancy:pulse_pylon", here.name(), where);
						assertEquals("upper", here.properties().getString("part"), where + " is the pylon's head");
						MausoleumLayout.Block middle = hallTemplate.at(x, y - 1, z);
						MausoleumLayout.Block lower = hallTemplate.at(x, y - 2, z);
						assertEquals("hemomancy:pulse_pylon", middle.name(), where + " stands on its middle");
						assertEquals("middle", middle.properties().getString("part"), where);
						assertEquals("hemomancy:pulse_pylon", lower.name(), where + " stands on its foot");
						assertEquals("lower", lower.properties().getString("part"), where);
					}
					default -> fail(where + " is not a mark kind the trials know");
				}
			}
			switch (kind) {
				case "beholding" -> assertEquals(List.of(0, 1, 2, 3), sorted(indices.get("WITNESS")));
				case "litany" -> {
					assertEquals(1, indices.get("LITANY_STONE").size());
					assertEquals(List.of(0, 1, 2, 3, 4, 5), sorted(indices.get("OFFERING_STAND")));
				}
				case "downfall" -> {
					assertEquals(1, indices.get("START").size());
					assertEquals(List.of(0, 1, 2), sorted(indices.get("SPAWN")).stream().distinct().toList());
				}
				default -> {
					assertEquals(1, indices.get("CIRCLE").size());
					assertTrue(indices.get("PYLON").size() >= 3, "pylons around the ring");
				}
			}
		}
	}

	private static List<Integer> sorted(List<Integer> values) {
		return values.stream().sorted().toList();
	}

	/**
	 * Track 4: the chamber is the complex's great room and seats the 2 x 4 x 2 tomb. It is authored with the door in the
	 * west wall and the long axis along x. The sarcophagus faces west on the room's centre line; the cells its fillers
	 * grow into (x -1..2, y 0..1, z -1..1 from the main block) and the strip its lid slides over (z -2) are open; and
	 * the hall keeps a flat 15 x 15 floor with four blocks of air over it for the fight.
	 */
	@ParameterizedTest
	@ValueSource(strings = {"hemorath", "seraphae", "putriciel", "velorum"})
	void theChamberIsAGreatRoomThatSeatsTheTomb(String saint) {
		MausoleumLayout.Template chamber = new MausoleumLayout(saint).template("chamber");
		int[] size = chamber.size();
		assertTrue(size[0] >= 25 && size[1] >= 11 && size[2] >= 19,
				"at least 25 long, 11 tall and 19 wide: " + Arrays.toString(size));

		List<MausoleumLayout.Block> tombs = chamber.blocks().values().stream()
				.filter(block -> block.name().equals("hemomancy:saint_sarcophagus")).toList();
		assertEquals(1, tombs.size(), "one tomb");
		MausoleumLayout.Block tomb = tombs.get(0);
		assertEquals("west", tomb.properties().getString("facing"), "its foot toward the door");
		int[] at = tomb.pos();
		assertEquals(size[2] - 1, 2 * at[2], "on the room's centre line");
		for (int dx = -1; dx <= 2; dx++) {
			for (int dy = 0; dy <= 1; dy++) {
				for (int dz = -2; dz <= 1; dz++) {
					if (dx == 0 && dy == 0 && dz == 0) continue;
					assertTrue(isAir(chamber, at[0] + dx, at[1] + dy, at[2] + dz), (dz == -2 ? "the lid's strip" :
							"a tomb cell") + " is open at " + dx + "," + dy + "," + dz + " from the main block");
				}
			}
		}

		assertTrue(hasClearFloor(chamber, 15, 4), "a flat 15 x 15 floor with four blocks of air over it");
	}

	/**
	 * Track 5: the ring door is one multiblock, a 5 x 5 face one deep in the nave's east end wall. Its controller stands
	 * at the bottom centre facing west into the nave and grows its 24 fillers when the template is placed, so their
	 * cells are left open. The slab sinks into a floor track under the door; the track runs on under the chamber's
	 * west wall directly behind it (both connectors leave track, not a gap), where the open door's 3 x 4 passage leads
	 * through to the processional.
	 */
	@ParameterizedTest
	@ValueSource(strings = {"hemorath", "seraphae", "putriciel", "velorum"})
	void theRingDoorClosesTheNave(String saint) {
		MausoleumLayout complex = new MausoleumLayout(saint);
		MausoleumLayout.Template nave = complex.template("nave");
		int x = nave.size()[0] - 1;
		int zc = (nave.size()[2] - 1) / 2;
		List<MausoleumLayout.Block> doors = nave.blocks().values().stream()
				.filter(block -> block.name().equals("hemomancy:ring_door")).toList();
		assertEquals(1, doors.size(), "one ring door");
		MausoleumLayout.Block door = doors.get(0);
		assertArrayEquals(new int[] {x, 1, zc}, door.pos(), "the controller stands at the bottom centre of the east end");
		assertEquals("west", door.properties().getString("facing"), "its front faces the nave");
		assertEquals("false", door.properties().getString("open"));
		assertEquals("hemomancy:ring_door", door.nbt().getString("id"));
		assertEquals(saint.toUpperCase(), door.nbt().getString("Saint"));
		assertEquals(Tag.TAG_BYTE, door.nbt().getTagType("FillersPlaced"));
		assertFalse(door.nbt().getBoolean("FillersPlaced"), "its fillers grow when the template is placed");
		for (int y = 1; y <= 5; y++) {
			for (int dz = -2; dz <= 2; dz++) {
				if (y == 1 && dz == 0) continue;
				assertTrue(isAir(nave, x, y, zc + dz), "a filler cell is open at y " + y + ", z " + (zc + dz));
			}
		}

		MausoleumLayout.Template chamber = complex.template("chamber");
		MausoleumLayout.Jigsaw entry = chamber.jigsaws().stream().filter(j -> j.name().endsWith("chamber_in"))
				.findFirst().orElseThrow();
		int floor = entry.pos()[1];
		int czc = entry.pos()[2];
		assertEquals(0, entry.pos()[0], "the chamber's way in is its west wall, directly behind the door's column");
		List<String> track = trackRow(nave, x, 0, zc);
		assertEquals(track, trackRow(chamber, 0, floor, czc), "the chamber continues the nave's track");
		assertEquals(track.get(0), track.get(4), "accent stones at both ends");
		assertEquals(List.of(track.get(2), track.get(2), track.get(2)), track.subList(1, 4), "one track course");
		assertFalse(track.get(0).equals(track.get(2)), "the ends stand out from the course");
		assertFalse(track.get(2).equals(nave.at(x, 0, zc + 3).name()), "the track reads against the floor beside it");
		for (int y = floor + 1; y <= floor + 4; y++) {
			for (int dz = -1; dz <= 1; dz++) {
				assertTrue(isAir(chamber, 0, y, czc + dz), "the open door's passage at y " + y + ", z " + (czc + dz));
			}
			assertFalse(isAir(chamber, 0, y, czc - 2) || isAir(chamber, 0, y, czc + 2), "jambs behind the frame");
		}
		for (int dz = -1; dz <= 1; dz++) {
			assertFalse(isAir(chamber, 0, floor + 5, czc + dz), "a lintel behind the frame's top row");
		}
	}

	/** The five floor cells under a ring door's face, a connector read as the block it leaves behind. */
	private static List<String> trackRow(MausoleumLayout.Template template, int x, int y, int zc) {
		List<String> row = new ArrayList<>();
		for (int dz = -2; dz <= 2; dz++) {
			MausoleumLayout.Block block = template.at(x, y, zc + dz);
			assertNotNull(block, "a track cell at " + x + "," + y + "," + (zc + dz));
			row.add(block.name().equals("minecraft:jigsaw") ? block.nbt().getString("final_state") : block.name());
		}
		return row;
	}

	private static boolean isAir(MausoleumLayout.Template template, int x, int y, int z) {
		MausoleumLayout.Block block = template.at(x, y, z);
		return block != null && block.name().equals("minecraft:air");
	}

	/** Whether some side x side square of full floor blocks, all at one height, has headroom blocks of air over it. */
	private static boolean hasClearFloor(MausoleumLayout.Template template, int side, int headroom) {
		int[] size = template.size();
		for (int y = 0; y + headroom < size[1]; y++) {
			boolean[][] open = new boolean[size[0]][size[2]];
			for (int x = 0; x < size[0]; x++) {
				for (int z = 0; z < size[2]; z++) {
					MausoleumLayout.Block floor = template.at(x, y, z);
					boolean ok = floor != null && !floor.name().equals("minecraft:air")
							&& !floor.name().endsWith("_stairs") && !floor.name().endsWith("_slab");
					for (int h = 1; ok && h <= headroom; h++) ok = isAir(template, x, y + h, z);
					open[x][z] = ok;
				}
			}
			for (int x0 = 0; x0 + side <= size[0]; x0++) {
				square:
				for (int z0 = 0; z0 + side <= size[2]; z0++) {
					for (int x = x0; x < x0 + side; x++) {
						for (int z = z0; z < z0 + side; z++) {
							if (!open[x][z]) continue square;
						}
					}
					return true;
				}
			}
		}
		return false;
	}

	/** Every template of a Saint's complex, by name. */
	private static List<String> pieces(String saint) throws IOException {
		try (Stream<Path> files = Files.list(RepoPaths.DATA.resolve("structure/mausoleum/" + saint))) {
			return files.map(file -> file.getFileName().toString()).filter(name -> name.endsWith(".nbt"))
					.map(name -> name.substring(0, name.length() - ".nbt".length())).sorted().toList();
		}
	}

	/** Track 6: nothing in a mausoleum is an entity. The congregation is blocks, not kneeling armor stands. */
	@ParameterizedTest
	@ValueSource(strings = {"hemorath", "seraphae", "putriciel", "velorum"})
	void noTemplateHoldsAnEntity(String saint) throws IOException {
		for (String piece : pieces(saint)) {
			CompoundTag root = NbtIo.readCompressed(RepoPaths.DATA.resolve("structure/mausoleum/" + saint + "/" + piece
					+ ".nbt"), NbtAccounter.unlimitedHeap());
			assertFalse(root.contains("entities"), piece + " still carries an entities list");
			assertEquals(0, count(blocks(saint, piece), "minecraft:armor_stand"), piece + " places an armor stand");
		}
	}

	/**
	 * Track 6: Velorum's congregation is frozen devotees, both halves standing on the floor and turned alike: rows in
	 * the nave's vessel facing the ring door with the walk to it left clear, and the chamber's aisle alcoves and dais
	 * flanks.
	 */
	@Test
	void velorumsCongregationIsFrozenDevotees() {
		MausoleumLayout complex = new MausoleumLayout("velorum");
		for (String piece : List.of("nave", "chamber")) {
			MausoleumLayout.Template template = complex.template(piece);
			List<MausoleumLayout.Block> devotees = template.blocks().values().stream()
					.filter(block -> block.name().equals("hemomancy:frozen_devotee")).toList();
			List<MausoleumLayout.Block> lower = devotees.stream()
					.filter(block -> block.properties().getString("half").equals("lower")).toList();
			assertFalse(lower.isEmpty(), "devotees keep Velorum's " + piece);
			assertEquals(2 * lower.size(), devotees.size(), "every devotee in the " + piece + " has both halves");
			for (MausoleumLayout.Block devotee : lower) {
				int[] at = devotee.pos();
				String where = piece + " devotee at " + Arrays.toString(at);
				MausoleumLayout.Block upper = template.at(at[0], at[1] + 1, at[2]);
				assertNotNull(upper, where + " has an upper half");
				assertEquals("hemomancy:frozen_devotee", upper.name(), where + " has an upper half");
				assertEquals("upper", upper.properties().getString("half"), where);
				assertEquals(devotee.properties().getString("facing"), upper.properties().getString("facing"), where);
				assertEquals(devotee.properties().getString("pose"), upper.properties().getString("pose"), where);
				// Track 11: a turned head reaches into the lower cell for several poses, so both halves carry it.
				assertEquals(devotee.properties().getString("head"), upper.properties().getString("head"), where);
				assertFalse(isAir(template, at[0], at[1] - 1, at[2]), where + " kneels on the floor");
				if (piece.equals("nave")) {
					assertEquals("east", devotee.properties().getString("facing"), where + " faces the ring door");
				}
			}
		}
		MausoleumLayout.Template nave = complex.template("nave");
		int zc = (nave.size()[2] - 1) / 2;
		for (int x = 1; x < nave.size()[0] - 1; x++) {
			for (int z = zc - 1; z <= zc + 1; z++) {
				for (int y = 1; y <= 2; y++) {
					assertTrue(isAir(nave, x, y, z), "the walk to the ring door is clear at " + x + "," + y + "," + z);
				}
			}
		}
	}

	/**
	 * Track 6: a reliquary sconce hangs in an open cell on the wall behind it (opposite its facing). Velorum's lamps are
	 * rime lanterns recessed into the stone instead (Track 11), so his complex counts those as lit.
	 */
	@ParameterizedTest
	@ValueSource(strings = {"hemorath", "seraphae", "putriciel", "velorum"})
	void everySconceHangsOnAWall(String saint) throws IOException {
		MausoleumLayout complex = new MausoleumLayout(saint);
		int sconces = 0;
		for (String piece : pieces(saint)) {
			MausoleumLayout.Template template = complex.template(piece);
			for (MausoleumLayout.Block block : template.blocks().values()) {
				if (block.name().equals("hemomancy:rime_lantern")) sconces++;
				if (!block.name().equals("hemomancy:reliquary_sconce")) continue;
				sconces++;
				int[] at = block.pos();
				String facing = block.properties().getString("facing");
				int dx = facing.equals("east") ? 1 : facing.equals("west") ? -1 : 0;
				int dz = facing.equals("south") ? 1 : facing.equals("north") ? -1 : 0;
				String where = piece + " sconce at " + Arrays.toString(at) + " facing " + facing;
				assertTrue(dx != 0 || dz != 0, where + " faces a wall direction");
				MausoleumLayout.Block back = template.at(at[0] - dx, at[1], at[2] - dz);
				assertTrue(back != null && !back.name().equals("minecraft:air"), where + " has a wall behind it");
			}
		}
		assertTrue(sconces > 0, "the complex is lit by sconces or rime lanterns");
	}

	@ParameterizedTest
	@ValueSource(strings = {"hemorath", "seraphae", "putriciel", "velorum"})
	void everyJigsawPoolExists(String saint) throws IOException {
		try (Stream<Path> files = Files.list(RepoPaths.DATA.resolve("structure/mausoleum/" + saint))) {
			for (Path file : files.toList()) {
				String piece = file.getFileName().toString().replace(".nbt", "");
				for (Placed block : blocks(saint, piece)) {
					if (!block.name().equals("minecraft:jigsaw")) continue;
					String pool = block.nbt().getString("pool");
					if (pool.equals("minecraft:empty")) continue;
					assertTrue(Files.exists(RepoPaths.DATA.resolve("worldgen/template_pool/"
							+ pool.substring("hemomancy:".length()) + ".json")), piece + " -> " + pool);
				}
			}
		}
	}

	/**
	 * Track 7. Jigsaw pieces are placed with a known shape: vanilla never recomputes a stair's shape or a wall's sides,
	 * and quietly drops a property its block does not have (the gallery's iron door once carried a waterlogged it
	 * cannot hold). Every state a template places names only properties its block has, with values it accepts, and a
	 * stairs, slab or wall carries all of its own. Vanilla blocks go by shape family or by name; a Hemomancy block by
	 * the properties its blockstate switches on, plus the few a block entity renderer draws instead. The registry-backed
	 * twin is MausoleumAssemblyGameTests.everyTemplateStateIsARealState.
	 */
	@ParameterizedTest
	@ValueSource(strings = {"hemorath", "seraphae", "putriciel", "velorum"})
	void everyPlacedStateIsARealState(String saint) throws IOException {
		for (String piece : pieces(saint)) {
			Set<String> seen = new HashSet<>();
			for (Placed block : blocks(saint, piece)) {
				if (!seen.add(block.name() + block.properties())) continue;
				Map<String, Set<String>> table = expectedProperties(block.name());
				String where = saint + "/" + piece + " places " + block.name();
				assertNotNull(table, where + ", which has no blockstate");
				for (String key : block.properties().getAllKeys()) {
					assertTrue(table.containsKey(key), where + " with " + key + ", which it does not have");
					String value = block.properties().getString(key);
					Set<String> accepted = table.get(key);
					assertTrue(accepted.isEmpty() || accepted.contains(value), where + " with " + key + "=" + value);
				}
				if (SHAPED.stream().anyMatch(block.name()::endsWith) && !NOT_SHAPED.contains(block.name())) {
					assertEquals(table.keySet(), block.properties().getAllKeys(), where + " without all its state");
				}
			}
		}
	}

	private static final Set<String> BOOL = Set.of("true", "false");
	private static final Set<String> HORIZONTAL = Set.of("north", "east", "south", "west");
	private static final Set<String> WALL_SIDE = Set.of("none", "low", "tall");
	private static final Set<String> AXIS = Set.of("x", "y", "z");
	private static final List<String> SHAPED = List.of("_stairs", "_slab", "_wall");
	/** Hemomancy set pieces whose names happen to end like a shape family. */
	private static final Set<String> NOT_SHAPED = Set.of("hemomancy:ring_door");
	private static final Map<String, Map<String, Set<String>>> FAMILIES = Map.of(
			"_stairs", Map.of("facing", HORIZONTAL, "half", Set.of("top", "bottom"),
					"shape", Set.of("straight", "inner_left", "inner_right", "outer_left", "outer_right"),
					"waterlogged", BOOL),
			"_slab", Map.of("type", Set.of("top", "bottom", "double"), "waterlogged", BOOL),
			"_wall", Map.of("up", BOOL, "north", WALL_SIDE, "east", WALL_SIDE, "south", WALL_SIDE, "west", WALL_SIDE,
					"waterlogged", BOOL),
			"_button", Map.of("face", Set.of("floor", "wall", "ceiling"), "facing", HORIZONTAL, "powered", BOOL),
			"_pressure_plate", Map.of("powered", BOOL),
			"_door", Map.of("facing", HORIZONTAL, "half", Set.of("upper", "lower"), "hinge", Set.of("left", "right"),
					"open", BOOL, "powered", BOOL));
	private static final Map<String, Map<String, Set<String>>> VANILLA = Map.of(
			"minecraft:bone_block", Map.of("axis", AXIS),
			"minecraft:quartz_pillar", Map.of("axis", AXIS),
			"minecraft:hanging_roots", Map.of("waterlogged", BOOL),
			"minecraft:mangrove_roots", Map.of("waterlogged", BOOL),
			"minecraft:barrel", Map.of("facing", Set.of("north", "east", "south", "west", "up", "down"), "open", BOOL),
			"minecraft:jigsaw", Map.of("orientation", Set.of()));
	/** Properties a Hemomancy block may carry that no model switches on. */
	private static final Map<String, Set<String>> SILENT = Map.of("waterlogged", BOOL, "powered", BOOL);
	private static final Map<String, Map<String, Set<String>>> UNMODELLED = Map.of(
			"hemomancy:ring_door", Map.of("open", BOOL),
			"hemomancy:pulse_pylon", Map.of("charge", Set.of("0", "1", "2", "3")));

	/** The properties a block has and the values each accepts (empty: any), or null when it has no blockstate. */
	private static Map<String, Set<String>> expectedProperties(String name) {
		if (!NOT_SHAPED.contains(name)) {
			for (Map.Entry<String, Map<String, Set<String>>> family : FAMILIES.entrySet()) {
				if (name.endsWith(family.getKey())) return family.getValue();
			}
		}
		if (VANILLA.containsKey(name)) return VANILLA.get(name);
		if (!name.startsWith("hemomancy:")) return Map.of();
		String file = "blockstates/" + name.substring("hemomancy:".length()) + ".json";
		if (!Files.exists(RepoPaths.ASSETS.resolve(file))) return null;
		Map<String, Set<String>> table = new HashMap<>(SILENT);
		table.putAll(UNMODELLED.getOrDefault(name, Map.of()));
		JsonObject blockstate = ResourceReader.asset(file);
		if (blockstate.has("variants")) {
			for (String variant : blockstate.getAsJsonObject("variants").keySet()) {
				for (String pair : variant.split(",")) {
					if (pair.isEmpty()) continue;
					String[] kv = pair.split("=");
					table.computeIfAbsent(kv[0], key -> new TreeSet<>());
					table.put(kv[0], merge(table.get(kv[0]), Set.of(kv[1])));
				}
			}
		}
		if (blockstate.has("multipart")) {
			for (var part : blockstate.getAsJsonArray("multipart")) {
				JsonObject when = part.getAsJsonObject().getAsJsonObject("when");
				if (when == null) continue;
				List<JsonObject> clauses = new ArrayList<>();
				if (when.has("OR")) when.getAsJsonArray("OR").forEach(clause -> clauses.add(clause.getAsJsonObject()));
				else clauses.add(when);
				for (JsonObject clause : clauses) {
					for (String key : clause.keySet()) {
						table.put(key, merge(table.getOrDefault(key, Set.of()),
								Set.of(clause.get(key).getAsString().split("\\|"))));
					}
				}
			}
		}
		return table;
	}

	private static Set<String> merge(Set<String> a, Set<String> b) {
		Set<String> out = new TreeSet<>(a);
		out.addAll(b);
		return out;
	}
}
