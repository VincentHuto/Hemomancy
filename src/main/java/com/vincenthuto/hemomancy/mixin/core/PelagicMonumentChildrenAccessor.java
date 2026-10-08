package com.vincenthuto.hemomancy.mixin.core;

import net.minecraft.world.level.levelgen.structure.structures.OceanMonumentPieces;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import java.util.List;

@Mixin(value = OceanMonumentPieces.MonumentBuilding.class, remap = false)
public interface PelagicMonumentChildrenAccessor {
    @Accessor("childPieces") List<net.minecraft.world.level.levelgen.structure.StructurePiece> hemomancy$children();
}
