package com.vincenthuto.hemomancy.gui.radial;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public interface IDrawingHelper
{
    void renderTooltip(PoseStack matrixStack, ItemStack stack, int mouseX, int mouseY);
    
    public void renderTooltip(PoseStack matrixStack, Component component, int mouseX, int mouseY);

}
