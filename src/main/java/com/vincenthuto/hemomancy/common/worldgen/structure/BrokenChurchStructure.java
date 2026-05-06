package com.vincenthuto.hemomancy.common.worldgen.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.SpecimenJarBlock;
import com.vincenthuto.hemomancy.common.entity.boss.annetta.AnnettaKnowlesEntity;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.StructureInit;
import com.vincenthuto.hemomancy.common.tile.functional.SpecimenJarBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;

import java.util.Optional;

public class BrokenChurchStructure extends Structure {
    private static final int MAX_PLACEMENT_ATTEMPTS = 32;
    private static final int INITIAL_CENTERED_ATTEMPTS = 4;

    public static final MapCodec<BrokenChurchStructure> CODEC = RecordCodecBuilder
            .<BrokenChurchStructure>mapCodec(instance -> instance.group(BrokenChurchStructure.settingsCodec(instance),
                            StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(structure -> structure.startPool),
                            ResourceLocation.CODEC.optionalFieldOf("start_jigsaw_name")
                                    .forGetter(structure -> structure.startJigsawName),
                            Codec.intRange(0, 30).fieldOf("size").forGetter(structure -> structure.size),
                            HeightProvider.CODEC.fieldOf("start_height").forGetter(structure -> structure.startHeight),
                            Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap")
                                    .forGetter(structure -> structure.projectStartToHeightmap),
                            Codec.intRange(1, 128).fieldOf("max_distance_from_center")
                                    .forGetter(structure -> structure.maxDistanceFromCenter))
                    .apply(instance, BrokenChurchStructure::new));

    private final Holder<StructureTemplatePool> startPool;
    private final Optional<ResourceLocation> startJigsawName;
    private final int size;
    private final HeightProvider startHeight;
    private final Optional<Heightmap.Types> projectStartToHeightmap;
    private final int maxDistanceFromCenter;

    public BrokenChurchStructure(StructureSettings config, Holder<StructureTemplatePool> startPool,
                                 Optional<ResourceLocation> startJigsawName, int size,
                                 HeightProvider startHeight,
                                 Optional<Heightmap.Types> projectStartToHeightmap,
                                 int maxDistanceFromCenter) {
        super(config);
        this.startPool = startPool;
        this.startJigsawName = startJigsawName;
        this.size = size;
        this.startHeight = startHeight;
        this.projectStartToHeightmap = projectStartToHeightmap;
        this.maxDistanceFromCenter = maxDistanceFromCenter;
    }

    private static boolean extraSpawningChecks(GenerationContext context) {
        return StructurePlacementChecks.isSuitableLandChunk(context);
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        if (!BrokenChurchStructure.extraSpawningChecks(context)) {
            return Optional.empty();
        }

        int startY = this.startHeight.sample(context.random(),
                new WorldGenerationContext(context.chunkGenerator(), context.heightAccessor()));
        ChunkPos chunkPos = context.chunkPos();
        BlockPos blockPos = new BlockPos(chunkPos.getMinBlockX(), startY, chunkPos.getMinBlockZ());

        return JigsawPlacement.addPieces(context, this.startPool, this.startJigsawName, this.size,
                blockPos, false, this.projectStartToHeightmap, this.maxDistanceFromCenter,
                PoolAliasLookup.EMPTY, JigsawStructure.DEFAULT_DIMENSION_PADDING,
                LiquidSettings.APPLY_WATERLOGGING);
    }

    @Override
    public StructureType<?> type() {
        return StructureInit.broken_church.get();
    }

    @Override
    public void afterPlace(WorldGenLevel level, StructureManager structureManager,
                           ChunkGenerator chunkGenerator, RandomSource random, BoundingBox chunkBox,
                           ChunkPos chunkPos, PiecesContainer pieces) {
        BoundingBox fullBox = pieces.calculateBoundingBox();
        int floorY = fullBox.minY();
        int maxY = (fullBox.minY() + fullBox.maxY()) / 2;
        int cornerX = fullBox.minX() + 4;
        int cornerZ = fullBox.minZ() + 4;

        if (!chunkBox.isInside(cornerX, floorY, cornerZ)) {
            return;
        }

        BlockPos annettaPos = findFloor(level, random, cornerX, cornerZ, floorY, maxY, 3)
                .orElse(new BlockPos(cornerX, floorY + 1, cornerZ));
        spawnMob(level, EntityInit.annetta_knowles.get(), annettaPos);
        dressCornerScene(level, random, annettaPos);
    }

