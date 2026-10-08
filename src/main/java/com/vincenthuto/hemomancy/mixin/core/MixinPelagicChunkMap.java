package com.vincenthuto.hemomancy.mixin.core;

import com.mojang.datafixers.DataFixer;
import com.vincenthuto.hemomancy.common.worldgen.pelagic.PelagicWorldgen;
import net.minecraft.server.level.*;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

@Mixin(value = ChunkMap.class, remap = false)
public abstract class MixinPelagicChunkMap {
    @Shadow @Final private RandomState randomState;
    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/ChunkGenerator;createState(Lnet/minecraft/core/HolderLookup;Lnet/minecraft/world/level/levelgen/RandomState;J)Lnet/minecraft/world/level/chunk/ChunkGeneratorStructureState;"))
    private void hemomancy$shapeOcean(ServerLevel level, LevelStorageSource.LevelStorageAccess storage, DataFixer fixer,
            StructureTemplateManager structures, Executor executor, BlockableEventLoop<Runnable> main, LightChunkGetter light,
            ChunkGenerator generator, ChunkProgressListener progress, ChunkStatusUpdateListener status,
            Supplier<DimensionDataStorage> data, int distance, boolean sync, CallbackInfo ci) {
        PelagicWorldgen.initialize(level, generator, randomState);
    }
}
