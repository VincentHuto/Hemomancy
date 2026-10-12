package com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.MycelialCrucibleBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.joml.Matrix4f;

/**
 * Draws the Crucible's living parts over its baked model: the swirling culture pool, the cistern's
 * blood at its stored level, and the culture being worked turning above the pool. Positions mirror
 * tools/model_export/mycelial_crucible.py in the north-facing main block's pixels.
 */
public class MycelialCrucibleRenderer implements BlockEntityRenderer<MycelialCrucibleBlockEntity> {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final ModelResourceLocation CISTERN_BLOOD =
            ModelResourceLocation.standalone(Hemomancy.rloc("block/mycelial_crucible_blood"));
    private static final float BASIN_CENTRE = 8.0F / 16.0F;
    private static final float BASIN_APOTHEM = 5.8F / 16.0F;
    private static final float BASIN_Y = 10.035F / 16.0F;
    private static final int BASIN_RINGS = 4;
    private static final int BASIN_EDGE_STEPS = 2;
    private static final float BASIN_WAVE_HEIGHT = 0.012F;
    private static final float BASIN_WAVE_SPEED = 0.09F;
    private static final float SWIRL_INTENSITY = 2F;
    private static final float TAN = (float) Math.tan(Math.PI / 8);
    /** Cistern blood model: one pixel tall from the vat floor, stretched to the stored level. */
    private static final float CISTERN_FLOOR = 3.55F / 16.0F;
    private static final float CISTERN_DEPTH = 15.6F;
    private static final float ITEM_Y = 11.4F / 16.0F;

    public MycelialCrucibleRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(MycelialCrucibleBlockEntity te) {
        return true;
    }

