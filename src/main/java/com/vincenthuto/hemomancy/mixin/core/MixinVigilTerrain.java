package com.vincenthuto.hemomancy.mixin.core;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Beardifier;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** The buried chamber carves its own interior; its bounding box must not carve a second cavern. */
@Mixin(value = Beardifier.class, remap = false)
public abstract class MixinVigilTerrain {
    // The call lives in forStructuresInChunk's synthetic lambda. Match the invocation rather
    // than a compiler-assigned lambda name, and require exactly one matching call site.
    @Redirect(method = "*", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/levelgen/structure/StructurePiece;isCloseToChunk(Lnet/minecraft/world/level/ChunkPos;I)Z"),
            require = 1, allow = 1)
    private static boolean hemomancy$preserveVigilTerrain(StructurePiece piece, ChunkPos chunk, int radius) {
        if (piece instanceof PoolElementStructurePiece pool
                && pool.getElement() instanceof SinglePoolElement single
                && ((SinglePoolElementAccessor) single).hemomancy$template().left()
                    .filter(id -> id.getNamespace().equals(Hemomancy.MOD_ID)
                            && (id.getPath().equals("antecedent/vigil") || id.getPath().startsWith("antecedent/vigil/"))).isPresent()) {
            return false;
        }
        return piece.isCloseToChunk(chunk, radius);
    }
}
