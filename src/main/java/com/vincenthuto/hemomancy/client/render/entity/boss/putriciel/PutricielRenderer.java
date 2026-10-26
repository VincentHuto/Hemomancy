package com.vincenthuto.hemomancy.client.render.entity.boss.putriciel;

import com.vincenthuto.hemomancy.client.render.entity.boss.SaintRiseRender;
import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.boss.putriciel.PutricielModel;
import com.vincenthuto.hemomancy.client.model.entity.boss.putriciel.PutricielAnimState;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielVisualRules;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import com.vincenthuto.hemomancy.client.render.layer.mob.PutricielSporeGlowLayer;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class PutricielRenderer extends MobRenderer<PutricielEntity, PutricielModel<PutricielEntity>> {
    private static final ResourceLocation TEXTURE =
            Hemomancy.rloc("textures/entity/boss/putriciel/putriciel.png");

    public PutricielRenderer(Context context) {
        super(context, new PutricielModel<>(context.bakeLayer(PutricielModel.LAYER_LOCATION)), 1.0F);
        this.addLayer(new PutricielSporeGlowLayer<>(this));
    }

    @Override
    public ResourceLocation getTextureLocation(PutricielEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(PutricielEntity entity, PoseStack poseStack, float partialTick) {
        SaintRiseRender.sink(poseStack, entity, partialTick);
        poseStack.scale(PutricielVisualRules.SCALE, PutricielVisualRules.SCALE, PutricielVisualRules.SCALE);
    }

    @Override
    public void render(PutricielEntity entity, float yaw, float partial, PoseStack poses, MultiBufferSource buffers, int light) {
        super.render(entity, yaw, partial, poses, buffers, light);
        float bodyYaw = Mth.rotLerp(partial, entity.yBodyRotO, entity.yBodyRot);
        Vec3[] sacs = java.util.Arrays.stream(model.sacs()).map(p -> offset(p, bodyYaw)).toArray(Vec3[]::new);
        Vec3[] censers = java.util.Arrays.stream(model.censers()).map(p -> offset(p, bodyYaw)).toArray(Vec3[]::new);
        PutricielAnimState.of(entity).attachments(offset(model.core(), bodyYaw), offset(model.palm(), bodyYaw), sacs, censers);
    }

    private static Vec3 offset(Vector3f point, float yaw) {
        float scale = PutricielVisualRules.SCALE;
        point.set(-point.x * scale, (1.501F - point.y) * scale, point.z * scale);
        return new Vec3(point.rotateY((180 - yaw) * Mth.DEG_TO_RAD));
    }
}
