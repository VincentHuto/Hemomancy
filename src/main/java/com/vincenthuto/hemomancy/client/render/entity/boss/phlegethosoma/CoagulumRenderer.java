package com.vincenthuto.hemomancy.client.render.entity.boss.phlegethosoma;
import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.boss.phlegethosoma.TemporaryCoagulumEntity;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
public final class CoagulumRenderer extends EntityRenderer<TemporaryCoagulumEntity> {
    private final net.minecraft.client.renderer.block.BlockRenderDispatcher blocks;
    public CoagulumRenderer(EntityRendererProvider.Context c) {super(c);blocks=c.getBlockRenderDispatcher();}
    @Override public ResourceLocation getTextureLocation(TemporaryCoagulumEntity e) {return net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS;}
    @Override public void render(TemporaryCoagulumEntity e,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light) {
        pose.pushPose();pose.scale(1.5F,1.5F,1.5F);pose.translate(-.5,0,-.5);
        blocks.renderSingleBlock(BlockInit.blood_scorched_scab.get().defaultBlockState(),pose,buffers,light,OverlayTexture.NO_OVERLAY);pose.popPose();
        super.render(e,yaw,partial,pose,buffers,light);
    }
}
