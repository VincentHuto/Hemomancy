package com.vincenthuto.hemomancy.mixin.core;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ItemInHandRenderer.class)
public interface CastingHandRendererInvoker {
    @Invoker(value = "renderArmWithItem", remap = false)
    void hemomancy$renderCastingHand(AbstractClientPlayer player, float partial, float pitch,
            InteractionHand hand, float swing, ItemStack stack, float equipped,
            PoseStack poses, MultiBufferSource buffers, int light);
}
