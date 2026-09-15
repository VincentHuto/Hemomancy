package com.vincenthuto.hemomancy.mixin.core;

import com.vincenthuto.hemomancy.common.capability.HemoAttachmentTypes;
import com.vincenthuto.hemomancy.common.worldgen.feature.PhlegethonticWorldgenFeature;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.StaticCache2D;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.concurrent.CompletableFuture;

/** Finish the queued feature after neighboring ore decoration, before computing its emitted light. */
@Mixin(ChunkStatusTasks.class)
public class MixinPhlegethonticVeinCompletion {
    @Inject(method="light",at=@At("HEAD"),remap=false)
    private static void hemomancy$finishVein(WorldGenContext context,ChunkStep step,
            StaticCache2D<GenerationChunkHolder> cache,ChunkAccess chunk,
            CallbackInfoReturnable<CompletableFuture<ChunkAccess>> cir) {
        if(chunk.hasData(HemoAttachmentTypes.PHLEGETHONTIC_PENDING_VEIN))
            PhlegethonticWorldgenFeature.finishVein(new WorldGenRegion(context.level(),cache,step,chunk),chunk);
    }
}
