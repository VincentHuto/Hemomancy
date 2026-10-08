package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.worldgen.pelagic.*;
import com.vincenthuto.hemomancy.mixin.core.PelagicMonumentChildrenAccessor;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.*;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.structures.*;
import net.neoforged.neoforge.gametest.*;
import java.util.List;

@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class PelagicGameTests {
    @GameTest(template = "phlegethontic_test_room", timeoutTicks = 40)
    public static void snailBearingVentModifierTargetsOnlyHydrothermalDepths(GameTestHelper h) {
        var biomes = h.getLevel().registryAccess().registryOrThrow(Registries.BIOME);
        int owners = 0;
        for (var biome : biomes.holders().toList()) {
            long copies = biome.value().getGenerationSettings().features().stream()
                    .flatMap(net.minecraft.core.HolderSet::stream)
                    .filter(feature -> feature.is(com.vincenthuto.hemomancy.common.init.PlacedFeatureInit.DEEP_OCEAN_VENT)).count();
            boolean hydrothermal = biome.is(PelagicBiomes.key(PelagicLayer.HYDROTHERMAL));
            h.assertTrue(copies == (hydrothermal ? 1 : 0),
                    "Snail-bearing vent biome modifier assigned " + copies + " copies to " + biome.key().location());
            if (copies > 0) owners++;
        }
        h.assertTrue(owners == 1, "The existing vent feature must have exactly one home biome");
        h.succeed();
    }

    @GameTest(template = "phlegethontic_test_room", timeoutTicks = 40)
    public static void monumentReloadRestoresEveryRoomAtEveryOrientation(GameTestHelper h) {
        for (var direction : Direction.Plane.HORIZONTAL) {
            var original = new OceanMonumentPieces.MonumentBuilding(RandomSource.create(42), -61, -45, direction);
            var baseline = OceanMonumentStructure.regeneratePiecesAfterLoad(new ChunkPos(-2, -1), 42,
                    new PiecesContainer(List.of(original))).pieces().getFirst();
            original.move(0, -92, 0);
            var restored = OceanMonumentStructure.regeneratePiecesAfterLoad(new ChunkPos(-2, -1), 42,
                    new PiecesContainer(List.of(original))).pieces().getFirst();
            h.assertTrue(restored.getBoundingBox().minY() == -53, "Loaded monument must retain the saved seabed elevation");
            var before = ((PelagicMonumentChildrenAccessor)baseline).hemomancy$children();
            var after = ((PelagicMonumentChildrenAccessor)restored).hemomancy$children();
            h.assertTrue(!after.isEmpty() && before.size() == after.size(), "Room graph must survive reconstruction");
            for (int i = 0; i < before.size(); i++) {
                var a = before.get(i).getBoundingBox(); var b = after.get(i).getBoundingBox();
                h.assertTrue(b.minY() == a.minY() - 92 && b.maxY() == a.maxY() - 92
                        && a.minX() == b.minX() && a.minZ() == b.minZ(), "Every room must move with the parent");
            }
        }
        h.succeed();
    }

    @GameTest(template = "phlegethontic_test_room", timeoutTicks = 40)
    public static void biomeTagsAndGeneratorBoundariesAreLoaded(GameTestHelper h) {
        var registry = h.getLevel().registryAccess().registryOrThrow(Registries.BIOME);
        for (var layer : PelagicLayer.values()) {
            var biome = registry.getHolderOrThrow(PelagicBiomes.key(layer));
            h.assertTrue(biome.is(BiomeTags.IS_OVERWORLD), "Every Pelagic biome must be an Overworld biome");
            h.assertTrue(biome.is(layer == PelagicLayer.SHORE ? BiomeTags.IS_BEACH : BiomeTags.IS_OCEAN), "Missing habitat tag for " + layer);
        }
        for (var level : h.getLevel().getServer().getAllLevels()) {
            if (level.dimension().equals(Level.OVERWORLD)) continue;
            h.assertTrue(PelagicWorldgen.context(level.getChunkSource().randomState()) == null, "Other dimensions must retain their density routers");
        }
        h.succeed();
    }
}
