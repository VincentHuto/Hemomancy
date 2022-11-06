package com.vincenthuto.hemomancy.gui.radial.item;

import com.vincenthuto.hemomancy.gui.radial.DrawingContext;
import com.vincenthuto.hemomancy.gui.radial.GenericRadialMenu;

import net.minecraft.network.chat.Component;

public class TextRadialMenuItem extends RadialMenuItem
{
    private final Component text;
    private final int color;

    public TextRadialMenuItem(GenericRadialMenu owner, Component text)
    {
        super(owner);
        this.text = text;
        this.color = 0xFFFFFFFF;
    }

    public TextRadialMenuItem(GenericRadialMenu owner, Component text, int color)
    {
        super(owner);
        this.text = text;
        this.color = color;
    }

    @Override
    public void draw(DrawingContext context)
    {
        String textString = text.getString();
        float x = context.x - context.fontRenderer.width(textString) / 2.0f;
        float y = context.y - context.fontRenderer.lineHeight / 2.0f;
        context.fontRenderer.drawShadow(context.matrixStack, textString, x, y, color);
    }

    @Override
    public void drawTooltips(DrawingContext context)
    {
        // nothing to do (yet)
    }

    public int getColor()
    {
        return color;
    }

    public Component getText()
    {
        return text;
    }
}
