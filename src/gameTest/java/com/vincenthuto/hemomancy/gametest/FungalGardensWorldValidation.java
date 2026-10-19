package com.vincenthuto.hemomancy.gametest;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Samples the seeded Fungal Gardens generator without placing chunks, renders a biome/height map and a
 * side slice to {@code fungal-gardens-validation/}, and checks that each biome carries its own terrain.
 */
@GameTestHolder("fungal_gardens_worldgen_validation")
@PrefixGameTestTemplate(false)
public final class FungalGardensWorldValidation {
	private static final int SAMPLES = 256;
	private static final int STEP = 8;
	private static final int SEA_LEVEL = 20;
	private static final int FLOATING_GAP = 20;
	private static final ResourceLocation DIMENSION = ResourceLocation.parse("hemomancy:dimension/fungal_gardens.json");

	private static final Map<String, Integer> COLORS = new LinkedHashMap<>();
	static {
		COLORS.put("hemomancy:fungal_isles", 0x2E6F8E);
		COLORS.put("hemomancy:mycelial_meadows", 0x9C4A6A);
		COLORS.put("hemomancy:sporecrown_thicket", 0x5E7A2C);
		COLORS.put("hemomancy:hemorrhagic_plateau", 0xB5542E);
		COLORS.put("hemomancy:mycelial_depths", 0x3A2B5E);
		COLORS.put("hemomancy:hyphal_spires", 0xE0D8C0);
		COLORS.put("hemomancy:drifting_mycelium", 0x6FB0A0);
	}

	private record Column(String biome, int ground, int top, boolean water) {}

	@GameTest(template = "empty", timeoutTicks = 12000)
	public static void biomesCarryTheirOwnTerrain(GameTestHelper helper) {
		// The GameTest world only creates the Overworld, so decode the dimension straight from its datapack.
		MinecraftServer server = helper.getLevel().getServer();
		RegistryAccess registries = server.registryAccess();
		LevelStem stem;
		try (var reader = server.getResourceManager().getResourceOrThrow(DIMENSION).openAsReader()) {
			stem = LevelStem.CODEC.parse(RegistryOps.create(JsonOps.INSTANCE, registries), JsonParser.parseReader(reader))
					.getOrThrow();
		} catch (IOException exception) {
			helper.fail("Could not read " + DIMENSION + ": " + exception.getMessage());
			return;
		}
		helper.assertTrue(stem.generator() instanceof NoiseBasedChunkGenerator, "Fungal Gardens must use a noise generator");
		ChunkGenerator generator = stem.generator();
		RandomState random = RandomState.create(((NoiseBasedChunkGenerator) generator).generatorSettings().value(),
				registries.lookupOrThrow(Registries.NOISE), Long.getLong("hemomancy.fungal.validationSeed", 42L));
		BiomeSource biomes = generator.getBiomeSource();
		DimensionType type = stem.type().value();
		LevelHeightAccessor level = LevelHeightAccessor.create(type.minY(), type.height());

		int origin = -SAMPLES * STEP / 2;
		Column[][] grid = new Column[SAMPLES][SAMPLES];
		for (int i = 0; i < SAMPLES; i++) {
			for (int j = 0; j < SAMPLES; j++) {
				int x = origin + i * STEP;
				int z = origin + j * STEP;
				grid[i][j] = sample(level, generator, random, biomes, x, z);
			}
		}

		Path out = server.getServerDirectory().resolve("fungal-gardens-validation");
		Map<String, List<Column>> byBiome = new LinkedHashMap<>();
		for (Column[] row : grid) for (Column column : row) byBiome.computeIfAbsent(column.biome(), k -> new ArrayList<>()).add(column);
		try {
			Files.createDirectories(out);
			ImageIO.write(map(grid), "png", out.resolve("biome_map.png").toFile());
			for (String biome : List.of("hemomancy:hyphal_spires", "hemomancy:drifting_mycelium")) {
				int z = origin + richestRow(grid, biome) * STEP;
				ImageIO.write(slice(level, generator, random, biomes, origin, z), "png",
						out.resolve("slice_" + biome.substring(biome.indexOf(':') + 1) + ".png").toFile());
			}
			Files.writeString(out.resolve("stats.txt"), stats(byBiome), StandardCharsets.UTF_8);
		} catch (IOException exception) {
			helper.fail("Could not write validation output: " + exception.getMessage());
		}

		int total = SAMPLES * SAMPLES;
		for (String biome : COLORS.keySet()) {
			int count = byBiome.getOrDefault(biome, List.of()).size();
			helper.assertTrue(count >= total / 200, biome + " covers only " + count + " of " + total + " samples");
		}
		helper.assertTrue(waterShare(byBiome, "hemomancy:fungal_isles") >= 0.6, "Fungal Isles should be mostly sea");
		helper.assertTrue(waterShare(byBiome, "hemomancy:mycelial_meadows") <= 0.25, "Meadows should be dry lowland");
		helper.assertTrue(waterShare(byBiome, "hemomancy:mycelial_depths") <= 0.25, "Depths gorges should be dry");
		int meadows = percentile(byBiome, "hemomancy:mycelial_meadows", 0.5, false);
		int plateau = percentile(byBiome, "hemomancy:hemorrhagic_plateau", 0.5, false);
		int depths = percentile(byBiome, "hemomancy:mycelial_depths", 0.5, false);
		helper.assertTrue(meadows >= 22 && meadows <= 55, "Meadows median ground " + meadows + " is not lowland");
		helper.assertTrue(plateau >= 55, "Plateau median ground " + plateau + " is not highland");
		helper.assertTrue(depths < plateau && depths <= 42, "Depths median ground " + depths + " is not a gorge");
		helper.assertTrue(percentile(byBiome, "hemomancy:hyphal_spires", 0.9, true) >= 120, "Hyphal Spires are not tall");
		helper.assertTrue(floatingShare(byBiome, "hemomancy:drifting_mycelium") >= 0.15,
				"Drifting Mycelium has too little floating land");
		helper.succeed();
	}

