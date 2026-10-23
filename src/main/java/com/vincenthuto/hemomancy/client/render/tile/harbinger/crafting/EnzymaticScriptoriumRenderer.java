package com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import com.vincenthuto.hemomancy.common.block.harbinger.crafting.EnzymaticScriptoriumBlock;
import com.vincenthuto.hemomancy.client.render.RenderTypeInit;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.EnzymaticScriptoriumBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import org.joml.Matrix4f;

public final class EnzymaticScriptoriumRenderer implements BlockEntityRenderer<EnzymaticScriptoriumBlockEntity> {
    private static final int[][] COLORS = {{225, 25, 32}, {231, 95, 25}, {224, 207, 45}, {245, 237, 222},
            {25, 90, 37}, {35, 115, 220}, {155, 155, 155}, {130, 45, 175}};
    /** Feed-bead travel in blocks per tick, and the pause between a jar's repeating beads. */
    private static final float BEAD_SPEED = 1.5F / 16F;
    private static final float BEAD_GAP = 0.5F;
    /** Each jar's full feed line for each stage: its own capillary, then the stage's shared riser and nib. */
    private static final float[][][] PATHS = new float[3][8][];
    private static final float[][][] DISTANCES = new float[3][8][];

    static {
        for (int stage = 0; stage < 3; stage++) {
            for (int jar = 0; jar < 8; jar++) {
                float[] feed = ScriptoriumGeometry.FEEDS[jar];
                float[] tail = ScriptoriumGeometry.TAILS[stage];
                float[] path = new float[feed.length + tail.length];
                System.arraycopy(feed, 0, path, 0, feed.length);
                System.arraycopy(tail, 0, path, feed.length, tail.length);
                float[] distance = new float[path.length / 3];
                for (int p = 1; p < distance.length; p++) {
                    float dx = path[p * 3] - path[p * 3 - 3];
                    float dy = path[p * 3 + 1] - path[p * 3 - 2];
                    float dz = path[p * 3 + 2] - path[p * 3 - 1];
                    distance[p] = distance[p - 1] + (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
                }
                PATHS[stage][jar] = path;
                DISTANCES[stage][jar] = distance;
            }
        }
    }

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
        int stage = station.getBlockState().getValue(EnzymaticScriptoriumBlock.STAGE);
        pose.pushPose();
        pose.translate(0.5D, 0, 0.5D);
        pose.mulPose(Axis.YP.rotationDegrees(switch (facing) {
            case EAST -> -90; case SOUTH -> 180; case WEST -> 90; default -> 0;
        }));
        pose.translate(-0.5D, 0, -0.5D);
        float time = AnimationTime.gameTime(station.getLevel(), partialTick);
        pose.pushPose();
        pose.translate(0.5D, 0.97D + Math.sin(time * 0.045D) * 0.015D, 0.375D);
        pose.mulPose(Axis.YP.rotationDegrees(90));
        pose.mulPose(Axis.ZP.rotationDegrees(45));
        pose.scale(0.8F, 0.8F, 0.8F);
        book.setupAnim(time, 0.15F, 0.85F, 1.0F);
        book.renderToBuffer(pose, buffers.getBuffer(book.renderType(BOOK_TEXTURE)), light, overlay, -1);
        pose.popPose();
        Matrix4f matrix = pose.last().pose();
        if (stage >= 1) {
            int alpha = 200 + (int) (20 * Math.sin(time * .035F));
            glyphs(buffers.getBuffer(RenderTypeInit.RITE_BOUNDARY_CORE), matrix, ScriptoriumGeometry.RUBRIC_QUADS, alpha);
        }
        if (stage >= 2) {
            // Scraped writing surfaces and fades again on the palimpsest leaves.
            int alpha = 110 + (int) (90 * Math.sin(time * .02F));
            glyphs(buffers.getBuffer(RenderTypeInit.RITE_BOUNDARY_CORE), matrix, ScriptoriumGeometry.PALIMPSEST_QUADS, alpha);
        }
        for (int i = 0; i < 8; i++) {
            int stored = station.getItem(i).getCount();
            if (stored == 0) continue;
            float half = ScriptoriumGeometry.FLUID_HALF;
            float x = ScriptoriumGeometry.JAR_X[i];
            float z = ScriptoriumGeometry.JAR_Z[i];
            float bottom = ScriptoriumGeometry.FLUID_BOTTOM;
            float top = bottom + (stored / 64.0F) * ScriptoriumGeometry.FLUID_HEIGHT;
            int[] color = COLORS[i];
            // Switching render types can close the previous shared buffer; acquire it at each draw.
            box(buffers.getBuffer(RenderTypeInit.RITE_BOUNDARY_CORE), matrix, x - half, bottom, z - half,
                    x + half, top, z + half, color[0], color[1], color[2], 155);
            int doses = station.selected(i);
            if (doses > 0) {
                float brightness = .65F + .35F * (float) Math.sin(time * .22F + i);
                box(buffers.getBuffer(RenderTypeInit.RITE_BOUNDARY_GLOW), matrix, x - half + .012F, top - .025F, z - half + .012F,
                        x + half - .012F, top + .01F, z + half - .012F,
                        Math.min(255, (int) (color[0] * brightness + 30)),
                        Math.min(255, (int) (color[1] * brightness + 30)),
                        Math.min(255, (int) (color[2] * brightness + 30)), 215);
                beads(buffers, matrix, stage, i, doses, time, color);
            }
        }
        pose.popPose();
    }

