package com.vincenthuto.hemomancy.common.worldgen.structure.circus;

import com.vincenthuto.hemomancy.support.RepoPaths;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The pavilion tools/worldgen/circus_sites.py builds from the site kit (chunk 15 T8, D-29, D-42, PT-57): its ten
 * actors on open marks, the Ringmaster in his box on the upper gallery, two galleries whose stairs climb from the floor,
 * the three records it keeps, its own lamps and emitters, three wagons in the backyard, and nothing left of the old
 * wool tent.
 */
class CircusPavilionTemplateTest {
	private static final List<String> ACTORS = List.of("impaled_procession", "circus_ringmaster", "circus_fire_eater",
			"circus_acrobat", "circus_stilt_walker", "circus_knife_thrower", "circus_threadkeeper", "circus_understudy",
			"circus_strongman", "circus_beast_tamer");
	private static final List<String> OLD_TENT = List.of("minecraft:red_wool", "minecraft:blue_wool",
			"minecraft:dark_oak_planks", "hemomancy:circus_curtain", "hemomancy:puppeteers_wool");
	private static final List<String> FORBIDDEN = List.of("copper", "brass", "bronze", "gold", "gilded");

	private record Cell(String name, Map<String, String> props, CompoundTag nbt) {
	}

	private record Actor(String id, int x, int y, int z) {
	}

	@Test
	void theTemplateKeepsItsFootprint() throws Exception {
		var root = root();
		var size = root.getList("size", Tag.TAG_INT);
		assertEquals(List.of(81, 23, 65), List.of(size.getInt(0), size.getInt(1), size.getInt(2)));
	}

	@Test
	void everyActorStandsOnItsMarkInTheOpen() throws Exception {
		var cells = cells();
		var actors = actors();
		assertEquals(ACTORS.stream().map(id -> "hemomancy:" + id).sorted().toList(),
				actors.stream().map(Actor::id).sorted().toList());
		for (var actor : actors) {
			for (int up = 0; up < 2; up++) {
				var cell = cells.get(key(actor.x(), actor.y() + up, actor.z()));
				assertTrue(cell == null || cell.name().equals("minecraft:air"), actor + " is blocked by " + cell);
			}
		}
	}

	@Test
	void theRingmasterStandsInHisBoxOnTheUpperGallery() throws Exception {
		var cells = cells();
		var ringmaster = actors().stream().filter(a -> a.id().equals("hemomancy:circus_ringmaster")).findFirst().orElseThrow();
		assertEquals(8, ringmaster.y(), "he stands on the upper deck");
		var deck = cells.get(key(ringmaster.x(), ringmaster.y() - 1, ringmaster.z()));
		assertTrue(deck != null && deck.name().contains("planks"), "a deck under him: " + deck);
		var podium = cells.get(key(ringmaster.x(), ringmaster.y(), ringmaster.z() + 1));
		assertTrue(podium != null && podium.name().equals("hemomancy:circus_podium"), "his podium behind him: " + podium);
		var rail = cells.get(key(ringmaster.x(), ringmaster.y(), ringmaster.z() - 1));
		assertTrue(rail != null && rail.name().equals("hemomancy:circus_gallery_rail"), "the box's front rail: " + rail);
	}

	@Test
	void bothGalleriesHaveRailsAndStairsFromTheFloor() throws Exception {
		var railRows = new TreeSet<Integer>();
		var treads = new TreeSet<Integer>();
		for (var entry : cells().entrySet()) {
			int y = Integer.parseInt(entry.getKey().split(",")[1]);
			if (entry.getValue().name().equals("hemomancy:circus_gallery_rail")) railRows.add(y);
			if (entry.getValue().name().equals("hemomancy:blood_wood_stairs")
					&& "bottom".equals(entry.getValue().props().get("half"))) treads.add(y);
		}
		assertTrue(railRows.containsAll(List.of(6, 8)), "rails along the lower (5) and upper (7) decks: " + railRows);
		for (int y = 1; y <= 7; y++) assertTrue(treads.contains(y), "no stair tread at row " + y + ": " + treads);
	}

	@Test
	void thePavilionKeepsItsRecordsLampsAndEmitters() throws Exception {
		var counts = new HashMap<String, Integer>();
		var emitters = new ArrayList<CompoundTag>();
		for (var cell : cells().values()) {
			counts.merge(cell.name(), 1, Integer::sum);
			if (cell.name().equals("hemomancy:abocipher_emitter")) emitters.add(cell.nbt());
		}
		for (String record : List.of("impaled_procession_record", "circus_loft_record", "circus_quarters_record"))
			assertEquals(1, counts.getOrDefault("hemomancy:" + record, 0), record);
		// Six round the big top's ring and six round each side ring (2026-11-16).
		assertEquals(18, counts.getOrDefault("hemomancy:circus_footlight", 0), "the rings' footlights");
		assertTrue(counts.getOrDefault("hemomancy:hematic_lantern", 0) >= 20, "the pavilion's own lamps: " + counts.get("hemomancy:hematic_lantern"));
		assertEquals(3, emitters.size(), "three authored emitters");
		for (var nbt : emitters) assertEquals("harbinger_outpost", nbt.getString("Profile"));
	}

	@Test
	void theBackyardKeepsThreePaintedWagons() throws Exception {
		long hubs = cells().values().stream()
				.filter(cell -> cell.name().equals("hemomancy:circus_wagon_wheel") && "0".equals(cell.props().get("quarter")))
				.count();
		assertEquals(12, hubs, "three wagons on four wheels each (NQ-15-1 A)");
	}

	@Test
	void nothingOfTheOldTentOrAForbiddenMetalRemains() throws Exception {
		for (var cell : cells().values()) {
			assertFalse(OLD_TENT.contains(cell.name()), "old tent block " + cell.name());
			String path = cell.name().substring(cell.name().indexOf(':') + 1);
			assertFalse(FORBIDDEN.stream().anyMatch(path::contains), "forbidden metal " + cell.name());
		}
	}

	private static String key(int x, int y, int z) {
		return x + "," + y + "," + z;
	}

	private static CompoundTag root() throws Exception {
		return NbtIo.readCompressed(RepoPaths.resolve("src/main/resources/data/hemomancy/structure/crimson_troupe_pavilion.nbt"),
				NbtAccounter.unlimitedHeap());
	}

	private static List<Actor> actors() throws Exception {
		var actors = new ArrayList<Actor>();
		for (var tag : root().getList("entities", Tag.TAG_COMPOUND)) {
			var entity = (CompoundTag) tag;
			var pos = entity.getList("blockPos", Tag.TAG_INT);
			actors.add(new Actor(entity.getCompound("nbt").getString("id"), pos.getInt(0), pos.getInt(1), pos.getInt(2)));
		}
		return actors;
	}

	private static Map<String, Cell> cells() throws Exception {
		var root = root();
		var palette = root.getList("palette", Tag.TAG_COMPOUND);
		var cells = new HashMap<String, Cell>();
		for (var tag : root.getList("blocks", Tag.TAG_COMPOUND)) {
			var block = (CompoundTag) tag;
			var pos = block.getList("pos", Tag.TAG_INT);
			var state = palette.getCompound(block.getInt("state"));
			var props = new HashMap<String, String>();
			var properties = state.getCompound("Properties");
			for (String name : properties.getAllKeys()) props.put(name, properties.getString(name));
			cells.put(key(pos.getInt(0), pos.getInt(1), pos.getInt(2)),
					new Cell(state.getString("Name"), props, block.getCompound("nbt")));
		}
		return cells;
	}
}
