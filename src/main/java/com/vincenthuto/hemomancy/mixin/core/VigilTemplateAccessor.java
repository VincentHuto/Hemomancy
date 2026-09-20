package com.vincenthuto.hemomancy.mixin.core;

import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import java.util.List;

@Mixin(value = StructureTemplate.class, remap = false)
public interface VigilTemplateAccessor {
    @Accessor("palettes") List<StructureTemplate.Palette> hemomancy$palettes();
}
