package com.vincenthuto.hemomancy.gui.radial;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.ItemRenderer;

public interface IRadialMenuHost extends IDrawingHelper
{
    Font getFontRenderer();

    ItemRenderer getItemRenderer();

    Screen getScreen();
}
