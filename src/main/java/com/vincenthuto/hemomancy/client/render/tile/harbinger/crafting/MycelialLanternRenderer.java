package com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.common.block.harbinger.crafting.MycelialLanternBlock;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.MycelialLanternBlockEntity;
import com.vincenthuto.hutoslib.math.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Draws the Lantern's culture (or finished enzyme) turning inside the cloche and the stored blood
 * standing in the crock's sight glass. Positions mirror tools/model_export/mycelial_lantern.py.
 */
public class MycelialLanternRenderer implements BlockEntityRenderer<MycelialLanternBlockEntity> {
    private static final int DISPLAY_ITEM_MAX_LIGHT = 11;
    public static final ModelResourceLocation GAUGE_BLOOD =
            ModelResourceLocation.standalone(Hemomancy.rloc("block/mycelial_lantern_blood"));
    private static final float ITEM_Y = 13.25F / 16F;
    /** Gauge blood model: one pixel tall from the sight glass floor, stretched to the stored level. */
    private static final float GAUGE_FLOOR = 1.95F / 16F;
    private static final float GAUGE_DEPTH = 3.2F;

    private final ItemRenderer itemRenderer;

    public MycelialLanternRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public boolean shouldRenderOffScreen(MycelialLanternBlockEntity te) {
        return true;
    }

    @Override
    public void render(MycelialLanternBlockEntity te, float partialTick, PoseStack poseStack,
            MultiBufferSource buffers, int light, int overlay) {
        renderGaugeBlood(te, poseStack, buffers, light, overlay);
        ItemStack display = !te.getCultureStack().isEmpty() ? te.getCultureStack() : te.getOutputStack();
        if (!display.isEmpty()) {
            int itemLight = displayItemLight(te, light);
            renderDisplayItem(te, display, partialTick, poseStack, buffers, itemLight, overlay);
        }
    }

    private static void renderGaugeBlood(MycelialLanternBlockEntity te, PoseStack poseStack, MultiBufferSource buffers,
            int light, int overlay) {
        double max = te.getMaxBloodVolume();
        float fill = max <= 0 ? 0 : (float) Math.min(1, te.getBloodVolume() / max);
        if (fill <= 0) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        // Matches the blockstate's y rotation of the north-facing model.
        poseStack.mulPose(Axis.YP.rotationDegrees(switch (te.getBlockState().getValue(MycelialLanternBlock.FACING)) {
            case EAST -> -90; case SOUTH -> 180; case WEST -> 90; default -> 0;
        }));
        poseStack.translate(-0.5D, GAUGE_FLOOR, -0.5D);
        poseStack.scale(1, GAUGE_DEPTH * fill, 1);
        poseStack.translate(0, -GAUGE_FLOOR, 0);
        mc.getBlockRenderer().getModelRenderer().renderModel(poseStack.last(),
                buffers.getBuffer(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS)), null,
                mc.getModelManager().getModel(GAUGE_BLOOD), 1, 1, 1, light, overlay);
        poseStack.popPose();
    }

    private void renderDisplayItem(MycelialLanternBlockEntity te, ItemStack stack, float partialTick,
            PoseStack poseStack, MultiBufferSource buffers, int itemLight, int overlay) {
        float age = te.getLevel() != null ? AnimationTime.gameTime(te.getLevel(), partialTick) : partialTick;
        poseStack.pushPose();
        poseStack.translate(0.5D, ITEM_Y + Math.sin(age * 0.08F) * 0.02D, 0.5D);
        poseStack.mulPose(Vector3.YP.rotationDegrees(age * 2.0F).toMoj());
        poseStack.scale(0.6F, 0.6F, 0.6F);
        itemRenderer.renderStatic(stack, ItemDisplayContext.GROUND, itemLight, overlay, poseStack, buffers, te.getLevel(), 0);
        poseStack.popPose();
    }

    private static int displayItemLight(MycelialLanternBlockEntity te, int fallbackLight) {
        if (te.getLevel() == null) {
            return capPackedLight(fallbackLight, DISPLAY_ITEM_MAX_LIGHT);
        }
        int chamberLight = LevelRenderer.getLightColor(te.getLevel(), te.getBlockPos().above());
        return capPackedLight(chamberLight, DISPLAY_ITEM_MAX_LIGHT);
    }

    private static int capPackedLight(int packedLight, int maxLight) {
        int blockLight = (packedLight >> 4) & 0xF;
        int skyLight = (packedLight >> 20) & 0xF;
        return (Math.min(blockLight, maxLight) << 4) | (Math.min(skyLight, maxLight) << 20);
    }
}
