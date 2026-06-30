package com.vincenthuto.hemomancy.client.screen.radial;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public class ItemStackRadialMenuItem extends TextRadialMenuItem
{
    private final int slot;
    private final ItemStack stack;
    private final List<Component> customTooltip;

    public int getSlot()
    {
        return slot;
    }

    public ItemStack getStack()
    {
        return stack;
    }

    public ItemStackRadialMenuItem(GenericRadialMenu owner, int slot, ItemStack stack, Component altText)
    {
        this(owner, slot, stack, altText, List.of());
    }

    public ItemStackRadialMenuItem(GenericRadialMenu owner, int slot, ItemStack stack, Component altText,
            List<Component> customTooltip)
    {
        super(owner, altText, 0x7FFFFFFF);
        this.slot = slot;
        this.stack = stack;
        this.customTooltip = List.copyOf(customTooltip);
    }

    @Override
    public void draw(DrawingContext context)
    {
        if (stack.getCount() > 0)
        {
            var viewModelPose = RenderSystem.getModelViewStack();
            viewModelPose.pushMatrix();
            viewModelPose.mul(context.graphics.pose().last().pose());
            viewModelPose.translate(-8, -8, context.z);
            RenderSystem.applyModelViewMatrix();
            context.graphics.renderItem(stack, (int) context.x, (int) context.y);
            context.graphics.renderItemDecorations(context.font, stack, (int) context.x, (int) context.y, "");
            viewModelPose.popMatrix();
            RenderSystem.applyModelViewMatrix();
        }
        else
        {
            super.draw(context);
        }
    }

    @Override
    public void drawTooltips(DrawingContext context)
    {
        if (!customTooltip.isEmpty())
        {
            context.graphics.renderTooltip(context.font, customTooltip, Optional.empty(), (int) context.x, (int) context.y);
            return;
        }
        if (stack.getCount() > 0)
        {
            context.graphics.renderTooltip(context.font, stack, (int) context.x, (int) context.y);
        }
        else
        {
            super.drawTooltips(context);
        }
    }
}
