package com.vincenthuto.hemomancy.common.worldgen.structure.circus;

import com.google.gson.JsonObject;
import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.ResourceReader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The Circus site kit (chunk 15 T7) on the sampler tools/worldgen/circus_sites.py writes: every set piece is on it,
 * nothing on it is in the copper family or gilt, every wagon wheel has its four quarters, and every set piece has its
 * blockstate, models, item model and name, painted at 16 texels a block.
 */
class CircusKitTemplateTest {
	static final List<String> PIECES = List.of("circus_ring_curb", "circus_seating",
			"circus_gallery_rail", "circus_trapeze", "circus_valance", "circus_guy_stake", "circus_footlight",
			"circus_crossbar_rig", "circus_spindle_rack", "circus_trophy", "circus_pelt_drape", "circus_cookpot",
			"circus_costume_rack", "circus_trunk", "circus_ticket_window", "circus_pennant_line", "circus_target_board",
			"circus_weight_frame", "circus_fire_pit", "circus_bellows_stand", "circus_hound_hoop", "circus_prompt_table",
			"circus_stage_record", "circus_loft_record", "circus_quarters_record", "impaled_procession_record",
			"circus_podium", "circus_knife_wheel", "circus_stilt_rack", "circus_king_pole", "circus_quarter_pole",
			"circus_canvas", "circus_laced_canvas", "circus_wainscot", "circus_sawdust", "circus_wagon_panel",
			"circus_wrecked_panel", "circus_plinth", "circus_net", "circus_wagon_wheel");
	private static final List<String> FORBIDDEN = List.of("copper", "brass", "bronze", "gold", "gilded");

	private record Cell(int x, int y, int z, String name, Map<String, String> props) {
	}

	@Test
	void theSamplerCarriesEverySetPiece() throws Exception {
		Set<String> placed = new HashSet<>();
		for (var cell : sampler()) placed.add(cell.name());
		for (String piece : PIECES) assertTrue(placed.contains("hemomancy:" + piece), "The sampler lacks " + piece);
	}

	@Test
	void nothingOnTheSamplerIsCopperOrGilt() throws Exception {
		for (var cell : sampler()) {
			String path = cell.name().substring(cell.name().indexOf(':') + 1);
			assertFalse(FORBIDDEN.stream().anyMatch(path::contains), "The kit places " + cell.name());
		}
	}

	@Test
	void everyWagonWheelHasItsFourQuarters() throws Exception {
		Map<String, Cell> byPos = new HashMap<>();
		var cells = sampler();
		for (var cell : cells) byPos.put(cell.x() + "," + cell.y() + "," + cell.z(), cell);
		int wheels = 0;
		for (var cell : cells) {
			if (!cell.name().equals("hemomancy:circus_wagon_wheel") || !cell.props().get("quarter").equals("0")) continue;
			wheels++;
			String facing = cell.props().get("facing");
			int[] along = switch (facing) {
				case "north" -> new int[] {1, 0};
				case "east" -> new int[] {0, 1};
				case "south" -> new int[] {-1, 0};
				default -> new int[] {0, -1};
			};
			for (int q = 1; q < 4; q++) {
				int a = q & 1, up = q >> 1;
				var other = byPos.get((cell.x() + along[0] * a) + "," + (cell.y() + up) + "," + (cell.z() + along[1] * a));
				assertTrue(other != null && other.name().equals(cell.name()) && other.props().get("facing").equals(facing)
						&& other.props().get("quarter").equals(Integer.toString(q)), "Wheel at " + cell + " lacks quarter " + q);
			}
		}
		assertTrue(wheels >= 7, "The sampler's wagons stand on their wheels: " + wheels);
	}

	@Test
	void everySetPieceHasItsAssetsAtSixteenTexelsABlock() throws Exception {
		var lang = ResourceReader.asset("lang/en_us.json");
		for (String piece : PIECES) {
			assertTrue(lang.has("block.hemomancy." + piece), "No name for " + piece);
			assertTrue(ResourceReader.exists("assets/hemomancy/models/item/" + piece + ".json"), "No item model for " + piece);
			JsonObject states = ResourceReader.asset("blockstates/" + piece + ".json");
			Set<String> models = new HashSet<>();
			for (var variant : states.getAsJsonObject("variants").entrySet())
				models.add(variant.getValue().getAsJsonObject().get("model").getAsString());
			for (String model : models) {
				String path = model.substring("hemomancy:".length());
				JsonObject json = ResourceReader.asset("models/" + path + ".json");
				var textures = json.getAsJsonObject("textures");
				for (var texture : textures.entrySet()) {
					String location = texture.getValue().getAsString();
					assertTrue(location.startsWith("hemomancy:block/"), piece + " paints with " + location);
					var image = ImageIO.read(RepoPaths.ASSETS.resolve("textures/" + location.substring("hemomancy:".length()) + ".png").toFile());
					if (json.has("texture_size")) {
						var size = json.getAsJsonArray("texture_size");
						assertEquals(image.getWidth(), size.get(0).getAsInt(), piece + ": one texel per model pixel");
						assertEquals(image.getHeight(), size.get(1).getAsInt(), piece + ": one texel per model pixel");
					} else {
						assertEquals(16, image.getWidth(), piece + " surface is 16 texels a block");
					}
				}
			}
		}
	}

	/** The rules vanilla's model loader enforces: an element inside -16..32 and a rotation of 0, 22.5 or 45 degrees. */
	@Test
	void everySetPieceModelLoads() throws Exception {
		Set<String> paths = new HashSet<>();
		for (String piece : PIECES) {
			paths.add("item/" + piece);
			for (var variant : ResourceReader.asset("blockstates/" + piece + ".json").getAsJsonObject("variants").entrySet())
				paths.add(variant.getValue().getAsJsonObject().get("model").getAsString().substring("hemomancy:".length()));
		}
		for (String path : paths) {
			JsonObject model = ResourceReader.asset("models/" + path + ".json");
			if (!model.has("elements")) continue;
			for (var element : model.getAsJsonArray("elements")) {
				var el = element.getAsJsonObject();
				for (String end : List.of("from", "to"))
					for (var v : el.getAsJsonArray(end))
						assertTrue(v.getAsDouble() >= -16 && v.getAsDouble() <= 32, path + " " + el.get("name") + " leaves -16..32");
				if (el.has("rotation")) {
					double angle = Math.abs(el.getAsJsonObject("rotation").get("angle").getAsDouble());
					assertTrue(angle == 0 || angle == 22.5 || angle == 45, path + " " + el.get("name") + " turns " + angle);
				}
			}
		}
	}

	private static List<Cell> sampler() throws Exception {
		CompoundTag root = NbtIo.readCompressed(
				RepoPaths.resolve("src/gameTest/resources/data/crimson_troupe_validation/structure/circus_kit_sampler.nbt"),
				NbtAccounter.unlimitedHeap());
		var palette = root.getList("palette", Tag.TAG_COMPOUND);
		var cells = new java.util.ArrayList<Cell>();
		for (var tag : root.getList("blocks", Tag.TAG_COMPOUND)) {
			var block = (CompoundTag) tag;
			var pos = block.getList("pos", Tag.TAG_INT);
			var state = palette.getCompound(block.getInt("state"));
			var props = new HashMap<String, String>();
			var properties = state.getCompound("Properties");
			for (String key : properties.getAllKeys()) props.put(key, properties.getString(key));
			cells.add(new Cell(pos.getInt(0), pos.getInt(1), pos.getInt(2), state.getString("Name"), props));
		}
		return cells;
	}
}
