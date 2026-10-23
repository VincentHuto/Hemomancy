package com.vincenthuto.hemomancy.client.render.tile.harbinger.functional;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.ClairaudiographBlock;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.ClairaudiographBlockEntity;
import com.vincenthuto.hemomancy.client.render.item.harbinger.AmbergrisCylinderRenderer;
import com.vincenthuto.hemomancy.common.item.harbinger.CylinderMedia;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
/** Anchors are block pixels mirrored from tools/model_export/clairaudiograph.py. */
public class ClairaudiographRenderer implements BlockEntityRenderer<ClairaudiographBlockEntity> {
    private static final float PX=1/16F,CARVE_TICKS=80;
    public ClairaudiographRenderer(BlockEntityRendererProvider.Context context){}
    @Override public void render(ClairaudiographBlockEntity be,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        var mc=Minecraft.getInstance();float time=be.getLevel()==null?0:AnimationTime.gameTime(be.getLevel(), partial);
        pose.pushPose();pose.translate(.5,0,.5);pose.mulPose(Axis.YP.rotationDegrees(-be.getBlockState().getValue(ClairaudiographBlock.FACING).toYRot()+180));pose.translate(-.5,0,-.5);
        // Cylinder on the lathe mandrel, spinning about its own axis.
        var cylinder=be.inventory.getStackInSlot(1);
        if(!cylinder.isEmpty()){
            pose.pushPose();pose.translate(8*PX,7.25*PX,5.5*PX);
            if(be.playing() || be.progress>0)pose.mulPose(Axis.XP.rotationDegrees(time*9));
            if(CylinderMedia.supported(cylinder))AmbergrisCylinderRenderer.draw(cylinder,pose,buffers,light,overlay);
            else{pose.scale(.55F,.55F,.55F);mc.getItemRenderer().renderStatic(cylinder,ItemDisplayContext.FIXED,light,overlay,pose,buffers,be.getLevel(),0);}
            pose.popPose();
        }
        // The carver walks the lead screw from the left end of the wax while carving.
        pose.pushPose();pose.translate(be.progress>0?Math.min(CARVE_TICKS,be.progress+partial)/CARVE_TICKS*8*PX:0,0,0);
        var stylus=mc.getModelManager().getModel(ModelResourceLocation.standalone(Hemomancy.rloc("block/clairaudiograph_stylus")));
        mc.getBlockRenderer().getModelRenderer().renderModel(pose.last(),buffers.getBuffer(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS)),null,stylus,1,1,1,light,overlay);pose.popPose();
        // Sample hangs on the lancet inside the sampler's glass chamber.
        var sample=be.inventory.getStackInSlot(0);
        if(!sample.isEmpty()){
            pose.pushPose();pose.translate(3.25*PX,10.25*PX,11.75*PX);pose.scale(.22F,.22F,.22F);
            mc.getItemRenderer().renderStatic(sample,ItemDisplayContext.FIXED,light,overlay,pose,buffers,be.getLevel(),0);pose.popPose();
        }
        if(com.vincenthuto.hemomancy.common.antecedent.AhaematicSample.is(sample)) {
            pose.pushPose();pose.translate(3.25*PX-.1,8.5*PX-.075,11.75*PX-.1);pose.scale(.2F,.5F,.2F);
            float response=be.playing() && be.program()!=null && be.program().unresolved(be.elapsed())?Math.clamp((be.elapsed()-1100)/180F,0,1):0;
            AntecedentVesselRenderer.draw(pose,buffers,light,overlay,time,response,false,false);pose.popPose();
        }
        // A bolus of blood runs down the feed line into the left bearing while carving.
        if(be.progress>0){
            pose.pushPose();float travel=(time%16)/16F;pose.translate(PX,7.875*PX,(10.5-travel*4.25)*PX);
            var feed=mc.getModelManager().getModel(ModelResourceLocation.standalone(Hemomancy.rloc("block/clairaudiograph_feed")));
            mc.getBlockRenderer().getModelRenderer().renderModel(pose.last(),buffers.getBuffer(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS)),null,feed,1,1,1,light,overlay);pose.popPose();
        }
        pose.popPose();
    }
}
