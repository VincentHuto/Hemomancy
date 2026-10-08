package com.vincenthuto.hemomancy.client.render.entity.mob.aquatic;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.aquatic.PelagicCreatureModel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

public class PelagicCreatureRenderer<T extends Mob> extends MobRenderer<T, PelagicCreatureModel<T>> {
    private final ResourceLocation texture;
    private final PelagicCreatureModel.Style style;
    public PelagicCreatureRenderer(EntityRendererProvider.Context context, PelagicCreatureModel.Style style) {
        super(context, new PelagicCreatureModel<>(context.bakeLayer(style.layer()), style), .2F);
        this.style = style; texture = Hemomancy.rloc("textures/entity/pelagic/" + style.id + ".png");
        if (style.luminous) addLayer(new RenderLayer<>(this) {
            private final ResourceLocation glow = Hemomancy.rloc("textures/entity/pelagic/" + style.id + "_glow.png");
            @Override public void render(PoseStack pose, MultiBufferSource buffers, int light, T entity,
                                         float swing, float amount, float partial, float age, float yaw, float pitch) {
                if (!entity.isInvisible()) getParentModel().renderGlow(pose, buffers.getBuffer(RenderType.entityTranslucentEmissive(glow)), age);
            }
        });
    }
    @Override public ResourceLocation getTextureLocation(T entity) { return texture; }
    @Override protected RenderType getRenderType(T entity, boolean visible, boolean translucent, boolean glowing) {
        if (visible && style.translucent) return RenderType.entityTranslucent(texture);
        return super.getRenderType(entity, visible, translucent, glowing);
    }
}