    private void dressCornerScene(WorldGenLevel level, RandomSource random, BlockPos annettaPos) {
        BlockPos jarPos = annettaPos.offset(1, 0, 0);
        placeJar(level, jarPos);

        placeIfAir(level, annettaPos.offset(-1, 0, 0), BlockInit.devils_tooth.get().defaultBlockState());
        placeIfAir(level, annettaPos.offset(0, 0, 1), BlockInit.devils_tooth.get().defaultBlockState());
        placeIfAir(level, annettaPos.offset(2, 0, 1), BlockInit.devils_tooth.get().defaultBlockState());

        for (int i = 0; i < 8; i++) {
            int dx = random.nextInt(7) - 3;
            int dz = random.nextInt(7) - 3;
            if ((dx == 0 && dz == 0) || (dx == 1 && dz == 0)) {
                continue;
            }
            BlockPos pos = annettaPos.offset(dx, -1, dz);
            BlockState state = random.nextBoolean()
                    ? BlockInit.hemolytic_plating_block.get().defaultBlockState()
                    : Blocks.BONE_BLOCK.defaultBlockState();
            if (level.getBlockState(pos).isFaceSturdy(level, pos, Direction.UP)) {
                level.setBlock(pos.above(), state, 2);
            }
        }
    }

    private void placeJar(WorldGenLevel level, BlockPos pos) {
        BlockState state = BlockInit.specimen_jar.get().defaultBlockState()
                .setValue(SpecimenJarBlock.FACING, Direction.WEST);
        level.setBlock(pos, state, 2);
        if (level.getBlockEntity(pos) instanceof SpecimenJarBlockEntity jar) {
            CompoundTag specimen = new CompoundTag();
            specimen.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(EntityInit.tooth_pecks.get()).toString());
            jar.setSpecimen(specimen);
        }
    }

    private void placeIfAir(WorldGenLevel level, BlockPos pos, BlockState state) {
        if (level.getBlockState(pos).isAir() && level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)) {
            level.setBlock(pos, state, 2);
        }
    }

    private Optional<BlockPos> findFloor(WorldGenLevel level, RandomSource random,
                                         int originX, int originZ, int floorY, int maxY, int spread) {
        for (int attempt = 0; attempt < MAX_PLACEMENT_ATTEMPTS; attempt++) {
            int dx = (attempt < INITIAL_CENTERED_ATTEMPTS) ? 0 : random.nextInt(spread * 2 + 1) - spread;
            int dz = (attempt < INITIAL_CENTERED_ATTEMPTS) ? 0 : random.nextInt(spread * 2 + 1) - spread;
            int startY = floorY + (attempt % INITIAL_CENTERED_ATTEMPTS);

            BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(originX + dx, startY, originZ + dz);
            for (int y = startY; y <= maxY; y++) {
                mutable.setY(y);
                if (level.getBlockState(mutable).isAir()
                        && level.getBlockState(mutable.below()).isFaceSturdy(level, mutable.below(), Direction.UP)) {
                    return Optional.of(mutable.immutable());
                }
            }
        }
        return Optional.empty();
    }

    private <T extends Entity> void spawnMob(WorldGenLevel level, EntityType<T> type, BlockPos pos) {
        T entity = type.create(level.getLevel());
        if (entity == null) return;
        entity.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                level.getRandom().nextFloat() * 360.0f, 0.0f);
        if (entity instanceof Mob mob) {
            mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null);
            mob.setPersistenceRequired();
            if (mob instanceof AnnettaKnowlesEntity annetta) {
                annetta.setCowering();
            }
        }
        level.addFreshEntityWithPassengers(entity);
    }
}
