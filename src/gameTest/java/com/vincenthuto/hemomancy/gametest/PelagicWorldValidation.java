package com.vincenthuto.hemomancy.gametest;

import com.google.gson.*;
import com.mojang.logging.LogUtils;
import com.vincenthuto.hemomancy.common.block.harbinger.plant.BoneWormColonyBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.plant.GiantTubeWormColonyBlock;
import com.vincenthuto.hemomancy.common.worldgen.pelagic.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import java.nio.file.*;
import java.util.*;

/** Explicit opt-in fresh-world acceptance; all coordinates come from natural climate sampling. */
public final class PelagicWorldValidation {
    public record Site(PelagicLayer layer, int x, int z, double predictedFloor) {}
    private static final Map<Site, PelagicLandmarks.Voxel> LANDMARKS = new LinkedHashMap<>();
    @GameTestGenerator
    public static Collection<TestFunction> tests() {
        if (System.getProperty("hemomancy.pelagic.validationSeed") == null) return List.of();
        if (Boolean.getBoolean("hemomancy.pelagic.expectDisabled"))
            return List.of(new TestFunction("pelagic_worldgen", "disabled_ocean", "pelagic_worldgen_validation:room", 100,
                    0, true, h -> {
                var source = h.getLevel().getChunkSource();
                h.assertTrue(PelagicWorldgen.context(source.randomState()) == null, "Disabled expansion must not install terrain hooks");
                for (var layer : PelagicLayer.values()) if (layer != PelagicLayer.REEF)
                    h.assertTrue(source.getGenerator().getBiomeSource().possibleBiomes().stream().noneMatch(b -> b.is(PelagicBiomes.key(layer))),
                            "Disabled expansion must not add depth/coast biomes to the source");
                h.succeed();
            }));
        return List.of(new TestFunction("pelagic_worldgen", "fresh_ocean_seed", "pelagic_worldgen_validation:room",
                Boolean.getBoolean("hemomancy.pelagic.ecologyProbe") ? 100000 : 12000, 0, true, PelagicWorldValidation::inspect));
    }

