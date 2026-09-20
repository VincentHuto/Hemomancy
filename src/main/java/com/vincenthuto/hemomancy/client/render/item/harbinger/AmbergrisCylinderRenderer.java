package com.vincenthuto.hemomancy.client.render.item.harbinger;
import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.*;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
public class AmbergrisCylinderRenderer extends BlockEntityWithoutLevelRenderer {
    public static final IClientItemExtensions EXTENSIONS=new IClientItemExtensions(){
        private AmbergrisCylinderRenderer renderer;
        @Override public BlockEntityWithoutLevelRenderer getCustomRenderer(){if(renderer==null)renderer=new AmbergrisCylinderRenderer();return renderer;}
    };
    private static final ModelPart CYLINDER=model();
    private static ModelPart model(){
        var mesh=new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("wax",CubeListBuilder.create().texOffs(0,0).addBox(-5,-2,-2,10,4,4).texOffs(0,8).addBox(-4,-2.7F,-1.4F,8,5.4F,2.8F),PartPose.ZERO);
        mesh.getRoot().addOrReplaceChild("ends",CubeListBuilder.create().texOffs(0,16).addBox(-5.5F,-2.5F,-2.5F,1,5,5).addBox(4.5F,-2.5F,-2.5F,1,5,5),PartPose.ZERO);
        return LayerDefinition.create(mesh,32,32).bakeRoot();
    }
    private AmbergrisCylinderRenderer(){super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),Minecraft.getInstance().getEntityModels());}
    public static void draw(ItemStack stack,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        boolean recorded=(stack.has(DataComponentInit.CLAIRAUDIOGRAPH_RECORDING.get()) || stack.has(DataComponentInit.ANCIENT_RECORDING.get()));
        CYLINDER.render(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(Hemomancy.rloc("textures/item/ambergris_cylinder"+(recorded?"_recorded":"")+".png"))),light,overlay);
        if(recorded && !stack.has(DataComponentInit.ANCIENT_RECORDING.get()))CYLINDER.render(pose,buffers.getBuffer(RenderTypeInit.getCrimsonGlint()),light,overlay);
    }
    @Override public void renderByItem(ItemStack stack,ItemDisplayContext context,PoseStack pose,MultiBufferSource buffers,int light,int overlay){pose.pushPose();pose.translate(.5,.5,.5);draw(stack,pose,buffers,light,overlay);pose.popPose();}
}
