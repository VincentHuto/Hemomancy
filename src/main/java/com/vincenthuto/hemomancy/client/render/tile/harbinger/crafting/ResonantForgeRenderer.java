package com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.crafting.ResonantForgeBlock;
import com.vincenthuto.hemomancy.common.enchanting.ResonantForgeRules;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.ResonantForgeBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Matrix4f;

public class ResonantForgeRenderer implements BlockEntityRenderer<ResonantForgeBlockEntity> {
    public ResonantForgeRenderer(BlockEntityRendererProvider.Context context) {}

    @Override public boolean shouldRenderOffScreen(ResonantForgeBlockEntity forge) { return true; }
    @Override public int getViewDistance() { return 96; }

    @Override public void render(ResonantForgeBlockEntity forge, float partial, PoseStack pose,
            MultiBufferSource buffers, int light, int overlay) {
        if (forge.getLevel() == null) return;
        Direction facing = forge.getBlockState().getValue(ResonantForgeBlock.FACING);
        float time = forge.getLevel().getGameTime() + partial;
        float phase = forge.totalTicks() == 0 ? 0 : Math.min(1, (forge.progress() + partial) / forge.totalTicks());
        Minecraft mc = Minecraft.getInstance();

        pose.pushPose();
        pose.translate(.5, 0, .5);
        pose.mulPose(Axis.YP.rotationDegrees(switch (facing) {
            case EAST -> -90; case SOUTH -> 180; case WEST -> 90; default -> 0;
        }));
        pose.translate(-.5, 0, -.5);

        boolean hammering = forge.operation() == ResonantForgeBlockEntity.Operation.APPLY;
        float strike = hammering ? (float) Math.pow(Math.max(0, Math.sin(phase * Math.PI * 16)), 5) : 0;
        float hammerWear = Math.min(1F, forge.hammerUses() / (float) ResonantForgeRules.HAMMER_SERVICE_INTERVAL);
        pose.pushPose();
        pose.translate(0, -strike * .55, 0);
        pose.translate(-.5, .65, .25);
        pose.scale(1F - hammerWear * .18F, 1F - hammerWear * .35F, 1F - hammerWear * .12F);
        pose.translate(.5, -.65, -.25);
        renderModel(mc, "resonant_forge_hammer", pose, buffers, light, overlay);
        pose.popPose();

        pose.pushPose();
        pivotRotate(pose, -.5, .0625, .875, Axis.XP, hammering ? -18F * strike : 0);
        renderModel(mc, "resonant_forge_treadle", pose, buffers, light, overlay);
        pose.popPose();

        pose.pushPose();
        pivotRotate(pose, -.5, 1.19, .375, Axis.ZP, hammering ? time * 22F : 0);
        renderModel(mc, "resonant_forge_cam", pose, buffers, light, overlay);
        pose.popPose();

        boolean grinding = forge.operation() == ResonantForgeBlockEntity.Operation.GRIND
                || forge.operation() == ResonantForgeBlockEntity.Operation.STABILIZE;
        float wheelWear = Math.min(1F, forge.wheelUses() / (float) ResonantForgeRules.WHEEL_SERVICE_INTERVAL);
        pose.pushPose();
        pivotRotate(pose, 1.5, .94, .5, Axis.ZP, grinding ? time * 24 : 0);
        if (wheelWear > .5F) pivotRotate(pose, 1.5, .94, .5, Axis.YP,
                (float) Math.sin(time * .45F) * 7 * (wheelWear - .5F) * 2);
        renderModel(mc, "resonant_forge_wheel", pose, buffers, light, overlay);
        pose.popPose();

        pose.pushPose();
        pivotRotate(pose, 1.5, 1.18, .69, Axis.XP, grinding ? -9F + (float) Math.sin(time * .8F) * 5F : 0);
        renderModel(mc, "resonant_forge_stylus", pose, buffers, light, overlay);
        pose.popPose();

        renderItem(mc, forge.getItem(ResonantForgeBlockEntity.APPLICATION_ITEM), pose, buffers, light, overlay,
                -.5, .48, .52, .55F, hammering ? strike * 3 : 0);
        renderItem(mc, forge.getItem(ResonantForgeBlockEntity.APPLICATION_CYLINDER), pose, buffers, light, overlay,
                -.78, .84, .48, .4F, hammering ? time * 8 : 0);
        renderItem(mc, forge.getItem(ResonantForgeBlockEntity.GRINDING_ITEM), pose, buffers, light, overlay,
                1.5, .48, .65, .45F, 0);
        renderItem(mc, forge.getItem(ResonantForgeBlockEntity.GRINDING_CYLINDER), pose, buffers, light, overlay,
                1.72, .91, .35, .4F, grinding ? time * 12 : 0);
        if (forge.hammerIronDeposited()) renderItem(mc, new net.minecraft.world.item.ItemStack(BlockInit.hematic_iron_block.get()),
                pose, buffers, light, overlay, -.55, .66, .7, .35F, 0);
        if (forge.hammerAshDeposited()) renderItem(mc, new net.minecraft.world.item.ItemStack(BlockInit.smouldering_ash_trail.get()),
                pose, buffers, light, overlay, -.35, .68, .68, .28F, 0);
        if (forge.wheelAshDeposited()) renderItem(mc, new net.minecraft.world.item.ItemStack(BlockInit.befouling_ash_trail.get()),
                pose, buffers, light, overlay, 1.5, .7, .68, .28F, 0);

        if (forge.getBloodVolume() > 0) {
            float height = .25F + .18F * (float) (forge.getBloodVolume() / ResonantForgeRules.BLOOD_CAPACITY);
            Matrix4f matrix = pose.last().pose();
            box(buffers.getBuffer(RenderTypeInit.RITE_BOUNDARY_CORE), matrix,
                    .18F, height, .18F, .82F, height + .025F, .82F, 155, 12, 18, 205);
            if (forge.operation() != ResonantForgeBlockEntity.Operation.NONE)
                box(buffers.getBuffer(RenderTypeInit.RITE_BOUNDARY_GLOW), matrix,
                        .22F, height + .026F, .22F, .78F, height + .04F, .78F, 235, 45, 54, 175);
        }
        pose.popPose();
    }

