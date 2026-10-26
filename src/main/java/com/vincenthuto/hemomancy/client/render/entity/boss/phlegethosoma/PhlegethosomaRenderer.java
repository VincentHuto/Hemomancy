package com.vincenthuto.hemomancy.client.render.entity.boss.phlegethosoma;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.boss.phlegethosoma.PhlegethosomaEntity;
import com.vincenthuto.hemomancy.client.model.entity.boss.phlegethosoma.PhlegethosomaModel;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
public final class PhlegethosomaRenderer extends MobRenderer<PhlegethosomaEntity,PhlegethosomaModel> {
    private static final ResourceLocation TEXTURE=Hemomancy.rloc("textures/entity/phlegethosoma/phlegethosoma.png");
    public PhlegethosomaRenderer(EntityRendererProvider.Context c) {super(c,new PhlegethosomaModel(c.bakeLayer(PhlegethosomaModel.LAYER_LOCATION)),.65F);addLayer(new com.vincenthuto.hemomancy.client.render.layer.mob.PhlegethosomaGlowLayer(this));}
    @Override public boolean shouldRender(PhlegethosomaEntity entity,net.minecraft.client.renderer.culling.Frustum frustum,
            double x,double y,double z) {
        return entity.shouldRender(x,y,z) && frustum.isVisible(entity.getBoundingBoxForCulling().inflate(9));
    }
    @Override public ResourceLocation getTextureLocation(PhlegethosomaEntity e) {return TEXTURE;}
}
