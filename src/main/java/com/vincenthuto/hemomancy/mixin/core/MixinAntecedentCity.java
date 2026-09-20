package com.vincenthuto.hemomancy.mixin.core;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pools.*;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Optional;

@Mixin(value=JigsawStructure.class,remap=false)
public abstract class MixinAntecedentCity {
    @Shadow @Final private Holder<StructureTemplatePool> startPool;
    @Inject(method="findGenerationPoint",at=@At("RETURN"),cancellable=true)
    private void hemomancy$appendVigil(Structure.GenerationContext context,CallbackInfoReturnable<Optional<Structure.GenerationStub>> callback) {
        if(!startPool.is(ResourceLocation.withDefaultNamespace("ancient_city/city_center")) || callback.getReturnValue().isEmpty()) return;
        var original=callback.getReturnValue().get();
        callback.setReturnValue(Optional.of(new Structure.GenerationStub(original.position(),builder->{
            var pieces=original.getPiecesBuilder().build().pieces();
            pieces.forEach(builder::addPiece);
            com.vincenthuto.hemomancy.common.antecedent.VigilPlacement.find(
                    context.structureTemplateManager(), context.heightAccessor(), pieces).forEach(builder::addPiece);
        })));
    }
}
