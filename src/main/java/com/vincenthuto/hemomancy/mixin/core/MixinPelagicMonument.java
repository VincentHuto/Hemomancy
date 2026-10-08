package com.vincenthuto.hemomancy.mixin.core;

import com.vincenthuto.hemomancy.common.worldgen.pelagic.PelagicWorldgen;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.*;
import net.minecraft.world.level.levelgen.structure.structures.OceanMonumentStructure;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import java.util.Optional;

@Mixin(value = OceanMonumentStructure.class, remap = false)
public abstract class MixinPelagicMonument {
    @Inject(method = "findGenerationPoint", at = @At("HEAD"), cancellable = true)
    private void hemomancy$stableFootprint(Structure.GenerationContext context,
            CallbackInfoReturnable<Optional<Structure.GenerationStub>> cir) {
        if (PelagicWorldgen.context(context.randomState()) == null) return;
        int[] floors = hemomancy$footprint(context);
        if (floors[1] - floors[0] > 20 || floors[1] > 36) cir.setReturnValue(Optional.empty());
    }

    @Inject(method = "generatePieces", at = @At("TAIL"))
    private static void hemomancy$anchor(StructurePiecesBuilder builder, Structure.GenerationContext context, CallbackInfo ci) {
        if (PelagicWorldgen.context(context.randomState()) == null) return;
        var piece = builder.build().pieces().getFirst();
        hemomancy$move(piece, hemomancy$footprint(context)[1] - piece.getBoundingBox().minY());
    }

    @Inject(method = "regeneratePiecesAfterLoad", at = @At("RETURN"))
    private static void hemomancy$restoreDepth(ChunkPos chunk, long seed, PiecesContainer saved,
            CallbackInfoReturnable<PiecesContainer> cir) {
        if (saved.isEmpty() || cir.getReturnValue().isEmpty()) return;
        var rebuilt = cir.getReturnValue().pieces().getFirst();
        hemomancy$move(rebuilt, saved.pieces().getFirst().getBoundingBox().minY() - rebuilt.getBoundingBox().minY());
    }

    @Unique private static int[] hemomancy$footprint(Structure.GenerationContext context) {
        int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        for (int dx = -29; dx <= 29; dx += 14) for (int dz = -29; dz <= 29; dz += 14) {
            int y = context.chunkGenerator().getBaseHeight(context.chunkPos().getMinBlockX() + dx,
                    context.chunkPos().getMinBlockZ() + dz, Heightmap.Types.OCEAN_FLOOR_WG,
                    context.heightAccessor(), context.randomState());
            min = Math.min(min, y); max = Math.max(max, y);
        }
        return new int[]{min, max};
    }

    @Unique private static void hemomancy$move(StructurePiece piece, int dy) {
        if (dy == 0) return;
        piece.move(0, dy, 0);
        for (var child : ((PelagicMonumentChildrenAccessor)piece).hemomancy$children()) child.move(0, dy, 0);
    }
}
