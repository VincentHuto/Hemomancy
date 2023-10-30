package com.vincenthuto.hemomancy.client.screen.radial;

import javax.annotation.Nullable;

import net.minecraft.network.chat.MutableComponent;

public abstract class RadialMenuItem
{
    private final GenericRadialMenu owner;
    private MutableComponent centralText;
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
    public MutableComponent getCentralText()
    {
        return centralText;
    }

    public void setCentralText(@Nullable MutableComponent centralText)
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
        // to be implemented by users
        return false;
    }
}
