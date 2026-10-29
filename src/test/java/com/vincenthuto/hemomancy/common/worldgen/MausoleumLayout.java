package com.vincenthuto.hemomancy.common.worldgen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.ResourceReader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A Saint's mausoleum laid out from its templates the way {@code JigsawPlacement} lays out rigid pieces: each child
 * turned the one way that faces its connector to the parent's, its connector on the block in front of the parent's.
 * Nothing is placed, so the layout of every choice of approach can be checked against vanilla's silent drop of a piece
 * whose box meets another.
 */
final class MausoleumLayout {
	static final List<String> HORIZONTAL = List.of("north", "east", "south", "west");

	record Jigsaw(int[] pos, String front, String top, String name, String target, String pool, String joint) {}

	record Block(String name, CompoundTag properties, int[] pos, CompoundTag nbt) {}

	record Template(String name, int[] size, List<Jigsaw> jigsaws, Map<List<Integer>, Block> blocks) {
		Block at(int x, int y, int z) {
			return blocks.get(List.of(x, y, z));
		}
	}

	/** A laid-out piece: its template, origin and quarter turns clockwise, inclusive box, and depth in the chain. */
	record Piece(Template template, int[] origin, int rotation, int[] min, int[] max, int depth) {
		String name() {
			return template.name();
		}

		boolean meets(int[] min2, int[] max2) {
			for (int i = 0; i < 3; i++) if (min[i] > max2[i] || min2[i] > max[i]) return false;
			return true;
		}

		boolean meets(Piece other) {
			return meets(other.min, other.max);
		}

		int[] world(int[] local) {
			int[] turned = turn(rotation, local);
			return new int[] {origin[0] + turned[0], origin[1] + turned[1], origin[2] + turned[2]};
		}
	}

	record Layout(List<Piece> pieces, List<Piece> niches) {
		Piece first(String name) {
			return pieces.stream().filter(piece -> piece.name().equals(name)).findFirst().orElseThrow();
		}
	}

	private final String saint;
	private final Map<String, Template> templates = new HashMap<>();

	MausoleumLayout(String saint) {
		this.saint = saint;
	}

	Template template(String name) {
		return templates.computeIfAbsent(name, this::load);
	}

	private Template load(String name) {
		try {
			CompoundTag root = NbtIo.readCompressed(RepoPaths.DATA.resolve("structure/mausoleum/" + saint + "/" + name
					+ ".nbt"), NbtAccounter.unlimitedHeap());
			ListTag size = root.getList("size", Tag.TAG_INT);
			ListTag palette = root.getList("palette", Tag.TAG_COMPOUND);
			List<Jigsaw> jigsaws = new ArrayList<>();
			Map<List<Integer>, Block> blocks = new HashMap<>();
			for (Tag element : root.getList("blocks", Tag.TAG_COMPOUND)) {
				CompoundTag block = (CompoundTag) element;
				CompoundTag state = palette.getCompound(block.getInt("state"));
				ListTag pos = block.getList("pos", Tag.TAG_INT);
				int[] at = {pos.getInt(0), pos.getInt(1), pos.getInt(2)};
				Block placed = new Block(state.getString("Name"), state.getCompound("Properties"), at,
						block.getCompound("nbt"));
				blocks.put(List.of(at[0], at[1], at[2]), placed);
				if (placed.name().equals("minecraft:jigsaw")) {
					String[] orientation = placed.properties().getString("orientation").split("_");
					CompoundTag nbt = placed.nbt();
					jigsaws.add(new Jigsaw(at, orientation[0], orientation[1], nbt.getString("name"),
							nbt.getString("target"), nbt.getString("pool"), nbt.getString("joint")));
				}
			}
			return new Template(name, new int[] {size.getInt(0), size.getInt(1), size.getInt(2)}, jigsaws, blocks);
		} catch (IOException exception) {
			throw new UncheckedIOException(exception);
		}
	}

	/** The element names of a pool, weighted elements first as listed. */
	List<String> elements(String pool) {
		JsonArray elements = ResourceReader.data("worldgen/template_pool/" + pool.substring("hemomancy:".length())
				+ ".json").getAsJsonArray("elements");
		List<String> names = new ArrayList<>();
		for (JsonElement element : elements) {
			String location = element.getAsJsonObject().getAsJsonObject("element").get("location").getAsString();
			names.add(location.substring(location.lastIndexOf('/') + 1));
		}
		return names;
	}