    private static void inspect(GameTestHelper h) {
        try {
            var level = h.getLevel();
            var context = PelagicWorldgen.context(level.getChunkSource().randomState());
            h.assertTrue(context != null, "Pelagic context must be installed in the real Overworld noise pipeline");
            h.assertTrue(level.getSeed() == Long.getLong("hemomancy.pelagic.validationSeed", 42), "Requested seed must be used");
            inspectUnchangedLand(h, level, context);
            var sites = findSites(context);
            sites.addAll(findLandmarks(context));
            LogUtils.getLogger().info("PELAGIC sites {}", sites);
            var report = new JsonObject();
            report.addProperty("seed", level.getSeed());
            report.addProperty("preset", System.getProperty("hemomancy.pelagic.validationPreset", "normal"));
            var results = new JsonArray();
            var requested = new LinkedHashSet<ChunkPos>();
            for (var site : sites) {
                int cx = site.x >> 4, cz = site.z >> 4;
                for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++) requested.add(new ChunkPos(cx + dx, cz + dz));
            }
            var generation = new ArrayList<>(requested);
            if (System.getProperty("hemomancy.pelagic.validationOrder", "forward").equals("reverse")) Collections.reverse(generation);
            long start = System.nanoTime();
            for (var chunk : generation) level.getChunk(chunk.x, chunk.z);
            for (var site : sites) results.add(inspectSite(h, level, context, site));
            report.add("biomeLocate", inspectBiomeLocate(h, level, sites));
            report.add("reefEdges", inspectReefEdges(h, context, sites));
            report.add("coastalEdges", inspectCoastalEdges(h, context, sites));
            report.add("boneWormCounts", new Gson().toJsonTree(inspectBoneWormCounts(h, level, sites)));
            report.add("tubeWormCounts", new Gson().toJsonTree(inspectTubeWormCounts(h, level, sites)));
            inspectMonument(h, level, sites);
            report.add("sites", results);
            report.addProperty("generationSeconds", (System.nanoTime() - start) / 1e9);
            Files.writeString(Path.of("pelagic-report.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report));
            for (var layer : PelagicLayer.values())
                h.assertTrue(sites.stream().filter(s -> s.layer == layer).count() >= 3, "Three natural sites required for " + layer);
            if (Boolean.getBoolean("hemomancy.pelagic.ecologyProbe")) PelagicEcologyWorldProbe.start(h, sites);
            else h.succeed();
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    private static JsonObject inspectCoastalEdges(GameTestHelper h, PelagicContext context, List<Site> sites) {
        int crossings = 0, reefCrossings = 0, measured = 0, measuredReef = 0, maximumHeightStep = 0;
        double maximumDensityStep = 0;
        var examples = new JsonArray();
        var steepSteps = new JsonArray();
        var level = h.getLevel();
        var generator = level.getChunkSource().getGenerator();
        var state = level.getChunkSource().randomState();
        for (var site : sites) {
            if (site.layer != PelagicLayer.SHORE && site.layer != PelagicLayer.REEF) continue;
            for (int x = site.x - 256; x < site.x + 256; x += 4)
                for (int z = site.z - 256; z < site.z + 256; z += 4)
                    for (int axis = 0; axis < 2; axis++) {
                        int px = x + (axis == 0 ? 3 : 0), pz = z + (axis == 1 ? 3 : 0);
                        int nx = px + (axis == 0 ? 1 : 0), nz = pz + (axis == 1 ? 1 : 0);
                        var a = context.sample(px, pz); var b = context.sample(nx, nz);
                        if (a.surface().is(PelagicBiomes.key(PelagicLayer.SHORE))
                                == b.surface().is(PelagicBiomes.key(PelagicLayer.SHORE))) continue;
                        if (java.util.stream.Stream.of(a, b).anyMatch(sample -> {
                            String namespace = sample.surface().unwrapKey().orElseThrow().location().getNamespace();
                            return !namespace.equals("minecraft") && !sample.surface().is(PelagicBiomes.key(PelagicLayer.SHORE))
                                    && !sample.surface().is(PelagicBiomes.key(PelagicLayer.REEF))
                                    && !sample.surface().is(PelagicBiomes.key(PelagicLayer.OPEN));
                        })) continue;
                        crossings++;
                        boolean reef = a.surface().is(PelagicBiomes.key(PelagicLayer.REEF))
                                || b.surface().is(PelagicBiomes.key(PelagicLayer.REEF));
                        if (reef) reefCrossings++;
                        for (int y = 40; y <= 75; y++) {
                            double first = PelagicDensity.shape(.2, a.column(), y, false, false);
                            double second = PelagicDensity.shape(.2, b.column(), y, false, false);
                            double change = Math.abs(first - second);
                            maximumDensityStep = Math.max(maximumDensityStep, change);
                            // Magnitude changes inside solid rock are not surface discontinuities.
                            if (first * second <= 0)
                                h.assertTrue(change < .8, "Coastal surface density discontinuity at " + px + ", " + y + ", " + pz
                                        + ": " + first + " -> " + second);
                        }
                        // Compare actual noise-generated heights, not just the requested profile.
                        if (reef ? measuredReef < 32 && reefCrossings % 13 == 1
                                : measured - measuredReef < 32 && crossings % 97 == 0) {
                            int ay = generator.getBaseHeight(px, pz, Heightmap.Types.OCEAN_FLOOR_WG, level, state);
                            int by = generator.getBaseHeight(nx, nz, Heightmap.Types.OCEAN_FLOOR_WG, level, state);
                            int step = Math.abs(ay - by);
                            maximumHeightStep = Math.max(maximumHeightStep, step);
                            h.assertTrue(step <= 6, "Generated coast has a sheer step of " + step + " at " + px + ", " + pz);
                            if (step > 3) {
                                var edge = new JsonObject();
                                edge.addProperty("x", px); edge.addProperty("z", pz); edge.addProperty("step", step);
                                edge.addProperty("pool", a.column().pool() || b.column().pool());
                                edge.addProperty("reef", reef); steepSteps.add(edge);
                            }
                            measured++;
                            if (reef) measuredReef++;
                            if (reef && examples.size() < 4) {
                                var example = new JsonObject();
                                example.addProperty("x", px); example.addProperty("z", pz);
                                example.addProperty("floor", ay); example.addProperty("neighborFloor", by);
                                examples.add(example);
                            }
                        }
                    }
        }
        h.assertTrue(crossings > 10 && measured > 0, "Natural coastal boundaries must be measured");
        h.assertTrue(reefCrossings > 0, "Include actual Rockpool Shore to reef intersections");
        h.assertTrue(measuredReef > 0, "Measure generated heights at Rockpool Shore to reef intersections");
        var result = new JsonObject();
        result.addProperty("crossings", crossings); result.addProperty("reefCrossings", reefCrossings);
        result.addProperty("noiseHeightChecks", measured); result.addProperty("maximumNoiseHeightStep", maximumHeightStep);
        result.addProperty("reefNoiseHeightChecks", measuredReef);
        result.addProperty("maximumDensityStep", maximumDensityStep); result.add("reefExamples", examples);
        result.add("steepSteps", steepSteps);
        return result;
    }

    private static JsonObject inspectReefEdges(GameTestHelper h, PelagicContext context, List<Site> sites) {
        int crossings = 0;
        double steepest = 0;
        double originalSteepest = 0;
        int worstX = 0, worstZ = 0;
        var router = h.getLevel().getChunkSource().randomState().router();
        for (var site : sites) {
            if (site.layer != PelagicLayer.REEF) continue;
            for (int x = site.x - 128; x < site.x + 128; x += 4)
                for (int z = site.z - 128; z < site.z + 128; z += 4)
                    for (int axis = 0; axis < 2; axis++) {
                        int px = x + (axis == 0 ? 3 : 0), pz = z + (axis == 1 ? 3 : 0);
                        var a = context.sample(px, pz);
                        var b = context.sample(px + (axis == 0 ? 1 : 0), pz + (axis == 1 ? 1 : 0));
                        if (java.util.stream.Stream.of(a, b).anyMatch(sample -> {
                            String namespace = sample.surface().unwrapKey().orElseThrow().location().getNamespace();
                            return !namespace.equals("minecraft") && !namespace.equals("hemomancy");
                        })) continue;
                        if (!a.surface().is(BiomeTags.IS_OCEAN) || !b.surface().is(BiomeTags.IS_OCEAN)
                                || a.surface().is(PelagicBiomes.key(PelagicLayer.REEF))
                                == b.surface().is(PelagicBiomes.key(PelagicLayer.REEF))) continue;
                        double step = Math.abs(a.column().floor() - b.column().floor());
                        double[] originalFloors = new double[2];
                        for (int side = 0; side < 2; side++) {
                            int sx = px + (axis == 0 ? side : 0), sz = pz + (axis == 1 ? side : 0);
                            var point = new net.minecraft.world.level.levelgen.DensityFunction.SinglePointContext(sx, 63, sz);
                            var surface = (side == 0 ? a : b).surface().is(PelagicBiomes.key(PelagicLayer.REEF))
                                    ? PelagicTerrainSampler.Surface.REEF : PelagicTerrainSampler.Surface.OPEN;
                            originalFloors[side] = context.terrain().sample(sx, sz, router.continents().compute(point),
                                    router.erosion().compute(point), router.ridges().compute(point), surface).floor();
                        }
                        double originalStep = Math.abs(originalFloors[0] - originalFloors[1]);
                        if (originalStep > originalSteepest) {
                            originalSteepest = originalStep;
                            worstX = px; worstZ = pz;
                        }
                        steepest = Math.max(steepest, step);
                        crossings++;
                        h.assertTrue(step < 4, "Reef edge jumps " + step + " blocks at " + px + ", " + pz);
                    }
        }
        h.assertTrue(crossings > 10, "Natural reef boundaries must be exercised");
        var result = new JsonObject();
        result.addProperty("crossings", crossings);
        result.addProperty("maximumAdjacentFloorStep", steepest);
        result.addProperty("maximumUnblendedFloorStep", originalSteepest);
        result.addProperty("worstOriginalX", worstX);
        result.addProperty("worstOriginalZ", worstZ);
        return result;
    }

    public static List<Site> findSites(PelagicContext context) {
        var sites = new ArrayList<Site>();
        var counts = new EnumMap<PelagicLayer, Integer>(PelagicLayer.class);
        int scale = System.getProperty("hemomancy.pelagic.validationPreset", "normal").equals("large_biomes") ? 4 : 1;
        for (int radius = 256 * scale; radius <= 8192 * scale && sites.size() < 21; radius += 256 * scale) {
            for (int x = -radius; x <= radius; x += 32 * scale) for (int z = -radius; z <= radius; z += 32 * scale) {
                if (Math.max(Math.abs(x), Math.abs(z)) <= radius - 256 * scale) continue;
                var sample = context.sample(x, z);
                var c = sample.column();
                if (c.influence() < .999) continue;
                PelagicLayer layer;
                if (sample.surface().is(PelagicBiomes.key(PelagicLayer.SHORE)) && c.floor() >= 60) layer = PelagicLayer.SHORE;
                else if (!sample.surface().is(BiomeTags.IS_OCEAN)) continue;
                else if (sample.surface().is(PelagicBiomes.key(PelagicLayer.REEF)) && c.floor() > 36 && c.floor() < 48) layer = PelagicLayer.REEF;
                else if (sample.surface().is(PelagicBiomes.key(PelagicLayer.OPEN)) && c.floor() < 0
                        && counts.getOrDefault(PelagicLayer.OPEN, 0) < 3) layer = PelagicLayer.OPEN;
                else if (c.floor() > 17 && c.floor() < 28) layer = PelagicLayer.TWILIGHT;
                else if (c.floor() > -5 && c.floor() < 7) layer = PelagicLayer.MIDNIGHT;
                else if (c.floor() > -27 && c.floor() < -16) layer = PelagicLayer.CARRION;
                else if (c.floor() <= -53) layer = PelagicLayer.HYDROTHERMAL;
                else continue;
                if (counts.getOrDefault(layer, 0) >= 3) continue;
                final int sx = x, sz = z;
                if (sites.stream().anyMatch(s -> Math.abs(s.x - sx) < 160 && Math.abs(s.z - sz) < 160)) continue;
                sites.add(new Site(layer, x, z, c.floor()));
                counts.merge(layer, 1, Integer::sum);
            }
        }
        sites.sort(Comparator.comparing(Site::layer).thenComparingInt(Site::x).thenComparingInt(Site::z));
        return sites;
    }

    private static JsonArray inspectBiomeLocate(GameTestHelper helper, ServerLevel level, List<Site> sites) {
        var basin = sites.stream().filter(site -> site.layer() == PelagicLayer.HYDROTHERMAL).findFirst().orElseThrow();
        var underwater = new BlockPos(basin.x(), -44, basin.z());
        helper.assertTrue(level.getBiome(underwater).is(PelagicBiomes.key(PelagicLayer.HYDROTHERMAL)),
                "Locate control must contain a generated Hydrothermal biome at Y-44");
        var results = new JsonArray();
        var source = level.getChunkSource().getGenerator().getBiomeSource();
        var sampler = level.getChunkSource().randomState().sampler();
        for (int y : new int[]{-27, 63, 120, -60}) {
            var origin = new BlockPos(basin.x(), y, basin.z());
            for (var layer : new PelagicLayer[]{PelagicLayer.HYDROTHERMAL, PelagicLayer.TWILIGHT, PelagicLayer.MIDNIGHT, PelagicLayer.CARRION}) {
                var key = PelagicBiomes.key(layer);
                java.util.function.Predicate<net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome>> predicate = biome -> biome.is(key);
                var found = level.findClosestBiome3d(predicate, origin, 0, 32, 64);
                helper.assertTrue(found != null && found.getSecond().is(key),
                        "Vanilla locate spacing skipped " + layer.id + " from Y" + y + " despite the generated biome in this column");
                helper.assertTrue(found.getFirst().getX() == origin.getX() && found.getFirst().getZ() == origin.getZ(),
                        "Biome lookup must keep its requested horizontal search bounds");
                var fine = source.findClosestBiome3d(origin, 0, 32, 4, predicate, sampler, level);
                helper.assertTrue(Objects.equals(fine, level.findClosestBiome3d(predicate, origin, 0, 32, 4)),
                        "Already-fine biome searches must retain their requested spacing");
                boolean[] succeeded = {false};
                var command = level.getServer().createCommandSourceStack().withLevel(level).withPosition(net.minecraft.world.phys.Vec3.atCenterOf(origin))
                        .withPermission(4).withCallback((success, value) -> succeeded[0] = success);
                level.getServer().getCommands().performPrefixedCommand(command, "locate biome " + key.location());
                helper.assertTrue(succeeded[0], "Actual /locate biome command failed for " + layer.id + " from Y" + y);
                var row = new JsonObject(); row.addProperty("biome", layer.id); row.addProperty("originY", y);
                row.addProperty("foundY", found.getFirst().getY()); row.addProperty("commandSucceeded", true); results.add(row);
            }
            java.util.function.Predicate<net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome>> vanilla =
                    biome -> biome.unwrapKey().orElseThrow().location().getNamespace().equals("minecraft");
            helper.assertTrue(Objects.equals(source.findClosestBiome3d(origin, 0, 32, 64, vanilla, sampler, level),
                    level.findClosestBiome3d(vanilla, origin, 0, 32, 64)), "Vanilla-only biome lookups must be unchanged");
        }
        return results;
    }

    private static Map<Integer, Integer> inspectBoneWormCounts(GameTestHelper helper, ServerLevel level, List<Site> sites) {
        var counts = new TreeMap<Integer, Integer>();
        for (var site : sites) {
            if (site.layer() != PelagicLayer.CARRION) continue;
            for (var pos : BlockPos.betweenClosed(new BlockPos(site.x() - 24, -53, site.z() - 24),
                    new BlockPos(site.x() + 23, -6, site.z() + 23))) {
                var state = level.getBlockState(pos);
                if (state.getBlock() instanceof BoneWormColonyBlock) counts.merge(state.getValue(BoneWormColonyBlock.WORMS), 1, Integer::sum);
            }
        }
        helper.assertTrue(counts.keySet().equals(Set.of(2, 3, 4, 5)), "Natural fossil colonies should include every worm count: " + counts);
        return counts;
    }

    private static Map<Integer, Integer> inspectTubeWormCounts(GameTestHelper helper, ServerLevel level, List<Site> sites) {
        var counts = new TreeMap<Integer, Integer>();
        var visited = new HashSet<ChunkPos>();
        for (var site : sites) {
            if (site.layer() != PelagicLayer.HYDROTHERMAL) continue;
            for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++) {
                var chunk = new ChunkPos((site.x() >> 4) + dx, (site.z() >> 4) + dz);
                if (visited.add(chunk)) inspectTubeWormChunk(helper, level, chunk, counts);
            }
        }
        // Colonies are sparse; sample additional natural vent chunks rather than requiring every size at one vent.
        var vent = sites.stream().filter(site -> site.layer() == PelagicLayer.HYDROTHERMAL && LANDMARKS.containsKey(site)).findFirst().orElseThrow();
        var context = PelagicWorldgen.context(level.getChunkSource().randomState());
        int extraVents = 0;
        for (int radius = 1; radius <= 64 && counts.size() < 4 && extraVents < 32; radius++) {
            for (int dx = -radius; dx <= radius && counts.size() < 4 && extraVents < 32; dx++)
                for (int dz = -radius; dz <= radius && counts.size() < 4 && extraVents < 32; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) continue;
                    var chunk = new ChunkPos((vent.x() >> 4) + dx, (vent.z() >> 4) + dz);
                    if (visited.contains(chunk)) continue;
                    var plan = PelagicLandmarks.plan(context.terrain(), chunk.x, chunk.z, (x, z) -> context.sample(x, z).column());
                    if (plan.stream().noneMatch(voxel -> voxel.material() == PelagicLandmarks.Material.MAGMA)) continue;
                    visited.add(chunk); extraVents++;
                    inspectTubeWormChunk(helper, level, chunk, counts);
                }
        }
        helper.assertTrue(counts.keySet().equals(Set.of(2, 3, 4, 5)), "Natural vent colonies should include every worm count: " + counts);
        LogUtils.getLogger().info("PELAGIC tube worm counts {} after {} additional natural vent chunks", counts, extraVents);
        return counts;
    }

    private static void inspectTubeWormChunk(GameTestHelper helper, ServerLevel level, ChunkPos chunk, Map<Integer, Integer> counts) {
        level.getChunk(chunk.x, chunk.z);
        for (var pos : BlockPos.betweenClosed(new BlockPos(chunk.getMinBlockX(), -53, chunk.getMinBlockZ()),
                new BlockPos(chunk.getMaxBlockX(), -29, chunk.getMaxBlockZ()))) {
            var state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof GiantTubeWormColonyBlock)
                    || state.getValue(GiantTubeWormColonyBlock.HALF) != net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER) continue;
            int count = state.getValue(GiantTubeWormColonyBlock.WORMS);
            var upper = level.getBlockState(pos.above());
            helper.assertTrue(upper.is(state.getBlock()) && upper.getValue(GiantTubeWormColonyBlock.WORMS) == count
                    && upper.getValue(GiantTubeWormColonyBlock.HALF) == net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER,
                    "Generated tube worm halves must share their worm count at " + pos);
            counts.merge(count, 1, Integer::sum);
        }
    }

    private static List<Site> findLandmarks(PelagicContext context) {
        var sites = new ArrayList<Site>();
        int maximum = System.getProperty("hemomancy.pelagic.validationPreset", "normal").equals("large_biomes") ? 1024 : 256;
        for (int radius = 16; radius <= maximum && sites.size() < 3; radius += 16) {
            for (int cx = -radius; cx <= radius; cx++) for (int cz = -radius; cz <= radius; cz++) {
                if (Math.max(Math.abs(cx), Math.abs(cz)) <= radius - 16) continue;
                var plan = PelagicLandmarks.plan(context.terrain(), cx, cz, (x,z) -> context.sample(x,z).column());
                for (var voxel : plan.stream().sorted(Comparator.comparingInt(PelagicLandmarks.Voxel::y).reversed()).toList()) {
                    var layer = voxel.material() == PelagicLandmarks.Material.MAGMA ? PelagicLayer.HYDROTHERMAL
                            : voxel.material() == PelagicLandmarks.Material.BONE ? PelagicLayer.CARRION
                            : voxel.material() == PelagicLandmarks.Material.STONE ? PelagicLayer.SHORE : null;
                    if (layer == null || sites.stream().anyMatch(s -> s.layer == layer)) continue;
                    var site = new Site(layer, voxel.x(), voxel.z(), context.sample(voxel.x(),voxel.z()).column().floor());
                    sites.add(site);
                    LANDMARKS.put(site, voxel);
                }
            }
        }
        if (sites.size() != 3) throw new IllegalStateException("Natural vent, fossil and shore-stack sites required; found " + sites);
        return sites;
    }

    private static JsonObject inspectSite(GameTestHelper h, ServerLevel level, PelagicContext context, Site site) throws Exception {
        var landmark = LANDMARKS.get(site);
        if (landmark != null) {
            var expected = switch (landmark.material()) {
                case MAGMA -> Blocks.MAGMA_BLOCK; case BONE -> Blocks.BONE_BLOCK; case STONE -> Blocks.STONE;
                default -> throw new IllegalStateException("Unexpected landmark marker");
            };
            var marker = new BlockPos(landmark.x(), landmark.y(), landmark.z());
            h.assertTrue(level.getBlockState(marker).is(expected), "Natural " + landmark.material() + " landmark missing at " + marker);
        }
        var result = new JsonObject();
        result.addProperty("layer", site.layer.id); result.addProperty("x", site.x); result.addProperty("z", site.z);
        result.addProperty("predictedFloor", site.predictedFloor);
        var heights = new JsonArray(); var materials = new JsonArray();
        var hash = java.security.MessageDigest.getInstance("SHA-256");
        var blocksHash = java.security.MessageDigest.getInstance("SHA-256");
        var biomeHash = java.security.MessageDigest.getInstance("SHA-256");
        var noiseHash = java.security.MessageDigest.getInstance("SHA-256");
        byte[] bytes = new byte[4];
        int min = 100, max = -64, exposed = 0, dry = 0, deepColumns = 0, flatColumns = 0;
        for (int x = site.x - 24; x < site.x + 24; x++) for (int z = site.z - 24; z < site.z + 24; z++) {
            int floor = level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z) - 1;
            var p = new BlockPos(x, floor, z);
            min = Math.min(min, floor); max = Math.max(max, floor);
            heights.add(floor); materials.add(BuiltInRegistries.BLOCK.getKey(level.getBlockState(p).getBlock()).toString());
            hash.update((x + "," + z + "," + floor + ":" + level.getBlockState(p)).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            var base = (x & 3) == 0 && (z & 3) == 0 ? level.getChunkSource().getGenerator().getBaseColumn(x, z, level,
                    level.getChunkSource().randomState()) : null;
            for (int y = -58; y <= Math.max(75, floor); y++) {
                var sample = new BlockPos(x, y, z);
                int id = net.minecraft.world.level.block.Block.getId(level.getBlockState(sample));
                bytes[0] = (byte)id; bytes[1] = (byte)(id >> 8); bytes[2] = (byte)(id >> 16); bytes[3] = (byte)(id >> 24);
                blocksHash.update(bytes);
                if (base != null && y <= 75) {
                    int noiseId = net.minecraft.world.level.block.Block.getId(base.getBlock(y));
                    noiseHash.update((byte)noiseId); noiseHash.update((byte)(noiseId >> 8));
                    noiseHash.update((byte)(noiseId >> 16)); noiseHash.update((byte)(noiseId >> 24));
                }
                if (y <= 75 && (x & 3) == 0 && (y & 3) == 0 && (z & 3) == 0)
                    biomeHash.update(level.getBiome(sample).unwrapKey().orElseThrow().location().toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
                if (y < 12 && context.sample(x, z).column().influence() > .99)
                    h.assertTrue(!level.getBlockState(sample).is(Blocks.KELP) && !level.getBlockState(sample).is(Blocks.KELP_PLANT), "Deep canyons must be free of kelp at " + sample);
            }
            if (level.getBlockState(p).is(Blocks.BEDROCK)) exposed++;
            if (context.sample(x, z).column().floor() <= -53 && context.sample(x, z).column().influence() > .999) {
                deepColumns++;
                if (floor >= -55 && floor <= -53) flatColumns++;
            }
            if (floor < 60 && context.sample(x, z).column().influence() >= .999 && level.getBlockState(p.above()).isAir()) dry++;
            h.assertTrue(!level.getFluidState(p.above()).is(FluidTags.LAVA), "No ocean-floor lava at " + p);
        }
        h.assertTrue(exposed == 0 && dry == 0, "Seabed must be sealed and flooded: " + site + " bedrock=" + exposed + " air=" + dry);
        int[] ys = {50, 24, 0, -20, -40};
        if (site.layer == PelagicLayer.HYDROTHERMAL) for (int y : ys) {
            var actual = level.getBiome(new BlockPos(site.x, y, site.z));
            var expected = context.biome(site.x, y, site.z, context.sample(site.x, site.z).surface());
            h.assertTrue(actual.is(expected.unwrapKey().orElseThrow()), "Loaded vertical biome mismatch at Y" + y + " " + actual + " != " + expected);
            if (y > site.predictedFloor + 8)
                h.assertTrue(level.getFluidState(new BlockPos(site.x, y, site.z)).is(FluidTags.WATER), "Deep column must be full of water at " + y);
        }
        result.addProperty("minimum", min); result.addProperty("maximum", max);
        result.addProperty("deepColumns", deepColumns); result.addProperty("flatColumns", flatColumns);
        result.addProperty("sha256", HexFormat.of().formatHex(hash.digest()));
        result.addProperty("blocksSha256", HexFormat.of().formatHex(blocksHash.digest()));
        result.addProperty("biomesSha256", HexFormat.of().formatHex(biomeHash.digest()));
        result.addProperty("noiseSha256", HexFormat.of().formatHex(noiseHash.digest()));
        if (landmark != null) result.addProperty("landmark", landmark.material().toString());
        result.add("heights", heights); result.add("materials", materials);
        LogUtils.getLogger().info("PELAGIC measured {}: {}..{}, flat {}/{}", site, min, max, flatColumns, deepColumns);
        return result;
    }

    private static void inspectMonument(GameTestHelper h, ServerLevel level, List<Site> sites) {
        var structure = level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE)
                .get(net.minecraft.resources.ResourceLocation.withDefaultNamespace("monument"));
        var generator = level.getChunkSource().getGenerator();
        for (var site : sites) {
            if (site.layer != PelagicLayer.HYDROTHERMAL) continue;
            for (int dx = -6; dx <= 6; dx += 2) for (int dz = -6; dz <= 6; dz += 2) {
                var chunk = new ChunkPos((site.x >> 4) + dx, (site.z >> 4) + dz);
                var start = structure.generate(level.registryAccess(), generator, generator.getBiomeSource(),
                        level.getChunkSource().randomState(), level.getStructureManager(), level.getSeed(), chunk, 0, level, b -> true);
                if (!start.isValid()) continue;
                int y = start.getPieces().getFirst().getBoundingBox().minY();
                if (y >= 0) continue;
                h.assertTrue(y >= -55 && y < 0, "New ocean monument must sit on the actual submerged floor");
                int sampledFloor = Integer.MIN_VALUE;
                for (int fx = -29; fx <= 29; fx += 14) for (int fz = -29; fz <= 29; fz += 14)
                    sampledFloor = Math.max(sampledFloor, generator.getBaseHeight(chunk.getMinBlockX() + fx,
                            chunk.getMinBlockZ() + fz, Heightmap.Types.OCEAN_FLOOR_WG, level, level.getChunkSource().randomState()));
                h.assertTrue(y == sampledFloor, "Monument base must meet the highest sampled natural floor, not float at a fixed Y");
                var serialization = net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext.fromLevel(level);
                var encoded = start.createTag(serialization, chunk);
                var decoded = net.minecraft.world.level.levelgen.structure.StructureStart.loadStaticStart(serialization, encoded, level.getSeed());
                h.assertTrue(decoded != null && decoded.isValid(), "Monument start must decode from saved NBT");
                var saved = new net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer(decoded.getPieces());
                var loaded = net.minecraft.world.level.levelgen.structure.structures.OceanMonumentStructure
                        .regeneratePiecesAfterLoad(chunk, level.getSeed(), saved);
                h.assertTrue(loaded.pieces().getFirst().getBoundingBox().minY() == y, "Deep monument must retain its elevation on reload");
                var bounds = start.getBoundingBox();
                for (int cx = bounds.minX() >> 4; cx <= bounds.maxX() >> 4; cx++)
                    for (int cz = bounds.minZ() >> 4; cz <= bounds.maxZ() >> 4; cz++) {
                        level.getChunk(cx, cz);
                        var target = new ChunkPos(cx, cz);
                        start.placeInChunk(level, level.structureManager(), generator, net.minecraft.util.RandomSource.create(level.getSeed()),
                                new net.minecraft.world.level.levelgen.structure.BoundingBox(target.getMinBlockX(), -64,
                                        target.getMinBlockZ(), target.getMaxBlockX(), level.getMaxBuildHeight() - 1, target.getMaxBlockZ()), target);
                    }
                int prismarine = 0;
                for (int px = bounds.minX(); px <= bounds.maxX(); px++) for (int pz = bounds.minZ(); pz <= bounds.maxZ(); pz++)
                    if (level.getBlockState(new BlockPos(px, y, pz)).is(Blocks.PRISMARINE)
                            || level.getBlockState(new BlockPos(px, y, pz)).is(Blocks.PRISMARINE_BRICKS)) prismarine++;
                h.assertTrue(prismarine > 100, "Monument base blocks must actually be placed at the anchored depth");
                LogUtils.getLogger().info("PELAGIC monument {} floor Y{}: {} base blocks; saved NBT reconstructs correctly", chunk, y, prismarine);
                return;
            }
        }
        h.fail("No valid deep monument footprint found around sampled basins");
    }

    private static void inspectUnchangedLand(GameTestHelper h, ServerLevel level, PelagicContext context) {
        var generator = (net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator)level.getChunkSource().getGenerator();
        var unshaped = net.minecraft.world.level.levelgen.RandomState.create(generator.generatorSettings().value(),
                level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.NOISE), level.getSeed());
        int compared = 0;
        for (int x = -2048; x <= 2048 && compared < 20; x += 128) for (int z = -2048; z <= 2048 && compared < 20; z += 128) {
            var sample = context.sample(x, z);
            if (sample.column().influence() != 0 || sample.surface().is(BiomeTags.IS_OCEAN)) continue;
            var expected = generator.getBaseColumn(x, z, level, unshaped);
            var actual = generator.getBaseColumn(x, z, level, level.getChunkSource().randomState());
            for (int y = -60; y < 200; y++)
                h.assertTrue(actual.getBlock(y).equals(expected.getBlock(y)), "Unchanged land control differs at " + x + "," + y + "," + z);
            compared++;
        }
        h.assertTrue(compared == 20, "Twenty unchanged inland control columns required");
    }
}
