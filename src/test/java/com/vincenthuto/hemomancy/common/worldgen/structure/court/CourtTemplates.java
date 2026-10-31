package com.vincenthuto.hemomancy.common.worldgen.structure.court;

import com.vincenthuto.hemomancy.support.RepoPaths;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Brined Court's saved templates as the Seat and outpost template tests read them: cells by position, with each
 * cell's block, properties and block-entity NBT, and the shared sea check (no air under the waterline that water, a
 * waterlogged block or the world's sea beside or above it could reach).
 */
final class CourtTemplates {
	static final List<String> FORBIDDEN = List.of("copper", "torch", "lantern", "campfire", "candle", "fire", "soul_");

	record Cell(String name, Map<String, String> props, CompoundTag nbt) {
		boolean wet() {
			return name.equals("minecraft:water") || "true".equals(props.get("waterlogged"));
		}
	}

	record Template(int[] size, Map<Long, Cell> cells) {
		Cell at(int x, int y, int z) {
			return cells.get(key(x, y, z));
		}

		/** The position of the jigsaw with this name. */
		int[] jigsaw(String name) {
			for (var entry : cells.entrySet()) {
				var cell = entry.getValue();
				if (cell.name().equals("minecraft:jigsaw") && cell.nbt().getString("name").equals(name)) return unkey(entry.getKey());
			}
			throw new AssertionError("No jigsaw " + name);
		}

		long count(String block) {
			return cells.values().stream().filter(c -> c.name().equals(block)).count();
		}

		/**
		 * Air cells at or under {@code waterline} that water could reach: a wet or void neighbour on the four sides or
		 * above (void under the waterline is the world's sea; past the top it is sea unless {@code openTop}).
		 */
		List<String> leaks(int waterline, boolean openTop) {
			var leaks = new ArrayList<String>();
			for (var entry : cells.entrySet()) {
				if (!entry.getValue().name().equals("minecraft:air")) continue;
				int[] p = unkey(entry.getKey());
				if (p[1] > waterline) continue;
				for (int[] d : new int[][] {{1, 0, 0}, {-1, 0, 0}, {0, 0, 1}, {0, 0, -1}, {0, 1, 0}}) {
					int x = p[0] + d[0], y = p[1] + d[1], z = p[2] + d[2];
					boolean wet;
					if (x < 0 || z < 0 || x >= size[0] || z >= size[2]) wet = y <= waterline;
					else if (y >= size[1]) wet = !openTop && y <= waterline;
					else {
						var other = at(x, y, z);
						wet = other == null ? y <= waterline : !other.name().equals("minecraft:jigsaw") && other.wet();
					}
					if (wet) leaks.add(p[0] + "," + p[1] + "," + p[2]);
				}
			}
			return leaks;
		}
	}

	private CourtTemplates() {
	}

	/** A template under data/hemomancy/structure/, e.g. {@code brined_court/still_bell/bell}. */
	static Template load(String path) throws Exception {
		var root = NbtIo.readCompressed(RepoPaths.DATA.resolve("structure/" + path + ".nbt"), NbtAccounter.unlimitedHeap());
		var sizeTag = root.getList("size", Tag.TAG_INT);
		int[] size = {sizeTag.getInt(0), sizeTag.getInt(1), sizeTag.getInt(2)};
		var palette = root.getList("palette", Tag.TAG_COMPOUND);
		var cells = new HashMap<Long, Cell>();
		for (var tag : root.getList("blocks", Tag.TAG_COMPOUND)) {
			var block = (CompoundTag) tag;
			var pos = block.getList("pos", Tag.TAG_INT);
			var state = palette.getCompound(block.getInt("state"));
			var props = new HashMap<String, String>();
			var properties = state.getCompound("Properties");
			for (String key : properties.getAllKeys()) props.put(key, properties.getString(key));
			cells.put(key(pos.getInt(0), pos.getInt(1), pos.getInt(2)),
					new Cell(state.getString("Name"), props, block.contains("nbt") ? block.getCompound("nbt") : null));
		}
		return new Template(size, cells);
	}

	static long key(int x, int y, int z) {
		return ((long) x << 40) | ((long) (y & 0xFFFFF) << 20) | (z & 0xFFFFF);
	}

	static int[] unkey(long key) {
		return new int[] {(int) (key >> 40), (int) ((key >> 20) & 0xFFFFF), (int) (key & 0xFFFFF)};
	}
}