	/**
	 * Lays the complex out with the given element chosen for each multi-element mandatory pool (by pool name, e.g.
	 * {@code wing_0 -> wing_0_b}); single-element pools take their element. Optional niches are laid out with every
	 * element they could become, recorded separately.
	 */
	Layout layOut(Map<String, String> chosen) {
		List<Piece> placed = new ArrayList<>();
		List<Piece> niches = new ArrayList<>();
		List<Piece> queue = new ArrayList<>();
		Template top = template("top");
		queue.add(piece(top, new int[3], 0, 0));
		while (!queue.isEmpty()) {
			Piece parent = queue.remove(0);
			placed.add(parent);
			for (Jigsaw jigsaw : parent.template().jigsaws()) {
				if (jigsaw.pool().equals("minecraft:empty")) continue;
				String pool = jigsaw.pool().substring(jigsaw.pool().lastIndexOf('/') + 1);
				if (pool.equals("niche")) {
					List<String> could = new ArrayList<>(elements(jigsaw.pool()));
					could.addAll(elements("hemomancy:mausoleum/" + saint + "/niche_end"));
					for (String name : could) niches.add(attach(parent, jigsaw, template(name)));
					continue;
				}
				List<String> options = elements(jigsaw.pool());
				String child = options.size() == 1 ? options.get(0) : chosen.get(pool);
				queue.add(attach(parent, jigsaw, template(child)));
			}
		}
		return new Layout(placed, niches);
	}

	/** Every way of choosing one element for each multi-element mandatory pool the complex reaches. */
	List<Map<String, String>> choices() {
		List<Map<String, String>> out = new ArrayList<>();
		out.add(new HashMap<>());
		for (int slot = 0; slot < 3; slot++) {
			String pool = "wing_" + slot;
			List<Map<String, String>> next = new ArrayList<>();
			for (Map<String, String> partial : out) {
				for (String element : elements("hemomancy:mausoleum/" + saint + "/" + pool)) {
					Map<String, String> more = new HashMap<>(partial);
					more.put(pool, element);
					next.add(more);
				}
			}
			out = next;
		}
		return out;
	}

	Piece attach(Piece parent, Jigsaw connector, Template child) {
		String front = turn(parent.rotation(), connector.front());
		int[] step = step(front);
		int[] at = parent.world(connector.pos());
		at = new int[] {at[0] + step[0], at[1] + step[1], at[2] + step[2]};
		String facing = opposite(front);
		for (int rotation = 0; rotation < 4; rotation++) {
			for (Jigsaw jigsaw : child.jigsaws()) {
				if (!jigsaw.name().equals(connector.target()) || !turn(rotation, jigsaw.front()).equals(facing)) continue;
				boolean vertical = front.equals("up") || front.equals("down");
				if (vertical && connector.joint().equals("aligned")
						&& !turn(rotation, jigsaw.top()).equals(turn(parent.rotation(), connector.top()))) continue;
				int[] turned = turn(rotation, jigsaw.pos());
				return piece(child, new int[] {at[0] - turned[0], at[1] - turned[1], at[2] - turned[2]}, rotation,
						parent.depth() + 1);
			}
		}
		throw new AssertionError(child.name() + " has no connector facing " + connector.name());
	}

	static Piece piece(Template template, int[] origin, int rotation, int depth) {
		int[] min = {Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE};
		int[] max = {Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};
		int[] size = template.size();
		for (int x : new int[] {0, size[0] - 1}) {
			for (int y : new int[] {0, size[1] - 1}) {
				for (int z : new int[] {0, size[2] - 1}) {
					int[] corner = turn(rotation, new int[] {x, y, z});
					for (int i = 0; i < 3; i++) {
						min[i] = Math.min(min[i], origin[i] + corner[i]);
						max[i] = Math.max(max[i], origin[i] + corner[i]);
					}
				}
			}
		}
		return new Piece(template, origin, rotation, min, max, depth);
	}

	/** StructureTemplate.transform about the origin: each quarter turn clockwise takes (x, z) to (-z, x). */
	static int[] turn(int rotation, int[] pos) {
		int x = pos[0], z = pos[2];
		for (int i = 0; i < rotation; i++) {
			int was = x;
			x = -z;
			z = was;
		}
		return new int[] {x, pos[1], z};
	}

	static String turn(int rotation, String direction) {
		int index = HORIZONTAL.indexOf(direction);
		return index < 0 ? direction : HORIZONTAL.get((index + rotation) % 4);
	}

	static String opposite(String direction) {
		return switch (direction) {
			case "up" -> "down";
			case "down" -> "up";
			default -> turn(2, direction);
		};
	}

	static int[] step(String direction) {
		return switch (direction) {
			case "north" -> new int[] {0, 0, -1};
			case "south" -> new int[] {0, 0, 1};
			case "east" -> new int[] {1, 0, 0};
			case "west" -> new int[] {-1, 0, 0};
			case "up" -> new int[] {0, 1, 0};
			default -> new int[] {0, -1, 0};
		};
	}
}