    @Override
    public void render(MycelialCrucibleBlockEntity te, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        if (te.getLevel() == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotationForFacing(te.getBlockState().getValue(FACING))));
        poseStack.translate(-0.5D, 0.0D, -0.5D);
        renderCisternBlood(te, poseStack, buffers, light, overlay);
        renderCulture(te, partialTick, poseStack, buffers, light, overlay);
        renderBasinShader(te, partialTick, poseStack, buffers);
        poseStack.popPose();
    }

    private static void renderCisternBlood(MycelialCrucibleBlockEntity te, PoseStack poseStack,
            MultiBufferSource buffers, int light, int overlay) {
        double max = te.getMaxBloodVolume();
        float fill = max <= 0 ? 0 : (float) Math.min(1, te.getBloodVolume() / max);
        if (fill <= 0) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        poseStack.pushPose();
        poseStack.translate(0, CISTERN_FLOOR, 0);
        poseStack.scale(1, CISTERN_DEPTH * fill, 1);
        poseStack.translate(0, -CISTERN_FLOOR, 0);
        mc.getBlockRenderer().getModelRenderer().renderModel(poseStack.last(),
                buffers.getBuffer(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS)), null,
                mc.getModelManager().getModel(CISTERN_BLOOD), 1, 1, 1, light, overlay);
        poseStack.popPose();
    }

    /** The scar or culture in the centre slot turns slowly under the dropper while it is worked. */
    private static void renderCulture(MycelialCrucibleBlockEntity te, float partialTick, PoseStack poseStack,
            MultiBufferSource buffers, int light, int overlay) {
        ItemStack culture = te.getCenterStack();
        if (culture.isEmpty()) {
            return;
        }
        float time = te.getLevel().getGameTime() + partialTick;
        poseStack.pushPose();
        poseStack.translate(0.5F, ITEM_Y + (float) Math.sin(time * 0.07F) * 0.3F / 16.0F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(time * 0.9F));
        poseStack.scale(0.35F, 0.35F, 0.35F);
        Minecraft.getInstance().getItemRenderer().renderStatic(culture, ItemDisplayContext.FIXED, light, overlay,
                poseStack, buffers, te.getLevel(), 0);
        poseStack.popPose();
    }

    private static void renderBasinShader(MycelialCrucibleBlockEntity te, float partialTick, PoseStack poseStack,
            MultiBufferSource buffers) {
        float gameTime = te.getLevel().getGameTime() + partialTick;
        float basinSeed = (float) Math.floorMod(te.getBlockPos().asLong(), 10000L);
        VertexConsumer consumer = buffers.getBuffer(
                HemoRenderTypes.mycelialCrucibleBasin(gameTime, basinSeed, SWIRL_INTENSITY));
        emitBasinSurface(consumer, poseStack.last().pose(), gameTime, basinSeed);
    }

    /**
     * The pool fills the bowl's octagon: rings from the centre out to each octagon edge, with UVs laid
     * flat across the pool so the shader's swirl stays continuous.
     */
    private static void emitBasinSurface(VertexConsumer consumer, Matrix4f matrix, float gameTime, float basinSeed) {
        float s = BASIN_APOTHEM * TAN;
        float[][] corners = {{BASIN_APOTHEM, -s}, {BASIN_APOTHEM, s}, {s, BASIN_APOTHEM}, {-s, BASIN_APOTHEM},
                {-BASIN_APOTHEM, s}, {-BASIN_APOTHEM, -s}, {-s, -BASIN_APOTHEM}, {s, -BASIN_APOTHEM}};
        int steps = corners.length * BASIN_EDGE_STEPS;
        float[][] edge = new float[steps + 1][];
        for (int i = 0; i <= steps; i++) {
            float[] a = corners[(i / BASIN_EDGE_STEPS) % corners.length];
            float[] b = corners[(i / BASIN_EDGE_STEPS + 1) % corners.length];
            float t = (float) (i % BASIN_EDGE_STEPS) / BASIN_EDGE_STEPS;
            edge[i] = new float[] {a[0] + (b[0] - a[0]) * t, a[1] + (b[1] - a[1]) * t};
        }
        for (int ring = 0; ring < BASIN_RINGS; ring++) {
            float inner = (float) ring / BASIN_RINGS;
            float outer = (float) (ring + 1) / BASIN_RINGS;
            for (int i = 0; i < steps; i++) {
                emitBasinVertex(consumer, matrix, edge[i], inner, gameTime, basinSeed);
                emitBasinVertex(consumer, matrix, edge[i + 1], inner, gameTime, basinSeed);
                emitBasinVertex(consumer, matrix, edge[i + 1], outer, gameTime, basinSeed);
                emitBasinVertex(consumer, matrix, edge[i], outer, gameTime, basinSeed);
            }
        }
    }

    private static void emitBasinVertex(VertexConsumer consumer, Matrix4f matrix, float[] edge, float reach,
            float gameTime, float basinSeed) {
        float x = BASIN_CENTRE + edge[0] * reach;
        float z = BASIN_CENTRE + edge[1] * reach;
        float u = 0.5F + edge[0] * reach / (2 * BASIN_APOTHEM);
        float v = 0.5F + edge[1] * reach / (2 * BASIN_APOTHEM);
        // Ripples settle toward the bowl wall, as the square pool's edge damping did.
        float edgeDamping = Math.min(1.0F, Math.max(0.0F, (1.0F - reach) * 0.5F / 0.18F));
        consumer.addVertex(matrix, x, basinWaveY(x, z, gameTime, basinSeed, edgeDamping), z)
                .setUv(u, v)
                .setColor(255, 255, 255, 230);
    }

    private static float basinWaveY(float x, float z, float gameTime, float basinSeed, float edgeDamping) {
        float phase = gameTime * BASIN_WAVE_SPEED + basinSeed * 0.017F;
        float waveA = (float) Math.sin((x * 18.0F + z * 9.0F) + phase);
        float waveB = (float) Math.sin((x * -11.0F + z * 16.0F) - phase * 0.73F);
        return BASIN_Y + (waveA * 0.68F + waveB * 0.32F) * BASIN_WAVE_HEIGHT * edgeDamping;
    }

    /** Matches the blockstate's y rotation of the north-facing model. */
    private static float rotationForFacing(Direction facing) {
        return switch (facing) {
            case SOUTH -> 180.0F;
            case WEST -> 90.0F;
            case EAST -> -90.0F;
            default -> 0.0F;
        };
    }
}