    /** Selected doses travel as glowing beads down the jar's capillary, through the hub and into the grimoire. */
    private static void beads(MultiBufferSource buffers, Matrix4f matrix, int stage, int jar, int doses, float time, int[] color) {
        float[] path = PATHS[stage][jar];
        float[] distance = DISTANCES[stage][jar];
        float length = distance[distance.length - 1];
        float cycle = length + BEAD_GAP;
        float[] point = new float[3];
        for (int dose = 0; dose < doses; dose++) {
            float head = (time * BEAD_SPEED + jar * .37F + dose * cycle / doses) % cycle;
            for (int trail = 0; trail < 3; trail++) {
                float s = head - trail * .045F;
                if (s < 0 || s > length) continue;
                locate(path, distance, s, point);
                float size = (trail == 0 ? .05F : .035F - trail * .006F);
                int alpha = 220 - trail * 70;
                int r = Math.min(255, color[0] + 60), g = Math.min(255, color[1] + 60), b = Math.min(255, color[2] + 60);
                box(buffers.getBuffer(RenderTypeInit.RITE_BOUNDARY_GLOW), matrix,
                        point[0] - size, point[1] - size, point[2] - size,
                        point[0] + size, point[1] + size, point[2] + size, color[0], color[1], color[2], alpha / 2);
                float core = size * .45F;
                box(buffers.getBuffer(RenderTypeInit.RITE_BOUNDARY_CORE), matrix,
                        point[0] - core, point[1] - core, point[2] - core,
                        point[0] + core, point[1] + core, point[2] + core, r, g, b, alpha);
            }
        }
    }

    private static void locate(float[] path, float[] distance, float s, float[] out) {
        int segment = 1;
        while (segment < distance.length - 1 && distance[segment] < s) segment++;
        float span = distance[segment] - distance[segment - 1];
        float t = span <= 0 ? 0 : (s - distance[segment - 1]) / span;
        for (int k = 0; k < 3; k++) {
            float from = path[(segment - 1) * 3 + k];
            out[k] = from + (path[segment * 3 + k] - from) * t;
        }
    }

    private static void glyphs(VertexConsumer out, Matrix4f matrix, float[][] quads, int alpha) {
        for (float[] q : quads) {
            quad(out, matrix, q[0], q[1], q[2], q[3], q[4], q[5], q[6], q[7], q[8], q[9], q[10], q[11],
                    255, 48, 64, alpha);
        }
    }

    private static void box(VertexConsumer out, Matrix4f matrix, float x0, float y0, float z0,
            float x1, float y1, float z1, int r, int g, int b, int a) {
        quad(out, matrix, x0,y1,z0, x1,y1,z0, x1,y1,z1, x0,y1,z1, r,g,b,a);
        quad(out, matrix, x0,y0,z0, x0,y0,z1, x1,y0,z1, x1,y0,z0, r,g,b,a);
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
