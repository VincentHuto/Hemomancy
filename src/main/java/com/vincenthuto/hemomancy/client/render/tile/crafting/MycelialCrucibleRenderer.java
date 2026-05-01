package com.vincenthuto.hemomancy.client.render.tile.crafting;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.common.tile.crafting.MycelialCrucibleBlockEntity;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

/**
 * Block entity renderer for the Mycelial Crucible.
 *
 * <p>Currently renders nothing — a custom model and texture will be added in a
 * future asset pass. The block is visible in-world through its block model JSON
 * in the meantime.
 */
public class MycelialCrucibleRenderer implements BlockEntityRenderer<MycelialCrucibleBlockEntity> {

    public MycelialCrucibleRenderer(BlockEntityRendererProvider.Context context) {
        // no model needed yet
    }

    @Override
    public void render(MycelialCrucibleBlockEntity te, float partialTick,
            PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        // Placeholder — animated particle effects handled in serverTick / clientTick
    }

    @Override
    public boolean shouldRenderOffScreen(MycelialCrucibleBlockEntity te) {
        return true;
    }
}
