package com.vincenthuto.hemomancy.gui.radial.item;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.gui.radial.DrawingContext;
import com.vincenthuto.hemomancy.gui.radial.GenericRadialMenu;

import net.minecraft.network.chat.Component;

public abstract class RadialMenuItem
{
    private final GenericRadialMenu owner;
    private Component centralText;
    private boolean visible;
    private boolean hovered;

    protected RadialMenuItem(GenericRadialMenu owner)
    {
        this.owner = owner;
    }

    public boolean isVisible()
    {
        return visible;
    }

    public void setVisible(boolean newVisible)
    {
        visible = newVisible;
        owner.visibilityChanged(this);
    }

    @Nullable
    public Component getCentralText()
    {
        return centralText;
    }

    public void setCentralText(@Nullable Component centralText)
    {
        this.centralText = centralText;
    }

    public boolean isHovered()
    {
        return hovered;
    }

    public void setHovered(boolean hovered)
    {
        this.hovered = hovered;
    }

    public abstract void draw(DrawingContext context);

    public abstract void drawTooltips(DrawingContext context);

    public boolean onClick()
    {
        return false;
    }
}
