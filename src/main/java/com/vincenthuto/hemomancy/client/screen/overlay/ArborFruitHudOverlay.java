package com.vincenthuto.hemomancy.client.screen.overlay;

import com.vincenthuto.hemomancy.client.event.ArborOfWillClientInteraction;
import com.vincenthuto.hutoslib.client.HLTextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/** Names the skill represented by the Arbor fruit under the crosshair. */
public final class ArborFruitHudOverlay {
    private static final double LABEL_REACH = 8.0D;

    private ArborFruitHudOverlay() { }

    public static void renderHUD(GuiGraphics graphics, int screenWidth, int screenHeight) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || minecraft.options.hideGui
                || minecraft.screen != null) return;
        ArborOfWillClientInteraction.Target target = ArborOfWillClientInteraction
                .findTarget(minecraft, LABEL_REACH, false).orElse(null);
        if (target == null) return;

        String rawName = target.skill().getName().replace("skill_", "").replace('_', ' ');
        Component name = Component.literal(HLTextUtils.toProperCase(rawName));
        int width = minecraft.font.width(name);
        int x = (screenWidth - width) / 2;
        int y = screenHeight / 2 + 15;
        int familyColor = target.skill().getBranchColor() & 0x00FFFFFF;
        graphics.fill(x - 5, y - 4, x + width + 5, y + 11, 0xB010080C);
        graphics.fill(x - 5, y - 4, x + width + 5, y - 2, 0xE0000000 | familyColor);
        graphics.renderOutline(x - 5, y - 4, width + 10, 15, 0x90000000 | familyColor);
        graphics.drawString(minecraft.font, name, x, y, 0xFFFFFFFF, true);
    }
}
