package com.vincenthuto.hemomancy.client.render.tile.crafting;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.common.tile.crafting.MycelialCrucibleBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.joml.Matrix4f;

public class MycelialCrucibleRenderer implements BlockEntityRenderer<MycelialCrucibleBlockEntity> {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private static final float BASIN_MIN = 2.375F / 16.0F;
    private static final float BASIN_MAX = 13.625F / 16.0F;
    private static final float BASIN_Y = 10.035F / 16.0F;
    private static final int BASIN_SUBDIVISIONS = 8;
    private static final float BASIN_WAVE_HEIGHT = 0.012F;
    private static final float BASIN_WAVE_SPEED = 0.09F;
    private static final float SWIRL_INTENSITY = 2F;

    public MycelialCrucibleRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(MycelialCrucibleBlockEntity te) {
        return true;
    }

    @Override
    public void render(MycelialCrucibleBlockEntity te, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        renderBasinShader(te, partialTick, poseStack, buffers);
    }

    private static void renderBasinShader(MycelialCrucibleBlockEntity te, float partialTick, PoseStack poseStack,
            MultiBufferSource buffers) {
        if (te.getLevel() == null) {
            return;
        }
        Direction facing = te.getBlockState().getValue(FACING);
        float gameTime = te.getLevel().getGameTime() + partialTick;
        float basinSeed = (float) Math.floorMod(te.getBlockPos().asLong(), 10000L);
        VertexConsumer consumer = buffers.getBuffer(
                HemoRenderTypes.mycelialCrucibleBasin(gameTime, basinSeed, SWIRL_INTENSITY));

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotationForFacing(facing)));
        poseStack.translate(-0.5D, 0.0D, -0.5D);
        Matrix4f matrix = poseStack.last().pose();
        emitBasinSurface(consumer, matrix, gameTime, basinSeed);
        poseStack.popPose();
    }

    private static void emitBasinSurface(VertexConsumer consumer, Matrix4f matrix, float gameTime, float basinSeed) {
        float span = BASIN_MAX - BASIN_MIN;
        float step = span / BASIN_SUBDIVISIONS;

        for (int xIndex = 0; xIndex < BASIN_SUBDIVISIONS; xIndex++) {
            float x0 = BASIN_MIN + step * xIndex;
            float x1 = x0 + step;
            float u0 = (float) xIndex / BASIN_SUBDIVISIONS;
            float u1 = (float) (xIndex + 1) / BASIN_SUBDIVISIONS;

            for (int zIndex = 0; zIndex < BASIN_SUBDIVISIONS; zIndex++) {
                float z0 = BASIN_MIN + step * zIndex;
                float z1 = z0 + step;
                float v0 = (float) zIndex / BASIN_SUBDIVISIONS;
                float v1 = (float) (zIndex + 1) / BASIN_SUBDIVISIONS;

                emitBasinVertex(consumer, matrix, x0, basinWaveY(x0, z1, u0, v1, gameTime, basinSeed), z1, u0, v1);
                emitBasinVertex(consumer, matrix, x1, basinWaveY(x1, z1, u1, v1, gameTime, basinSeed), z1, u1, v1);
                emitBasinVertex(consumer, matrix, x1, basinWaveY(x1, z0, u1, v0, gameTime, basinSeed), z0, u1, v0);
                emitBasinVertex(consumer, matrix, x0, basinWaveY(x0, z0, u0, v0, gameTime, basinSeed), z0, u0, v0);
            }
        }
    }

    private static float basinWaveY(float x, float z, float u, float v, float gameTime, float basinSeed) {
        float phase = gameTime * BASIN_WAVE_SPEED + basinSeed * 0.017F;
        float waveA = (float) Math.sin((x * 18.0F + z * 9.0F) + phase);
        float waveB = (float) Math.sin((x * -11.0F + z * 16.0F) - phase * 0.73F);
        float edge = Math.min(Math.min(u, 1.0F - u), Math.min(v, 1.0F - v));
        float edgeDamping = Math.min(1.0F, Math.max(0.0F, edge / 0.18F));
        return BASIN_Y + (waveA * 0.68F + waveB * 0.32F) * BASIN_WAVE_HEIGHT * edgeDamping;
    }

    private static float rotationForFacing(Direction facing) {
        return switch (facing) {
            case SOUTH -> 180.0F;
            case WEST -> 270.0F;
            case EAST -> 90.0F;
            default -> 0.0F;
        };
    }

    private static void emitBasinVertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z,
            float u, float v) {
        consumer.addVertex(matrix, x, y, z)
                .setUv(u, v)
                .setColor(255, 255, 255, 230);
    }
}
