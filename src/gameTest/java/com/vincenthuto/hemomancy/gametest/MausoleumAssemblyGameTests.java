package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.entity.boss.saint.EnumSaintType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Vanilla drops a jigsaw branch that does not fit without a word, so a complex can lose a Chapel or its tomb and still
 * generate. Each Saint's complex is laid out from its start pool for four seeds; every mandatory piece must be there.
 * The layout is taken high above the test floor because nothing is placed: only the pieces' boxes meet each other.
 */
@GameTestHolder("registration_validation")
@PrefixGameTestTemplate(false)
public final class MausoleumAssemblyGameTests {
	/** The spine from the entrance to the tomb, by template name (Track 3). */
	static final List<String> SPINE = List.of("top", "stairs", "antechamber", "descent", "gallery", "descent_turn_0",
			"descent_turn_1", "threshold", "nave", "chamber");
	private static final long[] SEEDS = {11L, 29L, 42L, 97L};

	private MausoleumAssemblyGameTests() {
	}

	@GameTest(templateNamespace = "registration_validation", template = "empty")
	public static void everyComplexKeepsItsMandatoryPieces(GameTestHelper helper) {
		BlockPos surface = helper.absolutePos(BlockPos.ZERO).atY(160);
		List<String> problems = new ArrayList<>();
		for (EnumSaintType saint : EnumSaintType.values()) {
			for (long seed : SEEDS) {
				var layout = MausoleumAssembly.layout(helper.getLevel(), saint, surface, seed);
				if (layout.isEmpty()) {
					problems.add(saint + "/" + seed + ": no start piece");
					continue;
				}
				for (String piece : missing(layout.get().byTemplate())) problems.add(saint + "/" + seed + ": no " + piece);
			}
		}
		helper.assertTrue(problems.isEmpty(), "Mandatory mausoleum pieces were dropped: " + problems);
		helper.succeed();
	}

	/**
	 * The mandatory pieces a laid-out complex lacks: the spine, an approach for each of the three wings (either layout),
	 * and three trial halls each with its focus room.
	 */
	static List<String> missing(Map<String, ?> byTemplate) {
		List<String> missing = new ArrayList<>();
		for (String piece : SPINE) if (!byTemplate.containsKey(piece)) missing.add(piece);
		for (int slot = 0; slot < 3; slot++) {
			String prefix = "wing_" + slot + "_";
			if (byTemplate.keySet().stream().noneMatch(name -> name.startsWith(prefix))) missing.add("wing_" + slot);
		}
		List<String> halls = byTemplate.keySet().stream().filter(name -> name.endsWith("_hall")).toList();
		if (halls.size() != 3) missing.add("three trial halls, found " + halls);
		for (String hall : halls) {
			String focus = hall.substring(0, hall.length() - "_hall".length()) + "_focus";
			if (!byTemplate.containsKey(focus)) missing.add(focus);
		}
		return missing;
	}

	/**
	 * Track 7: every state a Saint's templates place is a real state of a registered block. Jigsaw pieces are placed
	 * with a known shape, so nothing corrects a stair's shape or a wall's sides, and a property its block lacks (or a
	 * value it does not accept) is dropped without a word. The JVM twin is
	 * MausoleumComplexResourceTest.everyPlacedStateIsARealState.
	 */
	@GameTest(templateNamespace = "registration_validation", template = "empty")
	public static void everyTemplateStateIsARealState(GameTestHelper helper) {
		var resources = helper.getLevel().getServer().getResourceManager()
				.listResources("structure/mausoleum", path -> path.getPath().endsWith(".nbt"));
		List<String> problems = new ArrayList<>();
		int templates = 0;
		for (var entry : resources.entrySet()) {
			ResourceLocation file = entry.getKey();
			if (!file.getNamespace().equals("hemomancy") || file.getPath().split("/").length != 4) continue;
			templates++;
			CompoundTag root;
			try (InputStream in = entry.getValue().open()) {
				root = NbtIo.readCompressed(in, NbtAccounter.unlimitedHeap());
			} catch (IOException exception) {
				problems.add(file + ": " + exception.getMessage());
				continue;
			}
			for (Tag element : root.getList("palette", Tag.TAG_COMPOUND)) {
				CompoundTag state = (CompoundTag) element;
				ResourceLocation id = ResourceLocation.parse(state.getString("Name"));
				var block = BuiltInRegistries.BLOCK.getOptional(id);
				if (block.isEmpty()) {
					problems.add(file + " places " + id + ", which is not registered");
					continue;
				}
				CompoundTag properties = state.getCompound("Properties");
				for (String key : properties.getAllKeys()) {
					String value = properties.getString(key);
					if (!accepts(block.get(), key, value)) problems.add(file + " places " + id + "[" + key + "=" + value + "]");
				}
			}
		}
		helper.assertTrue(templates >= 4 * 20, "the mausoleum templates are loaded, found " + templates);
		helper.assertTrue(problems.isEmpty(), "Mausoleum templates place states their blocks do not have: " + problems);
		helper.succeed();
	}

	private static boolean accepts(Block block, String key, String value) {
		Property<?> property = block.getStateDefinition().getProperty(key);
		return property != null && property.getValue(value).isPresent();
	}
}
