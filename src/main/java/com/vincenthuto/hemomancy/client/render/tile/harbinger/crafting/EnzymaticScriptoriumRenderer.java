package com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.enchanting.ScriptoriumLayout;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import com.vincenthuto.hemomancy.common.block.harbinger.crafting.EnzymaticScriptoriumBlock;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.EnzymaticScriptoriumBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import org.joml.Matrix4f;

public final class EnzymaticScriptoriumRenderer implements BlockEntityRenderer<EnzymaticScriptoriumBlockEntity> {
    private static final int[][] COLORS = {{225, 25, 32}, {231, 95, 25}, {224, 207, 45}, {245, 237, 222},
            {25, 90, 37}, {35, 115, 220}, {155, 155, 155}, {130, 45, 175}};

    private static final ResourceLocation BOOK_TEXTURE = Hemomancy.rloc("textures/entity/liber_sanguinum.png");
    private final BookModel book;

    public EnzymaticScriptoriumRenderer(BlockEntityRendererProvider.Context context) {
        book = new BookModel(context.bakeLayer(ModelLayers.BOOK));
    }

    @Override public boolean shouldRenderOffScreen(EnzymaticScriptoriumBlockEntity station) {
        return true;
    }

    @Override public void render(EnzymaticScriptoriumBlockEntity station, float partialTick, PoseStack pose,
            MultiBufferSource buffers, int light, int overlay) {
        if (station.getLevel() == null) return;
        Direction facing = station.getBlockState().getValue(EnzymaticScriptoriumBlock.FACING);
        pose.pushPose();
        pose.translate(0.5D, 0, 0.5D);
        pose.mulPose(Axis.YP.rotationDegrees(switch (facing) {
            case EAST -> -90; case SOUTH -> 180; case WEST -> 90; default -> 0;
        }));
        pose.translate(-0.5D, 0, -0.5D);
        float time = station.getLevel().getGameTime() + partialTick;
        pose.pushPose();
        pose.translate(0.5D, 0.97D + Math.sin(time * 0.045D) * 0.015D, 0.375D);
        pose.mulPose(Axis.YP.rotationDegrees(90));
        pose.mulPose(Axis.ZP.rotationDegrees(45));
        pose.scale(0.8F, 0.8F, 0.8F);
        book.setupAnim(time, 0.15F, 0.85F, 1.0F);
        book.renderToBuffer(pose, buffers.getBuffer(book.renderType(BOOK_TEXTURE)), light, overlay, -1);
        pose.popPose();
        Matrix4f matrix = pose.last().pose();
        for (int i = 0; i < 8; i++) {
            int stored = station.getItem(i).getCount();
            if (stored == 0) continue;
            float x = ScriptoriumLayout.tubeX(i) - 1 / 16.0F;
            float z = ScriptoriumLayout.tubeZ(i) - 1 / 16.0F;
            float top = ScriptoriumLayout.FLUID_BOTTOM + (stored / 64.0F) * 3 * ScriptoriumLayout.FLUID_UNIT_HEIGHT;
            int[] color = COLORS[i];
            // Switching render types can close the previous shared buffer; acquire it at each draw.
            box(buffers.getBuffer(RenderTypeInit.RITE_BOUNDARY_CORE), matrix, x + .018F, ScriptoriumLayout.FLUID_BOTTOM, z + .018F,
                    x + 2 / 16.0F - .018F, top, z + 2 / 16.0F - .018F,
                    color[0], color[1], color[2], 155);
            if (station.selected(i) > 0) {
                float brightness = .65F + .35F * (float) Math.sin(time * .22F + i);
                box(buffers.getBuffer(RenderTypeInit.RITE_BOUNDARY_GLOW), matrix, x + .03F, top - .025F, z + .03F,
                        x + 2 / 16.0F - .03F, top + .01F, z + 2 / 16.0F - .03F,
                        Math.min(255, (int) (color[0] * brightness + 30)),
                        Math.min(255, (int) (color[1] * brightness + 30)),
                        Math.min(255, (int) (color[2] * brightness + 30)), 215);
            }
        }
        pose.popPose();
    }

    private static void box(VertexConsumer out, Matrix4f matrix, float x0, float y0, float z0,
            float x1, float y1, float z1, int r, int g, int b, int a) {
        quad(out, matrix, x0,y1,z0, x1,y1,z0, x1,y1,z1, x0,y1,z1, r,g,b,a);
        quad(out, matrix, x0,y0,z0, x1,y0,z0, x1,y1,z0, x0,y1,z0, r,g,b,a);
        quad(out, matrix, x1,y0,z1, x0,y0,z1, x0,y1,z1, x1,y1,z1, r,g,b,a);
        quad(out, matrix, x0,y0,z1, x0,y0,z0, x0,y1,z0, x0,y1,z1, r,g,b,a);
        quad(out, matrix, x1,y0,z0, x1,y0,z1, x1,y1,z1, x1,y1,z0, r,g,b,a);
    }

    private static void quad(VertexConsumer out, Matrix4f matrix,
            float ax,float ay,float az, float bx,float by,float bz,
            float cx,float cy,float cz, float dx,float dy,float dz,
            int r,int g,int b,int a) {
        out.addVertex(matrix, ax, ay, az).setColor(r,g,b,a);
        out.addVertex(matrix, bx, by, bz).setColor(r,g,b,a);
        out.addVertex(matrix, cx, cy, cz).setColor(r,g,b,a);
        out.addVertex(matrix, dx, dy, dz).setColor(r,g,b,a);
    }
}

