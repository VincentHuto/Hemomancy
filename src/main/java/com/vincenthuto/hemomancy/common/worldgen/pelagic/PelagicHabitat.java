package com.vincenthuto.hemomancy.common.worldgen.pelagic;

import com.vincenthuto.hemomancy.common.worldgen.pelagic.PelagicHabitatRules.Species;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;

/** Small, loaded-chunk-only queries shared by aquatic spawning, steering and growth placement. */
public final class PelagicHabitat {
    private PelagicHabitat() {}

    public static boolean loaded(LevelReader level, BlockPos pos) {
        return !level.isOutsideBuildHeight(pos) && level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4);
    }

    @Nullable public static PelagicLayer layer(LevelReader level, BlockPos pos) {
        if (!loaded(level, pos)) return null;
        var biome = level.getBiome(pos);
        for (var layer : PelagicLayer.values()) if (biome.is(PelagicBiomes.key(layer))) return layer;
        return null;
    }

    public static boolean sourceWater(LevelReader level, BlockPos pos) {
        return loaded(level, pos) && level.getBlockState(pos).is(Blocks.WATER)
                && level.getFluidState(pos).isSource();
    }

    public static boolean waterClearance(LevelReader level, BlockPos pos, int radius, int below, int above) {
        for (var p : BlockPos.betweenClosed(pos.offset(-radius, -below, -radius), pos.offset(radius, above, radius))) {
            if (!loaded(level, p) || !level.getFluidState(p).is(FluidTags.WATER)
                    || !level.getBlockState(p).getCollisionShape(level, p).isEmpty()) return false;
        }
        return true;
    }

    public static int floorDistance(LevelReader level, BlockPos pos, int limit) {
        for (int dy = 1; dy <= limit; dy++) {
            var p = pos.below(dy);
            if (!loaded(level, p)) return limit + 1;
            if (level.getBlockState(p).isFaceSturdy(level, p, Direction.UP)) return dy;
        }
        return limit + 1;
    }

    public static boolean mineral(BlockState state) {
        return state.is(Blocks.BASALT) || state.is(Blocks.SMOOTH_BASALT) || state.is(Blocks.TUFF)
                || state.is(Blocks.BLACKSTONE) || state.is(Blocks.MAGMA_BLOCK);
    }

    public static boolean coastalRock(BlockState state) {
        return state.is(BlockTags.BASE_STONE_OVERWORLD) || mineral(state) || state.is(Blocks.ANDESITE);
    }

    public static boolean nearBlock(LevelReader level, BlockPos pos, int radius, int vertical,
                                    java.util.function.Predicate<BlockState> predicate) {
        for (var p : BlockPos.betweenClosed(pos.offset(-radius, -vertical, -radius), pos.offset(radius, vertical, radius)))
            if (loaded(level, p) && predicate.test(level.getBlockState(p))) return true;
        return false;
    }

    public static boolean wetRock(LevelReader level, BlockPos pos) {
        if (!loaded(level, pos.below()) || !coastalRock(level.getBlockState(pos.below()))) return false;
        return nearBlock(level, pos, 2, 1, state -> state.getFluidState().is(FluidTags.WATER));
    }

    public static boolean suitable(LevelReader level, BlockPos pos, Species species) {
        var layer = layer(level, pos);
        if (!PelagicHabitatRules.inRange(species, layer, pos.getY())) return false;
        boolean shore = species == Species.CHITON || species == Species.HEMOLYMPHOPODA;
        if (!bodyClearance(level, pos, species)) return false;
        return PelagicHabitatRules.suitable(species, new PelagicHabitatRules.Site(layer, pos.getY(), true,
                level.getFluidState(pos).is(FluidTags.WATER), true, floorDistance(level, pos, 4),
                shore && wetRock(level, pos),
                species == Species.HAGFISH && nearBlock(level, pos, 6, 4, s -> s.is(Blocks.BONE_BLOCK)),
                species == Species.SNAIL && nearBlock(level, pos, 4, 3, s -> s.is(Blocks.MAGMA_BLOCK))
                        && nearBlock(level, pos, 2, 2, PelagicHabitat::mineral)));
    }

    private static boolean bodyClearance(LevelReader level, BlockPos pos, Species species) {
        if (!loaded(level, pos)) return false;
        return switch (species) {
            case CHITON, HEMOLYMPHOPODA -> level.getBlockState(pos).getCollisionShape(level, pos).isEmpty();
            case WHALE -> waterClearance(level, pos, 2, 2, 2);
            case SIPHONOPHORE -> waterClearance(level, pos, 0, 3, 1);
            case PYROSOME -> waterClearance(level, pos, 0, 0, 1);
            case VAMPIRE_SQUID -> waterClearance(level, pos, 1, 1, 1);
            default -> waterClearance(level, pos, 0, 0, 0);
        };
    }

    public static double depthCorrection(LevelReader level, BlockPos pos, Species species) {
        var layer = layer(level, pos);
        if (layer == null || layer == PelagicLayer.REEF || layer == PelagicLayer.SHORE) return 0;
        return PelagicHabitatRules.depthCorrection(species, pos.getY());
    }

    @Nullable public static Vec3 target(PathfinderMob mob, Species species, boolean bottom) {
        Vec3 best = null;
        int bestScore = -1;
        for (int i = 0; i < 12; i++) {
            var p = mob.blockPosition().offset(mob.getRandom().nextInt(13) - 6,
                    mob.getRandom().nextInt(7) - 3, mob.getRandom().nextInt(13) - 6);
            if (bottom) {
                int floor = floorDistance(mob.level(), p, 6);
                if (floor > 6) continue;
                p = p.below(floor - 1);
            }
            boolean fits = suitable(mob.level(), p, species);
            // A transplanted animal can still move. Habitat is a preference, not an invisible wall.
            if (!fits && (!bodyClearance(mob.level(), p, species) || bottom && floorDistance(mob.level(), p, 2) > 2)) continue;
            int score = fits ? layer(mob.level(), p) == species.home ? 2 : 1 : 0;
            if (species == Species.CHITON && mob.level().getBlockState(p).is(com.vincenthuto.hemomancy.common.init.BlockInit.hematic_algal_crust.get())) score += 2;
            if (score > bestScore) { best = Vec3.atBottomCenterOf(p); bestScore = score; }
        }
        return best;
    }
}