    private static void renderModel(Minecraft mc, String name, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        var model = mc.getModelManager().getModel(ModelResourceLocation.standalone(Hemomancy.rloc("block/" + name)));
        mc.getBlockRenderer().getModelRenderer().renderModel(pose.last(),
                buffers.getBuffer(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS)), null,
                model, 1, 1, 1, light, overlay);
    }

    private static void pivotRotate(PoseStack pose, double x, double y, double z, Axis axis, float degrees) {
        pose.translate(x, y, z);
        pose.mulPose(axis.rotationDegrees(degrees));
        pose.translate(-x, -y, -z);
    }

    private static void renderItem(Minecraft mc, net.minecraft.world.item.ItemStack stack, PoseStack pose,
            MultiBufferSource buffers, int light, int overlay, double x, double y, double z, float scale, float spin) {
        if (stack.isEmpty()) return;
        pose.pushPose(); pose.translate(x, y, z); pose.mulPose(Axis.YP.rotationDegrees(spin));
        pose.mulPose(Axis.XP.rotationDegrees(90)); pose.scale(scale, scale, scale);
        mc.getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, light, overlay, pose, buffers, null, 0);
        pose.popPose();
    }

    private static void box(VertexConsumer out, Matrix4f matrix, float x0, float y0, float z0,
            float x1, float y1, float z1, int r, int g, int b, int a) {
        quad(out,matrix,x0,y1,z0,x1,y1,z0,x1,y1,z1,x0,y1,z1,r,g,b,a);
        quad(out,matrix,x0,y0,z0,x1,y0,z0,x1,y1,z0,x0,y1,z0,r,g,b,a);
        quad(out,matrix,x1,y0,z1,x0,y0,z1,x0,y1,z1,x1,y1,z1,r,g,b,a);
        quad(out,matrix,x0,y0,z1,x0,y0,z0,x0,y1,z0,x0,y1,z1,r,g,b,a);
        quad(out,matrix,x1,y0,z0,x1,y0,z1,x1,y1,z1,x1,y1,z0,r,g,b,a);
    }

    private static void quad(VertexConsumer out, Matrix4f matrix,
            float ax,float ay,float az,float bx,float by,float bz,float cx,float cy,float cz,float dx,float dy,float dz,
            int r,int g,int b,int a) {
        out.addVertex(matrix,ax,ay,az).setColor(r,g,b,a); out.addVertex(matrix,bx,by,bz).setColor(r,g,b,a);
        out.addVertex(matrix,cx,cy,cz).setColor(r,g,b,a); out.addVertex(matrix,dx,dy,dz).setColor(r,g,b,a);
    }
}
