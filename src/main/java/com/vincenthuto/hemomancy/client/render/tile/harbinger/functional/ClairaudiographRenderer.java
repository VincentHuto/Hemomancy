package com.vincenthuto.hemomancy.client.render.tile.harbinger.functional;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.ClairaudiographBlock;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.ClairaudiographBlockEntity;
import com.vincenthuto.hemomancy.client.render.item.harbinger.AmbergrisCylinderRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
public class ClairaudiographRenderer implements BlockEntityRenderer<ClairaudiographBlockEntity> {
    public ClairaudiographRenderer(BlockEntityRendererProvider.Context context){}
    @Override public void render(ClairaudiographBlockEntity be,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        var mc=Minecraft.getInstance();float time=be.getLevel()==null?0:be.getLevel().getGameTime()+partial;
        pose.pushPose();pose.translate(.5,0,.5);pose.mulPose(Axis.YP.rotationDegrees(-be.getBlockState().getValue(ClairaudiographBlock.FACING).toYRot()+180));pose.translate(-.5,0,-.5);
        if(!be.inventory.getStackInSlot(1).isEmpty()){
            pose.pushPose();pose.translate(.47,.39,.39);
            if(be.playing() || be.progress>0)pose.mulPose(Axis.XP.rotationDegrees(time*9));
            pose.scale(.8F,.8F,.8F);AmbergrisCylinderRenderer.draw(be.inventory.getStackInSlot(1),pose,buffers,light,overlay);pose.popPose();
        }
        pose.pushPose();pose.translate(be.progress>0?Math.min(80,be.progress+partial)/80F*.35:0,0,0);
        var stylus=mc.getModelManager().getModel(ModelResourceLocation.standalone(Hemomancy.rloc("block/clairaudiograph_stylus")));
        mc.getBlockRenderer().getModelRenderer().renderModel(pose.last(),buffers.getBuffer(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS)),null,stylus,1,1,1,light,overlay);pose.popPose();
        if(!be.inventory.getStackInSlot(0).isEmpty()){
            pose.pushPose();pose.translate(.83,.34,.7);pose.scale(.3F,.3F,.3F);
            mc.getItemRenderer().renderStatic(be.inventory.getStackInSlot(0),ItemDisplayContext.FIXED,light,overlay,pose,buffers,be.getLevel(),0);pose.popPose();
        }
        if(be.progress>0){
            pose.pushPose();float travel=(time%16)/16F;pose.translate(.8-travel*.25,.23,.69-travel*.23);
            var feed=mc.getModelManager().getModel(ModelResourceLocation.standalone(Hemomancy.rloc("block/clairaudiograph_feed")));
            mc.getBlockRenderer().getModelRenderer().renderModel(pose.last(),buffers.getBuffer(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS)),null,feed,1,1,1,light,overlay);pose.popPose();
        }
        pose.popPose();
    }
}
