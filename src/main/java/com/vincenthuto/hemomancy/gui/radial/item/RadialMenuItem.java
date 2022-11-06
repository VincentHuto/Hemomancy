package com.vincenthuto.hemomancy.gui.radial.item;

import java.util.List;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.gui.radial.DrawingContext;
import com.vincenthuto.hemomancy.gui.radial.GenericRadialMenu;

import net.minecraft.network.chat.Component;

public abstract class RadialMenuItem
{
    private final GenericRadialMenu owner;
    private List<Component>  centralText;
    private boolean visible;
    private boolean hovered;

    protected RadialMenuItem(GenericRadialMenu owner)
    {
        this.owner = owner;
    }

    public abstract void draw(DrawingContext context);

    public abstract void drawTooltips(DrawingContext context);

    @Nullable
    public List<Component>  getCentralText()
    {
        return centralText;
    }

    public boolean isHovered()
    {
        return hovered;
    }

    public boolean isVisible()
    {
        return visible;
    }

    public boolean onClick()
    {
        return false;
    }

    public void setCentralText(@Nullable List<Component>  centralText)
    {
        this.centralText = centralText;
    }

    public void setHovered(boolean hovered)
    {
        this.hovered = hovered;
    }

    public void setVisible(boolean newVisible)
    {
        visible = newVisible;
        owner.visibilityChanged(this);
    }
}
