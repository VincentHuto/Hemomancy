package com.vincenthuto.hemomancy.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hutoslib.math.Vector3;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Renders the Qliphoth Seed item entity and attaches short HutosLib tendril
 * effects to it, as if the seed is seeking purchase in the ground.
 */
public class QliphothSeedItemEntityRenderer extends EntityRenderer<ItemEntity> {

    public QliphothSeedItemEntityRenderer(Context ctx) {
        super(ctx);
        this.shadowRadius = 0.15F;
        this.shadowStrength = 0.75F;
    }

    @Override
    public ResourceLocation getTextureLocation(ItemEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }

    @Override
    public void render(ItemEntity entityIn, float entityYaw, float partialTicks, PoseStack poseStack,
            MultiBufferSource bufferIn, int packedLightIn) {

        ItemStack itemstack = entityIn.getItem();
        if (itemstack.isEmpty()) {
            super.render(entityIn, entityYaw, partialTicks, poseStack, bufferIn, packedLightIn);
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.22D, 0.0D);
        float f3 = (entityIn.getAge() + partialTicks) / 20.0F + entityIn.bobOffs;
        poseStack.mulPose(Vector3.YP.rotation(f3).toMoj());
        QliphothSeedItemRenderer.renderSeedBody(ItemDisplayContext.GROUND, poseStack, bufferIn,
                packedLightIn, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
        poseStack.popPose();

        QliphothSeedTendrilEffects.spawnForEntity(entityIn, partialTicks);

        super.render(entityIn, entityYaw, partialTicks, poseStack, bufferIn, packedLightIn);
    }

}
