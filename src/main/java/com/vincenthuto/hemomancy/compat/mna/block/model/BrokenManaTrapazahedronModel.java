package com.vincenthuto.hemomancy.compat.mna.block.model;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.compat.mna.tile.BrokenManaTrapazahedronBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BrokenManaTrapazahedronModel extends GeoModel<BrokenManaTrapazahedronBlockEntity> {
    private static final ResourceLocation modelFile = Hemomancy.rloc("geo/block/broken_mana_trapazahedron_armature.geo.json");
    private static final ResourceLocation animFile = Hemomancy.rloc( "animations/block/broken_mana_trapazahedron_armature.animation.json");
    private static final ResourceLocation texFile = Hemomancy.rloc( "textures/mna/broken_mana_trapazahedron.png");

    public BrokenManaTrapazahedronModel() {
    }

    public ResourceLocation getAnimationResource(BrokenManaTrapazahedronBlockEntity arg0) {
        return animFile;
    }

    public ResourceLocation getModelResource(BrokenManaTrapazahedronBlockEntity arg0) {
        return modelFile;
    }

    public ResourceLocation getTextureResource(BrokenManaTrapazahedronBlockEntity arg0) {
        return texFile;
    }
}
