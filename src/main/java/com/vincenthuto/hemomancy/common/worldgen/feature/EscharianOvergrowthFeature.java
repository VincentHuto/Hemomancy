package com.vincenthuto.hemomancy.common.worldgen.feature;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.BiomeInit;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.worldgen.EscharianOvergrowthLayout;
import com.vincenthuto.hemomancy.common.worldgen.EscharianOvergrowthAnchors;
import com.vincenthuto.hemomancy.common.worldgen.EscharianOvergrowthLayout.Cell;
import com.vincenthuto.hemomancy.common.block.harbinger.EscharianScyphusBlock;
import com.vincenthuto.hemomancy.common.worldgen.EscharianOvergrowthPlacement;
import com.vincenthuto.hemomancy.common.worldgen.EscharianOvergrowthSubstrate;
import com.vincenthuto.hemomancy.common.worldgen.PhlegethonticBasinLayout;
import com.vincenthuto.hemomancy.common.worldgen.PhlegethonticRules;
import com.vincenthuto.hemomancy.common.worldgen.PhlegethonticTags;
import com.vincenthuto.hemomancy.common.worldgen.PhlegethonticTerrainPlan;
import com.vincenthuto.hemomancy.common.worldgen.config.EscharianOvergrowthConfiguration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.neoforged.fml.loading.FMLEnvironment;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class EscharianOvergrowthFeature extends Feature<EscharianOvergrowthConfiguration> {
    private record Anchor(BlockPos pos, Direction supportNormal) {}
    private enum PlacementResult { SUCCESS, REJECTED, WRITE_FAILED }

    public EscharianOvergrowthFeature() {
        super(EscharianOvergrowthConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<EscharianOvergrowthConfiguration> context) {
        WorldGenLevel level = context.level();
        if (!level.getLevel().dimension().equals(Level.NETHER)) return false;
        long anchorSeed = context.random().nextLong();
        long layoutSeed = context.random().nextLong();
        List<Anchor> anchors = findAnchors(level, context.origin(), context.config(), anchorSeed);
        for (Anchor anchor : anchors) {
            PlacementResult result = placeAt(level, context.config(), anchor, layoutSeed);
            if (result == PlacementResult.SUCCESS) return true;
            if (result == PlacementResult.WRITE_FAILED) return false;
        }
        return false;
    }

    private static PlacementResult placeAt(WorldGenLevel level, EscharianOvergrowthConfiguration config,
                                           Anchor anchor, long layoutSeed) {
        Cell anchorCell = cell(anchor.pos());
        long seed = PhlegethonticRules.seed(layoutSeed, anchor.pos().getX(), anchor.pos().getZ(), anchor.pos().getY());
        int reach = EscharianOvergrowthLayout.REACH + EscharianOvergrowthSubstrate.OUTER_REACH + 3;
        List<BoundingBox> structures = protectedBounds(level, new EscharianOvergrowthLayout.Bounds(
                anchorCell.x()-reach, anchorCell.y()-reach, anchorCell.z()-reach,
                anchorCell.x()+reach, anchorCell.y()+reach, anchorCell.z()+reach));
        java.util.Map<Cell, Boolean> naturalCache = new java.util.HashMap<>(), airCache = new java.util.HashMap<>();
        java.util.function.Predicate<Cell> natural = c -> naturalCache.computeIfAbsent(c,
                key -> validNatural(level, pos(key), structures));
        java.util.function.Predicate<Cell> air = c -> airCache.computeIfAbsent(c,
                key -> validTarget(level, pos(key), structures) && level.isEmptyBlock(pos(key)));
        java.util.Map<Cell, Boolean> spacingCache = new java.util.HashMap<>();
        java.util.function.Predicate<Cell> growthSupport = c -> natural.test(c)
                && spacingCache.computeIfAbsent(c, key -> separatedFromGrowth(level, pos(key)));
        var proposed = anchor.supportNormal() == Direction.UP
                ? EscharianOvergrowthLayout.pile(seed, anchorCell, growthSupport, air)
                : EscharianOvergrowthLayout.patch(seed, anchorCell.relative(anchor.supportNormal().getOpposite()),
                        config, growthSupport, air);
        var substrate = EscharianOvergrowthSubstrate.generate(seed, proposed, natural, air);
        if (!substrate.containsValue(EscharianOvergrowthSubstrate.Material.INFESTED)
                || !substrate.containsValue(EscharianOvergrowthSubstrate.Material.VENOUS)) return PlacementResult.REJECTED;
        // Preflight both the colony and its terrain replacement before making any writes.
        if (substrate.keySet().stream().anyMatch(c -> !validNatural(level, pos(c), structures))) return PlacementResult.REJECTED;
        if (!EscharianOvergrowthPlacement.preflight(proposed,
                c -> validNatural(level, pos(c), structures),
                c -> validTarget(level, pos(c), structures) && level.isEmptyBlock(pos(c)))) return PlacementResult.REJECTED;
        for (var entry : substrate.entrySet()) {
            var current = level.getBlockState(pos(entry.getKey()));
            // A later colony's outer transition must not erase an earlier infested foundation.
            if (current.is(BlockInit.infested_venous_stone.get())) continue;
            var desired = (entry.getValue() == EscharianOvergrowthSubstrate.Material.INFESTED
                    ? BlockInit.infested_venous_stone.get() : BlockInit.venous_stone.get()).defaultBlockState();
            if (!current.equals(desired) && !level.setBlock(pos(entry.getKey()), desired, 3)) {
                writeFailure(pos(entry.getKey()), "substrate placement");
                return PlacementResult.WRITE_FAILED;
            }
        }
        for (var entry : proposed.backing().entrySet()) {
            var block = entry.getValue() == EscharianOvergrowthLayout.Layer.RIM
                    ? BlockInit.escharian_overgrowth_rim.get() : BlockInit.escharian_overgrowth.get();
            if (!level.setBlock(pos(entry.getKey()), block.defaultBlockState(), 3)) {
                writeFailure(pos(entry.getKey()), "backing placement");
                return PlacementResult.WRITE_FAILED;
            }
        }
        for (var entry : proposed.plants().entrySet()) {
            var state = BlockInit.escharian_scyphus.get().defaultBlockState()
                    .setValue(EscharianScyphusBlock.FACING, entry.getValue().facing())
                    .setValue(EscharianScyphusBlock.COUNT, entry.getValue().count());
            if (!level.setBlock(pos(entry.getKey()), state, 3)) {
                writeFailure(pos(entry.getKey()), "Scyphus placement");
                return PlacementResult.WRITE_FAILED;
            }
        }
        return PlacementResult.SUCCESS;
    }

    private static List<Anchor> findAnchors(WorldGenLevel level, BlockPos origin,
                                            EscharianOvergrowthConfiguration config, long anchorSeed) {
        ChunkPos chunk = new ChunkPos(origin);
        List<PhlegethonticBasinLayout> layouts = PhlegethonticBasinLayout.nearChunk(level.getSeed(), chunk.x, chunk.z,
                level.getLevel().getChunkSource().getGenerator().getSeaLevel());
        java.util.Map<Long, PhlegethonticBasinLayout.Column> columns = new java.util.HashMap<>();
        int minY = level.getMaxBuildHeight() - 1, maxY = level.getMinBuildHeight();
        for (int x = chunk.getMinBlockX(); x <= chunk.getMaxBlockX(); x++) for (int z = chunk.getMinBlockZ(); z <= chunk.getMaxBlockZ(); z++) {
            var column = PhlegethonticTerrainPlan.column(layouts, x, z);
            columns.put(BlockPos.asLong(x, 0, z), column);
            minY = Math.min(minY, column.surface() + 1);
            maxY = Math.max(maxY, Math.max(column.surface() + 6, (int)Math.floor(column.ceiling()) + 2));
        }
        var bounds = new EscharianOvergrowthLayout.Bounds(chunk.getMinBlockX(), Math.max(level.getMinBuildHeight(), minY),
                chunk.getMinBlockZ(), chunk.getMaxBlockX(), Math.min(level.getMaxBuildHeight() - 2, maxY), chunk.getMaxBlockZ());
        var candidates = EscharianOvergrowthAnchors.find(bounds, cell -> {
            BlockPos candidate = pos(cell);
            var column = columns.get(BlockPos.asLong(cell.x(), 0, cell.z()));
            return cell.y() > column.surface() && level.ensureCanWrite(candidate)
                    && level.isEmptyBlock(candidate) && level.getBiome(candidate).is(BiomeInit.PHLEGETHONTIC_BASIN);
        }, (cell, normal) -> {
            BlockPos support = pos(cell).relative(normal.getOpposite());
            if (!level.ensureCanWrite(support)) return false;
            var state = level.getBlockState(support);
            return state.is(PhlegethonticTags.OVERGROWTH_SUPPORT) && state.getFluidState().isEmpty()
                    && !state.hasBlockEntity() && state.isFaceSturdy(level, support, normal);
        }, anchorSeed, config.anchorSearchBudget(), config.groundPileChance());
        java.util.Map<Long, Boolean> ichorCache = new java.util.HashMap<>();
        List<Anchor> anchors = new ArrayList<>();
        for (var sampled : candidates) {
            BlockPos candidate = pos(sampled.cell());
            int below = sampled.normal() == Direction.UP ? config.ichorRadius() : EscharianOvergrowthAnchors.OVERHEAD_ICHOR_REACH;
            BlockPos ichor = nearestIchor(level, candidate, config.ichorRadius(), below, ichorCache);
            if (ichor != null) anchors.add(new Anchor(candidate, sampled.normal()));
        }
        return List.copyOf(anchors);
    }
    private static BlockPos nearestIchor(WorldGenLevel level, BlockPos anchor, int radius, int below,
                                         java.util.Map<Long, Boolean> cache) {
        BlockPos nearest = null;
        int nearestDistance = Integer.MAX_VALUE;
        for (int dx = -radius; dx <= radius; dx++) for (int dz = -radius; dz <= radius; dz++) {
            int horizontal = Math.max(Math.abs(dx), Math.abs(dz));
            if (horizontal > radius || horizontal > nearestDistance) continue;
            for (int dy = radius; dy >= -below; dy--) {
                if (anchor.getY() + dy < level.getMinBuildHeight() || anchor.getY() + dy >= level.getMaxBuildHeight()) continue;
                BlockPos target = anchor.offset(dx, dy, dz);
                if (cache.computeIfAbsent(target.asLong(), ignored ->
                        level.getFluidState(target).is(PhlegethonticTags.ICHOR))) {
                    int distance = horizontal;
                    if (distance < nearestDistance) { nearest = target; nearestDistance = distance; }
                }
            }
        }
        return nearest;
    }

    private static boolean separatedFromGrowth(WorldGenLevel level, BlockPos target) {
        for(int dx=-1;dx<=1;dx++)for(int dy=-1;dy<=1;dy++)for(int dz=-1;dz<=1;dz++) {
            if(Math.abs(dx)+Math.abs(dy)+Math.abs(dz)>2)continue;
            BlockPos neighbor=target.offset(dx,dy,dz);
            if(!level.hasChunk(neighbor.getX()>>4,neighbor.getZ()>>4))return false;
            var state=level.getBlockState(neighbor);
            if(state.is(BlockInit.escharian_overgrowth.get()) || state.is(BlockInit.escharian_overgrowth_rim.get()))return false;
        }
        return true;
    }

    private static boolean validNatural(WorldGenLevel level, BlockPos target, List<BoundingBox> structures) {
        if (!level.ensureCanWrite(target) || !level.getBiome(target).is(BiomeInit.PHLEGETHONTIC_BASIN)
                || structures.stream().anyMatch(box -> box.isInside(target))) return false;
        var state = level.getBlockState(target);
        return state.is(PhlegethonticTags.OVERGROWTH_SUPPORT) && state.getFluidState().isEmpty()
                && !state.hasBlockEntity() && state.isCollisionShapeFullBlock(level, target);
    }

    private static boolean validTarget(WorldGenLevel level, BlockPos target, List<BoundingBox> structures) {
        if (!level.ensureCanWrite(target) || !level.getBiome(target).is(BiomeInit.PHLEGETHONTIC_BASIN)) return false;
        var state = level.getBlockState(target);
        return state.isAir()
                && state.getFluidState().isEmpty() && !state.hasBlockEntity()
                && structures.stream().noneMatch(box -> box.isInside(target));
    }

    private static List<BoundingBox> protectedBounds(WorldGenLevel level, EscharianOvergrowthLayout.Bounds bounds) {
        List<BoundingBox> result = new ArrayList<>();
        int minChunkX = Math.floorDiv(bounds.minX(), 16), maxChunkX = Math.floorDiv(bounds.maxX(), 16);
        int minChunkZ = Math.floorDiv(bounds.minZ(), 16), maxChunkZ = Math.floorDiv(bounds.maxZ(), 16);
        Set<java.util.Map.Entry<net.minecraft.world.level.levelgen.structure.Structure, Long>> visitedStarts = new HashSet<>();
        for (int x = minChunkX; x <= maxChunkX; x++) for (int z = minChunkZ; z <= maxChunkZ; z++) {
            if (!level.hasChunk(x, z)) continue;
            var chunk = level.getChunk(x, z);
            for (var reference : chunk.getAllReferences().entrySet()) for (long origin : reference.getValue()) {
                if (!visitedStarts.add(java.util.Map.entry(reference.getKey(), origin))) continue;
                int originX = ChunkPos.getX(origin), originZ = ChunkPos.getZ(origin);
                if (!level.hasChunk(originX, originZ)) continue;
                var start = level.getChunk(originX, originZ).getStartForStructure(reference.getKey());
                if (start != null && start.isValid()) result.add(start.getBoundingBox());
            }
        }
        result.sort(Comparator.comparingInt(BoundingBox::minX).thenComparingInt(BoundingBox::minZ));
        return result;
    }

    private static Cell cell(BlockPos pos) { return new Cell(pos.getX(), pos.getY(), pos.getZ()); }
    private static BlockPos pos(Cell cell) { return new BlockPos(cell.x(), cell.y(), cell.z()); }

    private static void writeFailure(BlockPos pos, String phase) {
        if (!FMLEnvironment.production)
            Hemomancy.LOGGER.warn("Escharian Overgrowth {} failed at {}; remaining writes aborted", phase, pos);
    }

}