	private static Column sample(LevelHeightAccessor level, ChunkGenerator generator, RandomState random, BiomeSource biomes,
			int x, int z) {
		NoiseColumn column = generator.getBaseColumn(x, z, level, random);
		int min = level.getMinBuildHeight();
		int max = level.getMaxBuildHeight();
		int ground = min;
		for (int y = min; y < max - 1; y++) {
			if (solid(column.getBlock(y)) && !solid(column.getBlock(y + 1))) {
				ground = y;
				break;
			}
		}
		int top = ground;
		boolean water = false;
		for (int y = max - 1; y > min; y--) {
			BlockState state = column.getBlock(y);
			if (!state.isAir()) {
				top = y;
				water = !state.getFluidState().isEmpty();
				break;
			}
		}
		String biome = biomes.getNoiseBiome(x >> 2, (ground + 1) >> 2, z >> 2, random.sampler()).unwrapKey()
				.map(key -> key.location().toString()).orElse("?");
		return new Column(biome, ground, top, water);
	}

	private static boolean solid(BlockState state) {
		return !state.isAir() && state.getFluidState().isEmpty();
	}

	private static int richestRow(Column[][] grid, String biome) {
		int bestRow = 0;
		long best = -1;
		for (int j = 0; j < SAMPLES; j++) {
			int row = j;
			long count = Arrays.stream(grid).filter(r -> r[row].biome().equals(biome)).count();
			if (count > best) {
				best = count;
				bestRow = j;
			}
		}
		return bestRow;
	}

	private static BufferedImage map(Column[][] grid) {
		BufferedImage image = new BufferedImage(SAMPLES, SAMPLES, BufferedImage.TYPE_INT_RGB);
		for (int i = 0; i < SAMPLES; i++) {
			for (int j = 0; j < SAMPLES; j++) {
				Column column = grid[i][j];
				int rgb = COLORS.getOrDefault(column.biome(), 0xFF00FF);
				double shade = column.water() ? 0.45 : 0.55 + Math.min(column.ground(), 160) / 320.0;
				if (column.top() - column.ground() >= FLOATING_GAP) shade = Math.min(1.25, shade + 0.35);
				image.setRGB(i, j, scale(rgb, shade));
			}
		}
		return image;
	}

	private static BufferedImage slice(LevelHeightAccessor level, ChunkGenerator generator, RandomState random,
			BiomeSource biomes, int origin, int z) {
		int width = SAMPLES * STEP / 2;
		int min = level.getMinBuildHeight();
		int height = level.getMaxBuildHeight() - min;
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		for (int i = 0; i < width; i++) {
			int x = origin + i * 2;
			NoiseColumn column = generator.getBaseColumn(x, z, level, random);
			int ground = sample(level, generator, random, biomes, x, z).ground();
			int rgb = COLORS.getOrDefault(biomes.getNoiseBiome(x >> 2, (ground + 1) >> 2, z >> 2, random.sampler())
					.unwrapKey().map(key -> key.location().toString()).orElse("?"), 0xFF00FF);
			for (int y = min; y < level.getMaxBuildHeight(); y++) {
				BlockState state = column.getBlock(y);
				int color = state.isAir() ? (y == SEA_LEVEL ? 0x303030 : 0x101010)
						: !state.getFluidState().isEmpty() ? 0x1F3F7F : scale(rgb, 0.6 + (y - min) / 640.0);
				image.setRGB(i, height - 1 - (y - min), color);
			}
		}
		return image;
	}

	private static String stats(Map<String, List<Column>> byBiome) {
		StringBuilder text = new StringBuilder("biome samples water floating ground[p10 p50 p90] top[p90]\n");
		byBiome.forEach((biome, columns) -> text.append(String.format("%s %d %.2f %.2f [%d %d %d] [%d]%n", biome,
				columns.size(), waterShare(byBiome, biome), floatingShare(byBiome, biome),
				percentile(byBiome, biome, 0.1, false), percentile(byBiome, biome, 0.5, false),
				percentile(byBiome, biome, 0.9, false), percentile(byBiome, biome, 0.9, true))));
		return text.toString();
	}

	private static double waterShare(Map<String, List<Column>> byBiome, String biome) {
		List<Column> columns = byBiome.getOrDefault(biome, List.of());
		return columns.isEmpty() ? 0 : columns.stream().filter(Column::water).count() / (double) columns.size();
	}

	private static double floatingShare(Map<String, List<Column>> byBiome, String biome) {
		List<Column> columns = byBiome.getOrDefault(biome, List.of());
		return columns.isEmpty() ? 0
				: columns.stream().filter(c -> c.top() - c.ground() >= FLOATING_GAP).count() / (double) columns.size();
	}

	private static int percentile(Map<String, List<Column>> byBiome, String biome, double fraction, boolean top) {
		int[] values = byBiome.getOrDefault(biome, List.of()).stream().mapToInt(c -> top ? c.top() : c.ground()).toArray();
		if (values.length == 0) return Integer.MIN_VALUE;
		Arrays.sort(values);
		return values[Math.min(values.length - 1, (int) (values.length * fraction))];
	}

	private static int scale(int rgb, double factor) {
		int r = (int) Math.min(255, ((rgb >> 16) & 0xFF) * factor);
		int g = (int) Math.min(255, ((rgb >> 8) & 0xFF) * factor);
		int b = (int) Math.min(255, (rgb & 0xFF) * factor);
		return (r << 16) | (g << 8) | b;
	}
}
